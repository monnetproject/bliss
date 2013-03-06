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
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 * @author John McCrae
 */
public class IntVector implements Vector<Integer> {

    private final int[] data;

    public IntVector(int n) {
        this.data = new int[n];
    }

    public IntVector(int[] data) {
        this.data = data;
    }

    public int[] data() {
        return data;
    }
    
    public static IntVector make(int... data) {
        return new IntVector(data);
    }

    @Override
    public double doubleValue(int idx) {
        return data[idx];
    }

    @Override
    public int intValue(int idx) {
        return data[idx];
    }

    @Override
    public Integer value(int idx) {
        return data[idx];
    }

    @Override
    public Integer put(Integer idx, Integer n) {
        final int rval = data[idx];
        data[idx] = n.intValue();
        return rval;
    }

    @Override
    public double put(int idx, double value) {
        double r = data[idx];
        data[idx] = (int) value;
        return r;
    }

    @Override
    public int put(int idx, int value) {
        int r = data[idx];
        data[idx] = value;
        return r;
    }

    @Override
    public int add(int idx, int val) {
        int r = data[idx];
        data[idx] += val;
        return r;
    }

    @Override
    public void sub(int idx, int val) {
        data[idx] -= val;
    }

    @Override
    public void multiply(int idx, int val) {
        data[idx] *= val;
    }

    @Override
    public void divide(int idx, int val) {
        data[idx] /= val;
    }

    @Override
    public double add(int idx, double val) {
        double r = data[idx];
        data[idx] += val;
        return r;
    }

    @Override
    public void sub(int idx, double val) {
        data[idx] -= val;
    }

    @Override
    public void multiply(int idx, double val) {
        data[idx] *= val;
    }

    @Override
    public void divide(int idx, double val) {
        data[idx] /= val;
    }

    @Override
    public <M extends Number> void add(Vector<M> vector) {
        assert (vector.length() == data.length);
        if (vector instanceof IntVector) {
            final int[] data2 = ((IntVector) vector).data;
            for (int i = 0; i < data.length; i++) {
                data[i] += data2[i];
            }
        } else {
            for (Map.Entry<Integer, M> e : vector.entrySet()) {
                data[e.getKey()] += e.getValue().intValue();
            }
        }
    }

    @Override
    public <M extends Number> void sub(Vector<M> vector) {
        assert (vector.length() == data.length);
        if (vector instanceof IntVector) {
            final int[] data2 = ((IntVector) vector).data;
            for (int i = 0; i < data.length; i++) {
                data[i] -= data2[i];
            }
        } else {
            for (Map.Entry<Integer, M> e : vector.entrySet()) {
                data[e.getKey()] -= e.getValue().intValue();
            }
        }
    }

    @Override
    public void multiply(double n) {
        for (int i = 0; i < data.length; i++) {
            data[i] *= n;
        }
    }

    @Override
    public <M extends Number> double innerProduct(Vector<M> y) {
        assert (y.length() == data.length);
        if (y instanceof IntVector) {
            final IntVector y2 = (IntVector) y;
            int innerProduct = 0;
            for (int i = 0; i < data.length; i++) {
                innerProduct += data[i] * y2.data[i];
            }
            return innerProduct;
        } else if (y.defaultValue().doubleValue() == 0.0) {
            double innerProduct = 0.0;
            for (Map.Entry<Integer, M> e : y.entrySet()) {
                innerProduct += data[e.getKey()] * e.getValue().doubleValue();
            }
            return innerProduct;
        } else {
            double innerProduct = 0.0;
            for (int i = 0; i < data.length; i++) {
                innerProduct += data[i] * y.doubleValue(i);
            }
            return innerProduct;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M extends Number, O extends Number> Matrix<O> outerProduct(Vector<M> y, Factory<O> using) {
        if (using == Vectors.AS_INTS) {
            int[][] data2 = new int[data.length][y.length()];
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < y.length(); j++) {
                    data2[i][j] = data[i] * y.intValue(j);
                }
            }
            return (Matrix<O>) new IntArrayMatrix(data2);
        } else if (using == Vectors.AS_REALS) {
            double[][] data2 = new double[data.length][y.length()];
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < y.length(); j++) {
                    data2[i][j] = y.doubleValue(j) * data[i];
                }
            }
            return (Matrix<O>) new DoubleArrayMatrix(data2);
        } else {
            final SparseMatrix<O> matrix = new SparseMatrix<O>(data.length, y.length(), using);
            for (int i = 0; i < data.length; i++) {
                for (Map.Entry<Integer, M> e : y.entrySet()) {
                    matrix.set(i, e.getKey(), e.getValue().doubleValue() * data[i]);
                }
            }
            return matrix;
        }
    }

    @Override
    public Set<Entry<Integer, Integer>> entrySet() {
        return new IntArraySet();
    }

    @Override
    public double[] toDoubleArray() {
        double[] dataDouble = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            dataDouble[i] = data[i];
        }
        return dataDouble;
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0) {
                size++;
            }
        }
        return size;
    }

    @Override
    public double norm() {
        double norm = 0.0;
        for (int i = 0; i < data.length; i++) {
            norm += data[i] * data[i];
        }
        return Math.sqrt(norm);
    }

    @Override
    public Integer defaultValue() {
        return 0;
    }

    @Override
    public Factory<Integer> factory() {
        return Vectors.AS_INTS;
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(i).append("=").append(data[i]);
            }
        }
        return sb.toString();
    }

    public static IntVector fromString(String s, int n) throws VectorFormatException {
        final int[] data = new int[n];
        final String[] ss = s.split(",");
        if (ss.length == n) {
            for (int i = 0; i < n; i++) {
                if (ss[i].contains("=")) {
                    final String[] sss = ss[i].split("=");
                    if (sss.length != 2) {
                        throw new VectorFormatException("Too many =s: " + ss[i]);
                    }
                    try {
                        data[Integer.parseInt(sss[0].replaceAll("\\[\\]\\s", ""))] = Integer.parseInt(sss[1].replaceAll("\\[\\]\\s", ""));
                    } catch (NumberFormatException x) {
                        throw new VectorFormatException(x);
                    }
                } else {
                    try {
                        data[i] = Integer.parseInt(ss[i].replaceAll("\\[\\]\\s", ""));
                    } catch (NumberFormatException x) {
                        throw new VectorFormatException(x);
                    }
                }
            }
        } else if (ss.length < n) {
            for (int i = 0; i < n; i++) {
                final String[] sss = ss[i].split("=");
                if (sss.length != 2) {
                    throw new VectorFormatException("Too many or too few =s: " + ss[i]);
                }
                try {
                    data[Integer.parseInt(sss[0].replaceAll("\\[\\]\\s", ""))] = Integer.parseInt(sss[1].replaceAll("\\[\\]\\s", ""));
                } catch (NumberFormatException x) {
                    throw new VectorFormatException(x);
                }
            }
        } else {
            throw new VectorFormatException("Real vector too long");
        }
        return new IntVector(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntVector other = (IntVector) obj;
        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Arrays.hashCode(this.data);
        return hash;
    }

    @Override
    public Vector<Integer> clone() {
        return new IntVector(Arrays.copyOf(data, data.length));
    }

    private class IntArraySet extends AbstractSet<Map.Entry<Integer, Integer>> {

        @Override
        public Iterator<Map.Entry<Integer, Integer>> iterator() {
            return new Iterator<Entry<Integer, Integer>>() {
                int n = 0;

                @Override
                public boolean hasNext() {
                    return n < data.length;
                }

                @Override
                public Entry<Integer, Integer> next() {
                    if (n < data.length) {
                        final int m = n++;
                        return new Map.Entry<Integer, Integer>() {
                            @Override
                            public Integer getKey() {
                                return m;
                            }

                            @Override
                            public Integer getValue() {
                                return data[m];
                            }

                            @Override
                            public Integer setValue(Integer value) {
                                final int old = data[m];
                                data[m] = value.intValue();
                                return old;
                            }
                        };
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not supported.");
                }
            };
        }

        @Override
        public int size() {
            return data.length;
        }
    }

    @Override
    public IntSet keySet() {
        return new IntStreamSet(size());
    }

    private static class IntStreamSet extends AbstractIntSet {

        private final int N;

        public IntStreamSet(int N) {
            this.N = N;
        }

        @Override
        public IntIterator iterator() {
            return new IntIterator() {
                private int n;

                @Override
                public boolean hasNext() {
                    return n < N;
                }

                @Override
                public Integer next() {
                    return n++;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not mutable.");
                }

                @Override
                public int nextInt() {
                    return n++;
                }

                @Override
                public int skip(int m) {
                    int rval = Math.min(N - n, m);
                    n += m;
                    return rval;
                }
            };
        }

        @Override
        public int size() {
            return N;
        }
    }

    @Override
    public boolean containsKey(int idx) {
        return idx >= 0 && idx < length();
    }

    @Override
    public Integer sum() {
        int i = 0;
        for (int j : data) {
            i += j;
        }
        return i;
    }
}
