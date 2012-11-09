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

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.VectorFunction;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class ArnoldiAlgorithm {

    private ArnoldiAlgorithm() {
    }
    public static final double EPSILON = 1e-20;

    public static Solution solve(VectorFunction<Double, Double> A, Vector<Double> r0, int K) {
        // h [1..k][0..k]
        double[][] h = new double[K][K + 1];
        // q [1..k]
        RealVector[] q = new RealVector[K];
        // r_k [1..W]
        // r_0 = q_1
        double[] r = r0.toDoubleArray();
        // h[1][0] = 1
        h[0][0] = 1;
        // k = 0;
        int k = 0;
        // while(h_k+1,k != 0) {
        while (k < K && h[k][k] > EPSILON) {
            // q_k+1 = r_k / h_k+1,k
            q[k] = new RealVector(Arrays.copyOf(r, r.length));
            q[k].multiply(1.0 / h[k][k]);
            // k = k + 1
            k++;
            // r_k = A q_k
            r = A.apply(q[k - 1]).toDoubleArray();
            // for i = 1:k
            for (int i = 0; i < k; i++) {
                // h_i,k = q_i ^T w
                h[i][k] = q[i].innerProduct(new RealVector(r));
                // r_k = rk - h_ik q_i
                for (int j = 0; j < r.length; j++) {
                    r[j] -= h[i][k] * q[i].doubleValue(j);
                }
            }
            // h_k+1,k = ||r_k||
            if (k < K) {
                for (int i = 0; i < r.length; i++) {
                    h[k][k] += r[i] * r[i];
                }
                h[k][k] = Math.sqrt(h[k][k]);
            }
        }
        // Clean up
        for (int i = 0; i < k; i++) {
            h[i] = Arrays.copyOfRange(h[i], 1, k + 1);
        }
        return new Solution(q, Arrays.copyOfRange(h, 0, k), k);
    }

    public static class Solution {

        public final RealVector[] q;
        public final double[][] h;
        public final int K;

        public Solution(RealVector[] q, double[][] h, int K) {
            this.q = q;
            this.h = h;
            this.K = K;
        }
    }
}
