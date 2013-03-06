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

import eu.monnetproject.math.sparse.IntList;
import eu.monnetproject.bliss.NGram;
import eu.monnetproject.bliss.NGramCarousel;
import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.WordMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleRBTreeMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;

/**
 *
 * @author jmccrae
 */
public class FilterBySalientList {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File corpusFile = opts.roFile("corpus", "The corpus");
        final File salientFile = opts.roFile("salientList", "The salient list");
        final File wordMapFile = opts.roFile("wordMap", "The wordmap");
        //final File filteredCorpus = opts.woFile("out", "The filtered corpus");
        final PrintStream out = opts.outFileOrStdout();
        if (!opts.verify(FilterBySalientList.class)) {
            return;
        }
        final int W = WordMap.calcW(wordMapFile);
        final String[] wordMap = WordMap.inverseFromFile(wordMapFile, W, false);
        final DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(salientFile));
        final Object2DoubleMap<NGram> ngrams = new Object2DoubleRBTreeMap<NGram>();
        int N = 0;
        while (true) {
            try {
                int n = in.readInt();
                N = Math.max(N, n);
                int[] ng = new int[n];
                for (int i = 0; i < n; i++) {
                    ng[i] = in.readInt();
                }
                ngrams.put(new NGram(ng), in.readDouble());
            } catch (EOFException x) {
                break;
            }
        }
        in.close();
        filterBySalients(corpusFile, ngrams, out, wordMap, N, SourceType.SECOND);
    }

    private static void filterBySalients(File corpusFile, Object2DoubleMap<NGram> ngrams, PrintStream out, String[] wordMap, int N, SourceType sourceType) throws IOException {
        final IntList doc[] = {new IntList(), new IntList()};
        final double[] salience = {0.0, 0.0};
        final DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpusFile));
        //final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(filteredCorpus));
        final DecimalFormat df = new DecimalFormat("0.0000000");
        int docNo = 0;
        final NGramCarousel carousel = new NGramCarousel(N);
        while (true) {
            try {
                int w = in.readInt();
                if (w == 0) {
                    docNo++;
                    if (docNo % 2 == 0) {
                        carousel.reset();
                        out.print(df.format(salience[sourceType == SourceType.FIRST ? 0 : 1] / doc[sourceType == SourceType.FIRST ? 0 : 1].size()) + " ");
                        for (int w2 : doc[sourceType == SourceType.FIRST ? 1 : 0]) {
                            carousel.offer(w2);
                            out.print(wordMap[w2]);
//                            for(int i = 1; i <= carousel.maxNGram(); i++) {
//                                if(ngrams.containsKey(carousel.ngram(i))) {
//                                    out.print("^"+i);
//                                }
//                            }
                            out.print(" ");
                        }
                        out.println();
                        doc[0].clear();
                        doc[1].clear();
                        salience[0] = 0.0;
                        salience[1] = 0.0;
                    }
                    carousel.reset();
                } else {
                    doc[docNo % 2].add(w);
                    carousel.offer(w);
                    for (int i = 1; i <= carousel.maxNGram(); i++) {
                        if (ngrams.containsKey(carousel.ngram(i))) {
                            salience[docNo % 2] += 1.0;//ngrams.get(carousel.ngram(i));
                        }
                    }
                }
            } catch (EOFException x) {
                in.close();
                break;
            }
        }
        out.flush();
        out.close();
    }
}
