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
import eu.monnetproject.math.sparse.Vector;

/**
 *
 * @author John McCrae
 */
public class OkapiBM25 implements CLESASimilarity {
    private final SparseIntArray[][] x;
    private final int l;
    private final double k1 = 1.6;
    private final double b = 0.75;
    private final double avgdl;
    private final double[] idf;

    public OkapiBM25(SparseIntArray[][] x, int l, int W) {
        this.x = x;
        this.l = l;
        this.idf = new double[W];
        this.avgdl = init(W);
    }
    
    @Override
    public double score(Vector<Integer> q, SparseIntArray d) {
        double score = 0.0;
        double dl = d.sum();
        for(int t : q.keySet()) {
            final double f_q_d = d.doubleValue(t);
            final double x = f_q_d + k1 * (1 - b + b * dl / avgdl);
            if(x != 0.0) {
                score += q.doubleValue(t) * idf[t] * f_q_d * (k1 + 1) / x;
            }
        }
        return score;
    }

    private double init(int W) {
        int sumdl = 0;
        int[] df = new int[W];
        final int J = x.length;
        for(int j = 0; j < J; j++) {
            sumdl += x[j][l].sum();
            for(int t : x[j][l].keySet()) {
                df[t]++;
            }
        }
        for(int t = 0; t < W; t++) {
            idf[t] = Math.log((0.5 + J - df[t]) / (0.5 + df[t]));
        }
        return (double)sumdl / J;
    }

}
