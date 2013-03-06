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
package eu.monnetproject.bliss.clesa;

import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;

/**
 * Implements pseudo-Lucene ranking for documents
 * 
 * @author jmccrae
 */
public class LucenePracticalSimilarity implements CLESASimilarity {
    private final double[] idf;
    private final double[] norm;
    private final SparseIntArray[][] x;
    private final int l;

    public LucenePracticalSimilarity(SparseIntArray[][] x, int l, int W) {
        final int[] _idf = new int[W];
        final int J = x.length;
        assert(J > 0);
        for(int j = 0; j < J; j++) {
            for(int t : x[j][l].keySet()) {
                _idf[t]++;
            }
        }
        this.idf = new double[W];
        for(int w = 0; w < W; w++) {
            idf[w] = -Math.log((double)_idf[w] / J);
            if(Double.isInfinite(idf[w])) {
                idf[w] = -100.0;
            }
        }
        this.norm = new double[J];
        for(int j = 0; j < J; j++) {
            norm[j] = 1.0 / Math.sqrt(x[j][l].sum());
            if(Double.isInfinite(norm[j])) {
                norm[j] = 1.0;
            }
        }
        this.x = x;
        this.l = l;
    }
    
    public int idx(SparseIntArray d) {
        for(int i = 0; i < x.length; i++) {
            if(d == x[i][l]) {
                return i;
            }
        }
        return -1;
    }
    
    public double coord(Vector<Integer> q, SparseIntArray d) {
        int overlap = 0;
        int maxOverlap = 0;
        for(int t : q.keySet()) {
            if(d.intValue(t) != 0) {
                overlap++;
            }
            maxOverlap++;
        }
        if(maxOverlap != 0) {
            return (double)overlap / maxOverlap;
        } else {
            return 1.0;
        }
    }
    
    //public double queryNorm(Vector<Integer> q) {
    //    return 0.0;
    //}
    
    public double tf(int t, SparseIntArray d) {
        return d.doubleValue(t);
    }
    
    public double idf(int t) {
        return idf[t];
    }
    
    public double boost(int t, Vector<Integer> q) {
        return q.doubleValue(t);
    }
    
    public double norm(SparseIntArray d) {
        return 1.0 / Math.sqrt(d.sum());
    }
    
    @Override public double score(Vector<Integer> q, SparseIntArray d) {
        double score = 0.0;
        final int j = idx(d);
        for(int t : q.keySet()) {
            final double idf_t = idf(t);
            score += tf(t,d) * idf_t * idf_t * boost(t,q) * norm[j];
        }
        return coord(q, d) * score;
    }
}
