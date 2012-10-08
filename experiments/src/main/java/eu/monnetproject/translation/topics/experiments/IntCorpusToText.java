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

import eu.monnetproject.translation.topics.WordMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntMap.FastEntrySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 *
 * @author John McCrae
 */
public class IntCorpusToText {

    private static void fail(String message) {
        System.err.println(message);
        System.err.println("\nUsage:\n"
                + "\tmvn exec:java -Dexec.mainClass=\"eu.monnetproject.translation.topics.experiments.IntCorpusToText\" -Dexec.args=\"wordMap W corpus[.gz|bz2] [outFile]\"");
        System.exit(-1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3 && args.length != 4) {
            fail("Wrong number of arguments");
        }
        final File wordMapFile = new File(args[0]);
        if (!wordMapFile.exists() || !wordMapFile.canRead()) {
            fail("Cannot access word map");
        }

        final int W;
        try {
            W = Integer.parseInt(args[1]);
        } catch (NumberFormatException x) {
            fail("Not an integer " + args[1]);
            return;
        }


        final File corpusFile = new File(args[2]);
        if (!corpusFile.exists() || !corpusFile.canRead()) {
            fail("Cannot access corpus");
        }

        final PrintStream out;
        if (args.length == 4) {
            final File outFile = new File(args[3]);
            if (outFile.exists() && !outFile.canWrite()) {
                fail("Cannot write to out file");
            }
            out = new PrintStream(outFile);
        } else {
            out = System.out;
        }

        final String[] invMap;
        System.err.println("Reading word map");
        invMap = WordMap.inverseFromFile(wordMapFile, W, true);

        final InputStream corpusIn;
        if (corpusFile.getName().endsWith(".gz")) {
            corpusIn = new GZIPInputStream(new FileInputStream(corpusFile));
        } else if (corpusFile.getName().endsWith(".bz2")) {
            corpusIn = new BZip2CompressorInputStream(new FileInputStream(corpusFile));
        } else {
            corpusIn = new FileInputStream(corpusFile);
        }

        intCorpus2Text(invMap, corpusIn, out);
    }

    private static void intCorpus2Text(String[] invMap, InputStream corpusIn, PrintStream out) throws IOException {
        final DataInputStream data = new DataInputStream(corpusIn);
        while (data.available() > 0) {
            int i = data.readInt();
            if (i != 0) {
                out.print(invMap[i]);
                out.print(" ");
            } else {
                out.println();
            }
        }
        data.close();
    }
}
