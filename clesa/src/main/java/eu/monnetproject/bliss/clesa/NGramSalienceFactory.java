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
package eu.monnetproject.bliss.clesa;

import eu.monnetproject.bliss.NGram;
import eu.monnetproject.bliss.NGramCarousel;
import eu.monnetproject.bliss.NGramSimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetricFactory;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 *
 * @author jmccrae
 */
public class NGramSalienceFactory implements SimilarityMetricFactory<InputStream> {

    private Object2IntMap<NGram>[][] readData(final InputStream data, int N) throws IOException {
        final DataInputStream in = new DataInputStream(data);
        final ArrayList<Object2IntMap<NGram>[]> list = new ArrayList<Object2IntMap<NGram>[]>();
        int docNo = 0;
        Object2IntMap<NGram>[] docs = new Object2IntMap[]{
            new Object2IntRBTreeMap<NGram>(),
            new Object2IntRBTreeMap<NGram>()
        };
        final NGramCarousel carousel = new NGramCarousel(N);
        while (true) {
            try {
                final int w = in.readInt();
                if (w == 0) {
                    if (docNo % 2 == 1) {
                        list.add(docs);
                        docs = new Object2IntMap[]{
                            new Object2IntRBTreeMap<NGram>(),
                            new Object2IntRBTreeMap<NGram>()
                        };
                    } 
                    docNo++;
                    carousel.reset();
                } else {
                    carousel.offer(w);
                    for(int n = 1; n <= carousel.maxNGram(); n++) {
                        final NGram ng = carousel.ngram(n);
                        if(docs[docNo % 2].containsKey(ng)) {
                            docs[docNo % 2].put(ng, docs[docNo %2].getInt(ng) + 1);
                        } else {
                            docs[docNo % 2].put(ng, 1);
                        }
                    }
                }
            } catch (EOFException x) {
                break;
            }
        }
        return list.toArray(new Object2IntMap[list.size()][]);
    }

    @Override
    public SimilarityMetric makeMetric(InputStream data, int W) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NGramSimilarityMetric makeNGramMetric(InputStream dat, int W, int N) throws IOException {
        return new NGramSalience(readData(dat,N), W);
    }

    @Override
    public Class<InputStream> datatype() {
        return InputStream.class;
    }
}
