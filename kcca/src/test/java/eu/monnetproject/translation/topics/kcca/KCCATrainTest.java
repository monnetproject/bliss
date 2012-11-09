package eu.monnetproject.translation.topics.kcca;

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition.Solution;
import eu.monnetproject.translation.topics.kcca.KCCATrain.D;
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
        double kappa = 0.8;
        KCCATrain instance = new KCCATrain();
        final double[][][] result = instance.train(corpus, 4, 2, 4, 0.8);
        final double[][][] expResult = new double[][][]{
            {
                {0.7210178, 0.2218516, 4.100756e-01, 0.5125945},
                {1.4420356, 0.4437033, 2.220446e-16, 0.0000000},
                {0.7210178, 0.2218516, 4.100756e-01, 0.5125945},
                {0.7210178, 0.2218516, 4.100756e-01, 0.5125945}
            },
            {
                {1.1338934, 1.1338934, 0.3779645, 1.1338934},
                {0.5669467, 0.5669467, 0.1889822, 0.5669467},
                {0.5669467, 0.5669467, 0.1889822, 0.5669467},
                {0.5669467, 0.5669467, 0.1889822, 0.5669467}
            }
        };
        for (int l = 0; l < 2; l++) {
            for (int w = 0; w < 4; w++) {
                for (int j = 0; j < 4; j++) {
                    result[l][w][j] = Math.abs(result[l][w][j]);
                }
                assertArrayEquals(expResult[l][w], result[l][w], 0.001);
            }
        }

    }

    @Test
    public void testNormalizer() throws Exception {
        System.out.println("Normalizer");
        final File corpus = File.createTempFile("corpus", ".bin");
        corpus.deleteOnExit();
        final DataOutputStream out = new DataOutputStream(new FileOutputStream(corpus));
        out.writeInt(1);
        out.writeInt(2);
        out.writeInt(0);
        out.writeInt(1);
        out.writeInt(2);
        out.writeInt(0);
        out.writeInt(2);
        out.writeInt(3);
        out.writeInt(0);
        out.writeInt(2);
        out.writeInt(3);
        out.writeInt(0);
        out.flush();
        out.close();
        final D normalizer = new KCCATrain.D(corpus, 2, 3, 0.2);
        final RealVector v = new RealVector(new double[]{1.0, 0.5, 0.2, 0.7});
        final Vector<Double> result = normalizer.apply(v);
        assertArrayEquals(new double[]{6.1, 5.6, 3.26, 3.76}, result.toDoubleArray(), 0.0001);

    }

    @Test
    public void testNormalizer2() throws Exception {
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
        final D normalizer = new KCCATrain.D(corpus, 2, 4, 0.8);
        final RealVector v = new RealVector(new double[]{1.0, 0.5, 0.2, 0.7});
        final Vector<Double> result = normalizer.apply(v);
        assertArrayEquals(new double[]{3.5, 4.0, 1.8, 1.68}, result.toDoubleArray(), 0.0001);
    }
}