package org.davidmoten.hilbert;

import java.util.TreeSet;

import com.github.davidmoten.guavamini.Preconditions;

public class Ranges2 {

    private final int bufferSize;

    // set is ordered by increasing distance to next node (Node is a linked list)
    private final TreeSet<Node> set;
    private Node ranges;
    private int count;

    public Ranges2(int bufferSize) {
        Preconditions.checkArgument(bufferSize > 1);
        this.bufferSize = bufferSize;
        this.ranges = null;
        this.set = new TreeSet<>();
    }

    public void add(Range r) {
        Preconditions.checkArgument(ranges == null || ranges.value.high() < r.low());
        System.out.println("adding " + r);
        ranges = insert(ranges, r);
        count++;
        if (ranges.next != null) {
            // if there are at least two ranges
            set.add(ranges);
            if (count > bufferSize) {
                // join the range with the smallest distance to next
                Node x = set.first();
                System.out.println("first = " + x.value);
                Node next = x.next();
                Node y = new Node(x.value.join(next.value));
                y.setNext(next.next());
                if (x.previous() == null) {
                    ranges = y;
                } else {
                    x.previous.setNext(y);
                    y.setPrevious(x.previous);
                }
                // x has been replaced now so null its references for the joy of gc (I remember
                // some old/new generation gc problem with linked lists that was fixed by doing
                // this)
                x.setNext(null);
                x.setPrevious (null);

                // remove x (its old distance was used for sorting)
                System.out.println("removing " + x);
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

    // NotThreadSafe
    private static final class Node implements Comparable<Node> {
        
        private static long counter = 0;
        
        final Range value;
        private Node next;
        private Node previous;
        private final long id;

        Node(Range value) {
            this.value = value;
            this.id = counter++;
        }
        
        Node next() {
            return next;
        }
        
        Node previous() {
            return previous;
        }
        
        Node setNext(Node next) {
            Preconditions.checkArgument(next != this);
            this.next = next;
            return this;
        }
        
        Node setPrevious(Node previous) {
            Preconditions.checkArgument(previous != this);
            this.previous = previous;
            return this;
        }

        Node insert(Range value) {
            Node n = new Node(value);
            n.next = this;
            previous = n;
            return n;
        }

        @Override
        public int compareTo(Node o) {
            if (this == o) {
                return 0;
            } else {
                if (next == null) {
                    return -1;
                }
                long x = next.value.low() - value.high();
                long y = o.next.value.low() - o.value.high();
                if (x < y) {
                    return -1;
                } else if (x == y) {
                    return Long.compare(id, o.id);
                } else {
                    return 1;
                }
            }
        }

        @Override
        public String toString() {
            return "Node [value=" + value + ", next=" + next + ", previous=" + previous + "]";
        }
        
    }

}
