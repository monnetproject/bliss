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
 * Sorg Association metric.
 * Based on "Cross-lingual Information Retrieval with Explicit Semantic Analysis" by 
 * P. Sorg and P. Cimiano
 * 
 * @author John McCrae
 */
public class SorgAssociation implements CLESASimilarity {

    private final double[] idf;
    private final double[] norm;
    private final SparseIntArray[][] x;
    private final int l;
    
    
    public SorgAssociation(SparseIntArray[][] x, int l, int W) {
        this.x = x;
        this.l = l;
        this.idf = new double[W];
        this.norm = new double[x.length];
        init(W);
    }
    
    private int idx(SparseIntArray d) {
        for(int i = 0; i < x.length; i++) {
            if(d == x[i][l]) {
                return i;
            }
        }
        return -1;
    }

    private void init(int W) {
        final int[] _idf = new int[W];
        
        for(int j = 0; j < x.length; j++) {
            for(int w : x[j][l].keySet()) {
                _idf[w]++;
            }
            // sqrt(|a|)^-1
            final int moda = x[j][l].sum();
            if(moda != 0) {
                norm[j] = 1.0 / Math.sqrt(moda);
            }
        }
        for(int w = 0; w < W; w++) {
            //                number of articles containing w_j
            // idf(w) = 1 +  -----------------------------------
            //                              |W| + 1
            if(_idf[w] != 0) {
                idf[w] = 1.0 + Math.log((double)_idf[w] / (x.length + 1));
            }
        }
    }

    @Override
    public double score(Vector<Integer> q, SparseIntArray d) {
        double Ct = 0.0;
        double tfidf = 0.0;
        for(int w : q.keySet()) {
            Ct += Math.abs(idf[w]);
            if(d.containsKey(w)) {
                // tf = sqrt(number of occurrences of w in d)
                // tfidf = sum_{t \in q}(tf(t) * idf(t))
                tfidf += q.doubleValue(w) * Math.sqrt(d.doubleValue(w)) * idf[w];
            }
        }
        if(Ct != 0.0) {
            //              1
            // Ct = -------------------
            //       sqrt(sum(idf(w)))
            Ct = 1.0 / Math.sqrt(Ct);
        }
        return Ct * norm[idx(d)] * tfidf;
    }
    
    
}
