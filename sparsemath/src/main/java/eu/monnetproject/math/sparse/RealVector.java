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
public class RealVector implements Vector<Double> {

    private final double[] data;

    public RealVector(int n) {
        this.data = new double[n];
    }

    public RealVector(double[] data) {
        this.data = data;
    }

    public double[] data() {
        return data;
    }

    @Override
    public double doubleValue(int idx) {
        return data[idx];
    }

    @Override
    public int intValue(int idx) {
        return (int) data[idx];
    }

    @Override
    public Double value(int idx) {
        return data[idx];
    }

    @Override
    public Double put(Integer idx, Double n) {
        final double rval = data[idx];
        data[idx] = n.doubleValue();
        return rval;
    }

    @Override
    public void put(int idx, double value) {
        data[idx] = value;
    }

    @Override
    public void put(int idx, int value) {
        data[idx] = value;
    }

    @Override
    public void add(int idx, int val) {
        data[idx] += val;
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
    public void add(int idx, double val) {
        data[idx] += val;
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
        assert(vector.length() == data.length);
        if(vector instanceof RealVector) {
            final double[] data2 = ((RealVector)vector).data;
            for(int i = 0; i < data.length; i++) {
                data[i] += data2[i];
            }
        } else {
            for(Map.Entry<Integer,M> e : vector.entrySet()) {
                data[e.getKey()] += e.getValue().doubleValue();
            }
        }
    }

    @Override
    public <M extends Number> void sub(Vector<M> vector) {
        assert(vector.length() == data.length);
        if(vector instanceof RealVector) {
            final double[] data2 = ((RealVector)vector).data;
            for(int i = 0; i < data.length; i++) {
                data[i] -= data2[i];
            }
        } else {
            for(Map.Entry<Integer,M> e : vector.entrySet()) {
                data[e.getKey()] -= e.getValue().doubleValue();
            }
        }
    }
    
    
    @Override
    public void multiply(double n) {
        for(int i = 0; i < data.length; i++) {
            data[i] *= n;
        }
    }
    
    @Override
    public <M extends Number> double innerProduct(Vector<M> y) {
        assert (y.length() == data.length);
        if (y instanceof RealVector) {
            final RealVector y2 = (RealVector) y;
            double innerProduct = 0.0;
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
        if(using == Vectors.AS_INTS) {
            int[][] data2 = new int[data.length][y.length()];
            for(int i = 0; i < data.length; i++) {
                for(int j = 0; j < y.length(); j++) {
                    data2[i][j] = (int)(data[i] * y.intValue(j));
                }
            }
            return (Matrix<O>)new IntArrayMatrix(data2);
        } else if(using == Vectors.AS_REALS) {
            double[][] data2 = new double[data.length][y.length()];
            for(int i = 0; i < data.length; i++) {
                for(int j = 0; j < y.length(); j++) {
                    data2[i][j] = y.doubleValue(j) * data[i];
                }
            }
            return (Matrix<O>)new DoubleArrayMatrix(data2);
        } else  {
            final SparseMatrix<O> matrix = new SparseMatrix<O>(data.length, y.length(), using);
            for(int i = 0; i < data.length; i++) {
                for(Map.Entry<Integer,M> e : y.entrySet()) {
                    matrix.set(i, e.getKey(), e.getValue().doubleValue() * data[i]);
                }
            }
            return matrix;
        } 
    }

    @Override
    public Set<Entry<Integer, Double>> entrySet() {
        return new DoubleArraySet();
    }

    @Override
    public double[] toDoubleArray() {
        return data;
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0.0) {
                size++;
            }
        }
        return size;
    }
    
    @Override
    public double norm() {
        double norm = 0.0;
        for(int i = 0; i < data.length; i++) {
            norm += data[i] * data[i];
        }
        return Math.sqrt(norm);
    }

    @Override
    public Double defaultValue() {
        return 0.0;
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public Factory<Double> factory() {
        return Vectors.AS_REALS;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RealVector other = (RealVector) obj;
        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Arrays.hashCode(this.data);
        return hash;
    }

    @Override
    public Vector<Double> clone() {
        return new RealVector(Arrays.copyOf(data, data.length));
    }

    public static RealVector fromString(String s, int n) throws VectorFormatException {
        final double[] data = new double[n];
        final String[] ss = s.split(",");
        if (ss.length == n) {
            for (int i = 0; i < n; i++) {
                if (ss[i].contains("=")) {
                    final String[] sss = ss[i].split("=");
                    if (sss.length != 2) {
                        throw new VectorFormatException("Too many =s: " + ss[i]);
                    }
                    try {
                        data[Integer.parseInt(sss[0].replaceAll("\\[\\]\\s", ""))] = Double.parseDouble(sss[1].replaceAll("\\[\\]\\s", ""));
                    } catch (NumberFormatException x) {
                        throw new VectorFormatException(x);
                    }
                } else {
                    try {
                        data[i] = Double.parseDouble(ss[i].replaceAll("\\[\\]\\s", ""));
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
                    data[Integer.parseInt(sss[0].replaceAll("\\[\\]\\s", ""))] = Double.parseDouble(sss[1].replaceAll("\\[\\]\\s", ""));
                } catch (NumberFormatException x) {
                    throw new VectorFormatException(x);
                }
            }
        } else {
            throw new VectorFormatException("Real vector too long");
        }
        return new RealVector(data);
    }

    private class DoubleArraySet extends AbstractSet<Map.Entry<Integer, Double>> {

        @Override
        public Iterator<Map.Entry<Integer, Double>> iterator() {
            return new Iterator<Entry<Integer, Double>>() {

                int n = 0;

                @Override
                public boolean hasNext() {
                    return n < data.length;
                }

                @Override
                public Entry<Integer, Double> next() {
                    if (n < data.length) {
                        final int m = n++;
                        return new Map.Entry<Integer, Double>() {

                            @Override
                            public Integer getKey() {
                                return m;
                            }

                            @Override
                            public Double getValue() {
                                return data[m];
                            }

                            @Override
                            public Double setValue(Double value) {
                                final double old = data[m];
                                data[m] = value.doubleValue();
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
}
