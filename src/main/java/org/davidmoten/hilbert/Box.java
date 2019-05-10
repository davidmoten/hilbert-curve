package org.davidmoten.hilbert;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

final class Box {

    final long[] a;
    final long[] b;

    Box(long[] a, long[] b) {
        Preconditions.checkArgument(a.length == b.length);
        this.a = a;
        this.b = b;
    }

    int dimensions() {
        return a.length;
    }

    @Override
    public String toString() {
        return "Box [" + Arrays.toString(a) + ", " + Arrays.toString(b) + "]";
    }

    void visitCells(Consumer<? super long[]> visitor) {
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

    void visitPerimeter(Consumer<? super long[]> visitor) {
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
            // try to increment once
            for (int i = y.length - 1; i >= 0; i--) {
                if (i > specialIndex) {
                    // to the right of specialIndex we only allow values between min + 1 and max -1
                    // inclusive
                    if (y[i] == maxes[i] - 1) {
                        y[i] = mins[i] + 1;
                        // continue looping to increment at the next index to the left
                    } else {
                        // increment happened without carryover so we break and report y
                        y[i] += 1;
                        break;
                    }
                } else if (i < specialIndex) {
                    // to the left of specialIndex we allow all values
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

    boolean contains(long[] point) {
        Preconditions.checkArgument(a.length == point.length);
        for (int i = 0; i < a.length; i++) {
            if (point[i] < Math.min(a[i], b[i]) || point[i] > Math.max(a[i], b[i])) {
                return false;
            }
        }
        return true;
    }
}
