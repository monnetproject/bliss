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
public class NGramCarouselTest {

    public NGramCarouselTest() {
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
     * Test of offer method, of class NGramCarousel.
     */
    @Test
    public void testOffer() {
        System.out.println("offer");
        int n = 0;
        NGramCarousel instance = new NGramCarousel(3);
        instance.offer(1);
        assertTrue(instance.maxNGram() == 1);
        instance.offer(2);
        assertTrue(instance.maxNGram() == 2);
        instance.offer(3);
        assertTrue(instance.maxNGram() == 3);
        assertEquals(ngram(1,2,3),instance.ngram(3));
        instance.offer(4);
        assertEquals(ngram(3,4), instance.ngram(2));
        instance.offer(5);
        assertEquals(ngram(3,4,5),instance.ngram(3));
        instance.offer(6);
        assertEquals(ngram(4,5,6), instance.ngram(3));
        instance.offer(7);
        assertEquals(ngram(5,6,7), instance.ngram(3));
    }
}