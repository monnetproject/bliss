/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.math.sparse.eigen;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
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
public class QROnDiskTest {
    
    public QROnDiskTest() {
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
     * Test of decompose method, of class QROnDisk.
     */
    @Test
    public void testDecompose() throws Exception {
        System.out.println("decompose");
        double[][] m = {
            {12, -51, 4},
            {6, 167, -68},
            {-4, 24, -41}
        };
        QR.Soln expResult = new QR.Soln(new double[][]{
            {-0.8571429, 0.3942857, -0.33142857},
            {-0.4285714, -0.9028571, 0.03428571},
            {0.2857143, -0.1714286, -0.94285714}
        },
                new double[][]{
            {-14, -21, 14},
            {0, -175, 70},
            {0, 0.00000, 35}
        });
        QROnDisk.DiskMatrix matrix = matrix(m);
        QROnDisk.DiskMatrix Q = QROnDisk.DiskMatrix.allocate(tmpFile(), 3);
        QROnDisk.DiskMatrix R = QROnDisk.DiskMatrix.allocate(tmpFile(), 3);
        QROnDisk.decompose(matrix, Q, R);
        matrixEquals(expResult.Q, Q, 3);
        matrixEquals(expResult.R, R, 3);
    }

    /**
     * Test of eigen method, of class QROnDisk.
     */
    @Test
    public void testEigen() throws Exception {
        System.out.println("eigen");
        double[][] m = {
            {24 , -45,    0},
            { -45 , 334 , -44},
            {0 , -44 , -82}
        };
        QR.Soln expResult = new QR.Soln(new double[][] {
            {0.1381762 ,0.04408421, -0.98942605 },
            {-0.9851873 , 0.10855444, -0.13274759},
            {0.1015545, 0.99311254, 0.05843084}
        }, new double[][] {
            {344.84700,0,0},
            {0,-86.80952,0},
            {0,0,17.96252}
        });
        QROnDisk.DiskMatrix matrix = matrix(m);
        QROnDisk.DiskMatrix eigenvectors = QROnDisk.DiskMatrix.allocate(tmpFile(), 3);
        QROnDisk.DiskMatrix Q = QROnDisk.DiskMatrix.allocate(tmpFile(), 3);
        QROnDisk.DiskMatrix Q2 = QROnDisk.DiskMatrix.allocate(tmpFile(), 3);
        QROnDisk.DiskMatrix R = QROnDisk.DiskMatrix.allocate(tmpFile(), 3);
        QROnDisk.Soln result = QROnDisk.eigen(matrix, eigenvectors, Q, Q2, R);
        matrixEquals(expResult.Q, result.Q, 3);
        matrixEquals(expResult.R, result.R, 3);
    }

    private QROnDisk.DiskMatrix matrix(double[][] mat) throws IOException {
        final File tmpFile = File.createTempFile("matrix", ".bin");
        tmpFile.deleteOnExit();
        final RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw");
        QROnDisk.DiskMatrix q = new QROnDisk.DiskMatrix(raf, mat.length);
        for(int i = 0; i < mat.length; i++) {
            q.writeRow(i, mat[i]);
        }
        return q;
    }
    
    private RandomAccessFile tmpFile() throws IOException {
        final File tmpFile = File.createTempFile("matrix", ".bin");
        tmpFile.deleteOnExit();
        return new RandomAccessFile(tmpFile, "rw");
    }
    
    /**
     * Test of transpose method, of class QROnDisk.
     */
    @Test
    public void testTranspose() throws Exception {
        System.out.println("transpose");
        final File tmpFile = File.createTempFile("matrix", ".bin");
        tmpFile.deleteOnExit();
        final RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw");
        QROnDisk.DiskMatrix q = new QROnDisk.DiskMatrix(raf, 10);
        final Random r = new Random();
        double[][] A = new double[10][];
        for(int i = 0; i < 10; i++) {
            double[] d = new double[10];
            for(int j = 0; j < 10; j++) {
                d[j] = r.nextDouble();
            }
            q.writeRow(i, d);
            A[i] = d;
        }
        int BLOCK_SIZE = 3;
        QROnDisk.transpose(q, BLOCK_SIZE);
        QR.transpose(A);
        matrixEquals(A, q, 10);
    }

    private void matrixEquals(double[][] A, QROnDisk.DiskMatrix q, int n) throws IOException {
        for(int i = 0; i < n; i++) {
            assertArrayEquals(A[i],q.readRow(i),0.00001);
        }
    }
}