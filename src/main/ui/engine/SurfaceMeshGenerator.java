package ui.engine;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import model.Vector3;
import model.ScalarField;

/**
 * Utility for sampling scalar fields into surface meshes for rendering.
 */
@ExcludeFromJacocoGeneratedReport
public class SurfaceMeshGenerator {

    // EFFECTS: samples the scalar field over the given domain and returns a grid of vertices
    public static Vector3[][] generateGrid(
            ScalarField field,
            float minX, float maxX,
            float minY, float maxY,
            int resolution
    ) {
        Vector3[][] grid = new Vector3[resolution + 1][resolution + 1];

        float dx = (maxX - minX) / resolution;
        float dy = (maxY - minY) / resolution;

        for (int i = 0; i <= resolution; i++) {
            for (int j = 0; j <= resolution; j++) {
                float x = minX + i * dx;
                float y = minY + j * dy;

                float height = field.evaluate(x, y);
                if (height < field.getZMin() || height > field.getZMax()) {
                    grid[i][j] = null;
                    continue;
                }

                float normalizedDepth = (y - minY) / (maxY - minY);
                float depth = -5.0f - normalizedDepth * 25.0f;

                grid[i][j] = new Vector3(x, height, depth);
            }
        }

        return grid;
    }
}
