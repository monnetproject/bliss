/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics.lda;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static eu.monnetproject.translation.topics.lda.LDATest.*;

/**
 *
 * @author jmccrae
 */
public class InitialHeuristicTest {
    
    public InitialHeuristicTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of heuristicInit method, of class InitialHeuristic.
     */
    @Test
    public void testHeuristicInit() {
        System.out.println("heuristicInit");
        final int K = 2;
        final int D = 4;
        final int W = 5;
        final int[] N = new int[] { 5, 5, 5, 5 };
        final int[][] x = new int[][] {
            new int[] { 0,2,1,0,1 },
            new int[] { 3,3,4,4,3 },
            new int[] { 1,2,0,0,1 },
            new int[] { 3,4,3,4,3 }
        };
        InitialHeuristic instance = new InitialHeuristic(K, D, W, N, x, 0.8);
        int[][] z = instance.getHeuristicZ();
        for(int j = 0; j < D; j++) {
            for(int i = 0; i < x[j].length; i++) {
                System.out.print(z[j][i]+" ");
            }
            System.out.println();
        }
        for(int i = 0; i < x[2].length; i++) {
            assertEquals(0, z[2][i]);
        }
    }
}
