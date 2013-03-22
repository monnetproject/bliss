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
package eu.monnetproject.bliss.experiments;

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.ParallelBinarizedReader;
import eu.monnetproject.bliss.WordMap;
import eu.monnetproject.math.sparse.SparseIntArray;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.DataOutputStream;
import java.io.File;

/**
 *
 * @author John McCrae
 */
public class OutputILPProblem {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File wordMap = opts.roFile("wordMap", "The word map");
        final File corpus = opts.roFile("corpus", "The corpus");
        final int J = opts.intValue("J", "The number of documents to handle");
        final DataOutputStream out = new DataOutputStream(opts.outFileOrStdout());
        if(!opts.verify(OutputILPProblem.class)) {
            return;
        }
        final int W = WordMap.calcW(wordMap);
        out.writeShort(W);
        out.writeShort(J);
        final ParallelBinarizedReader slowIn = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));
        
        for(int i = 0; i < J; i++) {
            final SparseIntArray docI = slowIn.nextFreqPair(W)[0];
            final ParallelBinarizedReader fastIn = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));
            for(int j = 0; j < J; j++) {
                final SparseIntArray docJ = fastIn.nextFreqPair(W)[0];
                final IntSet overlap = new IntRBTreeSet();
                overlap.addAll(docI.keySet());
                overlap.retainAll(docJ.keySet());
                retainIfNotEqualFreq(overlap,docI,docJ);
                out.writeShort(overlap.size());
                final IntIterator overlapIter = overlap.iterator();
                while(overlapIter.hasNext()) {
                    int w = overlapIter.nextInt();
                    out.writeShort(w);
                    out.writeDouble(docI.doubleValue(w) * (docJ.doubleValue(w) - docI.doubleValue(w)));
                }
            }
            fastIn.close();
        }
        slowIn.close();
        out.flush();
        out.close();
    }

    private static void retainIfNotEqualFreq(IntSet overlap, SparseIntArray docI, SparseIntArray docJ) {
        final IntIterator iter = overlap.iterator();
        while(iter.hasNext()) {
            final int w = iter.next();
            if(docI.intValue(w) == docJ.intValue(w)) {
                iter.remove();
            }
        }
    }
}
