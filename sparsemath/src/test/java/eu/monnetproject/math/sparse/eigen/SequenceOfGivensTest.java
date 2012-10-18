package eu.monnetproject.math.sparse.eigen;

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
public class SequenceOfGivensTest {

    public SequenceOfGivensTest() {
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
     * Test of applyTo method, of class SequenceOfGivens.
     */
    @Test
    public void testApplyTo() {
        System.out.println("applyTo");
        double[][] matrix = new double[][] { { 6,5,0 }, {5,1,4}, {0,4,3} };
        SequenceOfGivens instance = new SequenceOfGivens();
        instance.add(0, 1, 0.7682, -0.6402);
        instance.applyTo(matrix);
        double[][] expResult = new double[][] { 
            {7.8102,-0.0002,0},
            {4.4812,-2.4328,4},
            {2.5608,3.0728,3}
        };
        assertArrayEquals(expResult[0], matrix[0], 0.01);
        assertArrayEquals(expResult[1], matrix[1], 0.01);
        assertArrayEquals(expResult[2], matrix[2], 0.01);
    }
    
    /**
     * Test of applyTransposed method, of class SequenceOfGivens.
     */
    @Test
    public void testApplyTransposed() {
        System.out.println("applyTransposed");
        double[][] matrix = new double[][] { { 6,5,0 }, {5,1,4}, {0,4,3} };
        SequenceOfGivens instance = new SequenceOfGivens();
        instance.add(0, 1, 0.7682, -0.6402);
        instance.applyTransposed(matrix);
        double[][] expResult = new double[][] { 
            {1.4082,3.2008,-2.5608},
            {7.6822,3.9692,3.0728},
            {0,4,3}
        };
        assertArrayEquals(expResult[0], matrix[0], 0.01);
        assertArrayEquals(expResult[1], matrix[1], 0.01);
        assertArrayEquals(expResult[2], matrix[2], 0.01);
    }

}