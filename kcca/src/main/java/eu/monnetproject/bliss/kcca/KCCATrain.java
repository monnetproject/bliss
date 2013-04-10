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
package eu.monnetproject.bliss.kcca;

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition.Solution;
import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.ParallelBinarizedReader;
import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.eigen.CholeskyDecomposition;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Train a model using Kernel Canonical Correlation Analysis as described in
 * Vinokourov, Shawe-Taylor and Cristianini, "Inferring a Semantic
 * Representation of Text via Cross-Language Correlation Analysis"
 *
 * @author John McCrae
 */
public class KCCATrain {

    // Take WxJ corpus TF matrices C_x, C_y
    // Let K_x = C_x^T C_x
    //     K_y = C_y^T C_y
    // Let D = ( (1-k)K_xK_x + k.K_x             0         )
    //       = (          0            (1-k)K_yK_y + k.K_y )
    //     
    //     B = (   0    K_xK_y )
    //       = ( K_yK_x    0   )
    //
    // Solve Ba = pDa
    //
    // First use Lanczos to find
    //    D = Q^T T Q
    // We can solve as eigen value problem by using
    //  If Ba = pDa => 
    //    Q T^-1 Q^T B a = p a
    // We do not calculate T^-1 but calculate the inverse vector as required
    public static double[][][] train(File corpus, int W, int J, int K, double kappa) throws IOException {
        System.err.print("Calculating D");
        final DSoln D = calculateD(corpus, W, J, kappa);
        System.err.print("\nCholesky step (Source language)");
        final double[][] Dxi = CholeskyDecomposition.denseDecomp(D.Dx);
        System.err.print("\nCholesky step (Target language)");
        final double[][] Dyi = CholeskyDecomposition.denseDecomp(D.Dy);
        System.err.print("\nArnoldi step");
        final Solution eigen1 = SingularValueDecomposition.nonsymmEigen(new B(corpus, J, W, Dxi, Dyi), 2 * J, 2 * K, 1e-50);
        System.err.println("Calculating final vectors");
        final FPSoln pos = filterPostives(eigen1.S);
        return apply(corpus, W, J, K, eigen1.U, pos.posKs, pos.K2,eigen1.S);
    }

    private static class FPSoln {

        int K2;
        int[] posKs;

        public FPSoln(int K2, int[] posKs) {
            this.K2 = K2;
            this.posKs = posKs;
        }
    }

    private static FPSoln filterPostives(double[] v) {
        final IntArrayList pos = new IntArrayList(v.length);
        for (int i = 0; i < v.length; i++) {
            if (v[i] > 0) {
                pos.add(i);
            }
        }
        return new FPSoln(pos.size(), pos.toIntArray());
    }

    private static double[][][] apply(File corpus, int W, int J, int K, double[][] Z, int[] posK, int k2, double[] S) throws IOException {
        double[][][] Z2 = new double[2][W][k2];
        final DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpus));
        int N = 0;
        while (data.available() > 0) {
            try {
                int i = data.readInt();
                if (N / 2 >= J) {
                    break;
                }
                final int j = N / 2;
                if (i == 0) {
                    N++;
                } else {
                    for (int k = 0; k < posK.length; k++) {
                        if (N % 2 == 1) {
                            Z2[1][i - 1][k] += Z[posK[k]][j + J] / S[posK[k]];
                        } else {
                            Z2[0][i - 1][k] += Z[posK[k]][j] / S[posK[k]];
                        }
                    }
                }
            } catch (EOFException x) {
                break;
            }
        }
        return Z2;
    }

    private static class DSoln {

        double[][] Dx;
        double[][] Dy;

        public DSoln(double[][] Dx, double[][] Dy) {
            this.Dx = Dx;
            this.Dy = Dy;
        }
    }

    private static DSoln calculateD(File corpus, int W, int J, double kappa) throws IOException {
        final ParallelBinarizedReader slowIn = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));
        SparseIntArray[] slow;
        int j1 = 0;
        final double[][] Dx = new double[J][J];
        final double[][] Dy = new double[J][J];
        final double[][] Kx = new double[J][J];
        final double[][] Ky = new double[J][J];
        while ((slow = slowIn.nextFreqPair(W)) != null && j1 < J) {
            final ParallelBinarizedReader fastIn = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));
            SparseIntArray[] fast;
            int j2 = 0;
            while ((fast = fastIn.nextFreqPair(W)) != null && j2 < J) {
                Kx[j1][j2] = slow[0].innerProduct(fast[0]);
                Ky[j1][j2] = slow[1].innerProduct(fast[1]);
                j2++;
            }
            j1++;
            System.err.print(".");
        }
        System.err.println();
        for (j1 = 0; j1 < J; j1++) {
            for (int j2 = 0; j2 < J; j2++) {
                for (int j = 0; j < J; j++) {
                    Dx[j1][j2] += (1.0 - kappa) * Kx[j1][j] * Kx[j][j2];
                    Dy[j1][j2] += (1.0 - kappa) * Ky[j1][j] * Ky[j][j2];
                }
                Dx[j1][j2] += kappa * Kx[j1][j2];
                Dy[j1][j2] += kappa * Ky[j1][j2];
            }
            System.err.print(".");
        }

        return new DSoln(Dx, Dy);
    }

    public static class B implements VectorFunction<Double, Double> {

        private final File corpus;
        private final int J;
        private final int W;
        private final SparseMatrix<Double> Dxl, Dyl;

        public B(File corpus, int J, int W, double[][] Dxl, double[][] Dyl) {
            this.corpus = corpus;
            this.J = J;
            this.W = W;
            this.Dxl = SparseMatrix.fromArray(Dxl);
            this.Dyl = SparseMatrix.fromArray(Dyl);
        }

        @Override
        public Vector<Double> apply(Vector<Double> v) {
            try {
                System.err.print(".");
                final Vector<Double> v1 = calcKxKyv(v, J, W, corpus);
                // As B = ( K_x  0  ) (  0  K_y )
                //      = (  0  K_y ) ( K_x  0  )
                // We need only calculate diag(Kx,Ky)v and switch the vector after first iteration
                for (int j = 0; j < J; j++) {
                    double t = v1.doubleValue(j);
                    v1.put(j, v1.doubleValue(j + J));
                    v1.put(j + J, t);
                }
                final Vector<Double> v2 = calcKxKyv(v1, J, W, corpus);
                final Vector<Double> v7 = new SparseRealArray(2 * J);
                {
                    final Vector<Double> v3 = new ShiftedVector<Double>(0, J, v2);
                    final Vector<Double> v4 = CholeskyDecomposition.solve(Dxl, v3);
                    final Vector<Double> v5 = CholeskyDecomposition.solveT(Dxl, v4);
                    for (int j = 0; j < J; j++) {
                        v7.add(j, v5.doubleValue(j));
                    }
                }
                {
                    final Vector<Double> v3 = new ShiftedVector<Double>(J, J, v2);
                    final Vector<Double> v4 = CholeskyDecomposition.solve(Dyl, v3);
                    final Vector<Double> v6 = CholeskyDecomposition.solveT(Dyl, v4);
                    for (int j = 0; j < J; j++) {
                        v7.add(J + j, v6.doubleValue(j));
                    }
                }
                return v7;
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
    }

    /**
     * Calculate: ( K_x 0 ) ( v_x ) ( 0 K_y ) ( v_y )
     *
     * @param v The vector
     * @param J The dimensions of the matrix
     * @param W The number of words (middle dimension)
     * @param corpus The corpus as binary integerized form
     * @return The calculated vector
     */
    private static Vector<Double> calcKxKyv(Vector<Double> v, int J, int W, File corpus) throws IOException {
        assert (v.length() == 2 * J);
        double[] v1 = new double[2 * J];
        double[] mid = new double[2 * W];
        {
            final DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpus));
            int N = 0;
            while (data.available() > 0) {
                try {
                    int i = data.readInt();
                    if (N / 2 >= J) {
                        break;
                    }
                    final int j = (N % 2) * J + N / 2;
                    if (i == 0) {
                        N++;
                    } else {
                        mid[(N % 2) * W + i - 1] += v.doubleValue(j);
                    }
                } catch (EOFException x) {
                    break;
                }
            }
        }
        final RealVector m = new RealVector(mid);
        {
            final DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpus));
            int N = 0;
            final SparseIntArray doc = new SparseIntArray(2 * W);
            while (data.available() > 0) {
                try {
                    int i = data.readInt();
                    if (N / 2 >= J) {
                        break;
                    }
                    final int j = (N % 2) * J + N / 2;
                    if (i == 0) {
                        v1[j] = doc.innerProduct(m);
                        doc.clear();
                        N++;
                    } else {
                        doc.inc(i + (N % 2) * W - 1);
                    }
                } catch (EOFException x) {
                    break;
                }
            }
        }
        return new RealVector(v1);
    }

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final double kappa = opts.doubleValue("kappa", 0.67, "The kappa value");
        final File corpus = opts.roFile("corpus[.gz|.bz2]", "The training corpus");
        final int W = opts.intValue("W", "The number of distinct tokens in the corpus");
        final int J = opts.intValue("J", "The number of documents (per language)");
        final int K = opts.intValue("K", "The number of topics to use");
        final File outFile = opts.woFile("model", "The file to save the model to");

        if (!opts.verify(KCCATrain.class)) {
            return;
        }
        final double[][][] model = train(corpus, W, J, K, kappa);
        final int K2 = model[0][0].length;
        System.err.println("Writing model");
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(outFile));
        out.writeInt(2);
        out.writeInt(W);
        out.writeInt(K2);
        for (int l = 0; l < 2; l++) {
            for (int w = 0; w < W; w++) {
                for (int k = 0; k < K2; k++) {
                    out.writeDouble(model[l][w][k]);
                }
            }
        }
        out.flush();
        out.close();
    }
}
