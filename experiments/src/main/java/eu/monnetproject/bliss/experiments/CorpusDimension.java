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
package eu.monnetproject.bliss.experiments;

import eu.monnetproject.bliss.CLIOpts;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import static java.lang.Math.*;

/**
 *
 * @author John McCrae
 */
public class CorpusDimension {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File corpusFile = opts.roFile("corpus", "The corpus");
        if(!opts.verify(CorpusDimension.class)) {
            return;
        }
        final DataInputStream dataIn = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpusFile));
        int W = 0, J = 0, n = 0;
        try {
            DATA_LOOP:
            while (dataIn.available() > 0) {
                if (++n % 100000 == 0) {
                    System.err.print(".");
                }
                try {
                    int i = dataIn.readInt();
                    if (i != 0) {
                        W = max(W,i);
                    } else {
                        J++;
                        n--;
                    }
                } catch (EOFException x) {
                    break;
                }
            }
        } finally {
            dataIn.close();
        }
        W++;
        System.err.println();
        System.out.println("W (Number of distinct tokens)  ="+W);
        System.out.println("J (Number of documents)        ="+J);
        System.out.println("n (Number of tokens)           ="+n);
    }
}
