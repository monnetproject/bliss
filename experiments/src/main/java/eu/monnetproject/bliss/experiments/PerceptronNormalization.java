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
package eu.monnetproject.bliss.experiments;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.ParallelBinarizedReader;
import eu.monnetproject.bliss.WordMap;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.SparseRealArray;

/**
 *
 * @author John McCrae
 */
public class PerceptronNormalization {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File corpus = opts.roFile("corpus", "The corpus");
        final File wordMapFile = opts.roFile("wordMap", "The word map");
        final int J = opts.intValue("J", "The number of documents to handle");
        final PrintStream out = opts.outFileOrStdout();
        if (!opts.verify(PerceptronNormalization.class)) {
            return;
        }
        final int W = WordMap.calcW(wordMapFile);
        final double[][] wts = new double[W][2];
        for (int l = 0; l < 2; l++) {
            for (int w = 1; w < W; w++) {
                wts[w][l] = 1.0;
            }
        }

        final ParallelBinarizedReader slowIn = new ParallelBinarizedReader(
                CLIOpts.openInputAsMaybeZipped(corpus));
        for (int j = 0; j < J; j++) {
            final ParallelBinarizedReader fastIn = new ParallelBinarizedReader(
                    CLIOpts.openInputAsMaybeZipped(corpus));
            final SparseIntArray[] doc1 = slowIn.nextFreqPair(W);

            for (int j2 = 0; j2 < J; j2++) {
                final SparseIntArray[] doc2 = fastIn.nextFreqPair(W);
                if (j != j2) {
                    for (int l = 0; l < 2; l++) {
                        double o1 = 0.0, o2 = 0.0;
                        final SparseRealArray a = new SparseRealArray(W),
                                c = new SparseRealArray(W);
                        for (int w : doc2[l].keySet()) {
                            final double a1 = doc2[l].doubleValue(w) * doc1[l].doubleValue(w);
                            o1 += a1 * wts[w][l];
                            final double c1 = doc2[l].doubleValue(w) * doc2[l].doubleValue(w);
                            o2 += c1 * wts[w][l];
                            a.add(w, a1);
                            c.add(w, c1);
                        }
                        if (o2 != 0.0) {
                            double objective = Math.abs(o1 / o2);
                            if (!Double.isInfinite(objective)) {
                                for (int w : doc2[l].keySet()) {
                                    double delta = deltaValue(w, l, wts, a.doubleValue(w), c.doubleValue(w), o1, o2) * objective * wts[w][l];
                                    // HACK: stop the vector varying too radically
                                    if(delta > 0.1 || delta < -0.1) {
                                        delta = Math.signum(delta) * 0.1;
                                    }
                                    wts[w][l] += delta;
                                }
                                double norm = 0.0;
                                for (int w = 1; w < W; w++) {
                                    norm += wts[w][l] * wts[w][l];
                                }
                                norm = Math.sqrt(norm);
                                if (norm == 0.0 || Double.isNaN(norm)) {
                                    throw new RuntimeException("Zero'ed the vector! " + norm);
                                }
                                for (int w = 1; w < W; w++) {
                                    wts[w][l] /= norm;
                                }
                            }
                        }
                    }
                }
            }
            fastIn.close();
            System.err.print(".");
        }
        System.err.println();
        slowIn.close();
        for (int w = 1; w < W; w++) {
            out.println(wts[w][0] + "," + wts[w][1]);
        }
        out.flush();
        out.close();
    }

    private static double deltaValue(int w, int l, double[][] wts, double a, double c, double o1, double o2) {
        final double b = o1 - a * wts[w][l];
        final double d = o2 - c * wts[w][l];

        if (o2 == 0.0) {
            throw new RuntimeException("This shouldn't happen");
        }

        if (Math.abs(c * wts[w][l] + d) > 1e-30) {
            return Math.signum(o1 / o2) / (c * wts[w][l] + d) / (c * wts[w][l] + d) * (c * b - a * d);
        } else {
            return 0.0;
        }
    }
}
