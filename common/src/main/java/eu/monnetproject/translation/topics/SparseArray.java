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
 ********************************************************************************
 */
package eu.monnetproject.translation.topics;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class SparseArray extends Int2IntOpenHashMap {

    private static final long serialVersionUID = 9099860117350068663L;
    private final int n;
    private final int defaultValue;

    public SparseArray(int n) {
        this.n = n;
        this.defaultValue = 0;
    }

    public SparseArray(int n, int defaultValue) {
        this.n = n;
        this.defaultValue = defaultValue;
    }

    public SparseArray(int n, int defaultValue, int... values) {
        this.n = n;
        this.defaultValue = defaultValue;
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Wrong number of varargs to SparseArray");
        }
        for (int i = 0; i < values.length; i += 2) {
            put(values[i], values[i + 1]);
        }
    }

    @Override
    public Integer get(Object o) {
        final Integer rval = super.get(o);
        if (rval == null) {
            return defaultValue;
        } else {
            return rval;
        }
    }

    public void inc(int idx) {
        if (super.containsKey(idx)) {
            final Integer val = super.get(idx);
            if (val == defaultValue - 1) {
                super.remove(idx);
            } else {
                super.put(idx, val + 1);
            }
        } else {
            super.put(idx, defaultValue + 1);
        }
    }

    public void dec(int idx) {
        if (super.containsKey(idx)) {
            final Integer val = super.get(idx);
            if (val == defaultValue + 1) {
                super.remove(idx);
            } else {
                super.put(idx, val - 1);
            }
        } else {
            super.put(idx, defaultValue - 1);
        }
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : entrySet()) {
            sb.append(",").append(entry.getKey().toString()).append("=").append(entry.getValue().toString());
        }
        return sb.substring(1);
    }

    public static SparseArray fromString(String string, int length, int defaultValue) throws SparseArrayFormatException {
        return fromString(string, length, defaultValue, ",");
    }

    public static SparseArray fromString(String string, int length, int defaultValue, String sep) throws SparseArrayFormatException {
        String[] entries = string.split(sep);
        final SparseArray sparseArray = new SparseArray(length);
        for (String entry : entries) {
            if (entry.matches("\\s*")) {
                continue;
            }
            String[] values = entry.split("=");
            if (values.length != 2) {
                throw new SparseArrayFormatException("Bad sparse array value " + entry);
            }
            sparseArray.put(new Integer(values[0]), new Integer(values[1]));
        }
        return sparseArray;
    }

    public int[] toArray() {
        int[] arr = new int[n];
        if (defaultValue != 0) {
            Arrays.fill(arr, defaultValue);
        }
        for (Map.Entry<Integer, Integer> entry : entrySet()) {
            arr[entry.getKey()] = entry.getValue();
        }
        return arr;
    }

    public double[] toDoubleArray() {
        double[] d = new double[n];
        if (defaultValue != 0) {
            Arrays.fill(d, defaultValue);
        }
        for (int i : this.keySet()) {
            d[i] = this.get(i);
        }
        return d;
    }

    public SparseRealArray toRealArray() {
        final SparseRealArray sparseRealArray = new SparseRealArray(defaultValue, 0.0);
        for (Map.Entry<Integer, Integer> entry : entrySet()) {
            sparseRealArray.put(entry.getKey(), entry.getValue().doubleValue());
        }
        return sparseRealArray;
    }

    public static SparseArray fromArray(int[] arr) {
        SparseArray sa = new SparseArray(arr.length);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0) {
                sa.put(i, arr[i]);
            }
        }
        return sa;
    }

    public static SparseArray fromBinary(File file, int W) throws IOException {
        return fromBinary(new FileInputStream(file),W);
    }
    
    public static SparseArray fromBinary(InputStream stream, int W) throws IOException {
        final DataInputStream dis = new DataInputStream(stream);
        final SparseArray arr = new SparseArray(W);
        while (dis.available() > 0) {
            try {
                arr.inc(dis.readInt());
            } catch (EOFException x) {
                break;
            }
        }
        return arr;
    }

    public static SparseArray histogram(int[] vector, int W) {
        final SparseArray hist = new SparseArray(W);
        for (int i : vector) {
            hist.inc(i);
        }
        return hist;
    }

    public static int[][] toArray(SparseArray[] sas) {
        int[][] arrs = new int[sas.length][];
        for (int i = 0; i < sas.length; i++) {
            arrs[i] = sas[i].toArray();
        }
        return arrs;
    }

    public static SparseArray[] fromArray(int[][] arrs) {
        SparseArray[] sas = new SparseArray[arrs.length];
        for (int i = 0; i < arrs.length; i++) {
            sas[i] = SparseArray.fromArray(arrs[i]);
        }
        return sas;
    }

    public static SparseArray[][] fromArray(int[][][] arrs) {
        SparseArray[][] sass = new SparseArray[arrs.length][];
        for (int i = 0; i < arrs.length; i++) {
            sass[i] = fromArray(arrs[i]);
        }
        return sass;
    }

    public final int n() {
        return n;
    }
    private int sum = -1;

    public int sum() {
        if (sum >= 0) {
            return sum;
        } else {
            sum = defaultValue * n;
            for (int v : values()) {
                sum += v;
            }
            return sum;
        }
    }

    public int minIdx() {
        int min = Integer.MAX_VALUE;
        for (int i : keySet()) {
            if (i < min) {
                min = i;
            }
        }
        return min;
    }

    /*public int maxIdx() {
     int max = -Integer.MAX_VALUE;
     for(int i : keySet()) {
     if(i > max) {
     max = i;
     }
     }
     return max;
     }*/
    @Override
    public int add(int idx, int i) {
        if (this.containsKey(idx)) {
            final Integer val = super.get(idx);
            if (val == defaultValue) {
                this.remove(idx);
            } else {
                this.put(idx, val + i);
            }
            return val;
        } else {
            this.put(idx, i + defaultValue);
            return defaultValue;
        }
    }

    public void sub(int idx, int i) {
        if (this.containsKey(idx)) {
            final Integer val = super.get(idx);
            if (val == defaultValue) {
                this.remove(idx);
            } else {
                this.put(idx, val - i);
            }
        } else {
            if (i != defaultValue) {
                this.put(idx, -i);
            }
        }
    }
}