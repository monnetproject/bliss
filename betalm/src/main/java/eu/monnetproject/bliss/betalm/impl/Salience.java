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
package eu.monnetproject.bliss.betalm.impl;

import eu.monnetproject.bliss.NGram;
import eu.monnetproject.bliss.NGramSimilarityMetric;
import eu.monnetproject.math.sparse.Integer2DoubleVector;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.bliss.SimilarityMetric;
import eu.monnetproject.bliss.WordMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jmccrae
 */
public class Salience implements SimilarityMetric {

    final int W, J;
    final double alpha;
    final int[][] ctTotal; // lw
    final SparseIntArray[][] x; // jlw
    static String[] wordMap;

    static {
        try {
            wordMap = WordMap.inverseFromFile(new File("../wiki/en-es/sample.wordMap"), WordMap.calcW(new File("../wiki/en-es/sample.wordMap")), false);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    public Salience(SparseIntArray[][] x, int W, double alpha) {
        this.W = W;
        this.J = x.length;
        this.alpha = alpha;
        this.ctTotal = new int[2][W];
        this.x = x;
        init();
    }

    @Override
    public Vector<Double> simVecSource(Vector<Integer> termVec) {
        final double[] predicted = new double[W];
        final double[] salience = calculateSalience(termVec, 0);
        for(int j = 0; j < J; j++) {
            for(int w : x[j][1].keySet()) {
                if((double)ctTotal[1][w] - alpha * x[j][1].get(w) != 0.0) {
                    predicted[w] += salience[j] * x[j][1].get(w) / ((double)ctTotal[1][w] - alpha * x[j][1].get(w));
                }
            }
        }
        return RealVector.make(predicted);
       // return RealVector.make(salience);
    }

    @Override
    public Vector<Double> simVecTarget(Vector<Integer> termVec) {
        return new Integer2DoubleVector(termVec);
        //return RealVector.make(calculateSalience(termVec, 1));
    }

    @Override
    public int W() {
        return W;
    }

    @Override
    public int K() {
        //return J;
        return W;
    }
    
    public double[] calculateSalience(Vector<Integer> termVec, int l) {
        double[] salience = new double[J];
        for (int j = 0; j < J; j++) {
            final double xDocLength = x[j][l].sum().doubleValue();
            for (int w : x[j][l].keySet()) {
                if (ctTotal[l][w] != 0) {
                    salience[j] += termVec.doubleValue(w) * x[j][l].doubleValue(w) / ctTotal[l][w];
                }
            }
            if (xDocLength != 0.0) {
                salience[j] /= xDocLength;
            }
        }
       // print(topKwords(salience, 10));
        return salience;
    }
//
//    private void print(Set<String> ss) {
//        System.out.print("(");
//        for (String s : ss) {
//            System.out.print(s);
//            System.out.print(",");
//        }
//        System.out.println(")");
//    }
//
//    private Set<String> topKwords(final double[] scores, final int k) {
//        final IntRBTreeSet set = new IntRBTreeSet(new Comparator<Integer>() {
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                final int s = Double.compare(scores[o1], scores[o2]);
//                if (s == 0) {
//                    return o1 - o2;
//                } else {
//                    return s;
//                }
//            }
//        });
//        for (int w = 0; w < W; w++) {
//            if (set.size() < k) {
//                set.add(w);
//            } else if (scores[set.firstInt()] < scores[w]) {
//                set.remove(set.firstInt());
//                set.add(w);
//            }
//        }
//        final TreeSet<String> words = new TreeSet<String>();
//        for (int w : set) {
//            words.add(wordMap[w]);
//        }
//        return words;
//    }

    private void init() {
        for (int j = 0; j < J; j++) {
            for (int w : x[j][0].keySet()) {
                ctTotal[0][w] += x[j][0].intValue(w);
            }
            for (int w : x[j][1].keySet()) {
                ctTotal[1][w] += x[j][1].intValue(w);
            }
        }
    }
}
