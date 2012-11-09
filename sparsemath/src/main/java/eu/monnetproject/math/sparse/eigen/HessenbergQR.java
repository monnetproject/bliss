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
/*
 Copyright  1999 CERN - European Organization for Nuclear Research.
 Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
 is hereby granted without fee, provided that the above copyright notice appear in all copies and 
 that both that copyright notice and this permission notice appear in supporting documentation. 
 CERN makes no representations about the suitability of this software for any purpose. 
 It is provided "as is" without expressed or implied warranty.
 */
package eu.monnetproject.math.sparse.eigen;

/**
 * Derived from COLT http://acs.lbl.gov/software/colt/
 *
 * @author John McCrae
 */
public class HessenbergQR {

    private HessenbergQR() {
    }

    public static Solution solve(int N, double[][] H) {


        //  This is derived from the Algol procedure hqr2,
        //  by Martin and Wilkinson, Handbook for Auto. Comp.,
        //  Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        // Initialize
        double[] d = new double[N];
        double[] e = new double[N];
        double[][] V = new double[N][N];

        orthes(N, H, V);

        int nn = N;
        int n = nn - 1;
        int low = 0;
        int high = nn - 1;
        double eps = Math.pow(2.0, -52.0);
        double exshift = 0.0;
        double p = 0, q = 0, r = 0, s = 0, z = 0, t, w, x, y;

        // Store roots isolated by balanc and compute matrix norm

        double norm = 0.0;
        for (int i = 0; i < nn; i++) {
            if (i < low | i > high) {
                d[i] = H[i][i];
                e[i] = 0.0;
            }
            for (int j = Math.max(i - 1, 0); j < nn; j++) {
                norm = norm + Math.abs(H[i][j]);
            }
        }

        // Outer loop over eigenvalue index

        int iter = 0;
        while (n >= low) {

            // Look for single small sub-diagonal element

            int l = n;
            while (l > low) {
                s = Math.abs(H[l - 1][l - 1]) + Math.abs(H[l][l]);
                if (s == 0.0) {
                    s = norm;
                }
                if (Math.abs(H[l][l - 1]) < eps * s) {
                    break;
                }
                l--;
            }

            // Check for convergence
            // One root found

            if (l == n) {
                H[n][n] = H[n][n] + exshift;
                d[n] = H[n][n];
                e[n] = 0.0;
                n--;
                iter = 0;

                // Two roots found

            } else if (l == n - 1) {
                w = H[n][n - 1] * H[n - 1][n];
                p = (H[n - 1][n - 1] - H[n][n]) / 2.0;
                q = p * p + w;
                z = Math.sqrt(Math.abs(q));
                H[n][n] = H[n][n] + exshift;
                H[n - 1][n - 1] = H[n - 1][n - 1] + exshift;
                x = H[n][n];

                // Real pair

                if (q >= 0) {
                    if (p >= 0) {
                        z = p + z;
                    } else {
                        z = p - z;
                    }
                    d[n - 1] = x + z;
                    d[n] = d[n - 1];
                    if (z != 0.0) {
                        d[n] = x - w / z;
                    }
                    e[n - 1] = 0.0;
                    e[n] = 0.0;
                    x = H[n][n - 1];
                    s = Math.abs(x) + Math.abs(z);
                    p = x / s;
                    q = z / s;
                    r = Math.sqrt(p * p + q * q);
                    p = p / r;
                    q = q / r;

                    // Row modification

                    for (int j = n - 1; j < nn; j++) {
                        z = H[n - 1][j];
                        H[n - 1][j] = q * z + p * H[n][j];
                        H[n][j] = q * H[n][j] - p * z;
                    }

                    // Column modification

                    for (int i = 0; i <= n; i++) {
                        z = H[i][n - 1];
                        H[i][n - 1] = q * z + p * H[i][n];
                        H[i][n] = q * H[i][n] - p * z;
                    }

                    // Accumulate transformations

                    for (int i = low; i <= high; i++) {
                        z = V[i][n - 1];
                        V[i][n - 1] = q * z + p * V[i][n];
                        V[i][n] = q * V[i][n] - p * z;
                    }

                    // Complex pair

                } else {
                    d[n - 1] = x + p;
                    d[n] = x + p;
                    e[n - 1] = z;
                    e[n] = -z;
                }
                n = n - 2;
                iter = 0;

                // No convergence yet

            } else {

                // Form shift

                x = H[n][n];
                y = 0.0;
                w = 0.0;
                if (l < n) {
                    y = H[n - 1][n - 1];
                    w = H[n][n - 1] * H[n - 1][n];
                }

                // Wilkinson's original ad hoc shift

                if (iter == 10) {
                    exshift += x;
                    for (int i = low; i <= n; i++) {
                        H[i][i] -= x;
                    }
                    s = Math.abs(H[n][n - 1]) + Math.abs(H[n - 1][n - 2]);
                    x = y = 0.75 * s;
                    w = -0.4375 * s * s;
                }

                // MATLAB's new ad hoc shift

                if (iter == 30) {
                    s = (y - x) / 2.0;
                    s = s * s + w;
                    if (s > 0) {
                        s = Math.sqrt(s);
                        if (y < x) {
                            s = -s;
                        }
                        s = x - w / ((y - x) / 2.0 + s);
                        for (int i = low; i <= n; i++) {
                            H[i][i] -= s;
                        }
                        exshift += s;
                        x = y = w = 0.964;
                    }
                }

                iter = iter + 1;   // (Could check iteration count here.)

                // Look for two consecutive small sub-diagonal elements

                int m = n - 2;
                while (m >= l) {
                    z = H[m][m];
                    r = x - z;
                    s = y - z;
                    p = (r * s - w) / H[m + 1][m] + H[m][m + 1];
                    q = H[m + 1][m + 1] - z - r - s;
                    r = H[m + 2][m + 1];
                    s = Math.abs(p) + Math.abs(q) + Math.abs(r);
                    p = p / s;
                    q = q / s;
                    r = r / s;
                    if (m == l) {
                        break;
                    }
                    if (Math.abs(H[m][m - 1]) * (Math.abs(q) + Math.abs(r))
                            < eps * (Math.abs(p) * (Math.abs(H[m - 1][m - 1]) + Math.abs(z)
                            + Math.abs(H[m + 1][m + 1])))) {
                        break;
                    }
                    m--;
                }

                for (int i = m + 2; i <= n; i++) {
                    H[i][i - 2] = 0.0;
                    if (i > m + 2) {
                        H[i][i - 3] = 0.0;
                    }
                }

                // Double QR step involving rows l:n and columns m:n

                for (int k = m; k <= n - 1; k++) {
                    boolean notlast = (k != n - 1);
                    if (k != m) {
                        p = H[k][k - 1];
                        q = H[k + 1][k - 1];
                        r = (notlast ? H[k + 2][k - 1] : 0.0);
                        x = Math.abs(p) + Math.abs(q) + Math.abs(r);
                        if (x != 0.0) {
                            p = p / x;
                            q = q / x;
                            r = r / x;
                        }
                    }
                    if (x == 0.0) {
                        break;
                    }
                    s = Math.sqrt(p * p + q * q + r * r);
                    if (p < 0) {
                        s = -s;
                    }
                    if (s != 0) {
                        if (k != m) {
                            H[k][k - 1] = -s * x;
                        } else if (l != m) {
                            H[k][k - 1] = -H[k][k - 1];
                        }
                        p = p + s;
                        x = p / s;
                        y = q / s;
                        z = r / s;
                        q = q / p;
                        r = r / p;

                        // Row modification

                        for (int j = k; j < nn; j++) {
                            p = H[k][j] + q * H[k + 1][j];
                            if (notlast) {
                                p = p + r * H[k + 2][j];
                                H[k + 2][j] = H[k + 2][j] - p * z;
                            }
                            H[k][j] = H[k][j] - p * x;
                            H[k + 1][j] = H[k + 1][j] - p * y;
                        }

                        // Column modification

                        for (int i = 0; i <= Math.min(n, k + 3); i++) {
                            p = x * H[i][k] + y * H[i][k + 1];
                            if (notlast) {
                                p = p + z * H[i][k + 2];
                                H[i][k + 2] = H[i][k + 2] - p * r;
                            }
                            H[i][k] = H[i][k] - p;
                            H[i][k + 1] = H[i][k + 1] - p * q;
                        }

                        // Accumulate transformations

                        for (int i = low; i <= high; i++) {
                            p = x * V[i][k] + y * V[i][k + 1];
                            if (notlast) {
                                p = p + z * V[i][k + 2];
                                V[i][k + 2] = V[i][k + 2] - p * r;
                            }
                            V[i][k] = V[i][k] - p;
                            V[i][k + 1] = V[i][k + 1] - p * q;
                        }
                    }  // (s != 0)
                }  // k loop
            }  // check convergence
        }  // while (n >= low)

        // Backsubstitute to find vectors of upper triangular form

        if (norm == 0.0) {
            return new Solution(V, d);
        }

        for (n = nn - 1; n >= 0; n--) {
            p = d[n];
            q = e[n];

            // Real vector

            if (q == 0) {
                int l = n;
                H[n][n] = 1.0;
                for (int i = n - 1; i >= 0; i--) {
                    w = H[i][i] - p;
                    r = 0.0;
                    for (int j = l; j <= n; j++) {
                        r = r + H[i][j] * H[j][n];
                    }
                    if (e[i] < 0.0) {
                        z = w;
                        s = r;
                    } else {
                        l = i;
                        if (e[i] == 0.0) {
                            if (w != 0.0) {
                                H[i][n] = -r / w;
                            } else {
                                H[i][n] = -r / (eps * norm);
                            }

                            // Solve real equations

                        } else {
                            x = H[i][i + 1];
                            y = H[i + 1][i];
                            q = (d[i] - p) * (d[i] - p) + e[i] * e[i];
                            t = (x * s - z * r) / q;
                            H[i][n] = t;
                            if (Math.abs(x) > Math.abs(z)) {
                                H[i + 1][n] = (-r - w * t) / x;
                            } else {
                                H[i + 1][n] = (-s - y * t) / z;
                            }
                        }

                        // Overflow control

                        t = Math.abs(H[i][n]);
                        if ((eps * t) * t > 1) {
                            for (int j = i; j <= n; j++) {
                                H[j][n] = H[j][n] / t;
                            }
                        }
                    }
                }

                // Complex vector

            } else if (q < 0) {
                int l = n - 1;

                // Last vector component imaginary so matrix is triangular

                if (Math.abs(H[n][n - 1]) > Math.abs(H[n - 1][n])) {
                    H[n - 1][n - 1] = q / H[n][n - 1];
                    H[n - 1][n] = -(H[n][n] - p) / H[n][n - 1];
                } else {
                    final CDiv cdiv = cdiv(0.0, -H[n - 1][n], H[n - 1][n - 1] - p, q);
                    H[n - 1][n - 1] = cdiv.r;
                    H[n - 1][n] = cdiv.i;
                }
                H[n][n - 1] = 0.0;
                H[n][n] = 1.0;
                for (int i = n - 2; i >= 0; i--) {
                    double ra, sa, vr, vi;
                    ra = 0.0;
                    sa = 0.0;
                    for (int j = l; j <= n; j++) {
                        ra = ra + H[i][j] * H[j][n - 1];
                        sa = sa + H[i][j] * H[j][n];
                    }
                    w = H[i][i] - p;

                    if (e[i] < 0.0) {
                        z = w;
                        r = ra;
                        s = sa;
                    } else {
                        l = i;
                        if (e[i] == 0) {
                            final CDiv cdiv = cdiv(-ra, -sa, w, q);
                            H[i][n - 1] = cdiv.r;
                            H[i][n] = cdiv.i;
                        } else {

                            // Solve complex equations

                            x = H[i][i + 1];
                            y = H[i + 1][i];
                            vr = (d[i] - p) * (d[i] - p) + e[i] * e[i] - q * q;
                            vi = (d[i] - p) * 2.0 * q;
                            if (vr == 0.0 & vi == 0.0) {
                                vr = eps * norm * (Math.abs(w) + Math.abs(q)
                                        + Math.abs(x) + Math.abs(y) + Math.abs(z));
                            }
                            final CDiv cdiv = cdiv(x * r - z * ra + q * sa, x * s - z * sa - q * ra, vr, vi);
                            H[i][n - 1] = cdiv.r;
                            H[i][n] = cdiv.i;
                            if (Math.abs(x) > (Math.abs(z) + Math.abs(q))) {
                                H[i + 1][n - 1] = (-ra - w * H[i][n - 1] + q * H[i][n]) / x;
                                H[i + 1][n] = (-sa - w * H[i][n] - q * H[i][n - 1]) / x;
                            } else {
                                final CDiv cdiv2 = cdiv(-r - y * H[i][n - 1], -s - y * H[i][n], z, q);
                                H[i + 1][n - 1] = cdiv2.r;
                                H[i + 1][n] = cdiv2.i;
                            }
                        }

                        // Overflow control

                        t = Math.max(Math.abs(H[i][n - 1]), Math.abs(H[i][n]));
                        if ((eps * t) * t > 1) {
                            for (int j = i; j <= n; j++) {
                                H[j][n - 1] = H[j][n - 1] / t;
                                H[j][n] = H[j][n] / t;
                            }
                        }
                    }
                }
            }
        }

        // Vectors of isolated roots

        for (int i = 0; i < nn; i++) {
            if (i < low | i > high) {
                for (int j = i; j < nn; j++) {
                    V[i][j] = H[i][j];
                }
            }
        }

        // Back transformation to get eigenvectors of original matrix

        for (int j = nn - 1; j >= low; j--) {
            for (int i = low; i <= high; i++) {
                z = 0.0;
                for (int k = low; k <= Math.min(j, high); k++) {
                    z = z + V[i][k] * H[k][j];
                }
                V[i][j] = z;
            }
        }


        for (int i = 0; i < nn; i++) {
            norm = 0.0;
            for (int j = 0; j < nn; j++) {
                norm += V[j][i] * V[j][i];
            }
            norm = Math.sqrt(norm);
            for (int j = 0; j < nn; j++) {
                V[j][i] /= norm;
            }
        }

        return new Solution(V, d);
    }

    private static class CDiv {

        final double r, i;

        public CDiv(double r, double i) {
            this.r = r;
            this.i = i;
        }
    }

    private static CDiv cdiv(double xr, double xi, double yr, double yi) {
        double r, d;
        if (Math.abs(yr) > Math.abs(yi)) {
            r = yi / yr;
            d = yr + r * yi;
            return new CDiv((xr + r * xi) / d,
                    (xi - r * xr) / d);
        } else {
            r = yr / yi;
            d = yi + r * yr;
            return new CDiv((r * xr + xi) / d,
                    (r * xi - xr) / d);
        }
    }

    private static void orthes(int n, double[][] H, double[][] V) {
        //  This is derived from the Algol procedures orthes and ortran,
        //  by Martin and Wilkinson, Handbook for Auto. Comp.,
        //  Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutines in EISPACK.

        int low = 0;
        int high = n - 1;
        double[] ort = new double[n];

        for (int m = low + 1; m <= high - 1; m++) {

            // Scale column.

            double scale = 0.0;
            for (int i = m; i <= high; i++) {
                scale = scale + Math.abs(H[i][m - 1]);
            }
            if (scale != 0.0) {

                // Compute Householder transformation.

                double h = 0.0;
                for (int i = high; i >= m; i--) {
                    ort[i] = H[i][m - 1] / scale;
                    h += ort[i] * ort[i];
                }
                double g = Math.sqrt(h);
                if (ort[m] > 0) {
                    g = -g;
                }
                h = h - ort[m] * g;
                ort[m] = ort[m] - g;

                // Apply Householder similarity transformation
                // H = (I-u*u'/h)*H*(I-u*u')/h)

                for (int j = m; j < n; j++) {
                    double f = 0.0;
                    for (int i = high; i >= m; i--) {
                        f += ort[i] * H[i][j];
                    }
                    f = f / h;
                    for (int i = m; i <= high; i++) {
                        H[i][j] -= f * ort[i];
                    }
                }

                for (int i = 0; i <= high; i++) {
                    double f = 0.0;
                    for (int j = high; j >= m; j--) {
                        f += ort[j] * H[i][j];
                    }
                    f = f / h;
                    for (int j = m; j <= high; j++) {
                        H[i][j] -= f * ort[j];
                    }
                }
                ort[m] = scale * ort[m];
                H[m][m - 1] = scale * g;
            }
        }

        // Accumulate transformations (Algol's ortran).

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                V[i][j] = (i == j ? 1.0 : 0.0);
            }
        }

        for (int m = high - 1; m >= low + 1; m--) {
            if (H[m][m - 1] != 0.0) {
                for (int i = m + 1; i <= high; i++) {
                    ort[i] = H[i][m - 1];
                }
                for (int j = m; j <= high; j++) {
                    double g = 0.0;
                    for (int i = m; i <= high; i++) {
                        g += ort[i] * V[i][j];
                    }
                    // Double division avoids possible underflow
                    g = (g / ort[m]) / H[m][m - 1];
                    for (int i = m; i <= high; i++) {
                        V[i][j] += g * ort[i];
                    }
                }
            }
        }
    }

    public static class Solution {

        public final double[][] V;
        public final double[] d;

        public Solution(double[][] V, double[] d) {
            this.V = V;
            this.d = d;
        }
    }
}
