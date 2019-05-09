package org.davidmoten.hilbert;

import com.github.davidmoten.guavamini.Preconditions;

// NotThreadSafe
final class Node implements Comparable<Node> {

    final Range value;
    private Node next;
    private Node previous;
    private long distanceToPrevious;

    Node(Range value) {
        this.value = value;
    }

    Node next() {
        return next;
    }

    Node previous() {
        return previous;
    }

    Node setNext(Node next) {
        Preconditions.checkNotNull(next);
        Preconditions.checkArgument(next != this);
        this.next = next;
        next.distanceToPrevious = value.low() - next.value.high();
        next.previous = this;
        return this;
    }

    @Override
    public int compareTo(Node o) {
        if (this == o) {
            return 0;
        } else {
            if (next == null) {
                return -1;
            }
            long x = distanceToPrevious;
            long y = o.distanceToPrevious;
            if (x < y) {
                return -1;
            } else if (x == y) {
                return Long.compare(value.low(), o.value.low());
            } else {
                return 1;
            }
        }
    }

    @Override
    public String toString() {
        return "Node [value=" + value + ", next=" + next + ", previous=" + previous + "]";
    }

    void setDistanceToPrevious(long distance) {
        this.distanceToPrevious = distance;
    }

    void clearForGc() {
        next = null;
        previous = null;
    }
    
}