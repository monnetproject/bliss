/**
 * ********************************************************************************
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
package eu.monnetproject.translation.topics.lda;

import static java.lang.Math.*;

/**
 *
 * @author John McCrae
 */
public class FastLDA extends GibbsInference {

    /**
     * a[j] = sqrt(sum_K((N_kj + alpha)^ 2))
     */
    private final double[] a;
    /**
     * b[l][w] = sqrt(sum_K((N_lwk + beta)^ 2))
     */
    private final double[][] b;
    /**
     * c[l] = min_K(N_lk + W * beta)
     */
    private final double c[];
    /**
     * Per document topic sort
     */
    private final int[] mapk;

    public FastLDA(GibbsInput input,int K) {
        super(input,K);
        a = new double[D];
        b = new double[L][W];
        c = new double[L];
        mapk = new int[K];
        initFastLDA();
    }

    public FastLDA(int K, int D, int W, int[] DN, int[][] x) {
        super(K, D, W, DN, x);
        a = new double[D];
        b = new double[L][W];
        c = new double[L];
        mapk = new int[K];
        initFastLDA();
    }

    public FastLDA(int K, int D, int W, int L, int[] DN, int[][] x, int[] m, int[][] mu) {
        super(K, D, W, L, DN, x, m, mu);
        a = new double[D];
        b = new double[L][W];
        c = new double[L];
        mapk = new int[K];
        initFastLDA();
    }

    private void initFastLDA() {
        for (int j = 0; j < D; j++) {
            a[j] = sqrt((DN[j] + alpha) * (DN[j] + alpha) + (K - 1) * alpha * alpha);
        }
        for (int l = 0; l < L; l++) {
            for (int w = 0; w < W; w++) {
                b[l][w] = sqrt((N_lkw[l][0][w] + beta) * (N_lkw[l][0][w] + beta) + (K - 1) * beta * beta);
            }
            c[l] = W * beta;
        }
    }

    @Override
    protected void assignZ(int j, int i, int k) {
        final int oldK = z[j][i];
        if (k != oldK) {
            final double oldA1 = a_kj(oldK, j, 0);
            final double oldA2 = a_kj(k, j, 0);
            final double oldB1 = b_lwk(m[j], x[j][i], oldK, 0);
            final double oldB2 = b_lwk(m[j], x[j][i], k, 0);
            final double oldC1 = c_lk(m[j], oldK, 0);
            // C2 goes up
            //final double oldC2 = c_lk(m[j], k, 0);
            
            super.assignZ(j, i, k);
            
            final double newA1 = a_kj(oldK, j, 0);
            final double newA2 = a_kj(k, j, 0);
            final double newB1 = b_lwk(m[j], x[j][i], oldK, 0);
            final double newB2 = b_lwk(m[j], x[j][i], k, 0);
            final double newC1 = c_lk(m[j], oldK, 0);
            //final double newC2 = c_lk(m[j], k, 0);
            
            a[j] = sqrt(a[j] * a[j] - oldA1 * oldA1 - oldA2 * oldA2 + newA1 * newA1 + newA2 * newA2);
            b[m[j]][x[j][i]] = sqrt(b[m[j]][x[j][i]] * b[m[j]][x[j][i]] - oldB1 * oldB1 - oldB2 * oldB2 + newB1 * newB1 + newB2 * newB2);
            if(c[m[j]] == oldC1 && newC1 > oldC1) {
                c[m[j]] = arrMin(N_lk[m[j]]) + W * beta;
            }
                    
        }
    }

    @Override
    protected int sample(int i, int j) {
        final int l = m[j];
        final int w = x[j][i];

        double u = random.nextDouble();

        final double a_heldout = a_kj(z[j][i], j, 1);
        double a2 = sqrt(a[j] * a[j] + a_heldout * a_heldout - (a_heldout + 1) * (a_heldout + 1));

        final double b_heldout = b_lwk(l, w, z[j][i], 1);
        double b2 = sqrt(b[l][w] * b[l][w] + b_heldout * b_heldout - (b_heldout + 1) * (b_heldout + 1));

        final double c_heldout = c_lk(l, z[j][i], 1);
        double c2 = c[l] > c_heldout ? min(c_heldout, (double) arrMin(N_lk[l]) + W * beta) : c[l];

        for (int k2 = 0; k2 < K; k2++) {
            final int k = mapk[k2];
            final int dec = z[j][i] == k ? 1 : 0;
            final double a_new = a_kj(k, j, dec);
            final double b_new = b_lwk(l, w, k, dec);
            final double c_new = c_lk(l, k, dec);
            P[k + 1] = P[k] + a_new * b_new / c_new;

            a2 = sqrt(a2 * a2 - a_new * a_new);
            b2 = sqrt(b2 * b2 - b_new * b_new);
            c2 = c2 >= c_new ? arrMinOffset(N_lk[l], k) + W * beta : c2;

            final double sum = P[k + 1] + a2 * b2 / c2;
            if (u < P[k + 1] / sum) {
                // We have crossed the u threshold
                for (int t = k; t >= 0; t--) {
                    if (u > P[t] / sum) {
                        return t;
                    }
                }
            }
        }
        throw new RuntimeException();
    }

    @Override
     protected void singleIteration(int c) {
        iterNo++;
        iterDelta = 0;
        for (int j = 0; j < D; j++) {
            for (int i = 0; i < DN[j]; i++) {
                if (!frozen[j][i]) {
                    final int k = sample(i, j);
                    assignZ(j, i, k);
                }
            }
        }
    }
    
    private int arrMin(int[] x) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] < min) {
                min = x[i];
            }
        }
        return min;
    }

    private int arrMinOffset(int[] x, int n) {
        int min = Integer.MAX_VALUE;
        for (int i = n; i < x.length; i++) {
            if (x[i] < min) {
                min = x[i];
            }
        }
        return min;
    }
}
