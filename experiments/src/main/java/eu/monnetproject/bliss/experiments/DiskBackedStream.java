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
package eu.monnetproject.bliss.experiments;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author John McCrae
 */
public class DiskBackedStream implements Iterable<double[]> {

    private final int N, M;
    private int n;
    private final double[][] data;
    private final LinkedList<File> pages = new LinkedList<File>();
    private final LinkedList<Integer> sizes = new LinkedList<Integer>();

    public DiskBackedStream(int N, int M) {
        this.N = N;
        this.M = M;
        this.n = 0;
        this.data = new double[N][M];
    }

    public double[] get(int i) {
        final int pageNo = i / N;
        final int offset = i % N;
        final File file = pages.get(pageNo);
        FileInputStream fis = null;
        final double[] elem = new double[M];
        try {
            fis = new FileInputStream(file);
            final MappedByteBuffer dataAsBytes = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, offset * M * 8, M * 8);
            for (int m = 0; m < M; m++) {
                elem[m] = dataAsBytes.getDouble();
            }
        } catch (IOException x) {
            x.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception x) {
                }
            }
        }
        return elem;
    }

    @Override
    public Iterator<double[]> iterator() {
        return new DiskBackStreamIterator();
    }

    public static interface Builder {

        void add(double[] data);

        void finish();
    }

    public Builder builder() {
        return new DiskBackedStreamBuilder();
    }

    private void nextPage() {
        try {
            synchronized (this) {
                final File pageFile = File.createTempFile("stream", ".bin");
                pageFile.deleteOnExit();
                final DataOutputStream out = new DataOutputStream(new FileOutputStream(pageFile));
                for (int n2 = 0; n2 < n; n2++) {
                    for (int m = 0; m < M; m++) {
                        out.writeDouble(data[n2][m]);
                    }
                }
                out.flush();
                out.close();
                sizes.add(n);
                pages.add(pageFile);
                n = 0;
            }

        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    private class DiskBackedStreamBuilder implements Builder {

        @Override
        public void add(double[] d) {
            if (n >= N) {
                nextPage();
            }
            synchronized (this) {
                System.arraycopy(d, 0, data[n++], 0, d.length);
            }
        }

        @Override
        public void finish() {
            nextPage();
        }
    }

    private class DiskBackStreamIterator implements Iterator<double[]> {

        private final Iterator<File> pageIterator = pages.iterator();
        private final Iterator<Integer> sizeIterator = sizes.iterator();
        private int N2 = N;

        public DiskBackStreamIterator() {
            n = N;
        }

        @Override
        public boolean hasNext() {
            return (n < N2) || pageIterator.hasNext();
        }

        @Override
        public double[] next() {
            if (n == N2) {
                try {
                    final File file = pageIterator.next();
                    N2 = sizeIterator.next();
                    final FileInputStream fis = new FileInputStream(file);
                    final MappedByteBuffer dataAsBytes = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, N2 * M * 8);
                    for (int n2 = 0; n2 < N2; n2++) {
                        for (int m = 0; m < M; m++) {
                            data[n2][m] = dataAsBytes.getDouble();
                        }
                    }
                    fis.close();
                    n = 0;
                } catch (IOException x) {
                    throw new RuntimeException(x);
                }
            }
            return data[n++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
