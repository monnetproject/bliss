/*********************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or within
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software within specific prior written permission.
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
package eu.monnetproject.bliss.lda;

import eu.monnetproject.bliss.NGramSimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetricFactory;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author John McCrae
 */
public class LDASimilarityMetricFactory implements SimilarityMetricFactory<InputStream> {

    @Override
    public SimilarityMetric makeMetric(InputStream data, int W) throws IOException {
        final DataInputStream in = new DataInputStream(data);
        final int L = in.readInt();
        assert(L==2);
        final int J = in.readInt();
        final int W2 = in.readInt();
        assert(W==W2);
        final int K = in.readInt();
        final double alpha = in.readDouble();
        assert(alpha >= 0);
        final double beta = in.readDouble();
        assert(beta >= 0);
        final int[][][] N_lkw = new int[L][K][W];
        for (int l = 0; l < 2; l++) {
            for (int k = 0; k < K; k++) {
                for (int w = 0; w < W; w++) {
                    N_lkw[l][k][w] = in.readInt();
                }
            }
        }
        final int[][] N_lk = new int[L][K];
        for (int l = 0; l < 2; l++) {
            for (int k = 0; k < K; k++) {
                N_lk[l][k] = in.readInt();
            }
        }
        in.close();
        return new LDASimilarityMetric(K, W, N_lkw, N_lk, alpha, beta);
    }

    @Override
    public NGramSimilarityMetric makeNGramMetric(InputStream dat, int W) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    
    @Override
    public Class<InputStream> datatype() {
        return InputStream.class;
    }
}
