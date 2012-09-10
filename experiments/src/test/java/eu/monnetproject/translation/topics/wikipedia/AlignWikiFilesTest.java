/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.translation.topics.wikipedia;

import java.io.File;
import java.util.Scanner;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author jmccrae
 */
public class AlignWikiFilesTest {
    
    public AlignWikiFilesTest() {
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
     * Test of align method, of class AlignWikiFiles.
     */
    @Test
    public void testAlign() throws Exception {
        System.out.println("align");
        File inFile1 = new File("src/test/resources/alignFile1.txt");
        File inFile2 = new File("src/test/resources/alignFile2.txt");
        File outFile1 = File.createTempFile("alignOut", ".txt");
        File outFile2 = File.createTempFile("alignOut", ".txt");
        outFile1.deleteOnExit();
        outFile2.deleteOnExit();
        AlignWikiFiles.align(inFile1, inFile2, outFile1, outFile2);
        final String file1contents = new Scanner(outFile1).useDelimiter("\\Z").next();
        final String N = System.getProperty("line.separator");
        Assert.assertEquals(" blah blah" +  N + " blah blah" + N + " blah blah" + N + " blah blah" + N + " blah blah" + N + " blah blah",file1contents);
    }

}
