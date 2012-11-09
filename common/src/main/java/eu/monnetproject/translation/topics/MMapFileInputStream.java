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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This is a faster FileInputStream that maps several megabyte of data into
 * memory quickly using mmap(). This class is frequently about 60-90x faster
 * than the standard {@code java.io.FileInputStream}
 *
 * @author John McCrae
 */
public class MMapFileInputStream extends InputStream {

    /**
     * The file itself
     */
    private final FileChannel channel;
    /**
     * The size of the file
     */
    private final long fileSize;
    /**
     * The (maximal) buffer size
     */
    private final int bufSize;
    /**
     * The location in the stream of the start of the current buffer
     */
    private long buf0 = 0;
    /**
     * The position of the pointer relative to the start of the current buffer
     */
    private int pos = 0;
    /**
     * The buffer
     */
    private MappedByteBuffer buf;
    /**
     * The closed flag
     */
    private boolean isClosed = false;

    /**
     * Create a new mmapped file input stream
     *
     * @param file The file
     * @throws IOException If an I/O error occurred
     */
    public MMapFileInputStream(File file) throws IOException {
        // Default buf size = 4MB
        this(file, 4194304);
    }

    /**
     * Create a new mmapped file input stream
     *
     * @param fileName The file name
     * @throws IOException If an I/O error occurred
     */
    public MMapFileInputStream(String fileName) throws IOException {
        // Default buf size = 4MB
        this(new File(fileName), 4194304);
    }

    /**
     * Create a new mmapped file input stream
     *
     * @param file The file
     * @param bufSize The size of the buffer to mmap at one time
     * @throws IOException If an I/O error occurred
     */
    public MMapFileInputStream(File file, int bufSize) throws IOException {
        this.fileSize = file.length();
        this.bufSize = bufSize;
        this.channel = new FileInputStream(file).getChannel();
    }

    protected MappedByteBuffer getBufferWithAtLeast(int bytesFree) throws IOException {
        if (buf0 + pos >= fileSize) {
            return null;
        }
        if (buf == null || bufSize - pos < bytesFree) {
            // The current buffer is not large enough
            final long toRead = Math.min(fileSize - buf0 - pos, bufSize);
            // No more to read (EOF)
            if (toRead == 0) {
                return null;
            }
            // Move the buffer so that pos is the new start of buffer
            buf0 += pos;
            pos = 0;
            return buf = channel.map(FileChannel.MapMode.READ_ONLY, buf0, toRead);
        } else {
            // No problemo
            return buf;
        }
    }

    @Override
    public int read() throws IOException {
        if (isClosed) {
            throw new IOException("Stream closed");
        }
        final MappedByteBuffer buffer = getBufferWithAtLeast(1);
        if (buffer == null) {
            return -1;
        } else {
            pos++;
            return buffer.get() & 0xff;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        if (isClosed) {
            throw new IOException("Stream closed");
        }
        if (n > bufSize) {
            skip(bufSize);
            return skip(n - bufSize);
        }
        if (buf == null || pos + n > bufSize) {
            if (buf0 + pos + n > fileSize) {
                // n is beyound EOF
                int delta = (int) (fileSize - buf0 - pos);
                // Note this potentially allow pos > bufferSize ...  be careful!
                pos = (int) (fileSize - buf0);
                return delta;
            } else {
                // Hmm.... this means we skip over a buffer border
                // Calculate how much more we can read
                final long toRead = Math.min(fileSize - buf0 - pos - n, bufSize);
                // Nothing means no more bytes available :(
                if (toRead == 0) {
                    return 0;
                }
                // Advance buffer start and read new buffer
                buf0 += pos + n;
                pos = 0;
                buf = channel.map(FileChannel.MapMode.READ_ONLY, buf0, toRead);
                return n;
            }
        } else {
            // We just need to advance the pointer (phew!)
            pos += n;
            return n;
        }
    }

    @Override
    public int available() throws IOException {
        if (isClosed) {
            throw new IOException("Stream closed");
        }
        final long l = fileSize - buf0 - pos;
        if (l > 0xffffffffl) {
            return 1;
        }
        return (int) l;
    }
    private long mark;

    @Override
    public synchronized void mark(int readlimit) {
        this.mark = buf0 + pos;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (isClosed) {
            throw new IOException("Stream closed");
        }
        if (mark >= buf0) {
            // Still in same buffer
            pos = (int) (mark - buf0);
        } else {
            // Must go back
            final long toRead = Math.min(fileSize - mark, bufSize);
            // Nothing means no more bytes available :(
            if (toRead == 0) {
                return;
            }
            // Advance buffer start and read new buffer
            buf0 = mark;
            pos = 0;
            buf = channel.map(FileChannel.MapMode.READ_ONLY, buf0, toRead);

        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        buf = null;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (isClosed) {
            throw new IOException("Stream closed");
        }
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (len > bufSize) {
            // Direct read
            final long toRead = Math.min(fileSize - buf0 - pos, len);
            if(toRead == 0 && len != 0) {
                return -1;
            }
            final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, buf0 + pos, toRead);
            buffer.get(b, off, len);
            final long toReadNext = Math.min(fileSize - buf0 - pos - toRead, bufSize);
            if (toReadNext != 0) {
                buf0 = buf0 + pos + toRead;
                pos = 0;
                buf = channel.map(FileChannel.MapMode.READ_ONLY, buf0, toReadNext);
            } else {
                buf0 = buf0 + pos + toRead;
                pos = 0;
                buf = null;
            }
            return (int) toRead;
        } else {
            final int toRead = (int) Math.min(fileSize - buf0 - pos, len);
            final MappedByteBuffer buffer = getBufferWithAtLeast(toRead);
            if(buffer == null) {
                return -1;
            }
            buffer.get(b, off, toRead);
            pos += toRead;
            return toRead;
        }
    }
}
