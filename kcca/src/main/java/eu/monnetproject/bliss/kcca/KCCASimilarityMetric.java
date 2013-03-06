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
package eu.monnetproject.bliss.kcca;

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.bliss.SimilarityMetric;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class KCCASimilarityMetric implements SimilarityMetric {

    private final double[][][] U;
    private final int W;
    private final int K;

    public KCCASimilarityMetric(double[][][] U) {
        this.U = U;
        this.W = U[0].length;
        this.K = U[0][0].length;
    }
    
    @Override
    public Vector<Double> simVecSource(Vector<Integer> termVec) {
        double[] v = new double[K];
        for(Map.Entry<Integer,Integer> e : termVec.entrySet()) {
            for(int k = 0; k < K; k++) {
                v[k] += U[0][e.getKey()-1][k];
            }
        }
        return new RealVector(v);
    }

    @Override
    public Vector<Double> simVecTarget(Vector<Integer> termVec) {
        double[] v = new double[K];
        for(Map.Entry<Integer,Integer> e : termVec.entrySet()) {
            for(int k = 0; k < K; k++) {
                v[k] += U[1][e.getKey()-1][k];
            }
        }
        return new RealVector(v);
    }

    @Override
    public int W() {
        return W;
    }

    @Override
    public int K() {
        return K;
    }
    
    
    
}
