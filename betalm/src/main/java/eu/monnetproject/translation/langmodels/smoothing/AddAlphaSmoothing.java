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

/**
 *
 * @author John McCrae
 */
public class AddAlphaSmoothing implements NGramScorer {

    private final double alpha; // Constant
    private final double[] loss;
    private final double[] gamma;
    ///////////////////////////////////////////////////////////////////////////
    // Add alpha smoothing derives the probability according to:
    //   p(w_n|...) = (c(w_n...) + alpha) / (C_n + alpha * v1^n)   [1]
    //              = (c(w_n...) + alpha) * loss_n
    //
    //   Sum(p(w_n|....)) = C_n * loss_n + alpha * vn  * loss_n    [2]
    //                    = 1 - beta_n, (unless beta_n > 1)
    //
    //   p(w_i|w_{i-1}...w_{i-n}) = bow * p(w_i|w_{i-1}...w_{i-n+1})   [3]
    //         beta_n / (v1^n-vn) = bow * p(w_i|w_{i-1}...w_{i-n+1})  
    // =>
    //   bow = beta_n / (v1^n-vn) / p(w_i|w_{i-1}...w_{i-n+1})
    //   bow = gamma_n / p(w_i|w_{i-1}...w_{i-n+1})

    public AddAlphaSmoothing(int[] v, double[] C) {
        this.alpha = 15.0 / v[0]; // Heuristically from EuroParl
        this.loss = new double[C.length];
        for (int i = 0; i < C.length; i++) {
            loss[i] = 1.0 / (C[i] + alpha * Math.pow(v[0], i + 1)); // [1]
        }
        double[] beta = new double[C.length];
        for (int i = 0; i < C.length; i++) {
            beta[i] = 1.0 - (C[i] + alpha * v[i]) * loss[i];
            if (beta[i] < 0.0) {
                beta[i] = 0.0;
            }
        }
        this.gamma = new double[C.length];
        for (int i = 1; i < C.length; i++) {
            gamma[i] = beta[i] / (Math.pow(v[i], i + 1) - v[i]);
        }
    }

    @Override
    public double[] ngramScores(NGram nGram, WeightedNGramCountSet countSet) {
        final int n = nGram.ngram.length;
        final double c = countSet.ngramCount(n).getDouble(nGram);
        final double l = countSet.sum(nGram.history());
        return smooth(c, n);
    }
    
    public double[] smooth(double c, int n) {
            final double p = (c + alpha) * loss[n - 1];
        if (n == gamma.length) {
            return new double[]{Math.log10(p)};
        } else {
            return new double[]{Math.log10(p), Math.log10(gamma[n] / p)};
        }
    }
}
