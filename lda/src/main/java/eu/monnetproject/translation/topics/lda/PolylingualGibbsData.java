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
package eu.monnetproject.translation.topics.lda;

import eu.monnetproject.lang.Language;
import eu.monnetproject.math.sparse.SparseIntArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class PolylingualGibbsData {

    public final int[][] N_lk;
    public final SparseIntArray[][] N_lkw;
    public final int K, W, D;
    public final double alpha, beta;
    //public final double[][][] phi;
    public final double[][] theta;
    public final Language[] languages;
    private final int L;
    public final Map<String, Integer> words;

    public PolylingualGibbsData(int[][] N_lk, SparseIntArray[][] N_lkw, int K, int W, int D, double alpha, double beta, double[][][] phi, double[][] theta, Language[] languages, Map<String, Integer> words) {
        this.N_lk = N_lk;
        this.N_lkw = N_lkw;
        this.K = K;
        this.W = W;
        this.D = D;
        this.alpha = alpha;
        this.beta = beta;
        //this.phi = phi;
        this.theta = theta;
        this.languages = languages;
        this.L = languages.length;
        this.words = words;
    }

    private int[] words_total;
    
    public int getWordsTotal(int l) {
        if(words_total == null) {
            words_total = new int[l];
            for(int l1 = 0; l1 < L; l1++) {
                for(int k = 0; k < K; k++) {
                    words_total[l1] += N_lk[l1][k];
                }
            }
        }
        return words_total[l];
    }
    
    public void write(OutputStream outStream) {
        PrintStream out;
        if (outStream instanceof PrintStream) {
            out = (PrintStream) outStream;
        } else {
            out = new PrintStream(outStream);
        }
        out.println(K);
        out.println(W);
        out.println(D);
        out.println(L);
        out.println(alpha);
        out.println(beta);
        int l = 0;
        for (Language lang : languages) {
            out.println(lang.toString());
            for (int k = 0; k < K; k++) {
                out.print(N_lk[l][k]);
                out.print(" ");
            }
            out.println();
            for (int k = 0; k < K; k++) {
                out.println(N_lkw[l][k]);
            }
            for (int w = 0; w < W; w++) {
                for (int k = 0; k < K; k++) {
                    //out.print(phi[l][w][k]);
                    out.print(" ");
                }
                out.println();
            }
            l++;
        }
        for (int k = 0; k < K; k++) {
            for (int j = 0; j < D; j++) {
                out.print(theta[k][j]);
                out.print(" ");
            }
            out.println();
        }
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    public static PolylingualGibbsData read(InputStream inStream) throws GibbsFormatException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
        String s;
        s = reader.readLine();
        if (s == null) {
            throw new GibbsFormatException("No K");
        }
        int K = Integer.parseInt(s);

        s = reader.readLine();
        if (s == null) {
            throw new GibbsFormatException("No W");
        }
        int W = Integer.parseInt(s);

        s = reader.readLine();
        if (s == null) {
            throw new GibbsFormatException("No D");
        }
        int D = Integer.parseInt(s);

        s = reader.readLine();
        if (s == null) {
            throw new GibbsFormatException("No L");
        }
        int L = Integer.parseInt(s);

        s = reader.readLine();
        if (s == null) {
            throw new GibbsFormatException("No alpha");
        }
        double alpha = Double.parseDouble(s);

        s = reader.readLine();
        if (s == null) {
            throw new GibbsFormatException("No beta");
        }
        double beta = Double.parseDouble(s);

        Language[] languages = new Language[L];

        int[][] N_lk = new int[L][];
        SparseIntArray[][] N_lkw = new SparseIntArray[L][];
        //double[][][] phi = new double[L][][];
        for (int l = 0; l < L; l++) {
            s = reader.readLine();
            if (s == null) {
                throw new GibbsFormatException("Expected a language");
            }
            languages[l] = Language.get(s);
            N_lk[l] = new int[K];
            s = reader.readLine();
            if (s == null) {
                throw new GibbsFormatException("No N_k");
            }
            String[] N_k_str = s.split(" ");
            if (N_k_str.length != K) {
                throw new GibbsFormatException("N_k length is not K");
            }
            for (int k = 0; k < K; k++) {
                N_lk[l][k] = Integer.parseInt(N_k_str[k]);
            }

            N_lkw[l] = new SparseIntArray[K];
            for(int k = 0; k < K; k++) {
                s = reader.readLine();
                if (s == null) {
                    throw new GibbsFormatException("No N_wk @ index" + k);
                }
                N_lkw[l][k] = SparseIntArray.fromString(s, W, 0);
            }

            //phi[l] = new double[W][];
            for (int w = 0; w < W; w++) {
                s = reader.readLine();
                if (s == null) {
                    throw new GibbsFormatException("No phi @ index" + w);
                }
                //phi[l][w] = new double[K];
               // String[] phi_string = s.split(" ");
              //  for (int k = 0; k < K; k++) {
                    //phi[l][w][k] = Double.parseDouble(phi_string[k]);
               // }
            }
        }

        double[][] theta = new double[K][D];
        for (int k = 0; k < K; k++) {
            s = reader.readLine();
            if (s == null) {
                throw new GibbsFormatException("No theta @ index" + k);
            }
            String[] theta_string = s.split(" ");
            for (int j = 0; j < D; j++) {
                theta[k][j] = Double.parseDouble(theta_string[j]);
            }
        }
        Map<String, Integer> words = new HashMap<String, Integer>();
        while ((s = reader.readLine()) != null) {
            String[] ss = s.split("\\s");
            if (ss.length != 2) {
                throw new GibbsFormatException("Bad line: " + s);
            }
            words.put(ss[0], Integer.parseInt(ss[1]));
        }

        return new PolylingualGibbsData(N_lk, N_lkw, K, W, D, alpha, beta, null,/*phi,*/ theta, languages, words);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PolylingualGibbsData other = (PolylingualGibbsData) obj;
        if (!Arrays.deepEquals(this.N_lk, other.N_lk)) {
            return false;
        }
        if (!Arrays.deepEquals(this.N_lkw, other.N_lkw)) {
            return false;
        }
        if (this.K != other.K) {
            return false;
        }
        if (this.W != other.W) {
            return false;
        }
        if (this.D != other.D) {
            return false;
        }
        if (Double.doubleToLongBits(this.alpha) != Double.doubleToLongBits(other.alpha)) {
            return false;
        }
        if (Double.doubleToLongBits(this.beta) != Double.doubleToLongBits(other.beta)) {
            return false;
        }
        //if (!Arrays.deepEquals(this.phi, other.phi)) {
        //    return false;
        //}
        if (!Arrays.deepEquals(this.theta, other.theta)) {
            return false;
        }
        if (!Arrays.deepEquals(this.languages, other.languages)) {
            return false;
        }
        if (this.words != other.words && (this.words == null || !this.words.equals(other.words))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Arrays.deepHashCode(this.N_lk);
        hash = 67 * hash + Arrays.deepHashCode(this.N_lkw);
        hash = 67 * hash + this.K;
        hash = 67 * hash + this.W;
        hash = 67 * hash + this.D;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.alpha) ^ (Double.doubleToLongBits(this.alpha) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.beta) ^ (Double.doubleToLongBits(this.beta) >>> 32));
        //hash = 67 * hash + Arrays.deepHashCode(this.phi);
        hash = 67 * hash + Arrays.deepHashCode(this.theta);
        hash = 67 * hash + Arrays.deepHashCode(this.languages);
        hash = 67 * hash + (this.words != null ? this.words.hashCode() : 0);
        return hash;
    }

    public double phi(int l, int w, int k) {
        return ((double)N_lkw[l][k].get(w) + beta) / ((double)N_lk[l][k] + W * beta); 
    }
    
    public GibbsData monolingual(int l) {
        return new GibbsData(N_lk[l], N_lkw[l], K, W, D, alpha, beta, null/*phi[l]*/, theta);
    }
}
