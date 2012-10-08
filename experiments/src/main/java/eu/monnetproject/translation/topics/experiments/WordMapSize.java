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
import java.io.File;
import java.io.FileInputStream;

/**
 *
 * @author John McCrae
 */
public class WordMapSize {
    
    
    private static void fail(String message) {
        System.err.println(message);
        System.err.println("\nUsage:\n"
                + "\tmvn exec:java -Dexec.mainClass=\""+WordMapSize.class.getName()+"\" -Dexec.args=\"wordMap\"");
        System.exit(-1);
    }

    public static void main(String[] args) throws Exception {
        if(args.length != 1) {
            fail("Wrong number of arguments");
        }
        final File wordMapFile = new File(args[0]);
        if(!wordMapFile.exists() || !wordMapFile.canRead()) {
            fail("Cannot read wordMap");
        }
        
        int W = 0;
        int n = 0;
        final DataInputStream data = new DataInputStream(new FileInputStream(wordMapFile));
        while(data.available() > 0) {
            data.readUTF();
            int w = data.readInt();
            if(w > W) {
                W = w;
            }
            if(++n % 10000 == 0) {
                System.err.print(".");
            }
        }
        data.close();
        System.err.println();
        System.out.println("W=" + (W+1));
    }
}

 