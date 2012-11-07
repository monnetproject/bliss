package eu.monnetproject.translation.langmodels.smoothing;

import eu.monnetproject.translation.langmodels.NGram;
import eu.monnetproject.translation.langmodels.impl.CompileLanguageModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
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
        final Object2ObjectRBTreeMap<NGram, float[]> expHistories = new Object2ObjectRBTreeMap<NGram, float[]>();
        expHistories.put(ng(1), new float[]{0, 1, 1, 0, 2, 0, 0});
        expHistories.put(ng(2), new float[]{0, 3, 0, 0, 1, 1, 0});
        expHistories.put(ng(3), new float[]{0, 2, 0, 0, 1, 1, 0});
        expHistories.put(ng(4), new float[]{0, 1, 1, 0, 3, 0, 0});
        final Object2ObjectMap<NGram, float[]> histories = instance.histories().histories(1);
        assertEquals(expHistories.size(), histories.size());
        final ObjectIterator<Entry<NGram, float[]>> iter1 = expHistories.entrySet().iterator();
        final ObjectIterator<Entry<NGram, float[]>> iter2 = histories.entrySet().iterator();
        while (iter1.hasNext()) {
            final Entry<NGram, float[]> e1 = iter1.next();
            final Entry<NGram, float[]> e2 = iter2.next();
            assertEquals(e1.getKey(), e2.getKey());
            assertArrayEquals(e1.getValue(), e2.getValue(), 0.0f);
        }
    }

    @Test
    public void testCountOfCounts() {
        System.out.println("countOfCounts");
        LossyCounterWithHistory instance = new LossyCounterWithHistory(3);
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
        final int[][] coc = CompileLanguageModel.countOfCounts(instance.counts().asWeightedSet());
        int[][] expCoc = {
            {0, 0, 4},
            {7, 2},
            {10}
        };
        for (int i = 0; i < coc.length; i++) {
            assertArrayEquals(expCoc, coc);
        }
    }
}