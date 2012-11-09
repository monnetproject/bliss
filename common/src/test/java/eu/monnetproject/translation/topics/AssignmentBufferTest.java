package eu.monnetproject.translation.topics;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
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
public class AssignmentBufferTest {

    public AssignmentBufferTest() {
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
     * Test of getNext method, of class AssignmentBuffer.
     */
    @Test
    public void test() throws Exception {
        System.out.println("getNext");
        final File f = File.createTempFile("assign", "buf");
        f.deleteOnExit();
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
        for(int i = 0; i < 100; i++) {
            dos.writeInt(i);
        }
        dos.flush();
        dos.close();
        final RandomAccessFile raf = new RandomAccessFile(f, "rw");
        final AssignmentBuffer buf = new AssignmentBuffer(raf.getChannel(), 64, raf.length());
        for(int i = 0; i < 100; i++) {
            assertTrue(buf.hasNext());
            assertEquals(i, buf.getNext());
            if(i % 2 == 0) {
                buf.update(0);
            }
        }
        buf.reset();
        for(int i = 0; i < 100; i++) {
            assertTrue(buf.hasNext());
            assertEquals(i % 2 == 0 ? 0 : i, buf.getNext());
        }
    }

}