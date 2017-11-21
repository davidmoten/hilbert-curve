package org.davidmoten.hilbert;

import java.util.Arrays;

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
        this.a = a;
        this.b = b;
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
}
