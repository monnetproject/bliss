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
import eu.monnetproject.translation.langmodels.NGramCarousel;
import eu.monnetproject.translation.langmodels.WeightedCounter;
import eu.monnetproject.translation.langmodels.WeightedNGramCountSet;
import eu.monnetproject.translation.langmodels.WeightedNGramCountSetImpl;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class LossyWeightedCounterWithHistory implements WeightedCounter, CounterWithHistory {

    private final int N, H;
    private final NGramCarousel carousel;
    private final WeightedNGramCountSetImpl nGramCountSet;
    private final NGramHistories histories;
    private int[] b;
    private double[] bStep;
    /**
     * Number of tokens read
     */
    protected long[] p;
    protected long allp;
    private final double critical = Double.parseDouble(System.getProperty("sampling.critical", "0.2"));

    /**
     * Create a lossy counter that also records histories
     *
     * @param N The largest n-gram to count
     * @param H The largest history to store
     */
    @SuppressWarnings("unchecked")
    public LossyWeightedCounterWithHistory(int N, int H) {
        this.N = N;
        this.H = H;
        this.b = new int[N];
        Arrays.fill(b, 1);
        this.p = new long[N];
        this.bStep = new double[N];
        Arrays.fill(bStep, 1.0);
        this.carousel = new NGramCarousel(N);
        this.nGramCountSet = new WeightedNGramCountSetImpl(N);
        this.histories = new NGramHistoriesImpl(N, H + 1);
    }

    /**
     * Create a lossy counter that also records histories
     *
     * @param N The largest n-gram to count
     * @param H The largest history to store
     */
    public LossyWeightedCounterWithHistory(int N) {
        this(N, 3);
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
            final NGram history;
            final NGram future;
            final Object2ObjectMap<NGram, double[]> historySet;
            final Object2ObjectMap<NGram, double[]> futureHistorySet;
            if (i > 1) {
                history = ngram.history();
                future = ngram.future();
                historySet = histories.histories(i - 1);
                if (i > 2) {
                    futureHistorySet = histories.histories(i - 2);
                } else {
                    futureHistorySet = null;
                }
            } else {
                history = null;
                future = null;
                historySet = null;
                futureHistorySet = null;
            }
            final Object2DoubleMap<NGram> ngcs = nGramCountSet.ngramCount(i);
            nGramCountSet.add(i, v);
            if (ngcs.containsKey(ngram)) {
                final int count = (int) Math.ceil(ngcs.getDouble(ngram) * bStep[i - 1]);
                if (i > 1) {
                    if (count < H) {
                        final double[] h = historySet.get(history);
                        h[count]--;
                        h[count + 1]++;
                        final double[] f = historySet.get(future);
                        f[H + count]--;
                        f[H + count + 1]++;
                    } else {
                        historySet.get(history)[H]++;
                        historySet.get(future)[2 * H]++;
                    }
                }
                if (count <= H) {
                    histories.countOfCounts()[i-1][count - 1]--;
                    histories.countOfCounts()[i-1][count]++;
                } else if(count == H+1) {
                    histories.countOfCounts()[i-1][count-1]--;
                }
                ngcs.put(ngram, ngcs.getDouble(ngram) + v);
            } else {
                if (i > 1) {
                    if (!historySet.containsKey(history)) {
                        historySet.put(history, new double[2 * H + 1]);
                    }
                    if (!historySet.containsKey(future)) {
                        historySet.put(future, new double[2 * H + 1]);
                    }
                    historySet.get(history)[1]++;
                    historySet.get(future)[H + 1]++;
                    if (i > 2) {
                        final NGram futureHistory = future.history();
                        if (!futureHistorySet.containsKey(futureHistory)) {
                            futureHistorySet.put(futureHistory, new double[2 * H + 1]);
                        }
                        final double[] fh = futureHistorySet.get(futureHistory);
                        fh[0]++;
                    }
                }
                histories.countOfCounts()[i-1][0]++;
                ngcs.put(ngram, v);
            }

            if (i > 1) {
                final Object2DoubleMap<NGram> hcs = nGramCountSet.historyCount(i - 1);
                if (hcs.containsKey(history)) {
                    hcs.put(history, hcs.getDouble(history) + v);
                } else {
                    hcs.put(history, v);
                }
            }
            p[i - 1]++;
            bStep[i - 1] *= (double) (p[i - 1] - 1) / (double) p[i - 1];
            bStep[i - 1] += v / p[i - 1];
            nGramCountSet.setMean(i, bStep[i - 1]);
        }
        allp++;
        if (allp % 1000 == 0 && memoryCritical()) {
            prune();
        }
    }
    private final Runtime runtime = Runtime.getRuntime();

    private boolean memoryCritical() {
        return (double) (runtime.freeMemory() + runtime.maxMemory() - runtime.totalMemory()) / (double) runtime.maxMemory() < critical;
    }

    protected void prune() {
        do {
            System.err.print("P");
            for (int i = 1; i <= N; i++) {
                final double thresh = bStep[i - 1] * (b[i - 1] - N + i);
                final ObjectIterator<Object2DoubleMap.Entry<NGram>> iter = nGramCountSet.ngramCount(i).object2DoubleEntrySet().iterator();
                while (iter.hasNext()) {
                    final Object2DoubleMap.Entry<NGram> entry = iter.next();
                    if (entry.getValue() < thresh) {
                        final NGram key = entry.getKey();
                        nGramCountSet.sub(i, entry.getDoubleValue());
                        iter.remove();
                        histories.histories(key.ngram.length).remove(key);
                        if (i != N) {
                            nGramCountSet.historyCount(i).remove(key);
                        }
                    }
                }
                b[i - 1]++;
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

    @Override
    public NGramHistories histories() {
        return histories;
    }
}
