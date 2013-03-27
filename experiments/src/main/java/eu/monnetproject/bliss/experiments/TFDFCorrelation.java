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
import eu.monnetproject.bliss.ParallelBinarizedReader;
import eu.monnetproject.bliss.WordMap;
import eu.monnetproject.math.sparse.SparseIntArray;
import java.io.File;
import java.io.PrintStream;

/**
 *
 * @author John McCrae
 */
public class TFDFCorrelation {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File corpus = opts.roFile("corpus", "The corpus file");
        final File wordMap = opts.roFile("wordMap", "The word map");
        final PrintStream out = opts.outFileOrStdout();
        if (!opts.verify(TFDFCorrelation.class)) {
            return;
        }

        final int W = WordMap.calcW(wordMap);
        final ParallelBinarizedReader pbr = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));
        final int[][] tf = new int[W][2];
        final int[][] df = new int[W][2];
        int i = 0;
        SparseIntArray[] doc;
        while ((doc = pbr.nextFreqPair(W)) != null) {
            if(++i % 1000 == 0) {
                System.err.print(".");
            }
            for (int l = 0; l < 2; l++) {
                for (int w : doc[l].keySet()) {
                    tf[w][l] += doc[l].intValue(w);
                    df[w][l]++;
                }
            }
        }
        System.err.println();
        out.println("TF1,DF1,TF2,DF2");
        for(int w = 0; w < W; w++) {
            out.println(tf[w][0] + "," + df[w][0] + "," + tf[w][1] + "," + df[w][1]);
        }
        out.flush();
        out.close();
    }
}
