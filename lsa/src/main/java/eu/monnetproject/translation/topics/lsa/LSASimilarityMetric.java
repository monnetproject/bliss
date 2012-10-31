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
package eu.monnetproject.translation.topics.lsa;

import eu.monnetproject.translation.topics.SimilarityMetric;
import eu.monnetproject.translation.topics.SparseArray;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class LSASimilarityMetric implements SimilarityMetric {
    double[][] U1,U2;
    double[] S;
    int W,K;

    public LSASimilarityMetric(double[][] U1, double[][] U2, double[] S) {
        this.U1 = U1;
        this.U2 = U2;
        this.S = S;
        this.W = U1[0].length;
        this.K = U1.length;
    }
    
    
    
    @Override
    public double[] simVecSource(SparseArray termVec) {
        final double[] r = doMultiplication(U1,termVec);
        return r;
    }

    @Override
    public double[] simVecTarget(SparseArray termVec) {
        final double[] r = doMultiplication(U2,termVec);
        return r;
    }

    private double[] doMultiplication(double[][] U, SparseArray termVec) {
        double[] result = new double[K];
        for(Int2IntMap.Entry e : termVec.int2IntEntrySet()) {
            for(int k = 0; k < K; k++) {
                final double u = U[k][e.getIntKey()];
                if(Double.isInfinite(u)) {
                    continue;
                }
                if(Double.isNaN(u)) {
                    throw new IllegalArgumentException("Matrix contains NaN");
                }
                result[k] += u * e.getIntValue();
            }
        }
        for(int k = 0; k < K; k++) {
            result[k] /= S[k];
        }
        return result;
    }
    
    @Override
    public int W() {
        return W;
    }
}
