package model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KernelLibraryTest {

    @Test
    void containsExpectedKernels() {
        Map<String, double[][]> kernels = KernelLibrary.getAll();
        assertTrue(kernels.containsKey("Mean blur 3x3"));
        assertTrue(kernels.containsKey("Sharpen 3x3"));
        assertTrue(kernels.containsKey("Edge detect 3x3"));
    }

    @Test
    void matricesAreReturnedByName() {
        double[][] blur = KernelLibrary.getKernel("Mean blur 3x3");
        assertNotNull(blur);
        assertEquals(3, blur.length);
        assertEquals(3, blur[0].length);
    }
}
