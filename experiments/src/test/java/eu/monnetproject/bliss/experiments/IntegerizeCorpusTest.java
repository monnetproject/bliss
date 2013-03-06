/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.bliss.experiments;

import eu.monnetproject.bliss.experiments.IntegerizeCorpus;
import eu.monnetproject.bliss.WordMap;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
public class IntegerizeCorpusTest {
    
    public IntegerizeCorpusTest() {
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
     * Test of main method, of class IntegerizeCorpus.
     */
    @Test
    public void testIntegerize() throws Exception {
        System.out.println("integerize");
        final ByteArrayInputStream corpusIn = new ByteArrayInputStream("<doc title=\"test\">\na b c d e f\n</doc>\n<doc title=\"experiment\">\nA B C D 9 0\n</doc>".getBytes());
        final WordMap wordMap = new WordMap();
        wordMap.offer("z");
        wordMap.offer("a");
        final StringWriter sw = new StringWriter();
        final PrintWriter out = new PrintWriter(sw);
        final int sampling = 2;
        IntegerizeCorpus.integerize(corpusIn, wordMap, out, sampling);
        final String expected = "test:2 3 4 " + System.getProperty("line.separator") + "experiment:2 3 5 " + System.getProperty("line.separator");
        assertEquals(expected,sw.toString());
    }
}
