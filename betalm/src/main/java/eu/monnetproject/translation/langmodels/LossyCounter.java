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
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Random;

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
    private final Object2ObjectOpenHashMap<NGram,FreqDelta>[] counts;
    private final int w;
    private int b;
    // The number of elements in the stream
    private long p;
    final Random random = new Random();

    /**
     * Create a lossy counter
     *
     * @param N The largest n-gram to count
     * @param w The bucket width, e.g., 1000
     */
    @SuppressWarnings("unchecked")
    public LossyCounter(int N, int w) {
        this.N = N;
        this.w = w;
        this.b = 1;
        this.p = 0;
        this.carousel = new NGramCarousel(N);
        this.counts = new Object2ObjectOpenHashMap[N];
        for(int i = 0; i < N; i++) {
            counts[i] = new Object2ObjectOpenHashMap<NGram, FreqDelta>();
        }
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
            final Object2ObjectMap<NGram,FreqDelta> ngcs = counts[i-1];
            if (ngcs.containsKey(ngram)) {
                ngcs.get(ngram).freq++;
            } else {
                ngcs.put(ngram, new FreqDelta(1, b-1));
            }
        }
        if (++p % w == 0) {
            prune();
        }
    }
    private void prune() {
        b++;
        for (int i = 1; i <= N; i++) {
            final ObjectIterator<Entry<NGram, FreqDelta>> iter = counts[i-1].object2ObjectEntrySet().fastIterator();
            while (iter.hasNext()) {
                final Entry<NGram, FreqDelta> entry = iter.next();
                if (entry.getValue().freq + entry.getValue().delta <= b) {
                    iter.remove();
                } 
            }
        }
    }

    @Override
    public void docEnd() {
        carousel.reset();
    }

    @Override
    public NGramCountSet counts() {
        final StdNGramCountSet nGramCountSet = new StdNGramCountSet(N);
        
        for (int i = 1; i <= N; i++) {
            final ObjectIterator<Entry<NGram, FreqDelta>> iter = counts[i-1].object2ObjectEntrySet().fastIterator();
            final Object2IntMap<NGram> map = nGramCountSet.ngramCount(i);
            while(iter.hasNext()) {
                final Entry<NGram, FreqDelta> entry = iter.next();
                map.put(entry.getKey(), entry.getValue().freq);
                iter.remove();
            }
        }
        
        return nGramCountSet;
    }
    
    private static class FreqDelta {
        public int freq;
        public int delta;

        public FreqDelta(int freq, int delta) {
            this.freq = freq;
            this.delta = delta;
        }
        
        
    }
    
}
