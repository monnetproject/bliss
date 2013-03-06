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
package eu.monnetproject.bliss.lda;

import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition;
import eu.monnetproject.bliss.AssignmentBuffer;
import eu.monnetproject.bliss.CLIOpts;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Coupled Probabilistic Latent Semantic Analysis
 *
 * @author John McCrae
 */
public class CPLSATrain {
    private final double alpha;
    private final double beta;
    private final int W, J, K;
    private final int[][] N_jl;
    private final int L = 2;
    public final double[][][] phi_lwk, theta_lkj;
    private final int[][][] N_lkj;
    private final int[][][] N_lwk;
    private final int[][] N_lk;
    private boolean initialized = false;
    private final AssignmentBuffer buf;

    public CPLSATrain(File corpus, int J, int W, int K, double alpha, double beta) throws IOException {
        this.buf = AssignmentBuffer.interleavedFrom(corpus);
        this.W = W;
        this.K = K;
        this.J = J;
        this.alpha = alpha;
        this.beta = beta;
        this.N_jl = new int[J][L];
        this.phi_lwk = new double[L][W][K];
        this.theta_lkj = new double[L][K][J];
        this.N_lkj = new int[L][K][J];
        this.N_lwk = new int[L][W][K];
        this.N_lk = new int[L][K];
    }

    private static int compare(int i, int j) {
        return i - j;
    }

    public void initialize() throws IOException {
        final int[] f_w = new int[W];
        int sumN = 0;
        int Nj = 0;
        int j = 0;
        while (buf.hasNext()) {
            int w = buf.getNext();
            int k = buf.getNext();
            if (w != 0) {
                f_w[w - 1]++;
                sumN++;
                Nj++;
            } else {
                N_jl[j / L][j % L] = Nj;
                Nj = 0;
                j++;
            }
        }
        buf.reset();
        final Integer[] i_w = new Integer[W];
        int i2 = 0;
        while (i2 < W) {
            i_w[i2] = i2++;
        }
        Arrays.sort(i_w, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int c = CPLSATrain.compare(f_w[o1], f_w[o2]);
                if (c == 0) {
                    return CPLSATrain.compare(o1, o2);
                } else {
                    return -c;
                }
            }
        });
        final int bucketSize = sumN / K;
        final int[] w2k = new int[W];
        int kappa = 0;
        sumN = 0;
        for (int w : i_w) {
            w2k[w] = kappa;
            sumN += f_w[w];
            if (sumN >= (kappa + 1) * bucketSize) {
                kappa++;
                if (kappa == K) {
                    break;
                }
            }
        }

        j = 0;
        while (buf.hasNext()) {
            final int w = buf.getNext() - 1;
            buf.getNext();
            if (w == -1) {
                j++;
            } else {
                final int k = w2k[w];
                buf.update(k);
                N_lk[j % L][k]++;
                N_lkj[j % L][k][j / L]++;
                N_lwk[j % L][w][k]++;
            }
        }
        buf.reset();
        for (int l = 0; l < L; l++) {
            maximization(l);
        }
        initialized = true;
    }

    public void solve(int iterations, double epsilon) throws IOException {
        solve(iterations, epsilon, false);
    }

    public void solve(int iterations, double epsilon, boolean verbose) throws IOException {
        if (!initialized) {
            if (verbose) {
                System.err.println("Initializing");
            }
            initialize();
        }
        if (verbose) {
            System.err.print("Iterating");
        }
        for (int i = 0; i < iterations; i++) {
            if (verbose) {
                System.err.print(".");
            }
            final double lambda = dual(epsilon);

            expectation(lambda);

            for (int l = 0; l < L; l++) {
                maximization(l);
            }
        }

        if (verbose) {
            System.err.println();
        }
    }
    public static final int DUAL_MAX_ITERS = 1000;

    private double dual(double epsilon) throws IOException {
        double[][] p_jl = new double[J][L];
        double[][] pi_jl = new double[J][L];

        buf.reset();
        int j2 = 0;
        p_jl[0][0] = 1.0;
        while (buf.hasNext()) {
            int w = buf.getNext() - 1;
            int k = buf.getNext();
            final int l = j2 % L;
            final int j = j2 / L;
            if (w == -1) {
                j2++;
                if (j2 / L < J) {
                    p_jl[j2 / L][j2 % L] = 1.0;
                }
            } else {
                p_jl[j][l] *= phi_lwk[l][w][k];
                for (int k2 = 0; k2 < K; k2++) {
                    if (l == 0) {
                        pi_jl[j][l] += (double) N_lkj[0][k2][j] / N_lk[0][k2] - (double) N_lkj[1][k2][j] / N_lk[1][k2];
                    } else {
                        pi_jl[j][l] -= (double) N_lkj[0][k2][j] / N_lk[0][k2] - (double) N_lkj[1][k2][j] / N_lk[1][k2];
                    }
                }
            }
        }

        double lambda = 1;

        double value = dZetaOverZeta(lambda, p_jl, pi_jl);
        System.err.println(value);

        int iters = 0;

        while (Math.abs(value) > epsilon) {
            final double gradient = ddZetaOverZeta(lambda, p_jl, pi_jl);
            if (lambda <= 0.0 && value / gradient > 0) {
                return 0.0;
            } else if (gradient == 0) {
                System.err.println("Dual problem failed on local maximum");
                return lambda;
            } else {
                lambda -= value / gradient;
                value = dZetaOverZeta(lambda, p_jl, pi_jl);
            }
            if (iters++ > DUAL_MAX_ITERS) {
                System.err.println("Dual problem exceeded iteration limit");
                break;
            }
        }
        return lambda;
    }

    // Z'(lambda) / Z(lambda)
    private double dZetaOverZeta(double lambda, double[][] p_jl, double[][] pi_jl) {
        double zeta = 0.0;
        double dzeta = 0.0;

        for (int j = 0; j < J; j++) {
            for (int l = 0; l < L; l++) {
                final double z = p_jl[j][l] * Math.exp(-lambda * pi_jl[j][l]);
                zeta += z;
                dzeta += -pi_jl[j][l] * z;
            }
        }
        return dzeta / zeta;
    }

    // d/dlambda Z'(lambda) / Z(lambda) = [ Z''(lambda) * Z(lambda) * Z'(lambda) ^ 2] / Z(lambda) ^ 2
    private double ddZetaOverZeta(double lambda, double[][] p_jl, double[][] pi_jl) {
        double zeta = 0.0;
        double dzeta = 0.0;
        double ddzeta = 0.0;

        for (int j = 0; j < J; j++) {
            for (int l = 0; l < L; l++) {
                final double z = p_jl[j][l] * Math.exp(-lambda * pi_jl[j][l]);
                zeta += z;
                dzeta += -pi_jl[j][l] * z;
                ddzeta += pi_jl[j][l] * pi_jl[j][l] * z;
            }
        }

        return (ddzeta * zeta + dzeta * dzeta) / zeta / zeta;
    }
    private final Random random = new Random();

    private void expectation(double lambda) throws IOException {
        System.err.println(lambda);
        double[][][] pi_jlk = new double[J][L][K];

        for (int j = 0; j < J; j++) {
            for (int l = 0; l < L; l++) {
                for (int k = 0; k < K; k++) {
                    if (l == 0) {
                        pi_jlk[j][l][k] = (double) N_lkj[0][k][j] / N_lk[0][k] - (double) N_lkj[1][k][j] / N_lk[1][k];
                    } else {
                        pi_jlk[j][l][k] = (double) N_lkj[1][k][j] / N_lk[1][k] - (double) N_lkj[0][k][j] / N_lk[0][k];
                    }
                }
            }
        }

        int iterDelta = 0;
        
        buf.reset();
        int j2 = 0;
        while (buf.hasNext()) {
            int w = buf.getNext() - 1;
            final int oldK = buf.getNext();
            int j = j2 / L;
            int l = j2 % L;
            if (w == -1) {
                j2++;
            } else {
                int k = sample(w, j, l, lambda, pi_jlk);
                assignZ(j, l, w, k, oldK);
                if(k != oldK) {
                    iterDelta++;
                }
                buf.update(k);
            }

        }
        System.err.println(iterDelta);
    }

    private int sample(int w, int j, int l, double lambda, double[][][] pi_jlk) {
//        double u = random.nextDouble();
//        double[] P = new double[K + 1];
//        double sum = 0.0;
//        for (int k = 0; k < K; k++) {
//            P[k] = phi_lwk[l][w][k] * theta_lkj[l][k][j]
//                    * Math.exp(-lambda * pi[j][l][k]);
//            assert (P[k] >= 0);
//            sum += P[k];
//        }
//        for (int k = 0; k < K; k++) {
//            if (u < (P[k] / sum)) {
//                return k;
//            }
//            P[k + 1] += P[k];
//        }
//        throw new RuntimeException("P[K] = " + P[K] + " sum= " + sum);
        double bestP = Double.NEGATIVE_INFINITY;
        int bestK = -1;
        int ties = 0;
        for (int k = 0; k < K; k++) {
            double p = phi_lwk[l][w][k] * theta_lkj[l][k][j]
                    * Math.exp(-lambda * pi_jlk[j][l][k]);
            if(p > bestP || (p == bestP && (random.nextInt() % (++ties) == 0))) {
                bestP = p;
                bestK = k;
            }
        }
        return bestK;
        
    }

    private void assignZ(int j, int l, int w, int newK, int oldK) {
        if (newK == oldK) {
            return;
        }
        N_lk[l][oldK]--;
        N_lk[l][newK]++;
        N_lkj[l][oldK][j]--;
        N_lkj[l][newK][j]++;
        N_lwk[l][w][oldK]--;
        N_lwk[l][w][newK]++;
    }

    private void maximization(int l) {
        for (int k = 0; k < K; k++) {
            for (int j = 0; j < J; j++) {
                theta_lkj[l][k][j] = ((double) N_lkj[l][k][j] + alpha) / ((double) N_jl[j][l] + alpha * K);
            }
        }
        for (int w = 0; w < W; w++) {
            for (int k = 0; k < K; k++) {
                phi_lwk[l][w][k] = ((double) N_lwk[l][w][k] + beta) / ((double) N_lk[l][k] + beta * W);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File corpus = opts.roFile("corpus[.gz|bz2]", "The corpus file");
        double alpha = opts.doubleValue("alpha", -1, "The alpha parameter");
        final double beta = opts.doubleValue("beta", 0.01, "The beta parameter");

        final int W = opts.intValue("W", "The number of distinct tokens");
        final int J = opts.intValue("J", "The number of documents (per language)");
        final int K = opts.intValue("K", "The number of topics");
        final int N = opts.intValue("N", "The number of iterations to perform");

        final File outFile = opts.woFile("output", "The file to write the SVD to");

        if (!opts.verify(CPLSATrain.class)) {
            return;
        }
        if (alpha == -1.0) {
            alpha = 2.0 / K;
        }
        if (alpha < 0 || beta < 0) {
            throw new IllegalArgumentException("Alpha and beta cannot be negative");
        }
        System.err.println("Preparing corpus");
        final CPLSATrain train = new CPLSATrain(corpus, J, W, K, alpha, beta);
        train.solve(N, 1e-12, true);
        System.err.println("Writing model");
        train.writeModel(CLIOpts.openOutputAsMaybeZipped(outFile));
    }

    private void writeModel(OutputStream outFile) throws IOException {
        final DataOutputStream out = new DataOutputStream(outFile);
        out.writeInt(2);
        out.writeInt(J);
        out.writeInt(W);
        out.writeInt(K);
        out.writeDouble(alpha);
        out.writeDouble(beta);
        for (int l = 0; l < 2; l++) {
            for (int k = 0; k < K; k++) {
                for (int w = 0; w < W; w++) {
                    out.writeInt(N_lwk[l][w][k]);
                }
            }
        }
        for (int l = 0; l < 2; l++) {
            for (int k = 0; k < K; k++) {
                out.writeInt(N_lk[l][k]);
            }
        }
        out.flush();
        out.close();
    }
}
