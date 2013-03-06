/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.bliss.experiments;

import eu.monnetproject.bliss.experiments.DiskBackedStream;
import eu.monnetproject.bliss.experiments.DiskBackedStream.Builder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jmccrae
 */
public class DiskBackedStreamTest {
    
    public DiskBackedStreamTest() {
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
     * Test of iterator method, of class DiskBackedStream.
     */
    @Test
    public void testIterator() {
        System.out.println("iterator");
        DiskBackedStream instance = new DiskBackedStream(5, 3);
        final ArrayList<double[]> ref = new ArrayList<double[]>();
        
        Builder result = instance.builder();
        for(int i = 0; i < 15; i++) {
            final double[] v = randomVec(3);
            result.add(v);
            ref.add(v);
        }
        result.finish();
        int i = 0;
        final Iterator<double[]> iter = instance.iterator();
        final Iterator<double[]> refIterator = ref.iterator();
        while(refIterator.hasNext()) {
            System.err.println(i++);
            assert(iter.hasNext());
            Assert.assertArrayEquals(iter.next(),refIterator.next(),0.0);
        }
        assert(!iter.hasNext());
    }

    private static final Random random = new Random();
    
    private static double[] randomVec(int N) {
        final double[] v = new double[N];
        for(int i = 0; i < N; i++) {
            v[i] = random.nextDouble();
        }
        return v;
    }
    
    /**
     * Test of builder method, of class DiskBackedStream.
     */
    @Test
    public void testBuilder() {
        System.out.println("builder");
        DiskBackedStream instance = new DiskBackedStream(5, 3);
        
        Builder result = instance.builder();
        for(int i = 0; i < 15; i++) {
            result.add(randomVec(3));
        }
    }
}
