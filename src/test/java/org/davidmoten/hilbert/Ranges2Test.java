package org.davidmoten.hilbert;

import org.junit.Test;

public class Ranges2Test {

    @Test
    public void testRemovesCorrectRange() {

        Ranges2 r = new Ranges2(2);
        r.add(Range.create(1));
        r.add(Range.create(10));
        r.add(Range.create(11));
        r.add(Range.create(12));

        r.forEach(System.out::println);

    }

}
