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
package eu.monnetproject.translation.langmodels;

import eu.monnetproject.translation.langmodels.impl.ARPALM;
import eu.monnetproject.translation.langmodels.impl.CompileBetaModel;
import eu.monnetproject.translation.langmodels.impl.CompileLanguageModel;
import eu.monnetproject.translation.langmodels.impl.IntegerizedCorpusReader;
import eu.monnetproject.translation.langmodels.smoothing.NGramScorer;
import eu.monnetproject.translation.topics.CLIOpts;
import eu.monnetproject.translation.topics.SparseArray;
import eu.monnetproject.translation.topics.WordMap;
import eu.monnetproject.translation.topics.sim.BetaLMImpl;
import eu.monnetproject.translation.topics.sim.BetaSimFunction;
import eu.monnetproject.translation.topics.sim.Metrics;
import java.io.DataInputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author John McCrae
 */
public class AlphaSigmaGrid {

    //../wiki/en-es/ifrs.es.gz ../wiki/en-es/benrmp.train.gz 3 ../wiki/en-es/benrmp.wordMap 349513 ../wiki/en-es/benrmp.beta-cos2-nosm.lm.en && ./perplexity ../wiki/en-es/ifrs.en.txt ../wiki/en-es/benrmp.beta-cos2-nosm.lm.en 
    public static void main(String[] args) throws Exception {
        final File inFile = new File("../wiki/en-es/benrmp.train.gz");
        final File queryFile = new File("../wiki/en-es/ifrs.es.gz");
        final File wordMapFile = new File("../wiki/en-es/benrmp.wordMap");
        final File testDoc = new File("../wiki/en-es/ifrs.en.txt");
        final int W = 349513;

        final double alphaStep = 0.1, sigmaStep = 1.0, sigmaMax = 10.0;
        final double[][] perplexity = new double[(int)Math.round(1.0/alphaStep)][(int)Math.round(sigmaMax/sigmaStep)+1];

        
        
        PrintWriter out = new PrintWriter("alpha-sigma-results");
        
            for (double sigma = 0.0; sigma <= sigmaMax; sigma += sigmaStep) {
                out.print("\""+sigma + "\"");
                if(sigma + sigmaStep <= sigmaMax) {
                    out.print(",");
                } else {
                    out.println();
                }
            }

        for (double alpha = 0.0; alpha < 1.0; alpha += alphaStep) {
            System.setProperty("betalm.alpha", "" + alpha);
            for (double sigma = 0.0; sigma <= sigmaMax; sigma += sigmaStep) {
                final File tmpFile = File.createTempFile("lmlmlm", ".en");
                {
                    System.err.print("Loading word map:");
                    final String[] wordMap = WordMap.inverseFromFile(wordMapFile, W, true);

                    final CompileLanguageModel.SourceType sourceType = CompileLanguageModel.SourceType.INTERLEAVED_USE_FIRST;
                    final BetaLMImpl.Method betaMethod = BetaLMImpl.Method.DICE;
                    final SparseArray binQuery = SparseArray.fromBinary(CLIOpts.openInputAsMaybeZipped(queryFile), Integer.MAX_VALUE);
                    final BetaSimFunction betaSimFunction = Metrics.selective(CompileLanguageModel.betaSimFunction(betaMethod, binQuery, null), sigma);
                    final CompileLanguageModel.Smoothing smoothing = CompileLanguageModel.Smoothing.NONE;

                    final CompileBetaModel compiler = new CompileBetaModel();
                    final int N = 3;
                    System.err.print("Counting corpus");
                    final WeightedNGramCountSet countSet;
                    countSet = compiler.doCount(N, new IntegerizedCorpusReader(new DataInputStream(CLIOpts.openInputAsMaybeZipped(inFile))), sourceType, betaSimFunction, smoothing);


                    final NGramScorer scorer = CompileLanguageModel.getScorer(smoothing, countSet, compiler.histories);

                    System.err.print("Writing model");
                    final PrintStream out2 = new PrintStream(tmpFile);
                    compiler.writeModel(out2, wordMap, countSet, scorer);
                    out2.flush();
                    out2.close();
                }
                {

                    final ARPALM lm = new ARPALM(tmpFile);

                    final Scanner scanner = new Scanner(testDoc);

                    perplexity[(int) alpha * 10][(int) sigma] = Perplexity.calculatePerplexity(scanner, lm);
                    System.err.println("alpha=" + alpha + ";sigma= " + sigma + ";perplexity=" + perplexity[(int) alpha * 10][(int) sigma]);
                }
                tmpFile.delete();
            }
            final String perplexStr = Arrays.toString(perplexity[(int) alpha * 10]);
            out.println(perplexStr.substring(1, perplexStr.length()-1));
        }
        out.close();

    }
}
