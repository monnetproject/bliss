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
 ********************************************************************************
 */
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.Matrix;
import eu.monnetproject.math.sparse.TridiagonalMatrix;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.math.sparse.Vectors;
import java.util.Arrays;
import java.util.Random;

/**
 * Based on "Matrix Computations" (1990) by Golub & van Loan. 2nd Edition page
 * 485
 *
 * @author John McCrae
 */
public class LanczosAlgorithm {

    private LanczosAlgorithm() {
    }
    private static final Random random = new Random();
    
    private double[] v;
    
    public static Solution lanczos(VectorFunction<Double> A, final Vector<Double> w) {
        return lanczos(A,w,w.size());
    }
     
    public static Solution lanczos(VectorFunction<Double> A, final Vector<Double> w, int K) {
        final int n= w.size();
        assert(K <= n);
        if(n == 0) {
            return new Solution(new TridiagonalMatrix(new double[0], new double[0]), new double[0][0]);
        } else if(n == 1) {
            final Vector<Double> unitVector = Vectors.AS_REALS.make(new double[] { 1.0 });
            final double a11 = A.apply(unitVector).doubleValue(0);
            return new Solution(new TridiagonalMatrix(new double[] { a11 }, new double[0]), new double[][] { { 1 } });
        } else if(n == 2) {
            final Vector<Double> unit1Vector = Vectors.AS_REALS.make(new double[] { 1.0, 0.0 });
            final Vector<Double> unit2Vector = Vectors.AS_REALS.make(new double[] { 0.0, 1.0 });
            final double[] a1 = A.apply(unit1Vector).toDoubleArray();
            final double[] a2 = A.apply(unit2Vector).toDoubleArray();
            return new Solution(new TridiagonalMatrix(new double[] { a1[0], a2[1] } , 
                    new double[] { a1[1] }), new double[][] {
                        { 1, 0 },
                        { 0, 1 }
                    });
        }

        // v = 0; \beta_0 = 1; j = 0
        final double[] v = new double[n];
        final double[] alpha = new double[n + 1];
        final double[] beta = new double[n + 1];
        final double[][] q = new double[K][];
        
        q[0] = Arrays.copyOf(w.toDoubleArray(), n);
        
        int j = 0;
        beta[0] = 1;


        // while \beta_j \neq 0
        while (beta[j] != 0 && j < K) {
            // if j \neq 0
            if (j != 0) {
                // for i = 1:n
                for (int i = 1; i <= n; i++) {
                    // t = w_i; w_i = v_i / \beta_j; v_i = -\beta_j t
                    final double t = w.doubleValue(i-1);
                    w.put(i-1,v[i - 1] / beta[j]);
                    v[i - 1] = -beta[j] * t;
                }
                q[j] = Arrays.copyOf(w.toDoubleArray(),n);
            }
            // v = v + A.mult(w)
            final Vector<Double> aw = A.apply(w);
            for (int i = 0; i < n; i++) {
                v[i] += aw.doubleValue(i);
            }
            // j = j + 1; \alpha_j = w^Tv ; v = v - \alpha_j w; \beta_j = ||v||_2
            j++;
            if (j == n + 1) {
                break;
            }
            alpha[j] = 0;
            for (int i = 0; i < n; i++) {
                alpha[j] += w.doubleValue(i) * v[i];
            }
            beta[j] = 0;
            for (int i = 0; i < n; i++) {
                v[i] = v[i] - alpha[j] * w.doubleValue(i);
                beta[j] += v[i] * v[i];
            }
            beta[j] = Math.sqrt(beta[j]);
        }
        return new Solution(new TridiagonalMatrix(Arrays.copyOfRange(alpha, 1, K + 1), Arrays.copyOfRange(beta, 1, K)), q);
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
        assert(A.rows() == A.cols());
        assert(A.isSymmetric());
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

        public Solution(TridiagonalMatrix tridiagonal, double[][] q) {
            this.tridiagonal = tridiagonal;
            this.q = q;
        }

        public double[][] q() {
            return q;
        }

        public TridiagonalMatrix tridiagonal() {
            return tridiagonal;
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
