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

import eu.monnetproject.translation.topics.CLIOpts;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 *
 * @author John McCrae
 */
public class CountFrequencies {

    private static void fail(String message) {
        System.err.println(message);
        System.err.println("\nUsage:\n"
                + "\tmvn exec:java -Dexec.mainClass=\"" + CountFrequencies.class.getName() + "\" -Dexec.args=\"corpus W topN out\"");
        System.exit(-1);
    }

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File corpusFile = opts.roFile("corpus[.gz|.bz2]", "The corpus");
        
        final int W = opts.intValue("W", "The number of distinct tokens");

        final int topN = opts.intValue("topN", "The top N to set the top threshold at");
        
        final File outFile = opts.woFile("out", "The file to write frequencies to");

        if(!opts.verify(CountFrequencies.class)) {
            return;
        }
        
        final DataInputStream dataIn;

        if (corpusFile.getName().endsWith(".gz")) {
            dataIn = new DataInputStream(new GZIPInputStream(new FileInputStream(corpusFile)));
        } else if (corpusFile.getName().endsWith(".bz2")) {
            dataIn = new DataInputStream(new BZip2CompressorInputStream(new FileInputStream(corpusFile)));
        } else {
            dataIn = new DataInputStream(new FileInputStream(corpusFile));
        }
        final int[] freqs = new int[W];
        final DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(outFile));

        final int freqThresh = calcFreqs(dataIn, freqs, topN);

        System.out.println("Top " + topN + " words frequency > " + freqThresh);

        printFreqs(freqThresh, freqs, dataOut);
    }

    private static int calcFreqs(DataInputStream dataIn, int[] freqs, int topN) throws IOException {
        int[] topNvalues = new int[topN];
        int[] topNkeys = new int[topN];
        int n = 0;
        try {
            DATA_LOOP:
            while (dataIn.available() > 0) {
                if (++n % 100000 == 0) {
                    System.err.print(".");
                }
                try {
                    int i = dataIn.readInt();
                    if (i != 0) {
                        freqs[i]++;
                        for (int j = 0; j < topN; j++) {
                            if (topNkeys[j] == i) {
                                topNvalues[j]++;
                                continue DATA_LOOP;
                            }
                        }
                        for (int j = 0; j < topN; j++) {
                            if (freqs[i] > topNvalues[j]) {
                                topNvalues[j] = freqs[i];
                                topNkeys[j] = i;
                                continue DATA_LOOP;
                            }
                        }
                    }
                } catch (EOFException x) {
                    break;
                }
            }
        } finally {
            dataIn.close();
        }
        int freqThresh = Integer.MAX_VALUE;
        for (int j = 0; j < topN; j++) {
            freqThresh = Math.min(freqThresh, topNvalues[j]);
        }
        return freqThresh;
    }

    private static void printFreqs(int freqThresh, int[] freqs, DataOutputStream dataOut) throws IOException {
        try {
            dataOut.writeInt(freqThresh);
            for (int i = 1; i < freqs.length; i++) {
                dataOut.writeInt(freqs[i]);
            }
        } finally {
            dataOut.close();
        }
    }
}
