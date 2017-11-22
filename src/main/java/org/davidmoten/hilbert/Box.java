package org.davidmoten.hilbert;

import java.util.Arrays;

import com.github.davidmoten.guavamini.Preconditions;

final class Box {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(a);
        result = prime * result + Arrays.hashCode(b);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Box other = (Box) obj;
        if (!Arrays.equals(a, other.a))
            return false;
        if (!Arrays.equals(b, other.b))
            return false;
        return true;
    }

    final long[] a;
    final long[] b;

    Box(long[] a, long[] b) {
        Preconditions.checkArgument(a.length == b.length);
        this.a = a;
        this.b = b;
    }

    public int dimensions() {
        return a.length;
    }

    @Override
    public String toString() {
        return "Box [a=" + Arrays.toString(a) + ", b=" + Arrays.toString(b) + "]";
    }

    static Builder a(long... value) {
        return new Builder(value);
    }

    static final class Builder {
        long[] a;
        long[] b;

        Builder(long... a) {
            this.a = a;
        }

        Box b(long... values) {
            this.b = values;
            return new Box(a, b);
        }
    }

    public Box dropDimension(int dimension) {
        long[] x = dropDimension(a, dimension);
        long[] y = dropDimension(b, dimension);
        return new Box(x, y);
    }

    private static long[] dropDimension(long[] x, int dimension) {
        long[] y = new long[x.length - 1];
        for (int i = 0; i < x.length; i++) {
            if (i < dimension) {
                y[i] = x[i];
            } else if (i > dimension) {
                y[i - 1] = x[i];
            }
        }
        return y;
    }
}
