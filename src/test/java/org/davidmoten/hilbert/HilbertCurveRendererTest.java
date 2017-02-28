package org.davidmoten.hilbert;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.davidmoten.hilbert.HilbertCurveRenderer.Option;
import org.davidmoten.hilbert.exceptions.IORuntimeException;
import org.junit.Ignore;
import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class HilbertCurveRendererTest {

    @Test
    public void isUtilClass() {
        Asserts.assertIsUtilityClass(HilbertCurveRenderer.class);
    }

    @Test
    public void testImageCreation() {
        for (int bits = 1; bits <= 8; bits++) {
            HilbertCurveRenderer.renderToFile(bits, 1000, "target/hilbert-2d-bits-" + bits + ".png", Option.COLORIZE, Option.LABEL);
        }
    }

    @Test
    @Ignore
    public void compareImagesWithExpected() throws IOException {
        for (int bits = 2; bits <= 8; bits++) {
            BufferedImage expected = ImageIO.read(
                    HilbertCurveRendererTest.class.getResourceAsStream("/expected/hilbert-2d-bits-" + bits + ".png"));
            BufferedImage b = HilbertCurveRenderer.render(bits, 800, Option.COLORIZE, Option.LABEL);
            assertTrue(distance(expected, b) < 100);
        }
    }

    @Test
    public void testException() {
        new IORuntimeException(new IOException());
    }

    private static double distance(BufferedImage imgA, BufferedImage imgB) {
        // The images must be the same size.
        if (imgA.getWidth() == imgB.getWidth() && imgA.getHeight() == imgB.getHeight()) {
            int width = imgA.getWidth();
            int height = imgA.getHeight();

            double mse = 0;
            // Loop over every pixel.
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double diff = imgA.getRGB(x, y) - imgB.getRGB(x, y);
                    mse += diff * diff;
                }
            }
            return Math.sqrt(mse / height / width);
        } else {
            return -1;
        }

    }

}
