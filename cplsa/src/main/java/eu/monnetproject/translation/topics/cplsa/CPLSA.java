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
package eu.monnetproject.translation.topics.cplsa;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Coupled Probabilistic Latent Semantic Analysis
 *
 * @author John McCrae
 */
public class CPLSA {

    private final int[][][] X;
    private final double alpha;
    private final double beta;
    private final int W, J, K;
    private final int[][] N_jl;
    private final int L = 2;
    private final int[][][] Z;
    public final double[][][] phi_lwk, theta_lkj;
    private final int[][][] N_lkj;
    private final int[][][] N_lwk;
    private final int[][] N_lk;
    private boolean initialized = false;

    public CPLSA(int[][][] X, int W, int K, double alpha, double beta) {
        this.X = X;
        this.W = W;
        this.K = K;
        this.J = X.length;
        this.alpha = alpha;
        this.beta = beta;
        this.N_jl = new int[J][L];
        for (int j = 0; j < J; j++) {
            N_jl[j][0] = X[j][0].length;
            N_jl[j][1] = X[j][1].length;
        }
        this.Z = new int[J][L][];
        for (int j = 0; j < J; j++) {
            for (int l = 0; l < L; l++) {
                Z[j][l] = new int[N_jl[j][l]];
            }
        }
        this.phi_lwk = new double[L][W][K];
        this.theta_lkj = new double[L][K][J];
        this.N_lkj = new int[L][K][J];
        this.N_lwk = new int[L][W][K];
        this.N_lk = new int[L][K];
    }

    public void initialize() {
        for (int l = 0; l < L; l++) {
            final int[] f = new int[W];
            int sumN = 0;
            for (int j = 0; j < J; j++) {
                for (int n = 0; n < N_jl[j][l]; n++) {
                    f[X[j][l][n]]++;
                    sumN++;
                }
            }
            final Integer[] i = new Integer[W];
            int i2 = 0;
            while (i2 < W) {
                i[i2] = i2++;
            }
            Arrays.sort(i, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    int c = Integer.compare(f[o1], f[o2]);
                    if (c == 0) {
                        return Integer.compare(o1, o2);
                    } else {
                        return -c;
                    }
                }
            });
            final int bucketSize = sumN / K;
            final int[] w2k = new int[W];
            int kappa = 0;
            sumN = 0;
            for (int w : i) {
                w2k[w] = kappa;
                sumN += f[w];
                if (sumN >= (kappa + 1) * bucketSize) {
                    kappa++;
                    if(kappa == K) {
                        break;
                    }
                }
            }

            for (int j = 0; j < J; j++) {
                for (int n = 0; n < N_jl[j][l]; n++) {
                    final int w = X[j][l][n];
                    final int k = w2k[w];
                    Z[j][l][n] = k;
                    N_lk[l][k]++;
                    N_lkj[l][k][j]++;
                    N_lwk[l][w][k]++;
                }
            }
            maximization(l);
        }
        initialized = true;
    }

    
    public void solve(int iterations, double epsilon) {
        if(!initialized) {
            initialize();
        }
        for (int i = 0; i < iterations; i++) {
            final double lambda = dual(epsilon);

            expectation(lambda);

            for (int l = 0; l < L; l++) {
                maximization(l);
            }
        }
    }

    public static final int DUAL_MAX_ITERS = 1000;
    
    private double dual(double epsilon) {
        double[][] p = new double[J][L];
        double[][] pi = new double[J][L];

        for (int j = 0; j < J; j++) {
            for (int l = 0; l < L; l++) {
                p[j][l] = 1.0;
                for (int n = 0; n < N_jl[j][l]; n++) {
                    p[j][l] *= phi_lwk[l][X[j][l][n]][Z[j][l][n]];
                }

                for (int k = 0; k < K; k++) {
                    if (l == 0) {
                        pi[j][l] += (double) N_lkj[0][k][j] / N_lk[0][k] - (double) N_lkj[1][k][j] / N_lk[1][k];
                    } else {
                        pi[j][l] -= (double) N_lkj[0][k][j] / N_lk[0][k] - (double) N_lkj[1][k][j] / N_lk[1][k];
                    }
                }
            }
        }

        double lambda = 0.0;

        double value = dZetaOverZeta(lambda, p, pi);

        int iters = 0;
        
        while (Math.abs(value) > epsilon) {
            final double gradient = ddZetaOverZeta(lambda, p, pi);
            if (lambda <= 0.0 && value / gradient > 0) {
                return 0.0;
            }  else if(gradient == 0) {
                System.err.println("Dual problem failed on local maximum");
                return lambda;
            } else {
                lambda -= value / gradient;
                value = dZetaOverZeta(lambda, p, pi);
            }
            if(iters ++ > DUAL_MAX_ITERS) {
                System.err.println("Dual problem exceeded iteration limit");
                break;
            }
        }
        return lambda;
    }

    // Z'(lambda) / Z(lambda)
    private double dZetaOverZeta(double lambda, double[][] p, double[][] pi) {
        double zeta = 0.0;
        double dzeta = 0.0;

        for (int j = 0; j < J; j++) {
            for (int l = 0; l < L; l++) {
                final double z = p[j][l] * Math.exp(-lambda * pi[j][l]);
                zeta += z;
                dzeta += -pi[j][l] * z;
            }
        }
        return dzeta / zeta;
    }

    // d/dlambda Z'(lambda) / Z(lambda) = [ Z''(lambda) * Z(lambda) * Z'(lambda) ^ 2] / Z(lambda) ^ 2
    private double ddZetaOverZeta(double lambda, double[][] p, double[][] pi) {
        double zeta = 0.0;
        double dzeta = 0.0;
        double ddzeta = 0.0;

        for (int j = 0; j < J; j++) { 
            for (int l = 0; l < L; l++) {
                final double z = p[j][l] * Math.exp(-lambda * pi[j][l]);
                zeta += z;
                dzeta += -pi[j][l] * z;
                ddzeta += pi[j][l] * pi[j][l] * z;
            }
        }

        return (ddzeta * zeta + dzeta * dzeta) / zeta / zeta;
    }
    private final Random random = new Random();

    private void expectation(double lambda) {
        double[][][] pi = new double[J][L][K];

        for (int j = 0; j < J; j++) {
            for (int l = 0; l < L; l++) {
                for (int k = 0; k < K; k++) {
                    if (l == 0) {
                        pi[j][l][k] = (double) N_lkj[0][k][j] / N_lk[0][k] - (double) N_lkj[1][k][j] / N_lk[1][k];
                    } else {
                        pi[j][l][k] = (double) N_lkj[1][k][j] / N_lk[1][k] - (double) N_lkj[0][k][j] / N_lk[0][k];
                    }
                }
            }
        }

        for (int j = 0; j < J; j++) {
            for (int l = 0; l < L; l++) {
                for (int n = 0; n < N_jl[j][l]; n++) {
                    double[] p = new double[K];
                    for (int k = 0; k < K; k++) {
                        p[k] = (k == 0 ? 0.0 : p[k - 1])
                                + phi_lwk[l][X[j][l][n]][k] * theta_lkj[l][k][j]
                                * Math.exp(-lambda * pi[j][l][k]);
                    }
                    double d = random.nextDouble() * p[K - 1];
                    for (int k = 0; k < K; k++) {
                        if (d < p[k]) {
                            assignZ(j, l, n, k);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void assignZ(int j, int l, int n, int newK) {
        final int oldK = Z[j][l][n];
        if (newK == oldK) {
            return;
        }
        N_lk[l][oldK]--;
        N_lk[l][newK]++;
        N_lkj[l][oldK][j]--;
        N_lkj[l][newK][j]++;
        N_lwk[l][X[j][l][n]][oldK]--;
        N_lwk[l][X[j][l][n]][newK]++;
        Z[j][l][n] = newK;
    }

    private void maximization(int l) {
        for (int k = 0; k < K; k++) {
            for (int j = 0; j < J; j++) {
                theta_lkj[l][k][j] = ((double) N_lkj[l][k][j] + alpha) / ((double) N_jl[j][l] + alpha * K);
            }
        }
        for (int w = 0; w < W; w++) {
            for (int k = 0; k < K; k++) {
                phi_lwk[l][w][k] = ((double) N_lwk[l][w][k] + beta) / ((double) N_lk[l][k] + beta * W);
            }
        }
    }
}
