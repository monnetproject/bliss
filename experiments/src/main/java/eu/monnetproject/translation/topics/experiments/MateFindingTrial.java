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
package eu.monnetproject.translation.topics.experiments;

import eu.monnetproject.translation.topics.SparseArray;
import eu.monnetproject.translation.topics.lda.PolylingualGibbsData;
import eu.monnetproject.translation.topics.SimilarityMetric;
import eu.monnetproject.translation.topics.sim.BetaLMImpl;
import eu.monnetproject.translation.topics.clesa.CLESA;
import eu.monnetproject.translation.topics.sim.LinearSurjection;
import eu.monnetproject.translation.topics.sim.MinErrorSurjection;
import eu.monnetproject.translation.topics.sim.ParallelReader;
import eu.monnetproject.translation.topics.sim.WxWCLESA;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author John McCrae
 */
public class MateFindingTrial {

    private static final Random random = new Random();

    public static void compare(String trainFile, String testFile) throws Exception {
        final SimilarityMetric parallelSimilarity;
        
        final String method = System.getProperty("paraSimMethod", "COS_SIM");
        System.err.println("Metric = " + method);
        System.err.println("Reading train data");
        if (method.equals("CLESA")) {
            parallelSimilarity = new CLESA(new File(trainFile));
       // } else if (method.equals("OLDCLESA")) {
       //     parallelSimilarity = new OLDCLESA(new File(trainFile));
        } else if (method.equals("WXWCLESA")) {
            parallelSimilarity = new WxWCLESA(new File(trainFile));
        } else if(method.equals("LDA")) {
            int l1 = Integer.parseInt(System.getProperty("ldaLang1", "0"));
            int l2 = Integer.parseInt(System.getProperty("ldaLang2", "1"));
            parallelSimilarity = new LDAParaSim(PolylingualGibbsData.read(new FileInputStream(trainFile)), l1, l2);
        } else if(method.equals("WXWLDA")) {
            int l1 = Integer.parseInt(System.getProperty("ldaLang1", "0"));
            int l2 = Integer.parseInt(System.getProperty("ldaLang2", "1"));
            parallelSimilarity = new WxWLDAParaSim(PolylingualGibbsData.read(new FileInputStream(trainFile)), l1, l2);
      //  } else if(method.equals("SVD")) {
      //      parallelSimilarity = new SVDParaSim(new File(trainFile));
        } else if(method.equals("LIN_SURJ")) {
            parallelSimilarity = new LinearSurjection(new File(trainFile));
        } else if(method.equals("ME_SURJ")) {
            parallelSimilarity = new MinErrorSurjection(new File(trainFile));
        } else {
            parallelSimilarity = new BetaLMImpl(new File(trainFile));
        }
        compare(parallelSimilarity, testFile);
    }
    
//    public static void compare(String srcTestFile, String trgTestFile, int W, String testFile) throws Exception {
//        
//        final SimilarityMetric parallelSimilarity;
//        
//        final String method = System.getProperty("paraSimMethod", "COS_SIM");
//        System.err.println("Metric = " + method);
//        System.err.println("Reading train data");
//        if (method.equals("CLESA")) {
//            throw new RuntimeException("TODO");
//        } else if (method.equals("WXWCLESA")) {
//            throw new RuntimeException("TODO");
//        } else if(method.equals("LDA")) {
//            throw new RuntimeException("TODO");
//        } else if(method.equals("WXWLDA")) {
//            throw new RuntimeException("TODO");
//        } else if(method.equals("SVD")) {
//            throw new RuntimeException("TODO");
//        } else if(method.equals("LIN_SURJ")) {
//            throw new RuntimeException("TODO");
//        } else if(method.equals("ME_SURJ")) {
//            throw new RuntimeException("TODO");
//        } else {
//            parallelSimilarity = new ParallelSimilarityOnDisk(new File(srcTestFile), new File(trgTestFile), W);
//        }
//        compare(parallelSimilarity, testFile);
//    }
//    
    public static void compare(SimilarityMetric parallelSimilarity, String testFile) throws Exception {
        System.err.println("Reading test data");
        final BufferedReader in = new BufferedReader(new FileReader(testFile));
        final List<SparseArray[]> docs = new ArrayList<SparseArray[]>();
        String s;
        int W2 = 0;
        while (!(s = in.readLine()).matches("\\s*")) {
            final String[] ss = s.split("\\s+");
            final int[] i1 = new int[ss.length];
            for (int i = 0; i < ss.length; i++) {
                i1[i] = Integer.parseInt(ss[i]);
                if (i1[i] > W2) {
                    W2 = i1[i];
                }
            }
            s = in.readLine();
            final String[] ss2 = s.split("\\s+");
            final int[] i2 = new int[ss2.length];
            for (int i = 0; i < ss2.length; i++) {
                i2[i] = Integer.parseInt(ss2[i]);
                if (i2[i] > W2) {
                    W2 = i2[i];
                }
            }
            docs.add(new SparseArray[]{ParallelReader.histogram(i1,parallelSimilarity.W()), ParallelReader.histogram(i2,parallelSimilarity.W())});
        }
        System.err.println("Preparing data (" + docs.size() + ")");
        int idx = 0;
        double[][] predicted = new double[docs.size()][];
        double[][] foreign = new double[docs.size()][];
        for (SparseArray[] doc : docs) {
            predicted[idx] = parallelSimilarity.simVecSource(doc[0]);
            foreign[idx++] = parallelSimilarity.simVecTarget(doc[1]);
            //   System.out.println(Arrays.toString(predicted[idx-1]));
            //   System.out.println(Arrays.toString(foreign[idx-1]));
            if (idx % 10 == 0) {
                System.err.print(".");
            }
        }
        System.err.println("Starting classification");
        int correct = 0;
        int incorrect = 0;
        int ties = 0;
        int correct5 = 0;
        int correct10 = 0;
        double mrr = 0;
        for (int i = 0; i < predicted.length; i++) {
            double rightScore = cosSim(predicted[i], foreign[i]);
            int bestJ = -1;
            int rank = 1;
            double bestMatch = -Double.MAX_VALUE;
            for (int j = 0; j < foreign.length; j++) {
                final double cosSim = cosSim(predicted[i], foreign[j]);
                if ((cosSim > bestMatch && (ties = 0) == 0)
                        || (cosSim == bestMatch && random.nextInt(++ties) == 0)) {
                    bestMatch = cosSim;
                    bestJ = j;
                }
                if(cosSim > rightScore) {
                    rank++;
                }
                //System.out.println(i+","+j+","+cosSim);
            }
            if (i == bestJ) {
                correct++;
                System.out.print("+");
            } else {
                incorrect++;
                if(rank < 10) {
                    System.out.print(rank);
                } else {
                    System.out.print("-");
                }
            }
            if(rank <= 5) {
                correct5++;
            }
            if(rank <= 10) {
                correct10++;
            }
            mrr += 1.0 / rank;
        }
        System.out.println();
        System.out.println("Precision@1: " + correct);
        System.out.println("Precision@5: " + correct5);
        System.out.println("Precision@10: " + correct10);
        System.out.println("MRR: " + (mrr / predicted.length));
        
    }

    public static double cosSim(double[] vec1, double[] vec2) {
        double ab = 0.0;
        double a2 = 0.0;
        assert (vec1.length == vec2.length);
        for (int i = 0; i < vec1.length; i++) {
            ab += vec2[i] * vec1[i];
            a2 += vec1[i] * vec1[i];
        }
        double b2 = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            b2 += vec2[i] * vec2[i];
        }
        return a2 > 0 && b2 > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
    }

    public static void main(String[] args) throws Exception {
        if(args.length == 2) {
            compare(args[0],args[1]);
//        } else if(args.length == 4) {
//            compare(args[0], args[1], Integer.parseInt(args[2]), args[3]);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
