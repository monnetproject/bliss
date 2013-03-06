package eu.monnetproject.bliss.lda;

import eu.monnetproject.bliss.lda.CPLSATrain;
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
 * @author john
 */
public class CPLSATest {

    public CPLSATest() {
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
     * Test of initialize method, of class CPLSA.
     */
    @Test
    public void testInitialize() throws Exception {
        System.out.println("initialize");
        final int K = 2;
        final int J = 2;
        final int W = 10;
        final int L = 2;
        final int[][][] x = {
            {
                {0, 2, 1, 0, 1},
                {6, 7, 7, 5, 6}
            },
            {
                {3, 3, 4, 4, 3},
                {8, 9, 9, 9, 8}
            }
        };
        final File corpus = File.createTempFile("cplsa", "corpus");
        corpus.deleteOnExit();
        final DataOutputStream out = new DataOutputStream(new FileOutputStream(corpus));
        for(int j = 0; j < J; j++) {
            for(int l = 0; l < L; l++) {
                for(int n = 0; n < x[l][j].length; n++) {
                    out.writeInt(x[l][j][n]+1);
                }
                out.writeInt(0);
            }
        }
        out.flush();
        out.close();
        
        CPLSATrain instance = new CPLSATrain(corpus,J, W, K, 0.0, 0.0);
        double[][][] expTheta_lkj = {
            {
                {0.8, 0.0},
                {0.2, 1.0}
            },
            {
                {0.6, 0.6},
                {0.4, 0.4}
            }
        };
        double[][][] expPhi_lwk = {
            {
                {0.5, 0.0},
                {0.5, 0.0},
                {0.0, 1.0/6.0},
                {0.0, 0.0},
                {0.0, 0.0},
                {0.0, 1.0/6.0},
                {0.0, 2.0/6.0},
                {0.0, 2.0/6.0},
                {0.0, 0.0},
                {0.0, 0.0}
            },
            {
                {0.0, 0.0},
                {0.0, 0.0},
                {0.0, 0.0},
                {0.5, 0.0},
                {0.0, 0.5},
                {0.0, 0.0},
                {0.0, 0.0},
                {0.0, 0.0},
                {0.0, 0.5},
                {0.5, 0.0}
            }
        };

        instance.initialize();

        for (int l = 0; l < L; l++) {
            for (int k = 0; k < K; k++) {
                assertArrayEquals(expTheta_lkj[l][k], instance.theta_lkj[l][k], 0.0);
            }
            for (int w = 0; w < W; w++) {
                assertArrayEquals(expPhi_lwk[l][w], instance.phi_lwk[l][w], 0.0);
            }
        }

    }

    /**
     * Test of solve method, of class CPLSA.
     */
    @Test
    public void testSolve() throws Exception {
        System.out.println("solve");
        final int K = 2;
        final int J = 2;
        final int W = 10;
        final int L = 2;
        final int[][][] x = {
            {
                {0, 2, 1, 0, 1},
                {6, 7, 7, 5, 6}
            },
            {
                {3, 3, 4, 4, 3},
                {8, 9, 9, 9, 8}
            }
        };final File corpus = File.createTempFile("cplsa", "corpus");
        corpus.deleteOnExit();
        final DataOutputStream out = new DataOutputStream(new FileOutputStream(corpus));
        for(int j = 0; j < J; j++) {
            for(int l = 0; l < L; l++) {
                for(int n = 0; n < x[l][j].length; n++) {
                    out.writeInt(x[l][j][n]+1);
                }
                out.writeInt(0);
            }
        }
        out.flush();
        out.close();
        
        CPLSATrain instance = new CPLSATrain(corpus,J, W, K, 0.0, 0.0);
        int iterations = 1;
        double epsilon = 0.1;
        instance.solve(iterations, epsilon);
        if (instance.theta_lkj[0][0][0] > instance.theta_lkj[0][1][0]) {
            assert (instance.theta_lkj[1][0][0] > instance.theta_lkj[1][1][0]);
            assert (instance.theta_lkj[0][0][1] < instance.theta_lkj[0][1][1]);
        } else {
            assert (instance.theta_lkj[1][0][0] < instance.theta_lkj[1][1][0]);
            assert (instance.theta_lkj[0][0][1] > instance.theta_lkj[0][1][1]);
        }
    }
}