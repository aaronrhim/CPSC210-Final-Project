package ui;

import model.Convolution;
import model.ImageUtils;
import model.KernelLibrary;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console walkthrough that lets a user load an image, pick a kernel, and save the convolved result
 */
public class ConvolutionApp {
    private final Scanner scanner;

    private BufferedImage source;
    private BufferedImage result;
    private String chosenKernel;
    private int iterations;

    /*
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS: prepares the console app with default iteration count
     */
    public ConvolutionApp() {
        scanner = new Scanner(System.in);
        iterations = 1;
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: this, System.in/out
     * EFFECTS: runs an interactive loop until the user decides to quit
     */
    public void run() {
        boolean keepGoing = true;
        while (keepGoing) {
            printMenu();
            String cmd = scanner.nextLine().trim();
            keepGoing = handleCommand(cmd);
        }
        System.out.println("Goodbye!");
    }

    /*
     * REQUIRES: cmd is not null
     * MODIFIES: this, System.out
     * EFFECTS: performs the selected action and returns whether the loop should continue
     */
    private boolean handleCommand(String cmd) {
        switch (cmd) {
            case "1":
                loadImage();
                return true;
            case "2":
                selectKernel();
                return true;
            case "3":
                setIterations();
                return true;
            case "4":
                applyConvolution();
                return true;
            case "5":
                saveImage();
                return true;
            case "6":
                return false;
            default:
                System.out.println("Unrecognised option. Please try again.");
                return true;
        }
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: System.out
     * EFFECTS: shows the available actions to the user
     */
    private void printMenu() {
        System.out.println("\n=== Convolution Console ===");
        System.out.println("1 -> load image");
        System.out.println("2 -> choose kernel" + (chosenKernel == null ? "" : " (current: " + chosenKernel + ")"));
        System.out.println("3 -> set iterations (current: " + iterations + ")");
        System.out.println("4 -> apply convolution");
        System.out.println("5 -> save result");
        System.out.println("6 -> quit");
        System.out.print("Select option: ");
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS: loads an image from disk into memory if the path is valid
     */
    private void loadImage() {
        System.out.print("Enter image path: ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            System.out.println("No path provided.");
            return;
        }
        try {
            source = ImageUtils.readImage(path);
            result = null;
            System.out.println("Loaded " + source.getWidth() + "x" + source.getHeight() + " image.");
        } catch (IOException e) {
            System.out.println("Could not read image: " + e.getMessage());
        }
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS: lets the user pick one of the registered kernels
     */
    private void selectKernel() {
        List<String> names = new ArrayList<>(KernelLibrary.getAll().keySet());
        if (names.isEmpty()) {
            System.out.println("No kernels registered.");
            return;
        }
        for (int i = 0; i < names.size(); i++) {
            System.out.println((i + 1) + ") " + names.get(i));
        }
        System.out.print("Choose kernel by number: ");
        String choice = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(choice) - 1;
            if (idx < 0 || idx >= names.size()) {
                System.out.println("Out of range.");
                return;
            }
            chosenKernel = names.get(idx);
            System.out.println("Selected: " + chosenKernel);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a number.");
        }
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS: updates the number of convolution passes to apply
     */
    private void setIterations() {
        System.out.print("Enter iterations (1-20): ");
        String value = scanner.nextLine().trim();
        try {
            int iter = Integer.parseInt(value);
            if (iter < 1 || iter > 20) {
                System.out.println("Please stay between 1 and 20.");
                return;
            }
            iterations = iter;
        } catch (NumberFormatException e) {
            System.out.println("Please enter a whole number.");
        }
    }

    /*
     * REQUIRES: source and chosenKernel are not null
     * MODIFIES: this
     * EFFECTS: applies the convolution and stores the resulting image in memory
     */
    private void applyConvolution() {
        if (source == null) {
            System.out.println("Load an image first.");
            return;
        }
        if (chosenKernel == null) {
            System.out.println("Pick a kernel first.");
            return;
        }

        double[][] kernel = KernelLibrary.getKernel(chosenKernel);
        double[][] matrix = ImageUtils.toGrayMatrix(source);
        double[][] out = Convolution.padd2D(matrix, source.getWidth(), source.getHeight(),
                kernel, kernel[0].length, kernel.length, iterations);
        result = ImageUtils.fromMatrix(out);
        System.out.println("Convolution applied.");
    }

    /*
     * REQUIRES: result is not null
     * MODIFIES: filesystem (writes a file)
     * EFFECTS: saves the convolved image to disk if possible
     */
    private void saveImage() {
        if (result == null) {
            System.out.println("Nothing to save yet.");
            return;
        }
        System.out.print("Enter output path: ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            System.out.println("No path provided.");
            return;
        }
        try {
            File file = resolveOutputFile(path);
            ImageUtils.writeImage(result, file.getAbsolutePath());
            System.out.println("Saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to save image: " + e.getMessage());
        }
    }

    /**
     * Requires: path is not null.<br>
     * Modifies: filesystem (ensures the default directory exists).<br>
     * Effects: returns a file pointing to the requested output, defaulting to src/images for
     * relative paths.
     */
    private File resolveOutputFile(String path) {
        if (path.isEmpty()) {
            path = defaultFileName();
        }

        File baseDir = new File("src/images");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        File candidate = new File(path);
        boolean hasSeparator = path.contains("/") || path.contains(File.separator);

        if (!candidate.isAbsolute() && !hasSeparator) {
            candidate = new File(baseDir, candidate.getName());
        } else if (!candidate.isAbsolute()) {
            candidate = candidate.getAbsoluteFile();
        }

        if (!hasExtension(candidate.getName())) {
            if (hasSeparator) {
                File dir = candidate;
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                return new File(dir, defaultFileName());
            } else {
                File parent = candidate.getParentFile();
                if (parent == null) {
                    parent = baseDir;
                }
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                return new File(parent, candidate.getName() + ".png");
            }
        }

        File parent = candidate.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return candidate;
    }

    private boolean hasExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 && dot < name.length() - 1;
    }

    private String defaultFileName() {
        return "convolution-output.png";
    }
}
