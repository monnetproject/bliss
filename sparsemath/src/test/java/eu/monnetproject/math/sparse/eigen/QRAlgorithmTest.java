/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.DoubleArrayMatrix;
import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.TridiagonalMatrix;
import eu.monnetproject.math.sparse.eigen.QRAlgorithm.Solution;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealVector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author jmccrae
 */
public class QRAlgorithmTest {

    public QRAlgorithmTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of givens method, of class QRAlgorithm.
     */
    @Test
    public void testGivens() {
        System.out.println("givens");
        final Random random = new Random();
        for (int i = 0; i < 10; i++) {
            double a = random.nextDouble();
            double b = random.nextDouble();
            double[] result = QRAlgorithm.givens(a, b);
            final Array2DRowRealMatrix A = new Array2DRowRealMatrix(new double[][]{
                        result,
                        new double[]{-result[1], result[0]}
                    });
            final ArrayRealVector v = new ArrayRealVector(new double[]{a, b});
            final RealVector v2 = A.transpose().operate(v);
            assertEquals(0.0, v2.getEntry(1), 0.0001);
        }
    }

    /**
     * Test of wilkinsonShift method, of class QRAlgorithm.
     */
    @Test
    public void testWilkinsonShift() {
        System.out.println("wilkinsonShift");
        double[] alpha = {1, 2, 3, 4};
        double[] beta = {1, 1, .01};
        double[] expAlpha = {.5000, 1.785, 3.7140, 4.002497};
        double[] expBeta = {.5916, .1808, .0000044};
        QRAlgorithm.wilkinsonShift(alpha, beta, 0, 4, new SequenceOfGivens());
        assertArrayEquals(expAlpha, alpha, 0.01);
        assertArrayEquals(expBeta, beta, 0.01);
    }

    //@Test
    //public void testQRSolve() {
    //    System.out.println("qrSolve");
    //    // This matrix is just sensitive
    //    final SparseMatrix<Double> A1 = SparseMatrix.fromArray(new double[][]{
    //                {2, 0, 0, 0},
    //                {0, 5, 0, 0},
    //                {0, 0, 3, 0},
    //                {0, 0, 0, 3}
    //            });
    //    final double[] l1 = QRAlgorithm.qrSolve(A1, 0.01).values();
    //    Arrays.sort(l1);
    //    Assert.assertArrayEquals(new double[]{2, 3, 3, 5}, l1, 0.01);


    //    final SparseMatrix<Double> A2 = SparseMatrix.fromArray(new double[][]{
    //                {3, 0, 2, 0},
    //                {0, 4, 2, 1},
    //                {2, 2, 1, 0},
    //                {0, 1, 0, 0}
    //            });
    //    final double[] l2 = QRAlgorithm.qrSolve(A2, 0.01).values();
    //    Arrays.sort(l2);
    //    Assert.assertArrayEquals(new double[]{-1, -0.0514, 3.517, 5.534}, l2, 0.01);

    //    final SparseMatrix<Double> A3 = SparseMatrix.fromArray(new double[][]{
    //                {2, 0, 0, 0},
    //                {0, 0, 0, 3},
    //                {0, 0, 1, 3},
    //                {0, 3, 3, 1}
    //            });
    //    final double[] l3 = QRAlgorithm.qrSolve(A3, 0.01).values();
    //    Arrays.sort(l3);
    //    Assert.assertArrayEquals(new double[]{-3.533, 0.507, 2, 5.026}, l3, 0.01);

    //    final SparseMatrix<Double> A4 = SparseMatrix.fromArray(new double[][]{
    //                {2, 0, 0, 0},
    //                {0, 5, 0, 2},
    //                {0, 0, 3, 0},
    //                {0, 2, 0, 5}
    //            });
    //    final double[] l4 = QRAlgorithm.qrSolve(A4, 0.01).values();
    //    Arrays.sort(l4);
    //    Assert.assertArrayEquals(new double[]{2, 3, 3, 7}, l4, 0.01);


    //    final SparseMatrix<Double> A5 = SparseMatrix.fromArray(new double[][]{
    //                {0, 0, 1, 0},
    //                {0, 0, 0, 3},
    //                {1, 0, 0, 3},
    //                {0, 3, 3, 0}
    //            });
    //    final double[] l5 = QRAlgorithm.qrSolve(A5, 0.01).values();
    //    Arrays.sort(l5);
    //    Assert.assertArrayEquals(new double[]{-4.3028, -0.6972, 0.6972, 4.3028}, l5, 0.01);

    //    final TridiagonalMatrix A6 = new TridiagonalMatrix(new double[]{0, 3, 4, 0}, new double[]{4, 0, 2});
    //    final double[] l6 = QRAlgorithm.qrSolve(0.001, A6, TrivialEigenvalues.find(A6, true)).values();
    //    Arrays.sort(l6);
    //    Assert.assertArrayEquals(new double[]{-2.7720019, -0.8284271, 4.8284271, 5.7720019}, l6, 0.01);

    //    final TridiagonalMatrix A7 = new TridiagonalMatrix(new double[]{1, 4, 0, 0}, new double[]{3, 4, 4});
    //    final double[] l7 = QRAlgorithm.qrSolve(0.001, A7, TrivialEigenvalues.find(A7, true)).values();
    //    Arrays.sort(l7);
    //    Assert.assertArrayEquals(new double[]{-5.1698172, -0.67563241, 2.8727617, 7.9726879}, l7, 0.01);

    //    final Random random = new Random();
    //    for (int n = 0; n < 10; n++) {
    //        //final SparseMatrix<Double> arr = new SparseMatrix<Double>(4, 4, Vectors.AS_REALS);
    //        final TridiagonalMatrix arr = new TridiagonalMatrix(4);
    //        for (int i = 0; i < 4; i++) {
    //            for (int j = i == 0 ? 0 : i - 1; j < 4 && j <= i + 1; j++) {
    //                final double d = random.nextDouble();
    //                if (d >= 0.5) {
    //                    arr.set(j, i, (int) ((d - 0.5) * 10) + 1);
    //                    arr.set(i, j, (int) ((d - 0.5) * 10) + 1);
    //                }
    //            }
    //        }
    //        final double[][] arr2 = new double[4][];
    //        for (int i = 0; i < 4; i++) {
    //            arr2[i] = arr.row(i).toDoubleArray();
    //        }
    //        final Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(arr2);
    //        if (!new LUDecompositionImpl(matrix).getSolver().isNonSingular()) {
    //            n--;
    //            continue;
    //        }
    //        System.err.println("Matrix:" + arr.toString());
    //        final TrivialEigenvalues<Double> trivial = TrivialEigenvalues.find(arr, true);
    //        if (trivial.isTrivial()) {
    //            n--;
    //            continue;
    //        }
    //        final double[] result = QRAlgorithm.qrSolve(0.001, arr, trivial).values();
    //        final double[] expected = new EigenDecompositionImpl(matrix, 0.001).getRealEigenvalues();
    //        Arrays.sort(expected);
    //        Arrays.sort(result);
    //        System.out.println("QR:");
    //        for (int i = 0; i < 4; i++) {
    //            System.out.println(arr.row(i).toString());
    //        }
    //        System.out.println(Arrays.toString(expected));
    //        System.out.println(Arrays.toString(result));
    //        assertArrayEquals(expected, result, 0.01);
    //    }

    //    for (int n = 0; n < 10; n++) {
    //        final TridiagonalMatrix arr = new TridiagonalMatrix(4);
    //        for (int i = 0; i < 4; i++) {
    //            for (int j = i == 0 ? 0 : i - 1; j < 4 && j <= i + 1; j++) {
    //                final double d = random.nextDouble();
    //                if (d >= 0.5) {
    //                    arr.set(j, i, (int) ((d - 0.5) * 10) + 1);
    //                    arr.set(i, j, (int) ((d - 0.5) * 10) + 1);
    //                }
    //            }
    //        }
    //        final double[][] arr2 = new double[4][];
    //        for (int i = 0; i < 4; i++) {
    //            arr2[i] = arr.row(i).toDoubleArray();
    //        }
    //        final Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(arr2);
    //        if (!new LUDecompositionImpl(matrix).getSolver().isNonSingular()) {
    //            n--;
    //            continue;
    //        }
    //        final TrivialEigenvalues<Double> trivial = TrivialEigenvalues.find(arr, true);
    //        if (trivial.isTrivial()) {
    //            n--;
    //            continue;
    //        }
    //        final double[] result = QRAlgorithm.qrSolve(0.001, arr, trivial).values();
    //        final double[] expected = new EigenDecompositionImpl(matrix, 0.001).getRealEigenvalues();
    //        if (expected.length != 4) {
    //            n--;
    //            continue;
    //        }
    //        Arrays.sort(expected);
    //        Arrays.sort(result);
    //        System.out.println("QR:");
    //        for (int i = 0; i < 4; i++) {
    //            System.out.println(arr.row(i).toString());
    //        }
    //        System.out.println(Arrays.toString(expected));
    //        System.out.println(Arrays.toString(result));
    //        assertArrayEquals(expected, result, 0.01);
    //    }
    //}

//    @Test
//    public void testEigenvector2() {
//        System.err.println("eigenvector");
//        final SparseMatrix<Integer> A1 = SparseMatrix.fromArray(new int[][]{
//                    {5, 5, 0, 0},
//                    {5, 5, 5, 5},
//                    {0, 5, 5, 0},
//                    {0, 5, 0, 1}
//                });
//        final double[] result1 = QRAlgorithm.eigenvector(A1, 5).get(0);
//        assertArrayEquals(new double[]{-1, 0, 1, 0}, result1, 0.01);
//    }
//
//    @Test
//    public void testEigenvector() {
//        System.err.println("eigenvector");
//        final SparseMatrix<Integer> A1 = SparseMatrix.fromArray(new int[][]{
//                    {5, 4},
//                    {1, 2}
//                });
//        final double[] result1 = QRAlgorithm.eigenvector(A1, 6).get(0);
//        assertArrayEquals(new double[]{4, 1}, result1, 0.01);
//
//        final double[] result2 = new double[4];
//        final SparseMatrix<Integer> A2 = SparseMatrix.fromArray(new int[][]{
//                    {0, 0, 2, 1},
//                    {0, 1, 1, 0},
//                    {2, 1, 0, 0},
//                    {1, 0, 0, 0}
//                });
//        QRAlgorithm.eigenvector(A2, 1);
//
//        final Random random = new Random();
//        for (int n = 0; n < 10; n++) {
//            final SparseIntArray[] arr = new SparseIntArray[4];
//            for (int i = 0; i < 4; i++) {
//                arr[i] = new SparseIntArray(4);
//            }
//            for (int i = 0; i < 4; i++) {
//                for (int j = i; j < 4; j++) {
//                    final double d = random.nextDouble();
//                    if (d >= 0.5) {
//                        arr[j].put(i, (int) ((d - 0.5) * 10) + 1);
//                        arr[i].put(j, (int) ((d - 0.5) * 10) + 1);
//                    }
//                }
//            }
//            final double[][] arr2 = new double[4][];
//            for (int i = 0; i < 4; i++) {
//                arr2[i] = arr[i].toDoubleArray();
//            }
//            final Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(arr2);
//            if (!new LUDecompositionImpl(matrix).getSolver().isNonSingular()) {
//                n--;
//                continue;
//            }
//            final EigenDecompositionImpl eigendecomp = new EigenDecompositionImpl(matrix, 0.001);
//            final double[] eigenvalues = eigendecomp.getRealEigenvalues();
//
//            System.out.println("Eigenvector:");
//            for (int i = 0; i < 4; i++) {
//                System.out.println(arr[i].toString());
//            }
//            for (int i = 0; i < 4; i++) {
//                final List<double[]> eigenvectors = QRAlgorithm.eigenvector(SparseMatrix.fromArray(arr2), eigenvalues[i]);
//                final double[] expected = eigendecomp.getEigenvector(i).getData();
//                if (eigenvectors.size() == 1) {
//                    final double[] eigenvector = eigenvectors.get(0);
//                    double norm = 1.0;
//                    for (int j = 0; j < 4; j++) {
//                        if (Math.abs(eigenvector[j]) > 1e-8 && Math.abs(expected[j]) > 1e-8) {
//                            norm = expected[j] / eigenvector[j];
//                            break;
//                        }
//                    }
//                    for (int j = 0; j < 4; j++) {
//                        eigenvector[j] *= norm;
//                    }
//
//                    System.out.println(eigenvalues[i]);
//                    System.out.println(Arrays.toString(expected));
//                    System.out.println(">" + Arrays.toString(eigenvector));
//                    assertArrayEquals(expected, eigenvector, 0.01);
//                } else {
//                    System.out.println(eigenvalues[i]);
//                    System.out.println(Arrays.toString(expected));
//                    for (double[] eigenvector : eigenvectors) {
//                        if (compare(eigenvector, expected)) {
//                            return;
//                        }
//                        System.out.println(">" + Arrays.toString(eigenvector));
//                    }
//
//                    fail("no matching eigenvector calculated");
//                }
//            }
//        }
//    }
    private boolean compare(double[] expected, double[] result) {
        double norm = 1.0;
        for (int j = 0; j < 4; j++) {
            if (expected[j] != 0.0 && result[j] != 0.0) {
                norm = expected[j] / result[j];
                break;
            }
        }
        double diff = 0.0;
        for (int j = 0; j < 4; j++) {
            result[j] *= norm;
            diff += Math.abs(expected[j] - result[j]);
        }
        return diff < 0.01;
    }
}
