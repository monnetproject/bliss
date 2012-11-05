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

import eu.monnetproject.math.sparse.Vectors.Factory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class IntArrayMatrix implements Matrix<Integer> {

    private final int m, n;
    private final int[][] data;

    public IntArrayMatrix(int m, int n) {
        this.m = m;
        this.n = n;
        this.data = new int[m][n];
    }

    public IntArrayMatrix(int[][] data) {
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
    public <M extends Number> Vector<Integer> mult(Vector<M> x) {
        return mult(x,Vectors.AS_INTS);
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
    public Matrix<Integer> transpose() {
        int[][] data2 = new int[n][m];
        for(int i = 0; i < m; i++) {
            for(int j = 0; j < n; j++) {
                data2[j][i] = data[i][j];
            }
        }
        return new IntArrayMatrix(data2);
    }

    @Override
    public Vector<Integer> row(int i) {
        return new IntVector(data[i]);
    }
    
    
    

    /**
     * Get the underlying data
     */
    public int[][] data() {
        return data;
    }

    @Override
    public Integer value(int i, int j) {
        return data[i][j];
    }

    @Override
    public double doubleValue(int i, int j) {
        return data[i][j];
    }

    @Override
    public int intValue(int i, int j) {
        return data[i][j];
    }

    @Override
    public void set(int i, int j, int v) {
        data[i][j] = v;
    }

    @Override
    public void set(int i, int j, double v) {
        data[i][j] = (int)v;
    }

    @Override
    public void set(int i, int j, Integer v) {
        data[i][j] = v;
    }
    
    
    @Override
    public void add(int i, int j, int v) {
        data[i][j] += v;
    }

    @Override
    public void add(int i, int j, double v) {
        data[i][j] += (int)v;
    }

    @Override
    public void add(int i, int j, Integer v) {
        data[i][j] += v;
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
    public VectorFunction<Integer,Integer> asVectorFunction() {
        return new VectorFunction<Integer,Integer>() {

            @Override
            public Vector<Integer> apply(Vector<Integer> v) {
                return mult(v);
            }
        };
    }

    @Override
    public Factory<Integer> factory() {
        return Vectors.AS_INTS;
    }
    
    public static IntArrayMatrix fromFile(File file) throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(file));
        String s;
        final int m = Integer.parseInt(in.readLine());
        final int n = Integer.parseInt(in.readLine());
        int[][] arrs = new int[m][];
        int idx = 0;
        while((s = in.readLine()) != null) {
            final String[] ss = s.split(",");
            final int[] arr = new int[ss.length];
            for(int i = 0; i < ss.length; i++) {
                arr[i] = Integer.parseInt(ss[i].replaceAll("\\[\\]\\s", ""));
            }
            if(idx >= m) {
                throw new IOException("Too many lines");
            }
            arrs[idx++] = arr;
        }
        return new IntArrayMatrix(arrs);
    }
    
    public void toFile(File file) throws IOException {
        final PrintWriter out = new PrintWriter(file);
        out.println(m);
        out.println(n);
        for(int i = 0; i < m; i++) {
            out.println(Arrays.toString(data[m]));
        }
        out.flush();
        out.close();
    }
}

