package eu.monnetproject.translation.langmodels.smoothing;

import eu.monnetproject.translation.langmodels.NGram;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Map.Entry;
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
public class LossyCounterWithHistoryTest {

    public LossyCounterWithHistoryTest() {
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

    private static NGram ng(int... x) {
        return new NGram(x);
    }
    
    /**
     * Test of offer method, of class LossyCounterWithHistory.
     */
    @Test
    public void testOffer() {
        System.out.println("offer");
        LossyCounterWithHistory instance = new LossyCounterWithHistory(2);
        instance.offer(1);
        instance.offer(2);
        instance.offer(3);
        instance.offer(4);
        instance.offer(4);
        instance.offer(3);
        instance.offer(2);
        instance.offer(1);
        instance.offer(1);
        instance.offer(2);
        instance.offer(4);
        instance.offer(3);
        final Object2ObjectOpenHashMap<NGram, int[]> expHistories = new Object2ObjectOpenHashMap<NGram, int[]>();
        expHistories.put(ng(1), new int[] { 1,1,0,2,0,0 });
        expHistories.put(ng(2), new int[] { 3,0,0,1,1,0 });
        expHistories.put(ng(3), new int[] { 2,0,0,1,1,0 });
        expHistories.put(ng(4), new int[] { 1,1,0,3,0,0 });
        final Object2ObjectMap<NGram, int[]> histories = instance.histories();
        assertEquals(expHistories.size(),histories.size());
        final ObjectIterator<Entry<NGram, int[]>> iter1 = expHistories.entrySet().iterator();
        final ObjectIterator<Entry<NGram, int[]>> iter2 = histories.entrySet().iterator();
        while(iter1.hasNext()) {
            final Entry<NGram, int[]> e1 = iter1.next();
            final Entry<NGram, int[]> e2 = iter2.next();
            assertEquals(e1.getKey(),e2.getKey());
            assertArrayEquals(e1.getValue(), e2.getValue());
        }
    }

}