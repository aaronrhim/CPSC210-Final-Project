package model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {
    public static BufferedImage readImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public static void writeImage(BufferedImage img, String path) throws IOException {
        String format = getFormatFromPath(path);
        ImageIO.write(img, format, new File(path));
    }

    private static String getFormatFromPath(String path) {
        int dot = path.lastIndexOf('.')
                ;
        if (dot > 0 && dot < path.length() - 1) {
            return path.substring(dot + 1);
        }
        return "png";
    }

    // Convert color image to grayscale matrix [rows][cols] in range [0,1]
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

    // Convert matrix [rows][cols] in [0,1] to grayscale image
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

