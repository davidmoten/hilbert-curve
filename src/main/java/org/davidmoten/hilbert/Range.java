package org.davidmoten.hilbert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public final class Range implements Comparable<Range>{

    private final long low;
    private final long high;

    public Range(long low, long high) {
        this.low = Math.min(low, high);
        this.high = Math.max(low, high);
    }

    public static Range create(long low, long high) {
        return new Range(low, high);
    }

    public static Range create(long value) {
        return new Range(value, value);
    }

    public long low() {
        return low;
    }

    public long high() {
        return high;
    }

    public boolean contains(long value) {
        return low <= value && value <= high;
    }

    @Override
    public String toString() {
        return "Range [low=" + low + ", high=" + high + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (high ^ (high >>> 32));
        result = prime * result + (int) (low ^ (low >>> 32));
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
        Range other = (Range) obj;
        if (high != other.high)
            return false;
        if (low != other.low)
            return false;
        return true;
    }

    public Range join(Range range) {
        return Range.create(Math.min(low, range.low), Math.max(high, range.high));
    }

    @Override
    public int compareTo(Range o) {
        long x = this.low()-o.low();
        if (x<0) {
            return -1;
        } else if (x>0) {
            return 1;
        } else {
            long y = this.high()-o.high();
            if (y<0) {
                return -1;
            } else if (y>0) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    
    public static void main(String[] args) {
        ArrayList<Range> list = new ArrayList<>();
        list.add(new Range(100,1000));
        list.add(new Range(10,700));
        list.add(new Range(10000,12000));
        list.add(new Range(10000,10850));
        list.add(new Range(1,5));
        list.add(new Range(250,450));
        list.add(new Range(100,500));
        list.add(new Range(99,312));
        list.add(new Range(100,150));
        Iterator<Range> i =list.iterator();
        while (i.hasNext()) {
            System.out.println(i.next().toString());
        }
        System.out.println("===================");
        Collections.sort(list);
        i = list.iterator();
        while (i.hasNext()) {
            System.out.println(i.next().toString());
        }   
    }
}
