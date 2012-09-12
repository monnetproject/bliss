/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics.experiments;

import eu.monnetproject.lang.Language;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
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
public class InterlingualIndexTest {

    public InterlingualIndexTest() {
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
     * Test of buildILI method, of class InterlingualIndex.
     */
    @Test
    public void testBuildILI() throws Exception {
        System.out.println("buildILI");
        final String LS = System.getProperty("line.separator");
        File wikiFile = new File("src/test/resources/enwiki.10000.txt");
        final StringWriter sw = new StringWriter();
        Language targetLanguage = Language.SPANISH;
        PrintWriter out = new PrintWriter(sw);
        InterlingualIndex.buildILI(wikiFile, targetLanguage, out);
        final String expResult = "Anarchism\tAnarquismo" + LS
                + "Autism\tAutismo" + LS
                + "Albedo\tAlbedo" + LS
                + "A\tA" + LS
                + "Alabama\tAlabama" + LS
                + "Achilles\tAquiles" + LS
                + "Abraham Lincoln\tAbraham Lincoln" + LS
                + "Aristotle\tAristóteles" + LS
                + "An American in Paris\tUn americano en París (Gershwin)" + LS
                + "Academy Award for Best Art Direction\tAnexo:Óscar a la mejor dirección de arte" + LS
                + "Academy Award\tPremios Óscar" + LS
                + "Actrius\tActrices" + LS
                + "International Atomic Time\tTiempo Atómico Internacional" + LS
                + "Altruism\tAltruismo" + LS
                + "Ayn Rand\tAyn Rand" + LS
                + "Alain Connes\tAlain Connes" + LS
                + "Allan Dwan\tAllan Dwan" + LS
                + "Algeria\tArgelia" + LS
                + "Anthropology\tAntropología" + LS
                + "Agricultural science\tAgronomía" + LS;
        assertEquals(expResult, sw.toString());
    }
}
