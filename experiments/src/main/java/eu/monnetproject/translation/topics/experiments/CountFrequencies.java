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
package eu.monnetproject.translation.topics.experiments;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author John McCrae
 */
public class CountFrequencies {

    private static void fail(String message) {
        System.err.println(message);
        System.err.println("\nUsage:\n"
                + "\tmvn exec:java -Dexec.mainClass=\""+CountFrequencies.class.getName()+"\" -Dexec.args=\"corpus W\"");
        System.exit(-1);
    }
    
    public static void main(String[] args) throws Exception {
        if(args.length != 3) {
            fail("Wrong number of arguments");
        }
        final File corpusFile = new File(args[0]);
        
        if(!corpusFile.exists() || !corpusFile.canRead()) {
            fail("Cannot access corpusFile");
        }
        
        final int W;
        try {
            W = Integer.parseInt(args[1]);
        } catch(NumberFormatException x) {
            fail("Not an integer " + args[1]);
            return;
        }
        
        final File outFile = new File(args[2]);
        
        if(outFile.exists() && !outFile.canWrite()) {
            fail("Cannot access outFile");
        }
        
        final DataInputStream dataIn = new DataInputStream(new FileInputStream(corpusFile));
        final int[] freqs = new int[W];
        final DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(outFile));
        
        calcFreqs(dataIn, freqs);
        
        printFreqs(freqs, dataOut);
    }

    private static void calcFreqs(DataInputStream dataIn, int[] freqs) throws IOException {
        int n = 0;
        try {
            while(dataIn.available() > 0) {
                int i = dataIn.readInt();
                if(i != 0) {
                    freqs[i]++;
                }
                if(++n % 100000 == 0) {
                    System.err.print(".");
                }
            }
        } finally {
            dataIn.close();
        }
    }

    private static void printFreqs(int[] freqs, DataOutputStream dataOut) throws IOException {
        try {
            for(int i = 1; i < freqs.length; i++) {
                dataOut.writeInt(freqs[i]);
            }
        } finally {
            dataOut.close();
        }
    }
}
