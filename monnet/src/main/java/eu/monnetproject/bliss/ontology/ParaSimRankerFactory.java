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

import eu.monnetproject.bliss.betalm.impl.BetaLMImpl;
import eu.monnetproject.config.Configurator;
import eu.monnetproject.label.LabelExtractorFactory;
import eu.monnetproject.lang.Language;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.translation.TokenizerFactory;
import eu.monnetproject.translation.TranslationRanker;
import eu.monnetproject.bliss.WordMap;
import eu.monnetproject.translation.monitor.Messages;
import java.io.File;
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
        final Properties props = Configurator.getConfig("eu.monnetproject.bliss.parasim");
        if(props.containsKey(srcLang+"-"+trgLang)) {
            try {
                if(!props.containsKey(srcLang+"-"+trgLang+"-wordMap")) {
                    throw new IllegalArgumentException("model without wordMap");
                }
                final WordMap wordMap = WordMap.fromFile(new File(props.getProperty(srcLang+"-"+trgLang+"-wordMap")));
                Messages.info("Loading Parallel Similarity data");
                final BetaLMImpl paraSim = new BetaLMImpl(new File(props.get(srcLang+"-"+trgLang).toString()));
                Messages.info("Calculating Parallel Similarity vector");
                return new ParaSimRanker(paraSim, onto2doc(wordMap, ontology, srcLang),wordMap);
            } catch(Exception x) {
                Messages.componentLoadFail(TranslationRanker.class,x);
                return null;
            }
        } else {
            return null;
        }
    }

}
