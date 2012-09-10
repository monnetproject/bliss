/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics.lda;

import eu.monnetproject.translation.topics.SparseArray;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static eu.monnetproject.translation.topics.lda.LDATest.*;

/**
 *
 * @author jmccrae
 */
public class GibbsDataTest {

    public GibbsDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of write method, of class GibbsData.
     */
    @Test
    public void testReadWrite() throws Exception {
        System.out.println("write");
        int K = 2, W = 3, D = 3;
        double alpha = 1.0, beta = 0.01;
        int[] N_k = new int[]{1, 2};
        SparseArray[] N_kw = new SparseArray[]{
            new SparseArray(9,0, 1, 2, 2, 3, 5, 5, 6, 6),
            new SparseArray(9,0, 3, 2, 4, 1, 7, 7, 8, 8)
        };
        double[][] phi = new double[][]{
            new double[]{0.1, 0.2},
            new double[]{0.3, 0.4},
            new double[]{0.5, 0.6}
        };
        double[][] theta = new double[][]{
            new double[]{0.1, 0.2, 0.3},
            new double[]{0.4, 0.5, 0.6}
        };
        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        GibbsData instance = new GibbsData(N_k, N_kw, K, W, D, alpha, beta, phi, theta);
        instance.write(pos);
        GibbsData serializedInstance = GibbsData.read(pis);
        assertEquals(instance, serializedInstance);
    }
}
