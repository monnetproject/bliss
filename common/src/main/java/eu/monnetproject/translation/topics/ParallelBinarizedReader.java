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
package eu.monnetproject.translation.topics;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Read a parallel binary corpus file. Very much not thread-safe!
 *
 *
 * @author John McCrae
 */
public final class ParallelBinarizedReader {

    private final DataInputStream in;

    public ParallelBinarizedReader(InputStream in) {
        this.in = new DataInputStream(in);
    }
    private int[] buf = new int[1048576];

    public int[][] nextPair() throws IOException {
        if (in.available() == 0) {
            return null;
        }
        boolean inl1 = true;
        int[] l1 = null;
        int loc = 0;
        while (in.available() > 0) {
            try {
                final int i = in.readInt();
                if (i == 0) {
                    if (inl1) {
                        inl1 = false;
                        l1 = Arrays.copyOfRange(buf, 0, loc);
                        loc = 0;
                    } else {
                        return new int[][]{
                                    l1,
                                    Arrays.copyOfRange(buf, 0, loc)
                                };
                    }
                } else {
                    buf[loc++] = i;
                }
            } catch (EOFException x) {
                break;
            }
        }
        if (l1 == null) {
            System.err.println("Corpus did not balance... odd number of documents?");
            return null;
        } else {
            return new int[][]{
                        l1,
                        Arrays.copyOfRange(buf, 0, loc)
                    };
        }
    }

    public SparseArray[] nextFreqPair(int W) throws IOException {
        final int[][] pair = nextPair();
        if (pair == null) {
            return null;
        } else {
            return new SparseArray[]{
                        SparseArray.histogram(pair[0], W),
                        SparseArray.histogram(pair[1], W)
                    };
        }
    }

    public SparseArray[][] readAll(int W) throws IOException {
        final ArrayList<SparseArray[]> sparseArrays = new ArrayList<SparseArray[]>();
        SparseArray[] sa;
        while((sa = nextFreqPair(W)) != null) {
            sparseArrays.add(sa);
        }
        return sparseArrays.toArray(new SparseArray[sparseArrays.size()][]);
    }
}
