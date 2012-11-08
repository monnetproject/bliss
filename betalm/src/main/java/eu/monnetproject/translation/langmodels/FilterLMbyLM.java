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

import eu.monnetproject.translation.langmodels.impl.ARPALM;
import eu.monnetproject.translation.topics.CLIOpts;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Script used to filter one LM by the n-grams occuring in another... allows
 * more direct comparison of KN to unsmoothed models
 *
 * @author John McCrae
 */
public class FilterLMbyLM {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File lmRefFile = opts.roFile("lmRef", "The (smaller) language model to filter to");
        final File lmFiltFile = opts.roFile("lmFilt", "The (larger) language model to filter");
        final File lmOutFile = opts.woFile("lmOut", "The language model to write");

        if (!opts.verify(FilterLMbyLM.class)) {
            return;
        }
        final ArrayList<Set<String>> filtNgrams = new ArrayList<Set<String>>();
        final int N;
        {
            final ARPALM lmRef = new ARPALM(lmRefFile);
            N = lmRef.n;
            for (int n = 0; n < N; n++) {
                final TreeSet<String> set = new TreeSet<String>(lmRef.ngramIdx(n + 1).keySet());
                filtNgrams.add(set);
            }

            // delete lmRef; (but this is Java)
        }
        final ARPALM lmFilt = new ARPALM(lmFiltFile);

        for (int n = 0; n < N; n++) {
            filtNgrams.get(n).retainAll(lmFilt.ngramIdx(n + 1,filtNgrams.get(n)).keySet());
        }
        
        writeModel(lmFilt, N, filtNgrams, new PrintWriter(lmOutFile));
    }

    private static  void writeModel(ARPALM lmFilt, int N, List<Set<String>> filtNgrams, PrintWriter out) {
        out.println("\\data\\");
        for (int i = 1; i <= N; i++) {
            out.println("ngram " + i + "=" + filtNgrams.get(i - 1).size());
        }
        out.println();
        for (int i = 1; i <= N; i++) {
            out.println("\\" + i + "-grams:");
            int n = 0;
            final Object2IntMap<String> ngramIdx = lmFilt.ngramIdx(i,filtNgrams.get(i-1));
            for (String ngram : filtNgrams.get(i - 1)) {
                int idx = ngramIdx.getInt(ngram);
                final double[] scores = i < N && lmFilt.alpha[i-1][idx] != 0 ? 
                        new double[] { lmFilt.prob[i-1][idx], lmFilt.alpha[i-1][idx] } :
                        new double[] { lmFilt.prob[i-1][idx] };
                out.print(scores[0] + "\t");
                out.print(ngram);
                if (scores.length > 1) {
                    out.print("\t");
                    out.print(scores[1]);
                }
                out.println();
                if (++n % 10000 == 0) {
                    System.err.print(".");
                }

            }
            System.err.println();
            out.println();
        }
        out.println("\\end\\");
        out.flush();
        out.close();
    }
}