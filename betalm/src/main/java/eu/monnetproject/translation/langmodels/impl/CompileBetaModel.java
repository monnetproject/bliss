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
package eu.monnetproject.translation.langmodels.impl;

import eu.monnetproject.translation.langmodels.LossyWeightedCounter;
import eu.monnetproject.translation.langmodels.WeightedCounter;
import eu.monnetproject.translation.langmodels.WeightedNGramCountSet;
import eu.monnetproject.translation.langmodels.smoothing.CounterWithHistory;
import eu.monnetproject.translation.langmodels.smoothing.LossyCounterWithHistory;
import eu.monnetproject.translation.langmodels.smoothing.LossyWeightedCounterWithHistory;
import eu.monnetproject.translation.topics.SparseArray;
import eu.monnetproject.translation.topics.sim.BetaSimFunction;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class CompileBetaModel extends CompileLanguageModel {

    public WeightedNGramCountSet doCount(int N, IntegerizedCorpusReader reader, CompileLanguageModel.SourceType type, BetaSimFunction beta, Smoothing smoothing) throws IOException {
        final WeightedCounter counter = smoothing == Smoothing.KNESER_NEY ? new LossyWeightedCounterWithHistory(N) : new LossyWeightedCounter(N);
        long read = 0;
        if (type == CompileLanguageModel.SourceType.SIMPLE) {
            throw new IllegalArgumentException("Cannot BetaLM a monolingual corpus");
        }
        int doc = 0;
        while (reader.nextDocument()) {
            final int[] doc1 = Arrays.copyOfRange(reader.getBuffer(), 0, reader.getBufferSize());
            reader.nextDocument();
            final int[] doc2 = Arrays.copyOfRange(reader.getBuffer(), 0, reader.getBufferSize());
            final int[] docSrc = type == CompileLanguageModel.SourceType.INTERLEAVED_USE_FIRST ? doc2 : doc1;
            final int[] docTrg = type == CompileLanguageModel.SourceType.INTERLEAVED_USE_FIRST ? doc1 : doc2;
            double v = beta.score(SparseArray.histogram(docSrc, 0));
            double alpha = Double.parseDouble(System.getProperty("betalm.alpha","0.0"));
            v = (1.0-alpha) * v + alpha;
            //System.err.println("Doc #"+(++doc)+"="+v);
            if(v <= 0.0) {
                continue;
            }
            for (int tk : docTrg) {
                if (tk == 0) {
                    if (type == CompileLanguageModel.SourceType.SIMPLE) {
                        counter.docEnd();
                    } else {
                        counter.docEnd();
                    }
                } else {
                    counter.offer(tk, v);
                }
                if (++read % 1048576 == 0) {
                    System.err.print(".");
                }
            }
        }
        if (counter instanceof CounterWithHistory) {
            histories = ((CounterWithHistory) counter).histories();
        }
        return counter.counts();
    }
}
