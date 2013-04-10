/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.Vector;
import java.util.Arrays;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jmccrae
 */
public class CholeskyDecompositionTest {

    public CholeskyDecompositionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    private static final Random random = new Random();

    private double[][] randomSPDMatrix(int N) {
        double[][] A = new double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                A[i][j] = random.nextDouble();
            }
        }
        double[][] B = new double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                for (int k = 0; k < N; k++) {
                    B[i][j] += A[i][k] * A[j][k];
                }
            }
        }
        return B;
    }

    private double[] randomVector(int N) {
        double[] v = new double[N];
        for (int i = 0; i < N; i++) {
            v[i] = random.nextDouble();
        }
        return v;
    }

    /**
     * Test of denseDecomp method, of class CholeskyDecomposition.
     */
    @Test
    public void testDenseDecomp() {
        {
            double[][] A = {
                {2.1004668741991033, 1.560520364693791, 1.1091643251699563},
                {1.560520364693791, 1.3632703141496776, 0.6941647267611244},
                {1.1091643251699563, 0.6941647267611244, 0.8095983575081743}
            };
            CholeskyDecomposition.denseDecomp(A);
        }

        System.out.println("denseDecomp");
        for (int it = 0; it < 100; it++) {
            final int N = 3;
            double[][] A = randomSPDMatrix(N);
            double[][] B = CholeskyDecomposition.denseDecomp(A);
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    double p = 0.0;
                    for (int k = 0; k < N; k++) {
                        p += B[i][k] * B[j][k];
                    }
                    Assert.assertEquals(A[i][j], p, 0.00001);
                }
            }
        }
    }
    
    @Test
    public void testDenseInplaceDecomp() {
        System.out.println("denseInplaceDecomp");
        for (int it = 0; it < 100; it++) {
            final int N = 3;
            double[][] A = randomSPDMatrix(N);
            double[][] B = new double[N][N];
            for(int i = 0; i < N; i++) {
                System.arraycopy(A[i], 0, B[i], 0, N);
            }
            CholeskyDecomposition.denseInplaceDecomp(B);
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    double p = 0.0;
                    for (int k = 0; k < N; k++) {
                        p += B[i][k] * B[j][k];
                    }
                    Assert.assertEquals(A[i][j], p, 0.00001);
                }
            }
        }
    }

    @Test
    public void testSparseDecomp() {
        System.out.println("sparseDecomp");
        for (int it = 0; it < 100; it++) {
            final int N = 3;
            SparseMatrix<Double> A = SparseMatrix.fromArray(randomSPDMatrix(N));
            SparseMatrix<Double> B = CholeskyDecomposition.decomp(A, true);
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    double p = 0.0;
                    for (int k = 0; k < N; k++) {
                        p += B.doubleValue(i, k) * B.doubleValue(j, k);
                    }
                    Assert.assertEquals(A.doubleValue(i, j), p, 0.00001);
                }
            }
        }
    }

    @Test
    public void testSolve() {
        System.out.println("solve");
        final int N = 3;

        for (int it = 0; it < 100; it++) {
            SparseMatrix<Double> A = SparseMatrix.fromArray(randomSPDMatrix(N));
            A = CholeskyDecomposition.decomp(A, true);
            final SparseRealArray y = SparseRealArray.fromArray(randomVector(N));
            final Vector<Double> b = A.mult(y);
            Assert.assertArrayEquals(y.toDoubleArray(), CholeskyDecomposition.solve(A, b).toDoubleArray(), 0.0001);
        }
    }

    @Test
    public void testSolveT() {
        System.out.println("solveT");

        {
            double[][] A = {
                {1.011, 0.000, 0.000},
                {0.859, 0.443, 0.000},
                {0.875, 0.251, 0.225}
            };
            double[] y = {0.806,0.424,0.374};
            double[] b = SparseMatrix.fromArray(A).multTransposed(SparseRealArray.fromArray(y)).toDoubleArray();
            System.out.println(Arrays.toString(b));
        }
        
        for (int it = 0; it < 100; it++) {
            final int N = 3;
            SparseMatrix<Double> A = SparseMatrix.fromArray(randomSPDMatrix(N));
            A = CholeskyDecomposition.decomp(A, true);
            final SparseRealArray y = SparseRealArray.fromArray(randomVector(N));
            final Vector<Double> b = A.multTransposed(y);
            Assert.assertArrayEquals(y.toDoubleArray(), CholeskyDecomposition.solveT(A, b).toDoubleArray(), 0.0001);
        }
    }
}
