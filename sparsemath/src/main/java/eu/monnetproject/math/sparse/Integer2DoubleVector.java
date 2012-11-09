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

import eu.monnetproject.math.sparse.Vectors.Factory;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author John McCrae
 */
public class Integer2DoubleVector implements Vector<Double> {

    private final Vector<Integer> v;

    public Integer2DoubleVector(Vector<Integer> v) {
        this.v = v;
    }

    @Override
    public double doubleValue(int idx) {
        return v.doubleValue(idx);
    }

    @Override
    public int intValue(int idx) {
        return v.intValue(idx);
    }

    @Override
    public Double value(int idx) {
        return v.value(idx).doubleValue();
    }

    @Override
    public Double put(Integer idx, Double n) {
        return v.put(idx, n);
    }

    @Override
    public double put(int idx, double value) {
        return v.put(idx, value);
    }

    @Override
    public int put(int idx, int value) {
        return v.put(idx, value);
    }

    @Override
    public int add(int idx, int val) {
        return v.add(idx, val);
    }

    @Override
    public void sub(int idx, int val) {
        v.sub(idx, val);
    }

    @Override
    public void multiply(int idx, int val) {
        v.multiply(idx, val);
    }

    @Override
    public void divide(int idx, int val) {
        v.divide(idx, val);
    }

    @Override
    public double add(int idx, double val) {
        return v.add(idx, val);
    }

    @Override
    public void sub(int idx, double val) {
        v.sub(idx, val);
    }

    @Override
    public void multiply(int idx, double val) {
        v.multiply(idx, val);
    }

    @Override
    public void divide(int idx, double val) {
        v.divide(idx, val);
    }

    @Override
    public <M extends Number> void add(Vector<M> vector) {
        v.add(vector);
    }

    @Override
    public <M extends Number> void sub(Vector<M> vector) {
        v.sub(vector);
    }

    @Override
    public void multiply(double n) {
        v.multiply(n);
    }

    @Override
    public <M extends Number> double innerProduct(Vector<M> y) {
        return v.innerProduct(y);
    }

    @Override
    public <M extends Number, O extends Number> Matrix<O> outerProduct(Vector<M> y, Factory<O> using) {
        return v.outerProduct(y, using);
    }

    @Override
    public Set<Entry<Integer, Double>> entrySet() {
        final Set<Entry<Integer, Integer>> es = v.entrySet();
        return new AbstractSet<Entry<Integer, Double>>() {
            @Override
            public Iterator<Entry<Integer, Double>> iterator() {
                final Iterator<Entry<Integer, Integer>> it = es.iterator();
                return new Iterator<Entry<Integer, Double>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<Integer, Double> next() {
                        final Entry<Integer, Integer> e = it.next();
                        return new Entry<Integer, Double>() {
                            @Override
                            public Integer getKey() {
                                return e.getKey();
                            }

                            @Override
                            public Double getValue() {
                                return e.getValue().doubleValue();
                            }

                            @Override
                            public Double setValue(Double value) {
                                return e.setValue(value.intValue()).doubleValue();
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public int size() {
                return es.size();
            }
        };
    }

    @Override
    public double[] toDoubleArray() {
        return v.toDoubleArray();
    }

    @Override
    public int size() {
        return v.size();
    }

    @Override
    public Double defaultValue() {
        return v.defaultValue().doubleValue();
    }

    @Override
    public int length() {
        return v.length();
    }

    @Override
    public double norm() {
        return v.norm();
    }

    @Override
    public Vector<Double> clone() {
        throw new UnsupportedOperationException("Cannot clone Integer2DoubleVector, clone underlying element instead");
    }

    @Override
    public Factory<Double> factory() {
        if (v.factory() == Vectors.AS_SPARSE_INTS) {
            return Vectors.AS_SPARSE_REALS;
        } else {
            return Vectors.AS_REALS;
        }

    }

    @Override
    public IntSet keySet() {
        return v.keySet();
    }

    @Override
    public Double sum() {
        return v.sum().doubleValue();
    }

    @Override
    public boolean containsKey(int idx) {
        return v.containsKey(idx);
    }
}
