/*********************************************************************************
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
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class Metrics {

    private Metrics() { }
    
    public static double cosSim(SparseArray vec1, SparseArray vec2) {
        double ab = 0.0;
        double a2 = 0.0;
        for (int i : vec1.keySet()) {
            ab += (double) vec2.get(i) * (double) vec1.get(i);
            a2 += (double) vec1.get(i) * (double) vec1.get(i);
        }
        double b2 = 0.0;
        for (int i : vec2.keySet()) {
            b2 += (double) vec2.get(i) * (double) vec2.get(i);
        }
        return a2 > 0 && b2 > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
    }
    
    public static BetaSimFunction cosSim(final SparseArray vec1) {
        return new BetaSimFunction() {

            @Override
            public double score(SparseArray vec2) {
                return cosSim(vec1,vec2);
            }
        };
    }
    
    public static double cosSim(double[] vec1, SparseArray vec2) {
        double ab = 0.0;
        double a2 = 0.0;
        double b2 = 0.0;
        for (int i : vec2.keySet()) {
            ab += (double) vec2.get(i) * vec1[i];
            b2 += (double) vec2.get(i) * (double) vec2.get(i);
        }
        for (int i = 0; i < vec1.length; i++) {
            a2 += vec1[i] * vec1[i];
        }
        return a2 > 0 && b2 > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
    }
    
    private static final double KLD_NEG_COST = -5;

    public static double kullbackLeiblerDivergence(SparseArray vec1, SparseArray vec2) {
        final double N1 = vec1.sum(), N2 = vec2.sum();
        double kld = 0.0;
        for (Map.Entry<Integer, Integer> e : vec1.entrySet()) {
            final int tfj = vec2.get(e.getKey());
            if (tfj != 0) {
                kld += (double) e.getValue() / N1 * Math.max(KLD_NEG_COST, Math.log(N2 / N1 * e.getValue() * tfj));
            } else {
                kld += (double) e.getValue() / N1 * KLD_NEG_COST;
            }
        }
        return kld;
    }
    
    public static BetaSimFunction kullbackLeiblerDivergence(final SparseArray vec1) {
        return new BetaSimFunction() {

            @Override
            public double score(SparseArray vec2) {
                return kullbackLeiblerDivergence(vec1, vec2);
            }
        };
    }

    public static double jaccardIndex(SparseArray vec1, SparseArray vec2) {
        int intersect = 0;
        int union = 0;

        for (Map.Entry<Integer, Integer> e : vec1.entrySet()) {
            if (vec2.containsKey(e.getKey())) {
                intersect++;
            }
            union++;
        }

        for (Map.Entry<Integer, Integer> e : vec2.entrySet()) {
            if (!vec1.containsKey(e.getValue())) {
                union++;
            }
        }

        return union == 0 ? 0.0 : (double) intersect / union;
    }
    
    public static BetaSimFunction jaccardIndex(final SparseArray vec1) {
        return new BetaSimFunction() {

            @Override
            public double score(SparseArray vec2) {
                return jaccardIndex(vec1, vec2);
            }
        };
    }
    
    public static double normalCosSim(SparseArray vec1, SparseArray vec2, double[] mu, double sumMu2) {
        final int v1sum = vec1.sum();
        final int v2sum = vec2.sum();
        if (v1sum == 0 || v2sum == 0) {
            return 0;
        }
        double ab = sumMu2 * v1sum * v2sum;
        double a2 = sumMu2 * v1sum * v1sum;
        for (int i : vec1.keySet()) {
            if (i > mu.length) {
                continue;
            }
            final double v1i = vec1.get(i);// / v1sum;
            final double v2i = vec2.get(i);// / v2sum;
            ab += v2i * v1i - v2sum * mu[i] * v1i - v1sum * mu[i] * v2i;
            a2 += v1i * v1i - 2 * v1sum * mu[i] * v1i;
        }
        double b2 = sumMu2 * v2sum * v2sum;
        for (int i : vec2.keySet()) {
            if (i > mu.length) {
                continue;
            }
            final double v2i = vec2.get(i);// / v2sum;
            if (!vec1.containsKey(i)) {
                ab -= v1sum * mu[i] * v2i;
            }
            b2 += v2i * v2i - 2 * v2sum * mu[i] * v2i;
        }
        return a2 > 0 && b2 > 0 && ab > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
    }
    
    public static BetaSimFunction normalCosSim(final SparseArray vec1, final double[] mu, final double sumMu2) {
        return new BetaSimFunction() {

            @Override
            public double score(SparseArray vec2) {
                return normalCosSim(vec1, vec2, mu, sumMu2);
            }
        };
    }

    public static double diceCoefficient(SparseArray vec1, SparseArray vec2) {
        final int v1sum = vec1.size();
        final int v2sum = vec2.size();
        if (v1sum == 0 || v2sum == 0) {
            return 0;
        }
        int v12 = 0;
        for (Integer i : vec1.keySet()) {
            if (vec2.containsKey(i)) {
                v12++;
            }
        }
        return 2.0 * (double) v12 / (v1sum + v2sum);
    }
    
    public static BetaSimFunction diceCoefficient(final SparseArray vec1) {
        return new BetaSimFunction() {

            @Override
            public double score(SparseArray document) {
                return diceCoefficient(vec1, document);
            }
        };
    }

    public static double dfDiceCoefficient(SparseArray vec1, SparseArray vec2, double[] df) {
        double num = 0.0, denom = 0.0;
        for (Integer i : vec1.keySet()) {
            if(i >= df.length)
                continue;
            if (vec2.containsKey(i)) {
                num += (1.0 - df[i]);
            }
            denom += (1.0 - df[i]);
        }
        for (int i : vec2.keySet()) {
            if(i >= df.length)
                continue;
            denom += (1.0 - df[i]);
        }
        return denom == 0.0 ? 0.0 : (2.0 * num / denom);
    }
    
    public static BetaSimFunction dfDiceCoefficient(final SparseArray vec1, final double[] df) {
        return new BetaSimFunction() {

            @Override
            public double score(SparseArray document) {
                return dfDiceCoefficient(vec1, document, df);
            }
        };
    }
    
    public static double dfJaccardCoefficient(SparseArray vec1, SparseArray vec2, double[] df) {
        double num = 0.0, denom = 0.0;
        for (Integer i : vec1.keySet()) {
            if(i >= df.length)
                continue;
            if (vec2.containsKey(i)) {
                num += (1.0 - df[i]);
            }
            if(!vec2.containsKey(i))  {
                denom += (1.0 - df[i]);
            }
        }
        for (int i : vec2.keySet()) {
            if(i >= df.length)
                continue;
            denom += (1.0 - df[i]);
        }
        return denom == 0.0 ? 0.0 : num / denom;
    }
    
    public static BetaSimFunction dfJaccardCoefficient(final SparseArray vec1, final double[] df) {
        return new BetaSimFunction() {

            @Override
            public double score(SparseArray document) {
                return dfJaccardCoefficient(vec1, document, df);
            }
        };
    }

    public static double rogersTanimoto(SparseArray vec1, SparseArray vec2) {
        final int N = vec1.n();
        assert (vec2.n() == vec1.n());
        int diff = 0;
        for (Integer i : vec1.keySet()) {
            if (!vec2.containsKey(i)) {
                diff++;
            }
        }
        for (Integer i : vec2.keySet()) {
            if (!vec1.containsKey(i)) {
                diff++;
            }
        }

        return (double) (N - diff) / (double) (N + diff);
    }
    
    public static BetaSimFunction rogersTanimoto(final SparseArray vec1) {
        return new BetaSimFunction() {

            @Override
            public double score(SparseArray document) {
                return rogersTanimoto(vec1, document);
            }
        };
    }
    
    public static BetaSimFunction smoothed(final BetaSimFunction function, final double selectivity, final double minimal) {
        return new BetaSimFunction() {

            @Override
            public double score(SparseArray document) {
                return (1.0 - minimal) * Math.pow(function.score(document),selectivity) + minimal;
            }
        };
    }
}
