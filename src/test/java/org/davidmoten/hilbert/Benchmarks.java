package org.davidmoten.hilbert;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class Benchmarks {

    private static final int BITS = 10;
    private static final long N = 1L << BITS - 1;
    private static final HilbertCurve c = HilbertCurve.bits(BITS).dimensions(5);
    private static final SmallHilbertCurve small = HilbertCurve.small().bits(BITS).dimensions(5);
    private static final long[] point = new long[5];

    @Benchmark
    public void roundTripTimes1000(Blackhole b) {
        for (long i = 0; i < N; i++) {
            long[] point = c.point(i);
            b.consume(i == c.index(point).longValue());
        }
    }

    @Benchmark
    public void pointTimes1000(Blackhole b) {
        for (long i = 0; i < N; i++) {
            b.consume(c.point(i));
        }
    }

    @Benchmark
    public void roundTripSmallTimes1000(Blackhole b) {
        for (long i = 0; i < N; i++) {
            long[] point = small.point(i);
            b.consume(i == small.index(point));
        }
    }

    @Benchmark
    public void pointSmallTimes1000(Blackhole b) {
        for (long i = 0; i < N; i++) {
            b.consume(small.point(i));
        }
    }

    @Benchmark
    public void roundTripTimes1000LowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            c.point(i, point);
            b.consume(i == c.index(point).longValue());
        }
    }

    @Benchmark
    public void pointTimes1000LowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            c.point(i, point);
            b.consume(point);
        }
    }

    @Benchmark
    public void roundTripSmallTimes1000LowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            small.point(i, point);
            b.consume(i == small.index(point));
        }
    }

    @Benchmark
    public void pointSmallTimes1000LowAllocation(Blackhole b) {
        for (long i = 0; i < N; i++) {
            small.point(i, point);
            b.consume(point);
        }
    }

}