/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics.lda;

import eu.monnetproject.lang.Language;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static eu.monnetproject.translation.topics.lda.LDATest.*;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

/**
 *
 * @author jmccrae
 */
public class GibbsInputTest {
    
    public GibbsInputTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private GibbsInput getInstance() {
        int K = 3;
        int D = 3;
        int W = 5;
        int[] DN = { 1,2,3 };
        int[][] x = {
            { 0 },
            { 1, 2 },
            { 3, 4, 0 }
        };
        Language[] languages = { Language.AMHARIC, Language.ZULU };
        int[] m = { 0, 1, 0 };
        int[][] mu = {
            { 0, 1 },
            { 0, 1 },
            { 2 }
        };
        Map<String,Integer> words = new HashMap<String, Integer>();
        words.put("baa", 0);
        words.put("boo", 1);
        words.put("bow", 2);
        words.put("beep", 3);
        words.put("woof", 4);
        return new GibbsInput(D, W, DN, x, languages, m, mu, words);
    }
    
    /**
     * Test of validate method, of class GibbsInput.
     */
    @Test
    public void testValidate() {
        System.out.println("validate");
        GibbsInput instance = getInstance();
        boolean expResult = true;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class GibbsInput.
     */
    @Test
    public void testWrite() throws Exception {
        System.out.println("write");
        final PipedInputStream inStream = new PipedInputStream();
        PipedOutputStream outStream = new PipedOutputStream(inStream);
        GibbsInput instance = getInstance();
        instance.write(outStream);
        instance.write(System.err);
        outStream.close();
        final GibbsInput result = GibbsInput.read(inStream);
        assertEquals(instance, result);
    }

}
