/**
 * *******************************************************************************
 * This is a patented method. This code is not to be made publicly available!
 * *******************************************************************************
 */
package eu.monnetproject.bliss.kcca;

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.ParallelBinarizedReader;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.math.sparse.Vectors;
import eu.monnetproject.math.sparse.eigen.ArnoldiAlgorithm;
import eu.monnetproject.math.sparse.eigen.CholeskyDecomposition;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition.Solution;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jmccrae
 */
public class OPCATrain {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final double kappa = opts.doubleValue("kappa", 0.67, "The kappa value");
        final File corpus = opts.roFile("corpus[.gz|.bz2]", "The training corpus");
        final int W = opts.intValue("W", "The number of distinct tokens in the corpus");
        final int J = opts.intValue("J", "The number of documents (per language)");
        final int K = opts.intValue("K", "The number of topics to use");
        final File outFile = opts.woFile("model", "The file to save the model to");

        if (!opts.verify(KCCATrain.class)) {
            return;
        }
        final Solution soln = train(corpus, W, J, K, kappa);
        System.err.println("Writing model");
        write(soln, outFile);
    }

    /**
     * Calculate the document frequency
     *
     * @param corpus
     * @param W
     * @param J
     * @return A vector index [L][W]
     * @throws IOException
     */
    private static double[][] calcDF(File corpus, int W, int J) throws IOException {
        final ParallelBinarizedReader in = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));
        SparseIntArray[] s;
        int j = 0;
        final double[][] df = new double[2][W];
        while ((s = in.nextFreqPair(W)) != null && j < J) {
            for (int l = 0; l < 2; l++) {
                for (int w : s[l].keySet()) {
                    df[l][w]++;
                }
            }
            j++;
        }
        for (int l = 0; l < 2; l++) {
            for (int w = 0; w < W; w++) {
                df[l][w] = Math.log((double) J / df[l][w]);
            }
        }
        return df;
    }
    private static double[][] calcMean(File corpus, int W, int J, double[][]df) throws IOException {
        final ParallelBinarizedReader in = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));
        SparseIntArray[] s;
        int j = 0;
        final double[][] mean = new double[2][W];
        while ((s = in.nextFreqPair(W)) != null && j < J) {
            for (int l = 0; l < 2; l++) {
                for (int w : s[l].keySet()) {
                    mean[l][w] += l(s[l].doubleValue(w)) * df[l][w];
                }
            }
            j++;
        }
        for (int l = 0; l < 2; l++) {
            for (int w = 0; w < W; w++) {
                mean[l][w] /= J;
            }
        }
        return mean;
    }

    private static double l(double d) {
        return Math.log(d + 1);
    }

    /**
     * Calculate N = 1/J * (D_1 - D_2) ^ T (D_1 - D_2) + gamma * I
     *
     * @param corpus
     * @param W
     * @param J
     * @param gamma
     * @return
     */
    public static SparseMatrix<Double> calcN(File corpus, int W, int J, double[][] df, double gamma) throws IOException {
        final SparseMatrix<Double> N = new SparseMatrix<Double>(W - 1, W - 1, Vectors.AS_SPARSE_REALS);
        final ParallelBinarizedReader in = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));
        SparseIntArray[] s;
        int j1 = 0;
        while ((s = in.nextFreqPair(W)) != null && j1 < J) {
            for (int w1 : s[0].keySet()) {
                for (int w2 : s[0].keySet()) {
                    N.add(w1 - 1, w2 - 1, (l(s[0].doubleValue(w1)) * df[0][w1] - l(s[1].doubleValue(w1)) * df[1][w1])
                            * (l(s[0].doubleValue(w2)) * df[0][w2] - l(s[1].doubleValue(w2)) * df[1][w2]));
                }
            }

            for (int w1 : s[1].keySet()) {
                if (s[0].doubleValue(w1) == 0.0) {
                    for (int w2 : s[1].keySet()) {
                        if (s[0].doubleValue(w2) == 0.0) {
                            N.add(w1 - 1, w2 - 1, l(s[1].doubleValue(w1)) * df[1][w1]
                                    * l(s[1].doubleValue(w2)) * df[1][w1]);
                        }
                    }
                }
            }
            j1++;
        }

        for (int i = 0; i < W - 1; i++) {
            N.add(i, i, gamma);
        }

        return N;
    }

    private static SparseMatrix<Double> calcS(File corpus, int W, int J, double[][] mean, double[][] df) throws IOException {
        final SparseMatrix<Double> S = new SparseMatrix<Double>(W -1 , W - 1, Vectors.AS_REALS);
        final ParallelBinarizedReader in = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));
        SparseIntArray[] s;
        int j1 = 0;
        while ((s = in.nextFreqPair(W)) != null && j1 < J) {
            
            for (int w1 : s[0].keySet()) {
                for (int w2 : s[0].keySet()) {
                    S.add(w1 - 1, w2 - 1, ((l(s[0].doubleValue(w1)) * df[0][w1] * l(s[0].doubleValue(w2)) * df[1][w2])
                            + (l(s[1].doubleValue(w1)) * df[0][w1] - l(s[1].doubleValue(w2)) * df[1][w2]))/ 2.0);
                }
            }
        }
        
        for(int w1 = 0; w1 < W - 1; w1++) {
            for(int w2 = 0; w2 < W - 1; w2++) {
                S.add(w1,w2,mean[0][w1]*mean[0][w2] + mean[1][w1]*mean[1][w2]);
            }
        }
        
        return null;
    }

    private static void write(Solution soln, File outFile) throws IOException {
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(outFile));
        out.writeInt(soln.S.length);
        out.writeInt(soln.U[0].length*2);

        for (int i = 0; i < soln.U.length; i++) {
            for (int j = 0; j < soln.U[i].length; j++) {
                out.writeDouble(soln.U[i][j]);
            }
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
    
    private static Solution train(File corpus, int W, int J, int K, double gamma) throws IOException {
        System.err.println("Calc DF");
        final double[][] df = calcDF(corpus, W, J);
        System.err.println("Calc Mean");
        final double[][] mean = calcMean(corpus, W, J, df);
        System.err.println("Calc N");
        final SparseMatrix<Double> N = calcN(corpus, W, J, df, gamma);
        System.err.println("Calc S");
        final SparseMatrix<Double> S = calcS(corpus, W, J, mean, df);
        System.err.println("Cholesky");
        final SparseMatrix<Double> Ni = CholeskyDecomposition.decomp(N, true);
        System.err.println("Arnoldi");
        return SingularValueDecomposition.nonsymmEigen(new NiS(corpus, J, W, S, Ni), W, K, 1e-50);
    }
    
    private static class NiS implements VectorFunction<Double, Double> {

        private final File corpus;
        private final int J;
        private final int W;
        private final SparseMatrix<Double> S, Ni;

        public NiS(File corpus, int J, int W, SparseMatrix<Double> S, SparseMatrix<Double> Ni) {
            this.corpus = corpus;
            this.J = J;
            this.W = W;
            this.S = S;
            this.Ni = Ni;
        }

        @Override
        public Vector<Double> apply(Vector<Double> v) {
            final Vector<Double> v2 = S.mult(v);
            final Vector<Double> v3 = CholeskyDecomposition.solve(Ni, v2);
            final Vector<Double> v4 = CholeskyDecomposition.solveT(Ni, v3);
            return v4;
        }
    }
}
