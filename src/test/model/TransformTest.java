package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class TransformTest {

    @Test
    void buildsTranslationMatrix() {
        Transform translation = Transform.translation(new Vector3(1f, 2f, 3f));
        float[][] comp = translation.getComponents();
        assertEquals(1f, comp[0][3]);
        assertEquals(2f, comp[1][3]);
        assertEquals(3f, comp[2][3]);
        assertEquals(1f, comp[3][3]);
    }

    @Test
    void multipliesTransformAndVector() {
        Transform scale = Transform.scale(new Vector3(2f, 3f, 4f));
        Vector3 v = new Vector3(1f, 1f, 1f);
        Vector3 result = Transform.multiply(scale, v);
        assertEquals(2f, result.getX());
        assertEquals(3f, result.getY());
        assertEquals(4f, result.getZ());
    }

    @Test
    void multipliesTwoTransforms() {
        Transform scale = Transform.scale(new Vector3(2f, 2f, 2f));
        Transform translate = Transform.translation(new Vector3(1f, 0f, 0f));
        Transform combined = Transform.multiply(scale, translate);
        float[][] comp = combined.getComponents();
        assertEquals(2f, comp[0][0]);
        assertEquals(2f, comp[1][1]);
        assertEquals(2f, comp[2][2]);
        assertEquals(2f, comp[0][3]); // translation scaled along x
        assertNotNull(combined);
    }
}
