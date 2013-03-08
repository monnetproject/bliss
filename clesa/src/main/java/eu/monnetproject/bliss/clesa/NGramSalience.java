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

import eu.monnetproject.bliss.NGram;
import eu.monnetproject.bliss.NGramSimilarityMetric;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.Vector;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;

/**
 *
 * @author jmccrae
 */
public class NGramSalience implements NGramSimilarityMetric {

    private final Object2IntMap<NGram>[][] x;
    private final Object2IntMap<NGram>[] ctTotal;
    private final int W, J;

    public NGramSalience(Object2IntMap<NGram>[][] x, int W) {
        this.x = x;
        this.ctTotal = new Object2IntMap[] {
            new Object2IntRBTreeMap<NGram>(),
            new Object2IntRBTreeMap<NGram>()
        };
        this.W = W;
        this.J =  x.length;
        init();
    }

    @Override
    public Vector<Double> simVecSource(Object2IntMap<NGram> termVec) {
        return RealVector.make(calculateSalience(termVec, 0));
    }

    @Override
    public Vector<Double> simVecTarget(Object2IntMap<NGram> termVec) {
        return RealVector.make(calculateSalience(termVec, 1));
    }

    @Override
    public int W() {
        return W;
    }

    @Override
    public int K() {
        return J;
    }

    private int sum(Object2IntMap<NGram> q) {
        int sum = 0;
        for (Object2IntMap.Entry<NGram> e : q.object2IntEntrySet()) {
            sum += e.getIntValue();
        }
        return sum;
    }

    
    
    public double[] calculateSalience(Object2IntMap<NGram> query, int l) {
        double[] salience = new double[J];
        for (int j = 0; j < J; j++) {
            final double xDocLength = sum(x[j][l]);
            for (Object2IntMap.Entry<NGram> ng : x[j][l].object2IntEntrySet()) {
                if (ctTotal[l].containsKey(ng.getKey()) && query.containsKey(ng.getKey())) {
                    salience[j] += (double) query.getInt(ng.getKey()) * ng.getIntValue() / ctTotal[l].getInt(ng.getKey());
                }
            }
            if (xDocLength != 0.0) {
                salience[j] /= xDocLength;
            }
        }
        return salience;
    }
    
    private void init() {
        for (int j = 0; j < J; j++) {
            for(Object2IntMap.Entry<NGram> ng : x[j][0].object2IntEntrySet()) {
                if(ctTotal[0].containsKey(ng.getKey())) {
                    ctTotal[0].put(ng.getKey(), ctTotal[0].getInt(ng.getKey())+ng.getIntValue());
                } else {
                    ctTotal[0].put(ng.getKey(), ng.getIntValue());
                }
            }
            for(Object2IntMap.Entry<NGram> ng : x[j][1].object2IntEntrySet()) {
                if(ctTotal[1].containsKey(ng.getKey())) {
                    ctTotal[1].put(ng.getKey(), ctTotal[1].getInt(ng.getKey())+ng.getIntValue());
                } else {
                    ctTotal[1].put(ng.getKey(), ng.getIntValue());
                }
            }
        }
    }
}
