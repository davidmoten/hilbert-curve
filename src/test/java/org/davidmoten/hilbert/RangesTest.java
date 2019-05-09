package org.davidmoten.hilbert;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.davidmoten.guavamini.Lists;

public class RangesTest {

    @Test
    public void testMaxSizeNotExceededWhenMaxIs2() {
        Ranges r = new Ranges(2);
        r.add(Range.create(1));
        r.add(Range.create(10));
        checkIs(r, 1, 1, 10, 10);
        assertEquals(2, r.size());
    }

    @Test
    public void testMaxSizeExceededWhenMaxIs2() {
        Ranges r = new Ranges(2);
        r.add(Range.create(1));
        r.add(Range.create(10));
        r.add(Range.create(12));
        checkIs(r, 1, 1, 10, 12);
        assertEquals(2, r.size());
    }

    @Test
    public void testMaxSizeExceededWhenMaxIs3() {
        Ranges r = new Ranges(3);
        r.add(Range.create(1));
        r.add(Range.create(10));
        r.add(Range.create(12));
        r.add(Range.create(18));
        r.println();
        checkIs(r, 1, 1, 10, 12, 18, 18);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBufferSizeNegativeThrows() {
        new Ranges(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRangeDecreasingThrows() {
        Ranges r = new Ranges(3);
        r.add(Range.create(5, 6));
        r.add(Range.create(1, 2));
    }

    private void checkIs(Ranges r, int... ords) {
        List<Range> list = new ArrayList<>();
        for (int i = 0; i < ords.length; i += 2) {
            list.add(Range.create(ords[i], ords[i + 1]));
        }
        assertEquals(list, Lists.newArrayList(r));
    }
}
