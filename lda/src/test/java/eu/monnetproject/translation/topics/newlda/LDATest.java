package eu.monnetproject.translation.topics.newlda;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
public class LDATest {

    public LDATest() {
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
     * Test of train method, of class LDA.
     */
    @Test
    public void testTrain() throws Exception {
        System.out.println("train");
        
        final int K = 2;
        final int D = 4;
        final int W = 10;
        //final int L = 2;
        //final int[] N = { 5, 5, 5, 5 };
        final int[][] x = {
            { 0,2,1,0,1 },
            { 6,7,7,5,6 },
            { 3,3,4,4,3 },
            { 8,9,9,9,8 }
        };
        final File f = File.createTempFile("lda", ".buf");
        f.deleteOnExit();
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
        for(int i = 0; i < x.length; i++) {
            for(int j = 0; j < x[i].length; j++) {
                dos.writeInt(x[i][j]+1);
            }
            dos.writeInt(0);
        }
        dos.flush();
        dos.close();
        int iterations = 20;
        LDATrain instance = new LDATrain(f, K, 2, W, 0.1, 0.1);
        instance.train(iterations);
    }

}