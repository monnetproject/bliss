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
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.Matrix;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.TridiagonalMatrix;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.math.sparse.Vectors;
import java.util.Arrays;
import java.util.Random;

/**
 * Based on "Matrix Computations" (1990) by Golub & van Loan. 2nd Edition page
 * 485. And http://web.eecs.utk.edu/~dongarra/etemplates/node120.html
 *
 * @author John McCrae
 */
public class LanczosAlgorithm {

    private final boolean USE_GRAM_SCHMIDT = Boolean.parseBoolean(System.getProperty("svd.lanczos.usegs", "true"));

    private LanczosAlgorithm() {
    }
    private static final Random random = new Random();
    private double[] v;

    public static Solution lanczos(VectorFunction<Double> A, final Vector<Double> w) {
        return lanczos(A, w, w.size(), 1.0);
    }

    public static Solution lanczos(VectorFunction<Double> A, final Vector<Double> w, int K, double rho) {
        final int n = w.size();
        assert (K <= n);
        if (n == 0) {
            return new Solution(new TridiagonalMatrix(new double[0], new double[0]), new double[0][0],0.0);
        } else if (n == 1) {
            final Vector<Double> unitVector = Vectors.AS_REALS.make(new double[]{1.0});
            final double a11 = A.apply(unitVector).doubleValue(0);
            return new Solution(new TridiagonalMatrix(new double[]{a11}, new double[0]), new double[][]{{1}},0.0);
        } else if (n == 2) {
            final Vector<Double> unit1Vector = Vectors.AS_REALS.make(new double[]{1.0, 0.0});
            final Vector<Double> unit2Vector = Vectors.AS_REALS.make(new double[]{0.0, 1.0});
            final double[] a1 = A.apply(unit1Vector).toDoubleArray();
            final double[] a2 = A.apply(unit2Vector).toDoubleArray();
            return new Solution(new TridiagonalMatrix(new double[]{a1[0], a2[1]},
                    new double[]{a1[1]}), new double[][]{
                        {1, 0},
                        {0, 1}
                    },0.0);
        }

        // v = 0; \beta_0 = 1; j = 0
        final double[] v = new double[n];
        final double[] v2 = new double[n];
        final double[] alpha = new double[n + 1];
        final double[] beta = new double[n + 1];
        beta[0] = 1;
        final double[][] q = new double[n][K];
        final double[] r = new double[n];
        System.arraycopy(w.toDoubleArray(), 0, r, 0, n);

        //q[0] = Arrays.copyOf(w.toDoubleArray(), n);
        for (int i = 0; i < n; i++) {
            q[i][0] = r[i];
        }

        int j = 0;

        // while \beta_j \neq 0
        while (beta[j] != 0 && j < K) {
            // w = v_{j-1} * \beta_{j-1}
            // v_j = r / \beta_{j-1}
            for (int i = 0; i < n; i++) {
                v2[i] = v[i] * beta[j];
                v[i] = r[i] / beta[j];
                q[i][j] = v[i];
            }

            // r = Av_j - v_{j-1}\beta_{j-1}
            final Vector<Double> av = A.apply(new RealVector(v));
            for (int i = 0; i < n; i++) {
                r[i] = av.doubleValue(i) - v2[i];
            }
            // \alpha_j = v_j^T r
            alpha[j] = 0.0;
            for (int i = 0; i < n; i++) {
                alpha[j] += v[i] * r[i];
            }

            beta[j+1] = 0.0;
            // r = r - \alpha_j v_j
            // \beta_j = ||r||
            for (int i = 0; i < n; i++) {
                r[i] -= alpha[j] * v[i];
                beta[j+1] += r[i] * r[i];
            }
            beta[j+1] = Math.sqrt(beta[j+1]);

            // if (||r|| <  \rho \sqrt{\alpha_j^2 + \beta_{j-1}^2}) {
            if (j > 1 && beta[j+1] < rho * Math.sqrt(alpha[j] * alpha[j] + beta[j] * beta[j])) {
                // s = V_j^T r
                double[] s = new double[j];
                for (int i = 0; i < j; i++) {
                    for (int k = 0; k < n; k++) {
                        s[i] += q[k][i] * r[k];
                    }
                }
                // r = r - V_j s
                for (int i = 0; i < n; i++) {
                    for (int k = 0; k < j; k++) {
                        r[i] -= q[i][k] * s[k];
                    }
                }
                // \alpha_j = \alpha_j + s_j ; \beta_j = \beta_j + s_{j-1}
                alpha[j] = alpha[j] + s[j - 1];
                beta[j] = beta[j] + s[j - 2];
            }
            // j = j + 1
            j++;
        }
        return new Solution(new TridiagonalMatrix(Arrays.copyOfRange(alpha, 0, K), Arrays.copyOfRange(beta, 1, K)), q, beta[K]);
    }

    /**
     * Given a symmetric sparse integer matrix compute a tri-diagonalization of
     * the matrix
     *
     * @param A The matrix as a row-wise array of sparse array
     * @return The diagonal and off-diagonal vector, in such a way that a matrix
     * B can be constructed such that B[i][i] = r[0][i], B[i][i+1] = r[1][i] and
     * B[i+1][i] = r[1][i] and B[i][j] = 0 otherwise. This matrix has the same
     * set of eigenvalues as A.
     */
    public static Solution lanczos(Matrix<Double> A) {
        assert (A.rows() == A.cols());
        assert (A.isSymmetric());
        final int n = A.rows();
        double[] w = new double[n];

        // w is a random vector with 2-norm = 1
        for (int i = 0; i < n; i++) {
            w[i] = random.nextDouble();
        }

        double w2sum = 0.0;
        for (int i = 0; i < n; i++) {
            w2sum += w[i] * w[i];
        }
        w2sum = Math.sqrt(w2sum);

        for (int i = 0; i < n; i++) {
            w[i] = w[i] / w2sum;
        }
        return lanczos(A.asVectorFunction(), Vectors.AS_REALS.make(w));
    }

    public static class Solution {

        private final TridiagonalMatrix tridiagonal;
        private final double[][] q;
        private final double beta;

        public Solution(TridiagonalMatrix tridiagonal, double[][] q, double beta) {
            this.tridiagonal = tridiagonal;
            this.q = q;
            this.beta = beta;
        }

        public double[][] q() {
            return q;
        }

        public TridiagonalMatrix tridiagonal() {
            return tridiagonal;
        }

        public double beta() {
            return beta;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + (this.tridiagonal != null ? this.tridiagonal.hashCode() : 0);
            hash = 71 * hash + Arrays.deepHashCode(this.q);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Solution other = (Solution) obj;
            if (this.tridiagonal != other.tridiagonal && (this.tridiagonal == null || !this.tridiagonal.equals(other.tridiagonal))) {
                return false;
            }
            if (!Arrays.deepEquals(this.q, other.q)) {
                return false;
            }
            return true;
        }
    }
}
