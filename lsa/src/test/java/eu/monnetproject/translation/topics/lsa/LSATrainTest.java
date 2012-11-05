package eu.monnetproject.translation.topics.lsa;

import eu.monnetproject.translation.topics.lsa.LSATrain.LSAStreamIterable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author john
 */
public class LSATrainTest {

    public LSATrainTest() {
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
     * Test of main method, of class LSATrain.
     */
    @Test
    public void testSkipOddIterator() throws Exception {
        System.out.println("skipOddIterator");
        final File tmpFile = File.createTempFile("tmp", ".bin");
        tmpFile.deleteOnExit();
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(tmpFile));
        for(int i = 0; i < 100; i++) {
            if(i % 10 == 9) {
                dos.writeInt(0);
            } else {
                dos.writeInt(i);
            }
        }
        dos.flush();
        dos.close();
        int J = 0;
        final LSAStreamIterable iterable = new LSATrain.LSAStreamIterable(tmpFile, 100);
        int i = 0;
        for(int j : iterable) {
            if(i % 20 == 9) {
                i++;
            }
            if(i % 20 == 19) {
                J++;
            } else {
                Assert.assertEquals(i + (i % 20 > 9 ? 100 : 0),j);
            }
            i++;
        }
        Assert.assertEquals(5, J);
    }

}