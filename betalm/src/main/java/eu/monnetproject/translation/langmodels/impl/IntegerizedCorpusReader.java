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
package eu.monnetproject.translation.langmodels.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.IntBuffer;

/**
 *
 * @author John McCrae
 */
public class IntegerizedCorpusReader {

    private final DataInputStream corpus;
    public final static int CAPACITY = Integer.parseInt(System.getProperty("integer.corpus.max","262144"));
    private final IntBuffer buffer = IntBuffer.allocate(CAPACITY);
    
            
    public IntegerizedCorpusReader(DataInputStream corpus) {
        this.corpus = corpus;
    }
    
    public int nextToken() throws IOException {
        int tk = corpus.readInt();
        if(tk == 0) {
            buffer.clear();
        } else { 
            buffer.put(tk);
        }
        return tk;
    }
    
    public boolean nextDocument() throws IOException {
        if(corpus.available() == 0) {
            return false;
        }
        buffer.clear();
        int tk;
        while((tk = corpus.readInt()) != 0 && corpus.available() != 0 && buffer.remaining() > 0) {
            buffer.put(tk);
        }
        return true;
    }
    
    public boolean hasNext() throws IOException {
        // > 1 as we assume that the last document is \0-terminated 
        return corpus.available() > 1;
    }
    
    public int getBufferSize() {
        return buffer.position();
    }
    
    public int[] getBuffer() {
        return buffer.array();
    }
}
