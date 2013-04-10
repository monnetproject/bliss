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
package eu.monnetproject.math.sparse;

import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Not thread safe!
 *
 * @author jmccrae
 */
public class DiskBackedRealVector implements Vector<Double> {

    SoftReference<ByteBuffer> _data = new SoftReference<ByteBuffer>(null);
    private final FileChannel channel;
    private final long pos, length;
    private static final int SIZE_OF_DOUBLE = 8;

    private ByteBuffer data() {
        ByteBuffer d = _data.get();
        if (d == null) {
            try {
                d = channel.map(FileChannel.MapMode.READ_WRITE, pos, length);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
            _data = new SoftReference<ByteBuffer>(d);
        }
        return d;
    }

    public DiskBackedRealVector(FileChannel channel, long pos, long length) {
        this.channel = channel;
        this.pos = pos;
        this.length = length;
    }

    @Override
    public double doubleValue(int idx) {
        final ByteBuffer d = data();
        d.position(SIZE_OF_DOUBLE * idx);
        return d.getDouble();
    }

    @Override
    public int intValue(int idx) {
        return (int) doubleValue(idx);
    }

    @Override
    public Double value(int idx) {
        return doubleValue(idx);
    }

    @Override
    public Double put(Integer idx, Double n) {
        return put(idx.intValue(), n.doubleValue());
    }

    @Override
    public double put(int idx, double value) {
        final ByteBuffer d = data();
        d.position(SIZE_OF_DOUBLE * idx);
        double rval = d.getDouble();
        d.position(SIZE_OF_DOUBLE * idx);
        d.putDouble(value);
        return rval;
    }

    @Override
    public int put(int idx, int value) {
        return (int) put(idx, value);
    }

    @Override
    public int add(int idx, int val) {
        return (int) put(idx, doubleValue(idx) + val);
    }

    @Override
    public void sub(int idx, int val) {
        put(idx, doubleValue(idx) - val);
    }

    @Override
    public void multiply(int idx, int val) {
        put(idx, doubleValue(idx) * val);
    }

    @Override
    public void divide(int idx, int val) {
        put(idx, doubleValue(idx) / val);
    }

    @Override
    public double add(int idx, double val) {
        return put(idx, doubleValue(idx) / val);
    }

    @Override
    public void sub(int idx, double val) {
        put(idx, doubleValue(idx) - val);
    }

    @Override
    public void multiply(int idx, double val) {
        put(idx, doubleValue(idx) * val);
    }

    @Override
    public void divide(int idx, double val) {
        put(idx, doubleValue(idx) / val);
    }

    @Override
    public <M extends Number> void add(Vector<M> vector) {
        assert (vector.length() != length / SIZE_OF_DOUBLE);
        final ByteBuffer d = data();
        for (int i = 0; i < length; i += SIZE_OF_DOUBLE) {
            d.position(i);
            double v = d.getDouble();
            d.position(i);
            d.putDouble(v + vector.doubleValue(i / SIZE_OF_DOUBLE));
        }
    }

    @Override
    public <M extends Number> void sub(Vector<M> vector) {
        assert (vector.length() != length / SIZE_OF_DOUBLE);
        final ByteBuffer d = data();
        for (int i = 0; i < length; i += SIZE_OF_DOUBLE) {
            d.position(i);
            double v = d.getDouble();
            d.position(i);
            d.putDouble(v - vector.doubleValue(i / SIZE_OF_DOUBLE));
        }
    }

    @Override
    public void multiply(double n) {
        final ByteBuffer d = data();
        for (int i = 0; i < length; i += SIZE_OF_DOUBLE) {
            d.position(i);
            double v = d.getDouble();
            d.position(i);
            d.putDouble(v * n);
        }
    }

    @Override
    public <M extends Number> double innerProduct(Vector<M> y) {
        final ByteBuffer d = data();
        double ip = 0.0;
        for (int i = 0; i < length; i += SIZE_OF_DOUBLE) {
            d.position(i);
            double v = d.getDouble();
            d.putDouble(v * y.doubleValue(i / SIZE_OF_DOUBLE));
        }
        return ip;
    }

    @Override
    public <M extends Number, O extends Number> Matrix<O> outerProduct(Vector<M> y, Vectors.Factory<O> using) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Map.Entry<Integer, Double>> entrySet() {
        return new DoubleArraySet();
    }

    private class DoubleArraySet extends AbstractSet<Map.Entry<Integer, Double>> {

        @Override
        public Iterator<Map.Entry<Integer, Double>> iterator() {
            return new Iterator<Map.Entry<Integer, Double>>() {
                int n = 0;

                @Override
                public boolean hasNext() {
                    return n * SIZE_OF_DOUBLE < length;
                }

                @Override
                public Map.Entry<Integer, Double> next() {
                    if (n * SIZE_OF_DOUBLE < length) {
                        final int m = n++;
                        return new Map.Entry<Integer, Double>() {
                            @Override
                            public Integer getKey() {
                                return m;
                            }

                            @Override
                            public Double getValue() {
                                return doubleValue(m);
                            }

                            @Override
                            public Double setValue(Double value) {
                                final double old = doubleValue(m);
                                put(m, old);
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
            return (int) length / SIZE_OF_DOUBLE;
        }
    }

    @Override
    public double[] toDoubleArray() {
        double[] ds = new double[size()];
        ByteBuffer data = data();
        data.position(0);
        for (int i = 0; i < size(); i++) {
            ds[i] = data.getDouble();
        }
        return ds;
    }

    @Override
    public int size() {
        return (int) length / SIZE_OF_DOUBLE;
    }

    @Override
    public Double defaultValue() {
        return 0.0;
    }

    @Override
    public int length() {
        return size();
    }

    @Override
    public double norm() {
        ByteBuffer data = data();
        data.position(0);
        double norm = 0.0;
        for (int i = 0; i < size(); i++) {
            double d = data.getDouble();
            norm += d * d;
        }
        return Math.sqrt(norm);
    }

    @Override
    public Vector<Double> clone() {
        return new DiskBackedRealVector(channel, pos, length);
    }

    private void insertAll(double[] v) {
        final ByteBuffer d = data();
        d.position(0);
        for (int i = 0; i < v.length; i++) {
            d.putDouble(v[i]);
        }
    }

    public static class DiskBackedRealVectorFactory implements Vectors.Factory<Double> {

        private final FileChannel fileChannel;
        private long pos;

        public DiskBackedRealVectorFactory(FileChannel fileChannel) {
            this.fileChannel = fileChannel;
        }

        public DiskBackedRealVectorFactory(File file) throws FileNotFoundException {
            this.fileChannel = new RandomAccessFile(file, "rw").getChannel();
        }

        @Override
        public Vector<Double> make(int n, double defaultValue) {
            assert (defaultValue == 0.0);
            long p = pos;
            final DiskBackedRealVector v = new DiskBackedRealVector(fileChannel, pos, n * SIZE_OF_DOUBLE);
            pos += n * SIZE_OF_DOUBLE;
            return v;
        }

        @Override
        public Vector<Double> make(double[] data) {
            long p = pos;
            final DiskBackedRealVector v = new DiskBackedRealVector(fileChannel, pos, data.length * SIZE_OF_DOUBLE);
            v.insertAll(data);
            pos += data.length * SIZE_OF_DOUBLE;
            return v;
        }

        @Override
        public Vector<Double> fromString(String s, int n) throws VectorFormatException {
            String[] entries = s.split(",");
            final Vector<Double> v = make(n, 0.0);
            for (String entry : entries) {
                if (entry.matches("\\s*")) {
                    continue;
                }
                String[] values = entry.split("=");
                if (values.length != 2) {
                    throw new VectorFormatException("Bad sparse array value " + entry);
                }
                try {
                    v.put(new Integer(values[0]), new Double(values[1]));
                } catch (NumberFormatException x) {
                    throw new VectorFormatException(x);
                }
            }
            return v;
        }

        @Override
        public Double valueOf(double value) {
            return value;
        }
    }

    @Override
    public Vectors.Factory<Double> factory() {
        return new DiskBackedRealVectorFactory(channel);
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
        return idx >= 0 && idx < size();
    }

    @Override
    public Double sum() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector<Double> subvector(int offset, int length) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
