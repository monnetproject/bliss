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
 * *******************************************************************************
 */
package eu.monnetproject.translation.topics.wikipedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author John McCrae
 */
public class FrequencyCounter {

    public static void countFreqs(File textFolder, File outFile, int minFreqFilter) throws IOException {
        final HashMap<String, Integer> words = new HashMap<String, Integer>(524288);
        for (File textFile : textFolder.listFiles()) {
            if (textFile.getPath().endsWith(".txt")) {
                System.err.println("Reading: " + textFile);
                final BufferedReader in = new BufferedReader(new FileReader(textFile));
                String s;
                int i = 0;
                while ((s = in.readLine()) != null) {
                    final String[] tokens = s.split("\\b");
                    for (String token : tokens) {
                        if (token.matches(".*\\w.*")) {
                            final String cleanTk = token.replaceAll("\\s", "").toLowerCase();
                            if (words.containsKey(cleanTk)) {
                                words.put(cleanTk, words.get(cleanTk) + 1);
                            } else {
                                words.put(cleanTk, 1);
                            }
                            checkWords(words, minFreqFilter);
                        }
                    }
                    if (++i % 1000 == 0) {
                        System.err.print(".");
                    }
                }
                System.err.println("");
                in.close();
            }
        }
        final PrintWriter out = new PrintWriter(outFile);
        int n = 1;
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            if (entry.getValue() >= minFreqFilter) {
                out.println(entry.getKey() + " " + (n++) + " " + entry.getValue());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: textFileFolder minFreqFilter");
        }
        final File textFolder = new File(args[0]);
        if (!textFolder.exists() || !textFolder.isDirectory()) {
            throw new IllegalArgumentException(textFolder.getPath() + " does not exist or is not a directoy");
        }
        countFreqs(textFolder, new File(textFolder, "freqs"), Integer.parseInt(args[1]));
    }
    private static int hashSizeThresh = 524288;

    private static void checkWords(HashMap<String, Integer> words, int minFreqFilter) {
        if (words.size() > hashSizeThresh) {
            System.err.print("%");
            int removed = 0;
            final Iterator<Entry<String, Integer>> wordIter = words.entrySet().iterator();
            while (wordIter.hasNext()) {
                if (wordIter.next().getValue() < (minFreqFilter / 10)) {
                    wordIter.remove();
                    removed++;
                }
            }
            System.err.print("-" + removed + "%");
            if (words.size() > hashSizeThresh/2) {
                hashSizeThresh *= 2;
                System.err.print("x"+words.size());
            }
            System.gc();
        }
    }
}
