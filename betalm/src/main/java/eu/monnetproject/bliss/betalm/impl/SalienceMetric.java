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

import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.bliss.NGram;
import eu.monnetproject.bliss.NGramCarousel;
import eu.monnetproject.bliss.CLIOpts;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleRBTreeMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jmccrae
 */
public class SalienceMetric implements BetaSimFunction {
    final Object2DoubleMap<NGram> ngrams;
    final int N;

    public SalienceMetric(Object2DoubleMap<NGram> ngrams, int N) {
        this.ngrams = ngrams;
        this.N = N;
    }

    
    public static SalienceMetric fromFile(final File salienceFile) throws IOException {
        final DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(salienceFile));
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
        return new SalienceMetric(ngrams,N);
    }

    @Override
    public double scoreNGrams(IntList document, int W) {
        double salience = 0.0;
        final NGramCarousel carousel = new NGramCarousel(N);
        for(int w : document) {
            carousel.offer(w);
            for(int i = 1; i < carousel.maxNGram(); i++) {
                if(ngrams.containsKey(carousel.ngram(i))) {
                    salience += 1.0;
                }
            }
        }
        return salience / document.size();
    }
    
    
    
    @Override
    public double score(Vector<Integer> document) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*double salience = 0.0;
        final NGramCarousel carousel = new NGramCarousel(N);
        for(int w : document.keySet()) {
            carousel.offer(w);
            for(int i = 1; i < carousel.maxNGram(); i++) {
                if(ngrams.containsKey(carousel.ngram(i))) {
                    salience += 1.0;
                }
            }
        }
        return salience / document.size();*/
    }

}
