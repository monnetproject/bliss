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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import static eu.monnetproject.math.sparse.Vectors.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

/**
 * Implements a sparse matrix
 *
 * @author John McCrae
 */
public class SparseMatrix<N extends Number> implements Matrix<N>, Serializable {

    private static final long serialVersionUID = -2852604771859464493L;
    private final Vector<N>[] arr;
    private final int m, n;
    private final double defaultValue;
    transient private Factory<N> using;

    /**
     * Create a new sparse matrix
     *
     * @param m The number of rows
     * @param n The number of columns
     */
    @SuppressWarnings("unchecked")
    public SparseMatrix(int m, int n, Factory<N> using) {
        this.arr = (Vector<N>[]) new Vector<?>[m];
        this.m = m;
        this.n = n;
        this.defaultValue = 0.0;
        this.using = using;
    }

    @SuppressWarnings("unchecked")
    public SparseMatrix(int m, int n, double defaultValue, Factory<N> using) {
        this.arr = (Vector<N>[]) new Vector<?>[m];
        this.m = m;
        this.n = n;
        this.defaultValue = defaultValue;
        this.using = using;
    }

    /**
     * Construct a new matrix. The consistency of the vectors will not be
     * checked! The defaultValue of all arrays must be equal and all values in
     * the array must be less than n.
     *
     * @param n The maximum value in the arrays
     * @param arr The rows of the matrix
     * @param using The factory for building new vectors
     */
    public SparseMatrix(int n, Vector<N>[] arr, Factory<N> using) {
        this.arr = arr;
        this.m = arr.length;
        this.n = n;
        this.defaultValue = m > 0 ? arr[0].defaultValue().doubleValue() : 0.0;
        this.using = using;
    }

    /**
     * Multiply this matrix by a column vector
     *
     * @param x The vector
     * @param using {@link AS_INT_ARRAY} or {@link AS_REAL_ARRAY} as appropriate
     * @return The product vector
     */
    @Override
    public <M extends Number> Vector<N> mult(Vector<M> x) {
        assert (m == x.length());
        if (x instanceof RealVector) {
            double[] x2 = ((RealVector) x).data();

            final Vector<N> y = using.make(n, 0.0);
            for (int i = 0; i < m; i++) {
                final double value;
                if (arr[i] == null) {
                    if (defaultValue == 0.0) {
                        continue;
                    } else {
                        double v = 0.0;
                        for (int j = 0; j < x2.length; j++) {
                            v += defaultValue * x2[j];
                        }
                        value = v;
                    }
                } else {
                    value = arr[i].innerProduct(x);
                }
                y.put(i, using.valueOf(value));
            }
            return y;
        } else {
            final Vector<N> y = using.make(n, 0.0);
            for (int i = 0; i < m; i++) {
                final double value;
                if (arr[i] == null) {
                    if (defaultValue == 0.0) {
                        continue;
                    } else {
                        double v = 0.0;
                        for (Map.Entry<Integer, M> e : x.entrySet()) {
                            v += defaultValue * e.getValue().doubleValue();
                        }
                        value = v;
                    }
                } else {
                    value = arr[i].innerProduct(x);
                }
                if (value != 0.0) {
                    y.put(i, using.valueOf(value));
                }
            }
            return y;
        }
    }
    
    @Override
    public <M extends Number> Vector<N> multTransposed(Vector<M> x) {
        assert (m == cols());
        double[] result = new double[m];
        Arrays.fill(result, m*defaultValue);
        for(int i = 0; i < arr.length; i++) {
            for (Map.Entry<Integer, N> e : arr[i].entrySet()) {
                result[e.getKey().intValue()] += e.getValue().doubleValue() - defaultValue;
            }
        }
        return using.make(result);
    }

    /**
     * Multiply this matrix by a column vector
     *
     * @param x The vector
     * @param using {@link AS_INT_ARRAY} or {@link AS_REAL_ARRAY} as appropriate
     * @return The product vector
     */
    @Override
    public <M extends Number, P extends Number> Vector<P> mult(Vector<M> x, Factory<P> using) {
        assert (m == x.length());
        if (x instanceof RealVector) {
            double[] x2 = ((RealVector) x).data();

            final Vector<P> y = using.make(n, 0.0);
            for (int i = 0; i < m; i++) {
                final double value;
                if (arr[i] == null) {
                    if (defaultValue == 0.0) {
                        continue;
                    } else {
                        double v = 0.0;
                        for (int j = 0; j < x2.length; j++) {
                            v += defaultValue * x2[j];
                        }
                        value = v;
                    }
                } else {
                    value = arr[i].innerProduct(x);
                }
                y.put(i, using.valueOf(value));
            }
            return y;
        } else {
            final Vector<P> y = using.make(n, 0.0);
            for (int i = 0; i < m; i++) {
                final double value;
                if (arr[i] == null) {
                    if (defaultValue == 0.0) {
                        continue;
                    } else {
                        double v = 0.0;
                        for (Map.Entry<Integer, M> e : x.entrySet()) {
                            v += defaultValue * e.getValue().doubleValue();
                        }
                        value = v;
                    }
                } else {
                    value = arr[i].innerProduct(x);
                }
                if (value != 0.0) {
                    y.put(i, using.valueOf(value));
                }
            }
            return y;
        }
    }

    /**
     * Multiple this matrix by a sparse array and yield a dense vector
     *
     * @param x The vector
     * @return The result
     */
    public int[] multIntDense(Vector<Integer> x) {
        assert (m == x.length());
        final int[] y = new int[m];
        for (int i = 0; i < m; i++) {
            if (arr[i] == null) {
                if (defaultValue == 0.0) {
                    continue;
                } else {
                    double v = 0.0;
                    for (Map.Entry<Integer, Integer> e : x.entrySet()) {
                        v += defaultValue * e.getValue();
                    }
                    y[i] = (int) v;
                }
            } else {
                y[i] = (int) arr[i].innerProduct(x);
            }
        }
        return y;
    }

    /**
     * Multiple this matrix by a sparse array and yield a dense vector
     *
     * @param x The vector
     * @return The result
     */
    public double[] multRealDense(Vector<Double> x) {
        assert (m == x.length());
        final double[] y = new double[m];
        for (int i = 0; i < m; i++) {

            if (arr[i] == null) {
                if (defaultValue == 0.0) {
                    continue;
                } else {
                    double v = 0.0;
                    for (Map.Entry<Integer, Double> e : x.entrySet()) {
                        v += defaultValue * e.getValue();
                    }
                    y[i] = v;
                }
            } else {
                y[i] = arr[i].innerProduct(x);
            }
        }
        return y;
    }

    /**
     * Calculate AA^T where A is this matrix
     *
     * @param outerProduct The matrix to store the result in
     * @return
     */
    public void selfOuterProduct(Matrix<N> outerProduct) {
        assert (outerProduct.cols() == m);
        assert (outerProduct.rows() == m);
        for (int i = 0; i < m; i++) {
            for (Map.Entry<Integer, N> e : arr[i].entrySet()) {
                for (int j = 0; j < m; j++) {
                    final double v = arr[j].doubleValue(e.getKey()) * e.getValue().doubleValue();
                    if (v != 0.0) {
                        outerProduct.add(i, j, v);
                    }
                }
            }
        }
    }

    /**
     * Calculate A^TA where A is this matrix
     *
     * @param innerProduct The matrix to store the result in
     * @return
     */
    public void selfInnerProduct(Matrix<N> innerProduct) {
        assert (innerProduct.cols() == n);
        assert (innerProduct.rows() == n);
        for (int k = 0; k < n; k++) {
            for(Map.Entry<Integer,N> e : arr[k].entrySet()) {
                final int j = e.getKey();
                for(int i = 0; i < n; i++) {
                    final double v = arr[k].doubleValue(i) * e.getValue().doubleValue();
                    if(v != 0) {
                        innerProduct.add(i,j,v);
                    }
                }
            }
        }
    }

    /**
     * Generate the transpose of this matrix
     *
     * @param using {@link AS_INT_ARRAY} or {@link AS_REAL_ARRAY} as appropriate
     * @return The transposed matrix
     */
    @Override
    public SparseMatrix<N> transpose() {
        final SparseMatrix<N> t = new SparseMatrix<N>(n, m, defaultValue, using);
        for (int i = 0; i < m; i++) {
            if (arr[i] != null) {
                for (Map.Entry<Integer, N> e : arr[i].entrySet()) {
                    final int j = e.getKey();
                    if (t.arr[j] == null) {
                        t.arr[j] = using.make(m, defaultValue);
                    }
                    t.arr[j].put(i, e.getValue());
                }
            }
        }
        return t;
    }

    /**
     * Convert to a dense double array
     */
    public double[][] toArrays() {
        double[][] mat = new double[m][];
        for (int i = 0; i < m; i++) {
            if (arr[i] == null) {
                mat[i] = new double[n];
                if (defaultValue != 0.0) {
                    Arrays.fill(mat, defaultValue);
                }
            } else {
                mat[i] = arr[i].toDoubleArray();
            }
        }
        return mat;
    }

    /**
     * Convert a dense double array to a sparse matrix
     *
     * @param arrs The arrays, each element must be non-null and have same
     * length
     * @return The sparse matrix
     */
    public static SparseMatrix<Double> fromArray(double[][] arrs) {
        if (arrs.length == 0) {
            return new SparseMatrix<Double>(0, 0, AS_SPARSE_REALS);
        }
        final SparseMatrix<Double> mat = new SparseMatrix<Double>(arrs.length, arrs[0].length, AS_SPARSE_REALS);
        for (int i = 0; i < arrs.length; i++) {
            assert (arrs[i].length == arrs[0].length);
            for (int j = 0; j < arrs[i].length; j++) {
                if (arrs[i][j] != 0.0) {
                    if (mat.arr[i] == null) {
                        mat.arr[i] = new SparseRealArray(arrs[i].length);
                    }
                    mat.arr[i].put(j, arrs[i][j]);
                }
            }
        }
        return mat;
    }

    /**
     * Convert a dense double array to a sparse matrix
     *
     * @param arrs The arrays, each element must be non-null and have same
     * length
     * @return The sparse matrix
     */
    public static SparseMatrix<Integer> fromArray(int[][] arrs) {
        if (arrs.length == 0) {
            return new SparseMatrix<Integer>(0, 0, AS_INTS);
        }
        final SparseMatrix<Integer> mat = new SparseMatrix<Integer>(arrs.length, arrs[0].length, AS_INTS);
        for (int i = 0; i < arrs.length; i++) {
            assert (arrs[i].length == arrs[0].length);
            for (int j = 0; j < arrs[i].length; j++) {
                if (arrs[i][j] != 0.0) {
                    if (mat.arr[i] == null) {
                        mat.arr[i] = new SparseIntArray(arrs[i].length);
                    }
                    mat.arr[i].put(j, arrs[i][j]);
                }
            }
        }
        return mat;
    }

    /**
     * Get the nth row.
     *
     * @param idx The row to get
     */
    public Vector<N> row(int idx) {
        if (arr[idx] != null) {
            return arr[idx];
        } else {
            return arr[idx] = using.make(n, defaultValue);
        }
    }

    /**
     * Total number of rows
     */
    @Override
    public int rows() {
        return m;
    }

    /**
     * Total number of columns
     */
    @Override
    public int cols() {
        return n;
    }

    @Override
    public N value(int i, int j) {
        if (arr[i] == null) {
            return using.valueOf(defaultValue);
        } else {
            return arr[i].value(j);
        }
    }

    @Override
    public int intValue(int i, int j) {
        if (arr[i] == null) {
            return (int) defaultValue;
        } else {
            return arr[i].intValue(j);
        }
    }

    @Override
    public double doubleValue(int i, int j) {
        if (arr[i] == null) {
            return defaultValue;
        } else {
            return arr[i].doubleValue(j);
        }
    }

    @Override
    public void set(int i, int j, double v) {
        if (arr[i] == null) {
            arr[i] = using.make(n, defaultValue);
        }
        arr[i].put(j, using.valueOf(v));
    }

    @Override
    public void set(int i, int j, int v) {
        if (arr[i] == null) {
            arr[i] = using.make(n, defaultValue);
        }
        arr[i].put(j, using.valueOf(v));
    }

    @Override
    public void set(int i, int j, N v) {
        if (arr[i] == null) {
            arr[i] = using.make(n, defaultValue);
        }
        arr[i].put(j, v);
    }

    @Override
    public void add(int i, int j, double v) {
        if (arr[i] == null) {
            arr[i] = using.make(n, defaultValue);
            arr[i].put(j, using.valueOf(v + defaultValue));
        } else {
            arr[i].put(j, arr[i].value(j).doubleValue() + v);
        }
    }

    @Override
    public void add(int i, int j, int v) {
        if (arr[i] == null) {
            arr[i] = using.make(n, defaultValue);
            arr[i].put(j, using.valueOf(v + defaultValue));
        } else {
            arr[i].put(j, arr[i].value(j).intValue() + v);
        }
    }

    @Override
    public void add(int i, int j, N v) {
        if (arr[i] == null) {
            arr[i] = using.make(n, defaultValue);
            arr[i].put(j, v.doubleValue() + defaultValue);
        } else {
            arr[i].put(j, arr[i].value(j).doubleValue() + v.doubleValue());
        }
    }

    @Override
    public <M extends Number> void add(Matrix<M> matrix) {
        assert (m == matrix.rows());
        assert (n == matrix.cols());
        if (matrix instanceof SparseMatrix) {
            final Vector<M>[] arr2 = ((SparseMatrix<M>) matrix).arr;
            for (int i = 0; i < m; i++) {
                if (arr[i] != null && arr2 != null) {
                    arr[i].add(arr2[i]);
                } else if (arr2 != null) {
                    arr[i] = using.make(n, defaultValue);
                    arr[i].add(arr2[i]);
                }
            }
        } else {
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    row(i).add(j, matrix.doubleValue(i, j));
                }
            }
        }
    }

    @Override
    public <M extends Number> void sub(Matrix<M> matrix) {
        assert (m == matrix.rows());
        assert (n == matrix.cols());
        if (matrix instanceof SparseMatrix) {
            final Vector<M>[] arr2 = ((SparseMatrix<M>) matrix).arr;
            for (int i = 0; i < m; i++) {
                if (arr[i] != null && arr2 != null) {
                    arr[i].sub(arr2[i]);
                } else if (arr2 != null) {
                    arr[i] = using.make(n, defaultValue);
                    arr[i].sub(arr2[i]);
                }
            }
        } else {
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    row(i).sub(j, matrix.doubleValue(i, j));
                }
            }
        }
    }

    @Override
    public boolean isSymmetric() {
        if (m != n) {
            return false;
        }
        for (int i = 0; i < m; i++) {
            if (arr[i] == null) {
                continue;
            }
            for (Map.Entry<Integer, N> e : arr[i].entrySet()) {
                if (arr[e.getKey()] == null) {
                    return false;
                }
                if (arr[e.getKey()].value(i).doubleValue() != e.getValue().doubleValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public VectorFunction<N,N> asVectorFunction() {
        return new VectorFunction<N,N>() {

            @Override
            public Vector<N> apply(Vector<N> v) {
                return mult(v);
            }
        };
    }

    @Override
    public Factory<N> factory() {
        return using;
    }

    public static <N extends Number> SparseMatrix<N> fromFile(File file, Vectors.Factory<N> using) throws IOException, VectorFormatException {
        final BufferedReader in = new BufferedReader(new FileReader(file));
        String s;

        final int m = Integer.parseInt(in.readLine());
        final int n = Integer.parseInt(in.readLine());
        @SuppressWarnings("unchecked")
        final Vector<N>[] vectors = (Vector<N>[]) new Vector<?>[m];
        int idx = 0;
        while ((s = in.readLine()) != null) {
            if (idx >= m) {
                throw new VectorFormatException("Too many lines");
            }
            vectors[idx++] = using.fromString(s, n);
        }
        return new SparseMatrix<N>(n, vectors, using);
    }

    public void toFile(File file) throws IOException {
        final PrintWriter out = new PrintWriter(file);
        out.println(m);
        out.println(n);
        for (int i = 0; i < m; i++) {
            if (arr[i] == null) {
                out.println(using.make(n, defaultValue).toString());
            } else {
                out.println(arr[i].toString());
            }
        }
        out.flush();
        out.close();
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (arr.length == 0 || !(arr[0] instanceof SparseIntArray)) {
            using = (Factory<N>) AS_SPARSE_REALS;
        } else {
            using = (Factory<N>) AS_SPARSE_INTS;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final SparseMatrix<N> other = (SparseMatrix<N>) obj;
        if (!Arrays.deepEquals(this.arr, other.arr)) {
            return false;
        }
        if (this.m != other.m) {
            return false;
        }
        if (this.n != other.n) {
            return false;
        }
        if (Double.doubleToLongBits(this.defaultValue) != Double.doubleToLongBits(other.defaultValue)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Arrays.deepHashCode(this.arr);
        hash = 97 * hash + this.m;
        hash = 97 * hash + this.n;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.defaultValue) ^ (Double.doubleToLongBits(this.defaultValue) >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "SparseMatrix{" + "arr=" + Arrays.toString(arr) + '}';
    }
    
    
}
