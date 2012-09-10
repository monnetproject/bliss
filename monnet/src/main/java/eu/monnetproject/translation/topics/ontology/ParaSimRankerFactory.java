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

import eu.monnetproject.config.Configurator;
import eu.monnetproject.label.LabelExtractorFactory;
import eu.monnetproject.lang.Language;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.tokenizer.TokenizerFactory;
import eu.monnetproject.translation.TranslationRanker;
import eu.monnetproject.translation.topics.sim.BetaLM;
import eu.monnetproject.translation.topics.sim.BinaryReader;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author John McCrae
 */
public class ParaSimRankerFactory extends AbstractRankerFactory {

    //private final HashMap<String, ParallelSimilarityOnDisk> psods = new HashMap<String,ParallelSimilarityOnDisk>();
    
    public ParaSimRankerFactory(TokenizerFactory tokenizerFactory, LabelExtractorFactory labelExtractorFactory) {
        super(tokenizerFactory, labelExtractorFactory);
    }

    @Override
    public TranslationRanker getRanker(Ontology ontology, Language srcLang, Language trgLang, Set<Language> extraLanguages) {
        final Properties props = Configurator.getConfig("eu.monnetproject.translation.topics.parasim");
        if(props.containsKey(srcLang+"-"+trgLang)) {
            try {
                log.info("Loading Parallel Similarity data");
                final BetaLM paraSim = new BetaLM(new File(props.get(srcLang+"-"+trgLang).toString()));
                log.info("Calculating Parallel Similarity vector");
                return new ParaSimRanker(paraSim, onto2doc(paraSim.words, ontology, srcLang));
//                return new TranslationRanker() {
//
//                    @Override
//                    public double score(PhraseTableEntry entry, Entity entity) {
//                        return 0;//entry.getTranslation().asString().split("\\s+").length;
//                    }
//
//                    @Override
//                    public String getName() {
//                        return "ParaSim";
//                    }
//                };
            } catch(Exception x) {
                log.stackTrace(x);
                return null;
            }
//        } else if(props.containsKey(srcLang+"-"+trgLang+"-freqs") &&
//                props.containsKey(srcLang+"-"+trgLang+"-"+srcLang) &&
//                props.containsKey(srcLang+"-"+trgLang+"-"+trgLang)) {
//            try {
//                final HashMap<String, Integer> words = BinaryReader.readWords(new File(props.getProperty(srcLang+"-"+trgLang+"-freqs")));
//                final int W = words.size() + 1;
//                log.info("Loading Parallel Similarity data");
//                final ParallelSimilarityOnDisk parallelSimilarityOnDisk;
//                if(psods.containsKey(srcLang+"-"+trgLang)) {
//                    parallelSimilarityOnDisk = psods.get(srcLang+"-"+trgLang);
//                } else {
//                    parallelSimilarityOnDisk = new ParallelSimilarityOnDisk(new File(props.getProperty(srcLang+"-"+trgLang+"-"+srcLang)), new File(props.getProperty(srcLang+"-"+trgLang+"-"+trgLang)), W);
//                    psods.put(srcLang+"-"+trgLang, parallelSimilarityOnDisk);
//                }
//                log.info("Calculating Parallel Similarity vector");
//                final double[] simVec = parallelSimilarityOnDisk.simVec(onto2doc(words, ontology, srcLang));
//                return new ParaSimRanker(words, simVec, parallelSimilarityOnDisk.mu_f);
//                                
//            } catch(Exception x) {
//                log.stackTrace(x);
//                return null;
//            }
//            
        } else {
            return null;
        }
    }

}
