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
        System.out.println(instance.smooth(3, 1)[0] +" "+ instance.smooth(3, 1)[1]);
        System.out.println(instance.smooth(4, 1)[0] +" "+ instance.smooth(4, 1)[1]);
        System.out.println(instance.smooth(2, 1)[0] +" "+ instance.smooth(2, 1)[1]);
        System.out.println(instance.smooth(2, 2)[0]);
        System.out.println(instance.smooth(1, 2)[0]);
        assertArrayEquals(new double[] { -0.6913213504586012, -0.810024305687282 }, instance.smooth(3, 1), 0.01);
        assertArrayEquals(new double[] { -0.7602646410189646, -0.7410810151269186 }, instance.smooth(4, 1),0.01);
        assertArrayEquals(new double[] { -0.7560729519019886, -0.7452727042438946 }, instance.smooth(2, 1),0.01);
        assertArrayEquals(new double[] { -0.9680642204187359 }, instance.smooth(2, 2),0.01);
        assertArrayEquals(new double[] { -1.0517996465995412 }, instance.smooth(1, 2),0.01);
    }

}