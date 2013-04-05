package eu.monnetproject.bliss.kcca;

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
public class KCCATrainTest {

    public KCCATrainTest() {
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
     * Test of train method, of class KCCATrain.
     */
    @Test
    public void testTrain() throws IOException {
        System.out.println("train");
        final File corpus = File.createTempFile("corpus", ".bin");
        corpus.deleteOnExit();
        final DataOutputStream out = new DataOutputStream(new FileOutputStream(corpus));
        out.writeInt(1);
        out.writeInt(2);
        out.writeInt(0);
        out.writeInt(1);
        out.writeInt(1);
        out.writeInt(4);
        out.writeInt(0);
        out.writeInt(2);
        out.writeInt(3);
        out.writeInt(4);
        out.writeInt(0);
        out.writeInt(2);
        out.writeInt(3);
        out.writeInt(0);
        out.flush();
        out.close();
        final double[][][] result = KCCATrain.train(corpus, 4, 2, 2, 0.8);
        final double[][][] expResult = new double[][][]{
            {
                {0.7210178,0.7210178}, 
                {0.9428695,0.9428695}, 
                {0.2218516,0.2218516}, 
                {0.2218516,0.2218516}
            },
            {
                {-0.3779645,0.3779645}, 
                {0.5669467,-0.5669467},
                {0.5669467,-0.5669467},
                {-0.1889822,0.1889822}
            }
        };
        for (int l = 0; l < 2; l++) {
            for (int w = 0; w < 4; w++) {
                for (int k = 0; k < 2; k++) {
                    expResult[l][w][k] = Math.abs(expResult[l][w][k]);
                    result[l][w][k] = Math.abs(result[l][w][k]);
                }
                assertArrayEquals(expResult[l][w], result[l][w], 0.001);
            }
        }

    }

}