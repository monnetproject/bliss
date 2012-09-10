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
package eu.monnetproject.translation.topics.wikipedia;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;

/**
 *
 * @author John McCrae
 */
public class TextToBinary {
    public static final int BUF_SIZE = 0x7f0000;

    public static void textToBinary(File textFile, File freqFile, File binaryFile) throws IOException {
        final HashMap<String, Integer> freqMap = new HashMap<String, Integer>();
        final BufferedReader freqIn = new BufferedReader(new FileReader(freqFile));
        String s;
        int i = 0;
        System.err.println("Reading frequency map");
        while ((s = freqIn.readLine()) != null) {
            final String[] ss = s.split(" ");
            if (ss.length <= 1) {
                continue;
            } else if (ss.length != 3) {
                throw new RuntimeException("Bad line: " + s);
            }
            freqMap.put(ss[0], new Integer(ss[1]));
            if (++i % 1000 == 0) {
                System.err.print(".");
            }
        }
        System.err.println();
        i = 0;
        freqIn.close();
        System.err.println("Binary mapping");
        final DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(binaryFile));
        final BufferedReader textIn = new BufferedReader(new FileReader(textFile));
        final IntBuffer buffer = IntBuffer.allocate(BUF_SIZE);
        int loc = 0;
        while ((s = textIn.readLine()) != null) {
            boolean nonEmptyLine = false;
            final String[] tokens = s.split("\\b");
            for (String token : tokens) {
                if (token.matches(".*\\w.*")) {
                    final String cleanTk = token.replaceAll("\\s", "").toLowerCase();
                    final Integer tkIntVal = freqMap.get(cleanTk);
                    if (tkIntVal != null) {
                        nonEmptyLine = true;
                        //dataOut.writeInt(tkIntVal.intValue());
                        loc = writeInt(buffer, loc, tkIntVal.intValue(), dataOut);
                    }
                }
            }
            if (++i % 1000 == 0) {
                System.err.print(".");
            }
            if (nonEmptyLine) {
                //dataOut.writeInt(0);
                loc = writeInt(buffer, loc, 0, dataOut);
            }
        }
        for (int j = 0; j < loc; j++) {
            dataOut.writeInt(buffer.array()[j]);
        }
        dataOut.close();
        textIn.close();
    }

    private static int writeInt(IntBuffer buffer, int loc, Integer value, DataOutputStream out) throws IOException {
        if (loc < BUF_SIZE) {
            buffer.put(loc, value.intValue());
            return loc + 1;
        } else {
            for (int i = 0; i < BUF_SIZE; i++) {
                out.writeInt(buffer.array()[i]);
            }
            return 0;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("Usage: textFile freqFile binaryFile");
        }
        textToBinary(new File(args[0]), new File(args[1]), new File(args[2]));
    }
}
