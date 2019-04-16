package org.davidmoten.hilbert;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

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

    public void visitCells(Consumer<long[]> visitor) {
        long[] maxes = maxes(a, b);
        long[] mins = mins(a, b);
        long[] x = Arrays.copyOf(mins, mins.length);
        while (true) {
            visitor.accept(x);
            if (equals(x, maxes)) {
                break;
            } else {
                addOne(x, mins, maxes);
            }
        }
    }
    
    @VisibleForTesting
    static void addOne(long[] x,long[] mins, long[] maxes) {
        for (int i = x.length - 1; i>=0;i--) {
            if (x[i] != maxes[i]) {
                x[i]++;
                break;
            } else {
                x[i] = mins[i];
            }
        }
    }

    @VisibleForTesting
    static boolean equals(long[] a, long[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        } 
        return true;
    }

    private static long[] mins(long[] a, long[] b) {
        long[] c = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = Math.min(a[i], b[i]);
        }
        return c;
    }

    private static long[] maxes(long[] a, long[] b) {
        long[] c = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = Math.max(a[i], b[i]);
        }
        return c;
    }
}
