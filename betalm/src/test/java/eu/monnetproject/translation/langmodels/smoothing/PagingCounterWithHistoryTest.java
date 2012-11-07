package eu.monnetproject.translation.langmodels.smoothing;

import eu.monnetproject.translation.langmodels.NGram;
import eu.monnetproject.translation.langmodels.smoothing.PagingCounterWithHistory.PagingNGramReader;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author john
 */
public class PagingCounterWithHistoryTest {

    public PagingCounterWithHistoryTest() {
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

    @Test
    public void testPagingReader() throws Exception {
        final File page1 = File.createTempFile("lm1", ".lm");
        page1.deleteOnExit();
        final File page2 = File.createTempFile("lm2", ".lm");
        page2.deleteOnExit();
        final File page3 = File.createTempFile("lm3", ".lm"); // Phwoar!
        page3.deleteOnExit();
        {
            final DataOutputStream out1 = new DataOutputStream(new FileOutputStream(page1));

            out1.writeInt(0); // H

            out1.writeInt(3); // #1-grams

            out1.writeInt(1); // n-gram
            out1.writeInt(1); // count
            out1.writeInt(1); // history2
            out1.writeFloat(0.1f); // histories

            out1.writeInt(2); // n-gram
            out1.writeInt(2); // count
            out1.writeInt(1); // history2
            out1.writeFloat(0.2f); // histories

            out1.writeInt(3); // n-gram
            out1.writeInt(3); // count
            out1.writeInt(1); // history2
            out1.writeFloat(0.3f); // histories

            out1.writeInt(0); // H
            out1.writeInt(1); // #2-grams

            out1.writeInt(1);
            out1.writeInt(1); // 2-gram
            out1.writeInt(1); // count
            out1.writeInt(1); // history
            out1.writeInt(1); // history2
            out1.writeFloat(1); // histories
            out1.writeFloat(1); // historiesOfHistory
            out1.flush();
            out1.close();
        }
        {
            final DataOutputStream out2 = new DataOutputStream(new FileOutputStream(page2));

            out2.writeInt(0); // H

            out2.writeInt(3); // #1-grams

            out2.writeInt(1); // n-gram
            out2.writeInt(1); // count
            out2.writeInt(1); // history2
            out2.writeFloat(0.1f); // histories

            out2.writeInt(3); // n-gram
            out2.writeInt(3); // count
            out2.writeInt(1); // history2
            out2.writeFloat(0.3f); // histories

            out2.writeInt(4); // n-gram
            out2.writeInt(4); // count
            out2.writeInt(1); // history2
            out2.writeFloat(0.4f); // histories
            
            out2.writeInt(0); // H

            out2.writeInt(1); // #2-grams

            out2.writeInt(2);
            out2.writeInt(2); // 2-gram
            out2.writeInt(1); // count
            out2.writeInt(1); // history
            out2.writeInt(1); // history2
            out2.writeFloat(1); // histories
            out2.writeFloat(1); // historiesOfHistory
            out2.flush();
            out2.close();
        }
        
        {
            final DataOutputStream out3 = new DataOutputStream(new FileOutputStream(page3));

            out3.writeInt(0); // H

            out3.writeInt(2); // #1-grams

            out3.writeInt(2); // n-gram
            out3.writeInt(2); // count
            out3.writeInt(1); // history2
            out3.writeFloat(0.2f); // histories

            out3.writeInt(4); // n-gram
            out3.writeInt(4); // count
            out3.writeInt(1); // history2
            out3.writeFloat(0.4f); // histories
            
            out3.writeInt(0); // H
            out3.writeInt(1); // #2-grams

            out3.writeInt(3);
            out3.writeInt(3); // 2-gram
            out3.writeInt(1); // count
            out3.writeInt(1); // history
            out3.writeInt(1); // history2
            out3.writeFloat(1); // histories
            out3.writeFloat(1); // historiesOfHistory
            out3.flush();
            out3.close();
        }
        final LinkedList<File> pages = new LinkedList<File>();
        pages.add(page1);
        pages.add(page2);
        pages.add(page3);
        final PagingNGramReader reader = new PagingCounterWithHistory.PagingNGramReader(pages , 1, null, null);
        final Object2IntMap<NGram> unigramCounts = reader.ngramCount(1);
        final ObjectIterator<Entry<NGram>> unigramIter = unigramCounts.object2IntEntrySet().iterator();
        assertEquals(2,unigramIter.next().getIntValue());
        assertEquals(4,unigramIter.next().getIntValue());
        assertEquals(6,unigramIter.next().getIntValue());
        assertEquals(8,unigramIter.next().getIntValue());
        assertFalse(unigramIter.hasNext());
        final Object2IntMap<NGram> bigramCounts = reader.ngramCount(2);
        final ObjectIterator<Entry<NGram>> bigramIter = bigramCounts.object2IntEntrySet().iterator();
        assertEquals(1,bigramIter.next().getIntValue());
        assertEquals(1,bigramIter.next().getIntValue());
        assertEquals(1,bigramIter.next().getIntValue());
        assertFalse(bigramIter.hasNext());
    }
}