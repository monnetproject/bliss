/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics.lda;

import java.io.FileNotFoundException;
import java.io.InputStream;
import eu.monnetproject.lang.Language;
import java.io.FileOutputStream;
import java.util.HashMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static eu.monnetproject.translation.topics.lda.LDATest.*;

/**
 *
 * @author jmccrae
 */
public class GibbsInferenceTest {
    
    public GibbsInferenceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Simple separable test
     */
    @Test
    public void testSimple() throws FileNotFoundException {
        final int K = 2;
        final int D = 4;
        final int W = 5;
        final int[] N = new int[] { 5, 5, 5, 5 };
        final int[][] x = new int[][] {
            new int[] { 0,2,1,0,1 },
            new int[] { 1,2,0,0,1 },
            new int[] { 3,3,4,4,3 },
            new int[] { 3,4,3,4,3 }
        };
        final GibbsInference gibbsInference = new GibbsInference(K, D, W, N, x);
        gibbsInference.iterator(100);
        final double[][] phi = gibbsInference.phiPolyLingual()[0];
        final double[][] theta = gibbsInference.theta();
        System.err.println("Phi:");
        for(int w = 0; w < W; w++) {
            for(int k = 0; k < K; k++) {
                System.err.print(phi[w][k] + " ");
                
            }
            System.err.println();
        }
        System.err.println("Theta:");
        for(int k = 0; k < K; k++) {
            for(int j = 0; j < D; j++) {
                System.err.print(theta[k][j] + " ");
            }
            System.err.println();
        }
      //  gibbsInference.getData().write(new FileOutputStream("src/test/resources/simple.gibbs"));
        
        assertTrue((phi[0][0] - phi[0][1]) * (phi[3][0]-phi[3][1]) < 0);
        assertTrue((theta[0][0] - theta[1][0]) * (theta[0][2] - theta[1][2]) < 0);
    }
    
    @Test
    public void testPolylingual() throws Exception {
        final int K = 2;
        final int D = 4;
        final int W = 10;
        final int L = 2;
        final int[] N = { 5, 5, 5, 5 };
        final int[][] x = {
            { 0,2,1,0,1 },
            { 6,7,7,5,6 },
            { 3,3,4,4,3 },
            { 8,9,9,9,8 }
        };
        final int[] m = { 0,1,0,1 };
        final int[][] mu = {
            { 0, 1 },
            { 0, 1 },
            { 2, 3 },
            { 2, 3 }
        };
        final GibbsInference gibbsInference = new GibbsInference(K, D, W, L, N, x, m, mu);
        gibbsInference.iterator(100);
        final PolylingualGibbsData polylingualData = gibbsInference.getPolylingualData(new Language[] { Language.QUECHUA, Language.AYMARA }, new HashMap<String, Integer>());
        polylingualData.write(System.out);
    //    polylingualData.write(new FileOutputStream("src/test/resources/polylingual.gibbs"));
    }
    
    @Test
    public void testFastPow() {
        System.err.println("fastpow");
        final double res1 = GibbsInference.fastpow(2,2);
        assertEquals(4.0,res1,0.01);
        final double res2 = GibbsInference.fastpow(2,0.5);
        assertEquals(1.41,res2,0.01);
        final double res3 = GibbsInference.fastpow(3.6,7.2);
        assertEquals(10124,res3,100);
    }
}
