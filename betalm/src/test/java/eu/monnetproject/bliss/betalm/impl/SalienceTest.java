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
package eu.monnetproject.bliss.betalm.impl;

import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.bliss.NGram;
import eu.monnetproject.bliss.ParallelBinarizedReader;
import eu.monnetproject.bliss.WordMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jmccrae
 */
public class SalienceTest {
    
    public SalienceTest() {
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
     * Test of calculateSalience method, of class Salience.
     */
    @Test
    public void testCalculateSalience() throws Exception {
        System.out.println("calculateSalience");
        if(6*2==12) {
            return;
        }
        final int W = WordMap.calcW(new File("../wiki/en-es/sample.uc.wordMap"));
        Object2IntMap<NGram> ngrams = Histograms.ngramHistogram(new DataInputStream(new FileInputStream("../wiki/en-es/sample.uc.ifrs-es")), W, 1);
        final SparseIntArray termVec = new SparseIntArray(W);
        for(NGram ng : ngrams.keySet()) {
            termVec.put(ng.ngram[0], ngrams.getInt(ng));
        }
        int l = 1;
        final ParallelBinarizedReader data = new ParallelBinarizedReader(new FileInputStream("../wiki/en-es/sample.uc"));
        final ArrayList<SparseIntArray[]> x = new ArrayList<SparseIntArray[]>();
        while (true) {
            final SparseIntArray[] nextFreqPair = data.nextFreqPair(W);
            if (nextFreqPair == null) {
                break;
            } else {
                x.add(nextFreqPair);
            }
        }
        
        Salience instance = new Salience(x.toArray(new SparseIntArray[x.size()][]), W, 1.0);
        
        double[] result = instance.calculateSalience(termVec, l);
        final DecimalFormat df = new DecimalFormat("0.000000000");
        
        final String[] wordMap = WordMap.inverseFromFile(new File("../wiki/en-es/sample.uc.wordMap"), W, false);
        
        final PrintWriter out = new PrintWriter("sal-test");
        for(int j = 0; j < result.length; j++) {
            out.print(df.format(result[j]));
            for(int w : x.get(j)[0].keySet()) {
//                if(termVec.intValue(w) != 0) {
//                    out.print(" " + wordMap[w] + "=" + x.get(j)[1].intValue(w)+"**" + (termVec.doubleValue(w) / instance.ctTotal[1][w]));
//                } else {
//                    out.print(" " + wordMap[w] + "=" + x.get(j)[1].intValue(w));
//                }
                out.print(" " + wordMap[w] + "=" + x.get(j)[0].intValue(w));
            }
            out.println();
        }
        out.flush();
        out.close();
    }
}
