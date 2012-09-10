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
import eu.monnetproject.util.Logger;
import eu.monnetproject.util.Logging;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author John McCrae
 */
public class WikipediaMonolingualCorpus implements TextCorpus {

    private final Logger log = Logging.getLogger(this);
    private final URL corpus;
    private final Language language, primary;
    private final int maxDocuments;

    public WikipediaMonolingualCorpus(URL corpus, Language language, Language primary) {
        this.corpus = corpus;
        this.language = language;
        this.primary = primary;
        this.maxDocuments = Integer.MAX_VALUE;
    }

    public WikipediaMonolingualCorpus(URL corpus, Language language, Language primary, int maxDocuments) {
        this.corpus = corpus;
        this.language = language;
        this.primary = primary;
        this.maxDocuments = maxDocuments;
    }

    @Override
    public Iterable<TextDocument> getDocuments() {
        return new Iterable<TextDocument>() {

            @Override
            public Iterator<TextDocument> iterator() {
                try {
                    return new WikipediaCorpusIterator();
                } catch(Exception x) {
                    throw new RuntimeException(x);
                }
            }
        };
    }

    @Override
    public URL getURL() {
        return corpus;
    }

    private class WikipediaCorpusIterator implements Iterator<TextDocument> {

        private final BufferedReader in;
        private TextDocument next;
        private int read = 0;

        public WikipediaCorpusIterator() throws IOException {
            in = new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream(corpus.openStream())));
            tonext();
        }

        private void tonext() throws IOException {
            if(read > maxDocuments) {
                next = null;
                return;
            }
            StringBuilder doc = null;
            String s;
            String title = null;
            while ((s = in.readLine()) != null) {
                try {
                    final Matcher titleMatcher = Pattern.compile(".*<title>(.*)</title>.*").matcher(s);
                    if (titleMatcher.matches()) {
                        title = titleMatcher.group(1);
                    }
                    if (!language.equals(primary)) {
                        final Matcher langMatcher = Pattern.compile(".*\\[\\[" + primary + ":([^\\]]*)\\]\\].*").matcher(s);
                        if (langMatcher.matches()) {
                            log.info(title + " -> " + langMatcher.group(1));
                            title = langMatcher.group(1);
                        }
                    }
                    if (s.contains("<text")) {
                        doc = new StringBuilder();
                    } else if (s.contains("</text>")) {
                        if (doc != null) {
                            final String text = cleanWiki(doc.toString());
                            if(!text.matches("\\s*")) {
                                next = new WikipediaDocument(language, title, text, new URL(corpus + "#" + URLEncoder.encode(title, "UTF-8")));
                                read++;
                                log.info(title);
                                return;
                            }
                        }
                    } else if (doc != null) {
                        doc.append(s).append(System.getProperty("line.separator"));
                    }
                } catch (Exception x) {
                    log.stackTrace(x);
                }
            }
            next = null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public TextDocument next() {
            if(next != null) {
                TextDocument rval = next;
                try {
                    tonext();
                } catch(Exception x) {
                    throw new RuntimeException(x);
                }
                return rval;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        private String cleanWiki(String s) {
            return StringEscapeUtils.unescapeHtml(s.replaceAll("\\{\\{[^\\}]*\\}\\}", "").replaceAll("[='\\[\\]\\|]", "").replaceAll("^[\\*\\:#]+", "").replaceAll("<[^>]*>", ""));
        }
    }
}
