package org.davidmoten.hilbert;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class BoxTest {

    @Test
    public void testEquals() {
        assertTrue(Box.equals(new long[] { 1, 2, 3 }, new long[] { 1, 2, 3 }));
    }

    @Test
    public void testEqualsFalse() {
        assertFalse(Box.equals(new long[] { 1, 2, 3 }, new long[] { 1, 2, 4 }));
    }

    @Test
    public void testAddOne() {
        long[] mins = new long[] { 1, 2, 3 };
        long[] maxes = new long[] { 1, 4, 4 };
        long[] x = new long[] { 1, 2, 3 };
        Box.addOne(x, mins, maxes);
        assertArrayEquals(new long[] { 1, 2, 4 }, x);
        Box.addOne(x, mins, maxes);
        assertArrayEquals(new long[] { 1, 3, 3 }, x);
        Box.addOne(x, mins, maxes);
        assertArrayEquals(new long[] { 1, 3, 4 }, x);
        Box.addOne(x, mins, maxes);
        assertArrayEquals(new long[] { 1, 4, 3 }, x);
        Box.addOne(x, mins, maxes);
        assertArrayEquals(new long[] { 1, 4, 4 }, x);
        Box.addOne(x, mins, maxes);
        assertArrayEquals(new long[] { 1, 2, 3 }, x);
    }

    @Test
    public void testCellVisitor() {
        List<String> list = new ArrayList<>();
        Box box = new Box(new long[] { 1, 6, 3 }, new long[] { 2, 7, 2 });
        box.visitCells(x -> list.add(Arrays.toString(x)));
        box.visitCells(x -> System.out.println(Arrays.toString(x)));
        assertEquals(8, list.size());
        
    }
}
