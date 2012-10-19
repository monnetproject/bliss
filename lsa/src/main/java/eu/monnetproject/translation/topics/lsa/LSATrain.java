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
package eu.monnetproject.translation.topics.lsa;

import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition;
import eu.monnetproject.math.sparse.eigen.SingularValueDecomposition.Solution;
import eu.monnetproject.translation.topics.CLIOpts;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

/**
 *
 * @author John McCrae
 */
public class LSATrain {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);

        final double epsilon = opts.doubleValue("epsilon", 0.0001, "The error rate");
        final File corpus = opts.roFile("corpus[.gz|bz2]", "The corpus file");
        
        final int W = opts.intValue("W", "The number of distinct tokens");
        final int J = opts.intValue("J", "The number of documents (per language)");
        final int K = opts.intValue("K", "The number of topics");
        
        final File outFile = opts.woFile("output", "The file to write the SVD to");

        if (!opts.verify(LSATrain.class)) {
            return;
        }
        
        final SingularValueDecomposition svd = new SingularValueDecomposition();
        
        final Solution svdSoln = svd.calculate(new SkipOddZeroDataStreamIterable(corpus), W, J, K, epsilon);
        
        write(svdSoln,outFile);

    }

    private static void write(Solution soln, File outFile) throws IOException {
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(outFile));
        out.writeInt(soln.S.length);
        out.writeInt(soln.U.length);
        out.writeInt(soln.V[0].length);
        
        for(int i = 0; i < soln.U.length; i++) {
            for(int j = 0; j < soln.U[i].length; j++) {
                out.writeDouble(soln.U[i][j]);
            }
        }
        
        for(int i = 0; i < soln.S.length; i++) {
                out.writeDouble(soln.S[i]);
        }
        
        for(int i = 0; i < soln.V.length; i++) {
            for(int j = 0; j < soln.V[i].length; j++) {
                out.writeDouble(soln.V[i][j]);
            }
        }
        out.flush();
        out.close();
    }

    private static class SkipOddZeroDataStreamIterable implements IntIterable {

        private final File file;

        public SkipOddZeroDataStreamIterable(File file) throws IOException {
            this.file = file;
        }

        @Override
        public IntIterator iterator() {
            try {
                System.err.print(".");
                return new DataInputStreamAsIntIterator(CLIOpts.openInputAsMaybeZipped(file));
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }

        private static class DataInputStreamAsIntIterator implements IntIterator {

            private final DataInputStream data;
            private int next = -1;
            private boolean hasNext;
            private boolean odd = true;

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
                if(next == 0) {
                    if(odd && hasNext) {
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
}
