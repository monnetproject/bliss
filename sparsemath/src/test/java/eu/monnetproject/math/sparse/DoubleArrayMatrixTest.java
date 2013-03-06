package eu.monnetproject.math.sparse;

import eu.monnetproject.math.sparse.Vectors.Factory;
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
public class DoubleArrayMatrixTest {

    public DoubleArrayMatrixTest() {
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
     * Test of mult method, of class DoubleArrayMatrix.
     */
    @Test
    public void testMult_Vector() {
        System.out.println("mult");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(4, 1,2,3,4,5,6,7,8,9,10,11,12);
        Vector expResult = RealVector.make(30,70,110);
        Vector result = instance.mult(RealVector.make(1,2,3,4));
        assertEquals(expResult, result);
    }

    /**
     * Test of mult method, of class DoubleArrayMatrix.
     */
    @Test
    public void testMult_Vector_VectorsFactory() {
        System.out.println("mult");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(4, 1,2,3,4,5,6,7,8,9,10,11,12);;
        Vector expResult = RealVector.make(30,70,110);;
        Vector result = instance.mult(RealVector.make(1,2,3,4),Vectors.AS_REALS);
        assertEquals(expResult, result);
    }

    /**
     * Test of multTransposed method, of class DoubleArrayMatrix.
     */
    @Test
    public void testMultTransposed() {
        System.out.println("multTransposed");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(4, 1,2,3,4,5,6,7,8,9,10,11,12);
        Vector expResult = RealVector.make(38,44,50,56);
        Vector result = instance.multTransposed(RealVector.make(1,2,3));
        assertEquals(expResult, result);
    }

    /**
     * Test of isSymmetric method, of class DoubleArrayMatrix.
     */
    @Test
    public void testIsSymmetric() {
        System.out.println("isSymmetric");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,
                                                               2,4,5,
                                                               3,5,6);
        boolean expResult = true;
        boolean result = instance.isSymmetric();
        assertEquals(expResult, result);
    }

    /**
     * Test of transpose method, of class DoubleArrayMatrix.
     */
    @Test
    public void testTranspose() {
        System.out.println("transpose");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        Matrix expResult = DoubleArrayMatrix.make(3, 1,4,7,2,5,8,3,6,9);
        Matrix result = instance.transpose();
        assertEquals(expResult, result);
    }

    /**
     * Test of row method, of class DoubleArrayMatrix.
     */
    @Test
    public void testRow() {
        System.out.println("row");
        int i = 0;
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        Vector expResult = RealVector.make(1,2,3);
        Vector result = instance.row(i);
        assertEquals(expResult, result);
    }

    /**
     * Test of data method, of class DoubleArrayMatrix.
     */
    @Test
    public void testData() {
        System.out.println("data");
    }

    /**
     * Test of value method, of class DoubleArrayMatrix.
     */
    @Test
    public void testValue() {
        System.out.println("value");
        int i = 0;
        int j = 0;
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);;
        Double expResult = 1.0;
        Double result = instance.value(i, j);
        assertEquals(expResult, result);
    }

    /**
     * Test of doubleValue method, of class DoubleArrayMatrix.
     */
    @Test
    public void testDoubleValue() {
        System.out.println("doubleValue");
        int i = 0;
        int j = 0;
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        double expResult = 1.0;
        double result = instance.doubleValue(i, j);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of intValue method, of class DoubleArrayMatrix.
     */
    @Test
    public void testIntValue() {
        System.out.println("intValue");
        int i = 0;
        int j = 0;
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);;
        int expResult = 1;
        int result = instance.intValue(i, j);
        assertEquals(expResult, result);
    }

    /**
     * Test of set method, of class DoubleArrayMatrix.
     */
    @Test
    public void testSet_3args_1() {
        System.out.println("set");
        int i = 0;
        int j = 0;
        int v = 0;
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        instance.set(i, j, v);
    }

    /**
     * Test of set method, of class DoubleArrayMatrix.
     */
    @Test
    public void testSet_3args_2() {
        System.out.println("set");
        int i = 0;
        int j = 0;
        double v = 0.0;
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);;
        instance.set(i, j, v);
    }

    /**
     * Test of set method, of class DoubleArrayMatrix.
     */
    @Test
    public void testSet_3args_3() {
        System.out.println("set");
        int i = 0;
        int j = 0;
        Double v = 1.0;
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);;
        instance.set(i, j, v);
    }

    /**
     * Test of add method, of class DoubleArrayMatrix.
     */
    @Test
    public void testAdd_3args_1() {
        System.out.println("add");
        int i = 0;
        int j = 0;
        int v = 1;
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        instance.add(i, j, v);
        assertEquals(instance.intValue(i, j),2);
    }

    /**
     * Test of add method, of class DoubleArrayMatrix.
     */
    @Test
    public void testAdd_3args_2() {
        System.out.println("add");
        int i = 0;
        int j = 0;
        double v = 1.0;
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        instance.add(i, j, v);
        assertEquals(instance.intValue(i, j),2.0,0.0);
    }

    /**
     * Test of add method, of class DoubleArrayMatrix.
     */
    @Test
    public void testAdd_3args_3() {
        System.out.println("add");
        int i = 0;
        int j = 0;
        Double v = 1.0;
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        instance.add(i, j, v);
    }

    /**
     * Test of add method, of class DoubleArrayMatrix.
     */
    @Test
    public void testAdd_Matrix() {
        System.out.println("add");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);;;
        instance.add(DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9));
        assertEquals(4,instance.intValue(0, 1));
    }

    /**
     * Test of sub method, of class DoubleArrayMatrix.
     */
    @Test
    public void testSub() {
        System.out.println("sub");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        instance.sub(DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9));
        assertEquals(0, instance.intValue(2, 2));
    }

    /**
     * Test of rows method, of class DoubleArrayMatrix.
     */
    @Test
    public void testRows() {
        System.out.println("rows");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        int expResult = 3;
        int result = instance.rows();
        assertEquals(expResult, result);
    }

    /**
     * Test of cols method, of class DoubleArrayMatrix.
     */
    @Test
    public void testCols() {
        System.out.println("cols");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        int expResult = 3;
        int result = instance.cols();
        assertEquals(expResult, result);
    }

    /**
     * Test of asVectorFunction method, of class DoubleArrayMatrix.
     */
    @Test
    public void testAsVectorFunction() {
        System.out.println("asVectorFunction");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        VectorFunction result = instance.asVectorFunction();
        result.apply(RealVector.make(1,2,3));
    }

    /**
     * Test of factory method, of class DoubleArrayMatrix.
     */
    @Test
    public void testFactory() {
        System.out.println("factory");
    }

    /**
     * Test of product method, of class DoubleArrayMatrix.
     */
    @Test
    public void testProduct() {
        System.out.println("product");
        DoubleArrayMatrix instance = DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9);
        Matrix expResult = DoubleArrayMatrix.make(3, 30,36,42,66,81,96,102,126,150);
        Matrix result = instance.product(DoubleArrayMatrix.make(3, 1,2,3,4,5,6,7,8,9));
        assertEquals(expResult, result);
    }

    /**
     * Test of toDoubleArray method, of class DoubleArrayMatrix.
     */
    @Test
    public void testToDoubleArray() {
        System.out.println("toDoubleArray");
    }

}