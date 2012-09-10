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
package eu.monnetproject.translation.topics.sim;

import eu.monnetproject.translation.topics.SparseArray;
import java.io.*;
import java.util.HashMap;

/**
 *
 * @author John McCrae
 */
public final class BinaryReader {

    public static final int BUF_SIZE = 262144;

    private BinaryReader() {
    }

    public static SparseArray[] readFromFile(File file, int length, int arrMax) throws IOException {
        final DataInputStream in = new DataInputStream(new FileInputStream(file));
        System.err.println(file.getPath());
        final SparseArray[] elems = new SparseArray[length];
        final int[] buffer = new int[BUF_SIZE];
        int loc = 0;
        int elemCt = 0;
        while (in.available() > 0) {
            final int i = in.readInt();
            if (i != 0) {
                if (loc < BUF_SIZE) {
                    buffer[loc++] = i;
                } else if (loc == BUF_SIZE) {
                    System.err.println("Document " + elemCt + " curtailed");
                    loc++;
                }
            } else {
                final SparseArray arr = new SparseArray(arrMax);
                elems[elemCt] = arr;
                for (int j = 0; j < loc; j++) {
                    arr.inc(buffer[j]);
                }
                loc = 0;
                elemCt++;
                if(elemCt % 1000 == 0) {
                    System.err.print(".");
                }
            }
        }
        System.err.println();
        return elems;
    }
    
    public static SparseArray[][] read2FromFile(File file1, File file2, int length, int arrMax) throws IOException {
        final SparseArray[] sa1 = readFromFile(file1, length, arrMax);
        final SparseArray[] sa2 = readFromFile(file2, length, arrMax);
        final SparseArray[][] sa = new SparseArray[length][];
        for(int i = 0; i < length; i++) {
            sa[i] = new SparseArray[] { sa1[i], sa2[i] };
        }
        return sa;
    }
    
    public static HashMap<String,Integer> readWords(File freqs) throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(freqs));
        String s;
        int i = 1;
        final HashMap<String, Integer> map = new HashMap<String, Integer>();
        while((s = in.readLine()) != null) {
            if(s.indexOf(" ") >= 0) {
                map.put(s.substring(0, s.indexOf(" ")), i++);
            }
        }
        return map;
    }
}
