/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics.sim;

import eu.monnetproject.translation.topics.SparseArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author jmccrae
 */
public class LinearSurjectionTest {

    public LinearSurjectionTest() {
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
     * Test of simVecSource method, of class LinearSurjection.
     */
    @Test
    public void testSimVecSource() {
        System.out.println("simVecSource");
        final SparseArray[][] data = {
            {
                SparseArray.fromArray(new int[]{3, 3, 0, 0}),
                SparseArray.fromArray(new int[]{3, 3, 0, 0})
            },
            {
                SparseArray.fromArray(new int[]{0, 4, 2, 0}),
                SparseArray.fromArray(new int[]{4, 0, 0, 2})
            },
            {
                SparseArray.fromArray(new int[]{2, 2, 0, 1}),
                SparseArray.fromArray(new int[]{2, 2, 1, 0})
            },
            {
                SparseArray.fromArray(new int[]{0, 1, 0, 0}),
                SparseArray.fromArray(new int[]{1, 0, 0, 0})
            }
        };
        LinearSurjection linSurj = new LinearSurjection(data, 4);
        final double[] result = linSurj.simVecSource(SparseArray.fromArray(new int[]{5, 1, 0, 0}));
        final double[] expected = {1, 5, 0, 0};
        assertArrayEquals(expected, result, 0.1);
    }

}
