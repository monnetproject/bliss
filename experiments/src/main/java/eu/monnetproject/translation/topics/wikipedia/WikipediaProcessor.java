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
package eu.monnetproject.translation.topics.wikipedia;

import eu.monnetproject.lang.Language;
import eu.monnetproject.util.Logger;
import eu.monnetproject.util.Logging;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author John McCrae
 */
public class WikipediaProcessor {

    // Script:
    // WikipediaProcessor enwiki-latest-page-articles.xml.bz2 wikipedia en en
    // WikipediaProcessor eswiki-latest-page-articles.xml.bz2 wikipedia es en
    // LC_ALL=C sort wikipedia/en.txt > wikipedia/en.sorted.txt
    // LC_ALL=C sort wikipedia/es.txt > wikipedia/es.sorted.txt
    // AlignWikiFiles wikipedia/en.sorted.txt wikipedia/es.sorted.txt wiki-en-es/en.txt wiki-en-es/es.txt
    // FrequencyCounter wiki-en-es/ 50
    // TextToBinary wiki-en-es/en.txt wiki-en-es/freqs wiki-en-es/en.bin
    // TextToBinary wiki-en-es/es.txt wiki-en-es/freqs wiki-en-es/es.bin
    
    private static final Logger log = Logging.getLogger(WikipediaProcessor.class);

    public static void processDump(URL dumpURL, String corpusPath, Language language, Language primary) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream(dumpURL.openStream())));
        StringBuilder doc = null;
        String s;
        String title = null;
        final File file = new File(corpusPath + File.separator + language + ".txt");
        final PrintWriter out = new PrintWriter(file);
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

                        out.println(title + ":" + cleanWiki(doc.toString()));
                        out.flush();
                        log.info(title);
                        doc = null;
                    }
                } else if (doc != null) {
                    doc.append(s).append(" ");
                }
            } catch (Exception x) {
                log.stackTrace(x);
            }
        }
        out.close();
    }

    private static String cleanWiki(String s) {
        return StringEscapeUtils.unescapeHtml(s.
                replaceAll("\\{\\{[^\\}]*\\}\\}", " "). // remove templates {{template:dosomething}}
                replaceAll("\\[\\[([^\\]:\\|]*)\\]\\]","$1"). // clean links [[link]]
                replaceAll("\\[\\[[^\\]\\|]*\\|([^\\]:\\|]*)\\]\\]","$1"). // clean alt text links [[link|alt]]
                replaceAll("\\[\\[[^\\]]*\\]\\]"," "). // Remove all other links e.g., [[es:enlace]]
                replaceAll("[='\\[\\]\\|]", " "). // remove any remaining special characters
                replaceAll("^[\\*\\:#]+", " ")). // Remove indents // Unescape html codes
                replaceAll("<[^>]*>", " "). // Remove tag-like structures
                replaceAll("\\s+", " "); // Make all whitespace a single space
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("Usage: wikipedia-processor dumpURL outputDir dumpLang primaryLang");
            System.exit(-1);
            args = new String[]{
                "file:///home/jmccrae/Downloads/enwiki-latest-pages-articles.xml.bz2",
                "wikipedia",
                "en",
                "en"
            };
        }
        processDump(new URL(args[0]), args[1], Language.get(args[2]), Language.get(args[3]));
    }
    
    // x.split("\\b") filter { _.matches(".*\\w.*") } map { _.replaceAll("\\s","").toLowerCase }
}
