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
package eu.monnetproject.bliss;

import eu.monnetproject.math.sparse.SparseIntArray;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Read a parallel binary corpus file. Very much not thread-safe!
 *
 *
 * @author John McCrae
 */
public final class ParallelBinarizedReader {

    private final DataInputStream in;
    private final boolean inverted;

    public ParallelBinarizedReader(InputStream in) {
        this.in = new DataInputStream(in);
        inverted = false;
    }
    
    public ParallelBinarizedReader(InputStream in, boolean inverted) {
        this.in = new DataInputStream(in);
        this.inverted = inverted;
    }
    private int[] buf = new int[1048576];

    public int[][] nextPair() throws IOException {
        if (in.available() == 0) {
            return null;
        }
        boolean inl1 = true;
        int[] l1 = null;
        int loc = 0;
        while (in.available() > 0) {
            try {
                final int i = in.readInt();
                if (i == 0) {
                    if (inl1) {
                        inl1 = false;
                        l1 = Arrays.copyOfRange(buf, 0, loc);
                        loc = 0;
                    } else {
                        return inverted ?
                                new int[][]{
                                    Arrays.copyOfRange(buf, 0, loc),
                                    l1
                                } :
                                new int[][]{
                                    l1,
                                    Arrays.copyOfRange(buf, 0, loc)
                                };
                    }
                } else {
                    buf[loc++] = i;
                }
            } catch (EOFException x) {
                break;
            }
        }
        if (l1 == null) {
            if (loc != 0) {
                System.err.println("Corpus did not balance... odd number of documents?");
            }
            return null;
        } else {
            return inverted ?
                    new int[][]{
                        Arrays.copyOfRange(buf, 0, loc),
                        l1
                    }:
                    new int[][]{
                        l1,
                        Arrays.copyOfRange(buf, 0, loc)
                    };
        }
    }

    public SparseIntArray[] nextFreqPair(int W) throws IOException {
        final int[][] pair = nextPair();
        if (pair == null) {
            return null;
        } else {
            return new SparseIntArray[]{
                        SparseIntArray.histogram(pair[0], W),
                        SparseIntArray.histogram(pair[1], W)
                    };
        }
    }

    public Object2IntMap<NGram>[] nextNGramPair(int N) throws IOException {
        final int[][] pair = nextPair();
        if (pair == null) {
            return null;
        } else {
            final NGramCarousel carousel = new NGramCarousel(N);
            final Object2IntRBTreeMap<NGram>[] ngramPair = new Object2IntRBTreeMap[]{
                new Object2IntRBTreeMap(),
                new Object2IntRBTreeMap()
            };
            for (int l = 0; l < 2; l++) {
                for (int i = 0; i < pair[l].length; i++) {
                    carousel.offer(pair[l][i]);
                    for (int n = 1; n <= carousel.maxNGram(); n++) {
                        final NGram ng = carousel.ngram(n);
                        if (ngramPair[l].containsKey(ng)) {
                            ngramPair[l].put(ng, ngramPair[l].getInt(ng) + 1);
                        } else {
                            ngramPair[l].put(ng, 1);
                        }
                    }
                }
            }
            return ngramPair;
        }
    }

    public SparseIntArray[][] readAll(int W) throws IOException {
        final ArrayList<SparseIntArray[]> sparseArrays = new ArrayList<SparseIntArray[]>();
        SparseIntArray[] sa;
        while ((sa = nextFreqPair(W)) != null) {
            sparseArrays.add(sa);
        }
        return sparseArrays.toArray(new SparseIntArray[sparseArrays.size()][]);
    }

    public Object2IntMap<NGram>[][] readAllNGrams(int N) throws IOException {
        final ArrayList<Object2IntMap<NGram>[]> sparseArrays = new ArrayList<Object2IntMap<NGram>[]>();
        Object2IntMap<NGram>[] sa;
        while ((sa = nextNGramPair(N)) != null) {
            sparseArrays.add(sa);
        }
        return sparseArrays.toArray(new Object2IntMap[sparseArrays.size()][]);
    }
    
    public void close() throws IOException {
        in.close();
    }
}
