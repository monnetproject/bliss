/**
 * ********************************************************************************
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
package eu.monnetproject.bliss.betalm.impl;

import eu.monnetproject.math.sparse.SparseIntArray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class ParallelReader {

    public final SparseIntArray[][] x; // jli
    private final int W;
    private final int n;
    public final Map<String, Integer> words;

    private ParallelReader(SparseIntArray[][] x, int W) {
        this.x = x;
        this.W = W;
        this.n = 1;
        this.words = new HashMap<String, Integer>();
    }

    private ParallelReader(SparseIntArray[][] x, int W, int n) {
        this.x = x;
        this.W = W;
        this.n = 1;
        this.words = new HashMap<String, Integer>();
    }

    protected ParallelReader(ParallelReader pr) {
        this.x = pr.x;
        this.W = pr.W;
        this.n = pr.n;
        this.words = pr.words;
    }

    public static ParallelReader fromFile(File data) throws IOException {

        final BufferedReader in = new BufferedReader(new FileReader(data));
        final List<SparseIntArray[]> docs = new ArrayList<SparseIntArray[]>();
        String s;
        int W = 0;
        while (!(s = in.readLine()).matches("\\s*")) {
            final String[] ss = s.split("\\s+");
            final int[] i1 = new int[ss.length];
            for (int i = 0; i < ss.length; i++) {
                i1[i] = Integer.parseInt(ss[i]);
                if (i1[i] >= W) {
                    W = i1[i] + 1;
                }
            }
            s = in.readLine();
            final String[] ss2 = s.split("\\s+");
            final int[] i2 = new int[ss2.length];
            for (int i = 0; i < ss2.length; i++) {
                i2[i] = Integer.parseInt(ss2[i]);
                if (i2[i] >= W) {
                    W = i2[i] + 1;
                }
            }
            docs.add(new SparseIntArray[]{histogram(i1, W), histogram(i2, W)});
        }
        final ParallelReader reader = new ParallelReader(docs.toArray(new SparseIntArray[docs.size()][]), W);
        while ((s = in.readLine()) != null) {
            final String[] ss = s.split("\\s+");
            if (ss.length == 2) {
                reader.words.put(ss[0], Integer.parseInt(ss[1]));
            }
        }
        return reader;
    }

    public static ParallelReader fromFile(final File data, final int W, final int n) throws IOException {

        final BufferedReader in = new BufferedReader(new FileReader(data));
        final List<SparseIntArray[]> docs = new ArrayList<SparseIntArray[]>();
        String s;
        int Wn = W; // W ** n
        for (int i = 1; i < 1; i++) {
            Wn *= W;
        }
        while (!(s = in.readLine()).matches("\\s*")) {
            int v1 = 0, v2 = 0;
            final String[] ss = s.split("\\s+");
            final int[] i1 = new int[ss.length];
            for (int i = 0; i < ss.length; i++) {
                final int xi = Integer.parseInt(ss[i]);
                if (xi >= W) {
                    throw new RuntimeException("Read word idx " + xi + " but W=" + W);
                } else {
                    // This is the trick to read n-grams ;)
                    i1[i] = v1 = v1 % Wn * W + xi;
                }
            }
            s = in.readLine();
            final String[] ss2 = s.split("\\s+");
            final int[] i2 = new int[ss2.length];
            for (int i = 0; i < ss2.length; i++) {
                final int xi = Integer.parseInt(ss2[i]);
                if (xi >= W) {
                    throw new RuntimeException("Read word idx " + xi + " but W=" + W);
                } else {
                    // This is the trick to read n-grams ;)
                    i2[i] = v2 = v2 % Wn * W + xi;
                }
            }
            docs.add(new SparseIntArray[]{histogram(i1, W), histogram(i2, W)});
        }
        final ParallelReader reader = new ParallelReader(docs.toArray(new SparseIntArray[docs.size()][]), W);
        while ((s = in.readLine()) != null) {
            final String[] ss = s.split("\\s+");
            if (ss.length == 2) {
                reader.words.put(ss[0], Integer.parseInt(ss[1]));
            }
        }
        return reader;
    }

    public static SparseIntArray histogram(int[] vector, int W) {
        final SparseIntArray hist = new SparseIntArray(W);
        for (int i : vector) {
            hist.inc(i);
        }
        return hist;
    }

    public int[][] prepClean() {
        final int L = 2;
        boolean[][] used = new boolean[W][L];
        int[][] map = new int[W][L];
        int[] cts = new int[L];
        for (int j = 0; j < x.length; j++) {
            for (int l = 0; l < L; l++) {
                for (int w : x[j][l].keySet()) {
                    if (!used[w][l]) {
                        used[w][l] = true;
                        map[w][l] = cts[l]++;
                    }
                }
            }
        }
        System.err.println("Cts: " + Arrays.toString(cts));
        return map;
    }

    public void clean(PrintWriter out, int[][] map) {
        final int L = 2;
        for (int j = 0; j < x.length; j++) {
            if (x[j][0].isEmpty() || x[j][1].isEmpty()) {
                continue;
            }
            for (int l = 0; l < L; l++) {
                for (Map.Entry<Integer, Integer> e : x[j][l].entrySet()) {
                    for (int i = 0; i < e.getValue(); i++) {
                        out.print(map[e.getKey()][l] + " ");
                    }
                }
                out.println();
            }
        }
        out.println();
    }

    public int W() {
        int w = W;
        for (int i = 1; i < n; i++) {
            w *= W;
        }
        return w;
    }
}
