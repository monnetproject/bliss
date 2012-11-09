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
package eu.monnetproject.math.sparse;

import eu.monnetproject.math.sparse.Vectors.Factory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * A tridiagonal matrix, that is a symmetric matrix only the diagonal and values
 * immediately above or below the diagonal are non-zero. i.e., x[i,i] = alpha[i]
 * x[i,i+1] = x[i+1,i] = beta[i]
 *
 * @author John McCrae
 */
public class TridiagonalMatrix implements Matrix<Double> {

    private final double[] alpha;
    private final double[] beta;

    public TridiagonalMatrix(double[] alpha, double[] beta) {
        assert (alpha.length == 0 || beta.length == alpha.length - 1);
        this.alpha = alpha;
        this.beta = beta;
    }

    public TridiagonalMatrix(int n) {
        this.alpha = new double[n];
        this.beta = new double[n - 1];
    }

    @Override
    public Double value(int i, int j) {
        if (i == j) {
            return alpha[i];
        } else if (j == i + 1 || i == j + 1) {
            return beta[Math.min(i, j)];
        } else {
            return 0.0;
        }
    }

    @Override
    public double doubleValue(int i, int j) {
        if (i == j) {
            return alpha[i];
        } else if (j == i + 1 || i == j + 1) {
            return beta[Math.min(i, j)];
        } else {
            return 0.0;
        }
    }

    @Deprecated
    @Override
    public int intValue(int i, int j) {
        if (i == j) {
            return (int) alpha[i];
        } else if (j == i + 1 || i == j + 1) {
            return (int) beta[Math.min(i, j)];
        } else {
            return (int) 0.0;
        }
    }

    @Override
    public void set(int i, int j, int v) {
        if (i == j) {
            alpha[i] = v;
        } else if (j == i + 1 || i == j + 1) {
            beta[Math.min(i, j)] = v;
        } else if (v != 0) {
            throw new UnsupportedOperationException("Attempt to set index on non-tridiagonal matrix would make it non-tridiagonal");
        }
    }

    @Override
    public void set(int i, int j, double v) {
        if (i == j) {
            alpha[i] = v;
        } else if (j == i + 1 || i == j + 1) {
            beta[Math.min(i, j)] = v;
        } else if (v != 0) {
            throw new UnsupportedOperationException("Attempt to set index on non-tridiagonal matrix would make it non-tridiagonal");
        }
    }

    @Override
    public void set(int i, int j, Double v) {
        if (i == j) {
            alpha[i] = v;
        } else if (j == i + 1 || i == j + 1) {
            beta[Math.min(i, j)] = v;
        } else if (v != 0) {
            throw new UnsupportedOperationException("Attempt to set index on non-tridiagonal matrix would make it non-tridiagonal");
        }
    }

    @Override
    public void add(int i, int j, int v) {
        if (i == j) {
            alpha[i] += v;
        } else if (j == i + 1 || i == j + 1) {
            beta[Math.min(i, j)] += v;
        } else if (v != 0) {
            throw new UnsupportedOperationException("Attempt to set index on non-tridiagonal matrix would make it non-tridiagonal");
        }
    }

    @Override
    public void add(int i, int j, double v) {
        if (i == j) {
            alpha[i] += v;
        } else if (j == i + 1 || i == j + 1) {
            beta[Math.min(i, j)] += v;
        } else if (v != 0) {
            throw new UnsupportedOperationException("Attempt to set index on non-tridiagonal matrix would make it non-tridiagonal");
        }
    }

    @Override
    public void add(int i, int j, Double v) {
        if (i == j) {
            alpha[i] += v;
        } else if (j == i + 1 || i == j + 1) {
            beta[Math.min(i, j)] += v;
        } else if (v != 0) {
            throw new UnsupportedOperationException("Attempt to set index on non-tridiagonal matrix would make it non-tridiagonal");
        }
    }

    @Override
    public <M extends Number> Vector<Double> mult(Vector<M> x) {
        return mult(x, Vectors.AS_REALS);
    }

    @Override
    public <M extends Number, O extends Number> Vector<O> mult(Vector<M> x, Vectors.Factory<O> using) {
        assert (x.length() == alpha.length);
        final Vector<O> product = using.make(alpha.length, 0.0);
        for (int i = 0; i < alpha.length; i++) {
            double value = 0.0;
            if (i > 0) {
                value += x.doubleValue(i - 1) * beta[i - 1];
            }
            value += x.doubleValue(i) * alpha[i];
            if (i < beta.length) {
                value += x.doubleValue(i + 1) * beta[i];
            }
            product.put(i, value);
        }
        return product;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

    @Override
    public Matrix<Double> transpose() {
        return this;
    }

    @Override
    public Vector<Double> row(int i) {
        final RealVector v = new RealVector(alpha.length);
        if (i > 0) {
            v.put(i - 1, beta[i - 1]);
        }
        v.put(i, alpha[i]);
        if (i + 1 < alpha.length) {
            v.put(i + 1, beta[i]);
        }
        return v;
    }

    @Override
    public int rows() {
        return alpha.length;
    }

    @Override
    public int cols() {
        return alpha.length;
    }

    @Override
    public <M extends Number> void add(Matrix<M> matrix) {
        assert (matrix.rows() == alpha.length);
        assert (matrix.cols() == alpha.length);
        if (matrix instanceof TridiagonalMatrix) {
            final double[] alpha2 = ((TridiagonalMatrix) matrix).alpha;
            final double[] beta2 = ((TridiagonalMatrix) matrix).beta;
            for (int i = 0; i < alpha.length; i++) {
                alpha[i] += alpha2[i];
                if (i > 0) {
                    beta[i - 1] += beta2[i - 1];
                }
            }
        } else {
            for (int i = 0; i < alpha.length; i++) {
                alpha[i] += matrix.doubleValue(i, i);
                if (i < alpha.length - 1) {
                    if (matrix.doubleValue(i, i + 1) == matrix.doubleValue(i + 1, i)) {
                        beta[i] += matrix.doubleValue(i, i + 1);
                    } else {
                        throw new RuntimeException("Adding non tridiagonal matrix to tridiagonal matrix!");
                    }
                }
                for (int j = 0; j < alpha.length; j++) {
                    if (Math.abs(i - j) > 1 && matrix.doubleValue(i, j) > 1e-6) {
                        throw new RuntimeException("Adding non tridiagonal matrix to tridiagonal matrix!");
                    }
                }
            }
        }
    }

    @Override
    public <M extends Number> void sub(Matrix<M> matrix) {
        assert (matrix.rows() == alpha.length);
        assert (matrix.cols() == alpha.length);
        if (matrix instanceof TridiagonalMatrix) {
            final double[] alpha2 = ((TridiagonalMatrix) matrix).alpha;
            final double[] beta2 = ((TridiagonalMatrix) matrix).beta;
            for (int i = 0; i < alpha.length; i++) {
                alpha[i] -= alpha2[i];
                if (i > 0) {
                    beta[i - 1] -= beta2[i - 1];
                }
            }
        } else {
            for (int i = 0; i < alpha.length; i++) {
                alpha[i] -= matrix.doubleValue(i, i);
                if (i < alpha.length - 1) {
                    if (matrix.doubleValue(i, i + 1) == matrix.doubleValue(i + 1, i)) {
                        beta[i] -= matrix.doubleValue(i, i + 1);
                    } else {
                        throw new RuntimeException("Adding non tridiagonal matrix to tridiagonal matrix!");
                    }
                }
                for (int j = 0; j < alpha.length; j++) {
                    if (Math.abs(i - j) > 1 && matrix.doubleValue(i, j) > 1e-6) {
                        throw new RuntimeException("Adding non tridiagonal matrix to tridiagonal matrix!");
                    }
                }
            }
        }
    }

    @Override
    public VectorFunction<Double, Double> asVectorFunction() {
        return new VectorFunction<Double, Double>() {
            @Override
            public Vector<Double> apply(Vector<Double> v) {
                return mult(v);
            }
        };
    }

    @Override
    public Factory<Double> factory() {
        return Vectors.AS_REALS;
    }

    /**
     * Get the diagonal of the matrix
     */
    public double[] alpha() {
        return alpha;
    }

    /**
     * Get the off-diagonal of the matrix
     */
    public double[] beta() {
        return beta;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TridiagonalMatrix other = (TridiagonalMatrix) obj;
        if (!Arrays.equals(this.alpha, other.alpha)) {
            return false;
        }
        if (!Arrays.equals(this.beta, other.beta)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Arrays.hashCode(this.alpha);
        hash = 47 * hash + Arrays.hashCode(this.beta);
        return hash;
    }

    @Override
    public String toString() {
        return "TridiagonalMatrix{" + "alpha=" + Arrays.toString(alpha) + ", beta=" + Arrays.toString(beta) + '}';
    }

    public static TridiagonalMatrix fromFile(File file) throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(file));
        final String sN = in.readLine();
        if (sN == null) {
            throw new VectorFormatException("Expected n");
        }
        final int n = Integer.parseInt(sN);
        final String sAlpha = in.readLine();
        if (sAlpha == null) {
            throw new VectorFormatException("Expected alpha");
        }
        final String[] ssAlpha = sAlpha.split(",");
        if (ssAlpha.length != n) {
            throw new VectorFormatException("Wrong length for alpha");
        }
        final double[] alpha = new double[n];
        for (int i = 0; i < alpha.length; i++) {
            alpha[i] = Double.parseDouble(ssAlpha[i].replaceAll("[\\[\\]\\s]", ""));
        }
        final String sBeta = in.readLine();
        if (sBeta == null) {
            throw new VectorFormatException("Expected beta");
        }
        final String[] ssBeta = sBeta.split(",");
        if (ssBeta.length != n - 1) {
            throw new VectorFormatException("Wrong length for beta");
        }
        final double[] beta = new double[n - 1];
        for (int i = 0; i < beta.length; i++) {
            beta[i] = Double.parseDouble(ssBeta[i].replaceAll("[\\[\\]\\s]", ""));
        }
        return new TridiagonalMatrix(alpha, beta);
    }

    public void toFile(File file) throws IOException {
        final PrintWriter out = new PrintWriter(file);
        out.println(alpha.length);
        out.println(Arrays.toString(alpha));
        out.println(Arrays.toString(beta));
        out.flush();
        out.close();
    }

    public double[][] toDoubleArray() {
        final double[][] M = new double[cols()][cols()];
        for (int i = 0; i < cols(); i++) {
            M[i][i] = alpha[i];
        }
        for (int i = 1; i < cols(); i++) {
            M[i - 1][i] = M[i][i - 1] = beta[i - 1];
        }
        return M;
    }

    /**
     * Find w, such that Mw = v
     *
     * @param v the vector
     * @return w
     */
    public Vector<Double> invMult(Vector<Double> v) {
        final int n = v.length();
        if (n != alpha.length) {
            throw new IllegalArgumentException();
        }
        double[] delta = new double[n - 1];
        double[] gamma = new double[n - 1];

        delta[0] = v.doubleValue(0) / alpha[0];
        gamma[0] = -1.0 * beta[0] / alpha[0];

        for (int i = 1; i < n - 1; i++) {
            final double bga = beta[i - 1] * gamma[i - 1] + alpha[i];
            if (bga != 0.0) {
                delta[i] = (v.doubleValue(i) - beta[i - 1] * delta[i - 1]) / bga;
                gamma[i] = -1.0 * beta[i] / bga;
            } else {
                final double bga2 = beta[i] * gamma[i] + alpha[i + 1];
                if (bga2 == 0.0) {
                    // Value is 'free'
                    delta[i] = delta[i + 1] = 1.0;
                } else {
                    gamma[i + 1] = (v.doubleValue(i + 1) - beta[i] * delta[i]) / bga2;
                    delta[i + 1] = -1.0 * beta[i + 1] / bga2;
                    gamma[i] = -(alpha[i + 1] * gamma[i + 1] + beta[i + 1]) / beta[i];
                    delta[i] = beta[i + 1] * delta[i + 1] / beta[i] * gamma[i + 1];
                    i++;
                }
            }
        }

        double[] w = new double[n];
        final double bga3 = beta[n - 2] * gamma[n - 2] + alpha[n - 1];
        if (bga3 == 0.0) {
            w[n - 1] = 1.0; // value is 'free'
        } else {
            w[n - 1] = (v.doubleValue(n - 1) - beta[n - 2] * delta[n - 2]) / bga3;
        }
        for (int i = n - 2; i >= 0; i--) {
            w[i] = gamma[i] * w[i + 1] + delta[i];
        }
        return new RealVector(w);
    }

    @Override
    public <M extends Number> Vector<Double> multTransposed(Vector<M> x) {
        return mult(x);
    }

    @Override
    public <M extends Number> Matrix<Double> product(Matrix<M> B) {
        if (this.cols() != B.rows()) {
            throw new IllegalArgumentException("Matrix dimensions not suitable for product");
        }
        double[][] res = new double[this.rows()][B.cols()];
        for (int i = 0; i < this.rows(); i++) {
            for (int k = 0; k < B.cols(); k++) {
                res[i][k] = (i > 0 ? beta[i - 1] * B.doubleValue(i - 1, k) : 0.0)
                        + alpha[i] * B.doubleValue(i, k)
                        + (i + 1 != this.rows() ? beta[i] * B.doubleValue(i + 1, k) : 0.0);
            }
        }
        return new DoubleArrayMatrix(res);
    }
}
