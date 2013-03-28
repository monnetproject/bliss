/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.bliss.experiments;

import eu.monnetproject.bliss.WordMap;
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
 * @author jmccrae
 */
public class PerceptronNormalizationTest {
    
    public PerceptronNormalizationTest() {
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
     * Test of main method, of class PerceptronNormalization.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        final File corpusTmpFile = File.createTempFile("corpus", ".int");
        corpusTmpFile.deleteOnExit();
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(corpusTmpFile));
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(2);
        dos.writeInt(2);
        dos.writeInt(0);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(2);
        dos.writeInt(2);
        dos.writeInt(0);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(3);
        dos.writeInt(3);
        dos.writeInt(0);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(3);
        dos.writeInt(3);
        dos.writeInt(0);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(4);
        dos.writeInt(4);
        dos.writeInt(0);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(1);
        dos.writeInt(4);
        dos.writeInt(4);
        dos.writeInt(0);
        dos.flush();
        dos.close();
        final WordMap wordMap = new WordMap();
        wordMap.offer("a");
        wordMap.offer("b");
        wordMap.offer("c");
        wordMap.offer("d");
        final File wordMapTmpFile = File.createTempFile("wordMap", "");
        wordMapTmpFile.deleteOnExit();
        wordMap.write(wordMapTmpFile);
        String[] args = {
            corpusTmpFile.getPath(),
            wordMapTmpFile.getPath(),
            "3"
        };
        PerceptronNormalization.main(args);
    }
}
