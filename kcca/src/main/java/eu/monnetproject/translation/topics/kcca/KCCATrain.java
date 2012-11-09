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
package eu.monnetproject.translation.topics.kcca;

import eu.monnetproject.math.sparse.DoubleArrayMatrix;
import eu.monnetproject.math.sparse.Matrix;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.math.sparse.eigen.LanczosAlgorithm;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition.Solution;
import eu.monnetproject.translation.topics.CLIOpts;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Train a model using Kernel Canonical Correlation Analysis as described in
 * Vinokourov, Shawe-Taylor and Cristianini, "Inferring a Semantic Representation
 * of Text via Cross-Language Correlation Analysis"
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
        System.err.print("Lanczos step");
        final LanczosAlgorithm.Solution lancozSoln = LanczosAlgorithm.lanczos(new D(corpus, J, W, kappa), LanczosAlgorithm.randomUnit(2*J));
        System.err.print("\nArnoldi step");
        final SingularValueDecomposition svd = new SingularValueDecomposition();
        final Solution eigen1 = svd.nonsymmEigen(new B(corpus, J, W, lancozSoln), W, K, 1e-50);
        System.err.println("\nCalculating final vectors");
        return apply(corpus, W, J, K, eigen1.U);
    }

    private static double[][][] apply(File corpus, int W, int J, int K, double[][] Z) throws IOException {
        double[][][] Z2 = new double[2][W][K];
        final DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpus));
        int N = 0;
        while (data.available() > 0) {
            try {
                int i = data.readInt();
                final int j = (N % 2) * J + N / 2;
                if (i == 0) {
                    N++;
                } else {
                    for (int k = 0; k < K; k++) {
                        Z2[N % 2][i - 1][k] += Z[j][k];
                    }
                }
            } catch (EOFException x) {
                break;
            }
        }
        return Z2;
    }

    public static class B implements VectorFunction<Double, Double> {

        private final File corpus;
        private final int J;
        private final int W;
        private final LanczosAlgorithm.Solution lanczosSoln;
        private final Matrix<Double> Q;

        public B(File corpus, int J, int W, LanczosAlgorithm.Solution lanczosSoln) {
            this.corpus = corpus;
            this.J = J;
            this.W = W;
            this.lanczosSoln = lanczosSoln;
            this.Q = new DoubleArrayMatrix(lanczosSoln.q());
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
                final Vector<Double> v3 = Q.mult(v2);
                final Vector<Double> v4 = lanczosSoln.tridiagonal().invMult(v3);
                return Q.multTransposed(v4);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
    }

    public static class D implements VectorFunction<Double, Double> {

        private final File corpus;
        private final int J;
        private final int W;
        private final double kappa;

        public D(File corpus, int J, int W, double kappa) {
            this.corpus = corpus;
            this.J = J;
            this.W = W;
            this.kappa = kappa;
        }

        @Override
        public Vector<Double> apply(Vector<Double> v) {
            try {
                System.err.print(".");
                final Vector<Double> v1 = KxKyv(v);
                final Vector<Double> v2 = KxKyv(v1);
                v1.multiply(kappa);
                v2.multiply(1.0 - kappa);
                v1.add(v2);
                return v1;
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }

        public Vector<Double> KxKyv(Vector<Double> v) throws IOException {
            return calcKxKyv(v, J, W, corpus);
        }
    };

    /**
     * Calculate:
     *  ( K_x  0  ) ( v_x )
     *  (  0  K_y ) ( v_y )
     * @param v The vector
     * @param J The dimensions of the matrix
     * @param W The number of words (middle dimension)
     * @param corpus The corpus as binary integerized form
     * @return The calculated vector
     */
    private static Vector<Double> calcKxKyv(Vector<Double> v, int J, int W, File corpus) throws IOException {
        assert(v.length() == 2*J);
        double[] v1 = new double[2 * J];
        double[] mid = new double[2 * W];
        {
            final DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpus));
            int N = 0;
            while (data.available() > 0) {
                try {
                    int i = data.readInt();
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
        final double kappa = opts.doubleValue("kappa", 1.5, "The kappa value");
        final File corpus = opts.roFile("corpus[.gz|.bz2]", "The training corpus");
        final int W = opts.intValue("W", "The number of distinct tokens in the corpus");
        final int J = opts.intValue("J", "The number of documents (per language)");
        final int K = opts.intValue("K", "The number of topics to use");
        final File outFile = opts.woFile("model", "The file to save the model to");
        
        if(!opts.verify(KCCATrain.class)) {
            return;
        }
        final double[][][] model = train(corpus, W, J, K, kappa);
        System.err.println("Writing model");
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(outFile));
        out.writeInt(2);
        out.writeInt(W);
        out.writeInt(K);
        for(int l = 0; l < 2; l++) {
            for(int w = 0; w < W; w++) {
                for(int k = 0; k < K; k++) {
                    out.writeDouble(model[l][w][k]);
                }
            }
        }
        out.flush();
        out.close();
    }
}
