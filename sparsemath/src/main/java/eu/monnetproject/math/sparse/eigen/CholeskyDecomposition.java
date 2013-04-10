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

import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.Vectors;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class CholeskyDecomposition {

    private CholeskyDecomposition() {
    }

    public static double[][] denseDecomp(double[][] a) {
        int m = a.length;
        double[][] l = new double[m][m]; //automatically initialzed to 0's
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < (i + 1); k++) {
                double sum = 0;
                for (int j = 0; j < k; j++) {
                    sum += l[i][j] * l[k][j];
                }
                if (i == k && a[i][i] - sum < 0) {
                    for (int j = 0; j < m; j++) {
                        System.out.println(Arrays.toString(a[j]));
                    }
                    throw new IllegalArgumentException("Matrix not positive definite");
                }
                l[i][k] = (i == k) ? Math.sqrt(a[i][i] - sum)
                        : (a[i][k] - sum) / l[k][k];
            }
        }
        return l;
    }

    public static void denseInplaceDecomp(double[][] a) {
        int n = a.length;
        int i, j, k;
        double sum;
        double[] diagonal = new double[n];

        for (i = 0; i < n; i++) {
            System.err.print(".");
            for (j = i; j < n; j++) {

                for (sum = a[i][j], k = i - 1; k >= 0; k--) {
                    sum -= a[i][k] * a[j][k];
                }
                if (i == j) {
                    if (sum <= 0.0) {
                        throw new IllegalArgumentException("Matrix not positive definite");
                    }
                    diagonal[i] = Math.sqrt(sum);
                } else {
                    a[j][i] = sum / diagonal[i];
                    a[i][j] = 0;
                }

            }
        }


        for (i = 0; i < n; i++) {
            a[i][i] = diagonal[i];
        }


    }

    public static void denseOnDiskDecomp(File matrixFile, int n) {
        int i, j, k;
        double sum;
        double[] diagonal = new double[n];
        try {
            final FileChannel channel = new RandomAccessFile(matrixFile, "rw").getChannel();

            for (i = 0; i < n; i++) {
                final MappedByteBuffer row_i = channel.map(FileChannel.MapMode.READ_WRITE, (long) n * i * 8, n * 8);
                if (i % 10 == 9) {
                    System.err.print(".");
                }
                for (j = i; j < n; j++) {
                    final MappedByteBuffer row_j = channel.map(FileChannel.MapMode.READ_WRITE, (long) n * j * 8, n * 8);

                    row_i.position(j * 8);
                    sum = row_i.getDouble();
                    for (k = i - 1; k >= 0; k--) {
                        //sum -= a.doubleValue(i, k) * a.doubleValue(j, k);
                        row_i.position(k * 8);
                        row_j.position(k * 8);
                        sum -= row_i.getDouble() * row_j.getDouble();
                    }
                    if (i == j) {
                        if (sum <= 0.0) {
                            throw new IllegalArgumentException("Matrix not positive definite");
                        }
                        diagonal[i] = Math.sqrt(sum);
                    } else {
                        row_i.position(j * 8);
                        row_i.putDouble(sum / diagonal[i]);
                        row_j.position(i * 8);
                        row_j.putDouble(sum / diagonal[i]);
                    }

                }
            }


            for (i = 0; i < n; i++) {
                channel.map(FileChannel.MapMode.READ_WRITE, (long) ((long) i + (long) n * i) * 8, 8).putDouble(diagonal[i]);
            }
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    public static void denseInplaceDecomp(SparseMatrix<Double> a) {
        int n = a.rows();
        int i, j, k;
        double sum;
        double[] diagonal = new double[n];

        for (i = 0; i < n; i++) {
            System.err.print(".");
            for (j = i; j < n; j++) {

                for (sum = a.doubleValue(i, j), k = i - 1; k >= 0; k--) {
                    sum -= a.doubleValue(i, k) * a.doubleValue(j, k);
                }
                if (i == j) {
                    if (sum <= 0.0) {
                        throw new IllegalArgumentException("Matrix not positive definite");
                    }
                    diagonal[i] = Math.sqrt(sum);
                } else {
                    a.set(j, i, sum / diagonal[i]);
                    a.set(i, j, 0);
                }

            }
        }


        for (i = 0; i < n; i++) {
            a.set(i, i, diagonal[i]);
        }


    }

    public static SparseMatrix<Double> decomp(SparseMatrix<Double> a, boolean complete) {

        int m = a.rows();
        SparseMatrix<Double> l = new SparseMatrix<Double>(m, m, Vectors.AS_SPARSE_REALS); //automatically initialzed to 0's
        for (int i = 0; i < m; i++) {
            if (complete) {
                for (int k = 0; k < (i + 1); k++) {
                    chol(l, k, i, a);
                }
            } else {
                final Vector<Double> l_i = l.row(i);
                for (int k : l_i.keySet()) {
                    if (k >= (i + 1)) {
                        break;
                    }
                    chol(l, k, i, a);
                }
            }
        }
        return l;
    }

    public static void chol(SparseMatrix<Double> l, int k, int i, SparseMatrix<Double> a) throws IllegalArgumentException {
        double sum = 0;
        final Vector<Double> l_k = l.row(k);
        for (int j : l_k.keySet()) {
            sum += l.doubleValue(i, j) * l_k.doubleValue(j);
        }
        double a_ii = a.doubleValue(i, i);
        if (i == k) {
            if (a_ii - sum < 0) {
                throw new IllegalArgumentException("Matrix not positive definite");
            }
            l.set(i, k, Math.sqrt(a_ii - sum));
        } else {
            l.set(i, k, (a.doubleValue(i, k) - sum) / l.doubleValue(k, k));
        }
    }

    public static Vector<Double> solve(SparseMatrix<Double> a, Vector<Double> b) {
        assert (a.cols() == b.length());
        final int N = b.length();
        Vector<Double> y = new SparseRealArray(N);
        for (int i = 0; i < N; i++) {
            double sum = b.doubleValue(i);
            for (int j = 0; j < i; j++) {
                sum -= a.doubleValue(i, j) * y.doubleValue(j);
            }
            y.put(i, sum / a.doubleValue(i, i));
        }
        return y;
    }

    public static Vector<Double> solveOnDisk(File matrixFile, int N, Vector<Double> b) {
        assert (N == b.length());
        final byte[] buf = new byte[N*8];
        Vector<Double> y = new SparseRealArray(N);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(matrixFile, "r");
            //final FileChannel channel = raf.getChannel();
            for (int i = 0; i < N; i++) {
                //final MappedByteBuffer row_i = channel.map(FileChannel.MapMode.READ_ONLY, (long) i * N * 8, N * 8);
                raf.seek((long)i * N * 8);
                raf.readFully(buf);
                final ByteBuffer row_i = ByteBuffer.wrap(buf);
                double sum = b.doubleValue(i);
                row_i.position(0);
                for (int j = 0; j < i; j++) {

                    sum -= row_i.getDouble() * y.doubleValue(j);
                }
                y.put(i, sum / row_i.getDouble());
            }
            return y;
        } catch (IOException x) {
            throw new RuntimeException(x);
        } finally {
            if(raf != null) {
                try {
                    raf.close();
                } catch(Exception x) {
                    x.printStackTrace();
                }
            }
        }
    }

    public static Vector<Double> solveT(SparseMatrix<Double> a, Vector<Double> b) {
        assert (a.cols() == b.length());
        final int N = b.length();
        Vector<Double> y = new SparseRealArray(N);
        for (int i = N - 1; i >= 0; i--) {
            double sum = b.doubleValue(i);
            for (int j = i + 1; j < N; j++) {
                sum -= a.doubleValue(j, i) * y.doubleValue(j);
            }
            y.put(i, sum / a.doubleValue(i, i));
        }
        return y;
    }

    public static Vector<Double> solveTOnDisk(File matrixFile, int N, Vector<Double> b) {
        assert (N == b.length());
        final byte[] buf = new byte[N*8];
        Vector<Double> y = new SparseRealArray(N);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(matrixFile, "r");
            for (int i = N - 1; i >= 0; i--) {
                //final MappedByteBuffer row_i = channel.map(FileChannel.MapMode.READ_ONLY, (long) i * N * 8, N * 8);
                raf.seek((long)i * N * 8);
                raf.readFully(buf);
                final ByteBuffer row_i = ByteBuffer.wrap(buf);
                double sum = b.doubleValue(i);
                if (i + 1 != N) {
                    row_i.position((i + 1) * 8);
                }
                for (int j = i + 1; j < N; j++) {
                    sum -= row_i.getDouble() * y.doubleValue(j);
                }
                row_i.position(i * 8);
                y.put(i, sum / row_i.getDouble());
            }
            return y;
        } catch (IOException x) {
            throw new RuntimeException(x);
        } finally {
            if(raf != null) {
                try {
                    raf.close();
                } catch(Exception x) {
                    x.printStackTrace();
                }
            }
        }

    }
}
