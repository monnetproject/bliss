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
package eu.monnetproject.translation.topics.clesa;

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.translation.topics.SimilarityMetric;
import eu.monnetproject.translation.topics.sim.ParallelReader;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class CLESA implements SimilarityMetric {

    //public final SparseIntArray[][] x; 
    public final SparseIntArray[][] xt; 
    public final int[] n;
    public final int[] n_f;
    public final int J;
    public final int W;
    public final double[] df;
    public final double[] df_f;
    public final Map<String, Integer> words;

    public CLESA(File data) throws IOException {
        final ParallelReader pr = ParallelReader.fromFile(data);
        this.J = pr.x.length;
        this.n = new int[J];
        this.n_f = new int[J];
        this.W = pr.W();
        this.xt = transpose(pr.x);
        this.words = pr.words;
        this.df = new double[W];
        this.df_f = new double[W];
        initDF();
        System.err.println("Finished loading CLESA data");
    }

    public CLESA(SparseIntArray[][] x, int W, String[] words) {
        this.J = x.length;
        this.W = W;
        this.n = new int[J];
        this.n_f = new int[J];
        this.xt = transpose(x);
        this.words = new HashMap<String, Integer>();
        if(words != null) {
            for (int i = 0; i < words.length; i++) {
                this.words.put(words[i], i);
            }
        }
        this.df = new double[W];
        this.df_f = new double[W];
        initDF();
    }
    
    
    /**
     * Transpose the matrix to a WxJ matrix
     */
    private SparseIntArray[][] transpose(SparseIntArray[][] x) {
        final SparseIntArray[][] Xt = new SparseIntArray[W][2];
        for (int j = 0; j < x.length; j++) {
            n[j] = x[j][0].sum();
            n_f[j] = x[j][0].sum();
            for (int l = 0; l < 2; l++) {
                for (Int2IntMap.Entry e : x[j][l].int2IntEntrySet()) {
                    if (Xt[e.getIntKey()][l] == null) {
                        Xt[e.getIntKey()][l] = new SparseIntArray(x.length);
                    }
                    final int v = e.getIntValue();
                    if(v != 0) { // So DF calculation doesn't go wrong
                        Xt[e.getIntKey()][l].put(j, v);
                    }
                }
            }
        }
        return Xt;
    }

    @Override
    public Vector<Double> simVecSource(Vector<Integer> termVec) {
        double[] sim = new double[J];
        for(Map.Entry<Integer,Integer> e : termVec.entrySet()) {
            double termVecFreq = (double)e.getValue();
            if(e.getKey() >= W || xt[e.getKey()][0] == null) {
                continue;
            }
            for(int j = 0; j < J; j++) {
                if(n[j] != 0) {
                    sim[j] += (termVecFreq * xt[e.getKey()][0].get(j)) / n[j] * -1.0 * Math.log(df[e.getKey()]);
                }
             }
        }
        return new RealVector(sim);
    }

    @Override
    public Vector<Double> simVecTarget(Vector<Integer> termVec) {
        double[] sim = new double[J];
        for(Map.Entry<Integer,Integer> e : termVec.entrySet()) {
            double termVecFreq = (double)e.getValue();
            if(xt[e.getKey()][1] == null) {
                continue;
            }
            for(int j = 0; j < J; j++) {
                if(n_f[j] != 0) {
                    sim[j] += (termVecFreq * xt[e.getKey()][1].get(j)) / n_f[j] * -1.0 * Math.log(df_f[e.getKey()]);
                }
             }
        }
        return new RealVector(sim);
    }
    
    private void initDF() {
        for (int w = 0; w < W; w++) {
            df[w] = xt[w][0] == null ? 0 : (double)xt[w][0].size() / xt[w][0].n();
            df_f[w] = xt[w][1] == null ? 0 : (double)xt[w][1].size()/ xt[w][1].n();
        }
    }

    @Override
    public int W() {
        return W;
    }
}
