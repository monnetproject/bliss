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
package eu.monnetproject.translation.langmodels;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Map.Entry;

/**
 * Std NGram count set (implemented by Array of HashMaps)
 *
 * @author John McCrae
 */
public class NGramCountSetImpl implements NGramCountSet {

    private final int N;
    private final Object2IntOpenHashMap<NGram>[] counts;
    private final long[] sums;

    public NGramCountSetImpl(int N) {
        this.N = N;
        counts = new Object2IntOpenHashMap[N];
        for (int i = 0; i < N; i++) {
            counts[i] = new Object2IntOpenHashMap<NGram>();
        }
        sums = new long[N];
    }

    @Override
    public Object2IntMap<NGram> ngramCount(int n) {
        return counts[n - 1];
    }

    @Override
    public int N() {
        return N;
    }

    @Override
    public long sum(NGram history) {
        if (history.ngram.length == 0) {
            return sums[0];
        } else {
            long s = 0;
            final Object2IntOpenHashMap<NGram> futureCounts = counts[history.ngram.length];
            final ObjectIterator<Object2IntMap.Entry<NGram>> futureIter = futureCounts.object2IntEntrySet().iterator();
            while (futureIter.hasNext()) {
                final Object2IntMap.Entry<NGram> e = futureIter.next();
                if (e.getKey().history().equals(history)) {
                    s += e.getIntValue();
                }
            }
            return s;
        }
    }

    @Override
    public void inc(int n) {
        sums[n - 1]++;
    }

    @Override
    public void sub(int n, int v) {
        sums[n - 1] -= v;
    }
    private AsWeighted asWeighted;

    @Override
    public WeightedNGramCountSet asWeightedSet() {
        if (asWeighted == null) {
            return asWeighted = new AsWeighted();
        } else {
            return asWeighted;
        }
    }

    private class AsWeighted implements WeightedNGramCountSet {

        @Override
        public int N() {
            return N;
        }

        @Override
        public Object2DoubleMap<NGram> ngramCount(int n) {
            return new Object2IntAsDoubleMap<NGram>(counts[n - 1]);
        }

        @Override
        public double sum(NGram history) {
            return NGramCountSetImpl.this.sum(history);
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
}
