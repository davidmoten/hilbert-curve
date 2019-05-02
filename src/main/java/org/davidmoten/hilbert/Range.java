package org.davidmoten.hilbert;

import java.util.Collections;
import java.util.List;

import com.github.davidmoten.guavamini.Lists;

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

    public List<Range> split() {
        if (low == high) {
            return Collections.singletonList(this);
        }
        long x = Util.mostSignificantBetween(low + 1, high + 1) - 1;
        if (x == low) {
            return Lists.newArrayList(Range.create(low, low), Range.create(low + 1, high));
        } else {
            return Lists.newArrayList(Range.create(low, x), Range.create(x + 1, high));
        }
    }

    public List<Range> split(int n) {
        if (n == 0) {
            return Collections.singletonList(this);
        } else if (n == 1) {
            return split();
        }
        List<Range> split = split();
        if (split.size() == 1) {
            return split;
        } else {
            List<Range> result = Lists.newArrayList();
            for (Range range : split()) {
                result.addAll(range.split(n - 1));
            }
            return result;
        }
    }

    static List<Range> simplify(List<Range> list) {
        // mutates list!
        Collections.sort(list, (a, b) -> Long.compare(a.low(), b.low()));
        int i = 1;
        while (i < list.size()) {
            Range previous = list.get(i - 1);
            Range current = list.get(i);
            if (previous.high() >= current.low() - 1) {
                list.set(i - 1, Range.create(previous.low(), current.high()));
                list.remove(i);
            } else {
                i++;
            }
        }
        return list;
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
