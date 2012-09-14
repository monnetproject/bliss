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
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Random;

/**
 * Count using the sticky sampling method. This method is described in:
 * "Approximate Frequency Counts over Data Streams" (2002) G.S. Manku and R.
 * Motwani. I have slightly modified it so that instead of a fixed counter it
 * uses an adaptive counter based on available memory.
 *
 * @author John McCrae
 */
public class StickySamplingCounter implements Counter {

    private final int N;
    private final NGramCarousel carousel;
    private final StdNGramCountSet nGramCountSet;
    // The probability of adding a new n-gram
    private double r;
    // The number of elements in the stream
    protected long p;
    final Random random = new Random();
    private final double critical = Double.parseDouble(System.getProperty("sampling.critical", "0.2"));

    /**
     * Create a Sticky Sampling Counter
     *
     * @param N The largest n-gram to count
     */
    public StickySamplingCounter(int N) {
        this.N = N;
        this.p = 0;
        this.r = 1.0;
        this.carousel = new NGramCarousel(N);
        this.nGramCountSet = new StdNGramCountSet(N);
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
            if (ngcs.containsKey(ngram)) {
                ngcs.put(ngram, ngcs.getInt(ngram) + 1);
            } else if (random.nextDouble() < r) {
                ngcs.put(ngram, 1);
            }
        }
        p++;
        if (p % 1000 == 0 && memoryCritical()) {
            prune();
        }
    }

    private boolean memoryCritical() {
        final Runtime runtime = Runtime.getRuntime();
        return (double) (runtime.freeMemory() + runtime.maxMemory() - runtime.totalMemory()) / (double) runtime.maxMemory() < critical;
    }

    private int sampleFromGeometric(double rate) {
        // See "Non-Uniform Random Variate Generation" by L. Devroye
        return (int) (Math.log(random.nextDouble()) / Math.log(1 - rate));
    }

    protected void prune() {
        do {
            r *= 0.5;
            for (int i = 1; i <= N; i++) {
                final Object2IntMap<NGram> ngcs = nGramCountSet.ngramCount(i);
                final ObjectIterator<Entry<NGram>> iter = ngcs.object2IntEntrySet().iterator();
                while (iter.hasNext()) {
                    final int f = sampleFromGeometric(r);
                    final Entry<NGram> entry = iter.next();
                    if (entry.getIntValue() <= f) {
                        iter.remove();
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
