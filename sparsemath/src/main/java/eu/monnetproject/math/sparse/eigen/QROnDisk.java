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
package eu.monnetproject.math.sparse.eigen;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Perform QR Decomposition and Eigenvalue solve on very large matrices.
 * This implementation uses on disk matrices and multi-threading across most operations
 * 
 * @author jmccrae
 */
public class QROnDisk {

    public static int NUM_THREADS = 10;

    /**
     * Calculate the QR decomposition. Such that X = QR, Q is orthonormal and R is
     * upper triangular. The result is stored in the Q and R files
     * @param X The matrix (note this is not preserved!)
     * @param Q The buffer to write Q to. This should be initialized
     * @param R The buffer to write R to. This should be initialized
     * @throws IOException 
     */
    public static void decompose(DiskMatrix X, DiskMatrix Q, DiskMatrix R) throws IOException {
        final int n = X.n;
        transpose(X);
        double[] rDiag = new double[n];
        for (int minor = 0; minor < n; minor++) {
            performHouseholderReflection(minor, X, rDiag);
        }
        getQT(X, rDiag, Q);
        transpose(Q);
        getR(X, rDiag, R);
    }

    /**
     * Calculate the eigendecomposition of a matrix X. 
     * @param X A symmetric matrix to decompose
     * @param eigenvectors A nxn buffer
     * @param Q A nxn buffer
     * @param Q2 A nxn buffer
     * @param R A nxn buffer
     * @return The solution such that the file indicated by Soln.Q is the eigenvector
     * matrix (and is either the 2nd or 4th parameter) and Soln.R is an approximately
     * diagonal matrix with eigenvalues on the diagonal
     * @throws IOException 
     */
    public static Soln eigen(DiskMatrix X, DiskMatrix eigenvectors, DiskMatrix Q, DiskMatrix Q2, DiskMatrix R) throws IOException {
        final int n = X.n;
        identity(n, eigenvectors);
        identity(n, Q2);
        while (!converged(X)) {
            decompose(X, Q, R);
            matMult(eigenvectors, Q, Q2);
            DiskMatrix Qt = eigenvectors;
            eigenvectors = Q2;
            Q2 = Qt;
            matMult(R, Q, X);
            X.print();
        }
        return new Soln(eigenvectors, X);
    }

    /**
     * Perform Householder reflection for a minor A(minor, minor) of A.
     *
     * @param minor minor index
     * @param matrix transposed matrix
     * @since 3.2
     */
    protected static void performHouseholderReflection(final int minor, DiskMatrix qrt, double[] rDiag) throws IOException {
        final ThreadPoolExecutor threadPool = makeThreadPool();
        final double[] qrtMinor = qrt.readRow(minor);

        double xNormSqr = 0;
        for (int row = minor; row < qrtMinor.length; row++) {
            final double c = qrtMinor[row];
            xNormSqr += c * c;
        }
        final double a = (qrtMinor[minor] > 0) ? -Math.sqrt(xNormSqr) : Math.sqrt(xNormSqr);
        rDiag[minor] = a;

        if (a != 0.0) {
            qrtMinor[minor] -= a;
            for (int col = minor + 1; col < qrt.n; col++) {
                threadPool.execute(new HouseholderInner(qrt, col, minor, qrtMinor, a));
            }
            barrier(threadPool);
            qrt.writeRow(minor, qrtMinor);
        }
    }

    /**
     * Start a thread pool
     * @return 
     */
    private static ThreadPoolExecutor makeThreadPool() {
        final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(NUM_THREADS - 1, NUM_THREADS - 1, 10, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        threadPool.setRejectedExecutionHandler(new Wait());
        return threadPool;
    }

    /**
     * Wait for all threads to terminate
     * @param threadPool
     * @throws RuntimeException 
     */
    private static void barrier(ThreadPoolExecutor threadPool) throws RuntimeException {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(5, TimeUnit.DAYS);
        } catch (InterruptedException x) {
            throw new RuntimeException(x);
        }
    }

    private static class HouseholderInner implements Runnable {

        final DiskMatrix qrt;
        final int col;
        final int minor;
        final double[] qrtMinor;
        final double a;

        public HouseholderInner(final DiskMatrix qrt, final int col, final int minor, final double[] qrtMinor, final double a) {
            this.qrt = qrt;
            this.col = col;
            this.minor = minor;
            this.qrtMinor = qrtMinor;
            this.a = a;
        }

        @Override
        public void run() {
            try {
                final double[] qrtCol = qrt.readRow(col);
                double alpha = 0;
                for (int row = minor; row < qrtCol.length; row++) {
                    alpha -= qrtCol[row] * qrtMinor[row];
                }
                alpha /= a * qrtMinor[minor];

                for (int row = minor; row < qrtCol.length; row++) {
                    qrtCol[row] -= alpha * qrtMinor[row];
                }
                qrt.writeRow(col, qrtCol);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
    }

    private static class GetRInner implements Runnable {

        final int n;
        final DiskMatrix qrt;
        final int row;
        final double[] rDiag;
        final DiskMatrix R;

        public GetRInner(int n, DiskMatrix qrt, int row, double[] rDiag, DiskMatrix R) {
            this.n = n;
            this.qrt = qrt;
            this.row = row;
            this.rDiag = rDiag;
            this.R = R;
        }

        @Override
        public void run() {
            try {
                double[] ra_row = new double[n];
                final double[] qrt_row = qrt.readRow(row);
                ra_row[row] = rDiag[row];
                System.arraycopy(qrt_row, row + 1, ra_row, row + 1, n - row - 1);
                R.writeRow(row, ra_row);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
    }

    /**
     * Extract R from the QRT matrix
     * @param qrt The QRT matrix
     * @param rDiag The diagonal of R
     * @param R The matrix to write R to
     * @throws IOException 
     */
    private static void getR(DiskMatrix qrt, double[] rDiag, DiskMatrix R) throws IOException {
        final int n = qrt.n;
        transpose(qrt);
        final int m = n;
        final ThreadPoolExecutor threadPool = makeThreadPool();
        // copy the diagonal from rDiag and the upper triangle of qr
        for (int row = Math.min(m, n) - 1; row >= 0; row--) {
            threadPool.execute(new GetRInner(n, qrt, row, rDiag, R));
        }
        barrier(threadPool);
    }

    private static class GetQTInner implements Runnable {

        DiskMatrix qta;
        int col;
        int minor;
        final int m;
        final double[] qrtMinor;
        double[] rDiag;

        public GetQTInner(DiskMatrix qta, int col, int minor, int m, double[] qrtMinor, double[] rDiag) {
            this.qta = qta;
            this.col = col;
            this.minor = minor;
            this.m = m;
            this.qrtMinor = qrtMinor;
            this.rDiag = rDiag;
        }

        @Override
        public void run() {
            try {
                final double[] qta_col = qta.readRow(col);
                double alpha = 0;
                for (int row = minor; row < m; row++) {
                    alpha -= qta_col[row] * qrtMinor[row];
                }
                alpha /= rDiag[minor] * qrtMinor[minor];

                for (int row = minor; row < m; row++) {
                    qta_col[row] += -alpha * qrtMinor[row];
                }
                qta.writeRow(col, qta_col);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
    }

    /**
     * Get the Q^T matrix from the QR^T matrix
     * @param qrt The QR^T matrix
     * @param rDiag The diagonal of R
     * @param qta The matrix to write to
     * @throws IOException 
     */
    private static void getQT(DiskMatrix qrt, double[] rDiag, DiskMatrix qta) throws IOException {
        // QT is supposed to be m x m
        final int n = qrt.n;
        final int m = n;

        qta.clear();

        for (int minor = Math.min(m, n) - 1; minor >= 0; minor--) {
            final ThreadPoolExecutor threadPool = makeThreadPool();
            final double[] qrtMinor = qrt.readRow(minor);
            qta.writeSingle(minor, minor, 1.0);
            if (qrtMinor[minor] != 0.0) {
                for (int col = minor; col < m; col++) {
                    threadPool.execute(new GetQTInner(qta, col, minor, m, qrtMinor, rDiag));
                }
            }
            barrier(threadPool);
        }
    }

    private static class TransposeInner implements Runnable {

        final int i;
        final int BLOCK_SIZE;
        final DiskMatrix q;

        public TransposeInner(final int i, final int BLOCK_SIZE, final DiskMatrix q) {
            this.i = i;
            this.BLOCK_SIZE = BLOCK_SIZE;
            this.q = q;
        }

        @Override
        public void run() {
            try {
                double[][] values = new double[Math.min(i + BLOCK_SIZE, q.n) - i][Math.min(i + BLOCK_SIZE, q.n) - i];
                q.readBlock(i, i, BLOCK_SIZE, values);
                transpose2D(values);
                q.writeBlock(i, i, BLOCK_SIZE, values);
                for (int j = i + BLOCK_SIZE; j < q.n; j += BLOCK_SIZE) {
                    double[][] values1 = new double[Math.min(i + BLOCK_SIZE, q.n) - i][Math.min(j + BLOCK_SIZE, q.n) - j];
                    double[][] values2 = new double[Math.min(j + BLOCK_SIZE, q.n) - j][Math.min(i + BLOCK_SIZE, q.n) - i];
                    q.readBlock(i, j, BLOCK_SIZE, values1);
                    q.readBlock(j, i, BLOCK_SIZE, values2);
                    transpose2D(values1, values2);
                    q.writeBlock(i, j, BLOCK_SIZE, values1);
                    q.writeBlock(j, i, BLOCK_SIZE, values2);
                }
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
    }

    /**
     * Transpose a matrix using the blocking strategy
     * @param q The matrix to transpose (in place)
     * @throws IOException 
     */
    private static void transpose(DiskMatrix q) throws IOException {
        final int BLOCK_SIZE = 1024;
        transpose(q, BLOCK_SIZE);
    }

    /**
     * Transpose a matrix using the blocking strategy
     * @param q The matrix to transpose (in place)
     * @throws IOException 
     */
    public static void transpose(DiskMatrix q, int BLOCK_SIZE) throws IOException {
        final ThreadPoolExecutor threadPool = makeThreadPool();
        for (int i = 0; i < q.n; i += BLOCK_SIZE) {
            threadPool.execute(new TransposeInner(i, BLOCK_SIZE, q));
        }
        barrier(threadPool);
    }

    private static void transpose2D(double[][] q) {
        for (int i = 0; i < q.length; i++) {
            for (int j = i + 1; j < q[0].length; j++) {
                double t = q[i][j];
                q[i][j] = q[j][i];
                q[j][i] = t;
            }
        }
    }

    private static void transpose2D(double[][] q, double[][] q2) {
        for (int i = 0; i < q.length; i++) {
            for (int j = 0; j < q2.length; j++) {
                double t = q[i][j];
                q[i][j] = q2[j][i];
                q2[j][i] = t;
            }
        }
    }
    private static final double EPSILON = 1e-6;

    /**
     * True if the matrix is approximately diagonal
     * @param X The matrix
     * @return 
     * @throws IOException 
     */
    private static boolean converged(DiskMatrix X) throws IOException {
        for (int i = 0; i < X.n; i++) {
            final double[] row = X.readRow(i);
            for (int j = 0; j < X.n; j++) {
                if (i != j && Math.abs(row[j]) > EPSILON) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Initialize a matrix as a nxn identity matrix
     * @param n The size of the matrix
     * @param I The matrix to write to
     * @throws IOException 
     */
    private static void identity(int n, DiskMatrix I) throws IOException {
        I.reset();
        for (int i = 0; i < n; i++) {
            byte[] buf = new byte[n * 8];
            final ByteBuffer bb = ByteBuffer.wrap(buf);
            bb.putDouble(i * 8, 1.0);
            I.write(buf);
        }
    }

    private static class MatMultInner implements Runnable {

        DiskMatrix left;
        int i;
        DiskMatrix right;
        DiskMatrix result;

        public MatMultInner(DiskMatrix left, int i, DiskMatrix right, DiskMatrix result) {
            this.left = left;
            this.i = i;
            this.right = right;
            this.result = result;
        }

        @Override
        public void run() {
            try {
                final double[] left_i = left.readRow(i);
                final double[] result_i = new double[left.n];
                for (int j = 0; j < right.n; j++) {
                    final double[] right_j = right.readRow(j);
                    for (int k = 0; k < right.n; k++) {
                        result_i[k] += left_i[j] * right_j[k];
                    }
                }
                result.writeRow(i, result_i);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
    }

    private static void matMult(DiskMatrix left, DiskMatrix right, DiskMatrix result) throws IOException {
        final ThreadPoolExecutor threadPool = makeThreadPool();
        for (int i = 0; i < left.n; i++) {
            threadPool.execute(new MatMultInner(left, i, right, result));
        }
        barrier(threadPool);
    }

    /**
     * Class for on disk matrices
     */
    public static class DiskMatrix {

        private final RandomAccessFile matrix;
        public final int n;

        /**
         * Create a nxn on disk matrix
         * @param matrix A randomaccessfile opened in "rw" mode
         * @param n The size of the matrix
         */
        public DiskMatrix(RandomAccessFile matrix, int n) {
            this.matrix = matrix;
            this.n = n;
        }

        /**
         * Print to STDOUT (for debugging)
         * @throws IOException 
         */
        public void print() throws IOException {
            for (int i = 0; i < n; i++) {
                double[] d = readRow(i);
                for (int j = 0; j < n; j++) {
                    System.out.print(d[j] + " ");
                }
                System.out.println();
            }
        }

        /**
         * Replace this matrix with the nxn zero matrix 
         * @throws IOException 
         */
        public void clear() throws IOException {
            this.reset();
            for (int i = 0; i < n; i++) {
                byte[] buf = new byte[8 * n];
                this.write(buf);
            }
        }

        /**
         * Create and intialize a nxn zero matrix
         * @param matrix The matrix file
         * @param n The size of the matrix
         * @return The object
         * @throws IOException 
         */
        public static DiskMatrix allocate(RandomAccessFile matrix, int n) throws IOException {
            final DiskMatrix m = new DiskMatrix(matrix, n);
            m.clear();
            return m;
        }

        /**
         * Read a row
         * @param i The row to read
         * @return The row as a double array
         * @throws IOException 
         */
        public double[] readRow(int i) throws IOException {
            double[] row = new double[n];
            byte[] buf = new byte[n * 8];
            synchronized (this) {
                matrix.seek((long) n * i * 8);
                matrix.readFully(buf);
            }
            final ByteBuffer bb = ByteBuffer.wrap(buf);
            for (int j = 0; j < n; j++) {
                row[j] = bb.getDouble();
            }
            return row;
        }

        /**
         * Write a row
         * @param i The row to write
         * @param row The values to write
         * @throws IOException 
         */
        public void writeRow(int i, double[] row) throws IOException {
            byte[] buf = new byte[n * 8];
            final ByteBuffer bb = ByteBuffer.wrap(buf);
            for (int j = 0; j < n; j++) {
                bb.putDouble(row[j]);
            }
            synchronized (this) {
                matrix.seek((long) n * i * 8);
                matrix.write(buf);
            }
        }

        /**
         * Reset the matrix to the beginning (not thread-safe!)
         * @throws IOException 
         */
        public void reset() throws IOException {
            matrix.seek(0);
        }

        /**
         * Write directly to underlying file (not thread-safe!)
         * @param buf
         * @throws IOException 
         */
        public void write(byte[] buf) throws IOException {
            matrix.write(buf);
        }

        /**
         * Put a single value into the matrix (Avoid for speed reasons)
         * @param i The row index
         * @param j The col index
         * @param value The value
         * @throws IOException 
         */
        public void writeSingle(int i, int j, double value) throws IOException {
            byte[] buf = new byte[8];
            ByteBuffer.wrap(buf).putDouble(value);
            synchronized (this) {
                matrix.seek((long) n * i * 8 + j * 8);
                matrix.write(buf);
            }
        }

        /**
         * Read a mxm sub-matrix starting from (i,j). If there are not sufficient 
         * then the largest subblock is returned
         * @param i The row index to start the block
         * @param j The col index to start the block
         * @param m The size of the sub-matrix
         * @param values The values to write into should be a matrix of dimension
         * {@code [Math.min(i + m, this.n) - i][Math.min(j + m, this.n) - j]}
         * @throws IOException 
         */
        public void readBlock(int i, int j, int m, double[][] values) throws IOException {
            for (int i2 = i; i2 < Math.min(i + m, n); i2++) {
                final int cols = Math.min(j + m, n) - j;
                byte[] buf = new byte[8 * cols];
                synchronized (this) {
                    matrix.seek((long) n * i2 * 8 + j * 8);
                    matrix.readFully(buf);
                }
                final ByteBuffer bb = ByteBuffer.wrap(buf);
                for (int j2 = j; j2 < j + cols; j2++) {
                    values[i2 - i][j2 - j] = bb.getDouble();
                }
            }
        }

        /**
         * Write an mxm sub-matrix to (i,j). If the values matrix is too large overflow may occur!
         * @param i The row index to write to
         * @param j The col index to write to
         * @param m The size of the sub-matrix
         * @param values The values to write. Should be a matrix of dimension 
         * {@code [Math.min(i + m, this.n) - i][Math.min(j + m, this.n) - j]}
         * @throws IOException 
         */
        public void writeBlock(int i, int j, int m, double[][] values) throws IOException {
            assert(values.length == Math.min(i + m, this.n) - i);
            assert(values[0].length == Math.min(j + m, this.n) - j);
            for (int i2 = 0; i2 < values.length; i2++) {
                byte[] buf = new byte[values[i2].length * 8];
                final ByteBuffer bb = ByteBuffer.wrap(buf);
                for (int j2 = 0; j2 < values[i2].length; j2++) {
                    bb.putDouble(values[i2][j2]);
                }
                synchronized (this) {
                    matrix.seek((long) n * (i + i2) * 8 + j * 8);
                    matrix.write(buf);
                }
            }
        }

        /**
         * Read the diagonal of this matrix
         * @return The diagonal vector
         * @throws IOException 
         */
        public double[] diagonal() throws IOException {
            matrix.seek(0);
            double[] diag = new double[n];
            byte[] buf = new byte[8];
            for (int i = 0; i < n; i++) {
                matrix.read(buf);
                diag[i] = ByteBuffer.wrap(buf).getDouble();
                if (i != n - 1) {
                    matrix.skipBytes(8 * n);
                }
            }
            return diag;
        }
    }

    /**
     * A solution to a QR or eigen problem
     */
    public static class Soln {
        /**
         * The Q matrix (for a QR decomposition) or the eigenvector matrix (for eigendecomposition)
         */
        public final DiskMatrix Q;
        /**
         * The R matrix (for a QR decomposition) or the diagonal eigenvalue matrix (for eigendecomposition)
         */
        public final DiskMatrix R;

        public Soln(DiskMatrix Q, DiskMatrix R) {
            this.Q = Q;
            this.R = R;
        }
    }

    /**
     * Wait until a thread pool is free by polling
     */
    private static class Wait implements RejectedExecutionHandler {

        private final HashSet<Runnable> waiting = new HashSet<Runnable>();

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (waiting.contains(r)) {
                throw new RejectedExecutionException();
            }
            while (true) {
                waiting.add(r);
                try {
                    synchronized (this) {
                        this.wait(200);
                    }
                } catch (InterruptedException x) {
                }
                try {
                    if (executor.getActiveCount() < executor.getPoolSize()) {
                        executor.submit(r);
                        waiting.remove(r);
                        return;
                    }
                } catch (RejectedExecutionException ree) {
                }
            }
        }
    }
}
