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
package eu.monnetproject.translation.topics.lda;

import eu.monnetproject.lang.Language;
import eu.monnetproject.translation.topics.SparseArray;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author John McCrae
 */
public class GibbsInference {

    /**
     * Number of topics
     */
    protected final int K;
    /**
     * Number of documents
     */
    protected final int D;
    /**
     * Number of words in vocabulary
     */
    protected final int W;
    /**
     * Number of languages in the corpus
     */
    protected final int L;
    /**
     * Length of each document
     */
    protected final int[] DN; // [D]
    /**
     * Word in each document
     */
    protected final int[][] x; // [D][N[i]] < W
    /**
     * Topic assignment
     */
    protected final int[][] z; // [D][N[i]] < K
    /**
     * Counts
     */
    protected final int[][] N_kj;
    protected final int[][][] N_lkw;
    protected final int[][] N_lk;
    /**
     * Document-language assignments
     */
    protected final int[] m; // [D] < L
    /**
     * Similar documents
     */
    protected final int[][] mu; // [D][L] < D
    /**
     * Probability of a topic
     */
    protected final double[] P;
    /**
     * Prior on Dirichlet topics
     */
    protected final double alpha;
    /**
     * Prior on Dirichlet words
     */
    protected final double beta;
    /**
     * The set of "frozen" scores
     */
    protected final boolean[][] frozen;
    /**
     * The threshold for freezing
     */
    protected final double freezingPoint = Double.parseDouble(System.getProperty("lda.freezingPt", "1.0"));
    /**
     * The rate of cooling
     */
    protected final double cooling = Double.parseDouble(System.getProperty("lda.cooling", "0.0"));
    /**
     * The maximum temperature
     */
    protected final int maxTemp;
    protected int temp;
    protected final Random random = new Random();
    protected long begin;
    protected int iterDelta;
    protected final int deltaMax;

    public GibbsInference(int K, int D, int W, int[] DN, int[][] x) {
        this(K, D, W, 1, DN, x, new int[D], blankMu(D));
    }

    private static int[][] blankMu(int D) {
        int[][] mu = new int[D][];
        for (int j = 0; j < D; j++) {
            mu[j] = new int[]{j};
        }
        return mu;
    }

    public GibbsInference(int K, int D, int W, int L, int[] DN, int[][] x, int[] m, int[][] mu) {
        if (cooling > 0) {
            System.err.println("Cooling at rate " + cooling + " freezing at " + freezingPoint);
        }
        this.K = K;
        this.D = D;
        this.W = W;
        this.L = L;
        this.DN = DN;
        this.x = x;
        this.m = m;
        this.mu = mu;
        this.N_kj = new int[K][D];
        this.N_lkw = new int[L][K][W];
        this.N_lk = new int[L][K];
        initN();
        this.z = new int[D][];
        int dm = 0;
        for (int d = 0; d < D; d++) {
            dm += DN[d];
        }
        this.deltaMax = dm;
        this.alpha = Math.min(0.1,2.0 / K);
        this.beta = 0.01;
        this.P = new double[K + 1];
        this.frozen = new boolean[x.length][];
        int t = 0;
        for (int i = 0; i < x.length; i++) {
            this.frozen[i] = new boolean[x[i].length];
            t += x[i].length;
        }
        this.maxTemp = t;
        this.temp = t;
    }

    public GibbsInference(GibbsInput input, int K) {
        if (cooling > 0) {
            System.err.println("Cooling at rate " + cooling + " freezing at " + freezingPoint);
        }
        this.K = K;
        this.D = input.D;
        this.W = input.W;
        this.L = input.languages.length;
        this.DN = input.DN;
        this.x = input.x;
        this.N_kj = new int[K][D];
        this.N_lkw = new int[L][K][W];
        this.N_lk = new int[L][K];
        this.m = input.m;
        this.mu = input.mu;
        initN();
        this.z = new int[D][];
        int dm = 0;
        for (int d = 0; d < D; d++) {
            dm += DN[d];
        }
        this.deltaMax = dm;
        this.alpha = Math.min(0.1,2.0 / K);
        this.beta = 0.01;
        this.P = new double[K + 1];
        this.frozen = new boolean[x.length][];
        int t = 0;
        for (int i = 0; i < x.length; i++) {
            this.frozen[i] = new boolean[x[i].length];
            t += x[i].length;
        }
        this.maxTemp = t;
        this.temp = t;
    }

    public void initializeWithFixedZ(int[][] z2) {
        for (int j = 0; j < D; j++) {
            this.z[j] = new int[DN[j]];
            for (int i = 0; i < DN[j]; i++) {
                assignZ(j, i, z2[j][i]);
            }
        }
    }

    protected final void initN() {
        for (int j = 0; j < D; j++) {
            for(int j2 : mu[j]) {
                N_kj[0][j] += DN[j2];
            }
            N_lk[m[j]][0] += DN[j];
            for (int i = 0; i < DN[j]; i++) {
                N_lkw[m[j]][0][x[j][i]]++;
            }
        }
    }
    
    protected final void initZ() {
        for (int d = 0; d < D; d++) {
            this.z[d] = new int[DN[d]];
            for (int i = 0; i < DN[d]; i++) {
                assignZ(d, i, random.nextInt(K));
            }
        }
    }

    protected void assignZ(int j, int i, int k) {
        final int oldK = z[j][i];
        if (k != oldK) {
            final int w = x[j][i];
            final int l = m[j];

            for(int j2 : mu[j]) {
                N_kj[oldK][j2]--;
            }
            N_lkw[l][oldK][w]--;
            N_lk[l][oldK]--;

            z[j][i] = k;

            for(int j2 : mu[j]) {
                N_kj[k][j2]++;
            }
            N_lkw[l][k][w]++;
            N_lk[l][k]++;

            iterDelta++;
        }
    }

    protected void singleIteration(int c) {
        iterNo++;
        iterDelta = 0;
        for (int j = 0; j < D; j++) {
            for (int i = 0; i < DN[j]; i++) {
                if (!frozen[j][i]) {
                    final int k = sample(i, j);
                    assignZ(j, i, k);
                }
            }
        }
    }

    protected final double a_kj(int k, int j, int dec) {
        return (double) N_kj[k][j] + alpha - dec;
    }

    protected final double b_lwk(int l, int w, int k, int dec) {
        return ((double) N_lkw[l][k][w] + beta - dec);
    }

    protected final double c_lk(int l, int k, int dec) {
        return ((double) N_lk[l][k] + W * beta - dec);
    }

    protected int sample(int i, int j) {
        final int l = m[j];
        double u = random.nextDouble();
        double sum = 0.0;
        double bestPk = Double.NEGATIVE_INFINITY;
        int bestK = 0;
        for (int k = 0; k < K; k++) {
            final int dec = z[j][i] == k ? 1 : 0;
            P[k] = a_kj(k, j, dec) * b_lwk(l, x[j][i], k, dec) / c_lk(l, k, dec);
            assert (P[k] >= 0);
            if (P[k] > bestPk) {
                bestPk = P[k];
                bestK = k;
            }
            if (cooling > 0) {
                P[k] = fastpow(P[k], 1.0 + cooling * iterNo);
            }
            sum += P[k];
        }
        if (P[bestK] / sum > freezingPoint) {
            frozen[j][i] = true;
            temp--;
            return bestK;
        } else {
            for (int k = 0; k < K; k++) {
                if (u < (P[k] / sum)) {
                    return k;
                }
                P[k + 1] += P[k];
            }
        }
        throw new RuntimeException("P[K] = " + P[K] + " sum= " + sum);
    }
    
    private static final int Z_MAX = 10000;
    private static final double[] log_z = new double[Z_MAX];

    static {
        for (int i = 1; i < Z_MAX; i++) {
            log_z[i] = Math.log(i);
        }
    }

    public static double fastpow(double z, double a) {
        final int zc = (int)Math.ceil(z);
        final double z1 = (z - zc) / zc;
        final double lz = (zc < Z_MAX ? log_z[zc] : Math.log(zc)) + z1 - z1 * z1;
        final double a1 = (a - Math.floor(a)) * lz;
        double y = 1.0;
        for (int i = 0; i < (int)a; i++) {
            y *= z;
        }
        return y * (1 + a1 + a1 * a1 / 2);
    }

    void logZ() {
        System.err.println("z:");
        for (int j = 0; j < D; j++) {
            for (int i = 0; i < DN[j]; i++) {
                System.err.print(z[j][i] + " ");
            }
            System.err.println();
        }
    }
    
    protected int iterNo = 0;

    public void iterator(int total) {
        iterator(total, total);
    }

    /**
     * Perform {@code count} iterations of Gibbs sampling
     *
     * @param count The count
     * @param total The total iteration count (for calculating ETAs)
     */
    public void iterator(int count, final int total) {
        if(z[0] == null) {
            initZ();
        }
        if (begin == 0) {
            begin = System.currentTimeMillis();
        }
        for (int c = 0; c < count; c++) {
            //logZ();
            singleIteration(c);
            if (c % 10 == 0) {
                iterInfo(iterNo, total);
            }
        }
    }

//    protected double[][] phi() {
//        double[][] phi = new double[W][];
//        for (int w = 0; w < W; w++) {
//            phi[w] = new double[K];
//            for (int k = 0; k < K; k++) {
//                phi[w][k] = ((double) N_kw[k][w] + beta) / ((double) N_k(k) + W * beta);
//            }
//        }
//        return phi;
//    }
    protected double[][][] phiPolyLingual() {
        double[][][] phi = new double[L][][];
        for (int l = 0; l < L; l++) {
            phi[l] = new double[W][];
            for (int w = 0; w < W; w++) {
                phi[l][w] = new double[K];
                for (int k = 0; k < K; k++) {
                    phi[l][w][k] = ((double) N_lkw[l][k][w] + beta) / ((double) N_lk[l][k] + W * beta);
                }
            }
        }

        return phi;
    }

    protected double[][] theta() {
        double[][] theta = new double[K][];
        for (int k = 0; k < K; k++) {
            theta[k] = new double[D];
            for (int j = 0; j < D; j++) {
                int DNj = 0;
                for(int j2 : mu[j]) {
                    DNj += DN[j2];
                }
                theta[k][j] = ((double) N_kj[k][j] + alpha) / ((double) DNj + K * alpha);
            }
        }
        return theta;
    }

    public GibbsData getData() {
        return getPolylingualData(new Language[]{Language.ENGLISH}, new HashMap<String, Integer>()).monolingual(0);
        //return new GibbsData(N_k, SparseArray.fromArray(N_kw), K, W, D, alpha, beta, phi(), theta());
    }

    public PolylingualGibbsData getPolylingualData(Language[] languages, Map<String, Integer> words) {
        assert (languages.length == L);
        return new PolylingualGibbsData(N_lk, SparseArray.fromArray(N_lkw), K, W, D, alpha, beta, phiPolyLingual(), theta(), languages, words);
    }

    protected Collection<Integer> wordsInDoc(int j) {
        final HashSet<Integer> rval = new HashSet<Integer>();
        for (int i = 0; i < DN[j]; i++) {
            rval.add(x[j][i]);
        }
        return rval;
    }

    private String timeString(long time) {
        return (time / 3600000) + "h" + (time % 3600000 / 60000) + "m" + (time % 60000 / 1000) + "s";
    }

    protected void iterInfo(int iterNo, int iters) {
        long elapsed = System.currentTimeMillis() - begin;
        long expected = (long) (((double) (iters - iterNo) / (double) (iterNo)) * elapsed);
        double percent = (double) iterDelta / (double) deltaMax * 100.0;
        System.err.println("Iteration " + iterNo + " (Elapsed: " + timeString(elapsed) + " ETA:" + timeString(expected) + " Delta:" + iterDelta + " (" + percent + "%) Temperature: " + ((double) temp / (double) maxTemp) + ")");
    }
}
