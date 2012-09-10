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
package eu.monnetproject.translation.topics.lda;

import eu.monnetproject.translation.topics.lda.GibbsInput;
import eu.monnetproject.translation.topics.lda.PolylingualGibbsData;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author John McCrae
 */
public class TopicWords {

    public static String[] getWords(int W, GibbsInput input) {
        String[] rval = new String[W];
        for (Map.Entry<String, Integer> entry : input.words.entrySet()) {
            rval[entry.getValue()] = entry.getKey();
        }
        return rval;
    }

    public static void printTopicWords(PrintStream out, PolylingualGibbsData data, String[] words) {
        final TreeSet[] topicWords = new TreeSet[data.K];

        for (int k = 0; k < data.K; k++) {
            topicWords[k] = new TreeSet();
        }
        for (int l = 0; l < data.languages.length; l++) {
            for (int w = 0; w < data.W; w++) {
                int bestK = 0;
                double bestProb = 0.0;
                double sumProb = 0.0;
                int count = 0;
                for (int k = 0; k < data.K; k++) {
                    count += data.N_lkw[l][k].get(w);
                }
                if (count > 5) {
                    for (int k = 0; k < data.K; k++) {
                        if (data.phi(l,w,k) > bestProb) {
                            bestK = k;
                            bestProb = data.phi(l,w,k);
                        }
                        sumProb += data.phi(l,w,k);
                    }
                    topicWords[bestK].add(new WordWithProb(words[w], bestProb/*sumProb*/));
                }
            }
        }
        for (int k = 0; k < data.K; k++) {
            out.println("Topic: " + k);
            int i = 0;
            for (Object word : topicWords[k]) {
                out.println(" " + word);
                i++;
                if (i > 100) {
                    break;
                }
            }
            out.println();
        }
    }
    
    private static class WordWithProb implements Comparable<WordWithProb> {
        final public String word;
        public final double prob;

        public WordWithProb(String word, double prob) {
            this.word = word;
            this.prob = prob;
        }

        @Override
        public int compareTo(WordWithProb t) {
            if(this.prob > t.prob) {
                return -1;
            } else if(this.prob < t.prob) {
                return +1;
            } else {
                return word.compareTo(t.word);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final WordWithProb other = (WordWithProb) obj;
            if ((this.word == null) ? (other.word != null) : !this.word.equals(other.word)) {
                return false;
            }
            if (Double.doubleToLongBits(this.prob) != Double.doubleToLongBits(other.prob)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + (this.word != null ? this.word.hashCode() : 0);
            hash = 37 * hash + (int) (Double.doubleToLongBits(this.prob) ^ (Double.doubleToLongBits(this.prob) >>> 32));
            return hash;
        }

        @Override
        public String toString() {
            return word;
        }
    }
}
