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

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.bliss.ParallelReader;
import eu.monnetproject.bliss.SimilarityMetric;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class CLESA implements SimilarityMetric {

    public final SparseIntArray[][] x;
    //public final SparseIntArray[][] xt;
    /*
     public final int[] n;
     public final int[] n_f;*/
    public final int J;
    public final int W;
    public final CLESASimilarity[] similarity;
    /*public final double[] df;
     public final double[] df_f;
     public final double[] mu;
     public final double[] mu_f;
     public final double[] di;
     public final double[] di_f;
     public final Map<String, Integer> words;*/

    public CLESA(File data) throws IOException {
        final ParallelReader pr = ParallelReader.fromFile(data);
        this.J = pr.x.length;
        this.W = pr.W();
        this.x = pr.x;
        this.similarity = new CLESASimilarity[]{
            getSim(x, 0, W),
            getSim(x, 1, W)
        };
        //this.xt = transpose(pr.x);
        //initDF();
        //System.err.println("Finished loading CLESA data");
    }

    public CLESA(SparseIntArray[][] x, int W/*, String[] words*/) {
        this.J = x.length;
        this.W = W;
        this.x = x;
        this.similarity = new CLESASimilarity[]{
            getSim(x, 0, W),
            getSim(x, 1, W)
        };
//        this.n = new int[J];
//        this.n_f = new int[J];
//        this.xt = transpose(x);
//        this.words = new HashMap<String, Integer>();
//        if (words != null) {
//            for (int i = 0; i < words.length; i++) {
//                this.words.put(words[i], i);
//            }
//        }
//        this.df = new double[W];
//        this.df_f = new double[W];
//        this.mu = new double[W];
//        this.mu_f = new double[W];
//        this.di = new double[W];
//        this.di_f = new double[W];
//        initDF();
    }
    
    private CLESASimilarity getSim(SparseIntArray[][] x, int l, int W) {
        //return new NormalizedSimilarity(x, l, W);
        return new OkapiBM25(x, l, W);
    }

    /**
     * Transpose the matrix to a WxJ matrix
     */
//    private SparseIntArray[][] transpose(SparseIntArray[][] x) {
//        final SparseIntArray[][] Xt = new SparseIntArray[W][2];
//        for (int j = 0; j < x.length; j++) {
//            n[j] = x[j][0].sum();
//            n_f[j] = x[j][0].sum();
//            for (int l = 0; l < 2; l++) {
//                for (Int2IntMap.Entry e : x[j][l].int2IntEntrySet()) {
//                    if (Xt[e.getIntKey()][l] == null) {
//                        Xt[e.getIntKey()][l] = new SparseIntArray(x.length);
//                    }
//                    final int v = e.getIntValue();
//                    if (v != 0) { // So DF calculation doesn't go wrong
//                        Xt[e.getIntKey()][l].put(j, v);
//                    }
//                }
//            }
//        }
//        return Xt;
//    }
    @Override
    public Vector<Double> simVecSource(Vector<Integer> termVec) {
        double[] sim = new double[J];
        for (int j = 0; j < J; j++) {
            sim[j] = similarity[0].score(termVec, x[j][0]);
        }
//        for (Map.Entry<Integer, Integer> e : termVec.entrySet()) {
//            double termVecFreq = (double) e.getValue();
//            final int w = e.getKey();
//            if (e.getKey() >= W || xt[e.getKey()][0] == null) {
//                continue;
//            }
//            final SparseIntArray v = xt[w][0];
//            kernel(sim, termVecFreq, v, w, J, n, df);
//        }
        return new RealVector(sim);
    }

    @Override
    public Vector<Double> simVecTarget(Vector<Integer> termVec) {
        double[] sim = new double[J];
        for (int j = 0; j < J; j++) {
            sim[j] = similarity[1].score(termVec, x[j][1]);
        }
//        for (Map.Entry<Integer, Integer> e : termVec.entrySet()) {
//            double termVecFreq = (double) e.getValue();
//            final int w = e.getKey();
//            if (xt[w][1] == null) {
//                continue;
//            }
//            final SparseIntArray v = xt[w][1];
//            kernel(sim, termVecFreq, v, w, J, n_f, df_f);
//        }
        return new RealVector(sim);
    }
    private static PrintWriter out;

    private static void R_printHeader(int W) {
        try {
            out = new PrintWriter("df.csv");
        } catch (FileNotFoundException x) {
            x.printStackTrace();
        }
        out.print("varName");
        for (int i = 0; i < W; i++) {
            out.print(",W" + i);
        }
        out.println();
    }

    private static void R_print(String varName, double[] d) {
        out.print(varName);
        for (int i = 0; i < d.length; i++) {
            out.print("," + d[i]);
        }
        out.println();
        out.flush();
    }

//    private void initDF() {
//        double sum = 0.0, sum_f = 0.0;
//        for (int w = 0; w < W; w++) {
//            sum += xt[w][0] == null ? 0 : xt[w][0].sum();
//            sum_f += xt[w][1] == null ? 0 : xt[w][1].sum();
//        }
//        for (int w = 0; w < W; w++) {
//            df[w] = xt[w][0] == null ? 0 : (double) xt[w][0].size() / xt[w][0].n();
//            df_f[w] = xt[w][1] == null ? 0 : (double) xt[w][1].size() / xt[w][1].n();
//            mu[w] = xt[w][0] == null ? 0 : (double) xt[w][0].sum() / sum;
//            mu_f[w] = xt[w][1] == null ? 0 : (double) xt[w][1].sum() / sum_f;
//            if (xt[w][0] != null) {
//                for (Int2IntMap.Entry e : xt[w][0].int2IntEntrySet()) {
//                    di[w] += e.getIntValue() * e.getIntValue();
//                }
//            } else {
//                //di[w] = 1;
//            }
//            if (xt[w][1] != null) {
//                for (Int2IntMap.Entry e : xt[w][1].int2IntEntrySet()) {
//                    di_f[w] += e.getIntValue() * e.getIntValue();
//                }
//            } else {
//                //di[w] = 1;
//            }
//        }
//        //R_printHeader(W);
//        //R_print("di",di);
//        //R_print("di_f",di_f);
//        //R_print("df",df);
//        //R_print("df_f",df_f);
//        //out.close();
//    }

    @Override
    public int W() {
        return W;
    }

    @Override
    public int K() {
        return J;
    }
    private static final double p = Double.parseDouble(System.getProperty("p", "1.0"));

    private static void kernel(double[] sim, double termVecFreq, final SparseIntArray v, final int w, final int J, int[] n, double[] mu) {
        for (int j = 0; j < J; j++) {
            if (n[j] != 0) {
                final double dffw = mu[w];
                final double t = termVecFreq * v.get(j);// / n[j];
                if (dffw != 0.0 && t != 0.0) {
                    //sim[j] += Math.pow(t / dffw, p);
                    sim[j] += t * Math.log(1 / dffw);
                }
            }
        }
    }
}
