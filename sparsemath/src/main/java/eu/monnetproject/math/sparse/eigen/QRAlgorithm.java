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

import eu.monnetproject.math.sparse.Matrix;
import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.TridiagonalMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Based on "Matrix Computations" (1990) by Golub & van Loan. 2nd Edition page
 * 423
 *
 * @author John McCrae
 */
public class QRAlgorithm {

    /**
     * Finds [c,s] satisfying T [ c s ] [ a ] = [ r ] [ -s c ] [ b ] = [ 0 ]
     *
     * subject to c^2 + s^2 = 1
     *
     * @param a
     * @param b
     * @return [c,s]
     */
    public static double[] givens(double a, double b) {
        if (b == 0) {
            return new double[]{1.0, 0.0};
        } else {
            if (Math.abs(b) > Math.abs(a)) {
                final double tau = -a / b;
                final double s = 1.0 / Math.sqrt(1 + tau * tau);
                return new double[]{s * tau, s};
            } else {
                final double tau = -b / a;
                final double c = 1.0 / Math.sqrt(1 + tau * tau);
                return new double[]{c, c * tau};
            }
        }
    }

    private QRAlgorithm() {
    }

    /**
     * Apply the implicit QR Wilkinson shift. This in place calculates a new
     * tridiagonal such that the last beta[o+(n-1)] =~ 0.0. This method assumes
     * that beta[o-1] =~ 0.0 and beta[o+n] =~ 0.0 but does not check this
     * result.
     *
     * @param alpha The diagonal of the tridiagonal matrix
     * @param beta The off-diagonal of the tridiagonal matrix
     * @param o The offset
     * @param n The size of the sub-matrix to consider
     */
    public static void wilkinsonShift(double[] alpha, double[] beta, int o, int n) {
        //int n = alpha.length;
        // d = (t_{n-1,n-1} - t_{nn}) /2
        double d = (alpha[o + n - 2] - alpha[o + n - 1]) / 2;
        // \mu = t_{nn} - t^2_{n,n-1} / (d + sign(d) * \sqrt{d^2 + t^2_{n,n-1}})
        double mu = alpha[o + n - 1] - beta[o + n - 2] * beta[o + n - 2] / (d + Math.signum(d) * Math.sqrt(d * d + beta[o + n - 2] * beta[o + n - 2]));
        // x = t_{11} - mu
        double x = alpha[o + 1] - mu;
        // z = t_{21}
        double z = beta[o + 0];
        // for k = 1:n-1
        for (int k = 0; k < n - 1; k++) {
            // [c,s] = givens(x,z)
            final double[] cs = givens(x, z);
            final double c = cs[0];
            final double s = cs[1];

            // T = G_k^T T G_k where G_k = G(k,k+1,c,s)
            // t00 is T_{k-1,k-1}
            double t01 = k > 0 ? c * beta[o + k - 1] - s * z : 0;
            double t11 = c * c * alpha[o + k] + s * s * alpha[o + k + 1] - 2 * s * c * beta[o + k];
            double t12 = (c * c - s * s) * beta[o + k] + s * c * (alpha[o + k] - alpha[o + k + 1]);
            double t22 = s * s * alpha[o + k] + c * c * alpha[o + k + 1] + 2 * s * c * beta[o + k];
            double t13 = k < n - 2 ? -s * beta[o + k + 1] : 0;
            double t23 = k < n - 2 ? c * beta[o + k + 1] : 0;

            alpha[o + k] = t11;
            alpha[o + k + 1] = t22;
            if (k > 0) {
                beta[o + k - 1] = t01;
            }
            beta[o + k] = t12;

            // if k < n - 1
            if (k < n - 2) {
                beta[o + k + 1] = t23;
                // x = t_{k+1,k}
                x = beta[o + k];
                // z = t_{k+2,k}
                z = t13;
            }
        }
    }
    
    /**
     * Find the eigenvalues of sparse matrix by means of a Lanczos-QR method.
     *
     * @param A The matrix
     * @param epsilon The error rate
     * @return The eigenvalues of the matrix
     */
    public static double[] qrSolve(SparseMatrix<Double> A, double epsilon) {
        assert (A.rows() == A.cols());
        assert (A.isSymmetric());
        
        // It turns out that Lanczos sucks at the easy columns so we remove these
        // on the first pass
        final TrivialEigenvalues<Double> trivial = TrivialEigenvalues.find(A, true);
        if(trivial.nonTrivial == null) {
            return trivial.eigenvalues;
        }
        
        final TridiagonalMatrix tridiag = LanczosAlgorithm.lanczos(trivial.nonTrivial).tridiagonal();
        return qrSolve(epsilon, tridiag, trivial);
    }
    
    public static <N extends Number> double[] qrSolve(double epsilon, TridiagonalMatrix tridiag, TrivialEigenvalues<N> trivial) {
        final int n = tridiag.rows();
        if(n == 0) {
            return trivial.eigenvalues;
        } else if(n == 1) {
            throw new RuntimeException("Trivial reduction failed");
        } else if(n == 2) {
            double lambda1, lambda2;
            lambda1 = lambda2 = tridiag.alpha()[0] + tridiag.alpha()[1];
            double t = Math.sqrt((tridiag.alpha()[0] + tridiag.alpha()[1]) * (tridiag.alpha()[0] + tridiag.alpha()[1]) - 4.0 * (tridiag.alpha()[0] * tridiag.alpha()[1] - tridiag.beta()[0] * tridiag.beta()[0]));
            lambda1 = (lambda1 + t) / 2.0;
            lambda2 = (lambda2 - t) / 2.0;
            final double[] eigenvalues = new double[trivial.eigenvalues.length + 2];
            System.arraycopy(trivial.eigenvalues, 0, eigenvalues, 2, trivial.eigenvalues.length);
            eigenvalues[0] = lambda1;
            eigenvalues[1] = lambda2;
            return eigenvalues;
        }
        int p = 0;
        int q = 0;
        int iterTotal = 0;
        while (q != n) {
            // Find the largest q and the smallest p such that 
            //     [ B_11  0     0  ]
            // B = [  0   B_22   0  ]
            //     [  0    0   B_33 ]
            // And B_11 is pxp, B_33 is qxq
            // B_33 is diagonal
            //
            boolean qFound = false;
            for (int i = n - 2 - q; i >= 0; i--) {
                if (Math.abs(tridiag.beta()[i]) < epsilon) {
                    if (!qFound) {
                        q++;
                    } else {
                        p = i;
                        break;
                    }
                } else {
                    if (!qFound) {
                        qFound = true;
                    }
                }
            }

            if (q < n) {
                if (n - p - q >= 2) {
                    wilkinsonShift(tridiag.alpha(), tridiag.beta(), p, n - p - q);
                } else {
                    q++;
                }
            }
            if(++iterTotal > n * 100) {
                System.err.println(tridiag);
                throw new RuntimeException("Iteration limit on QR solve");
            }
        }

        if (trivial.eigenvalues.length == 0) {
            return tridiag.alpha();
        } else {
            double[] eigenvalues = new double[n+trivial.eigenvalues.length];
            System.arraycopy(tridiag.alpha(), 0, eigenvalues, 0, n);
            System.arraycopy(trivial.eigenvalues, 0, eigenvalues, n, trivial.eigenvalues.length);
            return eigenvalues;
        }
    }

    public static <N extends Number> List<double[]> eigenvector(Matrix<N> A, double lambda) {
        assert (A.rows() == A.cols());
        final int n = A.rows();
        final SparseRealArray[] B = new SparseRealArray[n];

        for (int i = 0; i < n; i++) {
            B[i] = new SparseRealArray(n);
            for (Map.Entry<Integer, N> e : A.row(i).entrySet()) {
                B[i].put(e.getKey().intValue(), e.getValue().doubleValue());
            }
            B[i].sub(i, lambda);
        }

        MAIN:
        for (int i = 0; i < n - 1; i++) {
            if (B[i].get(i) == 0.0) {
                BLOCK:
                {
                    for (int j = i + 1; j < n; j++) {
                        if (B[j].get(i) != 0.0) {
                            for (Map.Entry<Integer, Double> entry : B[j].entrySet()) {
                                B[i].add(entry.getKey(), entry.getValue());
                            }
                            break BLOCK;
                        }
                    }
                    continue MAIN;
                }
            }
            for (int j = i + 1; j < n; j++) {
                final double mu = B[j].get(i) / B[i].get(i);
                if (mu != 0.0) {
                    for (Map.Entry<Integer, Double> entry : B[i].entrySet()) {
                        if (entry.getKey() > i) {
                            B[j].sub(entry.getKey(), mu * entry.getValue());
                        }
                    }
                    B[j].remove(i);
                }
            }
        }

        final ArrayList<double[]> eigenvectors = new ArrayList<double[]>();

        eigenvectors.add(new double[n]);

        for (int i = n - 1; i >= 0; i--) {
            if (Math.abs(B[i].get(i)) < 1e-8) {
                final double[] nextEigen = new double[n];
                nextEigen[i] = 1.0;
                eigenvectors.add(nextEigen);
            } else {
                for (double[] eigenvector : eigenvectors) {
                    eigenvecIter(B, i, eigenvector);
                }
            }
        }

        return eigenvectors.subList(1, eigenvectors.size());
    }

    private static void eigenvecIter(final SparseRealArray[] B, int i, double[] eigenvector) {
        if (B[i].get(i) != 0.0) {
            for (Map.Entry<Integer, Double> entry : B[i].entrySet()) {
                if (entry.getKey() > i) {
                    eigenvector[i] -= entry.getValue() * eigenvector[entry.getKey()];
                }
            }
            eigenvector[i] /= B[i].get(i);
        } else {
            // If we have a singular column all non-zero below this
            eigenvector[i] = 1.0;
        }
    }
}
