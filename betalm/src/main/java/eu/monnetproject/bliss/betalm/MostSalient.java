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
package eu.monnetproject.bliss.betalm;

import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.bliss.NGram;
import eu.monnetproject.bliss.NGramCarousel;
import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.WordMap;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;

/**
 *
 * @author jmccrae
 */
public class MostSalient {

    public static IntSet mostSalient(final File reference, final File corpus, int W, int topN, SourceType sourceType) throws IOException {
        final PrecomputedValues precomp1 = PrecomputedValues.precompute(reference, W, SourceType.SIMPLE);
        final PrecomputedValues precomp2 = PrecomputedValues.precompute(corpus, W, sourceType);
        final double[] salience = new double[W];
        final IntRBTreeSet topNWords = new IntRBTreeSet(new IntComparator() {
            @Override
            public int compare(int i, int i1) {
                return salience[i] < salience[i1] ? -1 : (salience[i] > salience[i1] ? 1 : i - i1);
            }

            @Override
            public int compare(Integer o1, Integer o2) {
                return compare(o1.intValue(), o2.intValue());
            }
        });
        for (int w = 0; w < W; w++) {
            final double val = precomp1.mu.value(w);
            final Double val2 = precomp2.mu.value(w);
            if (val != 0.0 && val2 != 0.0) {
                salience[w] = val / val2;
                if (topNWords.size() < topN) {
                    topNWords.add(w);
                } else if (salience[w] > salience[topNWords.firstInt()]) {
                    topNWords.remove(topNWords.first());
                    topNWords.add(w);
                }
            }
        }
        return topNWords;
    }

    public static boolean inDoc(final int docNo, final SourceType sourceType) {
        if (sourceType == SourceType.SIMPLE) {
            return true;
        } else if (sourceType == SourceType.FIRST && docNo % 2 == 0) {
            return true;
        } else if (sourceType == SourceType.SECOND && docNo % 2 == 1) {
            return true;
        }
        return false;
    }

    public static Object2DoubleMap<NGram> countInReference(final File reference, final int N) throws IOException {
        final Object2DoubleMap<NGram> counts = new Object2DoubleRBTreeMap<NGram>();
        final DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(reference));
        final NGramCarousel carousel = new NGramCarousel(N);
        int docNo = 0;
        while (true) {
            try {
                final int w = in.readInt();
                if (w != 0) {
                    //if (inDoc(docNo, sourceType)) {
                        carousel.offer(w);
                        for (int n = 1; n <= carousel.maxNGram(); n++) {
                            final NGram ngram = carousel.ngram(n);
                            if (counts.containsKey(ngram)) {
                                counts.put(ngram, counts.getDouble(ngram) + 1.0);
                            } else {
                                counts.put(ngram, 1.0);
                            }
                        }
                    //}
                } else {
                    carousel.reset();
                    docNo++;
                }
            } catch (EOFException x) {
                break;
            }
        }
        return counts;
    }

    public static Object2DoubleMap<NGram> countInCorpus(final File reference, final int N, final ObjectSet<NGram> referenceNGrams, SourceType sourceType) throws IOException {
        final Object2DoubleMap<NGram> counts = new Object2DoubleRBTreeMap<NGram>();
        final DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(reference));
        final NGramCarousel carousel = new NGramCarousel(N);
        int docNo = 0;
        while (true) {
            try {
                final int w = in.readInt();
                if (w != 0) {
                    if (inDoc(docNo, sourceType)) {
                        carousel.offer(w);
                        for (int n = 1; n <= carousel.maxNGram(); n++) {
                            final NGram ngram = carousel.ngram(n);
                            if (referenceNGrams.contains(ngram)) {
                                if (counts.containsKey(ngram)) {
                                    counts.put(ngram, counts.getDouble(ngram) + 1.0);
                                } else {
                                    counts.put(ngram, 1.0);
                                }
                            }
                        }
                    }
                } else {
                    carousel.reset();
                    docNo++;
                }
            } catch (EOFException x) {
                break;
            }
        }
        return counts;
    }

    public static Object2DoubleMap<NGram> mostSalientNGrams(final File reference, final File corpus, int N, SourceType sourceType) throws IOException {
        final Object2DoubleMap<NGram> referenceCounts = countInReference(reference, N);
        final Object2DoubleMap<NGram> corpusCounts = countInCorpus(corpus, N, referenceCounts.keySet(), sourceType);
        final Object2DoubleMap<NGram> salience = new Object2DoubleRBTreeMap<NGram>();
        for (Object2DoubleMap.Entry<NGram> e : referenceCounts.object2DoubleEntrySet()) {
            if (corpusCounts.containsKey(e.getKey())) {
                salience.put(e.getKey(), e.getDoubleValue()/corpusCounts.getDouble(e.getKey()));
            }
        }
        referenceCounts.clear();
        corpusCounts.clear();
        final Object2DoubleMap<NGram> rankedSalience = new Object2DoubleRBTreeMap<NGram>(new Comparator<NGram>() {

            @Override
            public int compare(NGram o1, NGram o2) {
                final double salience1 = salience.getDouble(o1);
                final double salience2 = salience.getDouble(o2);
                if(salience1 < salience2) {
                    return +1;
                } else if(salience1 > salience2) {
                    return -1;
                } else {
                    return o1.compareTo(o2);
                }
            }
        });
        rankedSalience.putAll(salience);
        return rankedSalience;
    }

    public static Vector<Integer> filter(final IntSet salients, final Vector<Integer> source) {
        final SparseIntArray filtered = new SparseIntArray(source.length());
        for (int i : salients) {
            filtered.add(i, source.value(i));
        }
        return filtered;
    }
    
    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final SourceType sourceType = opts.enumOptional("t", SourceType.class, SourceType.FIRST, "The corpus type");
        final File refFile = opts.roFile("reference", "The reference ontology");
        final File corpusFile = opts.roFile("corpus", "The corpus file");
        final File wordMapFile = opts.roFile("wordMap", "The word map");
        final int N = opts.intValue("N", "The maximal n-gram to consider");
        final double thresh = opts.doubleValue("threshold", "The threshold of salience to filter at");
        final File outFile = opts.woFile("out", "The file to write the salient n-gram list to");
        if(!opts.verify(MostSalient.class)) {
            return;
        }
        final int W = WordMap.calcW(wordMapFile);
        final String[] wordMap = WordMap.inverseFromFile(wordMapFile, W, true);
        final Object2DoubleMap<NGram> salientNGrams = mostSalientNGrams(refFile, corpusFile, N, sourceType);
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(outFile));
        for(Object2DoubleMap.Entry<NGram> e : salientNGrams.object2DoubleEntrySet()) {
            if(e.getDoubleValue() > thresh) {
                final NGram ng = e.getKey();
                out.writeInt(ng.ngram.length);
                for(int i = 0; i < ng.ngram.length; i++) {
                    out.writeInt(ng.ngram[i]);
                }
                out.writeDouble(e.getDoubleValue());
            }
        }
        out.flush();
        out.close();
    }
}
