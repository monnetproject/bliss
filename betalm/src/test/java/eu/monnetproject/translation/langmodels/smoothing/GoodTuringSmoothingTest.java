package eu.monnetproject.translation.langmodels.smoothing;

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
public class GoodTuringSmoothingTest {

    public GoodTuringSmoothingTest() {
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
     * Test of smooth method, of class GoodTuringSmoothing.
     */
    @Test
    public void testSmooth() {
        System.out.println("smooth");
        GoodTuringSmoothing instance = new GoodTuringSmoothing(new double[] { 12,11 }, new int[][] {
            {0,1,2,1},
            {6,2}
        }, new int[] { 4, 8 });
        assertArrayEquals(new double[] { 0.44444, 0.1907 }, instance.smooth(3, 1), 0.01);
        assertArrayEquals(new double[] { 0.52083, 0.1627 }, instance.smooth(4, 1),0.01);
        assertArrayEquals(new double[] { 0.374, 0.2260 }, instance.smooth(2, 1),0.01);
        assertArrayEquals(new double[] { 1e-6 }, instance.smooth(2, 2),0.01);
        assertArrayEquals(new double[] { 0.07954 }, instance.smooth(1, 2),0.01);
    }

}