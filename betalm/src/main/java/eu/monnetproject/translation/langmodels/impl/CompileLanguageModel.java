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
package eu.monnetproject.translation.langmodels.impl;

import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.translation.langmodels.Counter;
import eu.monnetproject.translation.langmodels.LossyCounter;
import eu.monnetproject.translation.langmodels.NGram;
import eu.monnetproject.translation.langmodels.NGramCountSet;
import eu.monnetproject.translation.langmodels.WeightedNGramCountSet;
import eu.monnetproject.translation.langmodels.smoothing.AddAlphaSmoothing;
import eu.monnetproject.translation.langmodels.smoothing.CounterWithHistory;
import eu.monnetproject.translation.langmodels.smoothing.GoodTuringSmoothing;
import eu.monnetproject.translation.langmodels.smoothing.KneserNeySmoothing;
import eu.monnetproject.translation.langmodels.smoothing.LossyCounterWithHistory;
import eu.monnetproject.translation.langmodels.smoothing.NGramHistories;
import eu.monnetproject.translation.langmodels.smoothing.NGramScorer;
import eu.monnetproject.translation.langmodels.smoothing.PagingCounterWithHistory;
import eu.monnetproject.translation.langmodels.smoothing.SimpleNGramScorer;
import eu.monnetproject.translation.topics.CLIOpts;
import eu.monnetproject.translation.topics.WordMap;
import eu.monnetproject.translation.topics.sim.BetaLMImpl;
import eu.monnetproject.translation.topics.sim.BetaSimFunction;
import eu.monnetproject.translation.topics.sim.Metrics;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class CompileLanguageModel {

    public enum SourceType {

        SIMPLE,
        INTERLEAVED_USE_FIRST,
        INTERLEAVED_USE_SECOND
    };

    public enum Smoothing {

        NONE,
        ADD_ALPHA,
        GOOD_TURING,
        KNESER_NEY
    };
    public NGramHistories histories;

    public NGramCountSet doCount(int N, IntegerizedCorpusReader reader, SourceType type, Smoothing smoothing, boolean paging) throws IOException {

        final Counter counter;
        if (smoothing == Smoothing.KNESER_NEY && !paging) {
            counter = new LossyCounterWithHistory(N);
        } else if (!paging) {
            counter = new LossyCounter(N);
        } else {
            counter = new PagingCounterWithHistory(N);
        }
        long read = 0;
        boolean inDoc = type != SourceType.INTERLEAVED_USE_SECOND;
        while (reader.hasNext()) {
            try {
                final int tk = reader.nextToken();
                if (tk == 0) {
                    if (type == SourceType.SIMPLE) {
                        counter.docEnd();
                    } else {
                        if (inDoc) {
                            counter.docEnd();
                            inDoc = false;
                        } else {
                            inDoc = true;
                        }
                    }
                } else if (inDoc) {
                    counter.offer(tk);
                }
                if (++read % 1048576 == 0) {
                    System.err.print(".");
                }
            } catch (EOFException x) {
                break;
            }
        }
        if (counter instanceof CounterWithHistory) {
            histories = ((CounterWithHistory) counter).histories();
        }
        return counter.counts();
    }

    public void writeModel(PrintStream out, String[] inverseWordMap, WeightedNGramCountSet countSet, NGramScorer scorer) {
        out.println("\\data\\");
        for (int i = 1; i <= countSet.N(); i++) {
            try {
                out.println("ngram " + i + "=" + countSet.ngramCount(i).size());
            } catch(UnsupportedOperationException x) {
                out.println("ngram " + i + "=?");
            }
        }
        out.println();
        for (int i = 1; i <= countSet.N(); i++) {
            out.println("\\" + i + "-grams:");
            int n = 0;
            for (Object2DoubleMap.Entry<NGram> entry : countSet.ngramCount(i).object2DoubleEntrySet()) {
                final double[] scores = scorer.ngramScores(entry.getKey(), countSet);
                out.print(scores[0] + "\t");
                final int[] ng = entry.getKey().ngram;
                for (int j = 0; j < ng.length; j++) {
                    out.print(inverseWordMap[ng[j]]);
                    if (j + 1 != ng.length) {
                        out.print(" ");
                    }
                }
                if (scores.length > 1) {
                    out.print("\t");
                    out.print(scores[1]);
                }
                out.println();
                if (++n % 10000 == 0) {
                    System.err.print(".");
                }

            }
            System.err.println();
            out.println();
        }
        out.println("\\end\\");
        out.flush();
        out.close();
    }

    public static int[][] countOfCounts(WeightedNGramCountSet countset) {
        System.err.print("Counting counts");
        int n = 0;
        final int[][] CoC = new int[countset.N()][];
        for (int i = 0; i < countset.N(); i++) {
            final Object2DoubleMap<NGram> ngramCount = countset.ngramCount(i + 1);
            final IntArrayList counts = new IntArrayList();
            for (Object2DoubleMap.Entry<NGram> e : ngramCount.object2DoubleEntrySet()) {
                final int ci = (int) Math.ceil(e.getDoubleValue()) - 1;
                while (counts.size() <= ci) {
                    counts.add(0);
                }
                counts.set(ci, counts.get(ci) + 1);
                if (++n % 100000 == 0) {
                    System.err.print(".");
                }
            }
            CoC[i] = counts.toIntArray();
        }
        System.err.println();
        return CoC;
    }

    public static NGramScorer getScorer(Smoothing smoothing, WeightedNGramCountSet countset, NGramHistories histories) {
        switch (smoothing) {
            case NONE:
                return new SimpleNGramScorer();
            case ADD_ALPHA: {
                final int[] v = new int[countset.N()];
                final double[] C = new double[countset.N()];
                for (int i = 1; i <= countset.N(); i++) {
                    v[i - 1] = countset.ngramCount(i).size();
                    C[i - 1] = countset.total(i);
                }
                return new AddAlphaSmoothing(v, C);
            }
            case GOOD_TURING: {
                final int[] v = new int[countset.N()];
                final double[] C = new double[countset.N()];
                final int[][] CoC = countOfCounts(countset);
                for (int i = 1; i <= countset.N(); i++) {
                    v[i - 1] = countset.ngramCount(i).size();
                    C[i - 1] = countset.total(i);
                }
                return new GoodTuringSmoothing(C, CoC, v);
            }
            case KNESER_NEY: {
                final int[][] CoC = histories.countOfCounts();
                double[] r = new double[countset.N()];
                for (int i = 1; i <= countset.N(); i++) {
                    r[i - 1] = countset.mean(i);
                }
                return new KneserNeySmoothing(histories, CoC, countset.N(), 3, r);
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    public static BetaSimFunction betaSimFunction(BetaLMImpl.Method method, SparseIntArray query, PrecomputedValues precomp) {
        switch (method) {
            case COS_SIM:
                return Metrics.cosSim(query);
            case DF_DICE:
                return Metrics.dfDiceCoefficient(query, precomp.df);
            case DF_JACCARD:
                return Metrics.dfJaccardCoefficient(query, precomp.df);
            case DICE:
                return Metrics.diceCoefficient(query);
            case JACCARD:
                return Metrics.jaccardIndex(query);
            case KLD:
                return Metrics.kullbackLeiblerDivergence(query);
            case NORMAL_COS_SIM:
                return Metrics.normalCosSim(query, precomp.mu, precomp.sumMu2);
            case ROGERS_TANIMOTO:
                return Metrics.rogersTanimoto(query);
            case WxWCLESA:
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static void main(String[] _args) throws Exception {
        final CLIOpts opts = new CLIOpts(_args);

        final ArrayList<String> args = new ArrayList<String>(Arrays.asList(_args));


        StringBuilder betalmString = new StringBuilder("The BetaLM method: ");
        for (BetaLMImpl.Method method : BetaLMImpl.Method.values()) {
            betalmString.append(method.name()).append(" ");
        }
        final BetaLMImpl.Method betaMethod = opts.enumOptional("b", BetaLMImpl.Method.class, null, betalmString.toString());

        final SourceType sourceType = opts.enumOptional("t", SourceType.class, betaMethod == null ? SourceType.SIMPLE : SourceType.INTERLEAVED_USE_FIRST, "The type of source: SIMPLE, INTERLEAVED_USE_FIRST or INTERLEAVED_USE_SECOND");

        final Smoothing smoothing = opts.enumOptional("smooth", Smoothing.class, Smoothing.NONE, "The type of smoothing: NONE, ADD_ALPHA, GOOD_TURING, KNESER_NEY");

        final File queryFile = opts.roFile("f", "The query file (ontology)", null);

        final double smoothness = opts.doubleValue("s", 1.0, "The selective smoothing parameter");

        final double alpha = opts.doubleValue("a", 1.0, "The minimal smoothing parameter");

        final boolean paging = opts.flag("page", "Use paging (slower) to calculate language model");

        final File inFile = opts.roFile("corpus[.gz|.bz2]", "The corpus file");

        final int N = opts.nonNegIntValue("N", "The largest n-gram to calculate");

        final File wordMapFile = opts.roFile("wordMap", "The word map file");

        final int W = opts.nonNegIntValue("W", "The number of distinct tokens in corpus");

        final PrintStream out = opts.outFileOrStdout();

        if (!opts.verify(CompileLanguageModel.class)) {
            return;
        }

        final CompileLanguageModel compiler = betaMethod == null
                ? new CompileLanguageModel() : new CompileBetaModel();

        final BetaSimFunction betaSimFunction;
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
                return;
            }
            if (sourceType == SourceType.SIMPLE) {
                System.err.println("BetaLM needs a bilingual corpus, set -t");
                return;
            }
            final SparseIntArray binQuery = SparseIntArray.fromBinary(CLIOpts.openInputAsMaybeZipped(queryFile), Integer.MAX_VALUE);
            if (smoothness == 1.0) {
                betaSimFunction = betaSimFunction(betaMethod, binQuery, precomp);
            } else {
                betaSimFunction = Metrics.smoothed(betaSimFunction(betaMethod, binQuery, precomp), smoothness, alpha);
            }

        } else {
            betaSimFunction = null;
        }

        System.err.println("Reading from " + inFile.getPath());
        System.err.print("Counting corpus");
        final WeightedNGramCountSet countSet;
        if (betaMethod == null) {
            countSet = compiler.doCount(N, new IntegerizedCorpusReader(new DataInputStream(CLIOpts.openInputAsMaybeZipped(inFile))), sourceType, smoothing, paging).asWeightedSet();
        } else {
            countSet = ((CompileBetaModel) compiler).doCount(N, new IntegerizedCorpusReader(new DataInputStream(CLIOpts.openInputAsMaybeZipped(inFile))), sourceType, betaSimFunction, smoothing);
        }

        System.err.print("Loading word map:");
        final String[] wordMap = WordMap.inverseFromFile(wordMapFile, W, true);

        final NGramScorer scorer = getScorer(smoothing, countSet, compiler.histories);

        System.err.print("Writing model");
        compiler.writeModel(out, wordMap, countSet, scorer);
        out.flush();
        out.close();
    }
}
