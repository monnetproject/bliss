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
import java.util.Map;

/**
 * A matrix that wraps a double[][] array.
 *
 * @author John McCrae
 */
public class DoubleArrayMatrix implements Matrix<Double> {

    private final int m, n;
    private final double[][] data;

    public DoubleArrayMatrix(int m, int n) {
        this.m = m;
        this.n = n;
        this.data = new double[m][n];
    }

    public DoubleArrayMatrix(double[][] data) {
        this.data = data;
        this.m = data.length;
        if (m == 0) {
            n = 0;
        } else {
            n = data[0].length;
            for (int i = 1; i < m; i++) {
                assert (data[i].length == n);
            }
        }
    }

    @Override
    public <M extends Number> Vector<Double> mult(Vector<M> x) {
        return mult(x,Vectors.AS_REALS);
    }
    
    @Override
    public <M extends Number, O extends Number> Vector<O> mult(Vector<M> x, Vectors.Factory<O> using) {
        assert (x.length() == n);
        double[] product = new double[m];
        if (x instanceof RealVector) {
            final double[] x2 = ((RealVector) x).data();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    product[i] += data[i][j] * x2[j];
                }
            }
        } else if (x instanceof IntVector) {
            final int[] x2 = ((IntVector) x).data();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    product[i] += data[i][j] * x2[j];
                }
            }
        } else {
            for (int i = 0; i < m; i++) {
                for (Map.Entry<Integer, M> e : x.entrySet()) {
                    product[i] += data[i][e.getKey()] * e.getValue().doubleValue();
                }
            }
        }
        return using.make(product);
    }

    @Override
    public <M extends Number> Vector<Double> multTransposed(Vector<M> x) {
        assert (x.length() == n);
        double[] product = new double[m];
        if (x instanceof RealVector) {
            final double[] x2 = ((RealVector) x).data();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    product[i] += data[j][i] * x2[j];
                }
            }
        } else if (x instanceof IntVector) {
            final int[] x2 = ((IntVector) x).data();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    product[i] += data[j][i] * x2[j];
                }
            }
        } else {
            for (int i = 0; i < m; i++) {
                for (Map.Entry<Integer, M> e : x.entrySet()) {
                    product[i] += data[e.getKey()][i] * e.getValue().doubleValue();
                }
            }
        }
        return new RealVector(product);
    }
    
    @Override
    public boolean isSymmetric() {
        for(int i = 0; i < m; i++) {
            for(int j = i+1; j < n; j++) {
                if(data[i][j] != data[j][i])
                    return false;
            }
        }
        return true;
    }

    @Override
    public Matrix<Double> transpose() {
        double[][] data2 = new double[n][m];
        for(int i = 0; i < m; i++) {
            for(int j = 0; j < n; j++) {
                data2[j][i] = data[i][j];
            }
        }
        return new DoubleArrayMatrix(data2);
    }

    @Override
    public Vector<Double> row(int i) {
        return new RealVector(data[i]);
    }
    
    

    /**
     * Get the underlying data
     */
    public double[][] data() {
        return data;
    }

    @Override
    public Double value(int i, int j) {
        return data[i][j];
    }

    @Override
    public double doubleValue(int i, int j) {
        return data[i][j];
    }

    @Override
    public int intValue(int i, int j) {
        return (int) data[i][j];
    }

    @Override
    public void set(int i, int j, int v) {
        data[i][j] = v;
    }

    @Override
    public void set(int i, int j, double v) {
        data[i][j] = v;
    }

    @Override
    public void set(int i, int j, Double v) {
        data[i][j] = v;
    }
    
    @Override
    public void add(int i, int j, int v) {
        data[i][j] += v;
    }

    @Override
    public void add(int i, int j, double v) {
        data[i][j] += v;
    }

    @Override
    public void add(int i, int j, Double v) {
        data[i][j] += v;
    }

    @Override
    public <M extends Number> void add(Matrix<M> matrix) {
        for(int i = 0; i < m; i++) {
            for(int j = 0; j < n ;j++) {
                data[i][j] += matrix.doubleValue(i, j);
            }
        }
    }

    @Override
    public <M extends Number> void sub(Matrix<M> matrix) {
        for(int i = 0; i < m; i++) {
            for(int j = 0; j < n ;j++) {
                data[i][j] -= matrix.doubleValue(i, j);
            }
        }
    }
    
    @Override
    public int rows() {
        return m;
    }

    @Override
    public int cols() {
        return n;
    }

    @Override
    public VectorFunction<Double,Double> asVectorFunction() {
        return new VectorFunction<Double,Double>() {

            @Override
            public Vector<Double> apply(Vector<Double> v) {
                return mult(v);
            }
        };
    }

    @Override
    public Factory<Double> factory() {
        return Vectors.AS_REALS;
    }
}
