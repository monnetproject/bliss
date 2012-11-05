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

import eu.monnetproject.math.sparse.Integer2DoubleVector;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.translation.topics.SimilarityMetric;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static eu.monnetproject.translation.topics.sim.Metrics.*;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.util.Map.Entry;

/**
 *
 * @author John McCrae
 */
public class BetaLMImpl implements SimilarityMetric {
    /**
     * Enum capturing various similarity methods supported by Beta-LM
     */
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
    /**
     * The raw data
     */
    public final SparseIntArray[][] x; // jli
    /**
     * The maximum number of words
     */
    public final int W;
    private final double mu[];
    private final SparseRealArray muSp;
    public final double mu_f[];
    private final SparseRealArray mu_fSp;
    public final double sumMu2;
    public final Map<String, Integer> words;
    public final Method method = Method.valueOf(System.getProperty("paraSimMethod", "COS_SIM"));
    private final double df[];
    private final SparseRealArray dfSp;
    private final WxWCLESA wxwclesa;

    public BetaLMImpl(File data) throws IOException {
        final ParallelReader pr = ParallelReader.fromFile(data);
        this.W = pr.W();
        this.x = pr.x;
        this.words = pr.words;

        this.mu = new double[this.W];
        this.muSp = null;
        this.mu_f = new double[this.W];
        this.mu_fSp = null;
        this.sumMu2 = initMu(x, this.W);
        if (method == Method.DF_DICE || method == Method.DF_JACCARD) {
            df = new double[this.W];
            this.dfSp = null;
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
            dfSp = null;
        }
        if(method == Method.WxWCLESA) {
            wxwclesa = new WxWCLESA(data);
        } else {
            wxwclesa = null;
        }
    }
    
    public BetaLMImpl(File data, int W, int n) throws IOException {
        final ParallelReader pr = ParallelReader.fromFile(data, W, n);
        this.W = pr.W();
        this.x = pr.x;
        this.words = pr.words;

        this.mu = null;
        this.muSp = new SparseRealArray(W);
        this.mu_f = null;
        this.mu_fSp = new SparseRealArray(W);
        this.sumMu2 = initMuSp(x, this.W);
        if (method == Method.DF_DICE || method == Method.DF_JACCARD) {
            df = null;
            this.dfSp = new SparseRealArray(W);
            for (int j = 0; j < x.length; j++) {
                for (int w : x[j][0].keySet()) {
                    dfSp.inc(w);
                }
            }
            for (int w = 0; w < this.W; w++) {
                dfSp.divide(w, x.length);
            }
        } else {
            df = null;
            dfSp = null;
        }
        if(method == Method.WxWCLESA) {
            wxwclesa = new WxWCLESA(data);
        } else {
            wxwclesa = null;
        }
    }
    
    public BetaLMImpl(File srcData, File trgData, File freqs, int J, int W) throws IOException {
        this.W = W;
        this.x = BinaryReader.read2FromFile(srcData, trgData, J, W);
        this.words = freqs == null ? null : BinaryReader.readWords(freqs);
        
        this.mu = new double[this.W];
        this.muSp = null;
        this.mu_f = new double[this.W];
        this.mu_fSp = null;
        this.sumMu2 = initMu(x, this.W);
        if (method == Method.DF_DICE || method == Method.DF_JACCARD) {
            df = new double[this.W];
            this.dfSp = null;
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
            dfSp = null;
        }
        if(method == Method.WxWCLESA) {
            throw new UnsupportedOperationException("TODO");
        } else {
            wxwclesa = null;
        }
        
    }

    public BetaLMImpl(SparseIntArray[][] x, int W, String[] words) {
        this.words = new HashMap<String, Integer>();
        this.x = x;
        this.W = W;
        for (int i = 0; i < words.length; i++) {
            this.words.put(words[i], i);
        }

        this.mu = null;
        this.muSp = new SparseRealArray(W);
        this.mu_f = null;
        this.mu_fSp = new SparseRealArray(W);
        this.sumMu2 = initMuSp(x, this.W);
        if (method == Method.DF_DICE || method == Method.DF_JACCARD) {
            df = null;
            this.dfSp = new SparseRealArray(W);
            for (int j = 0; j < x.length; j++) {
                for (int w : x[j][0].keySet()) {
                    dfSp.inc(w);
                }
            }
            for (int w = 0; w < this.W; w++) {
                dfSp.divide(w, x.length);
            }
        } else {
            df = null;
            dfSp = null;
        }
        if(method == Method.WxWCLESA) {
            throw new IllegalArgumentException();
        } else {
            wxwclesa = null;
        }
    }
    
    

    public BetaLMImpl(SparseIntArray[][] x, int W, int n) {
        this.words = new HashMap<String, Integer>();
        this.x = x;
        this.W = W;
        
        this.mu = new double[this.W];
        this.muSp = null;
        this.mu_f = new double[this.W];
        this.mu_fSp = null;
        this.sumMu2 = initMu(x, W);
        if (method == Method.DF_DICE) {
            df = new double[this.W];
            this.dfSp = null;
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
            dfSp = null;
        }
        wxwclesa = null;
    }

    private double initMu(SparseIntArray[][] x, int W) {
        int N = 0;
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
                    mu_f[w] += fw / sum_f;
                }
            }
        }
        double sM2 = 0;
        for (int w = 0; w < W; w++) {
            mu[w] = mu[w] / N;
            mu_f[w] = mu_f[w] / N;
            sM2 += mu[w] * mu[w];
        }
        return sM2;
    }
    
    private double initMuSp(SparseIntArray[][] x, int W) {
        int N = 0;
        for (int j = 0; j < x.length; j++) {
            N++;
            final int sum = x[j][0].sum();
            final int sum_f = x[j][1].sum();
            if (sum == 0 && sum_f == 0) {
                continue;
            }
            if (sum != 0) {
                for (int w : x[j][0].keySet()) {
                    muSp.add(w, ((double) x[j][0].get(w)) / sum);
                }
            }

            if (sum_f != 0) {
                for (int w : x[j][1].keySet()) {
                    final double fw = (double) x[j][1].get(w);
                    mu_fSp.add(w,fw / sum_f);
                }
            }
        }
        double sM2 = 0;
        for(Entry<Integer, Double> muSp_w : muSp.entrySet()) {
            muSp_w.setValue(muSp_w.getValue() / N);
            sM2 += muSp_w.getValue() * muSp_w.getValue();
        }
        for(Entry<Integer, Double> mu_fSp_w : mu_fSp.entrySet()) {
            mu_fSp_w.setValue(mu_fSp_w.getValue() / N);
        }
        return sM2;
    }

    public double[] simVec(List<String> simTerms) {
        final SparseIntArray termVec = new SparseIntArray(W);
        for (String term : simTerms) {
            if (words.containsKey(term)) {
                termVec.inc(words.get(term));
            }
        }
        return simVecSource(termVec).toDoubleArray();

    }
    private final double cosSimPower = Double.parseDouble(System.getProperty("cosSimPower", "1.0"));
    private static final Random random = new Random();

    private double cosSimPower() {
        return Double.isInfinite(cosSimPower) ? 1.0 : cosSimPower;
    }
    
    private boolean isInfiniteCosSim() {
        return Double.isInfinite(cosSimPower);
    }
            
    
    private double calcCosSim(Vector<Integer> termVec, int j, double kldSum) throws RuntimeException {
        switch (method) {
            case COS_SIM:
                return Math.pow(cosSim(termVec, x[j][0]), cosSimPower());
            case NORMAL_COS_SIM:
                return Math.pow(normalCosSim(termVec, x[j][0],mu,sumMu2), cosSimPower());
            case KLD:
                return Math.pow(kullbackLeiblerDivergence(termVec, x[j][0]), cosSimPower()) / kldSum;
            case JACCARD:
                return Math.pow(jaccardIndex(termVec, x[j][0]), cosSimPower());
            case DICE:
                return Math.pow(diceCoefficient(termVec, x[j][0]), cosSimPower());
            case ROGERS_TANIMOTO:
                return Math.pow(rogersTanimoto(termVec, x[j][0]), cosSimPower());
            case DF_DICE:
                return Math.pow(dfDiceCoefficient(termVec, x[j][0],df), cosSimPower());
            case DF_JACCARD:
                return Math.pow(dfJaccardCoefficient(termVec, x[j][0],df), cosSimPower());
            case WxWCLESA:
                return cosSim(wxwclesa.simVecSource(termVec), x[j][0]);
            default:
                throw new RuntimeException();
        }
    }
    
    /**
     * Use this to output higher n-grams
     * @param termVec The term vector
     * @return The resultant n-gram prediction as a sparse array
     */
    public SparseRealArray predictedModel(SparseIntArray termVec) {
        final SparseRealArray sim = new SparseRealArray(W);
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
            double cosSim = calcCosSim(termVec, j, kldSum);
            final int sum = x[j][1].sum();
            if (sum == 0) {
                continue;
            }
            if (isInfiniteCosSim()) {
                if ((cosSim > bestSim && (ties = 0) == 0)
                        || (cosSim == bestSim && random.nextInt(++ties) == 0)) {
                    sim.clear();
                    for(Int2IntMap.Entry es : x[j][1].int2IntEntrySet()) {
                        sim.put(es.getIntKey(), es.getIntValue());
                    }
                    bestSim = cosSim;
                }
            } else {
                for (int w : x[j][1].keySet()) {
                    sim.add(w,cosSim * x[j][1].get(w) / sum);
                }
            }
        }
        for (int w = 0; w < W; w++) {
            sim.divide(w, N);
        }
        return sim;
        
    }
    
    /**
     * Generate the predicted TF-vector (for information retrieval experiments)
     * @param termVec The TF-vector in the source language
     * @return The predicted TF-vector in the target language
     */
    @Override
    public Vector<Double> simVecSource(Vector<Integer> termVec) {
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
            double cosSim = calcCosSim(termVec, j, kldSum);
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
                }
            }
        }
        for (int w = 0; w < W; w++) {
            sim[w] = sim[w] / N;
        }
        return new RealVector(sim);
    }

    @Override
    public int W() {
        return W;
    }

    public double[] simVec(int[] doc) {
        return simVecSource(ParallelReader.histogram(doc, W)).toDoubleArray();
    }

    /**
     * For compatibility in the IR experiments: this returns the 'predicted'
     * TF-vector in the target language, which is of course the same vector
     * @param termVec The term vector in the target language
     * @return The same vector as a 'predicted' vector
     */
    @Override
    public Vector<Double> simVecTarget(Vector<Integer> termVec) {
        return new Integer2DoubleVector(termVec);
    }
}
