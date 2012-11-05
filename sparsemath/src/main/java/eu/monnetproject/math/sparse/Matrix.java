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

/**
 *
 * @author John McCrae
 */
public interface Matrix<N extends Number> {

    N value(int i, int j);
    
    double doubleValue(int i, int j);
    
    int intValue(int i, int j);
    
    void set(int i, int j, int v);
    
    void set(int i, int j, double v);
    
    void set(int i, int j, N v);
    
    void add(int i, int j, int v);
    
    void add(int i, int j, double v);
    
    void add(int i, int j, N v);
    
    int rows();
    
    int cols();
    
    boolean isSymmetric();
    
    Matrix<N> transpose();
    
    Vector<N> row(int i);
    
    <M extends Number> Vector<N> mult(Vector<M> x);
    
    <M extends Number, O extends Number> Vector<O> mult(Vector<M> x, Vectors.Factory<O> using);
    
    <M extends Number> void add(Matrix<M> matrix);
    
    <M extends Number> void sub(Matrix<M> matrix);
    
    VectorFunction<N,N> asVectorFunction();
    
    Vectors.Factory<N> factory();
}
