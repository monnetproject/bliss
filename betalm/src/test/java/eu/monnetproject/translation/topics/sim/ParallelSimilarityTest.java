/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics.sim;

import eu.monnetproject.translation.topics.SparseArray;
import static eu.monnetproject.translation.topics.SparseArray.*;
import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jmccrae
 */
public class ParallelSimilarityTest {

    public ParallelSimilarityTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSimVec() {
        System.err.println("simVec");
        final SparseArray[][] x = new SparseArray[][]{
            new SparseArray[]{
                fromArray(new int[]{0, 1, 2, 0, 0, 0}),
                fromArray(new int[]{0, 0, 0, 0, 1, 2})
            },
            new SparseArray[]{
                fromArray(new int[]{2, 0, 1, 0, 0, 0}),
                fromArray(new int[]{0, 0, 0, 2, 0, 1})
            },
            new SparseArray[]{
                fromArray(new int[]{0, 2, 1, 0, 0, 0}),
                fromArray(new int[]{0, 0, 0, 0, 2, 1})
            }
        };
        final BetaLM instance = new BetaLM(x,6, new String[] { "a", "b", "c", "x", "y", "z" });
        final double[] vector = instance.simVec(Arrays.asList("a","a","a","b"));
        assertEquals(0,vector[0],0.0);
        assertTrue(vector[3] > vector[4]);
        
        
        assertTrue(true);
    }
}
