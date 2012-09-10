/**********************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
package eu.monnetproject.translation.topics;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class SparseRealArray extends HashMap<Integer, Double> {
    private static final long serialVersionUID = 8976723557456415580L;

    private final double defaultValue;
    private final double epsilon;

    public SparseRealArray() {
        this.defaultValue = 0;
        this.epsilon = 0.0;
    }

    public SparseRealArray(double defaultValue, double epsilon) {
        this.defaultValue = defaultValue;
        this.epsilon = epsilon;
    }

    @Override
    public Double get(Object o) {
        final Double rval = super.get(o);
        if (rval == null) {
            return defaultValue;
        } else {
            return rval;
        }
    }

    public void inc(int idx) {
        if (super.containsKey(idx)) {
            super.put(idx, super.get(idx) + 1);
        } else {
            super.put(idx, 1.0);
        }
    }

    public void dec(int idx) {
        if (super.containsKey(idx)) {
            final Double val = super.get(idx);
            if (Math.abs(val - 1.0 - defaultValue) <= epsilon) {
                super.remove(idx);
            } else {
                super.put(idx, val - 1);
            }
        } else {
            throw new RuntimeException("Decrementing below zero");
        }
    }

    @Override
    public String toString() {
        if(isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Double> entry : entrySet()) {
            sb.append(",").append(entry.getKey().toString()).append("=").append(entry.getValue().toString());
        }
        return sb.substring(1);
    }

    public static SparseRealArray fromString(String string, int defaultValue) throws SparseArrayFormatException {
        return fromString(string,defaultValue,",");
    }
    
    public static SparseRealArray fromString(String string, int defaultValue, String sep) throws SparseArrayFormatException {
        String[] entries = string.split(sep);
        final SparseRealArray SparseRealArray = new SparseRealArray();
        for (String entry : entries) {
            if (entry.matches("\\s*")) {
                continue;
            }
            String[] values = entry.split("=");
            if (values.length != 2) {
                throw new SparseArrayFormatException("Bad sparse array value " + entry);
            }
            SparseRealArray.put(new Integer(values[0]), new Double(values[1]));
        }
        return SparseRealArray;
    }
    
     public double[] toArray() {
        int max = -Integer.MAX_VALUE;
        for(int i : keySet()) {
            if(i > max) {
                max = i;
            }
        }
        double[] arr = new double[max+1];
        for(Map.Entry<Integer,Double> entry : entrySet()) {
            arr[entry.getKey()] = entry.getValue();
        }
        return arr;
    }
    
    public double[] toArray(int length) {
        double[] d = new double[length];
        for (int i : this.keySet()) {
            d[i] = this.get(i);
        }
        return d;
    }
    
    public static SparseRealArray fromArray(double[] arr) {
        SparseRealArray sa = new SparseRealArray();
        for(int i = 0; i < arr.length; i++) {
            if(arr[i] != 0) {
                sa.put(i, arr[i]);
            }
        }
        return sa;
    }
    
    public static double[][] toArray(SparseRealArray[] sas) {
        double[][] arrs = new double[sas.length][];
        for(int i = 0; i < sas.length; i++) {
            arrs[i] = sas[i].toArray();
        }
        return arrs;
    }
    
    public static SparseRealArray[] fromArray(double[][] arrs) {
        SparseRealArray[] sas = new SparseRealArray[arrs.length];
        for(int i = 0; i < arrs.length; i++) {
            sas[i] = SparseRealArray.fromArray(arrs[i]);
        }
        return sas;
    }
    
    public static SparseRealArray[][] fromArray(double[][][] arrs) {
        SparseRealArray[][] sass = new SparseRealArray[arrs.length][];
        for(int i = 0; i < arrs.length; i++) {
            sass[i] = fromArray(arrs[i]);
        }
        return sass;
    }
    
    private double sum = -1;
    
    public double sum() {
        if(sum >= 0) {
            return sum;
        } else {
            sum = 0;
            for(double v : values()) {
                sum += v;
            }
            return sum;
        }
    }
    
    public double minIdx() {
        double min = Double.MAX_VALUE;
        for(int i : keySet()) {
            if(i < min) {
                min = i;
            }
        }
        return min;
    }
    
    public double maxIdx() {
        double max = -Double.MAX_VALUE;
        for(int i : keySet()) {
            if(i > max) {
                max = i;
            }
        }
        return max;
    }

    public void add(int idx, double i) {
        if(this.containsKey(idx)) {
            this.put(idx, super.get(idx) + i);
        } else {
            this.put(idx, i);
        }
    }
    
    public void sub(int idx, double i) {
        if(this.containsKey(idx)) {
            this.put(idx, super.get(idx) - i);
        } else {
            this.put(idx, -i);
        }
    }
}