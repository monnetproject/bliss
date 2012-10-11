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
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

/**
 *
 * @author John McCrae
 */
public class KneserNeySmoothing implements NGramScorer {

    private final NGramHistories histories;
    private final double[][] d;
    private final int N;

    public KneserNeySmoothing(NGramHistories histories, int[][] CoC, int N) {
        this(histories, CoC, N, 3);
    }

    public KneserNeySmoothing(NGramHistories histories, int[][] CoC, int N, int D) {
        this.histories = histories;
        this.N = N;
        this.d = new double[CoC.length/*don't snigger*/][D];
        for (int i = 0; i < CoC.length; i++) {
            double y = (double) CoC[i][0] / (double) (CoC[i][0] + 2 * CoC[i][1]);
            assert (CoC[i].length > D + 1);
            for (int j = 0; j < D; j++) {
                d[i][j] = j + 1 - (j + 2) * CoC[i][j + 1] / CoC[i][j];
            }
        }
    }

    @Override
    public double[] ngramScores(NGram nGram, WeightedNGramCountSet countSet) {
        final int n = nGram.ngram.length;
        final double c = countSet.ngramCount(n).getDouble(nGram);
        final double l = countSet.sum(nGram.history());
        int ci = Math.min((int) Math.ceil(c), d[n].length - 1);
        if (n == N) {
            return new double[]{(c - d[n][ci]) / l};
        } else {
            final double[] history = histories.histories(n).get(nGram);
            assert (history != null);
            final int H = history.length / 2;
            double p = 0.0;
            for (int i = H; i < 2 * H; i++) {
                p += history[i];
            }
            p -= d[n][ci];
            p /= sumHistory(nGram, H);
            
            double bo = 0.0;
            for(int i = 0; i < d[n].length; i++) {
                bo += d[n][i] * history[i];
            }
            
            bo /= countSet.sum(nGram);
            
            return new double [] { p, bo };
        }
    }

    private double sumHistory(NGram nGram, int H) {
        double s = 0.0;
        final ObjectIterator<Entry<NGram, double[]>> iterator = histories.histories(nGram.ngram.length).object2ObjectEntrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<NGram, double[]> e = iterator.next();
            if (e.getKey().future().equals(nGram)) {
                for (int i = H; i < 2 * H; i++) {
                    s += e.getValue()[i];
                }
            }
        }
        return s;
    }
}
