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
package eu.monnetproject.translation.topics.sim;

import eu.monnetproject.translation.topics.SparseArray;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static eu.monnetproject.translation.topics.sim.Metrics.*;

/**
 *
 * @author John McCrae
 */
public class BetaLM implements SimilarityMetric {

    public enum Method {
        COS_SIM,
        NORMAL_COS_SIM,
        KLD,
        JACCARD,
        DICE,
        ROGERS_TANIMOTO,
        DF_JACCARD,
        DF_DICE,
        WxWCLESA
    }
    public final SparseArray[][] x; // jli
    public final int W;
    public final double mu[];
    public final double mu_f[];
    public final double sumMu2;
    public final Map<String, Integer> words;
    public final Method method = Method.valueOf(System.getProperty("paraSimMethod", "COS_SIM"));
    private final double df[];
    private final WxWCLESA wxwclesa;

    public BetaLM(File data) throws IOException {
        final ParallelReader pr = ParallelReader.fromFile(data);
        this.W = pr.W;
        this.x = pr.x;
        this.words = pr.words;

        this.mu = new double[this.W];
        this.mu_f = new double[this.W];
        this.sumMu2 = initMu(x, this.W);
        if (method == Method.DF_DICE || method == Method.DF_JACCARD) {
            df = new double[this.W];
            for (int j = 0; j < x.length; j++) {
                for (int w : x[j][0].keySet()) {
                    df[w]++;
                }
            }
            for (int w = 0; w < this.W; w++) {
                df[w] = df[w] / x.length;
            }
        } else {
            df = null;
        }
        if(method == Method.WxWCLESA) {
            wxwclesa = new WxWCLESA(data);
        } else {
            wxwclesa = null;
        }
    }
    
    public BetaLM(File srcData, File trgData, File freqs, int J, int W) throws IOException {
        this.W = W;
        this.x = BinaryReader.read2FromFile(srcData, trgData, J, W);
        this.words = freqs == null ? null : BinaryReader.readWords(freqs);
        
        this.mu = new double[this.W];
        this.mu_f = new double[this.W];
        this.sumMu2 = initMu(x, this.W);
        if (method == Method.DF_DICE || method == Method.DF_JACCARD) {
            df = new double[this.W];
            for (int j = 0; j < x.length; j++) {
                for (int w : x[j][0].keySet()) {
                    df[w]++;
                }
            }
            for (int w = 0; w < this.W; w++) {
                df[w] = df[w] / x.length;
            }
        } else {
            df = null;
        }
        if(method == Method.WxWCLESA) {
            throw new UnsupportedOperationException("TODO");
        } else {
            wxwclesa = null;
        }
        
    }

    public BetaLM(SparseArray[][] x, int W, String[] words) {
        this.words = new HashMap<String, Integer>();
        this.x = x;
        this.W = W;
        for (int i = 0; i < words.length; i++) {
            this.words.put(words[i], i);
        }

        this.mu = new double[this.W];
        this.mu_f = new double[this.W];
        this.sumMu2 = initMu(x, W);
        if (method == Method.DF_DICE) {
            df = new double[this.W];
            for (int j = 0; j < x.length; j++) {
                for (int w : x[j][0].keySet()) {
                    df[w]++;
                }
            }
            for (int w = 0; w < this.W; w++) {
                df[w] = df[w] / x.length;
            }
        } else {
            df = null;
        }
        wxwclesa = null;
    }

    private double initMu(SparseArray[][] x, int W) {
        int N = 0;
//        int df = 0;
        for (int j = 0; j < x.length; j++) {
            N++;
            final int sum = x[j][0].sum();
            final int sum_f = x[j][1].sum();
            if (sum == 0 && sum_f == 0) {
                continue;
            }
            if (sum != 0) {
                for (int w : x[j][0].keySet()) {
                    mu[w] += ((double) x[j][0].get(w)) / sum;
                }
            }

            if (sum_f != 0) {
                for (int w : x[j][1].keySet()) {
                    final double fw = (double) x[j][1].get(w);
//                    if (w == 48) {
//                        df++;
//                    }
                    mu_f[w] += fw / sum_f;
                }
            }
        }
//        System.err.println();
//        System.err.println("DF:" + df);
//        if (mu.length > 297) {
//            System.err.println("e48=" + mu[48]);
//            System.err.println("e297=" + mu[927]);
//            System.err.println("f48=" + mu_f[48] + "/" + x.length);
//            System.err.println("f297=" + mu_f[927]);
//        }
        double sM2 = 0;
        for (int w = 0; w < W; w++) {
            mu[w] = mu[w] / N;
            mu_f[w] = mu_f[w] / N;
            sM2 += mu[w] * mu[w];
        }
        return sM2;
    }

    public double[] simVec(List<String> simTerms) {
        final SparseArray termVec = new SparseArray(W);
        for (String term : simTerms) {
            if (words.containsKey(term)) {
                termVec.inc(words.get(term));
            }
        }
        return simVecSource(termVec);

    }
    private final double cosSimPower = Double.parseDouble(System.getProperty("cosSimPower", "1.0"));
    private static final Random random = new Random();

    private double cosSimPower() {
        return Double.isInfinite(cosSimPower) ? 1.0 : cosSimPower;
    }
    
    private boolean isInfiniteCosSim() {
        return Double.isInfinite(cosSimPower);
    }
            
    
    @Override
    public double[] simVecSource(SparseArray termVec) {
        double[] sim = new double[W];
        //double[] prior = new double[W];
        int N = 0;
        double bestSim = 0.0;
        int ties = 0;
        double kldSum = 0.0;
        if (method == Method.KLD) {
            for (int j = 0; j < x.length; j++) {
                kldSum += Math.abs(Math.pow(kullbackLeiblerDivergence(termVec, x[j][0]), cosSimPower()));
            }
        }

        for (int j = 0; j < x.length; j++) {
            N++;

            final double cosSim;
            switch (method) {
                case COS_SIM:
                    cosSim = Math.pow(cosSim(termVec, x[j][0]), cosSimPower());
                    break;
                case NORMAL_COS_SIM:
                    cosSim = Math.pow(normalCosSim(termVec, x[j][0],mu,sumMu2), cosSimPower());
                    break;
                case KLD:
                    cosSim = Math.pow(kullbackLeiblerDivergence(termVec, x[j][0]), cosSimPower()) / kldSum;
                    break;
                case JACCARD:
                    cosSim = Math.pow(jaccardIndex(termVec, x[j][0]), cosSimPower());
                    break;
                case DICE:
                    cosSim = Math.pow(diceCoefficient(termVec, x[j][0]), cosSimPower());
                    break;
                case ROGERS_TANIMOTO:
                    cosSim = Math.pow(rogersTanimoto(termVec, x[j][0]), cosSimPower());
                    break;
                case DF_DICE:
                    cosSim = Math.pow(dfDiceCoefficient(termVec, x[j][0],df), cosSimPower());
                    break;
                case DF_JACCARD:
                    cosSim = Math.pow(dfJaccardCoefficient(termVec, x[j][0],df), cosSimPower());
                    break;
                case WxWCLESA:
                    cosSim = cosSim(wxwclesa.simVecSource(termVec), x[j][0]);
                default:
                    throw new RuntimeException();
            }
            //System.out.print(cosSim+",");
            final int sum = x[j][1].sum();
            if (sum == 0) {
                continue;
            }
            if (isInfiniteCosSim()) {
                if ((cosSim > bestSim && (ties = 0) == 0)
                        || (cosSim == bestSim && random.nextInt(++ties) == 0)) {
                    final double[] s = x[j][1].toDoubleArray();
                    if (s.length < sim.length) {
                        Arrays.fill(sim, 0.0);
                    }
                    System.arraycopy(s, 0, sim, 0, s.length);
                    bestSim = cosSim;
                }
            } else {
                for (int w : x[j][1].keySet()) {
                    sim[w] += cosSim * x[j][1].get(w) / sum;
                    //sim[w] = cosSim * x[j][1].get(w) / N / sum + (N - 1.0) * sim[w] / N;
                    //               prior[w] = (double) x[j][1].get(w) / N / sum + (N - 1.0) * prior[w] / N;
                }
            }
        }
        // System.out.println();
        for (int w = 0; w < W; w++) {
            sim[w] = sim[w] / N;
        }

//        for (int w = 0; w < W; w++) {
//            if(prior[w] > 0) {
//                sim[w] = sim[w] / prior[w];
//            }
//        }
        return sim;
    }

    @Override
    public int W() {
        return W;
    }

    public double[] simVec(int[] doc) {
        return simVecSource(ParallelReader.histogram(doc, W));
    }

    @Override
    public double[] simVecTarget(SparseArray termVec) {
        return termVec.toDoubleArray();
    }
}
