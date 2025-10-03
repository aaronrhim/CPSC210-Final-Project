package model;

import java.awt.image.BufferedImage;
import java.io.IOException;

/*
 * Forward Pass applies convolution to test image, set # of iterations to 10
 */
public class ForwardPass {
    public static void main(String[] args) throws IOException {
        String inPath = args[0];
        String outPath = args.length >= 2 ? args[1] : "src/images/test.png";

        BufferedImage img = ImageUtils.readImage(inPath);
        double[][] mat = ImageUtils.toGrayMatrix(img);
        int width = img.getWidth();
        int height = img.getHeight();

        // Simple 3x3 mean blur kernel (values sum to 1)
        double[][] k = new double[][]{
            {1 / 9.0, 1 / 9.0, 1 / 9.0},
            {1 / 9.0, 1 / 9.0, 1 / 9.0},
            {1 / 9.0, 1 / 9.0, 1 / 9.0}
        };

        int kw = 3;
        int kh = 3;
        int iterations = 10; // 10 convolution iterations

        double[][] outMat = Convolution.padd2D(mat, width, height, k, kw, kh, iterations);

        // Convert back to image and save
        BufferedImage outImg = ImageUtils.fromMatrix(outMat);
        ImageUtils.writeImage(outImg, outPath);

        // Optionally print a small sample of the resulting matrix
        printMatrixSample(outMat, 5, 5);
    }

    private static void printMatrixSample(double[][] m, int rows, int cols) {
        int h = Math.min(rows, m.length);
        int w = Math.min(cols, m[0].length);
        System.out.println("Matrix sample:");
        for (int y = 0; y < h; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < w; x++) {
                sb.append(String.format("%.3f ", m[y][x]));
            }
            System.out.println(sb.toString());
        }
    }
}
