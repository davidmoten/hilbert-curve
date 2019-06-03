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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

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
                // don't use println because test may fail on Windows
                out.print("\n");
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

    public void smallHilbertCurveGetters() {
        SmallHilbertCurve h = HilbertCurve.small().bits(8).dimensions(3);
        assertEquals(8, h.bits());
        assertEquals(3, h.dimensions());
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
    public void testSmallQueryPerimeterAlgorithm() {
        Ranges r = small.query(point(0, 0), point(1, 1));
        assertEquals(1, r.size());
        assertEquals(Range.create(0, 3), r.toList().get(0));
    }

    @Test
    public void testIssue1() {
        int bits = 16;
        int dimensions = 2;
        long index = Math.round(Math.pow(2, bits * dimensions - 1) + 1);
        assertTrue(checkRoundTripSmall(bits, dimensions, index));
    }

    @Test
    public void testFullDomainSearch() {
        SmallHilbertCurve h = HilbertCurve.small().bits(5).dimensions(2);
        h.query(new long[] { 0, 0 }, new long[] { h.maxOrdinate(), h.maxOrdinate() });
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
        long[] point1 = scalePoint(lat1, lon1, t1, minTime, maxTime, h.maxOrdinate());
        long[] point2 = scalePoint(lat2, lon2, t2, minTime, maxTime, h.maxOrdinate());
        h.query(point1, point2);
        Range all = Range.create(0, (1 << bits * dimensions) - 1);
        {
            long t = System.currentTimeMillis();
            System.out.println("maxOrdinate=" + h.maxOrdinate());
            Ranges ranges = h.query(new long[] { 0, 0, 0 },
                    new long[] { h.maxOrdinate(), h.maxOrdinate(), h.maxOrdinate() });
            System.out.println("full domain query in 3 dimensions, 10 bits, took "
                    + (System.currentTimeMillis() - t) + "ms with " + ranges.size() + " ranges");
            assertEquals(1, ranges.size());
            assertEquals(all, ranges.iterator().next());
            ranges = h.query(new long[] { 0, 0, 0 },
                    new long[] { h.maxOrdinate(), h.maxOrdinate(), h.maxOrdinate() }, 12);
            System.out.println("full domain query in 3 dimensions, 10 bits, took "
                    + (System.currentTimeMillis() - t) + "ms with " + ranges.size() + " ranges");
            assertEquals(1, ranges.size());
            assertEquals(all, ranges.iterator().next());
            ranges = h.query(new long[] { 0, 0, 0 },
                    new long[] { h.maxOrdinate(), h.maxOrdinate(), h.maxOrdinate() }, 1);
            System.out.println("full domain query in 3 dimensions, 10 bits, took "
                    + (System.currentTimeMillis() - t) + "ms with " + ranges.size() + " ranges");
            assertEquals(1, ranges.size());
            assertEquals(all, ranges.iterator().next());
        }
        {
            long t = System.currentTimeMillis();
            int count = h
                    .query(new long[] { 0, 0, 0 },
                            new long[] { h.maxOrdinate(), h.maxOrdinate(), h.maxOrdinate() / 24 })
                    .size();
            System.out.println("full domain query in 3 dimensions, 10 bits, for first hour took "
                    + (System.currentTimeMillis() - t) + "ms with " + count + " ranges");
            assertEquals(295051, count);
        }
        {

            int maxRanges = 12;
            long[] p1 = Arrays.copyOf(point1, point1.length);
            long[] p2 = Arrays.copyOf(point2, point2.length);
            p1[2] = h.maxOrdinate() / 2;
            int windowMinutes = 60;
            p2[2] = h.maxOrdinate() * (12 * 60 + windowMinutes) / (24 * 60);
            int rangesCount = h.query(p1, p2).size();
            System.out.println(h.query(p1, p2));
            long t = System.currentTimeMillis();
            int count = h.query(p1, p2, maxRanges).size();
            System.out.println("sydney query in 3 dimensions, 10 bits, for one hour at midday took "
                    + (System.currentTimeMillis() - t) + "ms restricted to " + count
                    + " ranges from " + rangesCount + " ranges");
            assertEquals(maxRanges, count);
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
        assertEquals(Lists.newArrayList(Range.create(0, 1023)), r.toList());
    }

    @Test
    public void testSmallQueryVisitWholeRegionMultipleRanges() {
        SmallHilbertCurve c = HilbertCurve.small().bits(4).dimensions(2);
        long[] a = new long[] { 1, 1 };
        long[] b = new long[] { 7, 4 };
        Ranges result = c.query(a, b);
        Ranges expected = new Ranges(0).add(2, 2) //
                .add(6, 13) //
                .add(17, 18) //
                .add(22, 32) //
                .add(35, 37) //
                .add(53, 54) //
                .add(57, 57);
        assertEquals(expected.toList(), result.toList());
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
        assertEquals(Lists.newArrayList(Range.create(10, 229)), ranges.toList());
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

    @Test
    public void testMaxIndex() {
        SmallHilbertCurve h = HilbertCurve.small().bits(3).dimensions(2);
        assertEquals(63, h.maxIndex());
    }

    @Test
    public void testMaxOrdinate() {
        SmallHilbertCurve h = HilbertCurve.small().bits(3).dimensions(2);
        assertEquals(7, h.maxOrdinate());
    }

    @Test
    public void exhaustiveTestOfWholeDomain2DQueries() {
        for (int bits = 2; bits <= 4; bits++) {
            SmallHilbertCurve h = HilbertCurve.small().bits(bits).dimensions(2);
            TreeSet<Long> indexes = new TreeSet<>();
            List<Range> list = new ArrayList<>((int) h.maxIndex());
            for (int i = 0; i <= h.maxOrdinate(); i++) {
                for (int j = 0; j <= h.maxOrdinate(); j++) {
                    for (int k = 0; k <= h.maxOrdinate(); k++) {
                        for (int l = 0; l <= h.maxOrdinate(); l++) {
                            long[] a = new long[] { i, j };
                            long[] b = new long[] { k, l };
                            Ranges ranges = h.query(a, b);
                            indexes.clear();

                            // use a box visitor to check that the ranges are correct
                            Box box = new Box(a, b);
                            box.visitCells(cell -> indexes.add(h.index(cell)));
                            list.clear();
                            long rangeStart = indexes.pollFirst();
                            long rangeEnd = rangeStart;
                            while (true) {
                                Long v = indexes.pollFirst();
                                if (v == null) {
                                    list.add(Range.create(rangeStart, rangeEnd));
                                    break;
                                }
                                if (v == rangeEnd + 1) {
                                    rangeEnd = v;
                                } else {
                                    list.add(Range.create(rangeStart, rangeEnd));
                                    rangeStart = v;
                                    rangeEnd = v;
                                }
                            }
                            assertEquals(list, ranges.toList());
                        }
                    }
                }
            }
        }
    }

}
