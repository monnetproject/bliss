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
package eu.monnetproject.bliss.lsa;

import eu.monnetproject.math.sparse.Integer2DoubleVector;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition.Solution;
import eu.monnetproject.bliss.CLIOpts;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *
 * @author John McCrae
 */
public class LSATrain {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);

        final boolean tfidf = opts.flag("tfidf", "Apply log(TF).IDF transformation");
        final double epsilon = opts.doubleValue("epsilon", 1e-50, "The error rate");
        final File corpus = opts.roFile("corpus[.gz|bz2]", "The corpus file");

        final int W = opts.intValue("W", "The number of distinct tokens");
        final int J = opts.intValue("J", "The number of documents (per language)");
        final int K = opts.intValue("K", "The number of topics");

        final File outFile = opts.woFile("output", "The file to write the SVD to");

        if (!opts.verify(LSATrain.class)) {
            return;
        }

        //final SingularValueDecomposition svd = new SingularValueDecomposition();

        //final Solution svdSoln = svd.calculateSymmetric(new LSAStreamIterable(corpus, W), 2 * W, J, K, epsilon);
        final Solution svdSoln = SingularValueDecomposition.eigen(new LSAStreamApply(corpus, W, J, tfidf ? calculateDF(corpus, W, J) : null), 2*W, K, epsilon);

        write(svdSoln, outFile);

    }

    private static double[][] calculateDF(File corpus, int W, int J) throws IOException {
        double[][] df = new double[2][W];
        int N = 0;
        final DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpus));
        final IntSet inDoc = new IntRBTreeSet();
        while (data.available() > 0) {
            try {
                int i = data.readInt();
                if (i == 0) {
                    N++;
                    inDoc.clear();
                } else if (!inDoc.contains(i)) {
                    df[N % 2][i]++;
                    inDoc.add(i);
                }
            } catch (EOFException x) {
                break;
            }
        }
        assert (J * 2 == N);
        for (int w = 0; w < W; w++) {
            df[0][w] /= J;
            df[1][w] /= J;
        }
        return df;
    }

    private static void write(Solution soln, File outFile) throws IOException {
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(outFile));
        out.writeInt(soln.S.length);
        out.writeInt(soln.U[0].length);

        for (int i = 0; i < soln.U.length; i++) {
            for (int j = 0; j < soln.U[i].length; j++) {
                out.writeDouble(soln.U[i][j]);
            }
        }

        for (int i = 0; i < soln.S.length; i++) {
            out.writeDouble(soln.S[i]);
        }

        out.flush();
        out.close();
    }

    public static class TFIDFApply implements VectorFunction<Integer,Double> {
        private final double[][] df;
        private final int W;

        public TFIDFApply(double[][] df, int W) {
            this.df = df;
            this.W = W;
        }


        @Override
        public Vector<Double> apply(Vector<Integer> v) {
            final Vector<Double> tfidf = new SparseRealArray(v.length());
            for(int w : v.keySet()) {
                final int tf = v.intValue(w);
                final int l = w/W;
                tfidf.put(w, Math.log(tf+1) / Math.log(df[l][w-W*l]));
            }
            return tfidf;
        }
    }
    
    public static class IdentityApply implements VectorFunction<Integer,Double> {

        @Override
        public Vector<Double> apply(Vector<Integer> v) {
            return new Integer2DoubleVector(v);
        }
        
    }
    
    public static class LSAStreamApply implements VectorFunction<Double,Double> {

        private final File file;
        private final int W;
        private final int J;
        private final VectorFunction<Integer,Double> tfidf;

        
        private final double[] mid, r;
        
        public LSAStreamApply(File file, int W, int J, double[][] df) throws IOException {
            this.file = file;
            this.W = W;
            this.J = J;
            this.tfidf = df == null ? new IdentityApply() : new TFIDFApply(df, W);
            this.mid = new double[J];
            this.r = new double[2*W];
        }

        @Override
        public Vector<Double> apply(Vector<Double> v) {
            System.err.print(".");
            try {
                Arrays.fill(mid, 0.0);
                {
                    final DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(file));
                    int N = 0;
                    final SparseIntArray doc = new SparseIntArray(2 * W);
                    while (data.available() > 0) {
                        try {
                            int i = data.readInt();
                            if (i == 0) {
                                if (N % 2 == 1) {
                                    mid[N / 2] = tfidf.apply(doc).innerProduct(v);
                                    doc.clear();
                                }
                                N++;
                            } else {
                                doc.inc(i + (N % 2) * W - 1);
                            }
                        } catch (EOFException x) {
                            break;
                        }
                    }
                }
                {
                    final DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(file));
                    int N = 0;
                    Arrays.fill(r, 0.0);
                    final SparseIntArray doc = new SparseIntArray(2 * W);
                    while (data.available() > 0) {
                        try {
                            int i = data.readInt();
                            if (i == 0) {
                                if (N % 2 == 1) {
                                    final Iterator<Map.Entry<Integer, Double>> iter = tfidf.apply(doc).entrySet().iterator();
                                    while(iter.hasNext()) {
                                        final Map.Entry<Integer, Double> e = iter.next();
                                        r[e.getKey()] += mid[N/2] * e.getValue();
                                    }
                                    doc.clear();
                                }
                                N++;
                            } else {
                                doc.inc(i + (N % 2) * W - 1);
                            }
                        } catch (EOFException x) {
                            break;
                        }
                    }
                    return new RealVector(r);
                }
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
    }
}
