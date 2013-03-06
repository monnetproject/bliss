package eu.monnetproject.bliss;

import eu.monnetproject.bliss.WordMap;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
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
public class WordMapTest {

    public WordMapTest() {
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
     * Test of fromFile method, of class WordMap.
     */
    @Test
    public void testFromFile() throws Exception {
        System.out.println("fromFile");
        File file = File.createTempFile("wordMap", "");
        file.deleteOnExit();
        final DataOutputStream os = new DataOutputStream(new FileOutputStream(file));
        os.writeUTF("test");
        os.writeInt(1);
        os.writeUTF("experiment");
        os.writeInt(2);
        os.writeUTF("prove");
        os.writeInt(3);
        os.close();
        WordMap expResult = new WordMap();
        expResult.offer("test");
        expResult.offer("experiment");
        expResult.offer("prove");
        WordMap result = WordMap.fromFile(file);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WordMap.
     */
    @Test
    public void testWrite() throws Exception {
        System.out.println("write");
        File file = File.createTempFile("wordMap", "");
        file.deleteOnExit();
        WordMap instance = new WordMap();
        instance.offer("test");
        instance.offer("experiment");
        instance.offer("prove");
        instance.write(file);
        WordMap expResult = new WordMap();
        expResult.offer("test");
        expResult.offer("experiment");
        expResult.offer("prove");
        WordMap result = WordMap.fromFile(file);
        assertEquals(expResult, result);
    }

    /**
     * Test of verifyIntegrity method, of class WordMap.
     */
    @Test
    public void testVerifyIntegrity() {
        System.out.println("verifyIntegrity");
        WordMap instance = new WordMap();
        instance.offer("test");
        instance.offer("experiment");
        instance.offer("prove");
        boolean expResult = true;
        boolean result = instance.verifyIntegrity();
        assertEquals(expResult, result);
    }

    /**
     * Test of invert method, of class WordMap.
     */
    @Test
    public void testInvert() {
        System.out.println("invert");
        WordMap instance = new WordMap();
        instance.offer("test");
        instance.offer("experiment");
        instance.offer("prove");
        String[] expResult = { null, "test", "experiment", "prove" };
        String[] result = instance.invert();
        assertArrayEquals(expResult, result);
    }


}