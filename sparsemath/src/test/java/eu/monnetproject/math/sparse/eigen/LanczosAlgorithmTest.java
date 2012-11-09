/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.TridiagonalMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jmccrae
 */
public class LanczosAlgorithmTest {

    public LanczosAlgorithmTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLanczos() {
        final SparseMatrix<Double> A = SparseMatrix.fromArray(new double[][]{
                    {1, 3, 4},
                    {3, 2, 8},
                    {4, 8, 3}
                });
        final TridiagonalMatrix tri = LanczosAlgorithm.lanczos(A).tridiagonal();
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

    @Test
    public void testLanczosDiag() {
        final SparseMatrix<Double> A = SparseMatrix.fromArray(new double[][]{
                    {1, 0, 0, 0},
                    {0, 2, 0, 0},
                    {0, 0, 3, 0},
                    {0, 0, 0, 4}
                });
        double[] w = {0.2192688, 0.1660255, 0.849479, 0.4507047};
        final TridiagonalMatrix tri = LanczosAlgorithm.lanczos(A.asVectorFunction(), new RealVector(w)).tridiagonal();
        Assert.assertArrayEquals(new double[]{3.079, 2.338, 2.578, 2.004}, tri.alpha(), 0.01);
        Assert.assertArrayEquals(new double[]{0.6455, 1.277, 0.381}, tri.beta(), 0.01);
    }
}
