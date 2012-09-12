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
package betalm;

import eu.monnetproject.translation.topics.PTBTokenizer;
import eu.monnetproject.translation.topics.SparseArray;
import eu.monnetproject.translation.topics.SparseRealArray;
import eu.monnetproject.translation.topics.Tokenizer;
import eu.monnetproject.translation.topics.sim.BetaLMImpl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Runner for BetaLM process. The following are assumed to be the input
 *
 * <ol> <li> A source corpus consisting of explicit topics split by the
 * following code http://medialab.di.unipi.it/wiki/Wikipedia_Extractor <li> A
 * target corpus consisting of explicit topics split by the following code
 * http://medialab.di.unipi.it/wiki/Wikipedia_Extractor <li> A document to adapt
 * the topic model to <li> Degree of language model to build <li> The output
 * file path for the language model </ol>
 *
 * @author John McCrae
 */
public class BetaLM {

    private static final Tokenizer tokenizer = new PTBTokenizer();

    private static void fail(String message) {
        System.err.println(message);
        System.err.println();
        System.err.println("Usage:\n\tbetalm [-m METHOD] corpusSrc corpusTrg query n lm-file");
        System.exit(-1);
    }

    public static void main(String[] args) throws Exception {
        final ArrayList<String> argList = new ArrayList<String>(Arrays.asList(args));
        for (int i = 0; i < argList.size(); i++) {
            if (argList.get(i).equals("-m")) {
                try {
                    BetaLMImpl.Method.valueOf(argList.get(i + 1));
                } catch (IllegalArgumentException x) {
                    System.err.println("Invalid method: " + argList.get(i + 1) + "\nSupported:\n");
                    for (BetaLMImpl.Method method : BetaLMImpl.Method.values()) {
                        System.err.println("\t" + method.name());
                    }
                    fail("");
                }
                System.setProperty("paraSimMethod", argList.get(i + 1));
                argList.remove(i);
                argList.remove(i);
                i--;
            }
        }
        if (argList.size() != 5) {
            fail("Wrong number of arguments");
        }
        final File corpusSrc = new File(argList.get(0));
        if (!corpusSrc.exists() || !corpusSrc.canRead()) {
            fail("Could not access corpus");
        }
        final File corpusTrg = new File(argList.get(0));
        if (!corpusTrg.exists() || !corpusTrg.canRead()) {
            fail("Could not access corpus");
        }
        final File query = new File(argList.get(1));
        if (!query.exists() || !query.canRead()) {
            fail("Could not access query");
        }
        final int N = Integer.parseInt(argList.get(2));
        final File lmFile = new File(argList.get(3));
        if (lmFile.exists() && !lmFile.canWrite()) {
            fail("Output file exists and is not writable");
        }
        System.err.println("Building word map");
        final Map<String, Integer> wordMap = new HashMap<String, Integer>();
        final int W = buildWordMap(wordMap, corpusTrg, buildWordMap(wordMap, corpusTrg, 1));

        System.err.println("Total words: " + W);

        StringBuilder header = new StringBuilder();

        final PrintWriter out = new PrintWriter(lmFile);

        for (int n = 1; n <= N; n++) {
            final List<SparseArray> srcNgrams = buildNGramFrequencies(corpusSrc, wordMap, W, n);
            final List<SparseArray> trgNgrams = buildNGramFrequencies(corpusSrc, wordMap, W, n);
            final SparseArray[][] parallelCorpus = pairCorpora(srcNgrams, trgNgrams);
            final List<SparseArray> queryNgrams = buildNGramFrequencies(query, wordMap, W, n);
            if(queryNgrams.size() > 1) {
                System.err.println("WARNING: Query contained multiple documents, using only first");
            } else if(queryNgrams.isEmpty()) {
                fail("Query document empty");
            }
            final BetaLMImpl betaLMImpl = new BetaLMImpl(parallelCorpus, W, n);
            final SparseRealArray predictedModel = betaLMImpl.predictedModel(queryNgrams.get(0));
            
        }
    }

    private static int buildWordMap(Map<String, Integer> wordMap, File corpus, int W_initial) throws FileNotFoundException {
        assert(W_initial > 0);
        final Scanner scanner = new Scanner(corpus).useDelimiter("\r?\n");
        int W = W_initial;
        while (scanner.hasNext()) {
            final String line = scanner.next();
            if (line.matches("</?doc.*")) {
                continue;
            }
            final List<String> tokens = tokenizer.tokenize(line);
            for (String token : tokens) {
                if (!wordMap.containsKey(token)) {
                    wordMap.put(token, W++);
                }
            }
        }
        return W+1;
    }
    
    private static List<SparseArray> buildNGramFrequencies(File corpus, Map<String, Integer> wordMap, int W, int n) throws FileNotFoundException {
        final ArrayList<SparseArray> freqList = new ArrayList<SparseArray>();
        final int Wn1 = intpow(W, n - 1);
        final int Wn = intpow(W, n);
        SparseArray current = new SparseArray(Wn);
        freqList.add(current);
        final Scanner scanner = new Scanner(corpus).useDelimiter("\r?\n");
        int v = 0;

        while (scanner.hasNext()) {
            final String line = scanner.next();
            if (line.startsWith("<doc")) {
                if(!current.isEmpty()) {
                    current = new SparseArray(Wn);
                    freqList.add(current);
                }
                continue;
            }
            if (line.startsWith("</doc")) {
                current = null;
                v = 0;
                continue;
            }
            if (current != null) {
                final List<String> tokens = tokenizer.tokenize(line);
                for (String token : tokens) {
                    final int wordIdx = wordMap.get(token);
                    final int ngramIdx = v % Wn1 * W + wordIdx;
                    current.inc(ngramIdx);
                }
            }
        }
        return freqList;
    }
    
    private static SparseArray[][] pairCorpora(List<SparseArray> src, List<SparseArray> trg) {
        if(src.size() != trg.size()) {
            throw new IllegalArgumentException("Source and target corpora differ in length");
        }
        final SparseArray[][] parallelCorpus = new SparseArray[src.size()][];
        final Iterator<SparseArray> srcIter = src.iterator();
        final Iterator<SparseArray> trgIter = trg.iterator();
        int i = 0;
        while(srcIter.hasNext()) {
            parallelCorpus[i++] = new SparseArray[] { srcIter.next(), trgIter.next() };
        }
        return parallelCorpus;
    }

    private static int intpow(int W, int n) {
        int w = 1;
        for(int i = 0; i < n; i++) {
            w *= W;
        }
        return w;
    }
}
