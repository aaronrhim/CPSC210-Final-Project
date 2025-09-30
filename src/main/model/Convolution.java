package model;

public class Convolution {
    public static void main(String[] args) {
        double[][] input = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0},
            {7.0, 8.0, 9.0}
        };
        int width = input[0].length;   // columns (x)
        int height = input.length;     // rows (y)
        double[][] k = {
            {1 / 3.0, 1 / 3.0},
            {1 / 3.0, 1 / 3.0}
        };

        int kwidth = 2;
        int kheight = 2;

        // valid convolution (no padding)
        double[][] validConv = conv2D(input, width, height, k, kwidth, kheight);
        // same-sized canvas with padding (one iteration)
        double[][] sameConv = padd2D(input, width, height, k, kwidth, kheight, 1);

        // Print valid conv output (size: (height-kh+1) x (width-kw+1))
        for (int i = 0; i < validConv.length; i++) {
            for (int j = 0; j < validConv[0].length; j++) {
                System.out.print(validConv[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static double pointwiseConv(double[][] input, int x, int y, double[][] k, int kwidth, int kheight) {
        double output = 0;
        // Convolve kernel into single output pixel at (x,y)
        // input[row][col] => input[y + dy][x + dx]
        for (int dx = 0; dx < kwidth; dx++) {
            for (int dy = 0; dy < kheight; dy++) {
                output += input[y + dy][x + dx] * k[dy][dx];
            }
        }
        return output;
    }

    public static double[][] conv2D(double[][] input, int width, int height, double[][] k, int kwidth, int kheight) {
        int outWidth = width - kwidth + 1;
        int outHeight = height - kheight + 1;
        double[][] output = new double[outHeight][outWidth]; // [rows][cols]

        // initialize (optional since Java zeros arrays)
        for (int i = 0; i < outHeight; i++) {
            for (int j = 0; j < outWidth; j++) {
                output[i][j] = 0;
            }
        }

        // apply pointwise convolution across the valid output region
        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {
                output[y][x] = pointwiseConv(input, x, y, k, kwidth, kheight);
            }
        }

        return output;
    }

    public static double[][] convPadding(double[][] input, int width, int height,
                                         double[][] k, int kwidth, int kheight) {
        int outWidth = width - kwidth + 1;
        int outHeight = height - kheight + 1;
        int top = kheight / 2;
        int left = kwidth / 2;

        // Compute valid conv on original input
        double[][] small = conv2D(input, width, height, k, kwidth, kheight);
        double[][] large = new double[height][width]; // [rows][cols]
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                large[i][j] = 0;
            }
        }

        for (int i = 0; i < outHeight; i++) {
            for (int j = 0; j < outWidth; j++) {
                large[i + top][j + left] = small[i][j];
            }
        }

        return large;
    }

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

