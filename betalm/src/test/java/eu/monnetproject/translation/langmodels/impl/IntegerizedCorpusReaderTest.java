package eu.monnetproject.translation.langmodels.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Arrays;
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
public class IntegerizedCorpusReaderTest {

    public IntegerizedCorpusReaderTest() {
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
     * Test of nextToken method, of class IntegerizedCorpusReader.
     */
    @Test
    public void testNextToken() throws Exception {
        System.out.println("nextToken");
        final DataInputStream corpus = new DataInputStream(new ByteArrayInputStream(new byte[] { 
            0,0,0,1,
            0,0,0,2,
            0,0,0,3,
            0,0,0,0,
            0,0,0,1,
            0,0,0,3,
            0,0,0,4,
            0,0,0,0,
            0,0,0,1,
            0,0,0,1,
            0,0,0,0,
                
        }));
        int[] expResult = { 1,2,3,0,1,3,4,0,1,1,0 };
        IntegerizedCorpusReader instance = new IntegerizedCorpusReader(corpus);
        int i = 0;
        while(instance.hasNext()) {
            assertEquals(expResult[i],instance.nextToken());
            i++;
        }
    }

    /**
     * Test of nextDocument method, of class IntegerizedCorpusReader.
     */
    @Test
    public void testNextDocument() throws Exception {
        System.out.println("nextDocument");
        
        final DataInputStream corpus = new DataInputStream(new ByteArrayInputStream(new byte[] { 
            0,0,0,1,
            0,0,0,2,
            0,0,0,3,
            0,0,0,0,
            0,0,0,1,
            0,0,0,3,
            0,0,0,4,
            0,0,0,0,
            0,0,0,1,
            0,0,0,1,
            0,0,0,0,
                
        }));
        int[][] expResult = { 
            { 1,2,3 },
            { 1,3,4 },
            { 1,1 }
        };
        int i = 0;
        IntegerizedCorpusReader instance = new IntegerizedCorpusReader(corpus);
        while(instance.nextDocument()) {
            final int[] result = Arrays.copyOfRange(instance.getBuffer(),0,instance.getBufferSize());
            assertArrayEquals(expResult[i], result);
            i++;
        }
    }

}