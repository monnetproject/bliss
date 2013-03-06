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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author John McCrae
 */
public class Downsample {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        
        final File corpusFile = opts.roFile("corpus[.gz|.bz2]", "The corpus");
        
        final int downsample = opts.intValue("downsample", "Choose only every Xth document pair");
        
        final File outFile = opts.woFile("out[.gz|.bz2]", "The file to write data to");
        
        if(!opts.verify(FilterAndSplitCorpus.class)) {
            return;
        }
        
        final DataInputStream corpusIn = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpusFile));
        
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(outFile));
        
        downsample(corpusIn, out, 
                downsample*2/* two languages*/);
        
        out.flush();
        out.close();
    }

    private static void downsample(DataInputStream corpusIn, DataOutputStream out, int downsample) throws IOException {
        int n = 0;
        int r = 0;
        while(corpusIn.available() > 0) {
            try {
                final int i = corpusIn.readInt();
                if(i == 0) {
                    if(n % downsample <= 1) {
                        out.writeInt(0);
                    }
                    n++;
                } else {
                    if(n % downsample <= 1) {
                        out.writeInt(i);
                    }
                }
                if(++r % 100000 == 0) {
                    System.err.print(".");
                }
            } catch(EOFException x) {
                break;
            }
        }
        System.err.println();
    }
}
