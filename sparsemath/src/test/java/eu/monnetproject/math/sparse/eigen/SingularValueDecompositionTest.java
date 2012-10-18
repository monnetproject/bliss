package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition.Solution;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class SingularValueDecompositionTest {

    public SingularValueDecompositionTest() {
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
     * Test of calculate method, of class SingularValueDecomposition.
     */
    @Test
    public void testCalculate() throws IOException {
        System.out.println("calculate");
        File matrixFile = writeTempDoc();
        int W = 5;
        int J = 4;
        int K = 4;
        double epsilon = 0.0001;
        SingularValueDecomposition instance = new SingularValueDecomposition();
        Solution expResult = new Solution(new double[][]{
                    {-0.4711454,-0.05076119,-0.79897599,-0.3702467},
                    {-0.5780836,-0.47792513,0.06631807,0.6580340},
                    {-0.4914296,-0.17556759,0.58753338,-0.6184477},
                    {-0.4498203,0.85917803,0.10974433,0.2177866}
                },
                new double[][]{
                    {-0.4990883, -0.2681114, -0.4152654, -0.4337752, -0.5642879},
                    {-0.31035211, 0.25602104, 0.77203884, 0.08920279, -0.48387363},
                    {-0.48675799, 0.77871324, -0.26925406, -0.03771858, 0.28766632},
                    {-0.3579756, -0.3293398, 0.3912857, -0.5188684, 0.5840033}
                },
                new double[]{
                    9.1774677, 3.4735260, 1.8760096, 0.4350767
                });
        Solution result = instance.calculate(matrixFile, W, J, K, epsilon);

        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                assertEquals(Math.abs(expResult.U[i][j]), Math.abs(result.U[i][j]),0.02);
                assertEquals(Math.abs(expResult.V[i][j]), Math.abs(result.V[i][j]),0.02);
            }
        }
        assertArrayEquals(expResult.S, result.S, 0.2);
    }
    
    public double[] positive(double[] exp, double[] act) {
        final double sgn = Math.signum(exp[0]*act[0]);
        if(sgn == 0) {
            assert(false);
        }
        for(int i = 0; i < act.length; i++) {
            act[i] *= sgn;
        }
        return act;
    }

    @Test
    public void testInnerProduct() throws Exception {
        System.out.println("innerProduct");
        File matrixFile = writeTempDoc();
        final VectorFunction<Double> innerProduct = new SingularValueDecomposition.InnerProductMultiplication(matrixFile, 4);
        final RealVector v = new RealVector(new double[]{1, 2, 3, 4, 5});
        final Vector<Double> apply = innerProduct.apply(v);
        assertEquals(new RealVector(new double[]{283, 160, 242, 250, 324}), apply);
    }

    @Test
    public void testOuterProduct() throws Exception {
        System.out.println("outerProduct");
        File matrixFile = writeTempDoc();
        final VectorFunction<Double> outerProduct = new SingularValueDecomposition.OuterProductMultiplication(matrixFile, 5);
        final RealVector v = new RealVector(new double[]{1, 2, 3, 4});
        final Vector<Double> apply = outerProduct.apply(v);
        assertEquals(new RealVector(new double[]{189, 228, 202, 206}), apply);
    }

    private File writeTempDoc() throws IOException {
        final File tmpFile = File.createTempFile("matrix", ".bin");
        tmpFile.deleteOnExit();
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(tmpFile));
        dos.writeInt(1);
        dos.writeInt(3);
        dos.writeInt(5);
        dos.writeInt(1);
        dos.writeInt(5);
        dos.writeInt(4);
        dos.writeInt(3);
        dos.writeInt(4);
        dos.writeInt(1);
        dos.writeInt(0);

        dos.writeInt(3);
        dos.writeInt(4);
        dos.writeInt(5);
        dos.writeInt(1);
        dos.writeInt(5);
        dos.writeInt(1);
        dos.writeInt(5);
        dos.writeInt(1);
        dos.writeInt(5);
        dos.writeInt(2);
        dos.writeInt(4);
        dos.writeInt(0);

        dos.writeInt(5);
        dos.writeInt(1);
        dos.writeInt(3);
        dos.writeInt(2);
        dos.writeInt(2);
        dos.writeInt(4);
        dos.writeInt(1);
        dos.writeInt(5);
        dos.writeInt(5);
        dos.writeInt(4);
        dos.writeInt(0);

        dos.writeInt(3);
        dos.writeInt(5);
        dos.writeInt(2);
        dos.writeInt(3);
        dos.writeInt(3);
        dos.writeInt(2);
        dos.writeInt(3);
        dos.writeInt(4);
        dos.writeInt(4);
        dos.writeInt(1);
        dos.writeInt(0);

        dos.flush();
        dos.close();

        return tmpFile;
    }
}