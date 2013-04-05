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
package eu.monnetproject.bliss.kcca;

import eu.monnetproject.math.sparse.Matrix;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.Vectors.Factory;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author John McCrae
 */
public class ShiftedVector<N extends Number> implements Vector<N> {
    private final int off,len;
    private final Vector<N> v;

    public ShiftedVector(int off, int len, Vector<N> v) {
        this.off = off;
        this.len = len;
        this.v = v;
    }
    
    
    
    
    @Override
    public double doubleValue(int idx) {
        if(idx < len) {
            return v.doubleValue(idx+off);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int intValue(int idx) {
        if(idx < len) {
            return v.intValue(idx+off);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public N value(int idx) {
        if(idx < len) {
            return v.value(idx+off);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public N put(Integer idx, N n) {
        if(idx < len) {
            return v.put(idx+off,n);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public double put(int idx, double value) {
        if(idx < len) {
            return v.put(idx+off,value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int put(int idx, int value) {
        if(idx < len) {
            return v.put(idx+off,value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int add(int idx, int val) {
        if(idx < len) {
            return v.add(idx+off,val);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void sub(int idx, int val) {
        if(idx < len) {
            v.sub(idx+off,val);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void multiply(int idx, int val) {
        if(idx < len) {
            v.multiply(idx+off,val);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void divide(int idx, int val) {
        if(idx < len) {
            v.divide(idx+off,val);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public double add(int idx, double val) {
        if(idx < len) {
            return v.add(idx+off,val);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void sub(int idx, double val) {
        if(idx < len) {
            v.sub(idx+off,val);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void multiply(int idx, double val) {
        if(idx < len) {
            v.multiply(idx+off,val);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void divide(int idx, double val) {
        if(idx < len) {
            v.divide(idx+off,val);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public <M extends Number> void add(Vector<M> vector) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <M extends Number> void sub(Vector<M> vector) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void multiply(double n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <M extends Number> double innerProduct(Vector<M> y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <M extends Number, O extends Number> Matrix<O> outerProduct(Vector<M> y, Factory<O> using) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entry<Integer, N>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[] toDoubleArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public N defaultValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public double norm() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector<N> clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Factory<N> factory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IntSet keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsKey(int idx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public N sum() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector<N> subvector(int offset, int length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
