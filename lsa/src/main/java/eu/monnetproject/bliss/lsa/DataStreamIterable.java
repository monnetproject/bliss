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
package eu.monnetproject.bliss.lsa;

import eu.monnetproject.bliss.CLIOpts;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

/**
 *
 * @author John McCrae
 */
public class DataStreamIterable implements IntIterable {

    private final File file;
    private final int W;

    public DataStreamIterable(File file, int W) throws IOException {
        this.file = file;
        this.W = W;
    }

    @Override
    public IntIterator iterator() {
        try {
            System.err.print(".");
            return new DataInputStreamAsIntIterator(CLIOpts.openInputAsMaybeZipped(file), W);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    public static class DataInputStreamAsIntIterator implements IntIterator {

        private final InputStream data;
        private int next = -1;
        private boolean hasNext;
        private boolean odd = true;
        private final int W;

        public DataInputStreamAsIntIterator(InputStream is, int W) {
            this.data = is;
            this.W = W;
            advance();
        }
        final byte[] buf = new byte[4];

        private void advance() {
            try {
                int r = data.read(buf);
                if (r == -1) {
                    hasNext = false;
                    try {
                        data.close();
                    } catch (Exception x2) {
                    }
                    return;
                }
                while (r < 4) {
                    final int r2 = data.read(buf, r, 4 - r);
                    if (r2 == 0) {
                        throw new RuntimeException("Broken read!");
                    }
                    r += r2;
                }
                hasNext = true;
                //System.out.println(bytesToHex(buf));
                next = (buf[0] & 0xFF) << 24 | (buf[1] & 0xFF) << 16 | (buf[2] & 0xFF) << 8 | (buf[3] & 0xFF);
                //next = ByteBuffer.wrap(buf).getInt(); 
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
            int rv = odd ? next : (next + W);
            advance();
            if (next == 0) {
                if (odd && hasNext) {
                    advance();
                }
                odd = !odd;
            }
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