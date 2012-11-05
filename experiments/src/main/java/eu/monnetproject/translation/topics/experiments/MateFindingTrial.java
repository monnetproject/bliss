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

import eu.monnetproject.math.sparse.IntVector;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.translation.topics.CLIOpts;
import eu.monnetproject.translation.topics.ParallelBinarizedReader;
import eu.monnetproject.translation.topics.SimilarityMetric;
import eu.monnetproject.translation.topics.SimilarityMetricFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author John McCrae
 */
public class MateFindingTrial {

    private static final Random random = new Random();

    @SuppressWarnings("unchecked")
    public static void compare(File trainFile, Class<SimilarityMetricFactory> factoryClazz, int W, File testFile, boolean oneStep) throws Exception {
        final ParallelBinarizedReader testPBR = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(testFile));
        final SimilarityMetricFactory smf = factoryClazz.newInstance();
        final SimilarityMetric metric;
        if (smf.datatype().equals(ParallelBinarizedReader.class)) {
            final ParallelBinarizedReader trainPBR = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(trainFile));
            metric = ((SimilarityMetricFactory<ParallelBinarizedReader>) smf).makeMetric(trainPBR, W);
        } else if (InputStream.class.isAssignableFrom(smf.datatype())) {
            final InputStream train = CLIOpts.openInputAsMaybeZipped(trainFile);
            metric = ((SimilarityMetricFactory<InputStream>) smf).makeMetric(train, W);
        } else if (Object.class.equals(smf.datatype())) {
            metric = ((SimilarityMetricFactory<Object>) smf).makeMetric(null, W);
        } else {
            throw new IllegalArgumentException();
        }

        compare(metric, W, testPBR, oneStep);
    }

    public static void compare(SimilarityMetric parallelSimilarity, int W, ParallelBinarizedReader testFile, boolean oneStep) throws Exception {
        System.err.println("Reading test data");
        final List<SparseIntArray[]> docs = new ArrayList<SparseIntArray[]>();
        SparseIntArray[] s;
        int W2 = 0;
        while ((s = testFile.nextFreqPair(W)) != null) {
            docs.add(s);
        }
        System.err.println("Preparing data (" + docs.size() + ")");
        int idx = 0;
        double[][] predicted = new double[docs.size()][];
        double[][] foreign = new double[docs.size()][];
        if (!oneStep) {
            for (SparseIntArray[] doc : docs) {
                predicted[idx] = parallelSimilarity.simVecSource(doc[0]).toDoubleArray();
                foreign[idx++] = parallelSimilarity.simVecTarget(doc[1]).toDoubleArray();
                //   System.out.println(Arrays.toString(predicted[idx-1]));
                //   System.out.println(Arrays.toString(foreign[idx-1]));
                if (idx % 10 == 0) {
                    System.err.print(".");
                }
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
            double rightScore;
            if(oneStep) {
                rightScore = cosSim(parallelSimilarity.simVecSource(docs.get(i)[0]), parallelSimilarity.simVecTarget(docs.get(i)[1]));
            } else {
                rightScore = cosSim(predicted[i], foreign[i]);
            }
            int bestJ = -1;
            int rank = 1;
            double bestMatch = -Double.MAX_VALUE;
            for (int j = 0; j < foreign.length; j++) {
                final double cosSim;
                if(oneStep) {
                    cosSim = cosSim(parallelSimilarity.simVecSource(docs.get(i)[0]), parallelSimilarity.simVecTarget(docs.get(j)[1]));
                } else {
                    cosSim = cosSim(predicted[i], foreign[j]);
                }
                if ((cosSim > bestMatch && (ties = 0) == 0)
                        || (cosSim == bestMatch && random.nextInt(++ties) == 0)) {
                    bestMatch = cosSim;
                    bestJ = j;
                }
                if (cosSim > rightScore) {
                    rank++;
                }
                //System.out.println(i+","+j+","+cosSim);
            }
            if (i == bestJ) {
                correct++;
                System.out.print("+");
            } else {
                incorrect++;
                if (rank < 10) {
                    System.out.print(rank);
                } else {
                    System.out.print("-");
                }
            }
            if (rank <= 5) {
                correct5++;
            }
            if (rank <= 10) {
                correct10++;
            }
            mrr += 1.0 / rank;
        }
        System.out.println();
        System.out.println("Precision@1: " + correct);
        System.out.println("Precision@5: " + correct5);
        System.out.println("Precision@10: " + correct10);
        System.out.println("MRR: " + (mrr / docs.size()));

    }
 public static <M extends Number, N extends Number> double cosSim(Vector<M> vec1, Vector<N> vec2) {
        double ab = 0.0;
        double a2 = 0.0;
        for (int i : vec1.keySet()) {
            ab += vec2.value(i).doubleValue() * vec1.value(i).doubleValue();
            a2 +=  vec1.value(i).doubleValue() * vec1.value(i).doubleValue();
        }
        double b2 = 0.0;
        for (int i : vec2.keySet()) {
            b2 +=  vec2.value(i).doubleValue() * vec2.value(i).doubleValue();
        }
        return a2 > 0 && b2 > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
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
        final CLIOpts opts = new CLIOpts(args);

        final boolean oneStep = opts.flag("oneStep", "Calculate the mate-finding in one-step mode (this involves J^2 calls to the similarity function)");

        final File trainFile = opts.roFile("trainFile", "The training file");

        final Class<SimilarityMetricFactory> factoryClazz = opts.clazz("metricFactory", SimilarityMetricFactory.class, "The factory for the cross-lingual similarity measure");

        final int W = opts.intValue("W", "The total number of distinct tokens");

        final File testFile = opts.roFile("testFile", "The test file");

        if (!opts.verify(MateFindingTrial.class)) {
            return;
        }

        compare(trainFile, factoryClazz, W, testFile, oneStep);
    }
}
