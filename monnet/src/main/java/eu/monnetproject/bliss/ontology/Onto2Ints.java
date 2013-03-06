/**
 * *******************************************************************************
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
package eu.monnetproject.bliss.ontology;

import eu.monnetproject.framework.services.Services;
import eu.monnetproject.label.LabelExtractor;
import eu.monnetproject.label.LabelExtractorFactory;
import eu.monnetproject.lang.Language;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.ontology.OntologySerializer;
import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.PTBTokenizer;
import eu.monnetproject.bliss.Tokenizer;
import eu.monnetproject.bliss.WordMap;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author John McCrae
 */
public class Onto2Ints {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File ontoFile = opts.roFile("ontology", "The ontology file");
        final File wordMapFile = opts.roFile("wordMap", "The word map");
        final Language lang = opts.language("lang", "The language");
        final File outFile = opts.woFile("out[.gz|.bz2]", "The query document");

        final OntologySerializer ontoSerializer = Services.get(OntologySerializer.class);
        final LabelExtractorFactory lef = Services.get(LabelExtractorFactory.class);

        if (!opts.verify(Onto2Ints.class)) {
            return;
        }
        final LabelExtractor extractor = lef.getExtractor(new LinkedList<URI>(), false, false);
        final Tokenizer tokenizer = new PTBTokenizer();
        
        System.err.print("Loading word map");
        final WordMap wordMap = WordMap.fromFile(wordMapFile, true);
        System.err.println();
        
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(outFile));
        
        final Ontology onto = ontoSerializer.read(new FileReader(ontoFile));
        for (Entity entity : onto.getEntities()) {
            final Collection<String> labels = extractor.getLabels(entity).get(lang);
            if(labels == null) {
                continue;
            }
            for(String label : labels) {
                final List<String> tokens = tokenizer.tokenize(label);
                for(String token : tokens) {
                    token = normalize(token);
                    if(wordMap.containsKey(token)) {
                        int i = wordMap.getInt(token);
                        out.writeInt(i);
                    }
                }
            }
        }
        out.writeInt(0);
        out.flush();
        out.close();
    }
    
    /**
     * Must exactly match IntegerizeCorpus.normalize
     * @param token
     * @return 
     */
    public static String normalize(String token) {
        return token.toLowerCase();
    }
}
