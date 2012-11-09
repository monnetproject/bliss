/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.math.sparse;

import eu.monnetproject.math.sparse.Vectors.Factory;
import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author jmccrae
 */
public class SparseMatrixTest {

    public SparseMatrixTest() {
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
     * Test of mult method, of class SparseMatrix.
     */
    @Test
    public void testMult_Vector() {
        System.out.println("mult");
        Vector<Double> x = new RealVector(new double[]{1.0, 2.0, 3.0});
        SparseMatrix<Double> instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 1);
        instance.set(0, 1, 1);
        instance.set(1, 1, 1);
        instance.set(1, 2, 2);
        instance.set(2, 1, 3);
        Vector expResult = new RealVector(new double[]{3.0, 8.0, 6.0});
        Vector result = instance.mult(x);
        assertEquals(expResult, result);
    }

    /**
     * Test of mult method, of class SparseMatrix.
     */
    @Test
    public void testMult_Vector_VectorsFactory() {
        System.out.println("mult");
        Vector<Integer> x = new IntVector(new int[]{1, 2, 3});
        Factory<Double> using = Vectors.AS_SPARSE_REALS;
        SparseMatrix<Double> instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 0.5);
        instance.set(0, 1, 0.5);
        instance.set(1, 1, 0.5);
        instance.set(1, 2, 1.0);
        instance.set(2, 1, 1.5);
        Vector<Double> expResult = SparseRealArray.fromArray(new double[]{1.5, 4.0, 3.0});
        Vector result = instance.mult(x, using);
        assertEquals(expResult, result);
    }

    /**
     * Test of multIntDense method, of class SparseMatrix.
     */
    @Test
    public void testMultIntDense() {
        System.out.println("multIntDense");
        Vector<Integer> x = new IntVector(new int[]{1, 2, 3});
        SparseMatrix<Double> instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 1);
        instance.set(0, 1, 1);
        instance.set(1, 1, 1);
        instance.set(1, 2, 2);
        instance.set(2, 1, 3);
        int[] expResult = new int[]{3, 8, 6};
        int[] result = instance.multIntDense(x);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of multRealDense method, of class SparseMatrix.
     */
    @Test
    public void testMultRealDense() {
        System.out.println("multRealDense");
        Vector<Double> x = new RealVector(new double[]{1.0, 2.0, 3.0});
        SparseMatrix<Double> instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 1);
        instance.set(0, 1, 1);
        instance.set(1, 1, 1);
        instance.set(1, 2, 2);
        instance.set(2, 1, 3);
        double[] expResult = {3.0, 8.0, 6.0};
        double[] result = instance.multRealDense(x);
        assertArrayEquals(expResult, result, 0.0);
    }

    /**
     * Test of selfOuterProduct method, of class SparseMatrix.
     */
    @Test
    public void testSelfOuterProduct() {
        System.out.println("selfOuterProduct");
        Matrix<Double> outerProduct = new SparseMatrix<Double>(3, 3, Vectors.AS_SPARSE_REALS);
        SparseMatrix instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 1);
        instance.set(0, 1, 1);
        instance.set(1, 1, 1);
        instance.set(1, 2, 2);
        instance.set(2, 1, 3);
        instance.selfOuterProduct(outerProduct);
        assertEquals(2, outerProduct.value(0, 0).doubleValue(), 1e-6);
        assertEquals(1, outerProduct.value(0, 1).doubleValue(), 1e-6);
        assertEquals(3, outerProduct.value(0, 2).doubleValue(), 1e-6);
        assertEquals(1, outerProduct.value(1, 0).doubleValue(), 1e-6);
        assertEquals(5, outerProduct.value(1, 1).doubleValue(), 1e-6);
        assertEquals(3, outerProduct.value(1, 2).doubleValue(), 1e-6);
        assertEquals(3, outerProduct.value(2, 0).doubleValue(), 1e-6);
        assertEquals(3, outerProduct.value(2, 1).doubleValue(), 1e-6);
        assertEquals(9, outerProduct.value(2, 2).doubleValue(), 1e-6);
    }

    /**
     * Test of selfInnerProduct method, of class SparseMatrix.
     */
    @Test
    public void testSelfInnerProduct() {
        System.out.println("selfInnerProduct");
        Matrix<Double> innerProduct = new SparseMatrix<Double>(3, 3, Vectors.AS_SPARSE_REALS);
        SparseMatrix instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 1);
        instance.set(0, 1, 1);
        instance.set(1, 1, 1);
        instance.set(1, 2, 2);
        instance.set(2, 1, 3);
        instance.selfInnerProduct(innerProduct);
        assertEquals(1, innerProduct.value(0, 0).doubleValue(), 1e-6);
        assertEquals(1, innerProduct.value(0, 1).doubleValue(), 1e-6);
        assertEquals(0, innerProduct.value(0, 2).doubleValue(), 1e-6);
        assertEquals(1, innerProduct.value(1, 0).doubleValue(), 1e-6);
        assertEquals(11, innerProduct.value(1, 1).doubleValue(), 1e-6);
        assertEquals(2, innerProduct.value(1, 2).doubleValue(), 1e-6);
        assertEquals(0, innerProduct.value(2, 0).doubleValue(), 1e-6);
        assertEquals(2, innerProduct.value(2, 1).doubleValue(), 1e-6);
        assertEquals(4, innerProduct.value(2, 2).doubleValue(), 1e-6);
    }

    /**
     * Test of transpose method, of class SparseMatrix.
     */
    @Test
    public void testTranspose() {
        System.out.println("transpose");
        SparseMatrix instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 1);
        instance.set(0, 1, 1);
        instance.set(1, 1, 1);
        instance.set(1, 2, 2);
        instance.set(2, 1, 3);
        SparseMatrix expResult = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        expResult.set(0, 0, 1);
        expResult.set(1, 0, 1);
        expResult.set(1, 1, 1);
        expResult.set(2, 1, 2);
        expResult.set(1, 2, 3);
        SparseMatrix result = instance.transpose();
        assertEquals(expResult, result);
    }

    /**
     * Test of toArrays method, of class SparseMatrix.
     */
    @Test
    public void testToArrays() {
        System.out.println("toArrays");
        SparseMatrix instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 1);
        instance.set(0, 1, 1);
        instance.set(1, 1, 1);
        instance.set(1, 2, 2);
        instance.set(2, 1, 3);
        double[][] expResult = {
            {1.0, 1.0, 0.0},
            {0.0, 1.0, 2.0},
            {0.0, 3.0, 0.0}
        };
        double[][] result = instance.toArrays();
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(expResult[i], result[i], 0.0);
        }
    }

    /**
     * Test of fromArray method, of class SparseMatrix.
     */
    @Test
    public void testFromArray_doubleArrArr() {
        System.out.println("fromArray");
        double[][] arrs = {
            {1.0, 1.0, 0.0},
            {0.0, 1.0, 2.0},
            {0.0, 3.0, 0.0}
        };
        SparseMatrix expResult = new SparseMatrix<Double>(3, 3, Vectors.AS_SPARSE_REALS);
        expResult.set(0, 0, 1);
        expResult.set(0, 1, 1);
        expResult.set(1, 1, 1);
        expResult.set(1, 2, 2);
        expResult.set(2, 1, 3);
        SparseMatrix result = SparseMatrix.fromArray(arrs);
        assertEquals(expResult, result);
    }

    /**
     * Test of fromArray method, of class SparseMatrix.
     */
    @Test
    public void testFromArray_intArrArr() {
        System.out.println("fromArray");
        int[][] arrs = {
            {1, 1, 0},
            {0, 1, 2},
            {0, 3, 0}
        };
        SparseMatrix expResult = new SparseMatrix<Integer>(3, 3, Vectors.AS_SPARSE_INTS);
        expResult.set(0, 0, 1);
        expResult.set(0, 1, 1);
        expResult.set(1, 1, 1);
        expResult.set(1, 2, 2);
        expResult.set(2, 1, 3);
        SparseMatrix result = SparseMatrix.fromArray(arrs);
        assertEquals(expResult, result);
    }

    /**
     * Test of row method, of class SparseMatrix.
     */
    @Test
    public void testRow() {
        System.out.println("row");
    }

    /**
     * Test of rows method, of class SparseMatrix.
     */
    @Test
    public void testRows() {
        System.out.println("rows");
    }

    /**
     * Test of cols method, of class SparseMatrix.
     */
    @Test
    public void testCols() {
        System.out.println("cols");
    }

    /**
     * Test of value method, of class SparseMatrix.
     */
    @Test
    public void testValue() {
        System.out.println("value");
    }

    /**
     * Test of intValue method, of class SparseMatrix.
     */
    @Test
    public void testIntValue() {
        System.out.println("intValue");
        int i = 0;
        int j = 1;
        SparseMatrix instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 1);
        instance.set(0, 1, 1.5);
        instance.set(1, 1, 1);
        instance.set(1, 2, 2);
        instance.set(2, 1, 3);
        int expResult = 1;
        int result = instance.intValue(i, j);
        assertEquals(expResult, result);
    }

    /**
     * Test of doubleValue method, of class SparseMatrix.
     */
    @Test
    public void testDoubleValue() {
        System.out.println("doubleValue");
        int i = 0;
        int j = 1;
        SparseMatrix instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 1);
        instance.set(0, 1, 1.5);
        instance.set(1, 1, 1);
        instance.set(1, 2, 2);
        instance.set(2, 1, 3);
        double expResult = 1.5;
        double result = instance.doubleValue(i, j);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of set method, of class SparseMatrix.
     */
    @Test
    public void testSet_3args_1() {
        System.out.println("set");
    }

    /**
     * Test of set method, of class SparseMatrix.
     */
    @Test
    public void testSet_3args_2() {
        System.out.println("set");
    }

    /**
     * Test of set method, of class SparseMatrix.
     */
    @Test
    public void testSet_3args_3() {
        System.out.println("set");
    }

    /**
     * Test of add method, of class SparseMatrix.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        Matrix<Double> matrix = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        matrix.set(0, 0, 1);
        matrix.set(0, 1, 1.5);
        matrix.set(1, 1, 1);
        matrix.set(1, 2, 2);
        matrix.set(2, 1, 3);
        SparseMatrix instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 3);
        instance.set(0, 2, 1.5);
        instance.set(1, 0, 2);
        instance.set(1, 1, 1);
        instance.set(2, 1, 3);
        instance.add(matrix);
        assertEquals(4, instance.doubleValue(0, 0), 0.0);
        assertEquals(1.5, instance.doubleValue(0, 1), 0.0);
        assertEquals(1.5, instance.doubleValue(0, 2), 0.0);
        assertEquals(2, instance.doubleValue(1, 0), 0.0);
        assertEquals(2, instance.doubleValue(1, 1), 0.0);
        assertEquals(2, instance.doubleValue(1, 2), 0.0);
        assertEquals(0, instance.doubleValue(2, 0), 0.0);
        assertEquals(6, instance.doubleValue(2, 1), 0.0);
        assertEquals(0, instance.doubleValue(2, 2), 0.0);
    }

    /**
     * Test of sub method, of class SparseMatrix.
     */
    @Test
    public void testSub() {
        System.out.println("sub");
        Matrix<Double> matrix = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        matrix.set(0, 0, 1);
        matrix.set(0, 1, 1.5);
        matrix.set(1, 1, 1);
        matrix.set(1, 2, 2);
        matrix.set(2, 1, 3);
        SparseMatrix instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 3);
        instance.set(0, 2, 1.5);
        instance.set(1, 0, 2);
        instance.set(1, 1, 1);
        instance.set(2, 1, 3);
        matrix.sub(instance);
        assertEquals(-2.0, matrix.doubleValue(0, 0), 0.0);
        assertEquals(1.5, matrix.doubleValue(0, 1), 0.0);
        assertEquals(-1.5, matrix.doubleValue(0, 2), 0.0);
        assertEquals(-2, matrix.doubleValue(1, 0), 0.0);
        assertEquals(0, matrix.doubleValue(1, 1), 0.0);
        assertEquals(2, matrix.doubleValue(1, 2), 0.0);
        assertEquals(0, matrix.doubleValue(2, 0), 0.0);
        assertEquals(0, matrix.doubleValue(2, 1), 0.0);
        assertEquals(0, matrix.doubleValue(2, 2), 0.0);
    }

    /**
     * Test of isSymmetric method, of class SparseMatrix.
     */
    @Test
    public void testIsSymmetric() {
        System.out.println("isSymmetric");
        SparseMatrix instance = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.set(0, 0, 3);
        instance.set(0, 2, 1.5);
        instance.set(1, 0, 2);
        instance.set(1, 1, 1);
        instance.set(2, 1, 3);
        boolean expResult = false;
        boolean result = instance.isSymmetric();
        assertEquals(expResult, result);
        final SparseMatrix<Double> aat = new SparseMatrix<Double>(3, 3, Vectors.AS_REALS);
        instance.selfInnerProduct(aat);
        assertEquals(true, aat.isSymmetric());
    }

    /**
     * Test of fromFile method, of class SparseMatrix.
     */
    @Test
    public void testFromFile() throws Exception {
        System.out.println("fromFile");
    }

    /**
     * Test of toFile method, of class SparseMatrix.
     */
    @Test
    public void testToFile() throws Exception {
        System.out.println("toFile");
    }
}
