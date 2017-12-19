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

import eu.monnetproject.lang.Language;
import eu.monnetproject.ontology.OntologySerializer; 
import eu.monnetproject.framework.services.Services;
import eu.monnetproject.translation.Feature;
import eu.monnetproject.translation.Label;
import eu.monnetproject.translation.PhraseTableEntry;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author John McCrae
 */
public class LDARerankerTest {
//
//    public LDARerankerTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//    @Test
//    public void testSomeMethod() throws Exception {
////        final OntologySerializer ontoSerializer = Services.get(OntologySerializer.class);
//        /*final Ontology ontology = ontoSerializer.read(new FileReader("../eu.monnetproject.translation.controller/tmp/foaf.xml"));
//        final TranslationRanker ranker = new LDARankerFactory().getRanker(ontology, Language.ENGLISH, Language.SPANISH);
//        System.err.println(ranker.score(new PTE("financiado por"), null));
//        System.err.println(ranker.score(new PTE("financiado , por"),null));*/
//    }
//
//    
//    private class PTE implements PhraseTableEntry {
//        
//        private final String trans;
//
//        public PTE(String trans) {
//            this.trans = trans;
//        }
//
//        @Override
//        public double getApproxScore() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//        
//
//        @Override
//        public Label getTranslation() {
//            return new Label() {
//
//                @Override
//                public String asString() {
//                    return trans;
//                }
//
//                @Override
//                public Language getLanguage() {
//                    throw new UnsupportedOperationException("Not supported yet.");
//                }
//            };
//        }
//
//        @Override
//        public Label getForeign() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public Feature[] getFeatures() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
}
