package org.davidmoten.hilbert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class Benchmarks {

    private static final int BITS = 10;
    private static final int DIMENSIONS = 5;
    private static final HilbertCurve c = HilbertCurve.bits(BITS).dimensions(DIMENSIONS);
    private static final SmallHilbertCurve small = HilbertCurve.small().bits(BITS).dimensions(DIMENSIONS);
    private static final int N = (int) small.maxOrdinate();
    private static final long[] point = new long[DIMENSIONS];
    private static final List<long[]> points = createPoints();

    @Benchmark
    public void roundTripAllPoints10Bits1024Calls(Blackhole b) {
        for (long i = 0; i < N; i++) {
            long[] point = c.point(i);
            b.consume(c.index(point).longValue());
        }
    }

    @Benchmark
    public void toIndexAllPoints10Bits1024Calls(Blackhole b) {
        for (int i = 0; i < N; i++) {
            b.consume(c.index(points.get(i)).longValue());
        }
    }

    @Benchmark
    public void toIndexAllPoints10Bits1024CallsSmall(Blackhole b) {
        for (int i = 0; i < N; i++) {
            b.consume(small.index(points.get(i)));
        }
    }

    @Benchmark
    public void pointAllPoints10Bits1024Calls(Blackhole b) {
        for (long i = 0; i < N; i++) {
            b.consume(c.point(i));
        }
    }

    @Benchmark
    public void roundTripSmallAllPoints10Bits1024Calls(Blackhole b) {
        for (long i = 0; i < N; i++) {
            long[] point = small.point(i);
            b.consume(small.index(point));
        }
    }

    @Benchmark
    public void pointSmallAllPoints10Bits1024Calls(Blackhole b) {
        for (long i = 0; i < N; i++) {
            b.consume(small.point(i));
        }
    }

    @Benchmark
    public void roundTripAllPoints10Bits1024CallsLowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            c.point(i, point);
            b.consume(c.index(point).longValue());
        }
    }

    @Benchmark
    public void pointAllPoints10Bits1024CallsLowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            c.point(i, point);
            b.consume(point);
        }
    }

    @Benchmark
    public void roundTripSmallAllPoints10Bits1024CallsLowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            small.point(i, point);
            b.consume(small.index(point));
        }
    }

    @Benchmark
    public void pointSmallAllPoints10Bits1024CallsLowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            small.point(i, point);
            b.consume(point);
        }
    }

    private static final Query query = new Query();

    @Benchmark
    public Ranges querySydney() {
        return query.query();
    }

    @Benchmark
    public Ranges querySydneyMaxRanges8() {
        return query.query(8);
    }

    private static final class Query {
    	//query sydney region from whole world for one hour from midday from a day
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
        long[] point1 = GeoUtil.scalePoint(lat1, lon1, t1, minTime, maxTime, maxOrdinates);
        long[] point2 = GeoUtil.scalePoint(lat2, lon2, t2, minTime, maxTime, maxOrdinates);

        Ranges query() {
            return h.query(point1, point2);
        }

        Ranges query(int maxRanges) {
            return h.query(point1, point2, maxRanges);
        }
    }

    private static List<long[]> createPoints() {
        List<long[]> list = new ArrayList<>((int) N);
        for (long i = 0; i < N; i++) {
            list.add(c.point(i));
        }
        return list;
    }
}