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
import eu.monnetproject.translation.topics.CLIOpts;
import eu.monnetproject.translation.topics.SparseArray;
import eu.monnetproject.translation.topics.WordMap;
import eu.monnetproject.translation.topics.sim.BetaLMImpl;
import eu.monnetproject.translation.topics.sim.BetaSimFunction;
import eu.monnetproject.translation.topics.sim.Metrics;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
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
            try {
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
            } catch (EOFException x) {
                System.err.println("EOF");
                break;
            }
        }
        return counter.counts();
    }

    public void writeModel(PrintStream out, String[] inverseWordMap, WeightedNGramCountSet countSet) {
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
                if(i == 1) {
                    p = Math.log10(p / l);
                } else {
                    double p2 = countSet.ngramCount(i-1).getDouble(entry.getKey().history());
                    p = Math.log10(p / p2);
                }
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
        out.flush();
        out.close();
    }
    
    public void writeModel(PrintWriter out, String[] inverseWordMap, WeightedNGramCountSet countSet) {
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
        final CLIOpts opts = new CLIOpts(_args);


        final ArrayList<String> args = new ArrayList<String>(Arrays.asList(_args));

        final SourceType sourceType = opts.enumOptional("t", SourceType.class, SourceType.SIMPLE, "The type of source: SIMPLE, INTERLEAVED_USE_FIRST or INTERLEAVED_USE_SECOND");

        StringBuilder betalmString = new StringBuilder("The BetaLM method: ");
        for (BetaLMImpl.Method method : BetaLMImpl.Method.values()) {
            betalmString.append(method.name()).append(" ");
        }
        final BetaLMImpl.Method betaMethod = opts.enumOptional("b", BetaLMImpl.Method.class, null, betalmString.toString());
        
        final File queryFile = opts.roFile("f", "The query file (ontology)", null);
        
        final double smoothness = opts.doubleValue("s", 1.0, "The smoothing parameter");
        
        final File inFile = opts.roFile("corpus[.gz|.bz2]", "The corpus file");
        
        final int N = opts.nonNegIntValue("N", "The largest n-gram to calculate");
        
        final File wordMapFile = opts.roFile("wordMap", "The word map file");
        
        final int W = opts.nonNegIntValue("W", "The number of distinct tokens in corpus");

        final PrintStream out = opts.outFileOrStdout();
        
        if(!opts.verify(CompileLanguageModel.class)) {
            return;
        }
        
        final CompileLanguageModel compiler = betaMethod == null
                ? new CompileLanguageModel() : new CompileBetaModel();

        if (queryFile != null && (!queryFile.exists() || !queryFile.canRead())) {
            fail("Cannot read query file");
        }
        final BetaSimFunction betaSimFunction;
        if (betaMethod != null) {
            final PrecomputedValues precomp;
            if (PrecomputedValues.isNecessary(betaMethod)) {
                System.err.println("Methodology requires pre-scan of the corpus");
                precomp = PrecomputedValues.precompute(inFile, W, sourceType);
            } else {
                precomp = null;
            }
            if (queryFile == null) {
                fail("BetaLM does not work without a query");
            }
            final SparseArray binQuery = SparseArray.fromBinary(queryFile, Integer.MAX_VALUE);
            if (smoothness == 1.0) {
                betaSimFunction = betaSimFunction(betaMethod, binQuery, precomp);
            } else {
                betaSimFunction = Metrics.selective(betaSimFunction(betaMethod, binQuery, precomp), smoothness);
            }

        } else {
            betaSimFunction = null;
        }

        System.err.println("Compiling corpus");
        final WeightedNGramCountSet countSet;
        if (betaMethod == null) {
            countSet = compiler.doCount(N, new IntegerizedCorpusReader(new DataInputStream(CLIOpts.openInputAsMaybeZipped(inFile))), sourceType).asWeightedSet();
        } else {
            countSet = ((CompileBetaModel) compiler).doCount(N, new IntegerizedCorpusReader(new DataInputStream(CLIOpts.openInputAsMaybeZipped(inFile))), sourceType, betaSimFunction);
        }
        System.err.print("Loading word map:");
        final String[] wordMap = WordMap.inverseFromFile(wordMapFile, W, true);
        System.err.println("Writing model");
        compiler.writeModel(out, wordMap, countSet);
        out.flush();
        out.close();
    }
}
