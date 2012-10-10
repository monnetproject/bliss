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

import eu.monnetproject.translation.langmodels.Counter;
import eu.monnetproject.translation.langmodels.NGram;
import eu.monnetproject.translation.langmodels.NGramCarousel;
import eu.monnetproject.translation.langmodels.NGramCountSet;
import eu.monnetproject.translation.langmodels.NGramCountSetImpl;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Random;

/**
 *
 * @author John McCrae
 */
public class LossyCounterWithHistory implements Counter {

    private final int N, H;
    private final NGramCarousel carousel;
    private final NGramCountSetImpl nGramCountSet;
    private final Object2ObjectOpenHashMap<NGram, int[]> histories;
    private int b;
    /**
     * Number of tokens read
     */
    protected long p;
    final Random random = new Random();
    private final double critical = Double.parseDouble(System.getProperty("sampling.critical", "0.2"));

    /**
     * Create a lossy counter that also records histories
     *
     * @param N The largest n-gram to count
     * @param H The largest history to store
     */
    @SuppressWarnings("unchecked")
    public LossyCounterWithHistory(int N, int H) {
        this.N = N;
        this.H = H;
        this.b = 1;
        this.p = 0;
        this.carousel = new NGramCarousel(N);
        this.nGramCountSet = new NGramCountSetImpl(N);
        this.histories = new Object2ObjectOpenHashMap<NGram, int[]>();
    }

    /**
     * Create a lossy counter that also records histories
     *
     * @param N The largest n-gram to count
     * @param H The largest history to store
     */
    public LossyCounterWithHistory(int N) {
        this(N, 3);
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
            final NGram history;
            final NGram future;
            if (i > 1) {
                history = ngram.history();
                future = ngram.future();
            } else {
                history = null;
                future = null;
            }
            final Object2IntMap<NGram> ngcs = nGramCountSet.ngramCount(i);
            nGramCountSet.inc(i);
            if (ngcs.containsKey(ngram)) {
                final int count = ngcs.getInt(ngram);
                if (i > 1) {
                    if (count < H) {
                        final int[] h = histories.get(history);
                        h[count - 1]--;
                        h[count]++;
                        final int[] f = histories.get(future);
                        f[H + count - 1]--;
                        f[H + count]++;
                    } else {
                        histories.get(history)[H - 1]++;
                        histories.get(future)[2*H - 1]++;
                    }
                }
                ngcs.put(ngram, count + 1);
            } else {
                if (i > 1) {
                    if (!histories.containsKey(history)) {
                        histories.put(history, new int[2*H]);
                    }
                    if (!histories.containsKey(future)) {
                        histories.put(future, new int[2*H]);
                    }
                    histories.get(history)[0]++;
                    histories.get(future)[H]++;
                }
                ngcs.put(ngram, 1);
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
                    if (entry.getValue() < b) {
                        nGramCountSet.sub(i, entry.getIntValue());
                        iter.remove();
                        histories.remove(entry.getKey());
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
    
    public Object2ObjectMap<NGram,int[]> histories() {
        return histories;
    }
}
