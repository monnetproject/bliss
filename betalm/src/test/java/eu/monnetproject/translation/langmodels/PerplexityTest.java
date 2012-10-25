package eu.monnetproject.translation.langmodels;

import eu.monnetproject.translation.langmodels.impl.ARPALM;
import java.io.File;
import java.util.Scanner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static java.lang.Math.*;

/**
 *
 * @author john
 */
public class PerplexityTest {

    public PerplexityTest() {
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

    private static final double LOG_10_2 = 0.3010299956639812;
    /**
     * Test of calculatePerplexity method, of class Perplexity.
     */
    @Test
    public void testCalculatePerplexity() throws Exception {
        System.out.println("calculatePerplexity");
        Scanner scanner = new Scanner("this is a test\nAnd so is this\n");
        ARPALM lm = new ARPALM(new File("src/test/resources/simple.lm"));
        double expResult = (log10(2.0/8.0) + log10(1.0/6.0) + log10(1.0/6.0) + log10(2.0/6.0/60.0) + log10(1.0/8.0)) / 4.0 
                + (log10(1.0/8.0) + log10(1.0/6.0) + log10(2.0/6.0/60.0)  + log10(2.0/8.0) + log10(1.0/6.0)) / 4.0;
        double result = Perplexity.calculatePerplexity(scanner, lm);
        assertEquals(expResult/LOG_10_2, result, 0.00001);
    }

}