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
package eu.monnetproject.bliss.clesa;

import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.SparseRealArray;
import java.util.Random;
import static java.lang.Math.*;

/**
 *
 * @author John McCrae
 */
public class HappenstanceInverse {

    private static double krd(int i, int j) {
        return i == j ? 1.0 : 0.0;
    }

    public static SparseRealArray[] solveAXA(SparseIntArray[][] A, int l, int N) {
        final SparseRealArray[] X = new SparseRealArray[A[0][l].length()];
        for (int n = 0; n < A[0][l].length(); n++) {
            X[n] = new SparseRealArray(n);
        }
        final Random random = new Random();
        for (int iter = 0; iter < N; iter++) {
            final int i = random.nextInt(A.length), j = random.nextInt(A.length);
            int best_m = -1, best_n = -1;
            double leastScore = Double.POSITIVE_INFINITY;
            double ip = calcIP(A, l, i, j, X);

            for (int m : A[i][l].values()) {
                if (A[j][l].containsKey(m)) {
                    for (int n : A[i][l].values()) {
                        if (A[j][l].containsKey(n)) {
                            final double score = (krd(i, j) - ip
                                    + A[i][l].doubleValue(m) * X[m].value(n) * A[j][l].doubleValue(n))
                                    / (A[i][l].doubleValue(m) * A[j][l].doubleValue(n));
                            if (abs(score) < abs(leastScore)) {
                                leastScore = score;
                                best_m = m;
                                best_n = n;
                            }
                        }
                    }
                }
            }

            X[best_m].put(best_n, leastScore);
        }
        return X;
    }

    private static double calcIP(SparseIntArray[][] A, int l, int i, int j, SparseRealArray[] X) {
        double ip = 0.0;
        for (int m : A[i][l].values()) {
            if (A[j][l].containsKey(m)) {
                for (int n : A[i][l].values()) {
                    ip += A[i][l].doubleValue(m) * X[m].doubleValue(n) * A[j][l].doubleValue(n);
                }
            }
        }
        return ip;
    }
}
