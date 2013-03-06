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

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.WordMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 *
 * @author John McCrae
 */
public class CleanCorpus {
    public static final int BUF_SIZE = 1048576;

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);

        final boolean useUNK = opts.flag("unk", "Output low-frequency tokens as <UNK>");
        
        final boolean stopWords = opts.flag("stopWords", "Retain stop words");
        
        final File corpusFile = opts.roFile("corpus[.gz|.b2]", "The integerized corpus");

        //final int W = opts.intValue("W", "Total number of distinct tokens in corpus");
        final File wordMapFile = opts.roFile("wordMap", "The word map");
        
        final File freqFile = opts.roFile("freqs", "The file of token frequencies");

        final int freqMin = opts.intValue("freqMin", "The minimum frequency to accept");

        final int lenMin = opts.intValue("lenMin", "The minimum document length to accept");

        final File outFile = opts.woFile("out[.gz|.bz2]", "The file to write to");

        if (!opts.verify(CleanCorpus.class)) {
            return;
        }
        
        final int W = WordMap.calcW(wordMapFile);
        
        int[] freqArray = readFreqs(freqFile, W);
        
        final int freqMax = stopWords ? Integer.MAX_VALUE : freqArray[0];

        final DataInputStream corpusIn;
        if (corpusFile.getName().endsWith(".gz")) {
            corpusIn = new DataInputStream(new GZIPInputStream(new FileInputStream(corpusFile)));
        } else if (corpusFile.getName().endsWith(".bz2")) {
            corpusIn = new DataInputStream(new BZip2CompressorInputStream(new FileInputStream(corpusFile)));
        } else {
            corpusIn = new DataInputStream(new FileInputStream(corpusFile));
        }
        
        final DataOutputStream dataOut;
        if (outFile.getName().endsWith(".gz")) {
            dataOut = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(outFile)));
        } else if (corpusFile.getName().endsWith(".bz2")) {
            dataOut = new DataOutputStream(new BZip2CompressorOutputStream(new FileOutputStream(outFile)));
        } else {
            dataOut = new DataOutputStream(new FileOutputStream(outFile));
        }
        
        int remainingVocab = 0;
        
        for(int i = 1; i < freqArray.length; i++) {
            if(freqArray[i] >= freqMin && freqArray[i] < freqMax) {
                remainingVocab++;
            }
        }
        
        System.err.println("Remaining: " + remainingVocab);
        
        cleanCorpus(corpusIn, dataOut, freqArray, freqMin, freqMax, lenMin, useUNK ? W+1 : -1);
    }

    /**
     * @param freqFile
     * @param W
     * @return The frequency array, note [0] is the maximum threshold
     */
    public static int[] readFreqs(final File freqFile, final int W) throws FileNotFoundException, IOException {
        final DataInputStream freqIn = new DataInputStream(new FileInputStream(freqFile));
        int[] freqArray = new int[W];
        for (int i = 0; i < W; i++) {
            freqArray[i] = freqIn.readInt();
        }
        freqIn.close();
        return freqArray;
    }

    private static void cleanCorpus(DataInputStream corpusIn, DataOutputStream dataOut, int[] freqArray, int freqMin, int freqMax, int lenMin, int UNK) throws IOException {
        int[] buf = new int[BUF_SIZE];
        int loc = 0;
        int l1Size = 0;
        int l2Size = 0;
        int n = 0;
        boolean l1doc = true;
        while(corpusIn.available() > 0) {
            try {
                final int i = corpusIn.readInt();
                if(i == 0) {
                    if(l1doc) {
                        l1doc = false;
                        buf[loc++] = 0;
                    } else {
                        l1doc = true;
                        if(l1Size >= lenMin && l2Size >= lenMin) {
                            for(int j = 0; j < loc && j < BUF_SIZE; j++) {
                                dataOut.writeInt(buf[j]);
                            }
                            dataOut.writeInt(0);
                        }
                        l1Size = l2Size = loc = 0;
                    }
                } else {
                    if(freqArray[i] >= freqMin && freqArray[i] < freqMax) {
                        if(loc < BUF_SIZE) {
                            buf[loc++] = i;
                        } else if(loc == BUF_SIZE) {
                            System.err.println("Buffer too small!");
                            loc++;
                        }
                        if(l1doc) {
                            l1Size++;
                        } else {
                            l2Size++;
                        }
                    } else if(UNK > 0) {
                        if(loc <BUF_SIZE) {
                            buf[loc++] = UNK;
                        } else if(loc == BUF_SIZE) {
                            System.err.println("Buffer too small!");
                            loc++;
                        }
                        if(l1doc) {
                            l1Size++;
                        } else {
                            l2Size++;
                        }
                    }
                }
                if(++n % 100000 == 0) {
                    System.err.print(".");
                }
            } catch(EOFException x) {
                break;
            }
        }
        System.err.println();
        corpusIn.close();
        dataOut.close();
    }
}
