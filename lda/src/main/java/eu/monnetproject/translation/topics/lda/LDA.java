///**
// * ********************************************************************************
// * Copyright (c) 2011, Monnet Project All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met: *
// * Redistributions of source code must retain the above copyright notice, this
// * list of conditions and the following disclaimer. * Redistributions in binary
// * form must reproduce the above copyright notice, this list of conditions and
// * the following disclaimer in the documentation and/or other materials provided
// * with the distribution. * Neither the name of the Monnet Project nor the names
// * of its contributors may be used to endorse or promote products derived from
// * this software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
// * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// * *******************************************************************************
// */
package eu.monnetproject.translation.topics.lda;
//
import eu.monnetproject.translation.topics.Tokenizer;
import eu.monnetproject.translation.topics.Token;
import eu.monnetproject.lang.Language;
import eu.monnetproject.lang.Script;
import eu.monnetproject.framework.services.Services;
import eu.monnetproject.translation.topics.PTBTokenizer;
//import eu.monnetproject.tokens.Token;
//import eu.monnetproject.tokenizer.Tokenizer;
//import eu.monnetproject.tokenizer.TokenizerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
//
///**
// *
// * @author John McCrae
// */
public class LDA {
//
//    //private static final Logger log = Logging.getLogger(LDA.class);
//    private final TokenizerFactory tokenizerFactory = Services.get(TokenizerFactory.class);
//    private final HashMap<Script, Tokenizer> tokenizers = new HashMap<Script, Tokenizer>();
//
    public GibbsInput makeInput(TextCorpus corpus, int K, int minFreq) {
        return makeInput(corpus, K, minFreq, 1);
    }

    public GibbsInput makeInput(TextCorpus corpus, int K, int minFreq, int sampling) {
        HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
        HashMap<String, LinkedList<Integer>> docMap = new HashMap<String, LinkedList<Integer>>();
        HashMap<Integer, String> docIdxs = new HashMap<Integer, String>();
        HashSet<String> passed = new HashSet<String>();
        HashMap<String, Integer> freq = new HashMap<String, Integer>();
        LinkedList<int[]> docs = new LinkedList<int[]>();
        LinkedList<Language> languages = new LinkedList<Language>();
        LinkedList<Integer> docLangs = new LinkedList<Integer>();
        int W = 0;
        int j = 0;
        int docNo = 0;
        for (TextDocument doc : corpus.getDocuments()) {
            if (docNo++ % 1000 == 0) {
                //log.info("Document: " + docNo);
            }
            if (docNo % sampling != 0 && !docMap.containsKey(doc.getName())) {
                //log.warning("skipped" + doc.getName());
                continue;
            }
            if (!docMap.containsKey(doc.getName())) {
                docMap.put(doc.getName(), new LinkedList<Integer>());
            }
            docMap.get(doc.getName()).add(j);
            docIdxs.put(j, doc.getName());
            j++;
            final Language lang = doc.getLang();
            final Tokenizer tokenizer = tokenizerForDoc(doc);
            if (tokenizer == null) {
              // log.warning("No tokenizer for " + lang + " skipped");
                continue;
            }
            if (!languages.contains(lang)) {
                languages.add(lang);
            }
            final int langIdx = languages.indexOf(lang);
            docLangs.add(langIdx);
            LinkedList<Integer> d = new LinkedList<Integer>();
            System.err.println(">>>>" + doc.getName());
            for (Token tk : tokenizer.tokenize(doc.getText().toLowerCase())) {
                if (hasPassed(passed, freq, tk.getValue(), minFreq)) {
                    final String word = tk.getValue();
                    System.err.print(word + " ");
                    int wordIdx;
                    if (!wordMap.containsKey(word)) {
                        wordMap.put(word, W);
                        wordIdx = W;
                        W++;
                    } else {
                        wordIdx = wordMap.get(word);
                    }
                    d.add(wordIdx);
                }
            }
            System.err.println("");
            int[] d2 = new int[d.size()];
            for (int i = 0; i < d.size(); i++) {
                d2[i] = d.get(i);
            }
            docs.add(d2);
        }

        int[] DN = new int[docs.size()];
        for (int i = 0; i < docs.size(); i++) {
            DN[i] = docs.get(i).length;
        }

        int[] m = new int[docLangs.size()];
        for (int i = 0; i < docLangs.size(); i++) {
            m[i] = docLangs.get(i);
        }

        int[][] mu = new int[docs.size()][];
        for (int i = 0; i < docLangs.size(); i++) {
            final LinkedList<Integer> docList = docMap.get(docIdxs.get(i));
            int[] docArray = new int[docList.size()];
            int k = 0;
            for (int n : docList) {
                docArray[k++] = n;
            }
            mu[i] = docArray;
        }

        return new GibbsInput(docs.size(), W, DN, docs.toArray(new int[docs.size()][]), languages.toArray(new Language[languages.size()]), m, mu, wordMap);
    }

    private boolean hasPassed(HashSet<String> passed, HashMap<String, Integer> freq, String tk, int minFreq) {
        if (passed.contains(tk)) {
            return true;
        } else if (!freq.containsKey(tk)) {
            freq.put(tk, 1);
            return minFreq <= 1;
        } else {
            int f = freq.get(tk);
            if (f + 1 >= minFreq) {
                passed.add(tk);
                freq.remove(tk);
                return true;
            } else {
                freq.put(tk, f + 1);
                return false;
            }
        }
    }
//
//    private Tokenizer tokenizerForDoc(TextDocument doc) {
//        final Language lang = doc.getLang();
//        final Script[] knownScriptsForLanguage = Script.getKnownScriptsForLanguage(lang);
//        if (knownScriptsForLanguage == null || knownScriptsForLanguage.length == 0) {
//            log.warning("No known script for " + lang + " skipped");
//            return null;
//        }
//        Tokenizer tokenizer = null;
//        for (Script script : knownScriptsForLanguage) {
//            if (tokenizers.containsKey(script)) {
//                return tokenizers.get(script);
//            }
//            tokenizer = tokenizerFactory.getTokenizer(script);
//            if (tokenizer != null) {
//                tokenizers.put(script, tokenizer);
//                return tokenizer;
//            }
//        }
//        return null;
//    }
    private Tokenizer tokenizerForDoc(TextDocument doc) {
        return new PTBTokenizer();
    }
//
    private int[] docToArray(TextDocument doc, Map<String, Integer> words) {
        Tokenizer tokenizer = tokenizerForDoc(doc);
        if (tokenizer == null) {
            throw new IllegalArgumentException("Cannot tokenize document");
        }
        ArrayList<Integer> rv = new ArrayList<Integer>();
        for (Token tk : tokenizer.tokenize(doc.getText())) {
            if (words.containsKey(tk.getValue())) {
                rv.add(words.get(tk.getValue()));
            } else {
                // OOV
            }
        }
        int[] d = new int[rv.size()];
        int j = 0;
        for (int i : rv) {
            d[j++] = i;
        }
        return d;
    }

    public PolylingualGibbsData train(TextCorpus corpus, int K, int iterations) {
        return train(corpus, K, iterations, 0, null);
    }
//
    public PolylingualGibbsData train(TextCorpus corpus, int K, int iterations, String outputName) {
        return train(corpus, K, iterations, 0, outputName);
    }
//
    public PolylingualGibbsData train(TextCorpus corpus, int K, int iterations, int minFreq, String outputName) {
        final GibbsInput input = makeInput(corpus, K, minFreq);
        return train(input, K, iterations, outputName);
    }

    public PolylingualGibbsData train(GibbsInput input, int K, int iterations, String outputName) {
        //log.info("Validating input");
        if (!input.validate()) {
            throw new RuntimeException();
        }
        //log.info("Preparing inference");
        final GibbsInference gibbsInference = new GibbsInference(input, K);
        //log.info("Starting");
        for (int i = 0; i < iterations / 100; i++) {
            gibbsInference.iterator(100, iterations);
            try {
                if (outputName != null) {
                    gibbsInference.getPolylingualData(input.languages, input.words).write(new FileOutputStream(outputName + "." + (i * 100)));
                }
            } catch (Exception x) {
          //      log.stackTrace(x);
            }
        }
        return gibbsInference.getPolylingualData(input.languages, input.words);
    }

    public PolylingualGibbsData trainWithStartCondition(TextCorpus corpus, int K, int iterations, int[][] z) {
        return trainWithStartCondition(corpus, K, iterations, z, 0);
    }

    public PolylingualGibbsData trainWithStartCondition(TextCorpus corpus, int K, int iterations, int[][] z, int minFreq) {
        final GibbsInput input = makeInput(corpus, K, minFreq);
        if (!input.validate()) {
            throw new RuntimeException();
        }
        final GibbsInference gibbsInference = new GibbsInference(input, K);
        gibbsInference.initializeWithFixedZ(z);
        gibbsInference.iterator(iterations, iterations);
        gibbsInference.logZ();
        return gibbsInference.getPolylingualData(input.languages, input.words);
    }

    public double estimate(String word, Language language, TextDocument document, int iterations, double oovProb, PolylingualGibbsData data) {
        final int[] d = docToArray(document, data.words);
        final Estimator estimator = new Estimator();
        if (!data.words.containsKey(word)) {
            return oovProb;
        }
        int l = 0;
        for (; l < data.languages.length; l++) {
            if (language.equals(data.languages[l])) {
                break;
            }
        }
        if (l == data.languages.length) {
            return oovProb;
        }
        return estimator.wordProb(data.words.get(word), d, l, data, iterations);
    }
//
//    public static void main(String[] args) throws Exception {
//        if (args.length == 0) {
//            printUsage();
////            args = "-train wiki.enes.gibbsinput 10 100 wiki.enes.gibbs".split(" ");
//        }
//        final LDA lda = new LDA();
//        if (args[0].equals("-prepare")) {
//            if (args.length != 5 && args.length != 6) {
//                printUsage();
//            }
//            int sampling = 1;
//            if (args.length == 6) {
//                sampling = Integer.parseInt(args[5]);
//            }
//            final TextCorpusFactory corpusFactory = Services.get(TextCorpusFactory.class);
//            final TextCorpus corpus = corpusFactory.makeCorpus(new File(args[1]));
//            final GibbsInput input = lda.makeInput(corpus, Integer.parseInt(args[2]), Integer.parseInt(args[4]), sampling);
//            input.write(new FileOutputStream(args[3]));
//        } else if (args[0].equals("-train")) {
//            if (args.length != 5) {
//                printUsage();
//            }
//            final File data = new File(args[1]);
//            if (data.isDirectory()) {
//                final TextCorpusFactory corpusFactory = Services.get(TextCorpusFactory.class);
//                final PolylingualGibbsData out = lda.train(corpusFactory.makeCorpus(data), Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4]);
//                out.write(new FileOutputStream(args[4]));
//            } else {
//                log.info("Reading " + args[1]);
//                final GibbsInput input = GibbsInput.read(new FileInputStream(args[1]));
//                final PolylingualGibbsData out = lda.train(input, Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4]);
//                out.write(new FileOutputStream(args[4]));
//            }
//        } else if (args[0].equals("-estimate")) {
//            if (args.length != 7) {
//                printUsage();
//            }
//            final TextCorpusFactory corpusFactory = Services.get(TextCorpusFactory.class);
//            final double prob = lda.estimate(args[1],
//                    Language.get(args[2]),
//                    corpusFactory.makeDocument(new File(args[3])),
//                    Integer.parseInt(args[4]),
//                    Double.parseDouble(args[5]),
//                    PolylingualGibbsData.read(new FileInputStream(args[6])));
//            System.out.println(prob);
//        } else if (args[0].equals("-topicwords")) {
//            if (args.length != 3) {
//                printUsage();
//            }
//            final GibbsInput input = GibbsInput.read(new FileInputStream(args[1]));
//            final String[] topicWords = TopicWords.getWords(input.W, input);
//            final PolylingualGibbsData data = PolylingualGibbsData.read(new FileInputStream(args[2]));
//            TopicWords.printTopicWords(System.out, data, topicWords);
////        } else if (args[0].equals("-wikipedia")) {
////            if (args.length != 6 && args.length != 8) {
////                printUsage();
////            }
////            final int K = Integer.parseInt(args[4]);
////            final File out = new File(args[5]);
////            final String[] urlStrings = args[1].split(",");
////            final String[] langStrings = args[2].split(",");
////            final URL[] urls = new URL[urlStrings.length];
////            final Language[] langs = new Language[urlStrings.length];
////            for (int i = 0; i < urlStrings.length; i++) {
////                urls[i] = new URL(urlStrings[i]);
////                langs[i] = Language.get(langStrings[i]);
////            }
////            final WikipediaCorpus corpus = new WikipediaCorpus(urls, langs, Language.get(args[3]), args.length == 8 ? Integer.parseInt(args[6]) : Integer.MAX_VALUE);
////            final GibbsInput input = lda.makeInput(corpus, K, args.length == 8 ? Integer.parseInt(args[7]) : 0);
////            input.write(new FileOutputStream(out));
////        } else if (args[0].equals("-wikiprocess")) {
////            final String[] args2 = new String[args.length - 1];
////            for (int i = 1; i < args.length; i++) {
////                args2[i - 1] = args[i];
////            }
////            WikipediaProcessor.main(args2);
////        } else if (args[0].equals("-transeval")) {
////            if(args.length != 5) {
////                printUsage();
////            }
////            final LDAOntologyTranslator lot = new LDAOntologyTranslator();
////            final OntologySerializer ontoSerializer = Services.get(OntologySerializer.class);
////            final Ontology ontology = ontoSerializer.read(new FileDataSource(args[1]));
////            final File file = new File(args[2]);
////            final Language srcLang = Language.get(args[3]);
////            final Language trgLang = Language.get(args[4]);
////            lot.evaluateTranslations(ontology, file, srcLang, trgLang);
////        } else if (args[0].equals("-prepwiki")) {
////            if (args.length <= 6 && args.length > 8) {
////                printUsage();
////            }
////            final String[] wikiURLStrs = args[1].split(",");
////            final String[] wikiLangStrs = args[2].split(",");
////            if (wikiLangStrs.length != wikiURLStrs.length) {
////                throw new IllegalArgumentException("wikiURL length neq wikiLang length");
////            }
////            final URL[] wikiURLs = new URL[wikiURLStrs.length];
////            for (int i = 0; i < wikiURLStrs.length; i++) {
////                wikiURLs[i] = new URL(wikiURLStrs[i]);
////            }
////            final Language[] wikiLangs = new Language[wikiLangStrs.length];
////            for (int i = 0; i < wikiLangStrs.length; i++) {
////                wikiLangs[i] = Language.get(wikiLangStrs[i]);
////            }
////            final Language primaryLang = Language.get(args[3]);
////            //final int K = Integer.parseInt(args[4]);
////            final int minFreq = Integer.parseInt(args[4]);
////            final String outFileName = args[5];
////            final int sampling = args.length > 6 ? Integer.parseInt(args[6]) : 1;
////            final int maxDocs = args.length > 7 ? Integer.parseInt(args[7]) : Integer.MAX_VALUE;
////            final WikipediaCorpus corpus = new WikipediaCorpus(wikiURLs, wikiLangs, primaryLang, maxDocs);
////            final GibbsInput input = lda.makeInput(corpus, 0, minFreq, sampling);
////            input.write(new FileOutputStream(outFileName));
////        } else if (args[0].equals("-uci2monnet")) {
////            if (args.length != 4) {
////                printUsage();
////                return;
////            }
////            GibbsInput.readUCIFormat(new File(args[1]), new File(args[2])).write(new FileOutputStream(args[3]));
//        } else if (args[0].equals("-splitInput")) {
//            if (args.length != 5) {
//                printUsage();
//                return;
//            }
//            final GibbsInput input = GibbsInput.read(new FileInputStream(args[1]));
//            input.printSplit(Integer.parseInt(args[2]), new PrintStream(args[3]), new PrintStream(args[4]));
//        } else if (args[0].equals("-parallelData")) {
//            if(args.length != 5) {
//                printUsage();
//                return;
//            }
//            final GibbsInput input = GibbsInput.read(new FileInputStream(args[1]));
//            input.printParallel(Language.get(args[2]), Language.get(args[3]), new PrintWriter(args[4]));
//        } else {
//            printUsage();
//        }
//    }
//
//    private static void printUsage() {
//        System.err.println("Usage:");
//        System.err.println("  lda -prepare <file> K <data> minFreq [sampling]");
//        System.err.println("  lda -train <file> K iterations <data>");
//        System.err.println("  lda -train <input> K iterations <data>");
//        System.err.println("  lda -estimate word language <document> iterations oovProb <data>");
//        System.err.println("  lda -topicwords <input> <data>");
//        //System.err.println("  lda -wikipedia <wikiURL1,wikiURL2,...> <wikiLang1,wikiLang2,...> primaryLang K <out> [maxDocs minFreq]");
//        //System.err.println("  lda -prepwiki <wikiURL1,wikiURL2,...> <wikiLang1,wikiLang2,...> primaryLang minFreq <out> [sampling maxDocs]");
//        //System.err.println("  lda -uci2monnet dataFile vocabFile outFile");
//        System.err.println("  lda -splitInput inputFile splitSize mainInput splitInput");
//        System.err.println("  lda -parallelData inputFile lang1 lang2 outputFile");
//
////        System.err.println("  lda -transeval ontology gibbsdata srcLang trgLang");
//        System.exit(-1);
//    }
}
