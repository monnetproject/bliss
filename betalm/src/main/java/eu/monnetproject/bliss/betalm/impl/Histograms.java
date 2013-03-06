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
package eu.monnetproject.bliss.betalm.impl;

import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.bliss.NGram;
import eu.monnetproject.bliss.NGramCarousel;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 *
 * @author jmccrae
 */
public final class Histograms {

    private Histograms() {
    }

    public static Object2IntMap<NGram> ngramHistogram(DataInputStream stream, int W, int N) throws IOException {
        final DataInputStream dis = new DataInputStream(stream);
        final Object2IntMap<NGram> arr = new Object2IntRBTreeMap<NGram>();
        final NGramCarousel carousel = new NGramCarousel(N);
        while (dis.available() > 0) {
            try {
                final int w = dis.readInt();
                if (w != 0) {
                    carousel.offer(w);
                    if (carousel.maxNGram() >= N) {
                        final NGram ngram = carousel.ngram(N);
                        if (!arr.containsKey(ngram)) {
                            arr.put(ngram, 1);
                        } else {
                            arr.put(ngram, arr.get(ngram) + 1);
                        }
                    }
                } else {
                    carousel.reset();
                }
            } catch (EOFException x) {
                break;
            }
        }
        return arr;
    }

    public static Object2IntMap<NGram> ngramHistogram(int[] data, int W, int N) throws IOException {
        final Object2IntMap<NGram> arr = new Object2IntRBTreeMap<NGram>();
        final NGramCarousel carousel = new NGramCarousel(N);
        for (int w : data) {
            if (w != 0) {
                carousel.offer(w);
                if (carousel.maxNGram() >= N) {
                    final NGram ngram = carousel.ngram(N);
                    if (!arr.containsKey(ngram)) {
                        arr.put(ngram, 1);
                    } else {
                        arr.put(ngram, arr.get(ngram) + 1);
                    }
                }
            } else {
                carousel.reset();
            }
        }
        return arr;
    }
}
