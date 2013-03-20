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
package eu.monnetproject.bliss.betalm;

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.PTBTokenizer;
import eu.monnetproject.bliss.Tokenizer;
import eu.monnetproject.bliss.betalm.impl.ARPALM;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author John McCrae
 */
public class Perplexity {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);

        final File doc = opts.roFile("document", "The document on which to estimate the perplexity");

        final File lmFile = opts.roFile("lm", "The language model file");

        if (!opts.verify(Perplexity.class)) {
            return;
        }

        final ARPALM lm = new ARPALM(lmFile);

        final Scanner scanner = new Scanner(doc);

        double perplexity = calculatePerplexity(scanner, lm);

        System.err.println("Log2 Perplexity=" + (perplexity ));
    }
    private static final double LOG_10_2 = 0.3010299956639812;

    public static double calculatePerplexity(final Scanner scanner, final ARPALM lm) throws FileNotFoundException {
        double perplexity = 0.0;
        double bo = 0;
        double unk = 0;
        int docs = 0;
        
        for (int i = 1; i <= lm.n; i++) {
            System.err.print("Creating " + i + "-gram index...");
            lm.ngramIdx(i);
            System.err.println("done");
        }

        final Tokenizer tokenizer = new PTBTokenizer();

        while (scanner.hasNextLine()) {
            final List<String> tokens = tokenizer.tokenize(scanner.nextLine());

            final StringBuffer sb = new StringBuffer();

            int m = 0;

            double p = 0.0;
            double u = 0;
            double b = 0;

            for (String token : tokens) {
                if (m == lm.n) {
                    sb.replace(0, sb.indexOf(" ") + 1, "");
                    m--;
                }
                if (sb.length() != 0) {
                    sb.append(" ");
                }
                sb.append(token);
                m++;
                final int unkScore = lm.unk(sb.toString(), m);
                if (unkScore == -m) {
                    u++;
                }
                b += unkScore;

                p += lm.score(sb.toString(), m, -10);

            }
            perplexity += p / tokens.size();
            unk += u;
            bo += b;
            System.err.print(".");
            docs++;
        }
        System.err.println();
        System.err.println("DOCS="+docs);
        System.err.println("UNK=" + unk/docs);
        System.err.println("BACKOFFS=" + (-bo/docs));
        return perplexity/ LOG_10_2;
    }
}
