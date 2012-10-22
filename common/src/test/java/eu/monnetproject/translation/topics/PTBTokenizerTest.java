/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics;

import eu.monnetproject.lang.Script;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
public class PTBTokenizerTest {
    
    public PTBTokenizerTest() {
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
     * Test of tokenize method, of class PTBTokenizer.
     */
    @Test
    public void testTokenize() {
        System.out.println("tokenize");
        String input = "So in \"this\" test, we wish to check everything; that "
                + "is in the PTB Guidelines. So..., a question? A statement! "
                + "(and maybe a note). And I'll say I'm gonna test somethings like "
                + "you're and you've and don't and I'LL, I'M, YOU'RE, DON'T. And some garbage: "
                + "Gimme, gonna, gotta, 'twas, 'tis, more'n, whaddya, whatcha. "
                + "Finally,    we   add   some    exageratted    spacing! "
                + "\u00abFrench quotes\u00bb \u201eGerman quotes\u201f";
        PTBTokenizer instance = new PTBTokenizer();
        List<String> expResult = Arrays.asList("So","in","``","this","''","test",",","we","wish","to","check","everything",";","that",
                "is","in","the","PTB","Guidelines",".","So","...",",","a","question","?","A","statement","!",
                "(","and","maybe","a","note",")",".","And","I","'ll","say","I","'m","gon","na","test","somethings","like",
                "you","'re","and","you","'ve","and","do","n't","and","I","'LL",",","I","'M",",","YOU","'RE",",","DO","N'T",".","And","some","garbage",":",
                "Gim","me",",","gon","na",",","got","ta",",","'t","was",",","'t","is",",","more","'n",",","wha","dd","ya",",","wha","t","cha",".",
                "Finally",",","we","add","some","exageratted","spacing","!","\u00ab","French","quotes","\u00bb","\u201e","German","quotes","\u201f");
        List<String> result = instance.tokenize(input);
        final Iterator<String> tokIter = result.iterator();
        final Iterator<String> expIter = expResult.iterator();
        while(tokIter.hasNext() && expIter.hasNext()) {
            assertEquals(expIter.next(),tokIter.next());
        }
        assertFalse(tokIter.hasNext());
        assertFalse(expIter.hasNext());
    }
    
    @Test
    public void testTokenize2() {
        System.out.println("tokenize2");
        String input = "Why are there tokens. with a full.stop at the end.";
        PTBTokenizer instance = new PTBTokenizer();
        List<String> expResult = Arrays.asList("Why","are","there","tokens",".","with","a","full.stop","at","the","end",".");
        List<String> result = instance.tokenize(input);
        final Iterator<String> tokIter = result.iterator();
        final Iterator<String> expIter = expResult.iterator();
        while(tokIter.hasNext() && expIter.hasNext()) {
            assertEquals(expIter.next(),tokIter.next());
        }
        assertFalse(tokIter.hasNext());
        assertFalse(expIter.hasNext());
    }
    
    @Test
    public void testTokenize3() {
        System.out.println("tokenize3");
        String input = "\u00a1arriba! at http://www.ariba.com should remove URL";
        PTBTokenizer instance = new PTBTokenizer();
        List<String> expResult = Arrays.asList("\u00a1","arriba","!","at","should","remove","URL");
        List<String> result = instance.tokenize(input);
        final Iterator<String> tokIter = result.iterator();
        final Iterator<String> expIter = expResult.iterator();
        while(tokIter.hasNext() && expIter.hasNext()) {
            assertEquals(expIter.next(),tokIter.next());
        }
        assertFalse(tokIter.hasNext());
        assertFalse(expIter.hasNext());
    }

    /**
     * Test of getScript method, of class PTBTokenizer.
     */
    @Test
    public void testGetScript() {
        System.out.println("getScript");
        PTBTokenizer instance = new PTBTokenizer();
        Script expResult = Script.LATIN;
        Script result = instance.getScript();
        assertEquals(expResult, result);
    }
}
