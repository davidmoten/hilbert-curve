package org.davidmoten.hilbert;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

import org.junit.Assert;
import org.junit.Test;

public class HilbertCurveTest {

    @Test
    public void testIndex1() {
        HilbertCurve c = HilbertCurve.bits(2).dimensions(2);
        assertEquals(7, c.index(1, 2).intValue());
    }

    @Test
    public void testIndex2() {
        HilbertCurve c = HilbertCurve.bits(5).dimensions(2);
        assertEquals(256, c.index(0, 16).intValue());
    }

    @Test
    public void testToBigInteger() {
        long[] ti = { 0, 16 };
        assertEquals(256, HilbertCurve.toBigInteger(5, ti).intValue());
    }

    @Test
    public void testBitSet() {
        BitSet b = new BitSet(10);
        b.set(8);
        byte[] a = b.toByteArray();
        Util.reverse(a);
        assertEquals(256, new BigInteger(1, a).intValue());
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
    public void testPrintOutIndexValues() {
        int bits = 5;
        HilbertCurve c = HilbertCurve.bits(bits).dimensions(2);
        int n = 2 << (bits - 1);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(c.index(i, j));
                System.out.print("\t");
            }
            System.out.println();
        }
    }

    @Test
    public void testTranspose() {
        HilbertCurve c = HilbertCurve.bits(5).dimensions(2);
        long[] ti = c.transpose(BigInteger.valueOf(256));
        assertEquals(2, ti.length);
        assertEquals(0, ti[0]);
        assertEquals(16, ti[1]);
    }

    @Test
    public void testTransposeZero() {
        HilbertCurve c = HilbertCurve.bits(5).dimensions(2);
        long[] ti = c.transpose(BigInteger.valueOf(0));
        assertEquals(2, ti.length);
        assertEquals(0, ti[0]);
        assertEquals(0, ti[1]);
    }

    @Test
    public void testPointFromIndexBits1() {
        int bits = 2;
        HilbertCurve c = HilbertCurve.bits(bits).dimensions(2);
        for (long i = 0; i < Math.round(Math.pow(2, bits)); i++) {
            System.out.println(i + "\t" + Arrays.toString(c.point(BigInteger.valueOf(i))));
        }
    }

    @Test
    public void testRoundTrips() {
        boolean failed = false;
        for (int bits = 1; bits <= 10; bits++) {
            for (int dimensions = 2; dimensions <= 10; dimensions++)
                for (long i = 0; i < Math.pow(2, bits + 1); i++) {
                    if (!checkRoundTrip(bits, dimensions, i)) {
                        System.out.println("failed round trip for bits=" + bits + ", dimensions="
                                + dimensions + ", index=" + i);
                        failed = true;
                    }
                }
        }
        if (failed) {
            Assert.fail("round trips failed (listed in log)");
        }
    }

    @Test
    public void testRoundTrip3Dimensions3BitsIndex1() {
        assertTrue(checkRoundTrip(3, 3, 1));
    }

    @Test
    public void testPointFromIndexBits1Point0_1() {
        HilbertCurve c = HilbertCurve.bits(1).dimensions(2);
        long[] ti = c.pointToTransposedIndex(0, 1);
        assertEquals("0,1", ti[0] + "," + ti[1]);
        assertEquals(1, c.index(0, 1).intValue());
        long[] ti2 = c.transpose(BigInteger.valueOf(1));
        assertEquals("0,1", ti2[0] + "," + ti2[1]);
    }

    @Test
    public void testPointFromIndexBits1Point1_1() {
        HilbertCurve c = HilbertCurve.bits(1).dimensions(2);
        long[] ti = c.pointToTransposedIndex(1, 1);
        assertEquals("1,0", ti[0] + "," + ti[1]);
        assertEquals(2, c.index(1, 1).intValue());
        long[] ti2 = c.transpose(BigInteger.valueOf(2));
        assertEquals("1,0", ti2[0] + "," + ti2[1]);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBitsPositive() {
        HilbertCurve.bits(0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDimensionAtLeastTwo() {
        HilbertCurve.bits(2).dimensions(1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPointMatchesDimensions() {
        HilbertCurve c = HilbertCurve.bits(2).dimensions(2);
        long[] point = {1};
        c.index(point);
    }
    
    @Test(expected = NullPointerException.class)
    public void testIndexCannotBeNull() {
        HilbertCurve c = HilbertCurve.bits(2).dimensions(2);
        c.point((BigInteger) null);
    }
    
    private static boolean checkRoundTrip(int bits, int dimensions, long value) {
        HilbertCurve c = HilbertCurve.bits(bits).dimensions(dimensions);
        long[] point = c.point(value);
        assertEquals(dimensions, point.length);
        return value == c.index(point).longValue();
    }

}
