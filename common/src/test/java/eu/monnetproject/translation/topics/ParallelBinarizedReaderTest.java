package eu.monnetproject.translation.topics;

import eu.monnetproject.math.sparse.SparseIntArray;
import java.io.ByteArrayInputStream;
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
public class ParallelBinarizedReaderTest {

    public ParallelBinarizedReaderTest() {
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
     * Test of nextPair method, of class ParallelBinarizedReader.
     */
    @Test
    public void testNextPair() throws Exception {
        System.out.println("nextPair");
        final byte[] buf = new byte[]{
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 3,
            0, 0, 0, 4,
            0, 0, 0, 0,
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 5,
            0, 0, 0, 4,
            0, 0, 0, 0,
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 5,
            0, 0, 0, 4,
            0, 0, 0, 0,
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 3,
            0, 0, 0, 4,
            0, 0, 0, 0
        };
        ParallelBinarizedReader instance = new ParallelBinarizedReader(new ByteArrayInputStream(buf));
        int[][] expResult = new int[][]{
            {1, 2, 3, 4}, {1, 2, 5, 4}
        };
        int[][] result = instance.nextPair();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of nextFreqPair method, of class ParallelBinarizedReader.
     */
    @Test
    public void testNextFreqPair() throws Exception {
        System.out.println("nextFreqPair");
        int W = 6;
        final byte[] buf = new byte[]{
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 3,
            0, 0, 0, 4,
            0, 0, 0, 0,
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 5,
            0, 0, 0, 4,
            0, 0, 0, 0,
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 5,
            0, 0, 0, 4,
            0, 0, 0, 0,
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 3,
            0, 0, 0, 4,
            0, 0, 0, 0
        };
        ParallelBinarizedReader instance = new ParallelBinarizedReader(new ByteArrayInputStream(buf));
        SparseIntArray[] expResult = new SparseIntArray[]{
            SparseIntArray.histogram(new int[]{1, 2, 3, 4}, W),
            SparseIntArray.histogram(new int[]{1, 2, 5, 4}, W),};
        SparseIntArray[] result = instance.nextFreqPair(W);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of readAll method, of class ParallelBinarizedReader.
     */
    @Test
    public void testReadAll() throws Exception {
        System.out.println("readAll");
        int W = 6;
        final byte[] buf = new byte[]{
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 3,
            0, 0, 0, 4,
            0, 0, 0, 0,
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 5,
            0, 0, 0, 4,
            0, 0, 0, 0,
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 5,
            0, 0, 0, 4,
            0, 0, 0, 0,
            0, 0, 0, 1,
            0, 0, 0, 2,
            0, 0, 0, 3,
            0, 0, 0, 4,
            0, 0, 0, 0
        };
        ParallelBinarizedReader instance = new ParallelBinarizedReader(new ByteArrayInputStream(buf));
        SparseIntArray[][] expResult = new SparseIntArray[][]{
            {
                SparseIntArray.histogram(new int[]{1, 2, 3, 4}, W),
                SparseIntArray.histogram(new int[]{1, 2, 5, 4}, W)
            },
            {
                SparseIntArray.histogram(new int[]{1, 2, 5, 4}, W),
                SparseIntArray.histogram(new int[]{1, 2, 3, 4}, W)
            }
        };
        SparseIntArray[][] result = instance.readAll(W);
        assertArrayEquals(expResult, result);
    }
}