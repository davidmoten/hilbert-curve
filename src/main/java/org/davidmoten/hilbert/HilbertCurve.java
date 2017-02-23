package org.davidmoten.hilbert;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

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
public final class HilbertCurve {

    private final int bits;
    private final int dimensions;
    private final int length;
    private final long N;
    private final long M;
    private final long initialMask;

    private HilbertCurve(int bits, int dimensions) {
        this.bits = bits;
        this.dimensions = dimensions;
        this.length = bits * dimensions;
        // cache a few calculated values
        this.N = 2L << (bits - 1);
        this.M = 1L << (bits - 1);
        this.initialMask = 1L << (bits - 1);
    }

    /**
     * Returns a builder for and object that performs transformations for a
     * Hilbert curve with the given number of bits.
     * 
     * @param bits
     *            depth of the Hilbert curve. If bits is one, this is the
     *            top-level Hilbert curve
     * @return builder for object to do transformations with the Hilbert Curve
     */
    public static HilbertCurveBuilder bits(int bits) {
        return new HilbertCurveBuilder(bits);
    }

    public static class HilbertCurveBuilder {
        final int bits;

        private HilbertCurveBuilder(int bits) {
            Preconditions.checkArgument(bits > 0, "bits must be greater than zero");
            this.bits = bits;
        }

        public HilbertCurve dimensions(int dimensions) {
            Preconditions.checkArgument(dimensions > 1, "dimensions must be at least 2");
            return new HilbertCurve(bits, dimensions);
        }
    }

    public BigInteger index(long... point) {
        Preconditions.checkArgument(point.length == dimensions);
        return toBigInteger(transposedIndex(point));
    }

    public long[] point(BigInteger index) {
        Preconditions.checkNotNull(index);
        Preconditions.checkArgument(index.signum() != -1, "index cannot be negative");
        return transposedIndexToPoint(transpose(index));
    }

    public long[] point(long index) {
        return point(BigInteger.valueOf(index));
    }

    /**
     * Returns the transposed representation of the Hilbert curve index.
     * 
     * <p>The Hilbert index is expressed internally as an array of transposed bits.
     * 
     * <pre>
      Example: 5 bits for each of n=3 coordinates.
         15-bit Hilbert integer = A B C D E F G H I J K L M N O is stored
         as its Transpose                        ^
         X[0] = A D G J M                    X[2]|  7
         X[1] = B E H K N        &lt;-------&gt;       | /X[1]
         X[2] = C F I L O                   axes |/
                high low                         0------&gt; X[0]
     * </pre>
     * 
     * @param index
     *            index to be tranposed
     * @return transposed index
     */
    @VisibleForTesting
    long[] transpose(BigInteger index) {
        byte[] bytes = index.toByteArray();
        Util.reverse(bytes);
        BitSet b = BitSet.valueOf(bytes);
        long[] x = new long[dimensions];
        for (int idx = 0; idx < b.length(); idx++) {
            if (b.get(idx)) {
                int dim = (length - idx - 1) % dimensions;
                int shift = (idx / dimensions) % bits;
                x[dim] |= 1 << shift;
            }
        }
        return x;
    }

    /**
     * <p>
     * Given the axes (coordinates) of a point in N-Dimensional space, find the
     * distance to that point along the Hilbert curve. That distance will be
     * transposed; broken into pieces and distributed into an array.
     *
     * <p>
     * The number of dimensions is the length of the hilbertAxes array.
     *
     * <p>
     * Note: In Skilling's paper, this function is called AxestoTranspose.
     * 
     * @param point
     *            Point in N-space
     * @return The Hilbert distance (or index) as a transposed Hilbert index
     */
    @VisibleForTesting
    long[] transposedIndex(long... point) {
        int n = point.length; // n: Number of dimensions
        long[] x = Arrays.copyOf(point, n);
        long p, q, t;
        int i;
        // Inverse undo
        for (q = M; q > 1; q >>= 1) {
            p = q - 1;
            for (i = 0; i < n; i++)
                if ((x[i] & q) != 0)
                    x[0] ^= p; // invert
                else {
                    t = (x[0] ^ x[i]) & p;
                    x[0] ^= t;
                    x[i] ^= t;
                }
        } // exchange
          // Gray encode
        for (i = 1; i < n; i++)
            x[i] ^= x[i - 1];
        t = 0;
        for (q = M; q > 1; q >>= 1)
            if ((x[n - 1] & q) != 0)
                t ^= q - 1;
        for (i = 0; i < n; i++)
            x[i] ^= t;

        return x;
    }

    /**
     * Converts the Hilbert transposed index into an N-dimensional point
     * expressed as a vector of {@code long}.
     * 
     * In Skilling's paper this function is named {@code TransposeToAxes}
     * 
     * @param transposedIndex
     *            distance along the Hilbert curve in transposed form
     * @return the coordinates of the point represented by the transposed index
     *         on the Hilbert curve
     */
    private long[] transposedIndexToPoint(long... x) {
        // Note that x is mutated by this method (as a performance improvement
        // to avoid allocation)
        int n = x.length; // number of dimensions
        long p, q, t;
        int i;
        // Gray decode by H ^ (H/2)
        t = x[n - 1] >> 1;
        // Corrected error in Skilling's paper on the following line. The
        // appendix had i >= 0 leading to negative array index.
        for (i = n - 1; i > 0; i--)
            x[i] ^= x[i - 1];
        x[0] ^= t;
        // Undo excess work
        for (q = 2; q != N; q <<= 1) {
            p = q - 1;
            for (i = n - 1; i >= 0; i--)
                if ((x[i] & q) != 0L)
                    x[0] ^= p; // invert
                else {
                    t = (x[0] ^ x[i]) & p;
                    x[0] ^= t;
                    x[i] ^= t;
                }
        } // exchange
        return x;
    }

    // Quote from Paul Chernoch
    // Interleaving means take one bit from the first matrix element, one bit
    // from the next, etc, then take the second bit from the first matrix
    // element, second bit from the second, all the way to the last bit of the
    // last element. Combine those bits in that order into a single BigInteger,
    // which can have as many bits as necessary. This converts the array into a
    // single number.
    @VisibleForTesting
    BigInteger toBigInteger(long... transposedIndex) {
        BitSet b = new BitSet(length);
        int bIndex = length - 1;
        long mask = initialMask;
        for (int i = 0; i < bits; i++) {
            for (int j = 0; j < transposedIndex.length; j++) {
                if ((transposedIndex[j] & mask) != 0) {
                    b.set(bIndex);
                }
                bIndex--;
            }
            mask >>= 1;
        }
        if (b.isEmpty())
            return BigInteger.ZERO;
        else {
            byte[] bytes = b.toByteArray();
            // make Big Endian
            Util.reverse(bytes);
            return new BigInteger(1, bytes);
        }
    }

}
