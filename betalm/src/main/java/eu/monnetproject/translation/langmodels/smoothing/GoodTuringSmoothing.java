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
package eu.monnetproject.translation.langmodels.smoothing;

import eu.monnetproject.translation.langmodels.NGram;
import eu.monnetproject.translation.langmodels.WeightedNGramCountSet;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

/**
 *
 * @author John McCrae
 */
public class GoodTuringSmoothing implements NGramScorer {

    private static final double MIN_PROB = Double.parseDouble(System.getProperty("lm.smooth.minprob", "1e-6"));
    ///////////////////////////////////////////////////////////////////////////
    //   p(w_n...) = (c + 1) / C_n * f(c+1) / f(c)
    // where
    //    f(c) is a linear model on c ~ CoC_n(c)
    //
    //   1 - beta_n = Sum_i((ci+1) / C_n * f(c+1) / f(c))
    //              = 1/C_n * Sum_c[CoC_n(c) * (1 + c) * f(c + 1) / f(c)]
    //
    //   Hence gamma as for AddAlphaSmoothing
    private final double[] gamma;
    private final double[] intercept;
    private final double[] gradient;
    private final double[] C;

    /**
     * Initialize a Good-Turing Smoothing model
     *
     * @param C The number of (n-1)-grams seen
     * @param CoC The count of counts of (n-1)-grams
     * @param v The number of distinct (n-1)-grams seen
     */
    public GoodTuringSmoothing(double[] C, int[][] CoC, int[] v) {
        this.C = C;
        final int n = v.length;
        assert (C.length == n);
        assert (CoC.length == n);
        this.gradient = new double[n];
        this.intercept = new double[n];
        this.gamma = new double[n];
        for (int i = 0; i < n; i++) {
            double[][] x = new double[CoC[i].length + 1][1];
            for (int j = 1; j <= CoC[i].length; j++) {
                x[j][0] = j;
            }
            double[] y = new double[CoC[i].length + 1];
            y[0] = (int) Math.pow(v[0], i + 1) - v[i];
            for (int j = 1; j <= CoC[i].length; j++) {
                y[j] = CoC[i][j - 1];
            }
            final OLSMultipleLinearRegression lr = new OLSMultipleLinearRegression();
            lr.newSampleData(y, x);
            gradient[i] = lr.estimateRegressionParameters()[1];
            intercept[i] = lr.estimateRegressionParameters()[0];

            if (i > 0) {
                double sum = 0.0;
                for (int j = 0; j < CoC[i].length; j++) {
                    sum += CoC[i][j] * (2 + j) * f(j + 2, i) / f(j + 1, i);
                }
                sum /= C[i];

                double beta = Math.max(1 - sum, 0.0);

                gamma[i - 1] = beta / (Math.pow(v[0], i + 1) - v[i]);
            }
        }
    }

    private double f(double c, int n) {
        return c * gradient[n] + intercept[n];
    }

    public double[] smooth(double c, int n) {
        final double p = Math.max((c + 1) * f(c + 1, n - 1) / f(c, n - 1) / C[n - 1], MIN_PROB);
        if (n == gamma.length) {
            return new double[]{p};
        } else {
            return new double[]{p, gamma[n - 1] / p};
        }
    }

    @Override
    public double[] ngramScores(NGram nGram, WeightedNGramCountSet countSet) {
        final int n = nGram.ngram.length;
        final double c = countSet.ngramCount(n).getDouble(nGram);
        final double l = countSet.sum(nGram.history());
        return smooth(c, n);
    }
}
