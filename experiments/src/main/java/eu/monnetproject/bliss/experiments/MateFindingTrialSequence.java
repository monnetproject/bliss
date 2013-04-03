/**
 * ********************************************************************************
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
package eu.monnetproject.bliss.experiments;

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.SimilarityMetricFactory;
import eu.monnetproject.bliss.WordMap;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class MateFindingTrialSequence {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);

        final int ngram = opts.intValue("ngram", "The number of n-grams to use in n-gram based similarity", 0);

        final File trainFile = opts.roFile("trainFile", "The training file");

        final Class<SimilarityMetricFactory> factoryClazz = opts.clazz("metricFactory", SimilarityMetricFactory.class, "The factory for the cross-lingual similarity measure", MateFindingTrial.metricNames);

        final File wordMapFile = opts.roFile("wordMap", "The final containing the word map");

        final File testFile = opts.roFile("testFile", "The test file");

        opts.restAsSystemProperties();

        if (!opts.verify(MateFindingTrialSequence.class)) {
            return;
        }

        final int W = WordMap.calcW(wordMapFile);
        final PrintWriter out = new PrintWriter("mate-finding-sequence.csv");
        
        out.println("D,P1,P5,P10,MRR");
        
        for(double d = 1.6; d < 3; d += 0.2) {
            System.setProperty("clesaPower", d+"");
            final double[] scores = MateFindingTrial.compare(trainFile, factoryClazz, W, testFile, ngram,false);
            out.println(d+","+scores[0]+","+scores[1]+","+scores[2]+","+scores[3]);
            System.out.println("d="+d);
            System.out.println("scores="+Arrays.toString(scores));
        }
        out.flush();
        out.close();
    }
}
