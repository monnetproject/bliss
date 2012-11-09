package eu.monnetproject.math.sparse;

import eu.monnetproject.math.sparse.Vectors.Factory;
import java.io.File;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author john
 */
public class TridiagonalMatrixTest {

    public TridiagonalMatrixTest() {
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

    /**
     * Test of value method, of class TridiagonalMatrix.
     */
    @Test
    public void testValue() {
        System.out.println("value");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        Double expResult = 4.0;
        Double result = instance.value(0, 1);
        assertEquals(expResult, result);
    }

    /**
     * Test of doubleValue method, of class TridiagonalMatrix.
     */
    @Test
    public void testDoubleValue() {
        System.out.println("doubleValue");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        double expResult = 2.0;
        double result = instance.doubleValue(1, 1);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of intValue method, of class TridiagonalMatrix.
     */
    @Test
    public void testIntValue() {
        System.out.println("intValue");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        int expResult = 5;
        int result = instance.intValue(1, 2);
        assertEquals(expResult, result);
    }

    /**
     * Test of set method, of class TridiagonalMatrix.
     */
    @Test
    public void testSet_3args_1() {
        System.out.println("set");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        instance.set(2, 2, 5);
    }

    /**
     * Test of set method, of class TridiagonalMatrix.
     */
    @Test
    public void testSet_3args_2() {
        System.out.println("set");
        int i = 0;
        int j = 0;
        double v = 0.0;
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        instance.set(i, j, v);
    }

    /**
     * Test of set method, of class TridiagonalMatrix.
     */
    @Test
    public void testSet_3args_3() {
        System.out.println("set");
        int i = 0;
        int j = 0;
        Double v = 5.0;
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        instance.set(i, j, v);
    }

    /**
     * Test of add method, of class TridiagonalMatrix.
     */
    @Test
    public void testAdd_3args_1() {
        System.out.println("add");
        int i = 0;
        int j = 0;
        int v = 0;
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        instance.add(i, j, v);
    }

    /**
     * Test of add method, of class TridiagonalMatrix.
     */
    @Test
    public void testAdd_3args_2() {
        System.out.println("add");
        int i = 0;
        int j = 0;
        double v = 0.0;
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        instance.add(i, j, v);
    }

    /**
     * Test of add method, of class TridiagonalMatrix.
     */
    @Test
    public void testAdd_3args_3() {
        System.out.println("add");
        int i = 0;
        int j = 0;
        Double v = 5.0;
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        instance.add(i, j, v);
    }

    /**
     * Test of mult method, of class TridiagonalMatrix.
     */
    @Test
    public void testMult_Vector() {
        System.out.println("mult");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        Vector expResult = new RealVector(new double[]{5, 11, 8});
        Vector result = instance.mult(new RealVector(new double[]{1, 1, 1}));
        assertEquals(expResult, result);
    }

    /**
     * Test of mult method, of class TridiagonalMatrix.
     */
    @Test
    public void testMult_Vector_VectorsFactory() {
        System.out.println("mult");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        Vector expResult = new RealVector(new double[]{5, 11, 8});
        Vector result = instance.mult(new RealVector(new double[]{1, 1, 1}), Vectors.AS_REALS);
        assertEquals(expResult, result);
    }

    /**
     * Test of isSymmetric method, of class TridiagonalMatrix.
     */
    @Test
    public void testIsSymmetric() {
        System.out.println("isSymmetric");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        boolean expResult = true;
        boolean result = instance.isSymmetric();
        assertEquals(expResult, result);
    }

    /**
     * Test of transpose method, of class TridiagonalMatrix.
     */
    @Test
    public void testTranspose() {
        System.out.println("transpose");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        Matrix expResult = instance;
        Matrix result = instance.transpose();
        assertEquals(expResult, result);
    }

    /**
     * Test of row method, of class TridiagonalMatrix.
     */
    @Test
    public void testRow() {
        System.out.println("row");
        int i = 0;
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        Vector expResult = new RealVector(new double[]{1, 4, 0});
        Vector result = instance.row(i);
        assertEquals(expResult, result);
    }

    /**
     * Test of rows method, of class TridiagonalMatrix.
     */
    @Test
    public void testRows() {
        System.out.println("rows");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        int expResult = 3;
        int result = instance.rows();
        assertEquals(expResult, result);
    }

    /**
     * Test of cols method, of class TridiagonalMatrix.
     */
    @Test
    public void testCols() {
        System.out.println("cols");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        int expResult = 3;
        int result = instance.cols();
        assertEquals(expResult, result);
    }

    /**
     * Test of add method, of class TridiagonalMatrix.
     */
    @Test
    public void testAdd_Matrix() {
        System.out.println("add");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        Matrix instance2 = new DoubleArrayMatrix(new double[][]{{1, 4, 0}, {4, 2, 5}, {0, 5, 3}});
        instance.add(instance2);
    }

    /**
     * Test of sub method, of class TridiagonalMatrix.
     */
    @Test
    public void testSub() {
        System.out.println("sub");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        Matrix instance2 = new DoubleArrayMatrix(new double[][]{{1, 4, 0}, {4, 2, 5}, {0, 5, 3}});
        instance.sub(instance2);
    }

    /**
     * Test of asVectorFunction method, of class TridiagonalMatrix.
     */
    @Test
    public void testAsVectorFunction() {
        System.out.println("asVectorFunction");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        VectorFunction result = instance.asVectorFunction();
        assertEquals(new RealVector(new double[]{5, 11, 8}), result.apply(new RealVector(new double[]{1, 1, 1})));
    }

    /**
     * Test of factory method, of class TridiagonalMatrix.
     */
    @Test
    public void testFactory() {
        System.out.println("factory");
    }

    /**
     * Test of alpha method, of class TridiagonalMatrix.
     */
    @Test
    public void testAlpha() {
        System.out.println("alpha");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        double[] expResult = new double[]{1, 2, 3};
        double[] result = instance.alpha();
        assertArrayEquals(expResult, result, 0.01);
    }

    /**
     * Test of beta method, of class TridiagonalMatrix.
     */
    @Test
    public void testBeta() {
        System.out.println("beta");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        double[] expResult = new double[]{4, 5};
        double[] result = instance.beta();
        assertArrayEquals(expResult, result, 0.01);
    }

    /**
     * Test of fromFile method, of class TridiagonalMatrix.
     */
    @Test
    public void testFromFile() throws Exception {
        System.out.println("fromFile");
    }

    /**
     * Test of toFile method, of class TridiagonalMatrix.
     */
    @Test
    public void testToFile() throws Exception {
        System.out.println("toFile");
        File file = File.createTempFile("tridiag", "matrix");
        file.deleteOnExit();
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});

        instance.toFile(file);
        final TridiagonalMatrix instance2 = TridiagonalMatrix.fromFile(file);
        assertEquals(instance, instance2);
    }

    /**
     * Test of toDoubleArray method, of class TridiagonalMatrix.
     */
    @Test
    public void testToDoubleArray() {
        System.out.println("toDoubleArray");
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 2, 3}, new double[]{4, 5});
        double[][] expResult = new double[][]{{1, 4, 0}, {4, 2, 5}, {0, 5, 3}};
        double[][] result = instance.toDoubleArray();
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(expResult[i], result[i], 0.0);
        }
    }

    /**
     * Test of invMult method, of class TridiagonalMatrix.
     */
    @Test
    public void testInvMult() {
        System.out.println("invMult");
        Vector<Double> v = new RealVector(new double[]{3, 6});
        TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{1, 4}, new double[]{2});
        Vector expResult = new RealVector(new double[]{1, 1});
        Vector result = instance.invMult(v);
        assertArrayEquals(expResult.toDoubleArray(), result.toDoubleArray(), 0.01);

        instance = new TridiagonalMatrix(new double[]{0.5364256610676157, 0.47451949329180676, 0.803523486714734, 0.6781317227091326},
                new double[]{0.9982442548236763, 0.989776581063424, 0.23022611921077385});
        expResult = new RealVector(new double[]{0.8882429630977728, 1.2062852191267643, 0.7680282859511248, 0.14301181645471608});
        v = new RealVector(new double[]{1.680644, 2.219266, 1.844007, 0.273801});
        assertArrayEquals(expResult.toDoubleArray(), instance.invMult(v).toDoubleArray(), 0.01);

    }

    @Test
    public void testInvMultRandom() {
        System.out.println("invMultRandom");
        final Random r = new java.util.Random();
        for (int iter = 0; iter < 100; iter++) {
            Vector<Double> expResult = new RealVector(new double[]{r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()/*,r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()*/});
            TridiagonalMatrix instance = new TridiagonalMatrix(new double[]{r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()/*,r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()*/}, new double[]{r.nextDouble(), r.nextDouble(), r.nextDouble()/*,r.nextDouble(),r.nextDouble(),r.nextDouble(),r.nextDouble()*/});
            Vector v = instance.mult(expResult);
            Vector result = instance.invMult(v);
            System.err.println(instance);
            System.err.println(v);
            System.err.println(expResult);
            assertArrayEquals(expResult.toDoubleArray(), result.toDoubleArray(), 0.01);
        }
    }
}