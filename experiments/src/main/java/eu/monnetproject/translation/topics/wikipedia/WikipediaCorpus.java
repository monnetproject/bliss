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
package eu.monnetproject.translation.topics.wikipedia;

import eu.monnetproject.corpus.TextCorpus;
import eu.monnetproject.doc.TextDocument;
import eu.monnetproject.lang.Language;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author John McCrae
 */
public class WikipediaCorpus implements TextCorpus {

    private final WikipediaMonolingualCorpus[] corpora;

    public WikipediaCorpus(URL[] urls, Language[] languages, Language primary, int maxDocuments) {
        if (urls.length != languages.length) {
            throw new IllegalArgumentException("wikipedia urls and languages do not have the same length");
        }
        corpora = new WikipediaMonolingualCorpus[urls.length];
        for (int i = 0; i < urls.length; i++) {
            corpora[i] = new WikipediaMonolingualCorpus(urls[i], languages[i], primary, maxDocuments);
        }
    }

    @Override
    public Iterable<TextDocument> getDocuments() {
        return new Iterable<TextDocument>() {

            @Override
            public Iterator<TextDocument> iterator() {
                return new WikipediaCorpusIterator();
            }
        };
    }

    @Override
    public URL getURL() {
        return corpora[1].getURL();
    }

    private class WikipediaCorpusIterator implements Iterator<TextDocument> {

        private int idx = 0;
        private Iterator<TextDocument> current = corpora[0].getDocuments().iterator();

        private void tonext() {
            while (current != null && !current.hasNext()) {
                idx++;
                if (idx >= corpora.length) {
                    current = null;
                } else {
                    current = corpora[idx].getDocuments().iterator();
                }
            }
        }

        @Override
        public boolean hasNext() {
            tonext();
            return current != null && current.hasNext();
        }

        @Override
        public TextDocument next() {
            tonext();
            if (current == null) {
                throw new NoSuchElementException();
            } else {
                return current.next();
            }
        }

        @Override
        public void remove() {
            if (current == null) {
                throw new NoSuchElementException();
            } else {
                current.remove();
            }
        }
    }
}
