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
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.TridiagonalMatrix;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.translation.topics.CLIOpts;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * An implementation of SVD for super-large sparse matrices. For smaller
 * problems use a standard implementation (Apache Commons Math)
 *
 * @author John McCrae
 */
public class SingularValueDecomposition {

    private static final void toR(TridiagonalMatrix tm) {
        System.out.print("tridiag(c(");
        for (int i = 0; i < tm.alpha().length; i++) {
            System.out.print(tm.alpha()[i]);
            if (i + 1 != tm.alpha().length) {
                System.out.print(",");
            }
        }
        System.out.print("),c(");
        for (int i = 0; i < tm.beta().length; i++) {
            System.out.print(tm.beta()[i]);
            if (i + 1 != tm.beta().length) {
                System.out.print(",");
            }
        }
        System.out.println("))");
    }

    private static final void toR(double[][] m) {
        System.out.print("matrix(c(");
        for (int i = 0; i < m[0].length; i++) {
            for (int j = 0; j < m.length; j++) {
                System.out.print(m[j][i]);
                if (i + 1 != m[0].length || j + 1 != m.length) {
                    System.out.print(",");
                }
            }
        }
        System.out.println(")," + m.length + ")");
    }

    public Solution calculate(File matrixFile, int W, int J, int K, double epsilon) {
        final LanczosAlgorithm.Solution oLanczos = LanczosAlgorithm.lanczos(new OuterProductMultiplication(matrixFile, W), randomUnitNormVector(J), K, true);
        toR(oLanczos.tridiagonal());
        toR(oLanczos.q());
        final QRAlgorithm.Solution oQrSolve = QRAlgorithm.qrSolve(epsilon, oLanczos.tridiagonal(), null);
        System.out.println(oQrSolve.givensSeq().toRString(K));
        final double[][] oEigens = transpose(oQrSolve.givensSeq().applyTo(oLanczos.q()));
        toR(oEigens);
        final int[] oOrder = order(oQrSolve.values());

        final LanczosAlgorithm.Solution iLanczos = LanczosAlgorithm.lanczos(new InnerProductMultiplication(matrixFile, J), randomUnitNormVector(W), K, true);
        toR(iLanczos.tridiagonal());
        toR(iLanczos.q());
        final QRAlgorithm.Solution iQrSolve = QRAlgorithm.qrSolve(epsilon, iLanczos.tridiagonal(), null);
        System.out.println(iQrSolve.givensSeq().toRString(J));
        final double[][] iEigens = transpose(iQrSolve.givensSeq().applyTo(iLanczos.q()));
        toR(iEigens);
        final int[] iOrder = order(iQrSolve.values());

        final double[][] V = new double[K][];
        final double[][] U = new double[K][J];
        final double[] S = new double[K];

        for (int i = 0; i < K; i++) {
            S[i] = Math.sqrt(Math.abs((oLanczos.tridiagonal().alpha()[oOrder[i]] + iLanczos.tridiagonal().alpha()[iOrder[i]]) / 2.0));
            U[i] = oEigens[oOrder[i]];
            V[i] = iEigens[iOrder[i]];
        }

        return new Solution(transpose(U), V, S);
    }
    private static final Random r = new Random();

    protected Vector<Double> randomUnitNormVector(int J) {
        final double[] rv = new double[J];
        double norm = 0.0;
        for (int j = 0; j < J; j++) {
            rv[j] = r.nextDouble();
            norm += rv[j] * rv[j];
        }

        norm = Math.sqrt(norm);

        for (int j = 0; j < J; j++) {
            rv[j] /= norm;
        }

        return new RealVector(rv);
    }

    private static int[] order(final double[] d) {
        final Integer[] order = new Integer[d.length];
        for (int i = 0; i < d.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return -Double.compare(d[o1], d[o2]);
            }
        });
        final int[] order2 = new int[d.length];
        for (int i = 0; i < d.length; i++) {
            order2[i] = order[i];
        }
        return order2;
    }

    private static double[][] transpose(double[][] M) {
        final double[][] N = new double[M[0].length][M.length];
        for (int i = 0; i < M.length; i++) {
            for (int j = 0; j < M[i].length; j++) {
                N[j][i] = M[i][j];
            }
        }
        return N;
    }

    public static class OuterProductMultiplication implements VectorFunction<Double> {

        private final File matrixFile;
        private final int W;

        public OuterProductMultiplication(File matrixFile, int W) {
            this.matrixFile = matrixFile;
            this.W = W;
        }

        @Override
        public Vector<Double> apply(Vector<Double> v) {
            try {
                double[] mid = new double[W];
                int n = 0;
                DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(matrixFile));
                while (data.available() > 0) {
                    try {
                        int i = data.readInt();
                        if (i != 0) {
                            mid[i - 1] += v.doubleValue(n);
                        } else {
                            n++;
                        }
                    } catch (EOFException x) {
                        break;
                    }
                }
                data.close();
                n = 0;
                double[] a = new double[v.length()];
                data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(matrixFile));
                while (data.available() > 0) {
                    try {
                        int i = data.readInt();
                        if (i != 0) {
                            a[n] += mid[i - 1];
                        } else {
                            n++;
                        }
                    } catch (EOFException x) {
                        break;
                    }
                }
                data.close();
                return new RealVector(a);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }

        }
    }

    public static class InnerProductMultiplication implements VectorFunction<Double> {

        private final File matrixFile;
        private final int J;

        public InnerProductMultiplication(File matrixFile, int J) {
            this.matrixFile = matrixFile;
            this.J = J;
        }

        @Override
        public Vector<Double> apply(Vector<Double> v) {
            try {
                double[] mid = new double[J];
                int n = 0;
                DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(matrixFile));
                while (data.available() > 0) {
                    try {
                        int i = data.readInt();
                        if (i != 0) {
                            mid[n] += v.doubleValue(i - 1);
                        } else {
                            n++;
                        }
                    } catch (EOFException x) {
                        break;
                    }
                }
                data.close();
                n = 0;
                double[] a = new double[v.length()];
                data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(matrixFile));
                while (data.available() > 0) {
                    try {
                        int i = data.readInt();
                        if (i != 0) {
                            a[i - 1] += mid[n];
                        } else {
                            n++;
                        }
                    } catch (EOFException x) {
                        break;
                    }
                }
                data.close();
                return new RealVector(a);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }

        }
    }

    public static class Solution {

        public final double[][] U, V;
        public final double[] S;

        public Solution(double[][] U, double[][] V, double[] S) {
            this.U = U;
            this.V = V;
            this.S = S;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + Arrays.deepHashCode(this.U);
            hash = 59 * hash + Arrays.deepHashCode(this.V);
            hash = 59 * hash + Arrays.hashCode(this.S);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Solution other = (Solution) obj;
            if (!Arrays.deepEquals(this.U, other.U)) {
                return false;
            }
            if (!Arrays.deepEquals(this.V, other.V)) {
                return false;
            }
            if (!Arrays.equals(this.S, other.S)) {
                return false;
            }
            return true;
        }
    }
}
