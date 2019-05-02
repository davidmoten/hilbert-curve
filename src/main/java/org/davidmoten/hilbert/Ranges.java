package org.davidmoten.hilbert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.davidmoten.guavamini.Preconditions;

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

    public Ranges join(int n) {
        Preconditions.checkArgument(n >= 0);
        if (n == 0) {
            return this;
        } else {
            // TODO replace this with an efficient algorithm like a Max Heap which is kept
            // at size k so runtime complexity is O(n + klogk)
            Ranges r = this;
            for (int i = 0; i < n; i++) {
                r = r.joinOnePair();
            }
            return r;
        }
    }

    private Ranges joinOnePair() {
        // find the smallest gap and join those ranges
        int smallestGapIndex = -1;
        {
            long smallestGap = Long.MAX_VALUE;
            for (int i = 1; i < ranges.size(); i++) {
                long gap = ranges.get(i).low() - ranges.get(i - 1).high();
                if (gap < smallestGap) {
                    smallestGap = gap;
                    smallestGapIndex = i - 1;
                }
            }
        }
        if (smallestGapIndex != -1) {
            List<Range> list = new ArrayList<>(ranges.size() - 1);
            for (int i = 0; i < smallestGapIndex; i++) {
                list.add(ranges.get(i));
            }
            list.add(Range.create(//
                    ranges.get(smallestGapIndex).low(), ranges.get(smallestGapIndex + 1).high()));
            for (int i = smallestGapIndex + 2; i < ranges.size(); i++) {
                list.add(ranges.get(i));
            }
            return new Ranges(list);
        } else {
            return this;
        }
    }

    public int size() {
        return ranges.size();
    }

    public Stream<Range> stream() {
        return ranges.stream();
    }

    public long totalLength() {
        return ranges //
                .stream() //
                .map(x -> x.high() - x.low() + 1) //
                .collect(Collectors.reducing((x, y) -> x + y)) //
                .orElse(0L);
    }

}
