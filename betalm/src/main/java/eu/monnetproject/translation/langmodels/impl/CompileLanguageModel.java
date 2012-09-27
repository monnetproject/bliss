/**
 * *******************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *******************************************************************************
 */
package eu.monnetproject.translation.langmodels.impl;

import eu.monnetproject.translation.langmodels.LossyCounter;
import eu.monnetproject.translation.langmodels.NGram;
import eu.monnetproject.translation.langmodels.NGramCountSet;
import eu.monnetproject.translation.langmodels.WeightedNGramCountSet;
import eu.monnetproject.translation.topics.SparseArray;
import eu.monnetproject.translation.topics.WordMap;
import eu.monnetproject.translation.topics.sim.BetaLMImpl;
import eu.monnetproject.translation.topics.sim.BetaSimFunction;
import eu.monnetproject.translation.topics.sim.Metrics;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class CompileLanguageModel {

    public enum SourceType {
        SIMPLE,
        INTERLEAVED_USE_FIRST,
        INTERLEAVED_USE_SECOND
    };

    public NGramCountSet doCount(int N, IntegerizedCorpusReader reader, SourceType type) throws IOException {
        final LossyCounter counter = new LossyCounter(N);
        long read = 0;
        boolean inDoc = type != SourceType.INTERLEAVED_USE_SECOND;
        while (reader.hasNext()) {
            final int tk = reader.nextToken();
            if (tk == 0) {
                if (type == SourceType.SIMPLE) {
                    counter.docEnd();
                } else {
                    if (inDoc) {
                        counter.docEnd();
                        inDoc = false;
                    } else {
                        inDoc = true;
                    }
                }
            } else if (inDoc) {
                counter.offer(tk);
            }
            if (++read % 1048576 == 0) {
                System.err.print(".");
            }
        }
        return counter.counts();
    }

    public void writeModel(PrintWriter out, WordMap wordMap, WeightedNGramCountSet countSet) {
        final String[] inverseWordMap = wordMap.invert();
        out.println("\\data\\");
        for (int i = 1; i <= countSet.N(); i++) {
            out.println("ngram " + i + "=" + countSet.ngramCount(i).size());
        }
        out.println();
        for (int i = 1; i <= countSet.N(); i++) {
            double l = countSet.sum(i);
            out.println("\\" + i + "-grams:");
            for (Object2DoubleMap.Entry<NGram> entry : countSet.ngramCount(i).object2DoubleEntrySet()) {
                double p = entry.getDoubleValue();
                p = Math.log10(p / l);
                out.print(p + "\t");
                final int[] ng = entry.getKey().ngram;
                for (int j = 0; j < ng.length; j++) {
                    out.print(inverseWordMap[ng[j]]);
                    if (j + 1 != ng.length) {
                        out.print(" ");
                    }
                }
                out.println();
            }
            out.println();
        }
        out.println("\\end\\");
    }

    private static void fail(String message) {
        System.err.println(message);
        System.err.println();
        System.err.println("Usage:");
        System.err.println("\tmvn exec:java -Dexec.MainClass=\"" + CompileLanguageModel.class.getName() + "\" -Dexec.args=\"[-t sourceType] [-b betaFunction -f queryFile] [-s selectivity] in N wordMap out\"");
    }

    private static BetaSimFunction betaSimFunction(BetaLMImpl.Method method, SparseArray query, PrecomputedValues precomp) {
        switch (method) {
            case COS_SIM:
                return Metrics.cosSim(query);
            case DF_DICE:
                return Metrics.dfDiceCoefficient(query, precomp.df);
            case DF_JACCARD:
                return Metrics.dfJaccardCoefficient(query, precomp.df);
            case DICE:
                return Metrics.diceCoefficient(query);
            case JACCARD:
                return Metrics.jaccardIndex(query);
            case KLD:
                return Metrics.kullbackLeiblerDivergence(query);
            case NORMAL_COS_SIM:
                return Metrics.normalCosSim(query, precomp.mu, precomp.sumMu2);
            case ROGERS_TANIMOTO:
                return Metrics.rogersTanimoto(query);
            case WxWCLESA:
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static void main(String[] _args) throws Exception {
        final ArrayList<String> args = new ArrayList<String>(Arrays.asList(_args));

        SourceType sourceType = SourceType.SIMPLE;
        BetaLMImpl.Method betaMethod = null;
        File queryFile = null;
        double selectivity = 1.0;
        for (int i = 0; i < args.size() - 1; i++) {
            if (args.get(i).equals("-t")) {
                try {
                    sourceType = SourceType.valueOf(args.get(i + 1));
                } catch (IllegalArgumentException x) {
                    fail("Bad source type: please use SIMPLE, INTERLEAVED_USE_FIRST or INTERLEAVED_USE_SECOND");
                    return;
                }
                args.remove(i);
                args.remove(i--);
            } else if (args.get(i).equals("-b")) {
                try {
                    betaMethod = BetaLMImpl.Method.valueOf(args.get(i + 1));
                } catch(IllegalArgumentException x) {
                    StringBuilder errMsg = new StringBuilder("Bad BetaLM method, choose one of: ");
                    for(BetaLMImpl.Method method : BetaLMImpl.Method.values()) {
                        errMsg.append(method.name()).append(" ");
                    }
                    fail(errMsg.toString());
                }
                args.remove(i);
                args.remove(i--);
            } else if (args.get(i).equals("-f")) {
                queryFile = new File(args.get(i + 1));
                args.remove(i);
                args.remove(i--);
            } else if (args.get(i).equals("-s")) {
                try {
                    selectivity = Double.parseDouble(args.get(i+1));
                } catch(NumberFormatException x) {
                    fail("Bad value for selectivity (not a number)");
                }
                args.remove(i);
                args.remove(i--);
            }
        }
        if (args.size() != 4) {
            fail("Wrong number of arguments");
        }

        final File inFile = new File(args.get(0));
        if (!inFile.exists() || !inFile.canRead()) {
            fail("Cannot access corpus file");
        }
        final int N;
        try {
            N = Integer.parseInt(args.get(1));
        } catch (NumberFormatException x) {
            fail("Non-number for N");
            return;
        }
        if (N <= 0) {
            fail("Non-positive N value");
        }
        final File wordMapFile = new File(args.get(2));
        if (!wordMapFile.exists() || !wordMapFile.canRead()) {
            fail("Cannot access word map file");
        }
        final File outFile = new File(args.get(3));
        if (outFile.exists() && !outFile.canWrite()) {
            fail("Cannot write to out file");
        }
        final CompileLanguageModel compiler = betaMethod == null
                ? new CompileLanguageModel() : new CompileBetaModel();
        
        if(queryFile != null && (!queryFile.exists() || !queryFile.canRead())) {
            fail("Cannot read query file");
        }
        final WordMap wordMap = WordMap.fromFile(wordMapFile);
        final BetaSimFunction betaSimFunction;
        if(betaMethod != null) {
            final PrecomputedValues precomp;
            if(PrecomputedValues.isNecessary(betaMethod)) {
                System.err.println("Methodology requires pre-scan of the corpus");
                precomp = PrecomputedValues.precompute(inFile,wordMap.size()+1,sourceType);
            } else {
                precomp = null;
            }
            if(queryFile == null) {
                fail("BetaLM does not work without a query");
            }
            final SparseArray binQuery = SparseArray.fromBinary(queryFile, Integer.MAX_VALUE);
            if(selectivity == 1.0) {
                betaSimFunction = betaSimFunction(betaMethod, binQuery, precomp); 
            } else{
                betaSimFunction = Metrics.selective(betaSimFunction(betaMethod, binQuery, precomp), selectivity);
            }
           
        } else {
            betaSimFunction = null;
        }
        
        System.err.println("Compiling corpus");
        final WeightedNGramCountSet countSet;
        if (betaMethod == null) {
            countSet = compiler.doCount(N, new IntegerizedCorpusReader(new DataInputStream(new FileInputStream(inFile))), sourceType).asWeightedSet();
        } else {
            countSet = ((CompileBetaModel) compiler).doCount(N, new IntegerizedCorpusReader(new DataInputStream(new FileInputStream(inFile))), sourceType, betaSimFunction);
        }
        final PrintWriter out = new PrintWriter(outFile);
        System.err.println("Writing model");
        compiler.writeModel(out, wordMap, countSet);
        out.flush();
        out.close();
    }
}
