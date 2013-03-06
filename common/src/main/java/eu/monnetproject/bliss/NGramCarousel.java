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
package eu.monnetproject.bliss;

import eu.monnetproject.bliss.NGram;
import java.util.Arrays;

/**
 * This implements a data structure by which all NGrams can be quickly formed.
 * For example consider the following stream {@code [1,2,3,4,5,6] }. The
 * carousel will extract the relevant 3-grams as follows  {@code
 *    offer(1) : carousel = 1,0,0,0,0
 *    offer(2) : carousel = 1,2,0,0,0
 *    offer(3) : carousel = [1,2,3],0,0
 *    offer(4) : carousel = 4,[2,3,4],0
 *    offer(5) : carousel = 4,5,[3,4,5]
 *    offer(6) : carousel = [4,5,6],4,5
 * }
 *
 *
 * @author John McCrae
 */
public class NGramCarousel {

    private final int N;
    private final int[] carousel;
    private int b;

    public NGramCarousel(int N) {
        this.N = N;
        carousel = new int[2 * N - 1];
        b = 0;
    }

    /**
     * Accept a single word from the stream
     * @param n The next word
     */
    public void offer(int n) {
        if (b < N) {
            carousel[b++] = n;
        } else if (b == 2 * N - 1) {
            b = N;
            carousel[b-1] = n;
        } else {
            carousel[b-N] = n;
            carousel[b++] = n;
        }
    }
    
    /**
     * Reset the carousel
     */
    public void reset() {
        b = 0;
    }
    
    /**
     * The highest n-gram that has been read
     */
    public int maxNGram() {
        return Math.min(b, N);
    }
    
    /**
     * Get the most recently read n-gram
     * @param n The length of the n-gram
     * @return The NGram (as a hashable object)
     */
    public NGram ngram(int n) {
        if(n > N || n > b) {
            throw new IllegalArgumentException("NGram not in carousel");
        }
        if(n < 0) {
            throw new IllegalArgumentException("Negative n");
        }
        return new NGram(Arrays.copyOfRange(carousel, b-n, b));
    }
}
