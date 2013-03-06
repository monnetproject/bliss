package eu.monnetproject.bliss.lsa;

import eu.monnetproject.bliss.lsa.DataStreamIterable;
import eu.monnetproject.bliss.lsa.LSATrain;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.bliss.lsa.LSATrain.LSAStreamApply;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        final DataStreamIterable iterable = new DataStreamIterable(tmpFile, 100);
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
    
    private File writeTempDoc() throws IOException {
        final File tmpFile = File.createTempFile("matrix", ".bin");
        tmpFile.deleteOnExit();
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(tmpFile));
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(3);
        dos.writeInt(3);
        dos.writeInt(4);
        dos.writeInt(4);
        dos.writeInt(5);
        dos.writeInt(5);
        dos.writeInt(0);

        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(2);
        dos.writeInt(3);
        dos.writeInt(4);
        dos.writeInt(4);
        dos.writeInt(5);
        dos.writeInt(5);
        dos.writeInt(5);
        dos.writeInt(5);
        dos.writeInt(0);

        dos.writeInt(5);
        dos.writeInt(1);
        dos.writeInt(3);
        dos.writeInt(2);
        dos.writeInt(2);
        dos.writeInt(4);
        dos.writeInt(1);
        dos.writeInt(5);
        dos.writeInt(5);
        dos.writeInt(4);
        dos.writeInt(0);

        dos.writeInt(3);
        dos.writeInt(5);
        dos.writeInt(2);
        dos.writeInt(3);
        dos.writeInt(3);
        dos.writeInt(2);
        dos.writeInt(3);
        dos.writeInt(4);
        dos.writeInt(4);
        dos.writeInt(1);
        dos.writeInt(0);

        dos.flush();
        dos.close();

        return tmpFile;
    }
    
    @Test
    public void testLSAApply() throws Exception {
        File f = writeTempDoc();
            final LSAStreamApply lsaApply = new LSATrain.LSAStreamApply(f, 5, 2, null);
        Assert.assertArrayEquals(new double[] { 295,124,176,238,300,233,181,305,238,290 }, 
                lsaApply.apply(new RealVector(new double[] { 1,2,3,4,5,5,4,3,2,1 })).toDoubleArray()
                , 0.0000000001);
    }

}