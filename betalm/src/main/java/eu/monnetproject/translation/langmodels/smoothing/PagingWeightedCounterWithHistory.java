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
package eu.monnetproject.translation.langmodels.smoothing;

import eu.monnetproject.translation.langmodels.NGram;
import eu.monnetproject.translation.langmodels.WeightedNGramCountSet;
import eu.monnetproject.translation.topics.MMapFileInputStream;
import it.unimi.dsi.fastutil.objects.AbstractObject2DoubleMap;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author John McCrae
 */
public class PagingWeightedCounterWithHistory extends AbstractWeightedCounterWithHistory {

    private final LinkedList<File> pages = new LinkedList<File>();

    public PagingWeightedCounterWithHistory(int N) {
        super(N);
    }

    public PagingWeightedCounterWithHistory(int N, int H) {
        super(N, H);
    }

    // Pages are as follows
    // H (4 bytes)
    // #1-grams (4 bytes)
    //   unigram1 (4*n bytes)
    //   count (4 bytes)
    //   history(4 bytes)
    //   histories ((2 * H + 1)*4
    //   ...
    // 2 (4 bytes)
    // #2-grams (4 bytes)
    //   ...
    @Override
    protected void prune() {
        try {
            System.err.print("P");
            final File tmpFile = File.createTempFile("lmpage", ".lm");
            tmpFile.deleteOnExit();
            final DataOutputStream page = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFile)));
            for (int i = 1; i <= N; i++) {
                System.err.print(i);
                page.writeInt(H);
                final Object2DoubleMap<NGram> ngramCount = nGramCountSet.ngramCount(i);
                final Object2DoubleMap<NGram> historyCount = i == 1 ? null : nGramCountSet.historyCount(i - 1);
                final Object2DoubleMap<NGram> historyCount2 = i == N ? null : nGramCountSet.historyCount(i);
                final Object2ObjectMap<NGram, float[]> historiesCount = histories.histories(i);
                final Object2ObjectMap<NGram, float[]> historyOfHistoriesCount = i == 1 ? null : histories.histories(i - 1);
                page.writeInt(ngramCount.size());
                for (Object2DoubleMap.Entry<NGram> e : ngramCount.object2DoubleEntrySet()) {
                    for (int j = 0; j < i; j++) {
                        page.writeInt(e.getKey().ngram[j]);
                    }
                    page.writeDouble(e.getDoubleValue());
                    if (i > 1) {
                        page.writeDouble(historyCount.getDouble(e.getKey().history()));
                    }
                    if(historyCount2 != null && historyCount2.containsKey(e.getKey())) {
                        page.writeDouble(historyCount2.getDouble(e.getKey()));
                    } else {
                        page.writeDouble(Double.NaN);
                    }
                    final float[] h = historiesCount.get(e.getKey());
                    if (h != null) {
                        for (int j = 0; j < 2 * H + 1; j++) {
                            page.writeFloat(h[j]);
                        }
                    } else {
                        for (int j = 0; j < 2 * H + 1; j++) {
                            page.writeFloat(0.0f);
                        }
                    }
                    if (i > 1) {
                        final float[] h2 = historyOfHistoriesCount.get(e.getKey().history());
                        for (int j = 0; j < 2 * H + 1; j++) {
                            page.writeFloat(h2[j]);
                        }
                    }
                }
                nGramCountSet.ngramCount(i).clear();
                if (historyCount != null) {
                    historyCount.clear();
                    historyOfHistoriesCount.clear();
                }
                System.gc();
            }
            page.flush();
            page.close();
            histories.histories(N).clear();
            pages.add(tmpFile);
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    private PagingNGramReader reader;
    
    @Override
    public NGramHistories histories() {
        //if(pages.size() == 0) {
        //    return histories;
        //}
        if(reader == null) {
            prune();
            final int[][] CoC = histories.countOfCounts();
            final double[] sums = nGramCountSet.sums();
            reader = new PagingNGramReader(pages, N, sums, CoC);
        }
        return reader;
    }

    @Override
    public WeightedNGramCountSet counts() {
//        if(pages.size() == 0) {
//            return nGramCountSet;
//        }
        if(reader == null) {
            prune();
            final int[][] CoC = histories.countOfCounts();
            final double[] sums = nGramCountSet.sums();
            reader = new PagingNGramReader(pages, N, sums, CoC);
        }
        return reader;
    }

    public final static class PagingNGramReader implements NGramHistories, WeightedNGramCountSet {

        private final LinkedList<File> pages;
        private final int N;
        private final double[] sums;
        private final int[][] CoC;

        public PagingNGramReader(LinkedList<File> pages, int N, double[] sums, int[][] CoC) {
            this.pages = pages;
            this.N = N;
            this.sums = sums;
            this.CoC = CoC;
        }

        @Override
        public int N() {
            return N;
        }

        @Override
        public double[] sums() {
            return sums;
        }
        
        

        @Override
        public int[][] countOfCounts() {
            return CoC;
        }

        @Override
        public double sum(NGram history) {

            if (history.ngram.length == 0) {
                return sums[0];
            } else {
                return historyCount(history.ngram.length).getDouble(history);
            }
        }
        private NGramEntrySeeker seeker;

        @Override
        public Object2ObjectMap<NGram, float[]> histories(int n) {
            if(seeker == null) {
                seeker = new NGramEntrySeeker();
            }
            seeker.seekToN(n);
            return new HistoriesCount(seeker);
        }

        @Override
        public Object2DoubleMap<NGram> ngramCount(int n) {
            if (seeker == null) {
                seeker = new NGramEntrySeeker();
            }
            seeker.seekToN(n);
            return new Count(seeker);
        }

        @Override
        public Object2DoubleMap<NGram> historyCount(int n) {
            if (seeker == null) {
                throw new IllegalArgumentException("Please iterate ngramCount!");
            }
            return new HistoryCount(seeker);
        }

        @Override
        public double total(int n) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double mean(int n) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void add(int n, double v) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sub(int n, double v) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private class NGramEntrySeeker {
            // Data Block

            private double[] count, history, history2;
            private float[][] histories, historiesOfHistory;
            // Constant
            private final int H;
            // Current n
            private int n;
            // State
            private final DataInputStream[] readers; // The pages
            private int[] read; // Bytes read
            private int[] blockSize; // The current number of n-grams per page
            private int current; // The minimal of currents
            private NGram[] currents; // The pointer at each page

            public NGramEntrySeeker() {
                this.readers = new DataInputStream[pages.size()];
                try {

                    for (int i = 0; i < pages.size(); i++) {
                        readers[i] = new DataInputStream(new MMapFileInputStream(pages.get(i), 524288));
                    }
                    H = readers[0].readInt();


                    // Initialize data
                    count = new double[pages.size()];
                    history = new double[pages.size()];
                    history2 = new double[pages.size()];
                    histories = new float[pages.size()][2 * H + 1];
                    historiesOfHistory = new float[pages.size()][2 * H + 1];

                    // Verify pages and read block sizes
                    blockSize = new int[pages.size()];
                    blockSize[0] = readers[0].readInt();
                    for (int i = 1; i < pages.size(); i++) {
                        if (H != readers[i].readInt()) {
                            throw new IllegalArgumentException("Corrupt pages!");
                        }
                        blockSize[i] = readers[i].readInt();
                    }
                    read = new int[pages.size()];
                    n = 1;
                    currents = new NGram[pages.size()];
                    current = -1;
                } catch (IOException x) {
                    throw new RuntimeException(x);
                }

            }

            private boolean read(int i) throws IOException {
                try {
                if (read[i] >= blockSize[i]) {
                    currents[i] = null;
                    return false;
                } else {
                    int[] ngram = new int[n];
                    for (int j = 0; j < n; j++) {
                        ngram[j] = readers[i].readInt();
                    }
                    currents[i] = new NGram(ngram);
                    count[i] = readers[i].readDouble();
                    if(count[i] <= 0) {
                        throw new IllegalArgumentException("File corrupted");
                    }
                    if (n > 1) {
                        history[i] = readers[i].readDouble();
                    }
                    history2[i] = readers[i].readDouble();
                    for (int j = 0; j < 2 * H + 1; j++) {
                        histories[i][j] = readers[i].readFloat();
                    }
                    if (n > 1) {
                        for (int j = 0; j < 2 * H + 1; j++) {
                            historiesOfHistory[i][j] = readers[i].readFloat();
                        }
                    }
                    read[i]++;
                    return true;
                }
                } catch(EOFException x) {
                    System.err.println(read[i] + " >= " + blockSize[i]);
                    throw x;
                }
            }

            public void seekToN(int n) {
                if (n == this.n) {
                    return;
                } else if (n < this.n) {
                    throw new IllegalArgumentException("Backwards seek!");
                } else if (!hasNext() && n == this.n + 1) {
                    if (current == -1) {
                        return;
                    }
                    try {
                        for (int i = 0; i < pages.size(); i++) {
                            final int H2 = readers[i].readInt();
                            if (H != H2) {
                                
                                throw new IllegalArgumentException("Corrupt pages! ("+H2+")");
                            }
                            blockSize[i] = readers[i].readInt();
                        }
                        this.n++;
                        read = new int[pages.size()];
                        current = -1;
                    } catch (IOException x) {
                        throw new RuntimeException(x);
                    }
                } else {
                    throw new UnsupportedOperationException("Oops");
                }
            }

            public void seek(NGram ngram) {
                while (currents[current] == null || currents[current].compareTo(ngram) < 0) {
                    advance();
                }
            }

            public NGram current() {
                return currents[current];
            }

            public void advance() {
                try {
                    NGram currentNGram = current == -1 ? null : new NGram(Arrays.copyOf(currents[current].ngram, currents[current].ngram.length));

                    boolean canAdvance = false;
                    for (int i = 0; i < pages.size(); i++) {
                        if (current == -1 || (currents[i] != null && currents[i].compareTo(currentNGram) <= 0)) {
                            canAdvance = read(i) || canAdvance;
                        }
                    }
                    setCurrent();
                } catch (IOException x) {
                    throw new RuntimeException(x);
                }
            }

            public boolean hasNext() {
                for (int i = 0; i < currents.length; i++) {
                    if (read[i] < blockSize[i]) {
                        return true;
                    }
                }
                return false;
            }

            public boolean historical(NGram ngram) {
                return currents[current].history().equals(ngram);
            }

            public double count() {
                double c = 0;
                for (int i = 0; i < pages.size(); i++) {
                    if (currents[i] != null && currents[current].equals(currents[i])) {
                        c += count[i];
                    }
                }
                return c;
            }

            public double history() {
                double h = 0;
                for (int i = 0; i < pages.size(); i++) {
                    if (currents[i] != null && currents[current].equals(currents[i])) {
                        h += history[i];
                    }
                }
                return h;
            }

            public double history2() {
                double h = 0;
                for (int i = 0; i < pages.size(); i++) {
                    if (currents[i] != null && currents[current].equals(currents[i])) {
                        if(!Double.isNaN(history2[i])) {
                            h += history2[i];
                        }
                    }
                }
                return h;
            }


            public float[] histories() {
                float[] h = new float[2 * H + 1];
                for (int i = 0; i < pages.size(); i++) {
                    if (currents[i] != null && currents[current].equals(currents[i])) {
                        for (int j = 0; j < 2 * H + 1; j++) {
                            h[j] += histories[i][j];
                        }
                    }
                }
                return h;
            }

            public float[] historiesOfHistory() {
                float[] h = new float[2 * H + 1];
                for (int i = 0; i < pages.size(); i++) {
                    if (currents[i] != null && currents[current].equals(currents[i])) {
                        for (int j = 0; j < 2 * H + 1; j++) {
                            h[j] += historiesOfHistory[i][j];
                        }
                    }
                }
                return h;
            }

            private void setCurrent() {
                current = 0;
                for (int i = 1; i < pages.size(); i++) {
                    if (currents[current] == null) {
                        current = i;
                    } else if (currents[i] != null && currents[current].compareTo(currents[i]) >= 0) {
                        current = i;
                    }
                }
            }
        }

        private static class Count extends AbstractObject2DoubleMap<NGram> {

            private final NGramEntrySeeker seeker;

            public Count(NGramEntrySeeker seeker) {
                this.seeker = seeker;
            }

            @Override
            public boolean containsKey(Object k) {
                throw new IllegalArgumentException();
            }

            
            
            @Override
            public double getDouble(Object key) {
                if (!(key instanceof NGram)) {
                    throw new IllegalArgumentException();
                } else {
                    seeker.seek((NGram) key);
                    return seeker.count();
                }

            }

            @Override
            public int size() {
                throw new UnsupportedOperationException("Unknown size");
            }

            @Override
            public ObjectSet<Entry<NGram>> object2DoubleEntrySet() {
                return new AbstractObjectSet<Entry<NGram>>() {
                    @Override
                    public ObjectIterator<Entry<NGram>> iterator() {
                        return new ObjectIterator<Entry<NGram>>() {
                            @Override
                            public int skip(int n) {
                                int d = 0;
                                for (int i = 0; i < n; i++) {
                                    seeker.advance();
                                    if (!seeker.hasNext()) {
                                        break;
                                    }
                                    d++;
                                }
                                return d;
                            }

                            @Override
                            public boolean hasNext() {
                                return seeker.hasNext();
                            }

                            @Override
                            public Entry<NGram> next() {
                                seeker.advance();
                                return new Entry<NGram>() {
                                    @Override
                                    public double setValue(double value) {
                                        throw new UnsupportedOperationException("Not mutable.");
                                    }

                                    @Override
                                    public double getDoubleValue() {
                                        return seeker.count();
                                    }

                                    @Override
                                    public NGram getKey() {
                                        return seeker.current();
                                    }

                                    @Override
                                    public Double getValue() {
                                        return seeker.count();
                                    }

                                    @Override
                                    public Double setValue(Double value) {
                                        throw new UnsupportedOperationException("Not mutable.");
                                    }
                                };
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException("Not mutable.");
                            }
                        };
                    }

                    @Override
                    public int size() {
                        throw new UnsupportedOperationException("Unknown size.");
                    }
                };
            }
        }

        private static class HistoryCount extends AbstractObject2DoubleMap<NGram> {

            private final NGramEntrySeeker seeker;

            public HistoryCount(NGramEntrySeeker seeker) {
                this.seeker = seeker;
            }

            @Override
            public double getDouble(Object key) {
                if (key instanceof NGram && seeker.historical((NGram) key)) {
                    return seeker.history();
                } else if(seeker.current().equals(key)) {
                    return seeker.history2();
                } else {
                    throw new IllegalArgumentException("Out of sequence history");
                }
            }

            @Override
            public int size() {
                throw new UnsupportedOperationException("Unknown.");
            }

            @Override
            public ObjectSet<Entry<NGram>> object2DoubleEntrySet() {
                throw new UnsupportedOperationException("Please iterate counts not history!.");
            }
        }

        private static class HistoriesCount extends AbstractObject2ObjectMap<NGram, float[]> {

            private final NGramEntrySeeker seeker;

            public HistoriesCount(NGramEntrySeeker seeker) {
                this.seeker = seeker;
            }

            @Override
            public float[] get(Object key) {
                if (key instanceof NGram) {
                    if (seeker.historical((NGram) key)) {
                        return seeker.historiesOfHistory();
                    } else {
                        return seeker.histories();
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public int size() {
                throw new UnsupportedOperationException("Unknown.");
            }

            @Override
            public ObjectSet<Object2ObjectMap.Entry<NGram, float[]>> object2ObjectEntrySet() {
                throw new UnsupportedOperationException("Please iterate counts not history!.");
            }
        }
    }
}
