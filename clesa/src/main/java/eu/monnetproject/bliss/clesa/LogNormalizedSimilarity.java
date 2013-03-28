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
 * @author jmccrae
 */
public class LogNormalizedSimilarity implements CLESASimilarity {
    private final int[] tf;
    private final boolean useTf;

    public LogNormalizedSimilarity(SparseIntArray[][] x, int l, int W) {
        this.tf = new int[W];
        this.useTf = Boolean.parseBoolean(System.getProperty("tfNorm","true"));
        final int J = x.length;
        for(int j = 0; j < J; j++) {
            for(int t : x[j][l].keySet()) {
                tf[t] += useTf ? x[j][l].intValue(t) : 1.0;
            }
        }
    }
    
    @Override
    public double score(Vector<Integer> q, SparseIntArray d) {
        double score = 0.0;
        double norm = 0.0;
        for(int t : d.keySet()) {
            norm += d.doubleValue(t) * d.doubleValue(t) * -Math.log(1.0 / (1.0 + tf[t]));
        }
        norm = Math.sqrt(norm);
        for(int t : q.keySet()) {
            if(tf[t] != 0.0) {
                score += q.doubleValue(t) * d.doubleValue(t) * -Math.log(1.0 / (1.0 + tf[t]));
            }
        }
        return score / norm;
    }
}

