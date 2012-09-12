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
package eu.monnetproject.translation.topics.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 *
 * @author John McCrae
 */
public class FilterByILI {

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            fail("Wrong number of arguments");
        }

        final File intCorpusFile = new File(args[0]);
        final File iliFile = new File(args[1]);
        final File outFile = new File(args[2]);
        final boolean reverseILI;
        final boolean translate;
        if (args[3].equals("src-trans")) {
            reverseILI = false;
            translate = true;
        } else if (args[3].equals("trg")) {
            reverseILI = true;
            translate = false;
        } else {
            fail("Unrecognized parameter " + args[3]);
            return;
        }

        if (!intCorpusFile.exists() || !intCorpusFile.canRead()) {
            fail("Cannot access corpus file");
        }

        if (!iliFile.exists() || !iliFile.canRead()) {
            fail("Cannot access inter-lingual index");
        }

        if (outFile.exists() && !outFile.canWrite()) {
            fail("Cannot write output");
        }

        final HashMap<String, String> ili = buildILI(iliFile, reverseILI);

        final PrintWriter out;
        if (args[2].endsWith(".gz")) {
            out = new PrintWriter(new GZIPOutputStream(new FileOutputStream(args[2])));
        } else if (args[2].endsWith(".bz2")) {
            out = new PrintWriter(new BZip2CompressorOutputStream(new FileOutputStream(outFile)));
        } else {
            out = new PrintWriter(outFile);
        }

        filter(intCorpusFile, ili, out, translate);

        out.close();
    }

    public static HashMap<String, String> buildILI(File iliFile, boolean reverse) throws IOException {
        final Scanner scanner = new Scanner(fileAsInputStream(iliFile));
        final HashMap<String, String> ili = new HashMap<String, String>();
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            if (line.matches("\\s*")) {
                continue;
            }
            final String[] ss = line.split("\t");
            if (ss.length != 2) {
                throw new RuntimeException("Bad line: " + line);
            }
            if (reverse) {
                ili.put(ss[1], ss[0]);
            } else {
                ili.put(ss[0], ss[1]);
            }
        }
        return ili;
    }

    public static void filter(File corpusFile, HashMap<String, String> ili, PrintWriter out, boolean translate) throws IOException {
        final Scanner scanner = new Scanner(fileAsInputStream(corpusFile));
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final int n = line.lastIndexOf(":");
            if (n < 0) {
                throw new RuntimeException("Bad line: " + line);
            }
            final String title = line.substring(0, n);
            if (ili.containsKey(title)) {
                if (translate) {
                    out.println(ili.get(title) + line.substring(n));
                } else {
                    out.println(line);
                }
            }
        }
    }

    private static void fail(String message) {
        System.err.println(message);
        System.err.println("Usage:\n\tmvn exec:java -Dexec.mainClass=" + FilterByILI.class.getName() + " -Dexec.args=\"corpus.int[.gz|.bz2] ili out[.gz|.bz2] src-trans|trg\"");
        System.exit(-1);
    }

    public static InputStream fileAsInputStream(File corpusFile) throws IOException {
        return corpusFile.getName().endsWith(".gz")
                ? new GZIPInputStream(new FileInputStream(corpusFile)) : (corpusFile.getName().endsWith(".bz2")
                ? new BZip2CompressorInputStream(new FileInputStream(corpusFile)) : new FileInputStream(corpusFile));
    }
}
