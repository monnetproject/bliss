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
public class AddAlphaSmoothingTest {

    public AddAlphaSmoothingTest() {
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
     * Test of smooth method, of class AddAlphaSmoothing.
     */
    @Test
    public void testSmooth() {
        System.out.println("smooth");
        int[] v = { 4,4 };
        double[] C = { 12,11 };
        AddAlphaSmoothing instance = new AddAlphaSmoothing(v, C);
        assertArrayEquals(new double[] { 0.25, 0.21127 }, instance.smooth(3, 1), 0.01);
        assertArrayEquals(new double[] { 0.0950 }, instance.smooth(3, 2), 0.01);
        assertArrayEquals(new double[] { 0.0810 }, instance.smooth(2, 2), 0.01);
    }

}