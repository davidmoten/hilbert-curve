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
    public void testToString() {
        assertEquals("Box [[1, 2], [3, 4]]", new Box(new long[] { 1, 2 }, new long[] { 3, 4 }).toString());
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
    public void testVisitPerimeter() {
        long[] mins = new long[] { 1, 1, 1 };
        long[] maxes = new long[] { 3, 3, 3 };
        long[] x = new long[] { 1, 1, 1 };

        {
            List<String> list = new ArrayList<>();
            Box.visitPerimeter(mins, maxes, x, 2, y -> list.add(Arrays.toString(y)));
            assertEquals(
                    "[[1, 1, 1], [1, 2, 1], [1, 3, 1], [2, 1, 1], [2, 2, 1], [2, 3, 1], [3, 1, 1], [3, 2, 1], [3, 3, 1]]",
                    list.toString());
        }
        {
            List<String> list = new ArrayList<>();
            Box.visitPerimeter(mins, maxes, x, 1, y -> list.add(Arrays.toString(y)));
            assertEquals("[[1, 1, 2], [2, 1, 2], [3, 1, 2]]", list.toString());
        }
        {
            List<String> list = new ArrayList<>();
            Box.visitPerimeter(mins, maxes, x, 0, y -> list.add(Arrays.toString(y)));
            assertEquals("[[1, 2, 2]]", list.toString());
        }

    }

    @Test
    public void testCellVisitor() {
        List<String> list = new ArrayList<>();
        Box box = new Box(new long[] { 1, 6, 3 }, new long[] { 2, 7, 2 });
        box.visitCells(x -> list.add(Arrays.toString(x)));
        assertEquals(8, list.size());
        assertEquals("[1, 6, 2]", list.get(0));
        assertEquals("[1, 6, 3]", list.get(1));
        assertEquals("[1, 7, 2]", list.get(2));
        assertEquals("[1, 7, 3]", list.get(3));
        assertEquals("[2, 6, 2]", list.get(4));
        assertEquals("[2, 6, 3]", list.get(5));
        assertEquals("[2, 7, 2]", list.get(6));
        assertEquals("[2, 7, 3]", list.get(7));
    }

    @Test
    public void testPerimeterVisitor2D() {
        Box box = new Box(new long[] { 1, 1 }, new long[] { 3, 4 });
        List<long[]> list = new ArrayList<>();
        box.visitPerimeter(cell -> list.add(Arrays.copyOf(cell, cell.length)));
        assertEquals(10, list.size());
        assertContains(list, 1L, 1L);
        assertContains(list, 2L, 1L);
        assertContains(list, 3L, 1L);
        assertContains(list, 1L, 4L);
        assertContains(list, 2L, 4L);
        assertContains(list, 3L, 4L);
        assertContains(list, 1L, 2L);
        assertContains(list, 1L, 3L);
        assertContains(list, 3L, 2L);
        assertContains(list, 3L, 3L);
    }

    @Test
    public void testPerimeterVisitor2DPointOnly() {
        Box box = new Box(new long[] { 1, 1 }, new long[] { 1, 1 });
        List<long[]> list = new ArrayList<>();
        box.visitPerimeter(cell -> list.add(Arrays.copyOf(cell, cell.length)));
        assertEquals(1, list.size());
        assertContains(list, 1L, 1L);
    }

    @Test
    public void testPerimeterVisitor2DSmall() {
        Box box = new Box(new long[] { 1, 1 }, new long[] { 2, 1 });
        List<long[]> list = new ArrayList<>();
        box.visitPerimeter(cell -> list.add(Arrays.copyOf(cell, cell.length)));
        assertEquals(2, list.size());
        assertContains(list, 1L, 1L);
        assertContains(list, 2L, 1L);
    }

    @Test
    public void testPerimeterVisitor2DSmall2() {
        Box box = new Box(new long[] { 1, 2 }, new long[] { 1, 1 });
        List<long[]> list = new ArrayList<>();
        box.visitPerimeter(cell -> list.add(Arrays.copyOf(cell, cell.length)));
        assertEquals(2, list.size());
        assertContains(list, 1L, 1L);
        assertContains(list, 1L, 2L);
    }

    @Test
    public void testPerimeterVisitor3D() {
        Box box = new Box(new long[] { 1, 1, 1 }, new long[] { 3, 4, 1 });
        List<long[]> list = new ArrayList<>();
        box.visitPerimeter(cell -> list.add(Arrays.copyOf(cell, cell.length)));
        assertEquals(12, list.size());
        assertContains(list, 1L, 1L, 1L);
        assertContains(list, 1L, 2L, 1L);
        assertContains(list, 1L, 3L, 1L);
        assertContains(list, 1L, 4L, 1L);
        assertContains(list, 2L, 1L, 1L);
        assertContains(list, 2L, 2L, 1L);
        assertContains(list, 2L, 3L, 1L);
        assertContains(list, 2L, 4L, 1L);
        assertContains(list, 3L, 1L, 1L);
        assertContains(list, 3L, 2L, 1L);
        assertContains(list, 3L, 3L, 1L);
        assertContains(list, 3L, 4L, 1L);
    }

    private static void assertContains(List<long[]> list, long... v) {
        for (long[] x : list) {
            if (Arrays.equals(v, x)) {
                return;
            }
        }
        throw new AssertionError("value not found: " + Arrays.toString(v));
    }
}
