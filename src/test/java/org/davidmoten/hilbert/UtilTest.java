package org.davidmoten.hilbert;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class UtilTest {

    @Test
    public void isUtilClass() {
        Asserts.assertIsUtilityClass(Util.class);
    }

    @Test
    public void testReverseOddNumberOfElements() {
        byte[] bytes = { 1, 2, 3, 4, 5 };
        Util.reverse(bytes);
        assertArrayEquals(new byte[] { 5, 4, 3, 2, 1 }, bytes);
    }

    @Test
    public void testReverseEventNumberOfElements() {
        byte[] bytes = { 1, 2, 3, 4, 5, 6 };
        Util.reverse(bytes);
        assertArrayEquals(new byte[] { 6, 5, 4, 3, 2, 1 }, bytes);
    }

    @Test
    public void testReverseOfNullDoesNotThrow() {
        Util.reverse(null);
    }

    @Test
    public void testMostSignificantBetweenLong() {
        // want to find the binary number with the most trailing zeroes x
        // s.t. a < x < b
        assertEquals(6, (long) Util.mostSignificantBetween(5, 7));
        assertEquals(4, (long) Util.mostSignificantBetween(3, 7));
        assertEquals(4, (long) Util.mostSignificantBetween(3, 8));
        assertEquals(16, (long) Util.mostSignificantBetween(3, 18));
        assertEquals(3, (long) Util.mostSignificantBetween(3, 4));
        assertEquals(8, (long) Util.mostSignificantBetween(0, 16));
        assertEquals(72, (long) Util.mostSignificantBetween(71, 78));
        assertEquals(2, (long) Util.mostSignificantBetween(2, 2));
        assertEquals(3, (long) Util.mostSignificantBetween(3, 3));
        assertEquals(8, (long) Util.mostSignificantBetween(16, 0));
    }

}
