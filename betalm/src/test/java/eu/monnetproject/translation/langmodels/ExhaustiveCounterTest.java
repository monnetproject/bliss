package eu.monnetproject.translation.langmodels;

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
public class ExhaustiveCounterTest {

    public ExhaustiveCounterTest() {
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
        ExhaustiveCounter instance = new ExhaustiveCounter(3);
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
        ExhaustiveCounter instance = new ExhaustiveCounter(3);
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
}