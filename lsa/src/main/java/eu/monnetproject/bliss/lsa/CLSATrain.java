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
package eu.monnetproject.bliss.lsa;

import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition.Solution;
import eu.monnetproject.bliss.CLIOpts;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author John McCrae
 */
public class CLSATrain {

    public static void main(String[] args) throws Exception {

        final CLIOpts opts = new CLIOpts(args);

        final File corpus = opts.roFile("corpus[.gz|bz2]", "The corpus file");
        final int W = opts.intValue("W", "The number of distinct tokens");
        final int J = opts.intValue("J", "The number of documents (per language)");
        final int K = opts.intValue("K", "The number of topics");

        final File outFile = opts.woFile("output", "The file to write the SVD to");

        if (!opts.verify(LSATrain.class)) {
            return;
        }
        System.err.print("Solving lang 1 SVD");
        final Solution svd1 = SingularValueDecomposition.calculate(new CLSAIterable(corpus, 0,2), W, J, K, 1e-50);
        System.err.print("\nSolving lang 2 SVD");
        final Solution svd2 = SingularValueDecomposition.calculate(new CLSAIterable(corpus, 1,2), W, J, K, 1e-50);
        System.err.println("\nCalculating conjugate");
        final double[][] C = calcConjugate(svd1.U,svd2.U,K);
        System.err.println("Writing model");
        writeModel(outFile,svd1,svd2,C,W);
    }

    private static double[][] calcConjugate(double[][] Vx, double[][] Vy, int K) {
        double[][] C_kk = new double[K][K];
        for(int k1 = 0; k1 < K; k1++) {
            for(int k2 = 0; k2 <K; k2++) {
                for(int j = 0; j < Vx[k1].length; j++) {
                    C_kk[k1][k2] += Vx[k1][j] * Vy[k2][j];
                }
            }
        }
        return C_kk;
    }

    private static void writeModel(File outFile, Solution svd1, Solution svd2, double[][] C, int W) throws IOException {
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(outFile));
        out.writeInt(2);
        out.writeInt(W);
        final int K = svd1.U.length;
        out.writeInt(K);
        for(int k = 0; k < K; k++) {
            for(int w = 0; w < W; w++) {
                out.writeDouble(svd1.V[k][w] / svd1.S[k]);
            }
        }
        for(int k = 0; k < K; k++) {
            for(int w = 0; w < W; w++) {
                out.writeDouble(svd2.V[k][w] / svd2.S[k]);
            }
        }
        for(int k1 = 0; k1 < K; k1++) {
            for(int k2 = 0; k2 < K; k2++) {
                out.writeDouble(C[k1][k2]);
            }
        }
        out.flush();
        out.close();
    }


    private static class CLSAIterable implements IntIterable {

        private final File corpus;
        private final int l, L;

        public CLSAIterable(File corpus, int l, int L) {
            this.corpus = corpus;
            this.l = l;
            this.L = L;
        }


        @Override
        public IntIterator iterator() {
            try {
                System.err.print(".");
                return new CLSAIterator(new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpus)), l, L);
            } catch(IOException x) {
                throw new RuntimeException(x);
            }
        }

        private static class CLSAIterator implements IntIterator {

            private final DataInputStream dis;
            private int next;
            private int l, L;
            private boolean hasNext = true;

            public CLSAIterator(DataInputStream dis, int l, int L) {
                this.dis = dis;
                this.l = l;
                this.L = L;
                try {
                    while (l % L != 0) {
                        int n = dis.readInt();
                        if (n == 0) {
                            l++;
                        }
                    }
                } catch (EOFException x) {
                    hasNext = false;
                    return;
                } catch (IOException x) {
                    throw new RuntimeException(x);
                }
                advance();
            }

            @Override
            public int nextInt() {
                int n = next;
                advance();
                return n;
            }

            @Override
            public int skip(int n) {
                int i = 0;
                while (i < n && advance()) {
                    i++;
                }
                return i;
            }

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Integer next() {
                return nextInt();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported.");
            }

            private boolean advance() {
                try {
                    if (dis.available() == 0) {
                        hasNext = false;
                        return false;
                    }
                    next = dis.readInt();
                    if (next == 0) {
                        l++;
                        while (l % L != 0) {
                            int n = dis.readInt();
                            if (n == 0) {
                                l++;
                            }
                        }
                        return true;
                    } else {
                        return true;
                    }
                } catch (EOFException x) {
                    hasNext = false;
                    return false;
                } catch (IOException x) {
                    throw new RuntimeException();
                }
            }
        }
    }
}
