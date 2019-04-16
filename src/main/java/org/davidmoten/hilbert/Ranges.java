package org.davidmoten.hilbert;

import java.util.ArrayList;
import java.util.List;

//immutable
//TODO Test
class Ranges {
    
    private final List<Range> ranges;

    Ranges() {
        ranges = new ArrayList<>();
    }
    
    void add(long value) {
        int modifiedIndex = -1;
        for (int i = 0; i< ranges.size(); i++) {
            Range range = ranges.get(i);
            if (range.contains(value)) {
                throw new RuntimeException("unexpected");
            } else if (range.low() == value +1) {
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
        } else if (modifiedIndex < ranges.size() -1) {
            Range modified = ranges.get(modifiedIndex);
            Range r = ranges.get(modifiedIndex + 1);
            if (modified.high() == r.low() -1) {
                // join the two Range objects
                ranges.set(modifiedIndex, new Range(modified.low(), r.high()));
                ranges.remove(modifiedIndex + 1);
            }
        }
    }
    
    List<Range> get() {
        return ranges;
    }

}
