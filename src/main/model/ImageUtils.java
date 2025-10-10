package model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/*
 * Utility helpers for loading, saving, and converting images used in the convolution demos
 */
public class ImageUtils {

    /*
     * REQUIRES: path points to a readable image file
     * MODIFIES: nothing
     * EFFECTS: loads the image from disk and returns it
     */
    public static BufferedImage readImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    /*
     * REQUIRES: img and path are not null
     * MODIFIES: filesystem
     * EFFECTS: writes the image to disk using the extension inferred from the path
     */
    public static void writeImage(BufferedImage img, String path) throws IOException {
        String format = getFormatFromPath(path);
        ImageIO.write(img, format, new File(path));
    }

    /*
     * REQUIRES: path is not null
     * MODIFIES: nothing
     * EFFECTS: returns the file extension or defaults to png when none exists
     */
    private static String getFormatFromPath(String path) {
        int dot = path.lastIndexOf('.')
                ;
        if (dot > 0 && dot < path.length() - 1) {
            return path.substring(dot + 1);
        }
        return "png";
    }

    /*
     * REQUIRES: img is not null
     * MODIFIES: nothing
     * EFFECTS: converts the image to a greyscale matrix with values in [0, 1]
     */
    public static double[][] toGrayMatrix(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        double[][] out = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // standard luma transform
                double gray = 0.299 * r + 0.587 * g + 0.114 * b;
                out[y][x] = gray / 255.0;
            }
        }
        return out;
    }

    /*
     * REQUIRES: mat is rectangular and values fall in a reasonable numeric range
     * MODIFIES: nothing
     * EFFECTS: creates a greyscale BufferedImage from the matrix
     */
    public static BufferedImage fromMatrix(double[][] mat) {
        int height = mat.length;
        int width = mat[0].length;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int v = (int) Math.round(clamp01(mat[y][x]) * 255.0);
                int rgb = (v << 16) | (v << 8) | v;
                img.setRGB(x, y, (0xFF << 24) | rgb);
            }
        }
        return img;
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS: clamps the value into the [0,1] interval
     */
    private static double clamp01(double v) {
        if (v < 0) {
            return 0;
        }
        if (v > 1) {
            return 1;
        }
        return v;
    }
}
