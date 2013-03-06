package eu.monnetproject.bliss.lda;

import eu.monnetproject.bliss.lda.LDASimilarityMetric;
import eu.monnetproject.math.sparse.IntVector;
import eu.monnetproject.math.sparse.Vector;
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
public class LDASimilarityMetricTest {

    public LDASimilarityMetricTest() {
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
     * Test of simVecSource method, of class LDASimilarityMetric.
     */
    @Test
    public void testSimVecSource() {
        System.out.println("simVecSource");
        int K = 2;
        int W = 5;
        final int[][][] N_lkw = new int[][][] {
                                    { 
                                        { 10,10,1,1,1 },
                                        { 1,1,10,10,10 }
                                    },
                                    {
                                        { 1,1,10,10,10 },
                                        { 10,10,1,1,1 }
                                    }
                                };
        final int[][] N_lk = new int[][] {
            { 23, 23 },
            { 23, 23 }
        };
          
        
        Vector<Integer> termVec1 = new IntVector(new int[] { 0,5,5,0,1,1 });
        Vector<Integer> termVec2 = new IntVector(new int[] { 0,0,1,5,4,6 });
        LDASimilarityMetric instance = new LDASimilarityMetric(K, W, N_lkw, N_lk, 0.1, 0.1);
        final Vector<Double> v1 = instance.simVecSource(termVec1);
        final Vector<Double> v2 = instance.simVecSource(termVec2);
        final Vector<Double> v3 = instance.simVecTarget(termVec2);
        assertTrue(cosine(v1,v2) < cosine(v1,v3));
    }
    
    private double cosine(Vector<Double> v1, Vector<Double> v2) {
        assert(v1.length() == v2.length());
        double aa = 0, bb = 0, ab = 0;
        for(int i = 0; i < v1.length(); i++) {
            aa += v1.doubleValue(i) * v1.doubleValue(i);
            bb += v2.doubleValue(i) * v2.doubleValue(i);
            ab += v1.doubleValue(i) * v2.doubleValue(i);
        }
        return ab / Math.sqrt(bb*aa);
    }

}