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
import eu.monnetproject.bliss.PTBTokenizer;
import eu.monnetproject.bliss.Tokenizer;
import eu.monnetproject.bliss.WordMap;
import java.io.DataOutputStream;
import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author John McCrae
 */
public class IntegerizeAcquis {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File corpus = opts.roFile("corpus[.gz|.bz2]", "The corpus in Acquis format");
        final File wordMapFile = opts.woFile("wordMap", "The file to write the word map to");
        final File outFile = opts.woFile("corpusOut[.gz|.bz2]", "The file to write the integerized corpus to");
        if(!opts.verify(IntegerizeAcquis.class)) {
            return;
        }
        final WordMap wordMap = new WordMap();
        final Tokenizer tokenizer = new PTBTokenizer();
        final Scanner in = new Scanner(CLIOpts.openInputAsMaybeZipped(corpus));
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(outFile));
        int read = 0;
        while(in.hasNextLine()) {
            if(++read % 100000 == 0) {
                System.err.print(".");
            }
            final String line = in.nextLine();
            if(line.contains("<s1>") || line.contains("<s2>")) {
                final String lineWithoutXml = line.replaceAll("<[^>]+>", "");
                final List<String> tokens = tokenizer.tokenize(lineWithoutXml);
                for(String token : tokens) {
                    final int w = wordMap.offer(token);
                    out.writeInt(w);
                }
                out.writeInt(0);
            }
        }
        System.err.println();
        out.flush();
        out.close();
        wordMap.write(wordMapFile);
    }
}
