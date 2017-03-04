package org.davidmoten.hilbert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.davidmoten.hilbert.SmallHilbertCurve.Range;
import org.junit.Assert;
import org.junit.Test;

import com.github.davidmoten.guavamini.Lists;

public class HilbertCurveTest {

    private static final HilbertCurve c = HilbertCurve.bits(5).dimensions(2);

    private static SmallHilbertCurve small = HilbertCurve.small().bits(5).dimensions(2);

    @Test
    public void testIndex1() {
        HilbertCurve c = HilbertCurve.bits(2).dimensions(2);
        assertEquals(7, c.index(1, 2).intValue());
    }

    @Test
    public void testIndex2() {
        assertEquals(256, c.index(0, 16).intValue());
    }

    @Test
    public void testToBigInteger() {
        long[] ti = { 0, 16 };
        assertEquals(256, c.toIndex(ti).intValue());
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
    public void testPrintOutIndexValues() throws IOException {
        for (int bits = 1; bits <= 7; bits++) {
            HilbertCurve c = HilbertCurve.bits(bits).dimensions(2);
            int n = 2 << (bits - 1);
            PrintStream out = new PrintStream("target/indexes-2d-bits-" + bits + ".txt");
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    out.print(c.index(i, j));
                    if (j != n - 1)
                        out.print("\t");
                }
                out.println();
            }
            out.close();
            String actual = new String(Files.readAllBytes(new File("target/indexes-2d-bits-" + bits + ".txt").toPath()),
                    StandardCharsets.UTF_8);
            String expected = new String(
                    Files.readAllBytes(
                            new File("src/test/resources/expected/indexes-2d-bits-" + bits + ".txt").toPath()),
                    StandardCharsets.UTF_8);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testTranspose() {
        long[] ti = c.transpose(BigInteger.valueOf(256));
        assertEquals(2, ti.length);
        assertEquals(0, ti[0]);
        assertEquals(16, ti[1]);
    }

    @Test
    public void testTransposeZero() {
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
                        System.out.println(
                                "failed round trip for bits=" + bits + ", dimensions=" + dimensions + ", index=" + i);
                        failed = true;
                    }
                }
        }
        if (failed) {
            Assert.fail("round trips failed (listed in log)");
        }
    }

    @Test
    public void testRoundTripsLong() {
        boolean failed = false;
        for (int bits = 1; bits <= 10; bits++) {
            for (int dimensions = 2; dimensions <= Math.min(5, 63 / bits); dimensions++)
                for (long i = 0; i < Math.pow(2, bits + 1); i++) {
                    if (!checkRoundTripLong(bits, dimensions, i)) {
                        System.out.println(
                                "failed round trip for bits=" + bits + ", dimensions=" + dimensions + ", index=" + i);
                        failed = true;
                    }
                }
        }
        if (failed) {
            Assert.fail("round trips failed (listed in log)");
        }
    }

    @Test
    public void testPointManyBits() {
        HilbertCurve c = HilbertCurve.bits(60).dimensions(3);
        String num = "1000000000000000000000000000000000000000000000";
        System.out.println("length = " + num.length());
        BigInteger n = new BigInteger(num);
        System.out.println(Arrays.toString(c.point(n)));
        assertEquals(n, c.index(c.point(n)));
    }

    @Test
    public void testRoundTripLotsOfBits() {
        for (int i = 1; i <= 100000; i++)
            assertTrue(checkRoundTrip(63, 10, i));
    }

    @Test
    public void testRoundTripLotsOfDimensions() {
        for (int i = 1; i <= 100; i++)
            assertTrue(checkRoundTrip(63, 10000, i));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooManyBits() {
        HilbertCurve.bits(64);
    }

    @Test
    public void testRoundTrip3Dimensions3BitsIndex1() {
        assertTrue(checkRoundTrip(3, 3, 1));
    }

    @Test
    public void checkPathIsSingleStepOnly() {
        for (int bits = 1; bits <= 10; bits++) {
            for (int dimensions = 2; dimensions <= 10; dimensions++) {
                HilbertCurve c = HilbertCurve.bits(bits).dimensions(dimensions);
                long[] point = c.point(0);
                for (long i = 1; i < Math.pow(2, bits + 1); i++) {
                    long[] point2 = c.point(i);
                    int sum = 0;
                    for (int j = 0; j < point.length; j++) {
                        sum += Math.abs(point2[j] - point[j]);
                    }
                    assertEquals(1, sum);
                    point = point2;
                }
            }
        }
    }

    @Test
    public void testPointFromIndexBits1Point0_1() {
        HilbertCurve c = HilbertCurve.bits(1).dimensions(2);
        long[] ti = HilbertCurve.transposedIndex(1, 0, 1);
        assertEquals("0,1", ti[0] + "," + ti[1]);
        assertEquals(1, c.index(0, 1).intValue());
        long[] ti2 = c.transpose(BigInteger.valueOf(1));
        assertEquals("0,1", ti2[0] + "," + ti2[1]);
    }

    @Test
    public void testPointFromIndexBits1Point0_1MutateAllowed() {
        HilbertCurve c = HilbertCurve.bits(1).dimensions(2);
        long[] ti = HilbertCurve.transposedIndex(1, 0, 1);
        assertEquals("0,1", ti[0] + "," + ti[1]);
        assertEquals(1, c.index(0, 1).intValue());
        long[] ti2 = c.transpose(BigInteger.valueOf(1));
        assertEquals("0,1", ti2[0] + "," + ti2[1]);
    }

    @Test
    public void testPointFromIndexBits1Point1_1() {
        HilbertCurve c = HilbertCurve.bits(1).dimensions(2);
        long[] ti = HilbertCurve.transposedIndex(1, 1, 1);
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
        long[] point = { 1 };
        c.index(point);
    }

    @Test(expected = NullPointerException.class)
    public void testIndexCannotBeNull() {
        HilbertCurve c = HilbertCurve.bits(2).dimensions(2);
        c.point((BigInteger) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexCannotBeNegative() {
        HilbertCurve c = HilbertCurve.bits(2).dimensions(2);
        c.point(BigInteger.valueOf(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void smallBitsTimesDimensionsMustBeLessThan63() {
        HilbertCurve.small().bits(8).dimensions(8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void smallPointDimensionsForSmallCurve() {
        SmallHilbertCurve c = HilbertCurve.small().bits(8).dimensions(2);
        c.index(1, 2, 3);
    }

    private static boolean checkRoundTrip(int bits, int dimensions, long value) {
        HilbertCurve c = HilbertCurve.bits(bits).dimensions(dimensions);
        long[] point = c.point(value);
        assertEquals(dimensions, point.length);
        return value == c.index(point).longValue();
    }

    private static boolean checkRoundTripLong(int bits, int dimensions, long value) {
        SmallHilbertCurve c = HilbertCurve.small().bits(bits).dimensions(dimensions);
        long[] point = c.point(value);
        assertEquals(dimensions, point.length);
        return value == c.index(point);
    }

    @Test
    public void testMostSignificantBetweenLong() {
        // want to find the binary number with the most trailing zeroes x
        // s.t. a < x < b
        assertEquals(6, (long) SmallHilbertCurve.mostSignificantBetween(5, 7));
        assertEquals(4, (long) SmallHilbertCurve.mostSignificantBetween(3, 7));
        assertEquals(4, (long) SmallHilbertCurve.mostSignificantBetween(3, 8));
        assertEquals(16, (long) SmallHilbertCurve.mostSignificantBetween(3, 18));
        assertEquals(3, (long) SmallHilbertCurve.mostSignificantBetween(3, 4));
        assertEquals(8, (long) SmallHilbertCurve.mostSignificantBetween(0, 16));
        assertEquals(72, (long) SmallHilbertCurve.mostSignificantBetween(71, 78));
        assertEquals(2, (long) SmallHilbertCurve.mostSignificantBetween(2, 2));
        assertEquals(8, (long) SmallHilbertCurve.mostSignificantBetween(16, 0));
    }

    @Test
    public void testSplitOnHigh() {
        List<Range> list = new Range(3, 7).split(1);
        System.out.println(list);
        assertEquals(Lists.newArrayList( //
                Range.create(3, 3), Range.create(4, 7)), list);
    }

    @Test
    public void testSplitOnLow() {
        List<Range> list = new Range(3, 6).split(1);
        System.out.println(list);
        assertEquals(Lists.newArrayList( //
                Range.create(3, 3), //
                Range.create(4, 6)), list);
    }

    @Test
    public void testSplitOnMiddle() {
        List<Range> list = new Range(5, 10).split(1);
        System.out.println(list);
        assertEquals(Lists.newArrayList( //
                Range.create(5, 7), //
                Range.create(8, 10)), list);
    }

    @Test
    public void testSmallQuery() {
        List<Range> r = small.query(point(0, 0), point(1, 1), 0);
        assertEquals(1, r.size());
        assertEquals(Range.create(0, 3), r.get(0));
    }

    @Test
    public void testSmallQueryNoSplit() {
        List<Range> r = small.query(point(0, 3), point(4, 2), 0);
        assertEquals(1, r.size());
        assertEquals(Range.create(14, 54), r.get(0));
    }

    @Test
    public void testSmallQuerySplitDepth3() {
        List<Range> r = small.query(point(0, 3), point(4, 2), 3);
        System.out.println(r);
        assertEquals(Arrays.asList(Range.create(8, 15), Range.create(53, 54)), r);
    }

    @Test
    public void testSmallQuery4() {
        List<Range> ranges = small.query(point(0, 2), point(6, 8), 0);
        assertEquals(Arrays.asList(Range.create(14, 234)), ranges);
    }

    @Test
    public void testSmallQuery5() {
        List<Range> ranges = small.query(point(0, 2), point(6, 8), 1);
        assertEquals(Arrays.asList(Range.create(14, 234)), ranges);
    }

    private static long[] point(long... values) {
        return values;
    }

}
