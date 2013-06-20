/**
 * *******************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *******************************************************************************
 */
package eu.monnetproject.bliss.betalm;

import eu.monnetproject.bliss.WordMap;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jmccrae
 */
public class CompileModelTest {
    
    public CompileModelTest() {
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

    private static final String NL = System.getProperty("line.separator");
    
    public File corpus() throws Exception {
        final File tmpFile = File.createTempFile("corpus", ".tmp");
        tmpFile.deleteOnExit();
        final DataOutputStream out = new DataOutputStream(new FileOutputStream(tmpFile));
        out.writeInt(1);
        out.writeInt(2);
        out.writeInt(3);
        out.writeInt(0);
        out.writeInt(1);
        out.writeInt(0);
        out.writeInt(1);
        out.writeInt(2);
        out.writeInt(4);
        out.writeInt(0);
        out.writeInt(0);
        out.flush();
        out.close();
        return tmpFile;
    }
    
    public File wordMap() throws Exception {
        final WordMap wordMap = new WordMap();
        final File tmpFile = File.createTempFile("wordMap", ".tmp");
        tmpFile.deleteOnExit();
        wordMap.offer("A");
        wordMap.offer("B");
        wordMap.offer("C");
        wordMap.offer("D");
        wordMap.write(tmpFile);
        return tmpFile;
    }
    
    private final String[] expLM = {
        "",
        "\\data\\",
        "ngram 1=4",
        "ngram 2=3",
        "",
        "\\1-grams:",
        "-0.47712125471966244\tA",
        "-0.47712125471966244\tB",
        "-0.7781512503836436\tC",
        "-0.7781512503836436\tD",
        "",
        "\\2-grams:",
       // "0.0\tA B",
        "-0.3010299956639812\tB C",
        "-0.3010299956639812\tB D",
        "",
        "\\end\\"
    };
    
    /**
     * Test of main method, of class CompileModel.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        final File tmpFile = File.createTempFile("lmlm", ".tmp");
        tmpFile.deleteOnExit();
        final File tmpFreqFile = File.createTempFile("freq", ".tmp");
        tmpFreqFile.deleteOnExit();
        final DataOutputStream out = new DataOutputStream(new FileOutputStream(tmpFreqFile));
        out.writeInt(0);
        out.flush();
        out.close();
        String[] args = {
            "-stop",
            "0",
            corpus().getPath(),
            "2",
            wordMap().getPath(),
            tmpFreqFile.getPath(),
            tmpFile.getPath()
        };
        CompileModel.main(args);
        
        int i = 0;
        final Scanner in = new Scanner(tmpFile);
        while(in.hasNextLine()) {
            final String line = in.nextLine();
            Assert.assertEquals(expLM[i++],line);
        }
    }

    
}
