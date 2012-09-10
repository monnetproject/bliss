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
 ********************************************************************************
 */
package eu.monnetproject.translation.topics.ontology;

import eu.monnetproject.label.LabelExtractor;
import eu.monnetproject.label.LabelExtractorFactory;
import eu.monnetproject.lang.Language;
import eu.monnetproject.lang.Script;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.tokenizer.Tokenizer;
import eu.monnetproject.tokenizer.TokenizerFactory;
import eu.monnetproject.tokens.Token;
import eu.monnetproject.translation.TranslationRanker;
import eu.monnetproject.translation.TranslationRankerFactory;
import eu.monnetproject.translation.topics.lda.PolylingualGibbsData;
import eu.monnetproject.util.Logger;
import eu.monnetproject.util.Logging;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public abstract class AbstractRankerFactory implements TranslationRankerFactory {

    protected final Logger log = Logging.getLogger(this);
    protected final TokenizerFactory tokenizerFactory;// = Services.getFactory(TokenizerFactory.class);
    protected final LabelExtractorFactory labelExtractorFactory;// = Services.getFactory(LabelExtractorFactory.class);

    public AbstractRankerFactory(TokenizerFactory tokenizerFactory, LabelExtractorFactory labelExtractorFactory) {
        this.tokenizerFactory = tokenizerFactory;
        this.labelExtractorFactory = labelExtractorFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TranslationRanker getRanker(Ontology ontology, Language srcLang, Language trgLang) {
        return getRanker(ontology, srcLang, trgLang, Collections.EMPTY_SET);
    }

    protected int[] onto2doc(PolylingualGibbsData data, Ontology ontology, Language srcLang) {
        return onto2doc(data.words,ontology,srcLang);
    }
    
    protected int[] onto2doc(Map<String,Integer> words, Ontology ontology, Language srcLang) {
        LinkedList<Integer> doc = new LinkedList<Integer>();
        final Tokenizer tokenizer = getTokenizer(srcLang);
        @SuppressWarnings("unchecked")
        final LabelExtractor extractor = labelExtractorFactory.getExtractor(Collections.EMPTY_LIST, true, false);
        if (extractor == null) {
            log.severe("No label extractor");
        }
        final HashSet<URI> puns = new HashSet<URI>();
        for (Entity entity : ontology.getEntities()) {
            if (entity.getURI() == null || puns.contains(entity.getURI())) {
                continue;
            }
            puns.add(entity.getURI());
            final Map<Language, Collection<String>> labels = extractor.getLabels(entity);
            if (labels.containsKey(srcLang)) {
                for (String label : labels.get(srcLang)) {
                    for (Token token : tokenizer.tokenize(label.toLowerCase())) {
                        final int tokenIdx = wordIdx(token.getValue(), words);
                        if (tokenIdx >= 0) {
                            doc.add(tokenIdx);
                        }
                    }
                }
            }
        }
        int[] d = new int[doc.size()];
        for (int i = 0; i < doc.size(); i++) {
            d[i] = doc.get(i);
        }
        return d;
    }
    
    
    protected Tokenizer getTokenizer(Language lang) {
        Script script = Script.LATIN;
        final Script[] knownScriptsForLanguage = Script.getKnownScriptsForLanguage(lang);
        if (knownScriptsForLanguage.length > 0) {
            script = knownScriptsForLanguage[0];
        }
        return tokenizerFactory.getTokenizer(script);
    }

    protected int wordIdx(String word, PolylingualGibbsData data) {
        if (data.words.containsKey(word)) {
            return data.words.get(word);
        } else {
            return -1;
        }
    }
    
    protected int wordIdx(String word, Map<String,Integer> words) {
        if (words.containsKey(word)) {
            return words.get(word);
        } else {
            return -1;
        }
    }
}
