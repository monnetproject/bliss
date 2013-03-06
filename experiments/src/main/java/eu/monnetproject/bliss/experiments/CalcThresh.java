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

import eu.monnetproject.bliss.CLIOpts;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.AbstractList;

/**
 *
 * @author John McCrae
 */
public class CalcThresh {

    public static void main(String[] args) throws Exception {
        //args = "5 ../wiki/en-es/freqs".split(" ");
        final CLIOpts opts = new CLIOpts(args);
        
        final File outFiles = opts.woFile("out", "The values for W and minFreq will be written to $FILE.W and $FILE.thresh", null);
        final int N = opts.nonNegIntValue("N", "The number of words to calucate a frequency at");
        final File freqs = opts.roFile("freqs", "The frequency file");
        
        if(!opts.verify(CalcThresh.class)) {
            return;
        }
        
        final InputStream inputStream = CLIOpts.openInputAsMaybeZipped(freqs);
        final DataInputStream dataIn = new DataInputStream(inputStream);
        final IntSortedList intSortedList = new IntSortedList(N);
        int ct = 0;
        while (dataIn.available() > 0) {
            if(++ct % 100000 == 0) {
                System.err.print(".");
            }
            try {
                int i = dataIn.readInt();
                intSortedList.add(i);
            } catch (EOFException x) {
                break;
            }
        }
        System.err.println();
        if(outFiles != null) {
            final PrintWriter out = new PrintWriter(outFiles.getName()+".W");
            out.println(ct);
            out.flush();
            out.close();
        } 
        System.out.println("count="+ct);
        
        if(outFiles != null) {
            final PrintWriter out = new PrintWriter(outFiles.getName()+".thresh");
            out.println(intSortedList.first());
            out.flush();
            out.close();
        } 
        System.out.println("thresh="+intSortedList.first());
    }

    private static class IntSortedList extends AbstractList<Integer> {

        final int[] data;
        final int N;
        int n = 0;
        int minIdx = -1;
        int dataMin = Integer.MAX_VALUE;

        public IntSortedList(int N) {
            assert(N > 0);
            this.N = N;
            this.data = new int[N];
        }

        @Override
        public Integer get(int index) {
            return data[index];
        }

        @Override
        public int size() {
            return n;
        }

        @Override
        public boolean add(Integer e) {
            return add(e.intValue());
        }

        private void calcNewMin() {
            if(n == N) {
                minIdx = -1;
                dataMin = Integer.MAX_VALUE;
                for(int i = 0; i < N; i++) {
                    if(data[i] < dataMin) {
                        dataMin = data[i];
                        minIdx = i;
                    }
                }
            }
        }
        
        public boolean add(int e) {
            if(n < N) {
                if(e < dataMin) {
                    minIdx = n;
                    dataMin = e;
                }
                data[n++] = e;
                return true;
            } else {
                if(e < dataMin) {
                    return false;
                } else {
                    data[minIdx] = e;
                    calcNewMin();
                    return true;
                }
            }
        }
        
        public int first() {
            return dataMin;
        }
    }
}
