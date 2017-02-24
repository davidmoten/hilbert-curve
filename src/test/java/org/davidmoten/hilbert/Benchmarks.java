package org.davidmoten.hilbert;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class Benchmarks {

    private static final int BITS = 10;
    private static final long N = Math.round(Math.pow(2, BITS)) - 1;
    private static final HilbertCurve c = HilbertCurve.bits(BITS).dimensions(5);

    @Benchmark
    public void roundTrip(Blackhole b) {
        for (long i = 0; i < N; i++) {
            long[] point = c.point(i);
            b.consume(i == c.index(point).longValue());
        }
    }
    
    @Benchmark
    public void point(Blackhole b) {
        for (long i = 0; i < N; i++) {
            b.consume(c.point(i));
        }
    }

}