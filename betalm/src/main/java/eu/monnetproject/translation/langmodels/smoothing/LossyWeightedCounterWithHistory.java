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
public class LossyWeightedCounterWithHistory extends AbstractWeightedCounterWithHistory {

    public LossyWeightedCounterWithHistory(int N) {
        super(N);
    }

    public LossyWeightedCounterWithHistory(int N, int H) {
        super(N, H);
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
    public WeightedNGramCountSet counts() {
        return nGramCountSet;
    }

    @Override
    public NGramHistories histories() {
        return histories;
    }
}
