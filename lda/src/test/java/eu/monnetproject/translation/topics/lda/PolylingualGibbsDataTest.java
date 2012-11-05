/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics.lda;

import eu.monnetproject.lang.Language;
import eu.monnetproject.math.sparse.SparseIntArray;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static eu.monnetproject.translation.topics.lda.LDATest.*;

/**
 *
 * @author jmccrae
 */
public class PolylingualGibbsDataTest {

    public PolylingualGibbsDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of write method, of class PolylingualGibbsData.
     */
    @Test
    public void testReadWrite() throws Exception {
        System.out.println("write");
        int K = 2, W = 3, D = 3;
        double alpha = 1.0, beta = 0.01;
        int[][] N_lk = new int[][]{
            {1,2},
            {3,4}
        };
        SparseIntArray[][] N_lkw = new SparseIntArray[][]{
            {
                new SparseIntArray(8,0,1,1,2,2),
                new SparseIntArray(8,0,2,2,3,3)
            },
            {
                new SparseIntArray(8,0,4,4,5,5),
                new SparseIntArray(8,0,6,6,7,7)
            }
        };
        double[][][] phi = new double[][][]{
            {
                new double[]{0.1, 0.2},
                new double[]{0.3, 0.4},
                new double[]{0.5, 0.6}
            }, {
                new double[]{0.1, 0.2},
                new double[]{0.3, 0.4},
                new double[]{0.5, 0.6}
            }
        };
        double[][] theta = new double[][]{
            new double[]{0.1, 0.2, 0.3},
            new double[]{0.4, 0.5, 0.6}
        };
        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        final HashMap<String,Integer> words = new HashMap<String, Integer>();
        words.put("a", 0);
        words.put("b", 1);
        words.put("c", 2);
        PolylingualGibbsData instance = new PolylingualGibbsData(N_lk, N_lkw, K, W, D,alpha, beta, phi, theta,new Language[] { Language.PANJABI, Language.FAROESE }, words);

        instance.write(pos);
        pos.close();
        PolylingualGibbsData serializedInstance = PolylingualGibbsData.read(pis);

        assertEquals(instance, serializedInstance);
    }
}
