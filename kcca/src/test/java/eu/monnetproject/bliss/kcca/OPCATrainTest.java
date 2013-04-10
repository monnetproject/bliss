/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.bliss.kcca;

import eu.monnetproject.math.sparse.SparseMatrix;
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
 * @author jmccrae
 */
public class OPCATrainTest {

    public OPCATrainTest() {
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
     * Test of calcN method, of class OPCATrain.
     */
    @Test
    public void testCalcN() throws Exception {
        System.out.println("calcN");
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
        int W = 5;
        int J = 2;
        double[][] df = {
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1}
        };
        double gamma = 1.0;
        SparseMatrix result = OPCATrain.calcN(corpus, W, J, df, gamma);
        assertArrayEquals(new double[]{1.1644, -0.2810, 0, 0}, result.row(0).toDoubleArray(), 0.01);
        assertArrayEquals(new double[]{-0.2810, 1.4804, 0, 0}, result.row(1).toDoubleArray(), 0.01);
        assertArrayEquals(new double[]{0, 0, 1, 0}, result.row(2).toDoubleArray(), 0.01);
        assertArrayEquals(new double[]{0, 0, 0, 1.9609}, result.row(3).toDoubleArray(), 0.01);
        //assertEquals(expResult, result);
    }
}
