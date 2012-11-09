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
package eu.monnetproject.translation.topics.kcca;

import eu.monnetproject.translation.topics.SimilarityMetric;
import eu.monnetproject.translation.topics.SimilarityMetricFactory;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author John McCrae
 */
public class KCCASimilarityMetricFactory implements SimilarityMetricFactory<InputStream> {

    @Override
    public SimilarityMetric makeMetric(InputStream data, int W) throws IOException {
        final DataInputStream dis = new DataInputStream(data);
        final int L = dis.readInt();
        if(L != 2) {
            throw new IllegalArgumentException("Bad file");
        }
        final int W2 = dis.readInt();
        if(W != W2) {
            throw new IllegalArgumentException("Bad W");
        }
        final int K = dis.readInt();
        double[][][] U = new double[L][W][K];
        for(int l = 0; l < L; l++) {
            for(int w = 0; w < W; w++) {
                for(int k = 0; k < W; k++) {
                    U[l][w][k] = dis.readDouble();
                }
            }
        }
        dis.close();
        return new KCCASimilarityMetric(U);
    }

    @Override
    public Class<InputStream> datatype() {
        return InputStream.class;
    }

}
