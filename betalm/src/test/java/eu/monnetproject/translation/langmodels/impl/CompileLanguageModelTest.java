package eu.monnetproject.translation.langmodels.impl;

import eu.monnetproject.translation.langmodels.NGram;
import eu.monnetproject.translation.langmodels.NGramCountSet;
import eu.monnetproject.translation.langmodels.NGramCountSetImpl;
import eu.monnetproject.translation.langmodels.impl.CompileLanguageModel.SourceType;
import eu.monnetproject.translation.topics.WordMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author john
 */
public class CompileLanguageModelTest {

    public CompileLanguageModelTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of doCount method, of class CompileLanguageModel.
     */
    @Test
    public void testDoCount() throws Exception {
        System.out.println("doCount");
        int N = 2;
        final DataInputStream corpus = new DataInputStream(new ByteArrayInputStream(new byte[]{
                    0, 0, 0, 1,
                    0, 0, 0, 2,
                    0, 0, 0, 3,
                    0, 0, 0, 0,
                    0, 0, 0, 1,
                    0, 0, 0, 3,
                    0, 0, 0, 4,
                    0, 0, 0, 0,
                    0, 0, 0, 1,
                    0, 0, 0, 1,
                    0, 0, 0, 0,
                    0, 0, 0, 2,
                    0, 0, 0, 0
                }));
        IntegerizedCorpusReader reader = new IntegerizedCorpusReader(corpus);
        SourceType type = SourceType.SIMPLE;
        CompileLanguageModel instance = new CompileLanguageModel();
        NGramCountSet expResult = new NGramCountSetImpl(N);
        expResult.ngramCount(1).put(new NGram(new int[]{1}), 4);
        expResult.ngramCount(1).put(new NGram(new int[]{2}), 2);
        expResult.ngramCount(1).put(new NGram(new int[]{3}), 2);
        expResult.ngramCount(1).put(new NGram(new int[]{4}), 1);
        expResult.inc(1);
        expResult.inc(1);
        expResult.inc(1);
        expResult.inc(1);
        expResult.inc(1);
        expResult.inc(1);
        expResult.inc(1);
        expResult.inc(1);
        expResult.inc(1);
        expResult.ngramCount(2).put(new NGram(new int[]{1, 2}), 1);
        expResult.ngramCount(2).put(new NGram(new int[]{2, 3}), 1);
        expResult.ngramCount(2).put(new NGram(new int[]{1, 3}), 1);
        expResult.ngramCount(2).put(new NGram(new int[]{3, 4}), 1);
        expResult.ngramCount(2).put(new NGram(new int[]{1, 1}), 1);
        expResult.inc(2);
        expResult.inc(2);
        expResult.inc(2);
        expResult.inc(2);
        expResult.inc(2);
        NGramCountSet result = instance.doCount(N, reader, type);
        for (int i = 1; i <= N; i++) {
            final Object2IntMap<NGram> ngramExp = expResult.ngramCount(i);
            final Object2IntMap<NGram> ngramRes = result.ngramCount(i);
            final ObjectIterator<Entry<NGram>> expIter = ngramExp.object2IntEntrySet().iterator();
            final ObjectIterator<Entry<NGram>> resIter = ngramRes.object2IntEntrySet().iterator();
            while (expIter.hasNext()) {
                final Entry<NGram> expEntry = expIter.next();
                final Entry<NGram> nextEntry = resIter.next();
                assertEquals(expEntry, nextEntry);
            }
            assertFalse(resIter.hasNext());
        }
    }

    /**
     * Test of writeModel method, of class CompileLanguageModel.
     */
    @Test
    public void testWriteModel() {
        System.out.println("writeModel");
        final StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        String[] invWordMap = new String[] { null,"a","b","c","d" };
//        WordMap wordMap = new WordMap();
//        wordMap.offer("a");
  //      wordMap.offer("b");
    //    wordMap.offer("c");
      //  wordMap.offer("d");
        int N = 2;
        NGramCountSet countSet = new NGramCountSetImpl(N);
        countSet.ngramCount(1).put(new NGram(new int[]{1}), 4);
        countSet.ngramCount(1).put(new NGram(new int[]{2}), 2);
        countSet.ngramCount(1).put(new NGram(new int[]{3}), 2);
        countSet.ngramCount(1).put(new NGram(new int[]{4}), 1);
        countSet.inc(1);
        countSet.inc(1);
        countSet.inc(1);
        countSet.inc(1);
        countSet.inc(1);
        countSet.inc(1);
        countSet.inc(1);
        countSet.inc(1);
        countSet.inc(1);
        countSet.ngramCount(2).put(new NGram(new int[]{1, 2}), 1);
        countSet.ngramCount(2).put(new NGram(new int[]{2, 3}), 1);
        countSet.ngramCount(2).put(new NGram(new int[]{1, 3}), 1);
        countSet.ngramCount(2).put(new NGram(new int[]{3, 4}), 1);
        countSet.ngramCount(2).put(new NGram(new int[]{1, 1}), 1);
        countSet.inc(2);
        countSet.inc(2);
        countSet.inc(2);
        countSet.inc(2);
        countSet.inc(2);
        CompileLanguageModel instance = new CompileLanguageModel();
        instance.writeModel(out, invWordMap, countSet.asWeightedSet());
        final String ls = System.getProperty("line.separator");
        final String expResult = "\\data\\" + ls
                + "ngram 1=4" + ls
                + "ngram 2=5" + ls
                + "" + ls
                + "\\1-grams:" + ls
                + "-0.6532125137753437\tb" + ls
                + "-0.9542425094393249\td" + ls
                + "-0.6532125137753437\tc" + ls
                + "-0.35218251811136253\ta" + ls 
                + ls
                + "\\2-grams:" + ls
                + "-0.6989700043360187\tb c" + ls
                + "-0.6989700043360187\ta b" + ls
                + "-0.6989700043360187\ta a" + ls
                + "-0.6989700043360187\tc d" + ls
                + "-0.6989700043360187\ta c" + ls 
                + ls
                + "\\end\\" + ls;

        assertEquals(expResult, sw.toString());
    }
}