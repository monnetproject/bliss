/*********************************************************************************
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
package eu.monnetproject.translation.topics.newlda;

import eu.monnetproject.math.sparse.Integer2DoubleVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.translation.topics.SimilarityMetric;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author John McCrae
 */
public class LDASimilarityMetric implements SimilarityMetric {
/**
     * Number of topics
     */
    private final int K;
    /**
     * Number of words in vocabulary
     */
    private final int W;
    /**
     * Counts
     */
    private final int[][][] N_lkw;
    private final int[][] N_lk;
    private final double alpha,beta;
    private final double[] P;

    public LDASimilarityMetric(int K, int W, int[][][] N_lkw, int[][] N_lk, double alpha, double beta) {
        this.K = K;
        this.W = W;
        this.N_lkw = N_lkw;
        this.N_lk = N_lk;
        this.alpha = alpha;
        this.beta = beta;
        P = new double[K];
    }
    
    @Override
    public Vector<Double> simVecSource(Vector<Integer> termVec) {
        return simVec(termVec,0);
    }

    @Override
    public Vector<Double> simVecTarget(Vector<Integer> termVec) {
        return simVec(termVec, 1);
    }

    @Override
    public int W() {
        return W;
    }

    private final Random random = new Random();
    
    private Vector<Double> simVec(Vector<Integer> termVec, int l) {
        final int n = termVec.sum();
        final int[] x = new int[n];
        final int[] z = new int[n];
        final int[] N_k = new int[K];
        final int[][] N_wk = new int[W][K];
        int j = 0;
        for(Map.Entry<Integer,Integer> e : termVec.entrySet()) {
            for(int i = 0; i < e.getValue(); i++) {
                final int k = random.nextInt(K);
                x[j] = e.getKey()-1;
                z[j] = k;
                N_k[k]++;
                N_wk[e.getKey()-1][k]++;
                j++;
            }
        }
        for(int iter = 0; iter < 100; iter++) {
            for(int i = 0; i < n; i++) {
                final int oldK = z[i];
                final int k = sample(x[i], l, oldK, N_k, N_wk);
                N_k[oldK]--;
                N_wk[x[i]][oldK]--;
                z[i] = k;
                N_wk[x[i]][k]++;
                N_k[k]++;
            }
        }
        return new Integer2DoubleVector(SparseIntArray.histogram(z, K));
    }

    
    private int sample(int w, int l, int prevK, int[] N_k, int[][] N_wk) {
        double u = random.nextDouble();
        double sum = 0.0;
        for (int k = 0; k < K; k++) {
            final int dec = prevK == k ? 1 : 0;
            P[k] = a_kj(k, N_k, dec) * b_lwk(l, w, k, N_wk, dec) / c_lk(l, k, N_k, dec);
            assert (P[k] >= 0);
            sum += P[k];
        }
        for (int k = 0; k < K; k++) {
            if (u < (P[k] / sum)) {
                return k;
            }
            P[k + 1] += P[k];
        }
        throw new RuntimeException("P[K] = " + P[K] + " sum= " + sum);
    }


    private double a_kj(int k, int[] N_k, int dec) {
        return (double) N_k[k] + alpha - dec;
    }

    private double b_lwk(int l, int w, int k, int[][] N_wk, int dec) {
        return ((double) N_lkw[l][k][w] + N_wk[w][k] + beta - dec);
    }

    private double c_lk(int l, int k, int[] N_k, int dec) {
        return ((double) N_lk[l][k] + N_k[k] + W * beta - dec);
    }

}
