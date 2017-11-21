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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.guavamini.Preconditions;

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
            String actual = new String(
                    Files.readAllBytes(
                            new File("target/indexes-2d-bits-" + bits + ".txt").toPath()),
                    StandardCharsets.UTF_8);
            String expected = new String(Files.readAllBytes(
                    new File("src/test/resources/expected/indexes-2d-bits-" + bits + ".txt")
                            .toPath()),
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
    public void testRoundTripsSmall() {
        boolean failed = false;
        for (int bits = 1; bits <= 10; bits++) {
            for (int dimensions = 2; dimensions <= Math.min(5, 63 / bits); dimensions++)
                for (long i = 0; i < Math.pow(2, bits + 1); i++) {
                    if (!checkRoundTripSmall(bits, dimensions, i)) {
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

    private static boolean checkRoundTripSmall(int bits, int dimensions, long value) {
        SmallHilbertCurve c = HilbertCurve.small().bits(bits).dimensions(dimensions);
        long[] point = c.point(value);
        assertEquals(dimensions, point.length);
        return value == c.index(point);
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
    public void testSmallQueryLargerBoxDepthZero() {
        List<Range> ranges = small.query(point(0, 2), point(6, 8), 0);
        assertEquals(Arrays.asList(Range.create(14, 234)), ranges);
    }

    @Test
    public void testSmallQueryLargerBoxDepthMany() {
        List<Range> ranges = small.query(point(0, 2), point(6, 8), 4);
        System.out.println(ranges);
        assertEquals(Arrays.asList(Range.create(8, 41), Range.create(45, 46), Range.create(50, 55),
                Range.create(214, 214), Range.create(217, 218), Range.create(229, 230),
                Range.create(233, 234)), ranges);
    }

    @Test
    public void testIssue1() {
        int bits = 16;
        int dimensions = 2;
        long index = Math.round(Math.pow(2, bits * dimensions - 1) + 1);
        assertTrue(checkRoundTripSmall(bits, dimensions, index));
    }

    @Test
    public void testSydneyHarbourRangesForOneHourInADay() {
        int bits = 20;
        SmallHilbertCurve h = HilbertCurve.small().bits(bits).dimensions(3);
        long max = 1 << bits - 1;
        // top left -33.806477, 151.181767, t = 0
        // bottom right -33.882896, 151.281330, t = TimeUnit.HOURS.toMillis(1)
        List<Range> ranges = h.query2(scalePoint(-33.806477, 151.181767, 0, max),
                scalePoint(-33.882896, 151.281330, TimeUnit.HOURS.toMillis(1), max), 4);
        System.out.println(ranges.size());
        ranges.forEach(System.out::println);
        System.out.println("maxIndex = " + (1L << bits * 3));
        System.out.println((114172123465024476L - 114157502019852442L) / (double) (1L << bits * 3));
    }

    @Test
    public void testSplitIntoBoxesDepth1() {
        long[] a = new long[] { 1, 1 };
        long[] b = new long[] { 3, 2 };
        List<Box> x = SmallHilbertCurve.split(a, b, 1);
        System.out.println(x);
        assertEquals(Lists.newArrayList(Box.a(1, 1).b(1, 1), Box.a(1, 2).b(1, 2),
                Box.a(2, 1).b(3, 1), Box.a(2, 2).b(3, 2)), x);
    }

    @Test
    public void testSplitIntoBoxesDepth2() {
        long[] a = new long[] { 0, 0 };
        long[] b = new long[] { 15, 15 };
        List<Box> x = SmallHilbertCurve.split(a, b, 2);
        x.stream().forEach(System.out::println);

        assertEquals(Lists.newArrayList(Box.a(0, 0).b(3, 3), //
                Box.a(0, 4).b(3, 7), //
                Box.a(4, 0).b(7, 3), //
                Box.a(4, 4).b(7, 7), //
                Box.a(0, 8).b(3, 11), //
                Box.a(0, 12).b(3, 15), //
                Box.a(4, 8).b(7, 11), //
                Box.a(4, 12).b(7, 15), //
                Box.a(8, 0).b(11, 3), //
                Box.a(8, 4).b(11, 7), //
                Box.a(12, 0).b(15, 3), //
                Box.a(12, 4).b(15, 7), //
                Box.a(8, 8).b(11, 11), //
                Box.a(8, 12).b(11, 15), //
                Box.a(12, 8).b(15, 11), //
                Box.a(12, 12).b(15, 15)) //
                , x);
    }

    @Test
    public void testSplitWholeDomain() {
        int bits = 6;
        SmallHilbertCurve h = HilbertCurve.small().bits(bits).dimensions(2);
        long maxIndex = (1L << bits) - 1;
        for (int splits = 1; splits <= 5; splits++) {
            List<Range> ranges = h.query2(point(0, 0), point(maxIndex, maxIndex), splits);
            for (long i = 0; i < 1 << bits; i++) {
                final long x = i;
                assertTrue(ranges.stream().filter(r -> r.contains(x)).findFirst().isPresent());
            }
        }
    }

    @Test
    public void testToRange() {
        Box b = Box.a(0, 0).b(15, 15);
        SmallHilbertCurve h = HilbertCurve.small().bits(4).dimensions(2);
        Range r = h.toRange(b);
        assertEquals(Range.create(0, 255), r);
    }

    @Test
    public void testReduceWhenNoOverlapStaysSame() {
        List<Range> list = Lists.newArrayList(Range.create(0, 5), Range.create(7, 10));
        assertEquals(list, SmallHilbertCurve.reduce(list));
    }

    @Test
    public void testReduceWhenContiguousJoins() {
        List<Range> list = Lists.newArrayList(Range.create(0, 5), Range.create(6, 10));
        assertEquals(Lists.newArrayList(Range.create(0, 10)), SmallHilbertCurve.reduce(list));
    }

    @Test
    public void testReduceWhenOverlapJoins() {
        List<Range> list = Lists.newArrayList(Range.create(0, 5), Range.create(4, 10));
        assertEquals(Lists.newArrayList(Range.create(0, 10)), SmallHilbertCurve.reduce(list));
    }

    @Test
    public void testReduceWhenOneOverlapThenGap() {
        List<Range> list = Lists.newArrayList(Range.create(0, 5), Range.create(4, 10),
                Range.create(12, 13));
        assertEquals(Lists.newArrayList(Range.create(0, 10), Range.create(12, 13)),
                SmallHilbertCurve.reduce(list));
    }

    @Test
    public void testReduceWhenGapThenOverlap() {
        List<Range> list = Lists.newArrayList(Range.create(0, 5), Range.create(7, 10),
                Range.create(11, 13));
        assertEquals(Lists.newArrayList(Range.create(0, 5), Range.create(7, 13)),
                SmallHilbertCurve.reduce(list));
    }

    @Test
    public void testReduceEmpty() {
        List<Range> list = Collections.emptyList();
        assertEquals(list, SmallHilbertCurve.reduce(list));
    }

    private static long[] scalePoint(double lat, double lon, long time, long max) {
        long x = scale((lat + 90.0) / 180, max);
        long y = scale((lon + 180.0) / 360, max);
        long millisPerDay = TimeUnit.DAYS.toMillis(1);
        long z = scale(((double) time + millisPerDay / 2) / millisPerDay, max);
        return new long[] { x, y, z };
    }

    private static long scale(double d, long max) {
        Preconditions.checkArgument(d >= 0 && d <= 1);
        if (d == 1) {
            return max;
        } else {
            return Math.round(Math.floor(d * (max + 1)));
        }
    }

    private static long[] point(long... values) {
        return values;
    }

}
