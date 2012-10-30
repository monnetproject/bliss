package eu.monnetproject.translation.topics;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class MMapFileInputStreamTest {

    public MMapFileInputStreamTest() {
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

    private File setUpFile() throws IOException {
        final File tmpFile = File.createTempFile("tmp", ".bin");
        tmpFile.deleteOnExit();
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(tmpFile));
        for(int i = 0; i < 128; i++) {
            dos.writeByte(i);
        }
        dos.flush();
        dos.close();
        return tmpFile;
    }
    
    /**
     * Test of read method, of class MMapFileInputStream.
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        MMapFileInputStream instance = new MMapFileInputStream(setUpFile(), 8);
        for(int i = 0; i < 128; i++) {
            assertEquals(i,instance.read());
        }
    }

    /**
     * Test of skip method, of class MMapFileInputStream.
     */
    @Test
    public void testSkip() throws Exception {
        System.out.println("skip");
        
        MMapFileInputStream instance = new MMapFileInputStream(setUpFile(), 8);
        instance.skip(4);
        for(int i = 4; i < 10; i++) {
            assertEquals(i,instance.read());
        }
        instance.skip(2);
        for(int i = 12; i < 30; i++) {
            assertEquals(i,instance.read());
        }
        instance.skip(8);
        for(int i = 38; i < 50; i++) {
            assertEquals(i,instance.read());
        }
        instance.skip(20);
        for(int i = 70; i < 128; i++) {
            assertEquals(i,instance.read());
        }
    }

    /**
     * Test of available method, of class MMapFileInputStream.
     */
    @Test
    public void testAvailable() throws Exception {
        System.out.println("available");
        MMapFileInputStream instance = new MMapFileInputStream(setUpFile(), 8);
        for(int i = 0; i < 128; i++) {
            assertEquals(128-i,instance.available());
            instance.read();
        }
    }

    /**
     * Test of mark method, of class MMapFileInputStream.
     */
    @Test
    public void testMark() throws Exception {
        System.out.println("mark");
        MMapFileInputStream instance = new MMapFileInputStream(setUpFile(), 8);
        instance.skip(4);
        instance.mark(0);
        for(int i = 4; i < 50; i++) {
            instance.read();
        }
        instance.reset();
        assertEquals(4,instance.read());
    }

    /**
     * Test of markSupported method, of class MMapFileInputStream.
     */
    @Test
    public void testMarkSupported() throws Exception {
        System.out.println("markSupported");
        MMapFileInputStream instance = new MMapFileInputStream(setUpFile(), 8);
        assertTrue(instance.markSupported());
    }
    /**
     * Test of read method, of class MMapFileInputStream.
     */
    @Test
    public void testRead_3args() throws Exception {
        System.out.println("read");
        byte[] b = new byte[20];
        int off = 3;
        int len = 17;
        MMapFileInputStream instance = new MMapFileInputStream(setUpFile(),8);
        int expResult = 17;
        int result = instance.read(b, off, len);
        assertEquals(expResult, result);
        assertEquals(2,instance.read(b,off,2));
        
    }

}