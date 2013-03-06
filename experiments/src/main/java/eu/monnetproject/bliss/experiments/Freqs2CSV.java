/*********************************************************************************
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
import eu.monnetproject.bliss.WordMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.PrintStream;

/**
 *
 * @author John McCrae
 */
public class Freqs2CSV {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File wordMapFile = opts.roFile("wordMap", "The word map");
        final File freqFile = opts.roFile("freqs", "The frequency file");
        final PrintStream out = opts.outFileOrStdout();
        if(!opts.verify(Freqs2CSV.class)) {
            return;
        }
        System.err.println("CalcW");
        final int W = WordMap.calcW(wordMapFile);
        System.err.println("W="+W);
        final String[] wordMap = WordMap.inverseFromFile(wordMapFile, W, true);
        System.err.println("Read inv word map");
        final DataInputStream dataIn = new DataInputStream(CLIOpts.openInputAsMaybeZipped(freqFile));
        out.println("Word,Freq");
        dataIn.readInt(); // 0 is end of record
        int idx = 1;
        while(dataIn.available() > 0) {
            try {
                final int freq = dataIn.readInt();
                final String word = wordMap[idx++];
                out.println(word.replaceAll(",", "") + "," + freq);
            } catch(EOFException x) {
                break;
            }
        }
        out.flush();
        out.close();
    }
}
