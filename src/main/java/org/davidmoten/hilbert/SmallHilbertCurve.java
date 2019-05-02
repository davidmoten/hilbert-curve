package org.davidmoten.hilbert;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
     * @param point an array of {@code long}. Each coordinate can be between 0 and
     *              2<sup>bits</sup>-1.
     * @return index {@code long} in the range 0 to 2<sup>bits * dimensions</sup> -
     *         1
     * @throws IllegalArgumentException if length of point array is not equal to the
     *                                  number of dimensions.
     */
    public long index(long... point) {
        Preconditions.checkArgument(point.length == dimensions);
        return toIndex(HilbertCurve.transposedIndex(bits, point));
    }

    /**
     * Converts a {@code long} index (distance along the Hilbert Curve from 0) to a
     * point of dimensions defined in the constructor of {@code this}.
     * 
     * @param index index along the Hilbert Curve from 0. Maximum value 2 <sup>bits
     *              * dimensions</sup>-1.
     * @return array of longs being the point
     * @throws IllegalArgumentException if index is negative
     */
    public long[] point(long index) {
        return HilbertCurve.transposedIndexToPoint(bits, transposeLong(index));
    }

    public void point(long index, long[] x) {
        Util.zero(x);
        transposeLong(index, x);
        HilbertCurve.transposedIndexToPoint(bits, x);
    }

    // untranspose
    private long toIndex(long... transposedIndex) {
        long b = 0;
        int bIndex = length - 1;
        long mask = 1L << (bits - 1);
        for (int i = 0; i < bits; i++) {
            for (int j = 0; j < transposedIndex.length; j++) {
                if ((transposedIndex[j] & mask) != 0) {
                    b |= 1L << bIndex;
                }
                bIndex--;
            }
            mask >>= 1;
        }
        // b is expected to be BigEndian
        return b;
    }

    private void transposeLong(long index, long[] x) {
        for (int idx = 0; idx < 64; idx++) {
            if ((index & (1L << idx)) != 0) {
                int dim = (length - idx - 1) % dimensions;
                int shift = (idx / dimensions) % bits;
                x[dim] |= 1L << shift;
            }
        }
    }

    private long[] transposeLong(long index) {
        long[] x = new long[dimensions];
        transposeLong(index, x);
        return x;
    }

    /////////////////////////////////////////////////
    // Query support
    ////////////////////////////////////////////////

    public Ranges query(long[] a, long[] b, int maxRanges) {
        Preconditions.checkArgument(maxRanges >= 0);
        // TODO optimise this by joining ranges as they are found
        // instead of joining them at the end
        Ranges ranges = query(a, b);
        if (maxRanges >= ranges.size() || maxRanges == 0) {
            return ranges;
        } else {
            return ranges.join(ranges.size() - maxRanges);
        }
    }

    /**
     * Returns a list of index ranges exactly covering the region bounded by
     * {@code a} and {@code b}.
     * 
     * @param a one vertex of the region
     * @param b the opposing vertex to a
     */
    public Ranges query(long[] a, long[] b) {
        Box box = new Box(a, b);
        SortedSet<Long> set = new TreeSet<>();
        box.visitPerimeter(cell -> {
            long n = index(cell);
            set.add(n);
        });
        List<Long> list = new ArrayList<>(set);
        int i = 0;
        List<Range> ranges = new ArrayList<>();
        long rangeStart = -1;
        while (true) {
            if (i == list.size()) {
                break;
            }
            if (rangeStart == -1) {
                rangeStart = list.get(i);
            }
            while (i < list.size() - 1 && list.get(i + 1) == list.get(i) + 1) {
                i++;
            }
            if (i == list.size() - 1) {
                ranges.add(Range.create(rangeStart, list.get(i)));
                break;
            }
            long[] point = point(list.get(i) + 1);
            if (box.contains(point)) {
                // is not on the perimeter (would have been caught in previous while loop)
                // so is internal to the box which means the next value in the sorted hilbert
                // curve indexes for the perimiter must be where it exits
                i += 1;
            } else {
                ranges.add(Range.create(rangeStart, list.get(i)));
                rangeStart = -1;
                i++;
            }
        }
        return new Ranges(ranges);
    }

    public Ranges2 query2(long[] a, long[] b, int maxRanges) {
        Box box = new Box(a, b);
        SortedSet<Long> set = new TreeSet<>();
        box.visitPerimeter(cell -> {
            long n = index(cell);
            set.add(n);
        });
        List<Long> list = new ArrayList<>(set);
        int i = 0;
        Ranges2 ranges = new Ranges2(maxRanges);
        long rangeStart = -1;
        while (true) {
            if (i == list.size()) {
                break;
            }
            if (rangeStart == -1) {
                rangeStart = list.get(i);
            }
            while (i < list.size() - 1 && list.get(i + 1) == list.get(i) + 1) {
                i++;
            }
            if (i == list.size() - 1) {
                ranges.add(Range.create(rangeStart, list.get(i)));
                break;
            }
            long[] point = point(list.get(i) + 1);
            if (box.contains(point)) {
                // is not on the perimeter (would have been caught in previous while loop)
                // so is internal to the box which means the next value in the sorted hilbert
                // curve indexes for the perimiter must be where it exits
                i += 1;
            } else {
                ranges.add(Range.create(rangeStart, list.get(i)));
                rangeStart = -1;
                i++;
            }
        }
        return ranges;
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