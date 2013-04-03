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

import eu.monnetproject.bliss.NGramSimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetricFactory;
import eu.monnetproject.math.sparse.Integer2DoubleVector;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.Vector;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 *
 * @author John McCrae
 */
public class Word2WordTranslation implements SimilarityMetricFactory<File> {

    @Override
    public SimilarityMetric makeMetric(File data, int W) throws IOException {
        return new W2W(data,W);
    }

    @Override
    public NGramSimilarityMetric makeNGramMetric(File dat, int W, int n) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class<File> datatype() {
        return File.class;
    }

    private static class W2W implements SimilarityMetric {
        private final int W;
        private final Int2IntMap map;

        public W2W(File file, int W) {
            this.W = W;
            map = new Int2IntRBTreeMap();
            try {
                final Scanner in = new Scanner(file);
                while(in.hasNextLine()) {
                    final String line = in.nextLine();
                    if(line.matches("\\s*")) {
                        continue;
                    }
                    final String[] vals = line.split(" ");
                    if(vals.length != 2) {
                        throw new RuntimeException("Bad line: "+ line);
                    }
                    map.put(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]));
                }
                in.close();
            } catch(IOException x) {
                throw new RuntimeException(x);
            }
        }

        @Override
        public Vector<Double> simVecSource(Vector<Integer> termVec) {
            final SparseRealArray v = new SparseRealArray(termVec.length());
            for(Entry<Integer,Integer> e : termVec.entrySet()) {
                if(map.containsKey(e.getKey().intValue())) {
                    v.add(map.get(e.getKey().intValue()),e.getValue().intValue());
                } else {
                    v.add(e.getKey().intValue(),e.getValue().intValue());
                }
            }
            return v;
        }

        @Override
        public Vector<Double> simVecTarget(Vector<Integer> termVec) {
            return new Integer2DoubleVector(termVec);
        }

        @Override
        public int W() {
            return W;
        }

        @Override
        public int K() {
            return W;
        }
    }
}
