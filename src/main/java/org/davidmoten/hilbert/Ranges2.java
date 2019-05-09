package org.davidmoten.hilbert;

import java.util.Iterator;
import java.util.TreeSet;

import com.github.davidmoten.guavamini.Preconditions;

// NotThreadSafe
public class Ranges2 implements Iterable<Range> {

    private final int bufferSize;

    // set is ordered by increasing distance to next node (Node is a linked list)
    private final TreeSet<Node> set;
    private Node ranges; // in descending order of ranges e.g. Range(5,7) -> Range(1,3)
    private int count; // count of items in ranges

    public Ranges2(int bufferSize) {
        Preconditions.checkArgument(bufferSize > 1);
        this.bufferSize = bufferSize;
        this.ranges = null;
        this.set = new TreeSet<>();
    }

    public void add(Range r) {
        Preconditions.checkArgument(ranges == null || ranges.value.high() < r.low(),
                "ranges must be added in increasing order and without overlap");
        Node node = new Node(r);
        count++;
        if (ranges == null) {
            ranges = node;
        } else {
            // and set new head and recalculate distance for ranges
            node.setNext(ranges);

            // add old head to set (now that the distanceToPrevious has been calculated) 
            set.add(ranges);

            ranges = node;

            if (count > bufferSize) {
                // remove node from set with least distance to next node
                Node first = set.pollFirst();

                // replace that node in linked list (ranges) with a new Node
                // that has the concatenation of that node with previous node's range
                // also remove its predecessor 

                // first.previous will not be null because distance was present to be in set
                Range joined = first.value.join(first.previous().value);

                Node n = new Node(joined);
                // link and recalculate distance (won't change because the lower bound of the
                // new ranges is the same as the lower bound of the range of first)
                n.setNext(first.next());
                // link and calculate the distance for n
                Node firstPrevious = first.previous();
                if (firstPrevious == ranges) {
                    ranges = n;
                } else {
                    first.previous().previous().setNext(n);
                }

                // clear pointers from first to help gc out
                // there new gen to old gen promotion can cause problems
                first.clearForGc();

                // we have reduced number of nodes in list so reduce count
                count--;
            }
        }
    }

    @Override
    public Iterator<Range> iterator() {
        return new Iterator<Range>() {

            Node r = ranges;

            @Override
            public boolean hasNext() {
                return r != null;
            }

            @Override
            public Range next() {
                Range v = r.value;
                r = r.next();
                return v;
            }

        };
    }

    public void println() {
        forEach(System.out::println);
    }

}
