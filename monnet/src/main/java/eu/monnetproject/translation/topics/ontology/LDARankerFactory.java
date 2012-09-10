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

import eu.monnetproject.config.Configurator;
import eu.monnetproject.label.LabelExtractorFactory;
import eu.monnetproject.lang.Language;
import eu.monnetproject.lang.Script;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.tokenizer.TokenizerFactory;
import eu.monnetproject.translation.TranslationRanker;
import eu.monnetproject.translation.topics.lda.Estimator;
import eu.monnetproject.translation.topics.lda.GibbsFormatException;
import eu.monnetproject.translation.topics.lda.PolylingualGibbsData;
import eu.monnetproject.util.ResourceFinder;
import static eu.monnetproject.util.CollectionFunctions.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 *
 * @author John McCrae
 */
public class LDARankerFactory extends AbstractRankerFactory {
    private final Estimator estimator = new Estimator();

    public LDARankerFactory(TokenizerFactory tokenizerFactory, LabelExtractorFactory labelExtractorFactory) {
        super(tokenizerFactory, labelExtractorFactory);
    }
    private static PolylingualGibbsData polylingualGibbsData;

    public PolylingualGibbsData getData(Properties config, Language srcLang, Language trgLang) throws IOException, GibbsFormatException {
        PolylingualGibbsData data;
        // The config may change in unit tests ;)
        if (!config.containsKey("model")) {
            log.warning("No LDA model");
            return null;
        }
        if (polylingualGibbsData == null) {
            URL resource = ResourceFinder.getResource(config.getProperty("model"));
            if (resource == null) {
                log.severe("Could not locate LDA model at " + config.getProperty("model"));
                return null;
            }
            log.info("Reading Polylingual Gibbs Data: " + resource);
            polylingualGibbsData = data = PolylingualGibbsData.read(resource.openStream());
            log.info("Done Reading: " + resource + " ...calculating specific topic vector");
        } else {
            data = polylingualGibbsData;
        }
        if (!exists(data.languages, eq(srcLang))) { 
            log.severe(srcLang + " not supported");
            return null;
        } else if(!exists(data.languages, eq(trgLang))) {
            log.severe(trgLang + " not supported");
            return null;
        }
        return data;
    }

    public TranslationRanker getRanker(List<String> words, Language srcLang, Language trgLang) {
        try {
            final Properties config = Configurator.getConfig("eu.monnetproject.translation.topics.lda");
            final PolylingualGibbsData data = getData(config, srcLang, trgLang);

            if(data == null) {
                System.err.println("data load fail");
                return null;
            }
            
            final int l = lang(data, srcLang);
            if (l < 0) {
                System.err.println("Language not found " + srcLang);
                return null;
            }
            int n = data.W;
            final int[] d = new int[words.size()];
            int i = 0;
            for(String word : words) {
                if(data.words.containsKey(word)) {
                    d[i++] = data.words.get(word);
                } else {
                    d[i++] = n++;
                }
            }
            final double[] p = estimator.topics(d, l, data, 100);
            log.info("Topic vector calculated");
            return new LDAReranker(tokenizerFactory.getTokenizer(Script.LATIN), p, data, trgLang, Double.parseDouble(config.getProperty("oov", "-20.0")));
        } catch (GibbsFormatException x) {
            throw new RuntimeException(x);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }
    
    @Override
    public TranslationRanker getRanker(Ontology ontology, Language srcLang, Language trgLang, Set<Language> extraLanguages) {
        try {
            final Properties config = Configurator.getConfig("eu.monnetproject.translation.topics.lda");
            final PolylingualGibbsData data = getData(config, srcLang, trgLang);

            if(data == null)
                return null;
            
            final double[] p = loadLDA(ontology, srcLang, data, extraLanguages);
            log.info("Topic vector calculated");
            return new LDAReranker(tokenizerFactory.getTokenizer(Script.LATIN), p, data, trgLang, Double.parseDouble(config.getProperty("oov", "-20.0")));
        } catch (GibbsFormatException x) {
            throw new RuntimeException(x);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    public double[] loadLDA(Ontology ontology, Language srcLang, PolylingualGibbsData data, Set<Language> extraLanguages) throws IOException, GibbsFormatException {
        if (extraLanguages.isEmpty()) {
            final int l = lang(data, srcLang);
            if (l < 0) {
                return null;
            }
            final int[] d = onto2doc(data, ontology, srcLang);
            //return estimator.topics(d, l, data, 100);
            return estimator.definiteTopics(new int[][] { d } , new int[] { l }, data, 100);
        } else {
            final int langCount = 1 + extraLanguages.size() - (extraLanguages.contains(srcLang) ? 1 : 0);
            final int[] l = new int[langCount];
            final int[][] d = new int[langCount][];
            l[0] = lang(data, srcLang);
            if (l[0] < 0) {
                return null;
            }
            d[0] = onto2doc(data, ontology, srcLang);
            int i = 1;
            for (Language extraLanguage : extraLanguages) {
                l[i] = lang(data, extraLanguage);
                if (l[i] < 0) {
                    return null;
                }
                d[i] = onto2doc(data, ontology, extraLanguage);
                i++;
            }
            return estimator.definiteTopics(d, l, data, 100);
        }
    }

    private int lang(PolylingualGibbsData data, Language srcLang) {
        for (int l = 0; l < data.languages.length; l++) {
            if (data.languages[l].equals(srcLang)) {
                return l;
            }
        }
        return -1;
    }
}
