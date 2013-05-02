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

/**
 *
 * @author jmccrae
 */
public class QR {

    public static Soln decompose(double[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        double[][] qrt = matrix;
        transpose(qrt);
        double[] rDiag = new double[Math.min(m, n)];
        for (int minor = 0; minor < Math.min(qrt.length, qrt[0].length); minor++) {
            performHouseholderReflection(minor, qrt, rDiag);
        }
        double[][] Q = getQT(qrt, rDiag);
        transpose(Q);
        double[][] R = getR(qrt, rDiag);
        return new Soln(Q, R);
    }
    
    public static Soln eigen(double[][] matrix) {
        int n = matrix.length;
        assert(n == matrix[0].length);
        double[][] X = matrix;
        double[][] Q = identity(n);
        double[][] Q2 = new double[n][n];
        while(!converged(X)) {
            Soln s = decompose(X);
            matMult(Q,s.Q,Q2);
            double[][] Qt = Q;
            Q = Q2;
            Q2 = Qt;
            matMult(s.R,s.Q,X);
            print(X);
        }
        return new Soln(Q, X);
    }

    private static void print(double[][] matrix) {
        for(int i = 0; i < matrix.length; i++) {
            if(i != 0) {
                System.out.print("    ");
            }
            for(int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    /**
     * Perform Householder reflection for a minor A(minor, minor) of A.
     *
     * @param minor minor index
     * @param matrix transposed matrix
     * @since 3.2
     */
    protected static void performHouseholderReflection(int minor, double[][] qrt, double[] rDiag) {

        final double[] qrtMinor = qrt[minor];

        double xNormSqr = 0;
        for (int row = minor; row < qrtMinor.length; row++) {
            final double c = qrtMinor[row];
            xNormSqr += c * c;
        }
        final double a = (qrtMinor[minor] > 0) ? -Math.sqrt(xNormSqr) : Math.sqrt(xNormSqr);
        rDiag[minor] = a;

        if (a != 0.0) {
            qrtMinor[minor] -= a;
            for (int col = minor + 1; col < qrt.length; col++) {
                final double[] qrtCol = qrt[col];
                double alpha = 0;
                for (int row = minor; row < qrtCol.length; row++) {
                    alpha -= qrtCol[row] * qrtMinor[row];
                }
                alpha /= a * qrtMinor[minor];

                for (int row = minor; row < qrtCol.length; row++) {
                    qrtCol[row] -= alpha * qrtMinor[row];
                }
            }
        }
    }

    private static double[][] getR(double[][] qrt, double[] rDiag) {
        final int n = qrt.length;
        final int m = qrt[0].length;
        double[][] ra = new double[m][n];
        // copy the diagonal from rDiag and the upper triangle of qr
        for (int row = Math.min(m, n) - 1; row >= 0; row--) {
            ra[row][row] = rDiag[row];
            for (int col = row + 1; col < n; col++) {
                ra[row][col] = qrt[col][row];
            }
        }
        return ra;
    }

    private static double[][] getQT(double[][] qrt, double[] rDiag) {
        // QT is supposed to be m x m
        final int n = qrt.length;
        final int m = qrt[0].length;
        double[][] qta = new double[m][m];

        for (int minor = m - 1; minor >= Math.min(m, n); minor--) {
            qta[minor][minor] = 1.0d;
        }

        for (int minor = Math.min(m, n) - 1; minor >= 0; minor--) {
            final double[] qrtMinor = qrt[minor];
            qta[minor][minor] = 1.0d;
            if (qrtMinor[minor] != 0.0) {
                for (int col = minor; col < m; col++) {
                    double alpha = 0;
                    for (int row = minor; row < m; row++) {
                        alpha -= qta[col][row] * qrtMinor[row];
                    }
                    alpha /= rDiag[minor] * qrtMinor[minor];

                    for (int row = minor; row < m; row++) {
                        qta[col][row] += -alpha * qrtMinor[row];
                    }
                }
            }
        }
        return qta;
    }

    public static void transpose(double[][] q) {
        for (int i = 0; i < q.length; i++) {
            for (int j = i + 1; j < q[0].length; j++) {
                double t = q[i][j];
                q[i][j] = q[j][i];
                q[j][i] = t;
            }
        }
    }

    private static final double EPSILON = 1e-6;
    
    private static boolean converged(double[][] X) {
        for(int i = 0; i < X.length; i++) {
            for(int j = 0; j < X[0].length; j++) {
                if(i != j && Math.abs(X[i][j]) > EPSILON) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double[][] identity(int n) {
        double[][] I = new double[n][n];
        for(int i = 0; i < n; i++) {
            I[i][i] = 1.0;
        }
        return I;
    }

    private static void matMult(double[][] left, double[][] right, double[][] result) {
        for(int i = 0; i < left.length; i++) {
            for(int k = 0; k < right[0].length; k++) {
                result[i][k] = 0;
                for(int j = 0; j < right.length; j++) {
                    result[i][k] += left[i][j] * right[j][k];
                }
            }
        }
    }

    public static class Soln {

        public final double[][] Q;
        public final double[][] R;

        public Soln(double[][] Q, double[][] R) {
            this.Q = Q;
            this.R = R;
        }
    }
}
