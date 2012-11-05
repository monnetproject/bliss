/**********************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
package eu.monnetproject.math.sparse;

import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class Vectors {

    private Vectors() {
        
    }
    
    
    /**
     * An object for creating sub-arrays, generally either {@link AS_INT_ARRAY}
     * or {@link AS_REAL_ARRAY}
     *
     * @param <N>
     */
    public static interface Factory<N extends Number> {

        /**
         * Make a vector of length n with all values as defaultValue
         * @param n The length of the vector
         * @param defaultValue The value of all values
         * @return The vector
         */
        Vector<N> make(int n, double defaultValue);
        
        /**
         * Make a vector from the data
         * @param data The data
         * @return The vector
         */
        Vector<N> make(double[] data);
        
        /**
         * Make a vector from a string, the format for which should be the same
         * as the {@code toString()} function of the class this factory generates
         * @param s The string s
         * @param n The integer n
         * @return The vector
         * @throws VectorFormatException 
         */
        Vector<N> fromString(String s, int n) throws VectorFormatException;

        /**
         * Convert a double into a value that can be stored in this vector
         * @param value The value
         * @return The storeable value
         */
        N valueOf(double value);
    }
    /**
     * Use this to create real arrays in matrix operation
     */
    public static final Factory<Double> AS_SPARSE_REALS = new SparseRealArrayFactory();

    private static final class SparseRealArrayFactory implements Factory<Double> {

        @Override
        public Vector<Double> make(int n, double defaultValue) {
            return new SparseRealArray(n, defaultValue, 1e-8);
        }

        @Override
        public Vector<Double> make(double[] data) {
            return SparseRealArray.fromArray(data);
        }

        @Override
        public Vector<Double> fromString(String s, int n) throws VectorFormatException {
            return SparseRealArray.fromString(s, n);
        }

        @Override
        public Double valueOf(double value) {
            return value;
        }
    }
    
    /**
     * Use this to create real arrays in matrix operation
     */
    public static final Factory<Double> AS_REALS = new RealVectorArrayFactory();

    private static final class RealVectorArrayFactory implements Factory<Double> {

        @Override
        public Vector<Double> make(int n, double defaultValue) {
            if(defaultValue == 0.0) {
                return new RealVector(n);
            }  else {
                double[] data = new double[n];
                Arrays.fill(data, defaultValue);
                return new RealVector(data);
            }
        }

        @Override
        public Vector<Double> make(double[] data) {
            return new RealVector(data);
        }

        @Override
        public Vector<Double> fromString(String s, int n) throws VectorFormatException {
            return RealVector.fromString(s, n);
        }

        @Override
        public Double valueOf(double value) {
            return value;
        }
    }
    
    /**
     * Use this to create sparse integer arrays in matrix operation
     */
    public static final Factory<Integer> AS_SPARSE_INTS = new SparseIntArrayFactory();

    private static final class SparseIntArrayFactory implements Factory<Integer> {

        @Override
        public Vector<Integer> make(int n, double defaultValue) {
            return new SparseIntArray(n, (int) defaultValue);
        }

        @Override
        public Vector<Integer> make(double[] data) {
            final SparseIntArray arr = new SparseIntArray(data.length);
            for(int i = 0; i < data.length; i++) {
                if(data[i] != 0.0) {
                    arr.put(i, data[i]);
                }
            }
            return arr;
        }

        @Override
        public Vector<Integer> fromString(String s, int n) throws VectorFormatException {
            return SparseIntArray.fromString(s, n);
        }

        @Override
        public Integer valueOf(double value) {
            return (int) value;
        }
    }
    
    /**
     * Use this to create real arrays in matrix operation
     */
    public static final Factory<Integer> AS_INTS = new IntVectorArrayFactory();

    private static final class IntVectorArrayFactory implements Factory<Integer> {

        @Override
        public Vector<Integer> make(int n, double defaultValue) {
            if(defaultValue == 0.0) {
                return new IntVector(n);
            }  else {
                int[] data = new int[n];
                Arrays.fill(data, (int)defaultValue);
                return new IntVector(data);
            }
        }

        @Override
        public Vector<Integer> make(double[] data) {
            int[] data2 = new int[data.length];
            for(int i = 0; i < data.length; i++) {
                data2[i] = (int)data[i];
            }
            return new IntVector(data2);
        }

        @Override
        public Vector<Integer> fromString(String s, int n) throws VectorFormatException {
            return IntVector.fromString(s, n);
        }

        @Override
        public Integer valueOf(double value) {
            return (int)value;
        }
    }
}
