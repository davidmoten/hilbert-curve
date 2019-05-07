package org.davidmoten.hilbert;

import static org.davidmoten.hilbert.GeoUtil.scalePoint;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.github.davidmoten.guavamini.Lists;

public class HilbertCurveTest {

    private static final HilbertCurve c = HilbertCurve.bits(5).dimensions(2);

    private static final boolean FULL_DOMAIN_QUERY = false;

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
                for (long i = 0; i < 1 << bits + 1; i++) {
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
    public void testSmallQueryPerimeterAlgorithm() {
        Ranges r = small.query(point(0, 0), point(1, 1));
        assertEquals(1, r.size());
        assertEquals(Range.create(0, 3), r.get().get(0));
    }

    @Test
    public void testIssue1() {
        int bits = 16;
        int dimensions = 2;
        long index = Math.round(Math.pow(2, bits * dimensions - 1) + 1);
        assertTrue(checkRoundTripSmall(bits, dimensions, index));
    }

    @Test
    public void testTotalRangeExpandingWithIncreasingSplitDepth() {
        // get ranges for Sydney query to measure effectiveness
        float lat1 = -33.806477f;
        float lon1 = 151.181767f;
        long minTime = 1510779675000L;
        long maxTime = 1510876800000L;
        long t1 = minTime + (maxTime - minTime) / 2;
        float lat2 = -33.882896f;
        float lon2 = 151.281330f;
        long t2 = t1 + TimeUnit.HOURS.toMillis(1);
        int bits = 10;
        int dimensions = 3;
        SmallHilbertCurve h = HilbertCurve.small().bits(bits).dimensions(dimensions);
        long maxOrdinates = 1L << bits;
        long[] point1 = scalePoint(lat1, lon1, t1, minTime, maxTime, maxOrdinates);
        long[] point2 = scalePoint(lat2, lon2, t2, minTime, maxTime, maxOrdinates);
        Ranges ranges = h.query(point1, point2);
        DecimalFormat df = new DecimalFormat("0.00");
        DecimalFormat df2 = new DecimalFormat("00");
        for (int i = 0; i < ranges.get().size(); i++) {
            Ranges r = ranges.join(i);
            System.out.println(df2.format(r.get().size()) + "\t"
                    + df.format((double) r.totalLength() / ranges.totalLength()));
        }
        if (FULL_DOMAIN_QUERY) {
            long t = System.currentTimeMillis();
            h.query(new long[] { 0, 0, 0 },
                    new long[] { maxOrdinates, maxOrdinates, maxOrdinates });
            System.out.println("full domain query took " + (System.currentTimeMillis() - t) + "ms");
        }
        if (true) {
            long t = System.currentTimeMillis();
            int count = h.query(new long[] { 0, 0, 0 },
                    new long[] { maxOrdinates, maxOrdinates, maxOrdinates / 24 }).size();
            System.out.println("full domain query for first hour took "
                    + (System.currentTimeMillis() - t) + "ms with "+ count + " ranges");
        }
    }

    @Test
    public void testPointSaveAllocationsSmall() {
        long[] x = new long[2];
        SmallHilbertCurve c = HilbertCurve.small().bits(5).dimensions(2);
        c.point(682, x);
        assertEquals(31L, x[0]);
        assertEquals(31L, x[1]);

        c.point(100L, x);
        assertEquals(14L, x[0]);
        assertEquals(4L, x[1]);
    }

    @Test
    public void testSmallQueryVisitWholeRegion() {
        SmallHilbertCurve c = HilbertCurve.small().bits(5).dimensions(2);
        long[] a = new long[] { 0, 0 };
        long[] b = new long[] { 31, 31 };
        Ranges r = c.query(a, b);
        assertEquals(Lists.newArrayList(Range.create(0, 1023)), r.get());
    }

    @Test
    public void testSmallQueryVisitWholeRegionMultipleRanges() {
        SmallHilbertCurve c = HilbertCurve.small().bits(4).dimensions(2);
        long[] a = new long[] { 1, 1 };
        long[] b = new long[] { 7, 4 };
        Ranges result = c.query(a, b);
        Ranges expected = Ranges.create() //
                .add(2, 2) //
                .add(6, 13) //
                .add(17, 18) //
                .add(22, 32) //
                .add(35, 37) //
                .add(53, 54) //
                .add(57, 57);
        assertEquals(expected.get(), result.get());
    }

    @Test
    public void testPointSaveAllocations() {
        long[] x = new long[2];
        HilbertCurve c = HilbertCurve.bits(5).dimensions(2);
        c.point(682L, x);
        assertEquals(31L, x[0]);
        assertEquals(31L, x[1]);

        c.point(100L, x);
        assertEquals(14L, x[0]);
        assertEquals(4L, x[1]);
    }

    @Test
    public void testQuery1() {
        SmallHilbertCurve c = HilbertCurve.small().bits(5).dimensions(2);
        long[] point1 = new long[] { 3, 3 };
        long[] point2 = new long[] { 8, 10 };
        Ranges ranges = c.query(point1, point2, 1);
        assertEquals(Lists.newArrayList(Range.create(10, 229)), ranges.get());
    }

    @Test
    public void testQueryJoin0() {
        SmallHilbertCurve c = HilbertCurve.small().bits(5).dimensions(2);
        long[] point1 = new long[] { 3, 3 };
        long[] point2 = new long[] { 8, 10 };
        Ranges ranges = c.query(point1, point2, 0);
        ranges.stream().forEach(System.out::println);
    }

    @Test
    public void testQueryJoin3() {
        SmallHilbertCurve c = HilbertCurve.small().bits(5).dimensions(2);
        long[] point1 = new long[] { 3, 3 };
        long[] point2 = new long[] { 8, 10 };
        Ranges ranges = c.query(point1, point2, 3);
        ranges.stream().forEach(System.out::println);
    }

    @Test
    public void testQueryJoin6() {
        SmallHilbertCurve c = HilbertCurve.small().bits(5).dimensions(2);
        long[] point1 = new long[] { 3, 3 };
        long[] point2 = new long[] { 8, 10 };
        Ranges ranges = c.query(point1, point2, 6);
        ranges.stream().forEach(System.out::println);
    }

    private static long[] point(long... values) {
        return values;
    }

}
