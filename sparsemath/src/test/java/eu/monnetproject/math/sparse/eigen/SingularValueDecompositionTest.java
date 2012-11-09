package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.DataStreamIterable;
import eu.monnetproject.math.sparse.DoubleArrayMatrix;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition.Solution;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
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
        System.setProperty("SEED", "5");
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
        double epsilon = 1e-50;
        Solution expResult = new Solution(new double[][]{
                    {-0.4711454, -0.5780836, -0.4914296, -0.4498203},
                    {-0.05076119, -0.47792513, -0.17556759, 0.85917803},
                    {-0.79897599, 0.06631807, 0.58753338, 0.10974433},
                    {-0.3702467, 0.6580340, -0.6184477, 0.2177866}
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
        Solution result = SingularValueDecomposition.calculate(new DataStreamIterable(matrixFile), W, J, K, epsilon);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(Math.abs(expResult.U[i][j]), Math.abs(result.U[i][j]), 0.02);
                assertEquals(Math.abs(expResult.V[i][j]), Math.abs(result.V[i][j]), 0.02);
            }
        }
        assertArrayEquals(expResult.S, result.S, 0.2);
    }

    @Test
    public void testCalculateSymmetric() throws Exception {
        System.out.println("calculateSymmetric");
        File matrixFile = writeTempDoc2();
        int W = 10;
        int J = 10;
        int K = 10;
        double epsilon = 1e-20;
        final Random r = new Random(J);
//        SingularValueDecomposition instance = new SingularValueDecomposition() {
//            @Override
//            protected Vector<Double> randomUnitNormVector(int J) {
//
//                final double[] rv = new double[J];
//                double norm = 0.0;
//                for (int j = 0; j < J; j++) {
//                    rv[j] = r.nextDouble();
//                    norm += rv[j] * rv[j];
//                }
//
//                norm = Math.sqrt(norm);
//
//                for (int j = 0; j < J; j++) {
//                    rv[j] /= norm;
//                }
//
//                return new RealVector(rv);
//            }
//        };
        Solution result = SingularValueDecomposition.calculateSymmetric(new DataStreamIterable(matrixFile), W, J, K, epsilon);
    }

    public double[] positive(double[] exp, double[] act) {
        final double sgn = Math.signum(exp[0] * act[0]);
        if (sgn == 0) {
            assert (false);
        }
        for (int i = 0; i < act.length; i++) {
            act[i] *= sgn;
        }
        return act;
    }

    @Test
    public void testInnerProduct() throws Exception {
        System.out.println("innerProduct");
        File matrixFile = writeTempDoc();
        final VectorFunction<Double, Double> innerProduct = new SingularValueDecomposition.InnerProductMultiplication(new DataStreamIterable(matrixFile), 4);
        final RealVector v = new RealVector(new double[]{1, 2, 3, 4, 5});
        final Vector<Double> apply = innerProduct.apply(v);
        assertEquals(new RealVector(new double[]{283, 160, 242, 250, 324}), apply);
    }

    @Test
    public void testOuterProduct() throws Exception {
        System.out.println("outerProduct");
        File matrixFile = writeTempDoc();
        final VectorFunction<Double, Double> outerProduct = new SingularValueDecomposition.OuterProductMultiplication(new DataStreamIterable(matrixFile), 5);
        final RealVector v = new RealVector(new double[]{1, 2, 3, 4});
        final Vector<Double> apply = outerProduct.apply(v);
        assertEquals(new RealVector(new double[]{189, 228, 202, 206}), apply);
    }

    private File writeTempDoc() throws IOException {
        final File tmpFile = File.createTempFile("matrix", ".bin");
        tmpFile.deleteOnExit();
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(tmpFile));
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(3);
        dos.writeInt(3);
        dos.writeInt(4);
        dos.writeInt(4);
        dos.writeInt(5);
        dos.writeInt(5);
        dos.writeInt(0);

        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(2);
        dos.writeInt(3);
        dos.writeInt(4);
        dos.writeInt(4);
        dos.writeInt(5);
        dos.writeInt(5);
        dos.writeInt(5);
        dos.writeInt(5);
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

    private File writeTempDoc2() throws IOException {
        final File tmpFile = File.createTempFile("matrix", ".bin");
        tmpFile.deleteOnExit();
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(tmpFile));
        // A <- t( matrix(c(1,1,0,0,1,1,4,0,0,2, 1,1,1,1,0,2,1,1,0,2, 1,0,3,2,2,0,0,1,0,1, 0,1,1,0,2,1,2,2,0,1, 
        //                  1,0,1,0,2,1,2,2,1,0, 1,0,5,0,0,1,0,2,0,1, 0,2,0,2,1,2,0,1,1,1, 1,1,0,3,0,3,0,0,0,2,
        //                  2,1,1,1,0,0,1,2,1,1, 1,1,1,1,2,3,0,1,0,0),10))
        dos.writeInt(6);
        dos.writeInt(10);
        dos.writeInt(7);
        dos.writeInt(7);
        dos.writeInt(2);
        dos.writeInt(7);
        dos.writeInt(10);
        dos.writeInt(7);
        dos.writeInt(1);
        dos.writeInt(5);
        dos.writeInt(0);

        dos.writeInt(1);
        dos.writeInt(7);
        dos.writeInt(10);
        dos.writeInt(2);
        dos.writeInt(6);
        dos.writeInt(10);
        dos.writeInt(3);
        dos.writeInt(8);
        dos.writeInt(6);
        dos.writeInt(4);
        dos.writeInt(0);

        dos.writeInt(10);
        dos.writeInt(1);
        dos.writeInt(8);
        dos.writeInt(5);
        dos.writeInt(4);
        dos.writeInt(5);
        dos.writeInt(3);
        dos.writeInt(4);
        dos.writeInt(3);
        dos.writeInt(3);
        dos.writeInt(0);

        dos.writeInt(7);
        dos.writeInt(10);
        dos.writeInt(3);
        dos.writeInt(2);
        dos.writeInt(7);
        dos.writeInt(5);
        dos.writeInt(6);
        dos.writeInt(5);
        dos.writeInt(8);
        dos.writeInt(8);
        dos.writeInt(0);

        dos.writeInt(6);
        dos.writeInt(7);
        dos.writeInt(5);
        dos.writeInt(5);
        dos.writeInt(7);
        dos.writeInt(3);
        dos.writeInt(8);
        dos.writeInt(1);
        dos.writeInt(8);
        dos.writeInt(9);
        dos.writeInt(0);


        dos.writeInt(3);
        dos.writeInt(3);
        dos.writeInt(8);
        dos.writeInt(1);
        dos.writeInt(6);
        dos.writeInt(3);
        dos.writeInt(10);
        dos.writeInt(8);
        dos.writeInt(3);
        dos.writeInt(3);
        dos.writeInt(0);

        dos.writeInt(2);
        dos.writeInt(2);
        dos.writeInt(6);
        dos.writeInt(8);
        dos.writeInt(4);
        dos.writeInt(4);
        dos.writeInt(10);
        dos.writeInt(9);
        dos.writeInt(5);
        dos.writeInt(6);
        dos.writeInt(0);

        dos.writeInt(4);
        dos.writeInt(4);
        dos.writeInt(6);
        dos.writeInt(4);
        dos.writeInt(6);
        dos.writeInt(1);
        dos.writeInt(10);
        dos.writeInt(10);
        dos.writeInt(2);
        dos.writeInt(6);
        dos.writeInt(0);


        dos.writeInt(8);
        dos.writeInt(10);
        dos.writeInt(1);
        dos.writeInt(7);
        dos.writeInt(9);
        dos.writeInt(4);
        dos.writeInt(1);
        dos.writeInt(2);
        dos.writeInt(8);
        dos.writeInt(3);
        dos.writeInt(0);



        dos.writeInt(2);
        dos.writeInt(4);
        dos.writeInt(6);
        dos.writeInt(5);
        dos.writeInt(6);
        dos.writeInt(3);
        dos.writeInt(8);
        dos.writeInt(6);
        dos.writeInt(5);
        dos.writeInt(1);
        dos.writeInt(0);

        dos.flush();
        dos.close();

        return tmpFile;
    }

    @Test
    public void testEigenSolve() {
        double[][] M = {
            {4.4, 1.8, 0, 0},
            {1.8, 2.6, 0, 0},
            {0, 0, 9, 0},
            {0, 0, 0, 2.4}
        };
        final Solution result = new SingularValueDecomposition().eigen(new DoubleArrayMatrix(M).asVectorFunction(), 4, 4, 1e-50);
        double[][] expResult = {
            {0, 0, 1, 0},
            {0.851, 0.526, 0, 0},
            {0, 0, 0, 1},
            {-0.526, 0.851, 0, 0}
        };
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(Math.abs(expResult[i][j]), Math.abs(result.U[i][j]), 0.1);
            }
        }
    }

    @Test
    public void testNonsymmEigen() {
        System.out.println("nonsymmEigen");
        final DoubleArrayMatrix A = new DoubleArrayMatrix(new double[][]{
                    {1, 2, 1},
                    {6, -1, 0},
                    {-1, -2, -1}
                });
        final Solution soln = SingularValueDecomposition.nonsymmEigen(A.asVectorFunction(), 3, 3, 1e-50);
        final double[][] vectors = new double[][]{
            {0.4082, -0.8165, -0.4082},
            {0.4851, 0.7276, -0.4851},
            {0.0697, 0.4181, -0.9057}
        };
        for (int i = 0; i < 3; i++) {
            final double s = Math.signum(soln.U[i][0]);
            for (int j = 0; j < 3; j++) {
                soln.U[i][j] *= s;
            }
            assertArrayEquals(vectors[i], soln.U[i], 0.01);
        }
    }
}