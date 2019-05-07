package org.davidmoten.hilbert;

import static org.junit.Assert.assertEquals;

import org.davidmoten.hilbert.Ranges.Builder;
import org.junit.Test;

import com.github.davidmoten.guavamini.Lists;

public class RangesTest {
    
    @Test
    public void test() {
         Builder r = Ranges.builder();
        r.add(1);
        assertEquals(Lists.newArrayList(Range.create(1, 1)), r.build().get());
        r.add(2);
        assertEquals(Lists.newArrayList(Range.create(1, 2)), r.build().get());
        r.add(4);
        assertEquals(Lists.newArrayList(Range.create(1, 2), Range.create(4)), r.build().get());
        r.add(3);
        assertEquals(Lists.newArrayList(Range.create(1, 4)), r.build().get());
        r.add(0);
        assertEquals(Lists.newArrayList(Range.create(0, 4)), r.build().get());
        r.add(-2);
        assertEquals(Lists.newArrayList(Range.create(-2), Range.create(0, 4)), r.build().get());
    }

    
}
