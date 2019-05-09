package org.davidmoten.hilbert;

public final class Range {

    private final long low;
    private final long high;

    public Range(long low, long high) {
        this.low = Math.min(low, high);
        this.high = Math.max(low, high);
    }

    public static Range create(long low, long high) {
        return new Range(low, high);
    }

    public static Range create(long value) {
        return new Range(value, value);
    }

    public long low() {
        return low;
    }

    public long high() {
        return high;
    }

    public boolean contains(long value) {
        return low <= value && value <= high;
    }

    @Override
    public String toString() {
        return "Range [low=" + low + ", high=" + high + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (high ^ (high >>> 32));
        result = prime * result + (int) (low ^ (low >>> 32));
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
        Range other = (Range) obj;
        if (high != other.high)
            return false;
        if (low != other.low)
            return false;
        return true;
    }

    public Range join(Range range) {
        return Range.create(Math.min(low, range.low), Math.max(high, range.high));
    }

}
