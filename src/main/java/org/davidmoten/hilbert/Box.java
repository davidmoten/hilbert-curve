package org.davidmoten.hilbert;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

final class Box {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(a);
        result = prime * result + Arrays.hashCode(b);
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
        Box other = (Box) obj;
        if (!Arrays.equals(a, other.a))
            return false;
        if (!Arrays.equals(b, other.b))
            return false;
        return true;
    }

    final long[] a;
    final long[] b;

    Box(long[] a, long[] b) {
        Preconditions.checkArgument(a.length == b.length);
        this.a = a;
        this.b = b;
    }

    public int dimensions() {
        return a.length;
    }

    @Override
    public String toString() {
        return "Box [a=" + Arrays.toString(a) + ", b=" + Arrays.toString(b) + "]";
    }

    static Builder a(long... value) {
        return new Builder(value);
    }

    static final class Builder {
        long[] a;
        long[] b;

        Builder(long... a) {
            this.a = a;
        }

        Box b(long... values) {
            this.b = values;
            return new Box(a, b);
        }
    }

    public Box dropDimension(int dimension) {
        long[] x = dropDimension(a, dimension);
        long[] y = dropDimension(b, dimension);
        return new Box(x, y);
    }

    private static long[] dropDimension(long[] x, int dimension) {
        long[] y = new long[x.length - 1];
        for (int i = 0; i < x.length; i++) {
            if (i < dimension) {
                y[i] = x[i];
            } else if (i > dimension) {
                y[i - 1] = x[i];
            }
        }
        return y;
    }

    public void visitCells(Consumer<? super long[]> visitor) {
        long[] mins = mins(a, b);
        long[] maxes = maxes(a, b);
        long[] x = Arrays.copyOf(mins, mins.length);
        while (true) {
            visitor.accept(x);
            if (equals(x, maxes)) {
                break;
            } else {
                addOne(x, mins, maxes);
            }
        }
    }

    static final class Cell {
        final long[] point;

        Cell(long[] point) {
            this.point = point;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(point);
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
            Cell other = (Cell) obj;
            if (!Arrays.equals(point, other.point))
                return false;
            return true;
        }

    }

    public void visitPerimeter(Consumer<? super long[]> visitor) {
        long[] mins = mins(a, b);
        long[] maxes = maxes(a, b);
        for (int specialIndex = dimensions() - 1; specialIndex >= 0; specialIndex--) {
            long[] x = Arrays.copyOf(mins, mins.length);
            // visit for the minimum at specialIndex
            visitPerimeter(mins, maxes, x, specialIndex, visitor);
            if (mins[specialIndex] != maxes[specialIndex]) {
                // visit for the maximum at specialIndex
                long[] y = Arrays.copyOf(mins, mins.length);
                y[specialIndex] = maxes[specialIndex];
                visitPerimeter(mins, maxes, y, specialIndex, visitor);
            } else {
                break;
            }
        }
    }
    
    @VisibleForTesting
    static void visitPerimeter(long[] mins, long[] maxes, long[] x, int specialIndex,
            Consumer<? super long[]> visitor) {
        long[] y = Arrays.copyOf(x, x.length);
        for (int i = specialIndex + 1; i < y.length; i++) {
            if (mins[i] >= maxes[i] - 1) {
                return;
            }
            y[i] = mins[i] + 1;
        }
        visitor.accept(y);
        while (true) {
            System.out.println("y=" + Arrays.toString(y));
            // try to increment once
            for (int i = y.length - 1; i >= 0; i--) {
                // start at right of number to increment (y[y.length - 1])
                // for i > specialIndex increment with values z: min[i] < z < max[i]
                // once z is max - 1 then rotate through to min + 1 and continue
                // loop (visit next index to the left (i - 1))
                // if i == specialIndex leave unchanged and continue loop
                // if i < specialIndex then
                // -- if y[i] == max[i] set y[i] = min[i] and continue loop
                // -- else set y[i]=y[i]+1 and break
                //
                if (i > specialIndex) {
                    if (y[i] == maxes[i] - 1) {
                        y[i] = mins[i] + 1;
                        // continue looping to increment at the next index to the left
                    } else {
                        y[i] += 1;
                        break;
                    }
                } else if (i < specialIndex) {
                    if (y[i] == maxes[i]) {
                        if (i == 0) {
                            return;
                        } else {
                            y[i] = mins[i];
                        }
                    } else {
                        y[i] += 1;
                        break;
                    }
                } else if (i == specialIndex && i == 0) {
                    return;
                }
            }
            visitor.accept(y);
        }
    }

    /**
     * Returns true if and only if the value x is changed. x is incremented (with
     * carry over propagating left along the array). Once carryover hits the special
     * index position and the special index position already has max value (it can
     * only have min or max value, nothing in between) then false is returned (and x
     * is unchanged).
     * 
     * @param x
     * @param mins
     * @param maxes
     * @param specialIndex
     * @return true iff x is changed
     */
    @VisibleForTesting
    static boolean addOne(long[] x, long[] mins, long[] maxes, int specialIndex) {
        for (int i = x.length - 1; i >= specialIndex; i--) {
            if (x[i] != maxes[i]) {
                if (i == specialIndex) {
                    x[i] = maxes[i];
                } else {
                    x[i]++;
                }
                return true;
            } else {
                x[i] = mins[i];
            }
        }
        return false;
    }

    @VisibleForTesting
    static void addOne(long[] x, long[] mins, long[] maxes) {
        for (int i = x.length - 1; i >= 0; i--) {
            if (x[i] != maxes[i]) {
                x[i]++;
                break;
            } else {
                x[i] = mins[i];
            }
        }
    }

    @VisibleForTesting
    static boolean equals(long[] a, long[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private static long[] mins(long[] a, long[] b) {
        long[] c = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = Math.min(a[i], b[i]);
        }
        return c;
    }

    private static long[] maxes(long[] a, long[] b) {
        long[] c = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = Math.max(a[i], b[i]);
        }
        return c;
    }
}
