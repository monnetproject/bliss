package eu.monnetproject.translation.topics.clesa;

import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.translation.topics.sim.ParallelReader;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author jmccrae
 */
public class CLESATest {

    public CLESATest() {
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
     * Test of simVecSource method, of class CLESA.
     */
    @Test
    public void testSimVecSource() {
        System.out.println("simVecSource");
        // Comparable corpus
        final SparseIntArray[][] x = new SparseIntArray[][]{
            {
                // Source document 1
                ParallelReader.histogram(new int[]{0, 1, 2}, 9),
                // Target document 1
                ParallelReader.histogram(new int[]{4, 5, 6}, 9)
            },
            {
                // Source document 2
                ParallelReader.histogram(new int[]{2, 1, 3}, 9),
                // Target document 2
                ParallelReader.histogram(new int[]{6, 5, 5}, 9)
            },
            {
                // Source document 3
                ParallelReader.histogram(new int[]{3, 3, 4}, 9),
                // Target document 3
                ParallelReader.histogram(new int[]{7, 6, 8}, 9)
            }
        };
        // Query document
        SparseIntArray termVec = ParallelReader.histogram(new int[]{1, 3, 4}, 9);
        CLESA instance = new CLESA(x, 9, new String[]{});
        double[] expResult = new double[]{0.135155, 0.2703101, 0.6365142};
        // Map using source language part of comparable corpus
        double[] result = instance.simVecSource(termVec).toDoubleArray();
        assertArrayEquals(expResult, result, 0.0001);
    }

    /**
     * Test of simVecTarget method, of class CLESA.
     */
    @Test
    public void testSimVecTarget() {
        System.out.println("simVecTarget");
        // Comparable corpus
        final SparseIntArray[][] x = new SparseIntArray[][]{
            {
                // Source document 1
                ParallelReader.histogram(new int[]{0, 1, 2}, 9),
                // Target document 1
                ParallelReader.histogram(new int[]{4, 5, 6}, 9)
            },
            {
                // Source document 2
                ParallelReader.histogram(new int[]{2, 1, 3}, 9),
                // Target document 2
                ParallelReader.histogram(new int[]{6, 5, 5}, 9)
            },
            {
                // Source document 3
                ParallelReader.histogram(new int[]{3, 3, 4}, 9),
                // Target document 3
                ParallelReader.histogram(new int[]{7, 6, 8}, 9)
            }
        };
        // Query document
        SparseIntArray termVec = ParallelReader.histogram(new int[]{4, 4, 6}, 9);
        CLESA instance = new CLESA(x, 9, new String[]{});
        double[] expResult = new double[]{0.7324082, 0, 0};
        // Map using target language part of comparable corpus
        double[] result = instance.simVecTarget(termVec).toDoubleArray();
        assertArrayEquals(expResult, result, 0.0001);
    }
}
