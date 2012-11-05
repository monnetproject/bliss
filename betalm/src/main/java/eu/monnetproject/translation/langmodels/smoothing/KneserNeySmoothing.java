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
package eu.monnetproject.translation.langmodels.smoothing;

import eu.monnetproject.translation.langmodels.NGram;
import eu.monnetproject.translation.langmodels.WeightedNGramCountSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Arrays;
import java.util.WeakHashMap;

/**
 * Smoothing by means of the modified Kneser-Ney method
 *
 * See Koehn: "Statistical Machine Translation", pp. 201--203
 *
 * @author John McCrae
 */
public class KneserNeySmoothing implements NGramScorer {

    private static final double MIN_PROB = Double.parseDouble(System.getProperty("lm.smooth.minprob", "1e-10"));
    private final NGramHistories histories;
    private final double[][] d;
    private final int N;
    private final int v2;

    public KneserNeySmoothing(NGramHistories histories, int[][] CoC, int N) {
        this(histories, CoC, N, 3);
    }

    public KneserNeySmoothing(NGramHistories histories, int[][] CoC, int N, int D) {
        this(histories,CoC,N,D,ones(N));
    }
    
    private static double[] ones(int N) {
        final double[] arr = new double[N];
        Arrays.fill(arr, 1.0);
        return arr;
    }
    
    public KneserNeySmoothing(NGramHistories histories, int[][] CoC, int N, int D, double[] r) {
        this.histories = histories;
        this.N = N;
        this.d = new double[CoC.length/*don't snigger*/][D];
        for (int i = 0; i < CoC.length; i++) {
            double y = (double) CoC[i][0] / (double) (CoC[i][0] + 2 * CoC[i][1]);
            System.err.println("y=" + y);
            assert (CoC[i].length > D);
            System.err.println("CoC[" + (i+1) + "][0]=" + CoC[i][0]);
            for (int j = 0; j < D; j++) {
                System.err.println("CoC[" + (i+1) + "][" + (j+1) + "]=" + CoC[i][j + 1]);
                if (CoC[i][j] != 0) {
                    d[i][j] = r[i] * ((double) j + 1.0 - (y * (j + 2) * CoC[i][j + 1]) / (double) CoC[i][j]);
                    System.err.println("d_" + (i+1) + "[" + (j+1) + "]=" + d[i][j]);
                }
            }
        }
        int v2tmp = 0;
        for (int i = 0; i < CoC[1].length; i++) {
            v2tmp += CoC[1][i];
        }
        this.v2 = v2tmp;
    }

    private static double log10(double d) {
        if (d <= 0 || Double.isNaN(d)) {
            return Math.log10(MIN_PROB);
        } else {
            return Math.log10(d);
        }
    }

    //private static final NGram RIGOROUSLY = new NGram(new int[] { 30411 });
    
    @Override
    public double[] ngramScores(NGram nGram, WeightedNGramCountSet countSet) {
        final int n = nGram.ngram.length;
        final double c = countSet.ngramCount(n).getDouble(nGram);
        int ci = Math.min((int) Math.ceil(c) - 1, d[n - 1].length - 1);
        if (n == N) {
            final double l = countSet.sum(nGram.history());
            return new double[]{log10((c - d[n - 1][ci]) / l)};
        } else {
            final double[] history = histories.histories(n).get(nGram);
            if(history == null) {
                // Can happen... shouldn't but can
                return new double[]{log10((c - d[n - 1][ci]) / countSet.sum(nGram.history()))};
            }
            final int H = history.length / 2;
            double p = 0.0;
            for (int i = H + 1; i < 2 * H + 1; i++) {
                p += history[i];
            }
            p -= d[n - 1][ci];
            final double sh = sumHistory(nGram.history(), H);
            if (sh != 0.0) {
                p /= sumHistory(nGram.history(), H);
            } else {
                // This is probably wrong... perhaps 1.0?
                // There are no summed history as this token
                // only occurred at the end of sentences??
                p = MIN_PROB;
            }

            double bo = 0.0;
            for (int i = 0; i < d[n - 1].length; i++) {
                // Yes this should be a small n... but a big N is much closer
                // to the results of SRILM :s
                bo += d[N - 1][i] * history[i + 1];
            }

            bo /= countSet.sum(nGram);

            if (bo < 1) {
                return new double[]{log10(p), log10(bo)};
            } else {
                return new double[]{log10(p)};
            }
        }
    }
    
    private double sumHistory(NGram nGram, int H) {
        if (nGram.ngram.length > 0) {
            final Object2ObjectMap<NGram, double[]> hists = histories.histories(nGram.ngram.length);
            if (hists != null) {
                if (hists.containsKey(nGram)) {
                    return hists.get(nGram)[0];
                } else {
                    System.err.println("Missing n-gram " + nGram);
                }
            } else {
                System.err.println("No history!");
            }
            return 0.0;
        } else {
            return v2;
        }
    }
}
