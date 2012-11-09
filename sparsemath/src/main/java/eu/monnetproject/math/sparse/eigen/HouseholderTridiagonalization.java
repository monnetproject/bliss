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

import eu.monnetproject.math.sparse.DoubleArrayMatrix;
import eu.monnetproject.math.sparse.Matrix;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.TridiagonalMatrix;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.Vectors;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class HouseholderTridiagonalization {

    /**
     * Compute the householder vector of a given vector
     *
     * Complexity: O(sparsity(n))
     *
     * @return A vector with v(1) = 1 and (I - 2vv^T /v^Tv)x is zero in all but
     * first component
     */
    public static Vector<Double> house(Vector<Double> x, int j) {
        final int n = x.length();
        double mu = 0.0;
        final double[] x2 = x.toDoubleArray();
        for (int i = j; i < n; i++) {
            mu += x2[i] * x2[i];
        }
        mu = Math.sqrt(mu);
        final Vector<Double> v = x.clone();
        if (mu != 0.0) {
            final double beta = x.doubleValue(0) + Math.signum(x.doubleValue(0)) * mu;
            for (int i = 1; i < v.length(); i++) {
                v.divide(i, beta);
            }
        }
        v.put(0, 1);
        return v;
    }

    public static <N extends Number> TridiagonalMatrix tridiagonalize(Matrix<N> A) {
        assert (A.isSymmetric());
        final int n = A.rows();
        final DoubleArrayMatrix A2 = new DoubleArrayMatrix(n, n);
        A2.add(A);
        double[] alpha = new double[n];
        double[] beta = new double[n - 1];
        for (int k = 0; k < n - 2; k++) {
            final RealVector x = new RealVector(n - k - 1);
            for (int j = k + 1; j < n; j++) {
                x.put(j - k - 1, A2.doubleValue(j, k));
            }
            final Vector<Double> v = house(x, k);
            final double vtv = v.innerProduct(v);
            final RealVector w = new RealVector(n - k - 1);
            for (int i = 0; i < n - k - 1; i++) {
                for (int j = 0; j < n - k - 1; j++) {
                    w.data()[i] += 2 * A2.doubleValue(i + k + 1, j + k + 1) * v.doubleValue(j) / vtv;
                }
            }
            final double ptv = w.innerProduct(v);
            for (int j = 0; j < n - k - 1; j++) {
                w.sub(j, ptv * v.doubleValue(j) / vtv);
            }

            for (int i = 1; i < n - k; i++) {
                beta[k] -= A2.doubleValue(i + k, k) * ((i == 1 ? 1 : 0) - 2 * v.doubleValue(i - 1) * v.doubleValue(0) / vtv);
            }
            for (int i = 0; i < n - k - 1; i++) {
                for (int j = 0; j < n - k - 1; j++) {
                    A2.data()[i + k + 1][j + k + 1] -= v.doubleValue(i) * w.doubleValue(j) + w.doubleValue(i) * v.doubleValue(j);
                }
            }
            alpha[k] = A2.doubleValue(k, k);
        }
        beta[n - 2] = A2.doubleValue(n - 1, n - 2);
        alpha[n - 2] = A2.doubleValue(n - 2, n - 2);
        alpha[n - 1] = A2.doubleValue(n - 1, n - 1);

        return new TridiagonalMatrix(alpha, beta);
    }
}
