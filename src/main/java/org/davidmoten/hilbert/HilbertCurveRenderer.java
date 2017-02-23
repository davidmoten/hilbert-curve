package org.davidmoten.hilbert;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import javax.imageio.ImageIO;

import org.davidmoten.hilbert.exceptions.IORuntimeException;

public final class HilbertCurveRenderer {

    public static void renderToFile(int bits, int width, String filename) {
        BufferedImage b = render(bits, width);
        try {
            ImageIO.write(b, "PNG", new File(filename));
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    public static BufferedImage render(int bits, int width) {
        int dimensions = 2;
        HilbertCurve c = HilbertCurve.bits(bits).dimensions(dimensions);
        int n = 1 << bits;
        int height = width;
        BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = b.createGraphics();
        g.setBackground(Color.white);
        g.fillRect(0, 0, width, height);
        g.setPaint(Color.black);
        g.setStroke(new BasicStroke(0.5f));
        int margin = 10;
        int x = margin;
        int y = margin;
        for (long i = 0; i < n * n; i++) {
            long[] point = c.point(BigInteger.valueOf(i));
            int x2 = (int) Math.round((double) point[0] / (n - 1) * (width - 2 * margin) + margin);
            int y2 = (int) Math.round((double) point[1] / (n - 1) * (height - 2 * margin) + margin);
            g.drawLine(x, y, x2, y2);
            x = x2;
            y = y2;
        }
        return b;
    }
}
