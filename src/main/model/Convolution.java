package model;

/*
 * Performs 2D convolution over double matrices with and without padding
 */
public class Convolution {

    /*
     * REQUIRES: input and k are non-null; indices are in range
     * MODIFIES: nothing
     * EFFECTS: multiplies and sums the overlapping window of the kernel and input
     */
    public static double pointwiseConv(double[][] input, int x, int y, double[][] k, int kwidth, int kheight) {
        double output = 0;
        // input[row][col] => input[y + dy][x + dx]
        for (int dx = 0; dx < kwidth; dx++) {
            for (int dy = 0; dy < kheight; dy++) {
                output += input[y + dy][x + dx] * k[dy][dx];
            }
        }
        return output;
    }

    /*
     * REQUIRES: input and kernel dimensions are compatible for a valid convolution
     * MODIFIES: nothing
     * EFFECTS: returns the valid convolution (no padding) of the input with the kernel
     */
    public static double[][] conv2D(double[][] input, int width, int height, double[][] k, int kwidth, int kheight) {
        int outWidth = width - kwidth + 1;
        int outHeight = height - kheight + 1;
        double[][] output = new double[outHeight][outWidth]; // [rows][cols]

        // initialize
        // for (int i = 0; i < outHeight; i++) {
        //     for (int j = 0; j < outWidth; j++) {
        //         output[i][j] = 0;
        //     }
        // }

        // apply pointwise convolution across the valid output region
        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {
                output[y][x] = pointwiseConv(input, x, y, k, kwidth, kheight);
            }
        }

        return output;
    }

    /*
     * REQUIRES: input and kernel are non-null and compatible in size
     * MODIFIES: nothing
     * EFFECTS: returns a "same" sized convolution by centring the valid result inside a padded array
     */
    public static double[][] convPadding(double[][] input, int width, int height,
                                         double[][] k, int kwidth, int kheight) {
        int outWidth = width - kwidth + 1; // valid conv width
        int outHeight = height - kheight + 1; // valid conv height
        int top = kheight / 2; // rows of padding on top
        int left = kwidth / 2; // cols of padding on left

        // Compute valid conv on original input
        double[][] small = conv2D(input, width, height, k, kwidth, kheight);
        double[][] large = new double[height][width]; // [rows][cols]
        // for (int i = 0; i < height; i++) {
        //     for (int j = 0; j < width; j++) {
        //         large[i][j] = 0;
        //     }
        // }

        for (int i = 0; i < outHeight; i++) {
            for (int j = 0; j < outWidth; j++) {
                large[i + top][j + left] = small[i][j];
            }
        }

        return large;
    }

    /*
     * REQUIRES: iter >= 1 and parameters satisfy convPadding requirements
     * MODIFIES: nothing
     * EFFECTS: repeatedly applies padded convolution the requested number of times
     */
    public static double[][] padd2D(double[][] input, int width, int height,
                                    double[][] k, int kwidth, int kheight, int iter) {
        double[][] newInput = input;
        double[][] output = input;

        for (int i = 0; i < iter; ++i) {
            output = convPadding(newInput, width, height, k, kwidth, kheight);
            newInput = output;
        }

        return output;
    }
}
