package model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Collects a few hand-picked convolution kernels so that UI layers can surface them by name
 * The kernels are stored in insertion order to keep menu prompts predictable
 */
public final class KernelLibrary {
    private static final Map<String, double[][]> KERNELS = build();

    private KernelLibrary() {
        // empty constructor
    }

    /*
     * REQUIRES: name is not null and matches one of the registered kernels
     * MODIFIES: nothing
     * EFFECTS: returns the kernel coefficients for the given name, or null when the
     * name is unknown
     */
    public static double[][] getKernel(String name) {
        return KERNELS.get(name);
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS: returns an unmodifiable view of the registered kernels map
     */
    public static Map<String, double[][]> getAll() {
        return KERNELS;
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS: creates the ordered map of built-in kernels
     */
    private static Map<String, double[][]> build() {
        Map<String, double[][]> kernels = new LinkedHashMap<>();
        kernels.put("Mean blur 3x3", new double[][]{
            {1 / 9.0, 1 / 9.0, 1 / 9.0},
            {1 / 9.0, 1 / 9.0, 1 / 9.0},
            {1 / 9.0, 1 / 9.0, 1 / 9.0}
        });
        kernels.put("Sharpen 3x3", new double[][]{
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
        });
        kernels.put("Edge detect 3x3", new double[][]{
            {-1, -1, -1},
            {-1, 8, -1},
            {-1, -1, -1}
        });
        return Collections.unmodifiableMap(kernels);
    }
}
