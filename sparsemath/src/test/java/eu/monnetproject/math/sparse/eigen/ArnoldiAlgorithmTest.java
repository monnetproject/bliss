package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.DoubleArrayMatrix;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.eigen.ArnoldiAlgorithm.Solution;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static java.lang.Math.*;

/**
 *
 * @author john
 */
public class ArnoldiAlgorithmTest {

    public ArnoldiAlgorithmTest() {
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
     * Test of solve method, of class ArnoldiAlgorithm.
     */
    @Test
    public void testSolve() {
        System.out.println("solve");
        final DoubleArrayMatrix A = new DoubleArrayMatrix(new double[][]{
                    {1, 2, 3},
                    {3, 2, 1},
                    {2, 3, 1}
                });
        RealVector r0 = new RealVector(new double[]{1.0 / sqrt(3), 1.0 / sqrt(3), 1.0 / sqrt(3)});
        int K = 1;
        Solution result = ArnoldiAlgorithm.solve(A.asVectorFunction(), r0, K);
        final Solution expResult = new ArnoldiAlgorithm.Solution(
                new RealVector[]{
                    r0
                },
                new double[][]{{6}}, 1);
        for (int i = 0; i < result.h.length; i++) {
            assertArrayEquals(expResult.h[i], result.h[i], 0.001);
        }
    }

    @Test
    public void testSolve2() {
        System.out.println("solve2");
        final DoubleArrayMatrix A = new DoubleArrayMatrix(new double[][]{
                    {1, 2, 1},
                    {6, -1, 0},
                    {-1, -2, -1}
                });
        RealVector r0 = new RealVector(new double[]{1.0 / sqrt(3), 1.0 / sqrt(3), 1.0 / sqrt(3)});
        int K = 1;
        Solution result = ArnoldiAlgorithm.solve(A.asVectorFunction(), r0, 3);
        final Solution expResult = new ArnoldiAlgorithm.Solution(
                new RealVector[0],
                new double[][]{
                    {1.66666, 0.88278, -2.9624},
                    {4.0277, 1.27853, -1.69251},
                    {0.0, 0.61689, -3.9452}
                }, 3);
        for (int i = 0; i < result.h.length; i++) {
            assertArrayEquals(expResult.h[i], result.h[i], 0.001);
        }
    }
}