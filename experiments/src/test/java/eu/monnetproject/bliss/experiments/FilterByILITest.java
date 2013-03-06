package eu.monnetproject.bliss.experiments;

import eu.monnetproject.bliss.experiments.FilterByILI;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
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
public class FilterByILITest {

    public FilterByILITest() {
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
     * Test of buildILI method, of class FilterByILI.
     */
    @Test
    public void testBuildILI() throws Exception {
        System.out.println("buildILI");
        File iliFile = new File("src/test/resources/en-es.ili");
        HashMap<String, String> expResult = new HashMap<String, String>();
        expResult.put("Anarchism", "Anarquismo");
        expResult.put("Autism", "Autismo");
        expResult.put("Albedo", "Albedo");
        expResult.put("A", "A");
        expResult.put("Alabama", "Alabama");
        expResult.put("Achilles", "Aquiles");
        expResult.put("Abraham Lincoln", "Abraham Lincoln");
        expResult.put("Aristotle", "Aristóteles");
        expResult.put("An American in Paris", "Un americano en París (Gershwin)");
        expResult.put("Academy Award for Best Art Direction", "Anexo:Óscar a la mejor dirección de arte");
        expResult.put("Academy Award", "Premios Óscar");
        expResult.put("Actrius", "Actrices");
        expResult.put("International Atomic Time", "Tiempo Atómico Internacional");
        expResult.put("Altruism", "Altruismo");
        expResult.put("Ayn Rand", "Ayn Rand");
        expResult.put("Alain Connes", "Alain Connes");
        expResult.put("Allan Dwan", "Allan Dwan");
        expResult.put("Algeria", "Argelia");
        expResult.put("Anthropology", "Antropología");
        expResult.put("Agricultural science", "Agronomía");
        HashMap result = FilterByILI.buildILI(iliFile,false);
        assertEquals(expResult, result);
    }

    /**
     * Test of filter method, of class FilterByILI.
     */
    @Test
    public void testFilter() throws Exception {
        System.out.println("filter");
        final String LS = System.getProperty("line.separator");
        File corpusFile = File.createTempFile("corpus", ".int");
        corpusFile.deleteOnExit();
        final PrintWriter tout = new PrintWriter(corpusFile);
        tout.println("Altruism: 1 2 3 4 ");
        tout.println("Avarice: 3 4 5 6 ");
        tout.println("Anthropology: 10 12 13 14 ");
        tout.close();
        HashMap<String, String> ili = FilterByILI.buildILI(new File("src/test/resources/en-es.ili"),false);
        final StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        FilterByILI.filter(corpusFile, ili, out,true);
        String expResult = "Altruismo: 1 2 3 4 " + LS +
            "Antropología: 10 12 13 14 " + LS;
        assertEquals(expResult, sw.toString());
    }
}