package org.davidmoten.hilbert;

import static org.junit.Assert.assertArrayEquals;

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

}
