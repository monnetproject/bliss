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
package eu.monnetproject.bliss.betalm.impl;

import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class Metrics {

    private static abstract class BetaSimFunctionImpl implements BetaSimFunction {

        @Override
        public double scoreNGrams(IntList document, int W) {
            return score(SparseIntArray.histogram(document.toIntArray(), W));
        }
    }

    private Metrics() {
    }

    public static <M extends Number, N extends Number> double cosSim(Vector<M> vec1, Vector<N> vec2, final StopWordList stopWordList) {
        double ab = 0.0;
        double a2 = 0.0;
        for (int i : vec1.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            ab += (double) vec2.value(i).doubleValue() * (double) vec1.value(i).doubleValue();
            a2 += (double) vec1.value(i).doubleValue() * (double) vec1.value(i).doubleValue();
        }
        double b2 = 0.0;
        for (int i : vec2.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            b2 += (double) vec2.value(i).doubleValue() * (double) vec2.value(i).doubleValue();
        }
        return a2 > 0 && b2 > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
    }

    public static BetaSimFunction cosSim(final Vector<Integer> vec1, final StopWordList stopWordList) {
        return new BetaSimFunctionImpl() {
            double a2 = Double.NaN;

            @Override
            public double score(Vector<Integer> vec2) {
                double ab = 0.0;
                if (Double.isNaN(a2)) {
                    a2 = 0.0;
                    for (int i : vec1.keySet()) {
                        if (stopWordList.contains(i)) {
                            continue;
                        }
                        a2 += (double) vec1.value(i).doubleValue() * (double) vec1.value(i).doubleValue();
                    }
                }
                double b2 = 0.0;
                for (int i : vec2.keySet()) {
                    if (stopWordList.contains(i)) {
                        continue;
                    }
                    ab += (double) vec2.value(i).doubleValue() * (double) vec1.value(i).doubleValue();
                    b2 += (double) vec2.value(i).doubleValue() * (double) vec2.value(i).doubleValue();
                }
                return a2 > 0 && b2 > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
            }
        };
    }

    public static double cosSim(double[] vec1, Vector<Integer> vec2, final StopWordList stopWordList) {
        double ab = 0.0;
        double a2 = 0.0;
        double b2 = 0.0;
        for (int i : vec2.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            ab += (double) vec2.value(i).doubleValue() * vec1[i];
            b2 += (double) vec2.value(i).doubleValue() * (double) vec2.value(i).doubleValue();
        }
        for (int i = 0; i < vec1.length; i++) {
            if (stopWordList.contains(i)) {
                continue;
            }
            a2 += vec1[i] * vec1[i];
        }
        return a2 > 0 && b2 > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
    }
    private static final double KLD_NEG_COST = -5;

    public static double kullbackLeiblerDivergence(Vector<Integer> vec1, Vector<Integer> vec2, final StopWordList stopWordList) {
        final double N1 = vec1.sum(), N2 = vec2.sum();
        double kld = 0.0;
        for (Map.Entry<Integer, Integer> e : vec1.entrySet()) {
            if (stopWordList.contains(e.getKey())) {
                continue;
            }
            final int tfj = vec2.value(e.getKey()).intValue();
            if (tfj != 0) {
                kld += (double) e.getValue() / N1 * Math.max(KLD_NEG_COST, Math.log(N2 / N1 * e.getValue() * tfj));
            } else {
                kld += (double) e.getValue() / N1 * KLD_NEG_COST;
            }
        }
        return kld;
    }

    public static BetaSimFunction kullbackLeiblerDivergence(final Vector<Integer> vec1, final StopWordList stopWordList) {
        return new BetaSimFunctionImpl() {
            @Override
            public double score(Vector<Integer> vec2) {
                return kullbackLeiblerDivergence(vec1, vec2, stopWordList);
            }
        };
    }

    public static double jaccardIndex(Vector<Integer> vec1, Vector<Integer> vec2, final StopWordList stopWordList) {
        int intersect = 0;
        int union = 0;

        for (Map.Entry<Integer, Integer> e : vec1.entrySet()) {
            if (stopWordList.contains(e.getKey())) {
                continue;
            }
            if (vec2.containsKey(e.getKey())) {
                intersect++;
            }
            union++;
        }

        for (Map.Entry<Integer, Integer> e : vec2.entrySet()) {
            if (stopWordList.contains(e.getKey())) {
                continue;
            }
            if (!vec1.containsKey(e.getValue())) {
                union++;
            }
        }

        return union == 0 ? 0.0 : (double) intersect / union;
    }

    public static BetaSimFunction jaccardIndex(final Vector<Integer> vec1, final StopWordList stopWordList) {
        return new BetaSimFunctionImpl() {
            @Override
            public double score(Vector<Integer> vec2) {
                return jaccardIndex(vec1, vec2, stopWordList);
            }
        };
    }

    public static double normalCosSim(Vector<Integer> vec1, Vector<Integer> vec2, Vector<Double> mu, double sumMu2, final StopWordList stopWordList) {
        final int v1sum = vec1.sum();
        final int v2sum = vec2.sum();
        if (v1sum == 0 || v2sum == 0) {
            return 0;
        }
        double ab = sumMu2 * v1sum * v2sum;
        double a2 = sumMu2 * v1sum * v1sum;
        for (int i : vec1.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            if (i > mu.size()) {
                continue;
            }
            final double v1i = vec1.value(i).doubleValue();// / v1sum;
            final double v2i = vec2.value(i).doubleValue();// / v2sum;
            ab += v2i * v1i - v2sum * mu.doubleValue(i) * v1i - v1sum * mu.doubleValue(i) * v2i;
            a2 += v1i * v1i - 2 * v1sum * mu.doubleValue(i) * v1i;
        }
        double b2 = sumMu2 * v2sum * v2sum;
        for (int i : vec2.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            if (i > mu.size()) {
                continue;
            }
            final double v2i = vec2.value(i).doubleValue();// / v2sum;
            if (!vec1.containsKey(i)) {
                ab -= v1sum * mu.doubleValue(i) * v2i;
            }
            b2 += v2i * v2i - 2 * v2sum * mu.doubleValue(i) * v2i;
        }
        return a2 > 0 && b2 > 0 && ab > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
    }

    public static BetaSimFunction normalCosSim(final Vector<Integer> vec1, final Vector<Double> mu, final double sumMu2, final StopWordList stopWordList) {
        return new BetaSimFunctionImpl() {
            double b2 = Double.NaN;
            int v1sum = 0;

            @Override
            public double score(Vector<Integer> vec2) {
                if (v1sum == 0) {
                    v1sum = vec1.sum();
                }
                final int v2sum = vec2.sum();
                if (v1sum == 0 || v2sum == 0) {
                    return 0;
                }
                double ab = sumMu2 * v1sum * v2sum;
                double a2 = sumMu2 * v1sum * v1sum;
                for (int i : vec2.keySet()) {
                    if (stopWordList.contains(i)) {
                        continue;
                    }
                    if (i > mu.size()) {
                        continue;
                    }
                    final double v1i = vec1.value(i).doubleValue();// / v1sum;
                    final double v2i = vec2.value(i).doubleValue();// / v2sum;
                    ab += v2i * v1i - v2sum * mu.doubleValue(i) * v1i - v1sum * mu.doubleValue(i) * v2i;
                    a2 += v1i * v1i - 2 * v1sum * mu.doubleValue(i) * v1i;
                }
                if (Double.isNaN(b2)) {
                    b2 = sumMu2 * v2sum * v2sum;
                    for (int i : vec1.keySet()) {
                        if (stopWordList.contains(i)) {
                            continue;
                        }
                        if (i > mu.size()) {
                            continue;
                        }
                        final double v2i = vec1.value(i).doubleValue();// / v2sum;
                        //if (!vec2.containsKey(i)) {
//                            ab -= v1sum * mu.doubleValue(i) * v2i;
                        //                      }
                        b2 += v2i * v2i - 2 * v2sum * mu.doubleValue(i) * v2i;
                    }
                }
                return a2 > 0 && b2 > 0 && ab > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
            }
        };
    }

    public static double diceCoefficient(Vector<Integer> vec1, Vector<Integer> vec2, final StopWordList stopWordList) {
        final int v1sum = vec1.size();
        final int v2sum = vec2.size();
        if (v1sum == 0 || v2sum == 0) {
            return 0;
        }
        int v12 = 0;
        for (Integer i : vec1.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            if (vec2.containsKey(i)) {
                v12++;
            }
        }
        return 2.0 * (double) v12 / (v1sum + v2sum);
    }

    public static BetaSimFunction diceCoefficient(final Vector<Integer> vec1, final StopWordList stopWordList) {
        return new BetaSimFunctionImpl() {
            @Override
            public double score(Vector<Integer> document) {
                return diceCoefficient(vec1, document, stopWordList);
            }
        };
    }

    public static double dfDiceCoefficient(Vector<Integer> vec1, Vector<Integer> vec2, Vector<Double> df, final StopWordList stopWordList) {
        double num = 0.0, denom = 0.0;
        for (Integer i : vec1.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            if (i >= df.size()) {
                continue;
            }
            if (vec2.containsKey(i)) {
                num += (1.0 - df.doubleValue(i));
            }
            denom += (1.0 - df.doubleValue(i));
        }
        for (int i : vec2.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            if (i >= df.size()) {
                continue;
            }
            denom += (1.0 - df.doubleValue(i));
        }
        return denom == 0.0 ? 0.0 : (2.0 * num / denom);
    }

    public static BetaSimFunction dfDiceCoefficient(final Vector<Integer> vec1, final Vector<Double> df, final StopWordList stopWordList) {
        return new BetaSimFunctionImpl() {
            @Override
            public double score(Vector<Integer> document) {
                return dfDiceCoefficient(vec1, document, df, stopWordList);
            }
        };
    }

    public static double dfJaccardCoefficient(Vector<Integer> vec1, Vector<Integer> vec2, Vector<Double> df, final StopWordList stopWordList) {
        double num = 0.0, denom = 0.0;
        for (Integer i : vec1.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            if (i >= df.size()) {
                continue;
            }
            if (vec2.containsKey(i)) {
                num += (1.0 - df.doubleValue(i));
            }
            if (!vec2.containsKey(i)) {
                denom += (1.0 - df.doubleValue(i));
            }
        }
        for (int i : vec2.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            if (i >= df.size()) {
                continue;
            }
            denom += (1.0 - df.doubleValue(i));
        }
        return denom == 0.0 ? 0.0 : num / denom;
    }

    public static BetaSimFunction dfJaccardCoefficient(final Vector<Integer> vec1, final Vector<Double> df, final StopWordList stopWordList) {
        return new BetaSimFunctionImpl() {
            @Override
            public double score(Vector<Integer> document) {
                return dfJaccardCoefficient(vec1, document, df, stopWordList);
            }
        };
    }

    public static double rogersTanimoto(Vector<Integer> vec1, Vector<Integer> vec2, final StopWordList stopWordList) {
        final int N = vec1.length();
        assert (vec2.length() == vec1.length());
        int diff = 0;
        for (Integer i : vec1.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            if (!vec2.containsKey(i)) {
                diff++;
            }
        }
        for (Integer i : vec2.keySet()) {
            if (stopWordList.contains(i)) {
                continue;
            }
            if (!vec1.containsKey(i)) {
                diff++;
            }
        }

        return (double) (N - diff) / (double) (N + diff);
    }

    public static BetaSimFunction rogersTanimoto(final Vector<Integer> vec1, final StopWordList stopWordList) {
        return new BetaSimFunctionImpl() {
            @Override
            public double score(Vector<Integer> document) {
                return rogersTanimoto(vec1, document, stopWordList);
            }
        };
    }

    public static BetaSimFunction smoothed(final BetaSimFunction function, final double selectivity, final double minimal) {
        return new BetaSimFunction() {
            @Override
            public double scoreNGrams(IntList document, int W) {
                return (1.0 - minimal) * Math.pow(function.scoreNGrams(document,W), selectivity) + minimal;
            }

            @Override
            public double score(Vector<Integer> document) {
                return (1.0 - minimal) * Math.pow(function.score(document), selectivity) + minimal;
            }
        };
    }

    public static BetaSimFunction smoothed(final BetaSimFunction function, final double selectivity, final double minimal, final double mean) {
        return new BetaSimFunction() {
            @Override
            public double scoreNGrams(IntList document, int W) {
                final double score = function.scoreNGrams(document,W);
                return Math.pow(score, selectivity) / mean + minimal;
            }
            
            @Override
            public double score(Vector<Integer> document) {
                final double score = function.score(document);
                return Math.pow(score, selectivity) / mean + minimal;
            }
        };
    }
}
