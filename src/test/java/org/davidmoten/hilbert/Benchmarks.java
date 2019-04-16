package org.davidmoten.hilbert;

import java.util.ArrayList;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class Benchmarks {

    private static final int BITS = 10;
    private static final long N = 1L << BITS - 1;
    private static final int DIMENSIONS = 5;
    private static final HilbertCurve c = HilbertCurve.bits(BITS).dimensions(DIMENSIONS);
    private static final SmallHilbertCurve small = HilbertCurve.small().bits(BITS).dimensions(DIMENSIONS);
    private static final long[] point = new long[DIMENSIONS];
    private static final List<long[]> points = createPoints();

    @Benchmark
    public void roundTripTimes512(Blackhole b) {
        for (long i = 0; i < N; i++) {
            long[] point = c.point(i);
            b.consume(c.index(point).longValue());
        }
    }
    
    @Benchmark
    public void toIndexTimes512(Blackhole b) {
        for (int i = 0; i < N; i++) {
            b.consume(c.index(points.get(i)).longValue());
        }
    }
    
    @Benchmark
    public void toIndexTimes512Small(Blackhole b) {
        for (int i = 0; i < N; i++) {
            b.consume(small.index(points.get(i)));
        }
    }

    @Benchmark
    public void pointTimes512(Blackhole b) {
        for (long i = 0; i < N; i++) {
            b.consume(c.point(i));
        }
    }

    @Benchmark
    public void roundTripSmallTimes512(Blackhole b) {
        for (long i = 0; i < N; i++) {
            long[] point = small.point(i);
            b.consume(small.index(point));
        }
    }

    @Benchmark
    public void pointSmallTimes512(Blackhole b) {
        for (long i = 0; i < N; i++) {
            b.consume(small.point(i));
        }
    }

    @Benchmark
    public void roundTripTimes512LowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            c.point(i, point);
            b.consume(c.index(point).longValue());
        }
    }

    @Benchmark
    public void pointTimes512LowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            c.point(i, point);
            b.consume(point);
        }
    }

    @Benchmark
    public void roundTripSmallTimes512LowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            small.point(i, point);
            b.consume(small.index(point));
        }
    }

    @Benchmark
    public void pointSmallTimes512LowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            small.point(i, point);
            b.consume(point);
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