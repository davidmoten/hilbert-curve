package org.davidmoten.hilbert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.guavamini.Preconditions;

/**
 * Converts between Hilbert index ({@code BigInteger}) and N-dimensional points.
 * 
 * <p>
 * Note: This algorithm is derived from work done by John Skilling and published
 * in "Programming the Hilbert curve". (c) 2004 American Institute of Physics.
 * With thanks also to Paul Chernoch who published a C# algorithm for Skilling's
 * work on StackOverflow and
 * <a href="https://github.com/paulchernoch/HilbertTransformation">GitHub</a>).
 */
public final class SmallHilbertCurve {

    private final int bits;
    private final int dimensions;
    private final int length;

    private SmallHilbertCurve(int bits, int dimensions) {
        this.bits = bits;
        this.dimensions = dimensions;
        this.length = bits * dimensions;
    }

    /**
     * Converts a point to its Hilbert curve index.
     * 
     * @param point
     *            an array of {@code long}. Each coordinate can be between 0 and
     *            2<sup>bits</sup>-1.
     * @return index {@code long} in the range 0 to 2<sup>bits *
     *         dimensions</sup> - 1
     * @throws IllegalArgumentException
     *             if length of point array is not equal to the number of
     *             dimensions.
     */
    public long index(long... point) {
        Preconditions.checkArgument(point.length == dimensions);
        return toIndex(HilbertCurve.transposedIndex(bits, point));
    }

    /**
     * Converts a {@code long} index (distance along the Hilbert Curve from 0)
     * to a point of dimensions defined in the constructor of {@code this}.
     * 
     * @param index
     *            index along the Hilbert Curve from 0. Maximum value 2
     *            <sup>bits * dimensions</sup>-1.
     * @return array of longs being the point
     * @throws IllegalArgumentException
     *             if index is negative
     */
    public long[] point(long index) {
        return HilbertCurve.transposedIndexToPoint(bits, transposeLong(index));
    }

    // untranspose
    private long toIndex(long... transposedIndex) {
        long b = 0;
        int bIndex = length - 1;
        long mask = 1L << (bits - 1);
        for (int i = 0; i < bits; i++) {
            for (int j = 0; j < transposedIndex.length; j++) {
                if ((transposedIndex[j] & mask) != 0) {
                    b |= 1 << bIndex;
                }
                bIndex--;
            }
            mask >>= 1;
        }
        // b is expected to be BigEndian
        return b;
    }

    private long[] transposeLong(long index) {
        long[] x = new long[dimensions];
        for (int idx = 0; idx < 64; idx++) {
            if ((index & (1L << idx)) != 0) {
                int dim = (length - idx - 1) % dimensions;
                int shift = (idx / dimensions) % bits;
                x[dim] |= 1L << shift;
            }
        }
        return x;
    }

    // TODO
    // Brute force is to travel the perimeter of the bounding box looking
    // for max and min index values and return a single range. This method
    // is potentially very wasteful because the Hilbert curve has locality
    // discontinuities recursively at divisors of 2 in the domain and it is
    // unnecessary to travel the whole perimeter as it can be solved more
    // efficiently using a recursive technique.
    //
    // Minimal force is to recursively break the bounding box up into
    // smaller boxes so that the discontinuities have progressively less
    // effect. We stop the recursive process when we have split the interval
    public List<Range> query(long[] a, long[] b, int splitDepth) {
        Preconditions.checkArgument(a.length == dimensions);
        Preconditions.checkArgument(b.length == dimensions);
        List<List<Range>> rangesByDimension = Lists.newArrayList();
        for (int i = 0; i < dimensions; i++) {
            rangesByDimension.add( //
                    new Range(Math.min(a[i], b[i]), //
                            Math.max(a[i], b[i])) //
                                    .split(splitDepth));
        }

        // combine coordinate ranges from each dimension and from boxes
        // determine the indexes of the corners of the boxes. The min max of the
        // box corner indexes are the ranges returned by this method.
        List<List<Range>> combined = combine(rangesByDimension, dimensions);
        return Lists.newArrayList();
    }

    private List<List<Range>> combine(List<List<Range>> rangesByDimension, int n) {
        Function<Integer, Integer> indexMax = i -> rangesByDimension.get(i).size();
        List<Range> ranges = new ArrayList<Range>();
        int[] indexes = new int[dimensions];
        do {
            // do something with indexes
            List<Range> rangesToCombine = new ArrayList<>();
            for (int i = 0; i < dimensions; i++) {
                rangesToCombine.add(rangesByDimension.get(i).get(indexes[i]));
            }

            // cross-product the point coordinates and calculate the min and max
            // hilbert indexes to get the range for this hyperrectangle
            for (int i = 0; i < Math.pow(2, dimensions); i++) {

            }
            for (int i = 0; i < dimensions; i++) {
            }

            // add one
            for (int i = 0; i < dimensions; i++) {
                indexes[i] = (indexes[i] + 1) % indexMax.apply(i);
                if (indexes[i] != 0) {
                    break;
                }
            }
        } while (!allZero(indexes));
        return Lists.newArrayList();
    }

    private static long[] lows(List<Range> list) {
        long[] x = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            x[i] = list.get(i).low();
        }
        return x;
    }

    private static long[] highs(List<Range> list) {
        long[] x = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            x[i] = list.get(i).high();
        }
        return x;
    }

    private static boolean allZero(int[] a) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != 0)
                return false;
        }
        return true;
    }

    public static final class Range {

        private final long low;
        private final long high;

        public Range(long low, long high) {
            this.low = low;
            this.high = high;
        }

        public long low() {
            return low;
        }

        public long high() {
            return high;
        }

        public List<Range> split() {
            long x = mostSignificantBetween(low, high);
            if (x == low || x == high) {
                return Collections.singletonList(this);
            } else {
                return Lists.newArrayList(new Range(low, x), new Range(x + 1, high));
            }
        }

        public List<Range> split(int n) {
            if (n <= 1) {
                return split();
            }
            List<Range> result = Lists.newArrayList();
            for (Range range : split()) {
                result.addAll(range.split(n - 1));
            }
            return result;
        }

        @Override
        public String toString() {
            return "Range [low=" + low + ", high=" + high + "]";
        }
    }

    static long mostSignificantBetween(long a, long b) {
        if (a > b) {
            return mostSignificantBetween(b, a);
        } else if (a == b) {
            return a;
        } else {
            long x = a + 1;
            int bit = 0;
            while (x < b) {
                if ((x & (1 << bit)) == 0) {
                    bit++;
                } else {
                    long y = x + (1 << bit);
                    if (y < b) {
                        bit++;
                        x = y;
                    } else {
                        break;
                    }
                }
            }
            return x;
        }
    }

    public static final class Builder {
        private int bits;

        Builder() {
            // private instantiation
        }

        public Builder bits(int bits) {
            this.bits = bits;
            return this;
        }

        public SmallHilbertCurve dimensions(int dimensions) {
            Preconditions.checkArgument(bits * dimensions <= 63,
                    "bits * dimensions must be less than or equal to 63");
            return new SmallHilbertCurve(bits, dimensions);
        }

    }
}