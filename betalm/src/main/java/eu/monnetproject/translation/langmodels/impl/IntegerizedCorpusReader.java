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
package eu.monnetproject.translation.langmodels.impl;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 *
 * @author John McCrae
 */
public class IntegerizedCorpusReader {

    private final DataInputStream corpus;
    public final static int CAPACITY = Integer.parseInt(System.getProperty("integer.corpus.max", "262144"));
    private final int[] buffer = new int[CAPACITY];
    private int loc;

    public IntegerizedCorpusReader(DataInputStream corpus) {
        this.corpus = corpus;
    }

    public int nextToken() throws IOException, EOFException {
        int tk = corpus.readInt();
        if (tk == 0) {
            loc = 0;
        } else {
            if (loc < CAPACITY) {
                buffer[loc++] = tk;
            }
        }
        return tk;
    }

    public boolean nextDocument() throws IOException {
        if (corpus.available() == 0) {
            return false;
        }
        loc = 0;
        int tk;
        try {
            while ((tk = corpus.readInt()) != 0 && corpus.available() != 0) {
                if (loc < CAPACITY) {
                    buffer[loc++] = tk;
                }
            }
        } catch (EOFException x) {
            return true;
        }
        return true;
    }

    public boolean hasNext() throws IOException {
        return corpus.available() > 0;
    }

    public int getBufferSize() {
        return loc;
    }

    public int[] getBuffer() {
        return buffer;
    }
}
