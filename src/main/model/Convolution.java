package model;

public class Convolution {

    public static double pointwiseConv(double[][] input, int x, int y, double[][] k, int kwidth, int kheight) {
        double output = 0;
        // convolute kernel (3x3) into 1x1 matrix
        for (int i = 1; i < kwidth; i++) {
            for (int j = 1; j < kheight; j++) {
                output += (input[x + i][y + j] * k[i][j]);
            }
        }
        return output;
    }

    public static double[][] conv2D(double[][] input, int width, int height, double[][] k, int kwidth, int kheight) {
        int smallWidth = width - kwidth + 1;
        int smallHeight = height - kheight + 1;
        double[][] output = new double[smallWidth][smallHeight]; // matrix size changes after conv

        // initialize
        for (int i = 1; i < smallWidth; i++) {
            for (int j = 1; j < smallHeight; j++) {
                output[i][j] = 0;
            }
        }

        // apply pointwise convnolution to small 2D matrix and add the convoluted 1x1 pixels to new output
        for (int i = 1; i < smallHeight; i++) {
            for (int j = 1; j < smallHeight; j++) {
                output[i][j] = pointwiseConv(input, i, j, k, kwidth, kheight);
            }
        }

        return output;
    }


}
    