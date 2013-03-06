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

import static eu.monnetproject.bliss.betalm.SourceType.*;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.bliss.NGram;
import eu.monnetproject.bliss.NGramCarousel;
import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.betalm.impl.BetaSimFunction;
import eu.monnetproject.bliss.betalm.impl.StopWordList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class DoCount {

    public static String[] wordMap;
    public static PrintWriter docRanking, foreignDocRanking;
    public static IntSet queryFile;
    public static StopWordList stopWordList;
  
    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final SourceType sourceType = opts.enumOptional("t", SourceType.class, SourceType.FIRST, "The type of source: SIMPLE, FIRST or SECOND");
        final File corpus = opts.roFile("corpus[.gz|.bz2]", "The corpus");
        final int N = opts.nonNegIntValue("N", "The largest n-gram to count for");
        final File out = opts.woFile("out", "The files to write to");
        if (!opts.verify(DoCount.class)) {
            return;
        }
        final PrintWriter[] outs = new PrintWriter[N * 2];
        for (int i = 0; i < N; i++) {
            outs[i] = new PrintWriter(out.getName() + "." + i);
            if (i != 0) {
                outs[i + N] = new PrintWriter(out.getName() + ".h" + i);
            }
        }
//        doCount(corpus, N, outs, new BetaSimFunction() {
//            @Override
//            public double score(Vector<Integer> document) {
//                return 1.0;
//            }
//        }, sourceType, 0);

    }
    private static final DecimalFormat df = new DecimalFormat("0.000000000");

    private static void processDoc(IntArrayList doc, IntArrayList foreignDoc, int N, PrintWriter[] outs, BetaSimFunction beta, int W) throws IOException {
        final NGramCarousel carousel = new NGramCarousel(N);
        //final Object2IntMap<NGram> docArr = Histograms.ngramHistogram(foreignDoc.toIntArray(), W, 3);
        //final SparseIntArray docArr = SparseIntArray.histogram(foreignDoc.toIntArray(), W);
        double betaScore = beta.scoreNGrams(foreignDoc,W);
        if(betaScore == 0.0)
            return;
        if (wordMap != null && docRanking != null) {
            docRanking.print(df.format(betaScore) + " ");
            foreignDocRanking.print(df.format(betaScore) + " ");
            for (int w : foreignDoc) {
                if (w < wordMap.length) {
                    if (queryFile.contains(w) && !stopWordList.contains(w)) {
                        docRanking.print("*"+wordMap[w] + "* ");
                    } else {
                        docRanking.print(wordMap[w] + " ");
                    }
                } else {
                    docRanking.print("<UNK> ");
                }
            }
            for (int w : doc) {
                if (w < wordMap.length) {
                    if (queryFile.contains(w) && !stopWordList.contains(w)) {
                        foreignDocRanking.print("*"+wordMap[w] + "* ");
                    } else {
                        foreignDocRanking.print(wordMap[w] + " ");
                    }
                } else {
                    foreignDocRanking.print("<UNK> ");
                }
            }
            foreignDocRanking.println();
            docRanking.println();
        }
        for (int i = 0; i < doc.size(); i++) {
            int w = doc.getInt(i);
            carousel.offer(w);
            for (int n = 0; n < carousel.maxNGram(); n++) {
                final NGram ngram = carousel.ngram(n + 1);
                outs[n].println(Arrays.toString(ngram.ngram) + " " + betaScore);
                if (n > 0) {
                    outs[n + N].print(Arrays.toString(ngram.future().ngram) + " ");
                    outs[n + N].println(ngram.ngram[0] + " " + betaScore);
                }
            }
        }
    }

    public static void doCount(final File corpus, final int N, final PrintWriter[] outs, BetaSimFunction beta, SourceType sourceType, int W) throws IOException {
        int read = 0, docNo = 0;
        final DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpus));
        final IntArrayList[] doc = {new IntArrayList(), new IntArrayList()};
        while (true) {
            if (++read % 1000000 == 0) {
                System.err.print(".");
            }
            try {
                final int i = in.readInt();
                if (i != 0) {
                    doc[docNo % 2].add(i);
                } else {
                    if (docNo % 2 == 1 && sourceType != SIMPLE) {
                        if (sourceType == FIRST) {
                            processDoc(doc[0], doc[1], N, outs, beta, W);
                        } else /*if(sourceType == INTERLEAVED_USE_SECOND)*/ {
                            processDoc(doc[1], doc[0], N, outs, beta, W);
                        }
                        doc[0].clear();
                        doc[1].clear();
                    } else if (sourceType == SIMPLE) {
                        processDoc(doc[docNo % 2], doc[docNo % 2], N, outs, beta, W);
                        doc[docNo % 2].clear();
                    }
                    docNo++;
                }
            } catch (EOFException x) {
                //processDoc(doc, N,outs,beta,W);
                break;
            }
        }
        System.err.println();
        in.close();
        for (PrintWriter o : outs) {
            if (o != null) {
                o.flush();
                o.close();
            }
        }
    }
}
