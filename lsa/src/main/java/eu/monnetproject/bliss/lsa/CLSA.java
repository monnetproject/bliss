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
package eu.monnetproject.bliss.lsa;

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.NGramSimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetricFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class CLSA implements SimilarityMetricFactory<InputStream> {

    @Override
    public SimilarityMetric makeMetric(InputStream data, int W) throws IOException {
        final DataInputStream in = new DataInputStream(data);
        if(in.readInt() != 2) {
            throw new IllegalArgumentException("Wrong number of languages");
        }
        if(in.readInt() != W) {
            throw new IllegalArgumentException("Wrong number of words");
        }
        final int K = in.readInt();
        double[][] A_kw = new double[K][W], B_kw = new double[K][W], C_kk = new double[K][K];
        for(int k = 0; k < K; k++) {
            for(int w = 0; w < W; w++) {
                A_kw[k][w] = in.readDouble();
            }
        }
        for(int k = 0; k < K; k++) {
            for(int w = 0; w < W; w++) {
                B_kw[k][w] = in.readDouble();
            }
        }
        for(int k1 = 0; k1 < K; k1++) {
            for(int k2 = 0; k2 < K; k2++) {
                C_kk[k1][k2] = in.readDouble();
            }
        }
        in.close();
        return new CLSAImpl(A_kw, B_kw, C_kk, W, K);
    }

    @Override
    public NGramSimilarityMetric makeNGramMetric(InputStream dat, int W) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    

    @Override
    public Class<InputStream> datatype() {
        return InputStream.class;
    }

    private static class CLSAImpl implements SimilarityMetric {
        private final double[][] A_kw,B_kw;
        private final double[][] C_kk;
        private final int W,K;

        public CLSAImpl(double[][] A, double[][] B, double[][] C, int W, int K) {
            this.A_kw = A;
            this.B_kw = B;
            this.C_kk = C;
            this.W = W;
            this.K = K;
        }
        
        
        
        @Override
        public Vector<Double> simVecSource(Vector<Integer> termVec) {
            double[] v = new double[K];
            for(int k = 0; k < K; k++) {
                for(Map.Entry<Integer,Integer> e : termVec.entrySet()) {
                    v[k] += A_kw[k][e.getKey()-1] * e.getValue();
                }
            }
            return new RealVector(v);
        }

        @Override
        public Vector<Double> simVecTarget(Vector<Integer> termVec) {
            
            double[] v = new double[K];
            for(int k = 0; k < K; k++) {
                for(Map.Entry<Integer,Integer> e : termVec.entrySet()) {
                    v[k] += B_kw[k][e.getKey()-1] * e.getValue();
                }
            }
            double[] v2 = new double[K];
            for(int k1 = 0; k1 < K; k1++) {
                for(int k2 = 0; k2 < K; k2++) {
                    v2[k1] += C_kk[k2][k1] * v[k2];
                }
            }
            double vnorm = 0.0;
            double v2norm = 0.0;
            for(int k = 0; k < K; k++) {
                vnorm += v[k]*v[k];
                v2norm += v2[k]*v2[k];
            }
            double norm = Math.sqrt(vnorm/v2norm);
            for(int k = 0; k < K; k++) {
                v2[k] *= norm;
            }
            return new RealVector(v2);
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
}
 