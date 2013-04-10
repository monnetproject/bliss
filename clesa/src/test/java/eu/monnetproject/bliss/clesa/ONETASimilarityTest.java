/**
 * *******************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *******************************************************************************
 */
package eu.monnetproject.bliss.clesa;

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.ParallelBinarizedReader;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.Vector;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
 * @author jmccrae
 */
public class ONETASimilarityTest {

    public ONETASimilarityTest() {
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

    @Test
    public void testTrain() throws Exception {
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

        final File outFile1 = File.createTempFile("lmatrix", ".bin");
        outFile1.deleteOnExit();
        final File outFile2 = File.createTempFile("lmatrix", ".bin");
        outFile2.deleteOnExit();

        final String oldClesaMetric = System.getProperty("clesaMethod");
        System.setProperty("clesaMethod", CLESAMethod.SIMPLE.toString());

        ONETATrain.train(corpus, W, outFile1, outFile2);
        DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(outFile1));

        assertEquals(1.414, in.readDouble(), 0.001);
        assertEquals(0.707, in.readDouble(), 0.001);
        assertEquals(0.707, in.readDouble(), 0.001);
        assertEquals(1.581, in.readDouble(), 0.001);

        in.close();

        in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(outFile2));

        assertEquals(2.236, in.readDouble(), 0.001);
        assertEquals(0.000, in.readDouble(), 0.001);
        assertEquals(0.000, in.readDouble(), 0.001);
        assertEquals(1.414, in.readDouble(), 0.001);
        in.close();

        if (oldClesaMetric != null) {
            System.setProperty("clesaMethod", oldClesaMetric);
        } else {
            System.clearProperty("clesaMethod");
        }
    }

    /**
     * Test of simVecSource method, of class ONETASimilarity.
     */
    @Test
    public void testSimVecSource() throws IOException {
        System.out.println("simVecSource");
        ONETASimilarity instance = makeSimMetric();
        Vector<Integer> termVec = SparseIntArray.fromArray(new int[]{0, 1, 1, 0, 0});
        Vector<Double> expResult = SparseRealArray.fromArray(new double[]{1, 0});
        Vector result = instance.simVecSource(termVec);
        assertArrayEquals(expResult.toDoubleArray(), result.toDoubleArray(), 0.01);
    }

    /**
     * Test of simVecTarget method, of class ONETASimilarity.
     */
    @Test
    public void testSimVecTarget() throws IOException {
        System.out.println("simVecTarget");
        ONETASimilarity instance = makeSimMetric();
        Vector<Integer> termVec = SparseIntArray.fromArray(new int[]{0, 0, 1, 1, 0});
        Vector<Double> expResult = SparseRealArray.fromArray(new double[]{0, 1});
        Vector result = instance.simVecTarget(termVec);
        assertArrayEquals(expResult.toDoubleArray(), result.toDoubleArray(), 0.01);
    }

    /**
     * Test of readLMatrix method, of class ONETASimilarity.
     */
    @Test
    public void testReadLMatrix() throws Exception {
        System.out.println("readLMatrix");
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
        out.writeInt(2);
        out.writeInt(2);
        out.writeInt(4);
        out.writeInt(0);
        out.writeInt(1);
        out.writeInt(2);
        out.writeInt(0);
        out.flush();
        out.close();
        int W = 5;

        final File outFile1 = File.createTempFile("lmatrix", ".bin");
        outFile1.deleteOnExit();
        final File outFile2 = File.createTempFile("lmatrix", ".bin");
        outFile2.deleteOnExit();

        final String oldClesaMetric = System.getProperty("clesaMethod");
        System.setProperty("clesaMethod", CLESAMethod.SIMPLE.toString());



        ONETATrain.train(corpus, W, outFile1, outFile2);

        SparseMatrix<Double>[] exp = new SparseMatrix[]{
            SparseMatrix.fromArray(new double[][]{
                {1.414214, 0.7071068, 1.414214},
                {0.7071068, 1.5811388, 1.264911},
                {1.414214, 1.264911, 1.183216}
            }),
            SparseMatrix.fromArray(new double[][]{
                {2.236068, 0.000000, 0.8944272},
                {0.000000, 1.414214, 0.7071068},
                {0.8944272, 0.7071068, 0.8366600}
            })
        };
        final DataInputStream dis1 = new DataInputStream(new FileInputStream(outFile1));
        for (int j = 0; j < exp[0].rows(); j++) {
            assertArrayEquals(exp[0].row(j).toDoubleArray(), readNDoubles(dis1, 3), 0.01);
        }
        dis1.close();
        final DataInputStream dis2 = new DataInputStream(new FileInputStream(outFile2));
        for (int j = 0; j < exp[1].rows(); j++) {
            assertArrayEquals(exp[1].row(j).toDoubleArray(), readNDoubles(dis2, 3), 0.01);
        }
        dis2.close();
        

        final SparseIntArray[][] x = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus)).readAll(4);
        final ONETASimilarity instance = new ONETASimilarity(x, outFile1, outFile2, 4);
        final Vector<Double> svs = instance.simVecSource(SparseIntArray.fromArray(new int[]{0, 0, 1, 1, 1}));

        assertArrayEquals(new double[]{0, 1, 0}, svs.toDoubleArray(), 0.01);

        if (oldClesaMetric != null) {
            System.setProperty("clesaMethod", oldClesaMetric);
        } else {
            System.clearProperty("clesaMethod");
        }
    }

    private double[] readNDoubles(DataInputStream in, int n) throws IOException {
        double[] v = new double[n];
        for (int i = 0; i < n; i++) {
            v[i] = in.readDouble();
        }
        return v;
    }

    private ONETASimilarity makeSimMetric() throws IOException {
        final String oldClesaMetric = System.getProperty("clesaMethod");
        System.setProperty("clesaMethod", CLESAMethod.SIMPLE.toString());

        SparseIntArray[][] x = new SparseIntArray[][]{
            {
                SparseIntArray.fromArray(new int[]{0, 1, 1, 0, 0}),
                SparseIntArray.fromArray(new int[]{0, 2, 0, 0, 1})
            },
            {
                SparseIntArray.fromArray(new int[]{0, 0, 1, 1, 1}),
                SparseIntArray.fromArray(new int[]{0, 0, 1, 1, 0})
            }
        };
        final File kernelFile1 = File.createTempFile("kernel", ".bin");
        kernelFile1.deleteOnExit();
        final DataOutputStream out1 = new DataOutputStream(new FileOutputStream(kernelFile1));
        out1.writeDouble(1.414214);
        out1.writeDouble(0.7071068);
        out1.writeDouble(0.7071068);
        out1.writeDouble(1.5811388);
        out1.flush();
        out1.close();
        
        final File kernelFile2 = File.createTempFile("kernel", ".bin");
        kernelFile2.deleteOnExit();
        final DataOutputStream out2 = new DataOutputStream(new FileOutputStream(kernelFile2));
        out2.writeDouble(2.236068);
        out2.writeDouble(0.000000);
        out2.writeDouble(0.000000);
        out2.writeDouble(1.414214);
        out2.flush();
        out2.close();
        final ONETASimilarity instance = new ONETASimilarity(x, kernelFile1,kernelFile2, 5);
        if (oldClesaMetric != null) {
            System.setProperty("clesaMethod", oldClesaMetric);
        } else {
            System.clearProperty("clesaMethod");
        }
        return instance;
    }
}