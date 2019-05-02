package org.davidmoten.hilbert;

import java.util.Comparator;
import java.util.TreeSet;

import com.github.davidmoten.guavamini.Preconditions;

public class Ranges2 {

    private final int bufferSize;
    private final TreeSet<Node> set;
    private Node ranges;
    private int count;

    public Ranges2(int bufferSize) {
        this.bufferSize = bufferSize;
        this.ranges = null;
        this.set = new TreeSet<>();
    }

    public void add(Range r) {
        Preconditions.checkArgument(ranges == null || ranges.value.high() < r.low());
        ranges = insert(ranges, r);
        count++;
        if (ranges.next != null) {
            // if there are at least two ranges
            set.add(ranges);
            if (count > bufferSize) {
                // join the range with the smallest distance to next
                Node x = set.first();
                Node next = x.next;
                Node y = new Node(x.value.join(next.value));
                y.next = next.next;
                if (x.previous == null) {
                    ranges = y;
                } else {
                    x.previous.next = y;
                    y.previous = x.previous;
                }
                // x has been replaced now so null its references for the joy of gc (I remember
                // some old/new generation gc problem with linked lists that was fixed by doing
                // this)
                x.next = null;
                x.previous = null;

                // remove x (its old distance was used for sorting)
                set.remove(x);
                // add y as replacement for x
                set.add(y);
                count--;
            }
        }
    }
    
    private static Node insert(Node ranges, Range r) {
        if (ranges == null) {
            return new Node(r);
        } else {
            return ranges.insert(r);
        }
    }

    private static final class Node implements Comparator<Node> {
        final Range value;
        Node next;
        Node previous;

        Node(Range value) {
            this.value = value;
        }

        Node insert(Range value) {
            Node n = new Node(value);
            n.next = this;
            previous = n;
            return n;
        }

        @Override
        public int compare(Node a, Node b) {
            long x = a.next.value.low() - a.value.high();
            long y = b.next.value.low() - b.value.high();
            if (x < y) {
                return -1;
            } else if (x == y) {
                return 0;
            } else {
                return 1;
            }
        }
    }

}
