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
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class SparseIntArray extends Int2IntOpenHashMap implements Vector<Integer> {

    private static final long serialVersionUID = 9099860117350068663L;
    private final int n;
    private int defaultValue;

    /**
     * Create a new sparse integer array of length n
     *
     * @param n
     */
    public SparseIntArray(int n) {
        this.n = n;
        this.defaultValue = 0;
    }

    /**
     * Create a new sparse integer array of length n
     *
     * @param n The length of the array
     * @param defaultValue The default value (i.e., the value of sparse nodes)
     */
    public SparseIntArray(int n, int defaultValue) {
        this.n = n;
        this.defaultValue = super.defRetValue = defaultValue;
    }

    /**
     * Create a new sparse integer array of length n
     *
     * @param n The length of the array
     * @param defaultValue The default value (e.g., 0.0)
     * @param values The values as key,value pairs
     * @throws IllegalArgumentException If the length of values is not a
     * multiple of two
     */
    public SparseIntArray(int n, int defaultValue, int... values) {
        this.n = n;
        this.defaultValue = super.defRetValue = defaultValue;
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Wrong number of varargs to SparseIntArray");
        }
        for (int i = 0; i < values.length; i += 2) {
            put(values[i], values[i + 1]);
        }
    }

    protected SparseIntArray(int n, int defaultValue, Int2IntOpenHashMap map) {
        super(map);
        this.n = n;
        this.defaultValue = super.defRetValue = defaultValue;
    }

    @Override
    public Integer get(Object o) {
        final Integer rval = super.get(o);
        if (rval == null) {
            return defaultValue;
        } else {
            return rval;
        }
    }

    @Override
    public Integer value(int idx) {
        return get(idx);
    }

    @Override
    public double put(int idx, double value) {
        return super.put(idx, idx);
    }

    @Override
    public int put(int idx, int value) {
        return super.put(idx, value);
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
        for (Map.Entry<Integer, Integer> entry : entrySet()) {
            sb.append(",").append(entry.getKey().toString()).append("=").append(entry.getValue().toString());
        }
        return sb.substring(1);
    }

    public static SparseIntArray fromFrequencyString(String string, int n) throws VectorFormatException {
        final String[] entries = string.split("\\s+");
        final SparseIntArray array = new SparseIntArray(n);
        for (String entry : entries) {
            try {
                array.inc(Integer.parseInt(entry));
            } catch (NumberFormatException x) {
                throw new VectorFormatException(x);
            }
        }
        return array;
    }

    /**
     * Create from a string of the form "idx=val,...,idx=val"
     *
     * @param string The string
     * @param n The length of the array
     * @return The array object
     * @throws SparseIntArrayFormatException If the string is not formatted as a
     * sparse array
     */
    public static SparseIntArray fromString(String string, int n) throws VectorFormatException {
        return fromString(string, n, 0, ",");
    }

    /**
     * Create from a string of the form "idx=val,...,idx=val"
     *
     * @param string The string
     * @param n The length of the array
     * @param defaultValue The default value
     * @return The array object
     * @throws SparseIntArrayFormatException If the string is not formatted as a
     * sparse array
     */
    public static SparseIntArray fromString(String string, int length, int defaultValue) throws VectorFormatException {
        return fromString(string, length, defaultValue, ",");
    }

    /**
     * Create from a string of the form "idx=val,...,idx=val"
     *
     * @param string The string
     * @param n The length of the array
     * @param defaultValue The default value
     * @param sep The seperator
     * @return The array object
     * @throws SparseIntArrayFormatException If the string is not formatted as a
     * sparse array
     */
    public static SparseIntArray fromString(String string, int length, int defaultValue, String sep) throws VectorFormatException {
        String[] entries = string.split(sep);
        final SparseIntArray SparseIntArray = new SparseIntArray(length);
        for (String entry : entries) {
            if (entry.matches("\\s*")) {
                continue;
            }
            String[] values = entry.split("=");
            if (values.length != 2) {
                throw new VectorFormatException("Bad sparse array value " + entry);
            }
            SparseIntArray.put(new Integer(values[0]), new Integer(values[1]));
        }
        return SparseIntArray;
    }

    /**
     * Convert to a dense array
     */
    public int[] toArray() {
        int[] arr = new int[n];
        if (defaultValue != 0) {
            Arrays.fill(arr, defaultValue);
        }
        for (Map.Entry<Integer, Integer> entry : entrySet()) {
            arr[entry.getKey()] = entry.getValue();
        }
        return arr;
    }

    @Override
    public double[] toDoubleArray() {
        double[] d = new double[n];
        if (defaultValue != 0) {
            Arrays.fill(d, defaultValue);
        }
        for (int i : this.keySet()) {
            d[i] = this.get(i);
        }
        return d;
    }

    /**
     * Convert to a sparse real array
     */
    public SparseRealArray toRealArray() {
        final SparseRealArray sparseRealArray = new SparseRealArray(n, defaultValue, 0.0);
        for (Map.Entry<Integer, Integer> entry : entrySet()) {
            sparseRealArray.put(entry.getKey(), entry.getValue());
        }
        return sparseRealArray;
    }

    /**
     * Convert a dense array to a sparse array
     */
    public static SparseIntArray fromArray(int[] arr) {
        SparseIntArray sa = new SparseIntArray(arr.length);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0) {
                sa.put(i, arr[i]);
            }
        }
        return sa;
    }

    public final int n() {
        return n;
    }

    @Override
    public Integer sum() {
        int sum = defaultValue * n;
        for (int v : values()) {
            sum += v - defaultValue;
        }
        return sum;
    }

    @Override
    public double norm() {
        double norm = 0.0;
        for (int v : values()) {
            norm += v * v;
        }
        return Math.sqrt(norm);
    }

    @Override
    public double add(int idx, double val) {
        return super.add(idx, (int) val);
    }

    @Override
    public void sub(int idx, int i) {
        add(idx, -i);
    }

    @Override
    public void divide(int idx, int val) {
        multiply(idx, 1 / val);
    }

    @Override
    public void multiply(int idx, int i) {
        if (this.containsKey(idx)) {
            final int val = super.get(idx);
            if (val * i == defaultValue) {
                super.remove(idx);
            } else {
                super.put(idx, val * i);
            }
        } else {
            if (defaultValue != 0 && i != defaultValue) {
                super.put(idx, defaultValue * i);
            }
        }
    }

    public static SparseIntArray histogram(int[] vector, int W) {
        final SparseIntArray hist = new SparseIntArray(W);
        for (int i : vector) {
            hist.inc(i);
        }
        return hist;
    }

    public static SparseIntArray fromBinary(File file, int W) throws IOException {
        return fromBinary(new FileInputStream(file), W);
    }

    public static SparseIntArray fromBinary(InputStream stream, int W) throws IOException {
        final DataInputStream dis = new DataInputStream(stream);
        final SparseIntArray arr = new SparseIntArray(W);
        while (dis.available() > 0) {
            try {
                arr.inc(dis.readInt());
            } catch (EOFException x) {
                break;
            }
        }
        return arr;
    }

    @Override
    public void sub(int idx, double i) {
        add(idx, -i);
    }

    @Override
    public void divide(int idx, double val) {
        multiply(idx, 1 / val);
    }

    @Override
    public void multiply(int idx, double i) {
        if (this.containsKey(idx)) {
            final int val = super.get(idx);
            if (val * i == defaultValue) {
                super.remove(idx);
            } else {
                super.put(idx, (int) (val * i));
            }
        } else {
            if (defaultValue != 0.0 && i != defaultValue) {
                super.put(idx, (int) (defaultValue * i));
            }
        }
    }

    @Override
    public int intValue(int idx) {
        final Integer val = super.get(idx);
        return val == null ? defaultValue : val.intValue();
    }

    @Override
    public double doubleValue(int idx) {
        final Integer val = super.get(idx);
        return val == null ? (double) defaultValue : val.doubleValue();
    }

    @Override
    public int length() {
        return n;
    }

    @Override
    public <M extends Number> void add(Vector<M> vector) {
        assert (vector.length() == n);
        for (Map.Entry<Integer, M> e : vector.entrySet()) {
            add(e.getKey(), e.getValue().intValue());
        }
    }

    @Override
    public <M extends Number> void sub(Vector<M> vector) {
        assert (vector.length() == n);
        for (Map.Entry<Integer, M> e : vector.entrySet()) {
            sub(e.getKey(), e.getValue().intValue());
        }
    }

    @Override
    public void multiply(double n) {
        for (Map.Entry<Integer, Integer> e : entrySet()) {
            e.setValue((int) (e.getValue() * n));
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
                for (Map.Entry<Integer, Integer> e : entrySet()) {
                    innerProduct += y2[e.getKey()] * e.getValue();
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
            int[] y2 = ((IntVector) y).data();
            if (defaultValue == 0) {
                int innerProduct = 0;
                for (Map.Entry<Integer, Integer> e : entrySet()) {
                    innerProduct += e.getValue() * y2[e.getKey()];
                }
                return innerProduct;
            } else {
                int innerProduct = 0;
                for (int i = 0; i < n; i++) {
                    innerProduct += y2[i] * get(i);
                }
                return innerProduct;
            }
        } else {
            if (defaultValue == 0 || y.defaultValue().intValue() == 0) {
                int innerProduct = 0;
                if (this.size() <= y.size()) {
                    for (Map.Entry<Integer, Integer> e : entrySet()) {
                        innerProduct += e.getValue() * y.intValue(e.getKey());
                    }
                    return innerProduct;
                } else {
                    for (Map.Entry<Integer, M> e : y.entrySet()) {
                        innerProduct += e.getValue().intValue() * this.intValue(e.getKey());
                    }
                    return innerProduct;
                }
            } else {
                int innerProduct = 0;
                int notBothSparse = 0;
                for (Map.Entry<Integer, Integer> e : entrySet()) {
                    innerProduct += e.getValue() * y.intValue(e.getKey());
                    notBothSparse++;
                }
                for (Map.Entry<Integer, M> e : y.entrySet()) {
                    if (!super.containsKey(e.getKey())) {
                        innerProduct += defaultValue * e.getValue().intValue();
                        notBothSparse++;
                    }
                }
                return innerProduct + (n - notBothSparse) * defaultValue * y.defaultValue().intValue();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M extends Number, O extends Number> Matrix<O> outerProduct(Vector<M> y, Vectors.Factory<O> using) {
        if (using == Vectors.AS_INTS) {
            int[][] data2 = new int[n][y.length()];
            for (Map.Entry<Integer, Integer> e : entrySet()) {
                for (int j = 0; j < y.length(); j++) {
                    data2[e.getKey()][j] = e.getValue().intValue() * y.intValue(j);
                }
            }
            return (Matrix<O>) new IntArrayMatrix(data2);
        } else if (using == Vectors.AS_REALS) {
            double[][] data2 = new double[n][y.length()];
            for (Map.Entry<Integer, Integer> e : entrySet()) {
                for (int j = 0; j < y.length(); j++) {
                    data2[e.getKey()][j] = y.doubleValue(j) * e.getValue().intValue();
                }
            }
            return (Matrix<O>) new DoubleArrayMatrix(data2);
        } else {
            final SparseMatrix<O> matrix = new SparseMatrix<O>(n, y.length(), using);
            for (Map.Entry<Integer, Integer> e : entrySet()) {
                for (Map.Entry<Integer, M> e2 : y.entrySet()) {
                    matrix.set(e.getKey(), e2.getKey(), e2.getValue().doubleValue() * e.getKey().doubleValue());
                }
            }
            return matrix;
        }
    }

    @Override
    public Factory<Integer> factory() {
        return Vectors.AS_SPARSE_INTS;
    }

    @Override
    public Integer defaultValue() {
        return defaultValue;
    }

    @Override
    public SparseIntArray clone() {
        return new SparseIntArray(n, defaultValue, this);
    }
}
