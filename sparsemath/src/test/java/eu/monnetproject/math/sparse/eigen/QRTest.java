/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.Matrix;
import eu.monnetproject.math.sparse.SparseMatrix;
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
public class QRTest {

    public QRTest() {
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
     * Test of householder method, of class QR.
     */
    @Test
    public void testHouseholder() {
        System.out.println("householder");
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
        QR.Soln result = QR.decompose(m);
        final Matrix<Double> qr = SparseMatrix.fromArray(result.Q).product(SparseMatrix.fromArray(result.R));
        System.out.println(qr.toString());
        for (int i = 0; i < expResult.Q.length; i++) {
            assertArrayEquals(expResult.Q[i], result.Q[i], 0.01);
        }
        for (int i = 0; i < expResult.R.length; i++) {
            assertArrayEquals(expResult.R[i], result.R[i], 0.01);
        }
    }
    
    @Test
    public void testEigen() {
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
        QR.Soln result = QR.eigen(m);
        for (int i = 0; i < expResult.Q.length; i++) {
            assertArrayEquals(expResult.Q[i], result.Q[i], 0.01);
        }
        for (int i = 0; i < expResult.R.length; i++) {
            assertArrayEquals(expResult.R[i], result.R[i], 0.01);
        }
        
    }
}