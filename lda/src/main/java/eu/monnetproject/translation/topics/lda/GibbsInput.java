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
package eu.monnetproject.translation.topics.lda;

import eu.monnetproject.lang.Language;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author John McCrae
 */
public class GibbsInput {

    /**
     * The number of documents
     */
    public final int D;
    /**
     * The number of words in the vocabulary
     */
    public final int W;
    /**
     * The number of words in each document
     */
    public final int[] DN;
    /**
     * The contents of the corpus
     */
    public final int[][] x;
    /**
     * The languages used in the documents
     */
    public final Language[] languages;
    /**
     * The languages of each document
     */
    public final int[] m;
    /**
     * The map of same language documents
     */
    public final int[][] mu;
    /**
     * The map from words to integers
     */
    public final Map<String, Integer> words;

    public GibbsInput(int D, int W, int[] DN, int[][] x, Language[] languages, int[] m, int[][] mu, Map<String, Integer> words) {
        this.D = D;
        this.W = W;
        this.DN = DN;
        this.x = x;
        this.languages = languages;
        this.m = m;
        this.mu = mu;
        this.words = words;
    }

    public boolean validate() {
        if (D <= 0) {
            System.err.println("D <= 0");
            return false;
        }
        if (W <= 0) {
            System.err.println("W <= 0");
            return false;
        }
        if (DN.length != D) {
            System.err.println("DN.length != D");
            return false;
        }
        if (x.length != D) {
            System.err.println("x.length != D");
            return false;
        }
        for (int j = 0; j < D; j++) {
            if (x[j].length != DN[j]) {
                System.err.println("x[" + j + "].length != DN[" + j + "]");
                return false;
            }
            for (int i = 0; i < DN[j]; i++) {
                if (x[j][i] >= W) {
                    System.err.println("x[" + j + "][" + i + "] >= W");
                    return false;
                }
            }
        }
        if (m.length != D) {
            System.err.println("m.length != D");
            return false;
        }
        for (int j = 0; j < D; j++) {
            if (m[j] >= languages.length) {
                System.err.println("m[" + j + "] >= languages.length[=" + languages.length + "]");
                return false;
            }
        }
        if (mu.length != D) {
            System.err.println("mu.length != D");
            return false;
        }
        for (int j = 0; j < D; j++) {
            boolean selfFound = false;
            for (int k = 0; k < mu[j].length; k++) {
                if (mu[j][k] >= D) {
                    System.err.println("mu[" + j + "][" + k + "] >= D");
                    return false;
                }
                if (mu[j][k] == j) {
                    selfFound = true;
                }
            }
            if (!selfFound) {
                System.err.println("!selfFound @ " + j);
                return false;
            }
        }
        if (words.size() != W) {
            System.err.println("words.size() != W");
            return false;
        }
        Set<Integer> alreadySeen = new HashSet<Integer>();
        for (Integer i : words.values()) {
            if (alreadySeen.contains(i)) {
                System.err.println("alreadySeen contains " + i);
                return false;
            }
            if (i >= W) {
                System.err.println("i[=" + i + "] >= W");
                return false;
            }
            alreadySeen.add(i);
        }
        return true;
    }

    public void write(OutputStream outStream) {
        final PrintStream out = new PrintStream(outStream);
        out.println("LDA");
        out.println(W);
        out.println(D);
        for (Language lang : languages) {
            out.print(lang);
            out.print(" ");
        }
        out.println();
        for (int j = 0; j < D; j++) {
            printSingleDoc(out, j);
        }
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    private void printSingleDoc(final PrintStream out, int j) {
        printSingleDoc(out, j, null);
    }

    private void printSingleDoc(final PrintStream out, int j, int[] mapping) {
        out.println(m[j]);
        for (int i = 0; i < mu[j].length; i++) {
            if (mapping == null) {
                out.print(mu[j][i]);
            } else {
                out.print(mapping[mu[j][i]]);
            }
            out.print(" ");
        }
        out.println();
        for (int i = 0; i < DN[j]; i++) {
            out.print(x[j][i]);
            out.print(" ");
        }
        out.println();
    }

    public static GibbsInput read(InputStream inStream) throws GibbsFormatException, IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
        int lineNo = 0;
        String s;
        s = in.readLine();
        lineNo++;
        if (s == null) {
            throw new GibbsFormatException("Expected K");
        }
        s = in.readLine();
        lineNo++;
        if (s == null) {
            throw new GibbsFormatException("Expected W");
        }
        final int W = Integer.parseInt(s);
        s = in.readLine();
        lineNo++;
        if (s == null) {
            throw new GibbsFormatException("Expected D");
        }
        final int D = Integer.parseInt(s);
        s = in.readLine();
        lineNo++;
        if (s == null) {
            throw new GibbsFormatException("Expected languages");
        }
        String[] sl = s.split("\\s");
        final Language[] languages = new Language[sl.length];
        for (int i = 0; i < sl.length; i++) {
            languages[i] = Language.get(sl[i]);
        }
        final int[] m = new int[D];
        final int[][] mu = new int[D][];
        final int[][] x = new int[D][];
        final int[] DN = new int[D];
        for (int j = 0; j < D; j++) {
            s = in.readLine();
            lineNo++;
            if (s == null) {
                throw new GibbsFormatException("Expected m");
            }
            if (s.equals("")) {
                throw new GibbsFormatException("D value incorrect: expected " + D + " but section ended at " + j);
            }
            m[j] = Integer.parseInt(s);
            s = in.readLine();
            lineNo++;
            if (s == null) {
                throw new GibbsFormatException("Expected mu");
            }
            String[] ss = s.split("\\s");
            mu[j] = new int[ss.length];
            for (int i = 0; i < ss.length; i++) {
                mu[j][i] = Integer.parseInt(ss[i]);
            }
            s = in.readLine();
            lineNo++;
            if (s == null) {
                throw new GibbsFormatException("Expected x");
            }
            if (s.matches("\\s*")) {
                DN[j] = 0;
                x[j] = new int[0];
            } else {
                ss = s.split("\\s");
                DN[j] = ss.length;
                x[j] = new int[ss.length];
                for (int i = 0; i < ss.length; i++) {
                    try {
                        x[j][i] = Integer.parseInt(ss[i]);
                    } catch (NumberFormatException ex) {
                        throw new GibbsFormatException("Bad integer @ line " + lineNo);
                    }
                }
            }
        }
        Map<String, Integer> words = new HashMap<String, Integer>();
        while ((s = in.readLine()) != null) {
            lineNo++;
            if (s.matches("\\s*")) {
                continue;
            }
            String[] ss = s.split("\\s");
            if (ss.length != 2) {
                throw new GibbsFormatException("Bad line " + s);
            }
            words.put(ss[0], new Integer(ss[1]));
        }
        return new GibbsInput(D, W, DN, x, languages, m, mu, words);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GibbsInput other = (GibbsInput) obj;
        if (this.D != other.D) {
            return false;
        }
        if (this.W != other.W) {
            return false;
        }
        if (!Arrays.equals(this.DN, other.DN)) {
            return false;
        }
        if (!Arrays.deepEquals(this.x, other.x)) {
            return false;
        }
        if (!Arrays.deepEquals(this.languages, other.languages)) {
            return false;
        }
        if (!Arrays.equals(this.m, other.m)) {
            return false;
        }
        if (!Arrays.deepEquals(this.mu, other.mu)) {
            return false;
        }
        if (this.words != other.words && (this.words == null || !this.words.equals(other.words))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.D;
        hash = 97 * hash + this.W;
        hash = 97 * hash + Arrays.hashCode(this.DN);
        hash = 97 * hash + Arrays.deepHashCode(this.x);
        hash = 97 * hash + Arrays.deepHashCode(this.languages);
        hash = 97 * hash + Arrays.hashCode(this.m);
        hash = 97 * hash + Arrays.deepHashCode(this.mu);
        hash = 97 * hash + (this.words != null ? this.words.hashCode() : 0);
        return hash;
    }

    /**
     * Read the bag-of-words format from University of California Irving
     */
    public static GibbsInput readUCIFormat(File dataFile, File vocabFile) throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(dataFile));
        final int D = Integer.parseInt(in.readLine());
        final int W = Integer.parseInt(in.readLine());
        in.readLine(); // ignore NNZ
        String s;
        int docNo = 0;
        final LinkedList<String[]> stack = new LinkedList<String[]>();
        int DN_j = 0;
        final List<int[]> x = new ArrayList<int[]>();
        final List<Integer> DN = new ArrayList<Integer>();
        while ((s = in.readLine()) != null) {
            if (s.matches("\\s*")) {
                continue;
            }
            final String[] ss = s.split("\\s+");
            if (ss.length != 3) {
                throw new RuntimeException("Bad line: " + s);
            }
            int j = Integer.parseInt(ss[0]);
            if (j == docNo) {
                stack.push(ss);
                DN_j += Integer.parseInt(ss[2]);
            } else {
                int[] x_j = new int[DN_j];
                int i = 0;
                for (String[] ss2 : stack) {
                    final int w = Integer.parseInt(ss2[1]);
                    for (int m = i; m < i + Integer.parseInt(ss2[2]); m++) {
                        x_j[m] = w;
                    }
                    i += Integer.parseInt(ss2[2]);
                }
                x.add(x_j);
                DN.add(DN_j);
                stack.clear();
                DN_j = Integer.parseInt(ss[2]);
                docNo = j;
                stack.push(ss);
            }

        }
        final BufferedReader vin = new BufferedReader(new FileReader(vocabFile));
        final Map<String, Integer> words = new HashMap<String, Integer>();
        int i = 1;
        while ((s = vin.readLine()) != null) {
            words.put(s.trim(), i++);
        }
        int[][] mu = new int[docNo][1];
        for (int j = 0; j < docNo; j++) {
            mu[j][0] = j;
        }
        int[] DNarray = new int[DN.size()];
        for (int n = 0; n < DN.size(); n++) {
            DNarray[n] = DN.get(n);
        }
        return new GibbsInput(D, W, DNarray, x.toArray(new int[x.size()][]),
                new Language[]{Language.ENGLISH},
                new int[docNo], mu, words);
    }

    public void printParallel(Language lang1, Language lang2, PrintWriter out) {
        int l1 = -1, l2 = -1;
        for (int i = 0; i < languages.length; i++) {
            if (lang1.equals(languages[i])) {
                l1 = i;
            }
            if (lang2.equals(languages[i])) {
                l2 = i;
            }
        }
        if (l1 == -1 || l2 == -1) {
            throw new IllegalArgumentException();
        }
        for (int j = 0; j < D; j++) {
            if (m[j] == l1) {
                int j2 = -1;
                for (int j3 : mu[j]) {
                    if (m[j3] == l2) {
                        j2 = j3;
                    }
                }
                if (j2 != -1) {
                    for (int i = 0; i < DN[j]; i++) {
                        out.print(x[j][i] + " ");
                    }
                    out.println();
                    for (int i = 0; i < DN[j2]; i++) {
                        out.print(x[j2][i] + " ");
                    }
                    out.println();
                }
            }
        }
        out.println();
        for (String word : words.keySet()) {
            out.println(word + " " + words.get(word));
        }
    }

    public void printSplit(int splitSize, PrintStream out1, PrintStream out2) {
        int i = 0;
        boolean endLang0 = false;
        final HashSet<Integer> inSet1 = new HashSet<Integer>();
        final HashSet<Integer> inSet2 = new HashSet<Integer>();
        out1.println("LDA");
        out2.println("LDA");
        out1.println(W);
        out2.println(W);
        final int[] remappedIDs = new int[D];
        int D1 = 0, D2 = 0;
        // Do a dry-run to recalcuate the document mappings
        for (int j = 0; j < D; j++) {
            if (m[j] != 0 && !endLang0) {
                endLang0 = true;
                if (inSet1.contains(j)) {
                    remappedIDs[j] = D1;
                    D1++;
                } else if (inSet2.contains(j)) {
                    remappedIDs[j] = D2;
                    D2++;
                }
            } else if (!endLang0) {
                if (j % splitSize == 0) {
                    for (int j2 : mu[j]) {
                        inSet2.add(j2);
                    }
                    remappedIDs[j] = D2;
                    D2++;
                } else {
                    for (int j2 : mu[j]) {
                        inSet1.add(j2);
                    }
                    remappedIDs[j] = D1;
                    D1++;
                }
            } else if (m[j] != 0) {
                if (inSet1.contains(j)) {
                    remappedIDs[j] = D1;
                    D1++;
                } else if (inSet2.contains(j)) {
                    remappedIDs[j] = D2;
                    D2++;
                }
            } else {
                throw new RuntimeException("Language block not consistent... reprogram this method, John");
            }
        }
        out1.println(D1);
        out2.println(D2);
        for (Language lang : languages) {
            out1.print(lang);
            out2.print(lang);
            out1.print(" ");
            out2.print(" ");
        }
        out1.println();
        out2.println();
        endLang0 = false;
        for (int j = 0; j < D; j++) {
            if (m[j] != 0 && !endLang0) {
                endLang0 = true;
                if (inSet1.contains(j)) {
                    printSingleDoc(out1, j, remappedIDs);
                } else if (inSet2.contains(j)) {
                    printSingleDoc(out2, j, remappedIDs);
                }
            } else if (!endLang0) {
                if (j % splitSize == 0) {
                    printSingleDoc(out2, j, remappedIDs);
                } else {
                    printSingleDoc(out1, j, remappedIDs);
                }
            } else if (m[j] != 0) {
                if (inSet1.contains(j)) {
                    printSingleDoc(out1, j, remappedIDs);
                } else if (inSet2.contains(j)) {
                    printSingleDoc(out2, j, remappedIDs);
                }
            } else {
                throw new RuntimeException("Language block not consistent... reprogram this method, John");
            }
        }
        out1.println();
        out2.println();
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            out1.println(entry.getKey() + " " + entry.getValue());
            out2.println(entry.getKey() + " " + entry.getValue());
        }
    }
}
