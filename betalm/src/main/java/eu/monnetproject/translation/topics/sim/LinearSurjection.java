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
package eu.monnetproject.translation.topics.sim;

import eu.monnetproject.translation.topics.SparseArray;
import eu.monnetproject.translation.topics.SparseRealArray;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author John McCrae
 */
public class LinearSurjection implements SimilarityMetric {

    private final int W;
    private final List<List<Surj>> surjes;

    public LinearSurjection(File file) throws IOException {
        final ParallelReader data = ParallelReader.fromFile(file);
        this.W = data.W();
        final SparseArray[][] Xt = transpose(data.x);
        final double[][] norms = norms(Xt);
        this.surjes = calculateSurjections(Xt, norms);
    }

    public LinearSurjection(SparseArray[][] x, int W) {
        this.W = W;
        final SparseArray[][] Xt = transpose(x);
        final double[][] norms = norms(Xt);
        this.surjes = calculateSurjections(Xt, norms);
    }

    @Override
    public double[] simVecSource(SparseArray termVec) {
        double[] vec = new double[W];
        for (Map.Entry<Integer, Integer> e : termVec.entrySet()) {
            if (surjes.get(e.getKey()) != null) {
                for (Surj surj : surjes.get(e.getKey())) {
                    vec[surj.to] = surj.weight * e.getValue();
                }
            }
        }
        return vec;
    }

    @Override
    public double[] simVecTarget(SparseArray termVec) {
        return termVec.toDoubleArray();
    }

    @Override
    public int W() {
        return W;
    }

    /**
     * Transpose the matrix to a WxJ matrix
     */
    private SparseArray[][] transpose(SparseArray[][] x) {
        final SparseArray[][] Xt = new SparseArray[W][2];
        for (int j = 0; j < x.length; j++) {
            for (int l = 0; l < 2; l++) {
                for (Map.Entry<Integer, Integer> e : x[j][l].entrySet()) {
                    if (Xt[e.getKey()][l] == null) {
                        Xt[e.getKey()][l] = new SparseArray(x.length);
                    }
                    Xt[e.getKey()][l].put(j, e.getValue());
                }
            }
        }
        return Xt;
    }

    /**
     * Calculate ||x||_2 (idx 0 and 1) and ||x||_1 (idx 2 and 3) for both
     * languages
     */
    private double[][] norms(SparseArray[][] Xt) {
        double[][] norms = new double[W][4];
        for (int w = 0; w < Xt.length; w++) {
            for (int l = 0; l < 2; l++) {
                if (Xt[w][l] == null) {
                    continue;
                }
                for (int x : Xt[w][l].values()) {
                    norms[w][l] += x * x;
                    norms[w][l + 2] += x;
                }
                norms[w][l] = Math.sqrt(norms[w][l]);
                //norms[w][l+2] /= W (not necessary, and leads to f.p. error)
            }
        }
        return norms;
    }
    private static final Random random = new Random();

    private List<List<Surj>> calculateSurjections(SparseArray[][] Xt, double[][] norms) {
        final List<List<Surj>> surjections = new ArrayList<List<Surj>>(W);
        for (int w = 0; w < W; w++) {
            surjections.add(null);
        }
        for (int w1 = 0; w1 < Xt.length; w1++) {
            if (w1 % 1000 == 0) {
                System.err.print(".");
            }
            double bestSim = 0.0;
            int bestX = -1;
            int ties = 0;
            if (Xt[w1][1] == null || Xt[w1][1].isEmpty()) {
                continue;
            }
            for (int w0 = 0; w0 < Xt.length; w0++) {
                double sim = 0.0;
                if (Xt[w0][0] == null || Xt[w1][1].isEmpty()) {
                    continue;
                }
                for (Map.Entry<Integer, Integer> j : Xt[w1][1].entrySet()) {
                    sim += Xt[w0][0].get(j.getKey()) * j.getValue();
                }
                sim = sim / norms[w1][1] / norms[w0][0];
                if ((sim > bestSim && (ties = 0) == 0)
                        || (sim == bestSim && random.nextInt(++ties) == 0)) {
                    bestSim = sim;
                    bestX = w0;
                }
            }
            if (bestX >= 0) {
                if (surjections.get(bestX) == null) {
                    surjections.set(bestX, new LinkedList<Surj>());
                }
                surjections.get(bestX).add(new Surj(w1, norms[w1][3] / norms[bestX][2]));
            }
        }
        return surjections;
    }

    private static class Surj {

        final int to;
        final double weight;

        public Surj(int to, double weight) {
            this.to = to;
            this.weight = weight;
        }
    }

}
