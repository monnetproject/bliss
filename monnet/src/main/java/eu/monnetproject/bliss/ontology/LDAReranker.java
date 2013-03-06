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
package eu.monnetproject.bliss.ontology;

/**
 *
 * @author John McCrae
 */
public class LDAReranker {// implements TranslationRanker {
//
//    private final Estimator estimator = new Estimator();
//    private final Tokenizer tokenizer;
//    private final double[] p;
//    private final PolylingualGibbsData data;
//    private final Language trgLang;
//    private final double OOV;
//
//    public LDAReranker(Tokenizer tokenizer, double[] p, PolylingualGibbsData data, Language trgLang, double OOV) {
//        this.tokenizer = tokenizer;
//        this.p = p;
//        this.data = data;
//        this.trgLang = trgLang;
//        this.OOV = OOV;
//    }
//
//    private int lang(PolylingualGibbsData data, Language srcLang) {
//        for (int l = 0; l < data.languages.length; l++) {
//            if (data.languages[l].equals(srcLang)) {
//                return l;
//            }
//        }
//        return -1;
//    }
//
//    private int wordIdx(String word, PolylingualGibbsData data) {
//        if (data.words.containsKey(word)) {
//            return data.words.get(word);
//        } else {
//            return -1;
//        }
//    }
//    
//    public double getLogPhraseProb(String label, Tokenizer tokenizer, double[] p, Language lang, PolylingualGibbsData data) {
//        double rval = 0.0;
//        final int l = lang(data, lang);
//        if (l < 0) {
//            return 0.0;
//        }
//        int iv = 0;
//        for (Token token : tokenizer.tokenize(label)) {
//            int w = wordIdx(token.getValue(), data);
//            if (w >= 0) {
//                final GibbsData monolingual = data.monolingual(l);
//                final double wp = estimator.wordProb(w, p, monolingual);
//                final double pwp = estimator.priorWordProb(w, monolingual);
//                if(Double.isNaN(wp) || Double.isInfinite(wp) || wp == 0) {
//                    throw new RuntimeException();
//                }
//                if(Double.isNaN(pwp) || Double.isInfinite(pwp) || pwp == 0) {
//                    throw new RuntimeException();
//                }
//                //System.err.println(wp +"/"+pwp);
//                rval += Math.log(wp / pwp);
//                iv++;
//            } else {
//                rval += OOV;
//            }
//        }
//        if (iv != 0) {
//            return rval;
//        } else {
//            return -1000;
//        }
//    }
//
//    @Override
//    public double score(PhraseTableEntry entry, Entity entity) {
//            return getLogPhraseProb(entry.getTranslation().asString(), tokenizer, p, trgLang, data);
//    }
//
//    @Override
//    public String getName() {
//        return "LDA";
//    }
//
//    @Override
//    public void close() {
//    }
}
