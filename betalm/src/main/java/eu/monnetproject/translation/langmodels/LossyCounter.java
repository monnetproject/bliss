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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

/**
 * Count using the lossy counting method. This method is described in:
 * "Approximate Frequency Counts over Data Streams" (2002) G.S. Manku and R.
 * Motwani
 *
 * @author John McCrae
 */
public class LossyCounter implements Counter {

    private final int N;
    private final NGramCarousel carousel;
    private final NGramCountSetImpl nGramCountSet;
    private int b;
    /**
     * Number of tokens read
     */
    protected long p;
    private final double critical = Double.parseDouble(System.getProperty("sampling.critical", "0.2"));

    /**
     * Create a lossy counter
     *
     * @param N The largest n-gram to count
     * @param w The bucket width, e.g., 1000
     */
    @SuppressWarnings("unchecked")
    public LossyCounter(int N) {
        this.N = N;
        this.b = 1;
        this.p = 0;
        this.carousel = new NGramCarousel(N);
        this.nGramCountSet = new NGramCountSetImpl(N);
    }

    @Override
    public int N() {
        return N;
    }

    @Override
    public void offer(int w) {
        carousel.offer(w);
        for (int i = 1; i <= carousel.maxNGram(); i++) {
            final NGram ngram = carousel.ngram(i);
            final Object2IntMap<NGram> ngcs = nGramCountSet.ngramCount(i);
            nGramCountSet.inc(i);
            if (ngcs.containsKey(ngram)) {
                ngcs.put(ngram, ngcs.getInt(ngram) + 1);
            } else {
                ngcs.put(ngram, 1);
            }

            if (i > 1) {
                final Object2IntMap<NGram> hcs = nGramCountSet.historyCount(i - 1);
                final NGram history = ngram.history();
                if (hcs.containsKey(history)) {
                    hcs.put(history, hcs.getInt(history) + 1);
                } else {
                    hcs.put(history, 1);
                }
            }
        }
        p++;
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
            for (int i = 1; i <= N; i++) {
                final ObjectIterator<Object2IntMap.Entry<NGram>> iter = nGramCountSet.ngramCount(i).object2IntEntrySet().iterator();
                while (iter.hasNext()) {
                    final Object2IntMap.Entry<NGram> entry = iter.next();
                    if (entry.getValue() < (b - N + i)) {
                        final NGram key = entry.getKey();
                        nGramCountSet.sub(i, entry.getIntValue());
                        iter.remove();
                        if (i != N) {
                            nGramCountSet.historyCount(i).remove(key);
                        }
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
    public NGramCountSet counts() {
        return nGramCountSet;
    }
}
