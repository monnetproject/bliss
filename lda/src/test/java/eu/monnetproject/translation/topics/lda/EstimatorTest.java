/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics.lda;

import eu.monnetproject.lang.Language;
import java.util.HashMap;
import java.io.InputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static eu.monnetproject.translation.topics.lda.LDATest.*;

/**
 *
 * @author jmccrae
 */
public class EstimatorTest {

    public EstimatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of getTopics method, of class Estimator.
     */
    @Test
    public void testGetTopics() throws Exception {
        final InputStream resource = this.getClass().getResourceAsStream("/polylingual.gibbs");
        if (resource == null) {
            fail("Could not locate test data");
        }
        PolylingualGibbsData data = PolylingualGibbsData.read(resource);

        int[] d = new int[]{0, 1, 1, 3, 1, 0, 1, 1, 1, 0, 0, 0};
        int iterations = 100;
        Estimator instance = new Estimator();
        double[] result = instance.topics(d, 0, data, iterations);
        for (int i = 0; i < 2; i++) {
            System.err.print(result[i] + " ");
        }
        System.err.println();
        for (int i = 0; i < 2; i++) {
            System.err.print(data.phi(0,0, i) + " ");
        }
        System.err.println();
        assertTrue((result[0] - result[1]) * (data.phi(0,0,0) - data.phi(0,0,1)) >= 0);

        double prob3 = instance.wordProb(3, result, data.monolingual(0));
        double prob2 = instance.wordProb(2, result, data.monolingual(0));
        System.err.println("p(3)=" + prob3);
        System.err.println("p(2)=" + prob2);
        if(prob3 < prob2) {
            System.err.println("Failed monte carlos sim");
        }
        //assertTrue(prob3 < prob2);
    }

    private PolylingualGibbsData getPolyLingualData() throws Exception {
        final int K = 2;
        final int D = 4;
        final int W = 10;
        final int L = 2;
        final int[] N = {5, 5, 5, 5};
        final int[][] x = {
            {0, 2, 1, 0, 1},
            {6, 7, 7, 5, 6},
            {3, 3, 4, 4, 3},
            {8, 9, 9, 9, 8}
        };
        final int[] m = {0, 1, 0, 1};
        final int[][] mu = {
            {0, 1},
            {0, 1},
            {2, 3},
            {2, 3}
        };
        final GibbsInference gibbsInference = new GibbsInference(K, D, W, L, N, x, m, mu);
        gibbsInference.iterator(100);
        return gibbsInference.getPolylingualData(new Language[]{Language.QUECHUA, Language.AYMARA}, new HashMap<String, Integer>());
    }

    /**
     * Test of getTopics method, of class Estimator.
     */
    @Test
    public void testPolylingualTopics() throws Exception {
        final InputStream resource = this.getClass().getResourceAsStream("/polylingual.gibbs");
        if (resource == null) {
            fail("Could not locate test data");
        }
        final PolylingualGibbsData data = PolylingualGibbsData.read(resource);

        int[] d = new int[]{0, 2, 2, 1, 1};
        int iterations = 100;
        Estimator instance = new Estimator();
        double[] result = instance.topics(d, 0, data, iterations);
        System.err.println();
        if(System.getProperty("mc.test") != null) {
            assertTrue((result[0] - result[1]) * (data.phi(0,0,0) - data.phi(0,0,1)) >= 0);
        }
        final GibbsData targetLanguageModel = data.monolingual(1);
        double prob8 = instance.wordProb(8, result, targetLanguageModel);
        double prob7 = instance.wordProb(7, result, targetLanguageModel);
        System.err.println("p(8)=" + prob8);
        System.err.println("p(7)=" + prob7);
        assertTrue(prob8 < prob7);
    }
    
    @Test
    public void testPolylingualTopicsFull() throws Exception {
        final PolylingualGibbsData data = getPolyLingualData();
        int[] d = new int[]{0, 2, 2, 1, 1};
        int iterations = 100;
        Estimator instance = new Estimator();
        double[] result = instance.topics(d, 0, data, iterations);
        System.err.println();
        if(System.getProperty("mc.test") != null) {
            assertTrue((result[0] - result[1]) * (data.phi(0,0,0) - data.phi(0,0,1)) >= 0);
        }
        final GibbsData targetLanguageModel = data.monolingual(1);
        double prob8 = instance.wordProb(8, result, targetLanguageModel);
        double prob7 = instance.wordProb(7, result, targetLanguageModel);
        System.err.println("p(8)=" + prob8);
        System.err.println("p(7)=" + prob7);
        //assertTrue(prob8 < prob7);
    }
}
