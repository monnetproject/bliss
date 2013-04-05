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
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.Vectors;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class CholeskyDecomposition {

    private CholeskyDecomposition() {
    }

    public static double[][] denseDecomp(double[][] a) {
        int m = a.length;
        double[][] l = new double[m][m]; //automatically initialzed to 0's
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < (i + 1); k++) {
                double sum = 0;
                for (int j = 0; j < k; j++) {
                    sum += l[i][j] * l[k][j];
                }
                if (i == k && a[i][i] - sum < 0) {
                    for (int j = 0; j < m; j++) {
                        System.out.println(Arrays.toString(a[j]));
                    }
                    throw new IllegalArgumentException("Matrix not positive definite");
                }
                l[i][k] = (i == k) ? Math.sqrt(a[i][i] - sum)
                        : (a[i][k] - sum) / l[k][k];
            }
        }
        return l;
    }

    public static SparseMatrix<Double> decomp(SparseMatrix<Double> a, boolean complete) {

        int m = a.rows();
        SparseMatrix<Double> l = new SparseMatrix<Double>(m, m, Vectors.AS_SPARSE_REALS); //automatically initialzed to 0's
        for (int i = 0; i < m; i++) {
            if (complete) {
                for (int k = 0; k < (i + 1); k++) {
                    chol(l, k, i, a);
                }
            } else {
                final Vector<Double> l_i = l.row(i);
                for (int k : l_i.keySet()) {
                    if (k >= (i + 1)) {
                        break;
                    }
                    chol(l, k, i, a);
                }
            }
        }
        return l;
    }

    public static void chol(SparseMatrix<Double> l, int k, int i, SparseMatrix<Double> a) throws IllegalArgumentException {
        double sum = 0;
        final Vector<Double> l_k = l.row(k);
        for (int j : l_k.keySet()) {
            sum += l.doubleValue(i, j) * l_k.doubleValue(j);
        }
        double a_ii = a.doubleValue(i, i);
        if (i == k) {
            if (a_ii - sum < 0) {
                throw new IllegalArgumentException("Matrix not positive definite");
            }
            l.set(i, k, Math.sqrt(a_ii - sum));
        } else {
            l.set(i, k, (a.doubleValue(i, k) - sum) / l.doubleValue(k, k));
        }
    }

    public static Vector<Double> solve(SparseMatrix<Double> a, Vector<Double> b) {
        assert (a.cols() == b.length());
        final int N = b.length();
        Vector<Double> y = new SparseRealArray(N);
        for (int i = 0; i < N; i++) {
            double sum = b.doubleValue(i);
            for (int j = 0; j < i; j++) {
                sum -= a.doubleValue(i, j) * y.doubleValue(j);
            }
            y.put(i, sum / a.doubleValue(i, i));
        }
        return y;
    }

    public static Vector<Double> solveT(SparseMatrix<Double> a, Vector<Double> b) {
        assert (a.cols() == b.length());
        final int N = b.length();
        Vector<Double> y = new SparseRealArray(N);
        for (int i = N - 1; i >= 0; i--) {
            double sum = b.doubleValue(i);
            for (int j = i + 1; j < N; j++) {
                sum -= a.doubleValue(j, i) * y.doubleValue(j);
            }
            y.put(i,sum / a.doubleValue(i, i));
        }
        return y;
    }
}
