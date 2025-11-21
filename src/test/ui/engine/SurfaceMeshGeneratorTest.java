package ui.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import model.ScalarField;
import model.Vector3;
import org.junit.jupiter.api.Test;

public class SurfaceMeshGeneratorTest {

    @Test
    void testGenerateGrid() {
        ScalarField field = new ScalarField("x+y", (x, y) -> x + y);
        Vector3[][] grid = SurfaceMeshGenerator.generateGrid(field, -1f, 1f, -1f, 1f, 1);
        assertEquals(2, grid.length);
        assertEquals(2, grid[0].length);
        assertNotNull(grid[0][0]);
        field.setDomain(-1f, 1f, -1f, 1f, -0.1f, 0.1f);
        Vector3[][] clipped = SurfaceMeshGenerator.generateGrid(field, -1f, 1f, -1f, 1f, 1);
        assertNull(clipped[1][1]);
    }
}
