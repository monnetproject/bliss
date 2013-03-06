package eu.monnetproject.bliss.experiments;

import eu.monnetproject.bliss.experiments.InterleaveFiles;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
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
public class InterleaveFilesTest {

    public InterleaveFilesTest() {
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
     * Test of interleave method, of class InterleaveFiles.
     */
    @Test
    public void testInterleave() throws Exception {
        System.out.println("interleave");
        File corpusFile1 = File.createTempFile("src", ".txt");
        corpusFile1.deleteOnExit();
        final PrintWriter out1 = new PrintWriter(corpusFile1);
        out1.println("A: 1 2 3 4 ");
        out1.println("B: 1 2 3 4 ");
        out1.println("C: 1 2 5 4 ");
        out1.close();
        File corpusFile2 = File.createTempFile("src", ".txt");
        corpusFile2.deleteOnExit();
        final PrintWriter out2 = new PrintWriter(corpusFile2);
        out2.println("A: 1 2 5 4 ");
        out2.println("AA: 1 2 3 4 ");
        out2.println("C: 1 2 3 4 ");
        out2.println("D: 1 2 3 4 ");
        out2.close();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        InterleaveFiles.interleave(corpusFile1, corpusFile2, out);
        byte[] expResult = new byte[] {
            0,0,0,1,
            0,0,0,2,
            0,0,0,3,
            0,0,0,4,
            0,0,0,0,
            0,0,0,1,
            0,0,0,2,
            0,0,0,5,
            0,0,0,4,
            0,0,0,0,
            0,0,0,1,
            0,0,0,2,
            0,0,0,5,
            0,0,0,4,
            0,0,0,0,
            0,0,0,1,
            0,0,0,2,
            0,0,0,3,
            0,0,0,4,
            0,0,0,0
        };
        assertArrayEquals(expResult, baos.toByteArray());
    }

}