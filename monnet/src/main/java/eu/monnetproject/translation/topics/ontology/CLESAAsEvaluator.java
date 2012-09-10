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
package eu.monnetproject.translation.topics.ontology;

import eu.monnetproject.translation.Translation;
import eu.monnetproject.translation.eval.TranslationEvaluator;
import eu.monnetproject.translation.topics.SparseArray;
import eu.monnetproject.translation.topics.sim.CLESA;
import java.util.List;

/**
 *
 * @author John McCrae
 */
public class CLESAAsEvaluator implements TranslationEvaluator {

    private final CLESA clesa;
    private final List<List<Translation>> references;

    public CLESAAsEvaluator(CLESA clesa, List<List<Translation>> references) {
        this.clesa = clesa;
        this.references = references;
    }
    

    @Override
    public double score(List<eu.monnetproject.translation.Translation> translations) {
        double score = 0.0;
        double n = 0;
        for(int i = 0; i < translations.size(); i++) {
            final double[] sourceVector = clesa.simVecSource(toSparseArray(references.get(i).get(0).getTargetLabel().asString()));
            final double[] targetVector = clesa.simVecSource(toSparseArray(translations.get(i).getTargetLabel().asString()));
            double aa = 0.0, ab = 0.0, bb = 0.0;
            for(int j = 0; j < sourceVector.length; j++) {
                aa += sourceVector[j] * sourceVector[j];
                ab += sourceVector[j] * targetVector[j];
                bb += targetVector[j] * targetVector[j];
            }
            if(aa > 0 && bb > 0) {
                score += ab / Math.sqrt(aa) / Math.sqrt(bb);
            }
            n++;
        }
        return n > 0 ? score / n : 0;
    }
    
    private SparseArray toSparseArray(String string) {
        final String[] tokens = string.split("\\s+");
        final SparseArray sparseArray = new SparseArray(clesa.W);
        for(String token : tokens) {
            if(clesa.words.containsKey(token.toLowerCase())) {
                sparseArray.add(clesa.words.get(token.toLowerCase()), 1);
            }
        }
        return sparseArray;
    }

    @Override
    public double maxScore() {
        return 1.0;
    }

    @Override
    public String getName() {
        return "CLESA";
    }
}
 