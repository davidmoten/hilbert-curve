package org.davidmoten.hilbert;

import static org.junit.Assert.assertEquals;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

public class WikipediaHilbertTest {

    @Test
    public void createImageUsingWikpediaHilbertCurveCode() throws IOException {
        int bits = 5;
        int n = 1 << bits;
        for (int i = 0; i < n * n; i++) {
            long[] point = { 0, 0 };
            d2xy(n, i, point);
        }
        int width = 800;
        int height = width;
        BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = b.createGraphics();
        g.setPaint(Color.black);
        g.setStroke(new BasicStroke(0.5f));
        int margin = 10;
        int x = margin;
        int y = margin;
        for (long i = 0; i < n * n; i++) {
            long[] point = new long[2];
            d2xy(n, i, point);
            int x2 = (int) Math.round((double) point[0] / (n - 1) * (width - 2 * margin) + margin);
            int y2 = (int) Math.round((double) point[1] / (n - 1) * (height - 2 * margin) + margin);
            g.drawLine(x, y, x2, y2);
            x = x2;
            y = y2;
        }
        ImageIO.write(b, "PNG", new File("target/imageWiki.png"));
    }

    @Test
    public void roundTrip() {
        long[] point = new long[2];
        d2xy(32, 3, point);
        assertEquals(3, xy2d(32, point));
    }

    private static long xy2d(long n, long[] point) {
        long rx, ry, s, d = 0;
        for (s = n / 2; s > 0; s /= 2) {
            rx = (point[0] & s) > 0 ? 1 : 0;
            ry = (point[1] & s) > 0 ? 1 : 0;
            d += s * s * ((3 * rx) ^ ry);
            rot(s, point, rx, ry);
        }
        return d;
    }

    // convert d to (x,y)
    // n is pow(2, bits)
    private static long[] d2xy(int n, long d, long[] point) {
        long rx, ry;
        long s, t = d;
        point[0] = 0;
        point[1] = 0;
        for (s = 1; s < n; s *= 2) {
            rx = 1 & (t / 2);
            ry = 1 & (t ^ rx);
            rot(s, point, rx, ry);
            point[0] += s * rx;
            point[1] += s * ry;
            t /= 4;
        }
        return point;
    }

    // rotate/flip a quadrant appropriately
    private static void rot(long n, long[] point, long rx, long ry) {
        if (ry == 0) {
            if (rx == 1) {
                point[0] = n - 1 - point[0];
                point[1] = n - 1 - point[1];
            }

            // Swap x and y
            long t = point[0];
            point[0] = point[1];
            point[1] = t;
        }
    }

}
