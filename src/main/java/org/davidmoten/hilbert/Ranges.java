package org.davidmoten.hilbert;

import java.util.ArrayList;
import java.util.List;

//immutable
public final class Ranges {

    private final List<Range> ranges;

    Ranges(List<Range> ranges) {
        this.ranges = ranges;
    }

    public static Ranges create() {
        return new Ranges(new ArrayList<>());
    }

    static Builder builder() {
        return new Builder(new ArrayList<>());
    }

    static final class Builder {

        private List<Range> ranges;

        Builder(List<Range> ranges) {
            this.ranges = ranges;
        }

        void add(long value) {
            int modifiedIndex = -1;
            for (int i = 0; i < ranges.size(); i++) {
                Range range = ranges.get(i);
                if (range.contains(value)) {
                    throw new RuntimeException("unexpected");
                } else if (range.low() == value + 1) {
                    ranges.set(i, new Range(value, range.high()));
                    modifiedIndex = i;
                    break;
                } else if (range.high() == value - 1) {
                    ranges.set(i, new Range(range.low(), value));
                    modifiedIndex = i;
                    break;
                } else if (value < range.low()) {
                    ranges.add(i, new Range(value, value));
                    modifiedIndex = i;
                    break;
                }
            }
            if (modifiedIndex == -1) {
                ranges.add(new Range(value, value));
            } else if (modifiedIndex < ranges.size() - 1) {
                Range modified = ranges.get(modifiedIndex);
                Range r = ranges.get(modifiedIndex + 1);
                if (modified.high() == r.low() - 1) {
                    // join the two Range objects
                    ranges.set(modifiedIndex, new Range(modified.low(), r.high()));
                    ranges.remove(modifiedIndex + 1);
                }
            }
        }

        Ranges build() {
            return new Ranges(ranges);
        }
    }

    public List<Range> get() {
        return ranges;
    }

    public Ranges add(long low, long high) {
        Builder b = new Ranges.Builder(ranges);
        // TODO this is inefficient (but only used in unit tests)
        for (long i = low; i <= high; i++) {
            b.add(i);
        }
        return b.build();
    }

    public int size() {
        return ranges.size();
    }

}
