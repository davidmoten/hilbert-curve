package org.davidmoten.hilbert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.davidmoten.hilbert.internal.util.BoundedPriorityQueue;

import com.github.davidmoten.guavamini.Preconditions;

public class Ranges2 {

    private final int maxRanges;
    private final List<Range> ranges;
    private final TreeSet<RangeWithDistanceToNext> set;
    private static final Comparator<RangeWithDistanceToNext> COMPARATOR = (a,
            b) -> a.distance < b.distance ? -1 : (a.distance == b.distance ? 0 : 1);

    public Ranges2(int maxRanges) {
        this.maxRanges = maxRanges;
        this.ranges = new ArrayList<>(maxRanges);
        this.set = new TreeSet<>(COMPARATOR);
    }

    public void add(Range r) {
        Preconditions
                .checkArgument(ranges.isEmpty() || ranges.get(ranges.size() - 1).high() <= r.low());
        ranges.add(r);
        if (ranges.size() > 1) {
            {
                RangeWithDistanceToNext x = new RangeWithDistanceToNext( //
                        ranges.size() - 2, //
                        r.low() - ranges.get(ranges.size() - 2).high());
                set.add(x);
            }
            if (ranges.size() > maxRanges) {
                // join the range with the smallest distance to next
                RangeWithDistanceToNext x = set.first();
                Range y = ranges.get(x.index).join(ranges.get(x.index + 1));
                ranges.set(x.index, y);
                ranges.remove(x.index + 1);
                set.remove(new RangeWithDistanceToNext(x.index, -1));
                if (x.index < ranges.size() - 1) {
                    set.add(new RangeWithDistanceToNext(x.index,
                            ranges.get(x.index + 1).low() - y.high()));
                }
            }
        }
    }

    private static final class RangeWithDistanceToNext {

        final int index;
        final long distance; // not involved in equals, hashCode

        RangeWithDistanceToNext(int index, long distance) {
            Preconditions.checkArgument(distance > 0);
            this.index = index;
            this.distance = distance;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + index;
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
            RangeWithDistanceToNext other = (RangeWithDistanceToNext) obj;
            if (index != other.index)
                return false;
            return true;
        }

    }

}
