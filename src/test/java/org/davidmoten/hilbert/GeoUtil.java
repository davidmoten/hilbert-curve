package org.davidmoten.hilbert;

import com.github.davidmoten.guavamini.Preconditions;

public class GeoUtil {

    public static long[] scalePoint(float lat, float lon, long time, long minTime, long maxTime,
            long max) {
        long x = scale((lat + 90.0f) / 180, max);
        long y = scale((lon + 180.0f) / 360, max);
        long z = scale(((float) time - minTime) / (maxTime - minTime), max);
        return new long[] { x, y, z };
    }
    
    private static long scale(float d, long max) {
        Preconditions.checkArgument(d >= 0 && d <= 1);
        if (d == 1) {
            return max;
        } else {
            return Math.round(Math.floor(d * (max + 1)));
        }
    }

}
