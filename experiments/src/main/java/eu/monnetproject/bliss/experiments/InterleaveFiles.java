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
package eu.monnetproject.bliss.experiments;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 *
 * @author John McCrae
 */
public class InterleaveFiles {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            fail("Wrong number of arguments");
        }
        final File corpusFile1 = new File(args[0]);
        final File corpusFile2 = new File(args[1]);
        final File outFile = new File(args[2]);

        if (!corpusFile1.exists() || !corpusFile1.canRead()) {
            fail("Cannot access source corpus");
        }

        if (!corpusFile2.exists() || !corpusFile2.canRead()) {
            fail("Cannot access target corpus");
        }

        if (outFile.exists() && !outFile.canWrite()) {
            fail("Cannot write to output");
        }

        final DataOutputStream out;
        if (args[2].endsWith(".gz")) {
            out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(args[2])));
        } else if (args[2].endsWith(".bz2")) {
            out = new DataOutputStream(new BZip2CompressorOutputStream(new FileOutputStream(outFile)));
        } else {
            out = new DataOutputStream(new FileOutputStream(outFile));
        }

        interleave(corpusFile1, corpusFile2, out);

        out.close();
    }

    public static void interleave(File corpusFile1, File corpusFile2, DataOutputStream out) throws IOException {
        final Scanner scanner1 = new Scanner(FilterByILI.fileAsInputStream(corpusFile1));
        final Scanner scanner2 = new Scanner(FilterByILI.fileAsInputStream(corpusFile2));
        int i = 0;
        String s1 = scanner1.nextLine(), s2 = scanner2.nextLine();
        while (s1 != null && s2 != null) {
            final int split1 = s1.lastIndexOf(":");
            final int split2 = s2.lastIndexOf(":");

            if (split1 != -1 && split2 != -1) {
                final String title1 = s1.substring(0, split1);
                final String title2 = s2.substring(0, split2);
                if (title1.equals(title2)) {
                    writeData(out, s1.substring(split1 + 1));
                    writeData(out, s2.substring(split2 + 1));
                    s1 = scanner1.hasNextLine() ? scanner1.nextLine() : null;
                    s2 = scanner2.hasNextLine() ? scanner2.nextLine() : null;
                    if (++i % 10000 == 0) {
                        System.err.print(".");
                    }
                    continue;
                }
            }
            final int sgn = s1.compareTo(s2);
            if (sgn < 0) {
                s1 = scanner1.hasNextLine() ? scanner1.nextLine() : null;
            } else if (sgn > 0) {
                s2 = scanner2.hasNextLine() ? scanner2.nextLine() : null;
            } else {
                s1 = scanner1.hasNextLine() ? scanner1.nextLine() : null;
                s2 = scanner2.hasNextLine() ? scanner2.nextLine() : null;
            }
            if (++i % 10000 == 0) {
                System.err.print(".");
            }
        }
        System.err.println();
    }

    private static void writeData(DataOutputStream out, String dataStr) throws IOException {
        final String[] dataElemStrs = dataStr.split(" ");
        try {
            for (String s : dataElemStrs) {
                if (s.length() == 0) {
                    continue;
                }
                out.writeInt(Integer.parseInt(s));
            }
        } catch (NumberFormatException x) {
            System.err.println("Bad line: " + dataStr);
        }
        out.writeInt(0);
    }

    private static void fail(String message) {
        System.err.println(message);
        System.err.println("Usage:\n\tmvn exec:java -Dexec.mainClass=" + InterleaveFiles.class.getName() + " -Dexec.args=\"corpus.src.sort[.gz|.bz2] corpus.trg.sort[.gz|.bz2] out[.gz|.b2]\"");
        System.exit(-1);
    }
}
