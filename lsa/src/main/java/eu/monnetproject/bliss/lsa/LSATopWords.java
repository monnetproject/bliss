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
package eu.monnetproject.bliss.lsa;

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.WordMap;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.File;

/**
 *
 * @author John McCrae
 */
public class LSATopWords {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File modelFile = opts.roFile("lsa[.gz|.bz2]", "LSA model");
        final File wordMapFile = opts.roFile("wordmap", "The word map");
        final int n = opts.intValue("n", "The top N words to print");

        if (!opts.verify(LSATopWords.class)) {
            return;
        }
        System.err.println("Read model");
        final LSAModel model = LSAModel.load(CLIOpts.openInputAsMaybeZipped(modelFile));
        System.err.print("Load word map");
        final String[] wordMap = WordMap.inverseFromFile(wordMapFile, model.W, true);
        System.err.println();

        for (int i = 0; i < model.K; i++) {
            Int2DoubleMap topN1 = new Int2DoubleRBTreeMap(), topN2 = new Int2DoubleRBTreeMap();
            double min1 = Double.POSITIVE_INFINITY, min2 = Double.POSITIVE_INFINITY;
            int minKey1 = -1, minKey2 = -1;
            for (int j = 0; j < model.W; j++) {
                final double s1 = Math.abs(model.U1[i][j]);
                if (topN1.size() < n) {
                    if (minKey1 == -1 || s1 < minKey1) {
                        minKey1 = j;
                        min1 = model.U1[i][j];
                    }
                    topN1.put(j, s1);
                } else if (s1 > min1) {
                    topN1.remove(minKey1);
                    minKey1 = minKey(topN1);
                    min1 = topN1.get(minKey1);
                    topN1.put(j, s1);
                }
                
                
                final double s2 = Math.abs(model.U2[i][j]);
                if (topN2.size() < n) {
                    if (minKey2 == -1 || s2 < minKey2) {
                        minKey2 = j;
                        min2 = model.U2[i][j];
                    }
                    topN2.put(j, s2);
                } else if (s2 > min2) {
                    topN2.remove(minKey2);
                    minKey2 = minKey(topN2);
                    min2 = topN2.get(minKey2);
                    topN2.put(j, s2);
                }
            }
            System.out.print("T" + i + " L1:");
            for(int w : topN1.keySet()) {
                System.out.print(" " + wordMap[w]);
            }
            System.out.println();
            System.out.print("T" + i + " L2:");
            for(int w : topN2.keySet()) {
                System.out.print(" " + wordMap[w]);
            }
            System.out.println();
        }
    }

    public static int minKey(Int2DoubleMap map) {
        final ObjectIterator<Entry> iter = map.int2DoubleEntrySet().iterator();
        double min = Double.POSITIVE_INFINITY;
        int key = -1;
        while (iter.hasNext()) {
            final Entry e = iter.next();
            if (e.getDoubleValue() < min) {
                min = e.getDoubleValue();
                key = e.getIntKey();
            }
        }
        return key;
    }
}
