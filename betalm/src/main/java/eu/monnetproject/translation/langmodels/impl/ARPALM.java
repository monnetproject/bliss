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
package eu.monnetproject.translation.langmodels.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author John McCrae
 */
public class ARPALM {

    final public double[][] prob;
    final public double[][] alpha;
    final public String[][] grams;
    final public int n;

    public ARPALM(File lmFile) throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(lmFile));
        String s;
        DATA:
        {
            while ((s = in.readLine()) != null) {
                if (s.startsWith("\\data\\")) {
                    break DATA;
                }
            }
            throw new RuntimeException("Expected \\data\\");
        }

        int n = 0;

        final HashMap<Integer, Integer> ngramSizes = new HashMap<Integer, Integer>();

        final Pattern pattern = Pattern.compile("ngram (\\d)=(\\d+)");
        while ((s = in.readLine()) != null) {
            final Matcher matcher = pattern.matcher(s);
            if (matcher.matches()) {
                ngramSizes.put(Integer.parseInt(matcher.group(1)) - 1, Integer.parseInt(matcher.group(2)));
                n++;
            } else {
                break;
            }
        }

        this.n = n;

        alpha = new double[n][];
        grams = new String[n][];
        prob = new double[n][];
        for (int i = 0; i < n; i++) {
            alpha[i] = new double[ngramSizes.get(i)];
            grams[i] = new String[ngramSizes.get(i)];
            prob[i] = new double[ngramSizes.get(i)];
        }

        for (int m = 1; m <= n; m++) {
            NGRAM:
            {
                while ((s = in.readLine()) != null) {
                    if (s.startsWith("\\" + m + "-grams:")) {
                        break NGRAM;
                    }
                }
                throw new RuntimeException("Expected \\" + m + "-grams");
            }


            for (int i = 0; i < ngramSizes.get(m - 1); i++) {
                final StringTokenizer tok = new StringTokenizer(in.readLine());
                prob[m - 1][i] = Double.parseDouble(tok.nextToken());
                final StringBuilder sb = new StringBuilder();
                for (int j = 0; j < m; j++) {
                    if (j != 0) {
                        sb.append(" ");
                    }
                    sb.append(tok.nextToken());
                }
                if (m != n) {
                    if (tok.hasMoreTokens()) {
                        alpha[m - 1][i] = Double.parseDouble(tok.nextToken());
                    }
                }
                grams[m - 1][i] = sb.toString();
            }
            System.err.println("Read " + ngramSizes.get(m - 1) + " " + m + "-grams");
        }
    }

    public double[] getQuartiles(int n) {
        final double[] unisort = Arrays.copyOf(prob[n - 1], prob[n - 1].length);
        Arrays.sort(unisort);
        return new double[]{unisort[unisort.length / 4],
                    unisort[2 * unisort.length / 4],
                    unisort[3 * unisort.length / 4]};
    }
    private List<Object2IntMap<String>> ngramIdxs = new ArrayList<Object2IntMap<String>>();

    public Object2IntMap<String> ngramIdx(int n) {
        if (ngramIdxs.size() >= n && !ngramIdxs.get(n - 1).isEmpty()) {
            return ngramIdxs.get(n - 1);
        } else {
            final Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<String>();
            for (int i = 0; i < grams[n - 1].length; i++) {
                map.put(grams[n - 1][i], i);
            }
            while (ngramIdxs.size() < n - 1) {
                ngramIdxs.add(new Object2IntOpenHashMap<String>());
            }
            ngramIdxs.add(map);
            return map;
        }
    }

    public double score(String s, int n, double min) {
        assert (n == s.split(" ").length);
        assert (n > 0);
        if (ngramIdx(n).containsKey(s)) {
            return prob[n - 1][ngramIdx(n).getInt(s)];
        } else if (n > 1) {
            final String boStr = s.substring(0, s.lastIndexOf(" "));
            if (ngramIdx(n - 1).containsKey(boStr)) {
                return alpha[n - 2][ngramIdx(n - 1).getInt(boStr)]
                        + score(s.substring(s.indexOf(" ") + 1, s.length()), n - 1,  min);
            } else {
                return score(s.substring(s.indexOf(" ") + 1, s.length()), n - 1, min);
            }
        } else {
            return min;
        }
    }
    
    public int unk(String s, int n) {
        if (ngramIdx(n).containsKey(s)) {
            return 0;
        } else if (n > 1) {
            return -1 + unk(s.substring(s.indexOf(" ") + 1, s.length()),n-1);
        } else {
            return -1;
        }
    }
}
