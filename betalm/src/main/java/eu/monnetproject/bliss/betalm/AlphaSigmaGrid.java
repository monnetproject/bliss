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
package eu.monnetproject.bliss.betalm;

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.WordMap;
import eu.monnetproject.bliss.betalm.impl.ARPALM;
import eu.monnetproject.bliss.betalm.impl.BetaLMImpl;
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
        final CLIOpts opts = new CLIOpts(args);
        final File inFile = opts.roFile("trainFile", "The training corpus");
        final File queryFile = opts.roFile("queryFile", "The file to adapt to");
        final File freqFile = opts.roFile("freqFile", "The frequency file");
        final File wordMapFile = opts.roFile("wordMap", "The word map");
        final int N = opts.nonNegIntValue("N", "The largetst n-gram to consider");
        final File testDoc = opts.roFile("test.txt", "The test document to evaluate on");
        
        if(!opts.verify(AlphaSigmaGrid.class)) {
            return;
        }
        
        final int W = WordMap.calcW(wordMapFile);

        final double alphaStep = 0.05, alphaMax = 0.5, sigmaStep = 1.0, sigmaMax = 10.0;
        final double[][] perplexity = new double[(int) Math.round(1.0 / alphaStep)][(int) Math.round(sigmaMax / sigmaStep) + 1];



        PrintWriter out = new PrintWriter("alpha-sigma-results");

        for (double sigma = 0.0; sigma <= sigmaMax; sigma += sigmaStep) {
            out.print("\"" + sigma + "\"");
            if (sigma + sigmaStep <= sigmaMax) {
                out.print(",");
            } else {
                out.println();
            }
        }

        System.err.print("Loading word map:");
        final String[] wordMap = WordMap.inverseFromFile(wordMapFile, W, true);

        for (double alpha = 0.0; alpha < alphaMax; alpha += alphaStep) {
            for (double sigma = 0.0; sigma <= sigmaMax; sigma += sigmaStep) {
                final File tmpFile = File.createTempFile("lmlmlm", ".en");
                {
                    final PrintStream out2 = new PrintStream(tmpFile);
                    
                    CompileModel.compile(wordMapFile, freqFile, 150, BetaLMImpl.Method.COS_SIM, inFile, SourceType.FIRST, queryFile, sigma, -1, alpha, N, false, out2);
                    
                    out2.flush();
                    out2.close();
                    DeleteFileOnExit.clearNow();
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
            out.println(perplexStr.substring(1, perplexStr.length() - 1));
        }
        out.close();

    }
}
