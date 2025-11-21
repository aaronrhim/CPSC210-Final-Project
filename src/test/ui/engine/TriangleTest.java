package ui.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import model.Vector3;
import org.junit.jupiter.api.Test;

public class TriangleTest {

    @Test
    void testConstructors() {
        Vector3 v0 = new Vector3(0f, 0f, 0f);
        Vector3 v1 = new Vector3(1f, 0f, 0f);
        Vector3 v2 = new Vector3(0f, 1f, 0f);
        Triangle tri = new Triangle(v0, v1, v2);
        assertEquals(3, tri.verts.length);

        Triangle copy = new Triangle(tri);
        assertNotNull(copy.verts[0]);
    }
}
