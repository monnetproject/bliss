/**********************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
package eu.monnetproject.translation.langmodels;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;

/**
 *
 * @author John McCrae
 */
public interface WeightedNGramCountSet {

    /**
     * The length of the longest n-gram
     * @return n
     */
    int N();
    
    /**
     * The counts of n-gram
     * @param n The length of n-grams
     * @return A map giving an integer count for each n-gram
     */
    Object2DoubleMap<NGram> ngramCount(int n);
    
    /**
     * The total value of all n-gram
     * @param n The length of n-grams
     * @return Sum of all n-gram counts
     */
    double sum(int n);
    
    /**
     * Increment the total count of n-grams
     * @param n At length n
     * @param v The amount to increase the count
     */
    void add(int n, double v);
    
    /**
     * Remove a number of counts (due to pruning)
     * @param n At length n
     * @param v counts
     */
    void sub(int n, double v);
}
