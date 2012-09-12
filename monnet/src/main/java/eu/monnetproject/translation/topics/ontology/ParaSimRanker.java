/**
 * ********************************************************************************
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
package eu.monnetproject.translation.topics.ontology;

import eu.monnetproject.ontology.Entity;
import eu.monnetproject.translation.PhraseTableEntry;
import eu.monnetproject.translation.TranslationRanker;
import eu.monnetproject.translation.topics.sim.BetaLMImpl;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class ParaSimRanker implements TranslationRanker {

    private final double[] simVec;
    private final double[] mu;
    private final double defaultLoss = 1.0e-3;
    private final double defaultFreq = 1.0e-6;
    private final double simVecSum;
    private final Map<String, Integer> words;

    public ParaSimRanker(Map<String,Integer> words, double[] simVec, double[] mu) {
        this.words = words;
        this.simVec = simVec;
        this.mu = mu;
        double svs = 0.0;
        for(double d : simVec) {
            svs += d;
        }
        this.simVecSum = svs;
        final HashMap<Integer, String> invWords = new HashMap<Integer, String>();
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            invWords.put(entry.getValue(), entry.getKey());
        }
//        System.err.println("Equipment: " + score(new String[] { "equipment" }));
//        System.err.println("Team: " + score(new String[] { "team" }));
//        System.err.println("Assets: " + score(new String[] { "assets" }));
//        System.err.println("Equity: " + score(new String[] { "equity" }));
//        System.err.println("Heritage: " + score(new String[] { "heritage" }));
//        System.err.println("The: " + score(new String[] { "the" }));
//        System.err.println("But: " + score(new String[] { "but" }));
//        System.err.println("Again: " + score(new String[] { "again" }));
//        System.err.println("Then: " + score(new String[] { "then" }));
//        System.err.println("Have: " + score(new String[] { "have" }));
//        try {
//            final PrintWriter out = new PrintWriter("simvec");
//            for (int i = 0; i < simVec.length; i++) {
//                final String w = invWords.get(i);
//                out.println((w != null ? w.replaceAll(",","COMMA") : ("UNK" + new java.util.Random().nextInt())) + "," + simVec[i]);
//            }
//        } catch (Exception x) {
//            x.printStackTrace();
//        }
    }
    
    public ParaSimRanker(BetaLMImpl paraSim, int[] ontologyTerms) {
        this(paraSim.words,paraSim.simVec(ontologyTerms),paraSim.mu_f);
    }

    @Override
    public double score(PhraseTableEntry entry, Entity entity) {
        final String[] entryWords = entry.getTranslation().asString().split("\\s+");
        return score(entryWords);
    }
    
    private double score(String[] entryWords) {
        double score = 1.0;
        for (String word : entryWords) {
            if (words.containsKey(word)) {
                if (words.get(word) < simVec.length && simVec[words.get(word)] > 0) {
                  //  System.err.println(simVec[words.get(word)] + "/" + mu[words.get(word)]);
                    if(simVec[words.get(word)] != 0) {
                        if(mu[words.get(word)] != 0) {
//                            System.err.println(simVec[words.get(word)] +"/"+ mu[words.get(word)] +"/"+ simVecSum);
                            score *= simVec[words.get(word)] / mu[words.get(word)] / simVecSum;
                        } else {
//                            System.err.println( simVec[words.get(word)] +"/(default)"+ defaultFreq +"/"+ simVecSum);
                            score *= simVec[words.get(word)] / defaultFreq / simVecSum;
                        }
                    } else {
                        if(mu[words.get(word)] != 0) {
//                            System.err.println("M2:" + defaultFreq +"/"+ mu[words.get(word)] +"/"+ simVecSum);
                            score *= defaultFreq / mu[words.get(word)] / simVecSum;
                        } else {
//                            System.err.println("ZERO");
                            //score *= defaultFreq / defaultFreq;
                        }
                    }
                }
            } else {
//                            System.err.println("DEF_LOSS");
                score *= defaultLoss;
            }
        }
       // System.err.println(entry.getForeign().asString() + " ||| " + entry.getTranslation().asString() + " ||| " + (score != 0 ? Math.log(score) / entryWords.length : defaultLoss));
        return score != 0 ? Math.log(score) / entryWords.length : defaultLoss;
        //return 0;
    }

    @Override
    public String getName() {
        return "ParaSim";
    }

    @Override
    public void close() {
    }
}
