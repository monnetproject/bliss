package eu.monnetproject.math.sparse.eigen;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import eu.monnetproject.math.sparse.eigen.HessenbergQR.Solution;
import java.util.Arrays;
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
public class HessenbergQRTest {

    public HessenbergQRTest() {
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
     * Test of solve method, of class HessenbergQR.
     */
    @Test
    public void testSolve() {
        System.out.println("hessenbergQRSolve");
        final double[][] A = new double[][]{
            {1, 2, 3},
            {1, 1, 2},
            {0, 1, 1}
        };
        final Solution solution = HessenbergQR.solve(3, A);
        Arrays.sort(solution.d);
        assertArrayEquals(new double[]{-0.302775, 0, 3.302776,}, solution.d, 0.0001);
        final double[][] A2 = new double[][]{
            {1, 2, 3},
            {1, 1, 2},
            {0, 1, 1}
        };
        final EigenvalueDecomposition evd = new EigenvalueDecomposition(new DenseDoubleMatrix2D(A2));
        final DoubleMatrix1D re = evd.getRealEigenvalues();
        final double[] colt = re.toArray();
        Arrays.sort(colt);
        assertArrayEquals(colt, solution.d, 0.01);
    }
}