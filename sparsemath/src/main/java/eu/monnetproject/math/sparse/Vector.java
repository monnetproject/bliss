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
 ********************************************************************************
 */
package eu.monnetproject.math.sparse;

import java.util.Map;
import java.util.Set;

/**
 * A sparse array of some values
 *
 * @author John McCrae
 */
public interface Vector<N extends Number> {

    /**
     * The value at the given index. Note the default value will be returned at
     * sparse indexes
     *
     * @param idx The index
     * @return The value
     */
    double doubleValue(int idx);

    /**
     * The value at the given index. Note the default value will be returned at
     * sparse indexes
     *
     * @param idx The index
     * @return The value
     */
    int intValue(int idx);
    
    /**
     * The value at the given index. Note the default value will be returned at
     * sparse indexes
     *
     * @param idx The index
     * @return The value
     */
    N value(int idx);
    
    /**
     * Set a value at a given index
     *
     * @param idx The index
     * @param n The value
     * @return The previous value
     */
    N put(Integer idx, N n);
    
    /**
     * Set a value at a given index
     *
     * @param idx The index
     * @param n The value
     * @return The previous value
     */
    void put(int idx, double value);
    
    /**
     * Set a value at a given index
     *
     * @param idx The index
     * @param n The value
     * @return The previous value
     */
    void put(int idx, int value);

    /**
     * Add a value to the sparse array. This will automatically remove an
     * element if it results in the value at this index being the sparse value
     *
     * @param idx The index
     * @param val The value
     */
    void add(int idx, int val);

    /**
     * Subtract a value from the sparse array. This will automatically remove an
     * element if it results in the value at this index being the sparse value
     *
     * @param idx The index
     * @param val The value
     */
    void sub(int idx, int val);

    /**
     * Multiply a value by a value in the sparse array. This will automatically
     * remove an element if it results in the value at this index being the
     * sparse value
     *
     * @param idx The index
     * @param val The value
     */
    void multiply(int idx, int val);

    /**
     * Divide a value by a value in the sparse array. This will automatically
     * remove an element if it results in the value at this index being the
     * sparse value
     *
     * @param idx The index
     * @param val The value
     */
    void divide(int idx, int val);
    
    /**
     * Add a value to the sparse array. This will automatically remove an
     * element if it results in the value at this index being the sparse value
     *
     * @param idx The index
     * @param val The value
     */
    void add(int idx, double val);

    /**
     * Subtract a value from the sparse array. This will automatically remove an
     * element if it results in the value at this index being the sparse value
     *
     * @param idx The index
     * @param val The value
     */
    void sub(int idx, double val);

    /**
     * Multiply a value by a value in the sparse array. This will automatically
     * remove an element if it results in the value at this index being the
     * sparse value
     *
     * @param idx The index
     * @param val The value
     */
    void multiply(int idx, double val);

    /**
     * Divide a value by a value in the sparse array. This will automatically
     * remove an element if it results in the value at this index being the
     * sparse value
     *
     * @param idx The index
     * @param val The value
     */
    void divide(int idx, double val);
    
    /**
     * Add a vector to this vector. This will automatically remove an
     * element if it results in the value at this index being the sparse value
     *
     * @param vector The vector
     */
    <M extends Number> void add(Vector<M> vector);

    /**
     * Subtract a vector from this vector. This will automatically remove an
     * element if it results in the value at this index being the sparse value
     *
     * @param idx The index
     * @param val The value
     */
    <M extends Number> void sub(Vector<M> vector);
    
    /**
     * Multiply all values of this matrix by a value
     * @param n The value to multiply by
     */
    void multiply(double n);

    /**
     * Calculate the inner product of this array with another array, i.e.,
     * this^T y
     *
     * @param y The other array
     * @return The inner product
     */
    <M extends Number> double innerProduct(Vector<M> y);

    /**
     * Calculate the outer product of this array with another array i.e., this y^T
     * @param y The other array
     * @param using The factory for creating rows of the matrix
     * @return The outer product matrix
     */
    <M extends Number, O extends Number> Matrix<O> outerProduct(Vector<M> y, Vectors.Factory<O> using);
    
    /**
     * Get all non-sparse values in the array
     */
    Set<Map.Entry<Integer, N>> entrySet();
    
    /**
     * Convert to a dense array
     */
    double[] toDoubleArray();

    /**
     * Get the number of non-sparse values in the array
     */
    int size();

    /**
     * Get the default value of the array
     */
    N defaultValue();

    /**
     * Get the length (i.e., number of sparse and non-sparse values) of the
     * array
     */
    int length();
    
    /**
     * Return the 2-norm.
     */
    double norm();
    /**
     * Creates a copy of this vector
     */
    Vector<N> clone();
    
    /**
     * The factory for making more of this type of vector
     * @return 
     */
    Vectors.Factory<N> factory();
}
