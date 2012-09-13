package eu.monnetproject.translation.langmodels;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;
import java.util.Random;
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
public class StickySamplingCounterTest {

    public StickySamplingCounterTest() {
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

    
    private NGram ngram(int... words) {
        return new NGram(words);
    }
    /**
     * Test of offer method, of class ExhaustiveCounter.
     */
    @Test
    public void testOffer() {
        System.out.println("offer");
        StickySamplingCounter instance = new StickySamplingCounter(3);
        instance.offer(1);
        instance.offer(2);
        instance.offer(3);
        instance.offer(2);
        instance.offer(3);
        instance.offer(3);
        instance.offer(1);
        instance.offer(2);
        final NGramCountSet counts = instance.counts();
        
        assertEquals(2,counts.ngramCount(2).getInt(ngram(2,3)));
        assertEquals(1,counts.ngramCount(2).getInt(ngram(3,3)));
        assertEquals(1,counts.ngramCount(3).getInt(ngram(2,3,2)));
    }

    /**
     * Test of docEnd method, of class ExhaustiveCounter.
     */
    @Test
    public void testDocEnd() {
        System.out.println("docEnd");
        StickySamplingCounter instance = new StickySamplingCounter(3);
        instance.offer(1);
        instance.offer(2);
        instance.offer(3);
        instance.offer(2);
        instance.docEnd();
        instance.offer(3);
        instance.offer(3);
        instance.offer(1);
        instance.offer(2);
        final NGramCountSet counts = instance.counts();
        
        assertEquals(1,counts.ngramCount(2).getInt(ngram(2,3)));
        assertEquals(1,counts.ngramCount(2).getInt(ngram(3,3)));
        assertFalse(counts.ngramCount(3).containsKey(ngram(2,3,3)));
    }

    @Test
    public void testMemory() {
        System.setProperty("sampling.critical","0.8");
        final Runtime rt = Runtime.getRuntime();
        class MemoryTestStickSamplingCounter extends StickySamplingCounter {
            int prunes = 0;
            
            public MemoryTestStickSamplingCounter(int N) {
                super(N);
            }

            @Override
            protected void prune() {
                System.err.println("Pruning at " + p);
                System.err.println("Free: " + ((rt.freeMemory() + rt.maxMemory() - rt.totalMemory())/1048576) + " Max: " + (rt.maxMemory()/1048576));
                System.err.println("3-grams: " + counts().ngramCount(3).size());
                super.prune();
                prunes++;
                System.err.println("3-grams: " + counts().ngramCount(3).size());
                System.err.println("Result");
                System.err.println("Free: " + ((rt.freeMemory() + rt.maxMemory() - rt.totalMemory())/1048576) + " Max: " + (rt.maxMemory()/1048576));
            }
            
        }
        final MemoryTestStickSamplingCounter instance = new MemoryTestStickSamplingCounter(3);
        final Random random = new Random();
        long p = 0;
        while(instance.prunes < 2) {
            instance.offer(random.nextInt());
            if(++p % 100000 == 0) {
                System.err.println("Free: " + ((rt.freeMemory() + rt.maxMemory() - rt.totalMemory())/1048576) + " Max: " + (rt.maxMemory()/1048576));
            }
        }
    }
    
}