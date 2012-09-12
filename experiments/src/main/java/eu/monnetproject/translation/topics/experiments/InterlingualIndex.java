/*********************************************************************************
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
package eu.monnetproject.translation.topics.experiments;

import eu.monnetproject.lang.Language;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 *
 * @author John McCrae
 */
public class InterlingualIndex {

    public static final Pattern titlePattern = Pattern.compile(".*<title>(.*)</title>.*");
    
    public static void main(String[] args) throws Exception {
        if(args.length != 3) {
            fail("Wrong number of arguments");
        }
        
        final File wikiFile = new File(args[0]);
        final Language targetLanguage = Language.get(args[1]);
        final File outFile = new File(args[2]);
        
        if(!wikiFile.exists() || !wikiFile.canRead()) {
            fail("Cannot read wiki file");
        }
        
        if(outFile.exists() && !outFile.canWrite()) {
            fail("Cannot write to output file");
        }
        
        final PrintWriter out;
        if(args[2].endsWith(".gz")) {
            out = new PrintWriter(new GZIPOutputStream(new FileOutputStream(args[2])));
        } else if(args[2].endsWith(".bz2"))  {
            out = new PrintWriter(new BZip2CompressorOutputStream(new FileOutputStream(outFile)));
        } else {
            out = new PrintWriter(outFile);
        }
        
        buildILI(wikiFile, targetLanguage, out);
        
        out.close();
    }
    
    public static void buildILI(File wikiFile, Language targetLanguage, PrintWriter out) throws IOException {
        final Pattern langPattern = Pattern.compile(".*\\[\\[" + targetLanguage + ":([^\\]]+)\\]\\].*");
        final Scanner scanner;
        if(wikiFile.getName().endsWith(".bz2")) {
            scanner = new Scanner(new BZip2CompressorInputStream(new FileInputStream(wikiFile))).useDelimiter("\r?\n");
        } else {
            scanner = new Scanner(wikiFile).useDelimiter("\r?\n");
        }
        String title = null;
        while(scanner.hasNext()) {
            final String line = scanner.next();
            final Matcher titleMatcher = titlePattern.matcher(line);
            final Matcher langMatcher = langPattern.matcher(line);
            
            if(titleMatcher.matches()) {
                title = titleMatcher.group(1).replace("\t", " ");
            } else if(langMatcher.matches()) {
                out.println(title + "\t" + langMatcher.group(1).replaceAll("\t", " "));
            }            
        }
    }

    private static void fail(String message) {
        System.err.println(message);
        System.err.println("Usage:\n\tmvn exec:java -Dexec.mainClass=eu.monnetproject.translation.topics.experiments.InterlingualIndex -Dexec.args=\"wikiFile[.bz2] otherLanguage outFile[.gz|.bz2]");
        System.exit(-1);
    }
}
