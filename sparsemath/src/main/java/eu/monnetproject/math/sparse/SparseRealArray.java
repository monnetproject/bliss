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
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a sparse array of real values
 *
 * @author John McCrae
 */
public class SparseRealArray extends Int2DoubleOpenHashMap implements Vector<Double> {

    private static final long serialVersionUID = 8976723557456415580L;
    private double defaultValue;
    private final double epsilon;
    private final int n;

    /**
     * Create a sparse real array of length n
     *
     * @param n The length of the array
     */
    public SparseRealArray(int n) {
        this.n = n;
        this.defaultValue = 0;
        this.epsilon = 1e-8;
    }

    /**
     * Create a sparse real array of length n, a given defaultValue and a error
     *
     * @param n The length of the array
     * @param defaultValue The value of sparse indexes
     * @param epsilon The minimum error: x[i] is sparse if |x[i] - defaultValue|
     * &lt; epsilon
     */
    public SparseRealArray(int n, double defaultValue, double epsilon) {
        this.n = n;
        this.defaultValue = super.defRetValue = defaultValue;
        this.epsilon = epsilon;
    }

    protected SparseRealArray(int n, double defaultValue, double epsilon, Int2DoubleOpenHashMap map) {
        super(map);
        this.n = n;
        this.defaultValue = super.defRetValue = defaultValue;
        this.epsilon = epsilon;
    }

    @Override
    public Double get(Object o) {
        final Double rval = super.get(o);
        if (rval == null) {
            return defaultValue;
        } else {
            return rval;
        }
    }

    @Override
    public Double value(int idx) {
        return get(idx);
    }

    @Override
    public double put(int idx, double value) {
        return super.put(idx, value);
    }

    @Override
    public int put(int idx, int value) {
        return (int) super.put(idx, value);
    }

    /**
     * Increment an index
     */
    public void inc(int idx) {
        add(idx, 1);
    }

    /**
     * Decrement an index
     */
    public void dec(int idx) {
        add(idx, -1);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Double> entry : entrySet()) {
            sb.append(",").append(entry.getKey().toString()).append("=").append(entry.getValue().toString());
        }
        return sb.substring(1);
    }

    /**
     * Create from a string of the form "idx=val,...,idx=val"
     *
     * @param string The string
     * @param n The length of the array
     * @return The array object
     * @throws SparseArrayFormatException If the string is not formatted as a
     * sparse array
     */
    public static SparseRealArray fromString(String string, int n) throws VectorFormatException {
        return fromString(string, n, 0.0, ",");
    }

    /**
     * Create from a string of the form "idx=val,...,idx=val"
     *
     * @param string The string
     * @param n The length of the array
     * @param defaultValue The default value
     * @return The array object
     * @throws SparseArrayFormatException If the string is not formatted as a
     * sparse array
     */
    public static SparseRealArray fromString(String string, int n, double defaultValue) throws VectorFormatException {
        return fromString(string, n, defaultValue, ",");
    }

    /**
     * Create from a string of the form "idx=val,...,idx=val"
     *
     * @param string The string
     * @param n The length of the array
     * @param defaultValue The default value
     * @param sep The seperator
     * @return The array object
     * @throws SparseArrayFormatException If the string is not formatted as a
     * sparse array
     */
    public static SparseRealArray fromString(String string, int n, double defaultValue, String sep) throws VectorFormatException {
        String[] entries = string.split(sep);
        final SparseRealArray SparseRealArray = new SparseRealArray(n);
        for (String entry : entries) {
            if (entry.matches("\\s*")) {
                continue;
            }
            String[] values = entry.split("=");
            if (values.length != 2) {
                throw new VectorFormatException("Bad sparse array value " + entry);
            }
            try {
                SparseRealArray.put(new Integer(values[0]), new Double(values[1]));
            } catch (NumberFormatException x) {
                throw new VectorFormatException(x);
            }
        }
        return SparseRealArray;
    }

    /**
     * Convert to a dense array
     */
    public double[] toArray() {
        double[] arr = new double[n];
        for (Map.Entry<Integer, Double> entry : entrySet()) {
            arr[entry.getKey()] = entry.getValue();
        }
        return arr;
    }

    @Override
    public double[] toDoubleArray() {
        return toArray();
    }

    /**
     * Create a sparse array from a dense array
     *
     * @param arr The dense array
     * @return The sparse array, with zeroes removed
     */
    public static SparseRealArray fromArray(double[] arr) {
        SparseRealArray sa = new SparseRealArray(arr.length);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0) {
                sa.put(i, arr[i]);
            }
        }
        return sa;
    }

    /**
     * Compute the sum of the array
     *
     * @return
     */
    public Double sum() {
        double sum = n * defaultValue;
        for (double v : values()) {
            sum += v - defaultValue;
        }
        return sum;
    }

    @Override
    public double norm() {
        double norm = 0.0;
        for (double v : values()) {
            norm += v * v;
        }
        return Math.sqrt(norm);
    }

    /**
     * Subtract a value from a given index
     *
     * @param idx The index
     * @param i The value
     */
    @Override
    public void sub(int idx, double i) {
        add(idx, -i);
    }

    /**
     * Divide a value at a given index
     *
     * @param idx The index
     * @param val The value
     */
    @Override
    public void divide(int idx, double val) {
        multiply(idx, 1.0 / val);
    }

    /**
     * Multiple a value at a given index
     *
     * @param idx The index
     * @param i The value
     */
    @Override
    public void multiply(int idx, double i) {
        if (this.containsKey(idx)) {
            final double val = super.get(idx);
            if (Math.abs(val * i - defaultValue) <= epsilon) {
                super.remove(idx);
            } else {
                super.put(idx, val * i);
            }
        } else {
            if (defaultValue != 0.0 && i != 0.0) {
                super.put(idx, defaultValue * i);
            }
        }
    }

    @Override
    public int add(int idx, int val) {
        return (int) add(idx, (double) val);
    }

    @Override
    public void sub(int idx, int val) {
        add(idx, (double) -val);
    }

    @Override
    public void multiply(int idx, int val) {
        multiply(idx, (double) val);
    }

    @Override
    public void divide(int idx, int val) {
        multiply(idx, 1.0 / val);
    }

    @Override
    public int intValue(int idx) {
        final Double val = super.get(idx);
        return val == null ? (int) defaultValue : val.intValue();
    }

    @Override
    public double doubleValue(int idx) {
        final Double val = super.get(idx);
        return val == null ? defaultValue : val.doubleValue();
    }

    @Override
    public int length() {
        return n;
    }

    @Override
    public <M extends Number> void add(Vector<M> vector) {
        assert (vector.length() == n);
        for (Map.Entry<Integer, M> e : vector.entrySet()) {
            add(e.getKey(), e.getValue().doubleValue());
        }
    }

    @Override
    public <M extends Number> void sub(Vector<M> vector) {
        assert (vector.length() == n);
        for (Map.Entry<Integer, M> e : vector.entrySet()) {
            sub(e.getKey(), e.getValue().doubleValue());
        }
    }

    @Override
    public void multiply(double n) {
        for (Map.Entry<Integer, Double> e : entrySet()) {
            e.setValue(e.getValue() * n);
        }
        defaultValue *= n;
    }

    @Override
    public <M extends Number> double innerProduct(Vector<M> y) {
        assert (n == y.length());
        if (y instanceof RealVector) {
            double[] y2 = ((RealVector) y).data();
            if (defaultValue == 0) {
                double innerProduct = 0;
                for (Map.Entry<Integer, Double> e : entrySet()) {
                    innerProduct += e.getValue().doubleValue() * y2[e.getKey()];
                }
                return innerProduct;
            } else {
                double innerProduct = 0;
                for (int i = 0; i < n; i++) {
                    innerProduct += y2[i] * get(i);
                }
                return innerProduct;
            }

        } else if (y instanceof IntVector) {
            final int[] y2 = ((IntVector) y).data();
            if (defaultValue == 0) {
                double innerProduct = 0;
                for (Map.Entry<Integer, Double> e : entrySet()) {
                    innerProduct += e.getValue().doubleValue() * y2[e.getKey()];
                }
                return innerProduct;
            } else {
                double innerProduct = 0;
                for (int i = 0; i < n; i++) {
                    innerProduct += y2[i] * get(i);
                }
                return innerProduct;
            }
        } else {
            if (defaultValue == 0.0 || y.defaultValue().doubleValue() == 0.0) {
                double innerProduct = 0.0;
                if (this.size() <= y.size()) {
                    for (Map.Entry<Integer, Double> e : entrySet()) {
                        innerProduct += e.getValue().doubleValue() * y.doubleValue(e.getKey());
                    }
                    return innerProduct;
                } else {
                    for (Map.Entry<Integer, M> e : y.entrySet()) {
                        innerProduct += e.getValue().doubleValue() * this.doubleValue(e.getKey());
                    }
                    return innerProduct;
                }
            } else {
                double innerProduct = 0;
                int notBothSparse = 0;
                for (Map.Entry<Integer, Double> e : entrySet()) {
                    innerProduct += e.getValue().doubleValue() * y.doubleValue(e.getKey());
                    notBothSparse++;
                }
                for (Map.Entry<Integer, M> e : y.entrySet()) {
                    if (!super.containsKey(e.getKey())) {
                        innerProduct += defaultValue * e.getValue().doubleValue();
                        notBothSparse++;
                    }
                }
                return innerProduct + (n - notBothSparse) * defaultValue * y.defaultValue().doubleValue();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M extends Number, O extends Number> Matrix<O> outerProduct(Vector<M> y, Vectors.Factory<O> using) {
        if (using == Vectors.AS_INTS) {
            int[][] data2 = new int[n][y.length()];
            for (Map.Entry<Integer, Double> e : entrySet()) {
                for (int j = 0; j < y.length(); j++) {
                    data2[e.getKey()][j] = (int) (e.getValue().doubleValue() * y.doubleValue(j));
                }
            }
            return (Matrix<O>) new IntArrayMatrix(data2);
        } else if (using == Vectors.AS_REALS) {
            double[][] data2 = new double[n][y.length()];
            for (Map.Entry<Integer, Double> e : entrySet()) {
                for (int j = 0; j < y.length(); j++) {
                    data2[e.getKey()][j] = y.doubleValue(j) * e.getValue().doubleValue();
                }
            }
            return (Matrix<O>) new DoubleArrayMatrix(data2);
        } else {
            final SparseMatrix<O> matrix = new SparseMatrix<O>(n, y.length(), using);
            for (Map.Entry<Integer, Double> e : entrySet()) {
                for (Map.Entry<Integer, M> e2 : y.entrySet()) {
                    matrix.set(e.getKey(), e2.getKey(), e2.getValue().doubleValue() * e.getKey().doubleValue());
                }
            }
            return matrix;
        }
    }

    @Override
    public Double defaultValue() {
        return defaultValue;
    }

    @Override
    public SparseRealArray clone() {
        return new SparseRealArray(n, defaultValue, epsilon, this);
    }

    @Override
    public Factory<Double> factory() {
        return Vectors.AS_SPARSE_REALS;
    }
}