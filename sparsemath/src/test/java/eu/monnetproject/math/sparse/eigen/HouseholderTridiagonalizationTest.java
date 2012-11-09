/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.TridiagonalMatrix;
import eu.monnetproject.math.sparse.Vector;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author jmccrae
 */
public class HouseholderTridiagonalizationTest {

    public HouseholderTridiagonalizationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of house method, of class HouseholderTridiagonalization.
     */
    @Test
    public void testHouse() {
        System.out.println("house");
        RealVector x = new RealVector(new double[]{1, 2, 3, 4});
        int j = 0;
        double[] expResult = {1, 0.309, 0.463, 0.617};
        Vector<Double> result = HouseholderTridiagonalization.house(x, j);
        Assert.assertArrayEquals(expResult, result.toDoubleArray(), 0.01);
    }

    /**
     * Test of tridiagonalize method, of class HouseholderTridiagonalization.
     */
    @Test
    public void testTridiagonalize() {
        System.out.println("tridiagonalize");
        final SparseMatrix<Integer> A = SparseMatrix.fromArray(new int[][]{
                    {1, 3, 4},
                    {3, 2, 8},
                    {4, 8, 3}
                });
        final TridiagonalMatrix tri = HouseholderTridiagonalization.tridiagonalize(A);
        final RealMatrix mat = new Array2DRowRealMatrix(3, 3);
        for (int i = 0; i < 3; i++) {
            mat.setEntry(i, i, tri.alpha()[i]);
            if (i < 2) {
                mat.setEntry(i, i + 1, tri.beta()[i]);
                mat.setEntry(i + 1, i, tri.beta()[i]);
            }
        }
        final EigenDecompositionImpl eigenDecompositionImpl = new EigenDecompositionImpl(mat, 0.0001);
        final double[] realEigenvalues = eigenDecompositionImpl.getRealEigenvalues();
        Assert.assertArrayEquals(new double[]{12.64, -1.06, -5.57}, realEigenvalues, 0.01);
    }
}
