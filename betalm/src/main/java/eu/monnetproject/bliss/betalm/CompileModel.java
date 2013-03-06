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

import eu.monnetproject.bliss.betalm.COCAndMean.Data;
import eu.monnetproject.bliss.betalm.impl.BetaLMImpl;
import eu.monnetproject.bliss.betalm.impl.BetaSimFunction;
import eu.monnetproject.bliss.betalm.impl.Metrics;
import eu.monnetproject.bliss.betalm.impl.SalienceMetric;
import eu.monnetproject.bliss.betalm.impl.StopWordList;
import eu.monnetproject.bliss.betalm.impl.StopWordListImpl;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.WordMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.DataInputStream;
import java.io.EOFException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import static java.lang.Math.*;
import java.util.Comparator;
import java.util.TreeSet;

/**
 *
 * @author jmccrae
 */
public class CompileModel {

    private static final int H = 4;

    private static IntSet readQueryDoc(File queryDoc) throws IOException {
        if (queryDoc == null) {
            return new IntRBTreeSet();
        }
        final DataInputStream in = new DataInputStream(new FileInputStream(queryDoc));
        final IntRBTreeSet querySet = new IntRBTreeSet();
        while (true) {
            try {
                querySet.add(in.readInt());
            } catch (EOFException x) {
                break;
            }
        }
        return querySet;
    }

    public enum Smoothing {

        NONE,
        ADD_ALPHA,
        GOOD_TURING,
        KNESER_NEY
    };

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);

        StringBuilder betalmString = new StringBuilder("The BetaLM method: ");
        for (BetaLMImpl.Method method : BetaLMImpl.Method.values()) {
            betalmString.append(method.name()).append(" ");
        }

        final BetaLMImpl.Method betaMethod = opts.enumOptional("b", BetaLMImpl.Method.class, null, betalmString.toString());

        final SourceType sourceType = opts.enumOptional("t", SourceType.class, SourceType.FIRST, "The type of source: SIMPLE, FIRST or SECOND");

        final Smoothing smoothing = opts.enumOptional("smooth", Smoothing.class, Smoothing.NONE, "The type of smoothing: NONE, ADD_ALPHA, GOOD_TURING, KNESER_NEY");

        final File queryFile = opts.roFile("f", "The query file (ontology)", null);

        final double smoothness = opts.doubleValue("s", 1.0, "The selective smoothing parameter");

        final double alpha = opts.doubleValue("a", 0.0, "The minimal smoothing parameter");

        final int salience = opts.intValue("salience", "The salience (filtering on query document)", -1);

        final int stopWordCount = opts.intValue("stop", "The number of stop words to ignore", 150);
        
        final boolean writeDocs = opts.flag("writeDocs", "Write documents in corpus with ranking");

        final File inFile = opts.roFile("corpus[.gz|.bz2]", "The corpus file");

        final int N = opts.nonNegIntValue("N", "The largest n-gram to calculate");

        final File wordMapFile = opts.roFile("wordMap", "The word map file");

        final File freqFile = opts.roFile("freqs", "The frequency file for the corpus");

        final PrintStream out = opts.outFileOrStdout();

        if (!opts.verify(CompileModel.class)) {
            return;
        }



        System.err.println("Loading wordmap");
        final int W = WordMap.calcW(wordMapFile);

        final String[] words = WordMap.inverseFromFile(wordMapFile, W, true);

        System.err.println("Calculating stopwords");
        final StopWordList stopwords = stopWordList(freqFile, stopWordCount);

        System.err.println("Initializing Adaptive scorer");
        final BetaSimFunction beta;
        if (betaMethod != null) {
            final double mean = CalculateBetaMean.calcBetaMean(makeFunction(betaMethod, inFile, W, sourceType, queryFile, stopwords, smoothness, 0.0, 1.0, salience), inFile, sourceType, W);
            System.err.println("mean=" + mean);
            beta = makeFunction(betaMethod, inFile, W, sourceType, queryFile, stopwords, smoothness, alpha, mean, salience);
        } else {
            DoCount.queryFile = readQueryDoc(queryFile);
            beta = new DefaultBetaFunction();
        }


        System.out.println("1. Count the corpus");
        final File[] countFiles = initCountFiles(N);
        if(System.getProperty("writeDocs") != null || writeDocs) {
            DoCount.wordMap = words;
            DoCount.docRanking = new PrintWriter("rankedDocs");
            DoCount.foreignDocRanking = new PrintWriter("translatedDocs");
            //DoCount.queryFile = readQueryDoc(queryFile);
            DoCount.stopWordList = stopwords;
            //DoCount.values = PrecomputedValues.precompute(inFile, W, sourceType);
        }
        DoCount.doCount(inFile, N, openFiles(countFiles), beta, sourceType, W);
        if(System.getProperty("writeDocs") != null || writeDocs) {
            DoCount.foreignDocRanking.flush();
            DoCount.foreignDocRanking.close();
            DoCount.docRanking.flush();
            DoCount.docRanking.close();
        }

        System.out.println("2. Sort each file");
        final File[] sortedFiles = new File[N * 2];
        for (int i = 0; i < countFiles.length; i++) {
            if (countFiles[i] != null) {
                System.out.println(countFiles[i].getPath());
                sortedFiles[i] = sort(countFiles[i]);
            }
        }

        System.out.println("3. Accumulating unique values");
        final File[] uniqFiles = new File[N];
        for (int i = 0; i < N; i++) {
            final File uniqFile = new File(countFiles[i].getPath() + "uniq");
            uniqFile.deleteOnExit();
            Uniq.uniq(sortedFiles[i], new PrintStream(uniqFile));
            uniqFiles[i] = uniqFile;
        }

        System.out.println("4. Counting diversity of histories");
        final File[] divHistFiles = new File[N - 1];
        for (int i = 1; i < N; i++) {
            final File divHistFile = new File(countFiles[i - 1].getPath() + "divHist");
            divHistFile.deleteOnExit();
            Hist.hist(uniqFiles[i], false, new PrintStream(divHistFile));
            divHistFiles[i - 1] = divHistFile;
        }
        final File[] divFutFiles = new File[N - 1];
        for (int i = 1; i < N; i++) {
            final File divFutFile = new File(countFiles[i - 1].getPath() + "divFut");
            divFutFile.deleteOnExit();
            Hist.hist(uniqFiles[i], true, new PrintStream(divFutFile));
            divFutFiles[i - 1] = divFutFile;
        }

        System.out.println("5. Counting counts");
        int[][] CoC = new int[N][H];
        double[] r = new double[N];
        for (int i = 0; i < N; i++) {
            final Data d = COCAndMean.calculate(new Scanner(uniqFiles[i]), H);
            CoC[i] = d.CoC;
            r[i] = d.mean;
        }

        System.out.println("6. Unigram sum");
        double unigramSum = 0;
        {
            final Scanner unigramIn = new Scanner(uniqFiles[0]);
            while (unigramIn.hasNextLine()) {
                final String line = unigramIn.nextLine();
                if (line.length() > 0) {
                    final String[] elems = line.split(" ");
                    unigramSum += Double.parseDouble(elems[elems.length - 2]);
                }
            }
            unigramIn.close();
        }

        int[] countOfNGrams = new int[N];

        final Int2DoubleMap unigramScores = new Int2DoubleArrayMap(W);
        System.out.println("7. Unigram probs");
        final File[] ngrams = new File[N];
        {
            final Scanner unigramIn = new Scanner(uniqFiles[0]);
            ngrams[0] = new File(uniqFiles[0].getPath() + "ngrams");
            ngrams[0].deleteOnExit();
            final PrintWriter unigramOut = new PrintWriter(ngrams[0]);
            while (unigramIn.hasNextLine()) {
                final String line = unigramIn.nextLine();
                final String[] elems = line.split(" ");
                if (elems.length < 2) {
                    continue;
                }
                final double[] score = {log10(Double.parseDouble(elems[elems.length - 2])) - log10(unigramSum)};
                final String ngram = makeNgram(elems, 2, words);
                if (score.length == 1) {
                    unigramOut.println(ngram + "\t" + score[0]);
                } else {
                    unigramOut.println(ngram + "\t" + score[0] + "\t" + score[1]);
                }
                unigramScores.put(Integer.parseInt(elems[0].replaceAll("[\\[\\], ]", "")), score[0]);
                countOfNGrams[0]++;
            }
            unigramOut.flush();
            unigramOut.close();
            unigramIn.close();
        }


        System.out.println("8. Calculating probabilities");
        for (int i = 1; i < N; i++) {
            final Scanner uniqIn = new Scanner(uniqFiles[i]);
            final Scanner divHistIn = new Scanner(divHistFiles[i - 1]);
            ngrams[i] = new File(uniqFiles[i].getPath() + "ngrams");
            ngrams[i].deleteOnExit();
            final PrintWriter ngramOut = new PrintWriter(ngrams[i]);
            double sum = 0;
            String currentHist = null;
            while (uniqIn.hasNextLine()) {
                final String line = uniqIn.nextLine();
                final String[] elems = line.split(" ");
                if (elems.length < 2) {
                    continue;
                }

                if ((currentHist == null || !line.startsWith(currentHist)) && divHistIn.hasNextLine()) {
                    final String dhLine = divHistIn.nextLine();
                    currentHist = dhLine.substring(0, dhLine.indexOf("]")) + ",";
                    final String[] dhElems = dhLine.split(" ");
                    if (dhElems.length > 2) {
                        sum = Double.parseDouble(dhElems[dhElems.length - 1]);
                    } else {
                        System.err.println("dhLine:" + dhLine);
                    }
                }

                if (!line.startsWith(currentHist)) {
                    throw new RuntimeException();
                }
                final double[] score = {log10(Double.parseDouble(elems[elems.length - 2])) - log10(sum)};
                final int w = Integer.parseInt(elems[i].replaceAll("[\\[\\], ]", ""));
                if (score[0] > unigramScores.get(w)) {
                    final String ngram = makeNgram(elems, 2, words);
                    if (score.length == 1) {
                        ngramOut.println(ngram + "\t" + score[0]);
                    } else {
                        ngramOut.println(ngram + "\t" + score[0] + "\t" + score[1]);
                    }
                    countOfNGrams[i]++;
                }
            }
            ngramOut.flush();
            ngramOut.close();
            uniqIn.close();
        }

        System.out.println("9. Writing n-gram file");
        out.println();
        out.println("\\data\\");
        for (int i = 0; i < N; i++) {
            out.println("ngram " + (i + 1) + "=" + countOfNGrams[i]);
        }
        out.println();


        for (int i = 0; i < N; i++) {
            out.println("\\" + (i + 1) + "-grams:");
            final File sortedNGrams = sort(ngrams[i]);
            final Scanner sortedNGramsIn = new Scanner(sortedNGrams);
            while (sortedNGramsIn.hasNextLine()) {
                final String line = sortedNGramsIn.nextLine();
                final String[] elems = line.split("\t");
                if (elems.length == 2) {
                    out.println(elems[1] + "\t" + elems[0]);
                } else if (elems.length == 3) {
                    out.println(elems[1] + "\t" + elems[0] + "\t" + elems[2]);
                }
            }
            sortedNGramsIn.close();
            out.println();
        }
        out.println("\\end\\");
        out.flush();
        out.close();
    }

    private static File[] initCountFiles(final int N) throws IOException {
        final File[] countFiles = new File[N * 2];
        for (int n = 0; n < N; n++) {
            final File countFile = File.createTempFile("counts", "." + n);
            countFile.deleteOnExit();
            countFiles[n] = countFile;
            if (n > 0) {
                final File histFile = File.createTempFile("counts", ".h" + n);
                histFile.deleteOnExit();
                countFiles[n + N] = histFile;
            }
        }
        return countFiles;
    }

    private static StopWordList stopWordList(final File freqs, int stopWordCount) throws IOException {
        final DataInputStream in = new DataInputStream(new FileInputStream(freqs));
        final TreeSet<int[]> set = new TreeSet<int[]>(new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                if (o1[1] < o2[1]) {
                    return -1;
                } else if (o1[1] > o2[1]) {
                    return 1;
                } else {
                    return o1[0] - o2[0];
                }
            }
        });

        int w = 0;
        in.readInt(); // Ignore as w=0 is end-of-record
        while (true) {
            try {
                w++;
                int f = in.readInt();
                if (set.size() < stopWordCount) {
                    set.add(new int[]{w, f});
                } else if (set.first()[1] < f) {
                    set.remove(set.first());
                    set.add(new int[]{w, f});
                }
            } catch (EOFException x) {
                break;
            }
        }
        final StopWordList stopwords = new StopWordListImpl();
        for (int[] is : set) {
            stopwords.add(is[0]);
        }
        return stopwords;
    }

    private static PrintWriter[] openFiles(final File[] files) throws IOException {
        final PrintWriter[] pws = new PrintWriter[files.length];
        for (int i = 0; i < files.length; i++) {
            if (files[i] != null) {
                pws[i] = new PrintWriter(files[i]);
            }
        }
        return pws;
    }

    private static File sort(final File file) throws Exception {
        final File outFile = new File(file.getPath() + "sorted");
        outFile.deleteOnExit();
        try {
            Runtime.getRuntime().exec(new String[]{"sort", file.getPath(), "-o", outFile.getPath()}, new String[]{"LC_ALL=C"}).waitFor();
        } catch (IOException x) {
            System.err.println(x.getMessage());
            // In-memory sort :( Mostly just to ensure unit tests pass on non-Unix machines
            final ArrayList<String> lines = new ArrayList<String>();
            final Scanner in = new Scanner(file);
            while (in.hasNextLine()) {
                lines.add(in.nextLine());
            }
            in.close();
            Collections.sort(lines);
            final PrintWriter out = new PrintWriter(outFile);
            for (String line : lines) {
                out.println(line);
            }
            out.flush();
            out.close();
        }
        return outFile;
    }

    private static String makeNgram(String[] elems, int i, String[] words) {
        final StringBuilder sb = new StringBuilder();
        for (int j = 0; j < elems.length - i; j++) {
            final int w = Integer.parseInt(elems[j].replaceAll("[\\[\\], ]", ""));
            if (w > words.length) {
                sb.append("<UNK>");
            } else {
                sb.append(words[w]);
            }
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static BetaSimFunction betaSimFunction(BetaLMImpl.Method method, Vector<Integer> query, PrecomputedValues precomp, StopWordList stopwords) throws IOException {
        switch (method) {
            case COS_SIM:
                return Metrics.cosSim(query, stopwords);
            case DF_DICE:
                return Metrics.dfDiceCoefficient(query, precomp.df, stopwords);
            case DF_JACCARD:
                return Metrics.dfJaccardCoefficient(query, precomp.df, stopwords);
            case DICE:
                return Metrics.diceCoefficient(query, stopwords);
            case JACCARD:
                return Metrics.jaccardIndex(query, stopwords);
            case KLD:
                return Metrics.kullbackLeiblerDivergence(query, stopwords);
            case NORMAL_COS_SIM:
                return Metrics.normalCosSim(query, precomp.mu, precomp.sumMu2, stopwords);
            case ROGERS_TANIMOTO:
                return Metrics.rogersTanimoto(query, stopwords);
            case SALIENCE:
                return SalienceMetric.fromFile(new File("../wiki/en-es/sample.uc.ifrs-sal-es"));
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static BetaSimFunction makeFunction(BetaLMImpl.Method betaMethod, File inFile, int W, SourceType sourceType, File queryFile, StopWordList stopwords, double smoothness, double alpha, double mean, int salience) throws IOException {
        if (betaMethod != null) {
            final PrecomputedValues precomp;
            if (PrecomputedValues.isNecessary(betaMethod)) {
                System.err.println("Methodology requires pre-scan of the corpus");
                precomp = PrecomputedValues.precompute(inFile, W, sourceType);
            } else {
                precomp = null;
            }
            if (queryFile == null) {
                System.err.println("BetaLM needs a query file");
                return null;
            }
            if (sourceType == SourceType.SIMPLE) {
                System.err.println("BetaLM needs a bilingual corpus, set -t");
                return new DefaultBetaFunction();
            }
            final Vector<Integer> binQuery;
            if (salience > 0) {
                final SparseIntArray binQuery2 = SparseIntArray.fromBinary(CLIOpts.openInputAsMaybeZipped(queryFile), Integer.MAX_VALUE);
                final IntSet salient = MostSalient.mostSalient(queryFile, inFile, W, salience, sourceType);
                DoCount.queryFile = salient;
                binQuery = MostSalient.filter(salient, binQuery2);
            } else {
                binQuery = SparseIntArray.fromBinary(CLIOpts.openInputAsMaybeZipped(queryFile), Integer.MAX_VALUE);
                DoCount.queryFile = readQueryDoc(queryFile);
            }
            if (smoothness == 1.0 && alpha == 0.0) {
                return betaSimFunction(betaMethod, binQuery, precomp, stopwords);
            } else {
                return Metrics.smoothed(betaSimFunction(betaMethod, binQuery, precomp, stopwords), smoothness, alpha * mean);
            }

        } else {
            return new DefaultBetaFunction();
        }
    }

    private static class DefaultBetaFunction implements BetaSimFunction {

        @Override
        public double scoreNGrams(IntList document, int W) {
            return score(SparseIntArray.histogram(document.toIntArray(), W));
        }

        @Override
        public double score(Vector<Integer> document) {
            return 1.0;
        }
    }
}
