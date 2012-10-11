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
package eu.monnetproject.translation.langmodels;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

/**
 *
 * @author John McCrae
 */
public class WeightedNGramCountSetImpl implements WeightedNGramCountSet {
    private final int N;
    private final Object2DoubleOpenHashMap<NGram>[] counts;
    private final Object2DoubleOpenHashMap<NGram>[] historyCounts;
    private final double[] sums;

    @SuppressWarnings(value = "unchecked")
    public WeightedNGramCountSetImpl(int N) {
        this.N = N;
        counts = new Object2DoubleOpenHashMap[N];
        historyCounts = new Object2DoubleOpenHashMap[N - 1];
        for (int i = 0; i < N; i++) {
            counts[i] = new Object2DoubleOpenHashMap<NGram>();
            if (i > 0) {
                historyCounts[i - 1] = new Object2DoubleOpenHashMap<NGram>();
            }
        }
        sums = new double[N];
    }

    @Override
    public Object2DoubleMap<NGram> ngramCount(int n) {
        return counts[n - 1];
    }

    @Override
    public Object2DoubleMap<NGram> historyCount(int n) {
        return historyCounts[n - 1];
    }

    @Override
    public int N() {
        return N;
    }

    @Override
    public double sum(NGram history) {
        if (history.ngram.length == 0) {
            return sums[0];
        } else {
            return historyCounts[history.ngram.length-1].getDouble(history);
        }
    }

    @Override
    public double total(int n) {
        return sums[n - 1];
    }

    @Override
    public void add(int n, double v) {
        sums[n - 1] += v;
    }

    @Override
    public void sub(int n, double v) {
        sums[n - 1] -= v;
    }

}
