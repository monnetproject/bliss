/**********************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
package eu.monnetproject.translation.topics.experiments;

import eu.monnetproject.translation.topics.lda.Estimator;
import eu.monnetproject.translation.topics.lda.GibbsData;
import eu.monnetproject.translation.topics.lda.PolylingualGibbsData;
import eu.monnetproject.translation.topics.SparseArray;
import eu.monnetproject.translation.topics.sim.SimilarityMetric;

/**
 *
 * @author John McCrae
 */
public class WxWLDAParaSim implements SimilarityMetric {
    final PolylingualGibbsData polylingualGibbsData;
    final GibbsData data2;
    final int l1, l2;
    final Estimator estimator = new Estimator();

    public WxWLDAParaSim(PolylingualGibbsData polylingualGibbsData, int l1, int l2) {
        this.polylingualGibbsData = polylingualGibbsData;
        this.l1 = l1;
        this.l2 = l2;
        this.data2 = polylingualGibbsData.monolingual(l2);
    }
    
    @Override
    public double[] simVecSource(SparseArray termVec) {
        double[] sim = new double[polylingualGibbsData.W];
        int[] d = toDocument(termVec);
        double[] p = estimator.topics(d, l1, polylingualGibbsData, 100);
        for(int w = 0; w < polylingualGibbsData.W; w++) {
            sim[w] = estimator.wordProb(w, p, data2);
        }
        return sim;
    }

    @Override
    public double[] simVecTarget(SparseArray termVec) {
        return termVec.toDoubleArray();
    }

    private int[] toDocument(SparseArray termVec) {
        final int[] vec = new int[termVec.sum()];
        int j = 0;
        for(int w : termVec.keySet()) {
            final int n = termVec.get(w);
            for(int i = 0; i < n; i++) {
                vec[j++] = w;
            }
        }
        return vec;
    }

    public int W() { return polylingualGibbsData.W; }
}
