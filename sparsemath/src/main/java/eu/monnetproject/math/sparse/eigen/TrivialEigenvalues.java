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
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.Matrix;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.math.sparse.Vectors;
import eu.monnetproject.math.sparse.Vectors.Factory;
import java.io.*;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class TrivialEigenvalues<N extends Number> {

    private static <N extends Number> TrivialEigenvalues<N> filterMatrix(final boolean[] trivialEigenCols, Matrix<N> A) throws RuntimeException {
        int trivColCount = 0;
        for (int i = 0; i < trivialEigenCols.length; i++) {
            if (trivialEigenCols[i]) {
                trivColCount++;
            }
        }
        if (trivColCount == 0) {
            return new TrivialEigenvalues<N>(A, new double[0], new boolean[A.rows()]);
        }
        double[] trivs = trivColCount > 0 ? new double[trivColCount] : null;
        int[] map = new int[A.rows() - trivColCount];
        int trivIdx = 0;
        int nontrivIdx = 0;
        for (int i = 0; i < A.rows(); i++) {
            if (trivialEigenCols[i]) {
                final Vector<N> row = A.row(i);
                if (row.size() == 1) {
                    for (Map.Entry<Integer, N> e : row.entrySet()) {
                        if (e.getValue().doubleValue() != 0.0) {
                            trivs[trivIdx] = e.getValue().doubleValue();
                            break;
                        }
                    }
                } else {
                    trivs[trivIdx] = 0;
                }
                trivIdx++;
            } else {
                map[nontrivIdx++] = i;
            }
        }

        assert (trivIdx == trivColCount);

        if (trivIdx == A.rows()) {
            final boolean[] allTrue = new boolean[A.rows()];
            Arrays.fill(allTrue, false);
            return new TrivialEigenvalues<N>(null, trivs, allTrue);
        }
        return new TrivialEigenvalues<N>(new SymmetricFilteredMatrix<N>(map, A, trivColCount), trivs, trivialEigenCols);
    }
    public final Matrix<N> nonTrivial;
    public final double[] eigenvalues;
    private final boolean[] filter;

    private TrivialEigenvalues(Matrix<N> nonTrivial, double[] eigenvalues, boolean[] filter) {
        this.nonTrivial = nonTrivial;
        this.eigenvalues = eigenvalues;
        this.filter = filter;
    }

    public boolean isTrivial() {
        for(boolean trivial : filter) {
            if(trivial)
                return true;
        }
        return false;
    }
    
    /**
     * For the matrix compute the columns with trivial eigenvalues. This is
     * defined as the columns for which there is only one non-zero value (i.e.,
     * this is the eigenvalue) or no non-zero values (i.e., the eigenvalue is
     * zero)
     *
     * @return
     */
    public static <N extends Number, M extends Matrix<N>> boolean[] trivialEigenCols(M A, boolean isSymmetric) {
        assert ((isSymmetric && A.isSymmetric()) || (!isSymmetric && !A.isSymmetric()));
        final Matrix<N> mat;
        if (isSymmetric) {
            mat = A;
        } else {
            mat = A.transpose();
        }
        final boolean[] trivials = new boolean[mat.rows()];
        for (int i = 0; i < mat.rows(); i++) {
            final Vector<N> row = mat.row(i);
            // Zero row
            if (row.size() == 0) {
                trivials[i] = true;
                // Identity row
            } else if (row.size() == 1) {
                NONDIAGONAL_NONZERO:
                {
                    for (Map.Entry<Integer, N> e : row.entrySet()) {
                        if (e.getValue().doubleValue() != 0.0 && e.getKey().intValue() != i) {
                            break NONDIAGONAL_NONZERO;
                        }
                    }
                    trivials[i] = true;
                }
            } else {
                trivials[i] = false;
            }
        }
        return trivials;
    }

    public static <N extends Number> TrivialEigenvalues<N> find(Matrix<N> A, boolean symmetric) {
        final boolean[] trivialEigenCols = trivialEigenCols(A, true);
        return filterMatrix(trivialEigenCols, A);
    }

    private static class SymmetricFilteredMatrix<N extends Number> implements Matrix<N> {

        final int[] map, invMap;
        final Matrix<N> A;
        final int m;

        public SymmetricFilteredMatrix(int[] map, Matrix<N> A, int m) {
            assert (map.length + m == A.rows());
            this.map = map;
            this.invMap = new int[A.rows()];
            Arrays.fill(invMap, -1);
            for (int i = 0; i < map.length; i++) {
                invMap[map[i]] = i;
            }
            this.A = A;
            this.m = m;
        }

        @Override
        public N value(int i, int j) {
            return A.value(map[i], map[j]);
        }

        @Override
        public double doubleValue(int i, int j) {
            return A.doubleValue(map[i], map[j]);
        }

        @Override
        public int intValue(int i, int j) {
            return A.intValue(map[i], map[j]);
        }

        @Override
        public void set(int i, int j, int v) {
            A.set(map[i], map[j], v);
        }

        @Override
        public void set(int i, int j, double v) {
            A.set(map[i], map[j], v);
        }

        @Override
        public void set(int i, int j, N v) {
            A.set(map[i], map[j], v);
        }

        @Override
        public void add(int i, int j, int v) {
            A.set(map[i], map[j], v + A.value(map[i], map[j]).intValue());
        }

        @Override
        public void add(int i, int j, double v) {
            A.set(map[i], map[j], v + A.value(map[i], map[j]).doubleValue());
        }

        @Override
        public void add(int i, int j, N v) {
            A.set(map[i], map[j], v.doubleValue() + A.value(map[i], map[j]).doubleValue());
        }

        @Override
        public int rows() {
            return map.length;
        }

        @Override
        public int cols() {
            return map.length;
        }

        @Override
        public boolean isSymmetric() {
            return A.isSymmetric();
        }

        @Override
        public Matrix<N> transpose() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Vector<N> row(int i) {
            throw new UnsupportedOperationException("Not supported yet");
        }

        @Override
        public <M extends Number> Vector<N> mult(Vector<M> x) {
            throw new UnsupportedOperationException("Not supported yet (specify using).");
        }

        private <O extends Number> Vector<O> invMap(Vector<O> x, Factory<O> using) {
            final Vector<O> x2 = using.make(map.length, 0.0);
            for (Map.Entry<Integer, O> e : x.entrySet()) {
                final int i = e.getKey();
                if (invMap[i] >= 0) {
                    x2.put(invMap[i], e.getValue());
                }
            }
            return x2;
        }

        @Override
        public <M extends Number, O extends Number> Vector<O> mult(Vector<M> x, Factory<O> using) {
            final Vector<O> x2 = using.make(A.rows(), 0.0);
            for (Map.Entry<Integer, M> e : x.entrySet()) {
                x2.put(map[e.getKey()], e.getValue().doubleValue());
            }
            return invMap(A.mult(x2, using), using);
        }

        @Override
        public VectorFunction<N> asVectorFunction() {
            return new VectorFunction<N>() {

                @Override
                public Vector<N> apply(Vector<N> v) {
                    return mult(v,v.factory());
                }
            };
        }

        @Override
        public Factory<N> factory() {
            return A.factory();
        }

        @Override
        public <M extends Number> void add(Matrix<M> matrix) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <M extends Number> void sub(Matrix<M> matrix) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String toString() {
            return "SymmetricFilteredMatrix{" + "map=" + Arrays.toString(map) + ", invMap=" + Arrays.toString(invMap) + ", A=" + A + '}';
        }
    }

    public static <N extends Number> TrivialEigenvalues<N> fromFile(File file, Matrix<N> matrix) throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(file));
        final String sEigenvalues = in.readLine();
        if (sEigenvalues == null) {
            throw new IOException("Expected eigen values");
        }
        if (sEigenvalues.matches("\\s*")) {
            return new TrivialEigenvalues<N>(matrix, new double[0], new boolean[matrix.rows()]);
        }
        final String[] ssEvs = sEigenvalues.split(",");
        double[] eigenvalues = new double[ssEvs.length];
        for (int i = 0; i < ssEvs.length; i++) {
            eigenvalues[i] = Double.parseDouble(ssEvs[i].replaceAll("\\]\\[\\s", ""));
        }
        final String bLine = in.readLine();
        if (bLine == null) {
            throw new IOException("Expected filter line");
        }
        if (bLine.matches("\\s*")) {
            final boolean[] allTrue = new boolean[matrix.rows()];
            Arrays.fill(allTrue, false);
            return new TrivialEigenvalues<N>(null, eigenvalues, allTrue);
        }
        final String[] sbLine = bLine.split(",");
        boolean[] filter = new boolean[sbLine.length];
        for (int i = 0; i < sbLine.length; i++) {
            filter[i] = Boolean.parseBoolean(bLine.replaceAll("\\]\\[\\s", ""));
        }
        return filterMatrix(filter, matrix);
    }

    public void toFile(File file) throws IOException {
        final PrintWriter out = new PrintWriter(file);
        out.println(Arrays.toString(eigenvalues));
        out.println(Arrays.toString(filter));
        out.flush();
        out.close();
    }
}
