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
package eu.monnetproject.math.sparse;

import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 *
 * @author John McCrae
 */
public class DataStreamIterable implements IntIterable {
    private final File file;

    public DataStreamIterable(File file) throws IOException {
        this.file = file;
    }
    public static InputStream openInputAsMaybeZipped(File file) throws IOException {
        if (file.getName().endsWith(".gz")) {
            return new GZIPInputStream(new FileInputStream(file));
        } else if (file.getName().endsWith(".bz2")) {
            return new BZip2CompressorInputStream(new FileInputStream(file));
        } else {
            return new FileInputStream(file);
        }
    }
    @Override
    public IntIterator iterator() {
        try {
            return new DataInputStreamAsIntIterator(openInputAsMaybeZipped(file));
        } catch(IOException x) {
            throw new RuntimeException(x);
        } 
    }
    
    private static class DataInputStreamAsIntIterator implements IntIterator {

        private final DataInputStream data;
        private int next = -1;
        private boolean hasNext;

        public DataInputStreamAsIntIterator(InputStream is) {
            this.data = new DataInputStream(is);
            advance();
        }

        private void advance() {
            try {
                next = data.readInt();
                hasNext = true;
            } catch (EOFException x) {
                hasNext = false;
                try {
                    data.close();
                } catch (Exception x2) {
                }
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }

        @Override
        public int nextInt() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            int rv = next;
            advance();
            return rv;
        }

        @Override
        public int skip(int n) {
            int i = 0;
            for (; hasNext && i < n; i++) {
                advance();
            }
            return i;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Integer next() {
            return nextInt();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not mutable.");
        }
    }
    
}
