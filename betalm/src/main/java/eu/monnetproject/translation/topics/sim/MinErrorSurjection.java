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

import eu.monnetproject.math.sparse.Integer2DoubleVector;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.translation.topics.SimilarityMetric;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author John McCrae
 */
public class MinErrorSurjection implements SimilarityMetric {

    private final int W;
    private final List<List<Surj>> surjes;

    public MinErrorSurjection(File file) throws IOException {
        final ParallelReader data = ParallelReader.fromFile(file);
        this.W = data.W();
        final SparseIntArray[][] Xt = transpose(data.x);
        this.surjes = calculateSurjections(Xt, invMap(data.words));
    }

    public MinErrorSurjection(SparseIntArray[][] x, int W) throws FileNotFoundException {
        this.W = W;
        final SparseIntArray[][] Xt = transpose(x);
        this.surjes = calculateSurjections(Xt, null);
    }

    private Map<Integer, String> invMap(Map<String, Integer> map) {
        final HashMap<Integer, String> invMap = new HashMap<Integer, String>();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            invMap.put(e.getValue(), e.getKey());
        }
        return invMap;
    }

    @Override
    public Vector<Double> simVecSource(Vector<Integer> termVec) {
        double[] vec = new double[W];
        for (Map.Entry<Integer, Integer> e : termVec.entrySet()) {
            if (surjes.get(e.getKey()) != null) {
                for (Surj surj : surjes.get(e.getKey())) {
                    vec[surj.to] = surj.weight * e.getValue();
                }
            }
        }
        return new RealVector(vec);
    }

    @Override
    public Vector<Double> simVecTarget(Vector<Integer> termVec) {
        return new Integer2DoubleVector(termVec);
    }

    @Override
    public int W() {
        return W;
    }

    /**
     * Transpose the matrix to a WxJ matrix
     */
    private SparseIntArray[][] transpose(SparseIntArray[][] x) {
        final SparseIntArray[][] Xt = new SparseIntArray[W][2];
        for (int j = 0; j < x.length; j++) {
            for (int l = 0; l < 2; l++) {
                for (Int2IntMap.Entry e : x[j][l].int2IntEntrySet()) {
                    if (Xt[e.getIntKey()][l] == null) {
                        Xt[e.getIntKey()][l] = new SparseIntArray(x.length);
                    }
                    Xt[e.getIntKey()][l].put(j, e.getIntValue());
                }
            }
        }
        return Xt;
    }
    private static final Random random = new Random();

    private List<List<Surj>> calculateSurjections(SparseIntArray[][] Xt, Map<Integer, String> words) throws FileNotFoundException {
        System.out.println("Calculating surjections");
        final PrintWriter out = words == null ? null : new PrintWriter("mesurj.corresponds");
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
            double bestW = Double.NaN;
            int ties = 0;
            if (Xt[w1][1] == null || Xt[w1][1].isEmpty()) {
                out.println(words.get(w1) + "\tNaN\t?");
                continue;
            }
            for (int w0 = 0; w0 < Xt.length; w0++) {
                if (Xt[w0][0] == null || Xt[w1][1].isEmpty()) {
                    continue;
                }
                final double wt = minError(Xt[w1][1], Xt[w0][0],W);
                final double sim = errorDelta(Xt[w1][1], Xt[w0][0], wt);
                if ((sim > bestSim && (ties = 0) == 0)
                        || (sim == bestSim && random.nextInt(++ties) == 0)) {
                    bestSim = sim;
                    bestX = w0;
                    bestW = wt;
                }
            }
            if (bestX >= 0) {
                if (surjections.get(bestX) == null) {
                    surjections.set(bestX, new LinkedList<Surj>());
                }
                surjections.get(bestX).add(new Surj(w1, bestW));

                if (words != null) {
                    out.println(words.get(w1) + "\t" + bestSim + "\t" + words.get(bestX));
                }
            } else if (words != null) {
                out.println(words.get(w1) + "\tNaN\t?");

            }
        }
        out.flush();
        out.close();
        System.out.println("Done calculating surjections");
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

    public static double errorDelta(SparseIntArray x, SparseIntArray y, double a) {
        double xSum = 0.0;
        double error = 0.0;

        for (Map.Entry<Integer, Integer> ex : x.entrySet()) {
            xSum += ex.getValue();
            if (y.containsKey(ex.getKey())) {
                error += Math.abs(ex.getValue() - a * y.get(ex.getKey()));
            }
        }
        return xSum - error;
    }

    /**
     * For two given vectors return a value, a, that minimizes \Sum | x - ay |
     *
     * @param x The sparse array x
     * @param y The sparse array y
     * @return The weight that minimizes the error
     */
    public static double minError(SparseIntArray x, SparseIntArray y, int W) {
        final SparseRealArray alpha = new SparseRealArray(W);
        // d is derivate of error at -Inf
        double d = 0.0;
        for (Int2IntMap.Entry ex : x.int2IntEntrySet()) {
            if (y.containsKey(ex.getKey())) {
                final double yi = (double) y.get(ex.getKey());
                if(yi == 0.0)
                    continue;
                alpha.put(ex.getIntKey(), (double) ex.getIntValue() / yi);
                d -= yi;
            }
        }
        // i.e., x intersect y = 0
        if (alpha.isEmpty()) {
            return 0.0;
        }

        // We are taking a quick sort like approach so we pop the first alpha
        // value and then split all alpha values according to this value
        final Map.Entry<Integer, Double> alpha0 = alpha.entrySet().iterator().next();
        final List<Map.Entry<Integer, Double>> left = new ArrayList<Map.Entry<Integer, Double>>(alpha.size());
        final List<Map.Entry<Integer, Double>> right = new ArrayList<Map.Entry<Integer, Double>>(alpha.size());
        for (Map.Entry<Integer, Double> e : alpha.entrySet()) {
            if (e.getValue() <= alpha0.getValue()) {
                left.add(e);
            } else {
                right.add(e);
            }
        }
        final double weight = minErrorSolve(d, alpha0, left, right, y).weight;
        if(Double.isNaN(weight)) {
            throw new RuntimeException(x.toString() + "\n\n\n" + y.toString());
        }
        // Now we apply quicksort recursively
        return weight;
    }

    private static class WeightD {

        double weight;
        double d;

        public WeightD(double weight, double d) {
            this.weight = weight;
            this.d = d;
        }
    }

    // This function is looking for the point where the derivate of the error is
    // zero. Assuming the vectors are both positive this function is a monotonically
    // increasing step function. Therefore to evaluate we calculate the derivate only
    // at the steps
    private static WeightD minErrorSolve(double d, Map.Entry<Integer, Double> alpha0, List<Map.Entry<Integer, Double>> left, List<Map.Entry<Integer, Double>> right, SparseIntArray y) {
        assert (!left.isEmpty()); // as alpha0 \in left
        assert (d < 0); // as we have not found a suitable d value yet
        if (left.size() == 1) {
            // We are at a leaf node
            d += 2 * y.get(alpha0.getKey());
            if (d >= 0) {
                // We have now crossed the horizontal! This is the point of minimal error
                return new WeightD(alpha0.getValue(), d);
            } else if (right.isEmpty()) {
                // The solution is not in this branch... backtrack
                return new WeightD(Double.NaN, d);
            } else {
                // Check the right branch
                final Map.Entry<Integer, Double> newAlpha0 = right.iterator().next();
                final List<Map.Entry<Integer, Double>> newLeft = new ArrayList<Map.Entry<Integer, Double>>(right.size());
                final List<Map.Entry<Integer, Double>> newRight = new ArrayList<Map.Entry<Integer, Double>>(right.size());
                for (Map.Entry<Integer, Double> e : right) {
                    if (e.getValue() <= newAlpha0.getValue()) {
                        newLeft.add(e);
                    } else {
                        newRight.add(e);
                    }
                }
                return minErrorSolve(d, newAlpha0, newLeft, newRight, y);
            }
        } else {
            // Get next element of left, split at it and solve
            final Map.Entry<Integer, Double> alpha1 = left.get(1);
            final List<Map.Entry<Integer, Double>> newLeft = new ArrayList<Map.Entry<Integer, Double>>(left.size());
            final List<Map.Entry<Integer, Double>> newRight = new ArrayList<Map.Entry<Integer, Double>>(left.size());
            for (Map.Entry<Integer, Double> e : left) {
                if (e != alpha0 && e.getValue() <= alpha1.getValue()) {
                    newLeft.add(e);
                } else if (e != alpha0) {
                    newRight.add(e);
                }
            }
            final WeightD x = minErrorSolve(d, alpha1, newLeft, newRight, y);
            if (!Double.isNaN(x.weight)) {
                // The solve succeeded :)
                return x;
            } else {
                // The solve did not succeed
                d = x.d;
                d += 2 * y.get(alpha0.getKey());
                if (d >= 0) {
                    // But this pivot pushed over the edge :)
                    return new WeightD(alpha0.getValue(), d);
                } else if(right.isEmpty()) {
                    // No right branch... we must go back up
                    return new WeightD(Double.NaN, d);
                } else {
                    // Still not solved we step into the right branch
                    final Map.Entry<Integer, Double> alpha2 = right.get(0);
                    newLeft.clear();
                    newRight.clear();

                    for (Map.Entry<Integer, Double> e : right) {
                        if (e.getValue() <= alpha2.getValue()) {
                            newLeft.add(e);
                        } else if (e != alpha0) {
                            newRight.add(e);
                        }
                    }
                    return minErrorSolve(d, alpha2, newLeft, newRight, y);
                }
            }
        }
    }
}
