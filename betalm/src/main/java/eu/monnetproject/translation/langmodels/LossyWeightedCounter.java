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
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Random;

/**
 * As with lossy counter but counts with a different weight for each element
 *
 * @author John McCrae
 */
public class LossyWeightedCounter implements WeightedCounter {

    private final int N;
    private final NGramCarousel carousel;
    private final WeightedNGramCountSetImpl nGramCountSet;
    private double b;
    private double bStep = 1.0;
    /**
     * Number of tokens read
     */
    protected long p;
    final Random random = new Random();
    private final double critical = Double.parseDouble(System.getProperty("sampling.critical", "0.2"));

    /**
     * Create a lossy counter
     *
     * @param N The largest n-gram to count
     * @param w The bucket width, e.g., 1000
     */
    @SuppressWarnings("unchecked")
    public LossyWeightedCounter(int N) {
        this.N = N;
        this.b = 1;
        this.p = 0;
        this.carousel = new NGramCarousel(N);
        this.nGramCountSet = new WeightedNGramCountSetImpl(N);
    }

    @Override
    public int N() {
        return N;
    }

    @Override
    public void offer(int w, double v) {
        carousel.offer(w);
        for (int i = 1; i <= carousel.maxNGram(); i++) {
            final NGram ngram = carousel.ngram(i);
            final Object2DoubleMap<NGram> ngcs = nGramCountSet.ngramCount(i);
            if (ngcs.containsKey(ngram)) {
                ngcs.put(ngram, ngcs.getDouble(ngram) + v);
            } else {
                ngcs.put(ngram, v);
            }
            nGramCountSet.add(i, v);
        }
        p++;
        bStep *= (double) (p - 1) / (double) p;
        bStep += v / p;
        if (p % 1000 == 0 && memoryCritical()) {
            prune();
        }
    }
    private final Runtime runtime = Runtime.getRuntime();

    private boolean memoryCritical() {
        return (double) (runtime.freeMemory() + runtime.maxMemory() - runtime.totalMemory()) / (double) runtime.maxMemory() < critical;
    }

    protected void prune() {
        do {
            b++;
            final double thresh = bStep * b;
            for (int i = 1; i <= N; i++) {
                final ObjectIterator<Object2DoubleMap.Entry<NGram>> iter = nGramCountSet.ngramCount(i).object2DoubleEntrySet().iterator();
                while (iter.hasNext()) {
                    final Object2DoubleMap.Entry<NGram> entry = iter.next();
                    if (entry.getValue() < thresh) {
                        iter.remove();
                        nGramCountSet.sub(i, entry.getDoubleValue());
                    }
                }
            }
            System.gc();
        } while (memoryCritical());
    }

    @Override
    public void docEnd() {
        carousel.reset();
    }

    @Override
    public WeightedNGramCountSet counts() {
        return nGramCountSet;
    }

    private static class WeightedNGramCountSetImpl implements WeightedNGramCountSet {

        private final int N;
        private final Object2DoubleOpenHashMap<NGram>[] counts;
        private final double[] sums;

        public WeightedNGramCountSetImpl(int N) {
            this.N = N;
            counts = new Object2DoubleOpenHashMap[N];
            for (int i = 0; i < N; i++) {
                counts[i] = new Object2DoubleOpenHashMap<NGram>();
            }
            sums = new double[N];
        }

        @Override
        public Object2DoubleMap<NGram> ngramCount(int n) {
            return counts[n - 1];
        }

        @Override
        public int N() {
            return N;
        }

        @Override
        public double sum(int n) {
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
}
