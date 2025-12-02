package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import org.junit.jupiter.api.Test;

@ExcludeFromJacocoGeneratedReport
public class Vector3Test {

    @Test
    void testCross() {
        Vector3 i = new Vector3(1f, 0f, 0f);
        Vector3 j = new Vector3(0f, 1f, 0f);
        Vector3 k = Vector3.cross(i, j);
        assertEquals(0f, k.getX());
        assertEquals(0f, k.getY());
        assertEquals(1f, k.getZ());
    }

    @Test
    void testMultiply() {
        Vector3 v = new Vector3(1f, -2f, 3f);
        Vector3 scaled = Vector3.multiply(v, 2f);
        assertEquals(2f, scaled.getX());
        assertEquals(-4f, scaled.getY());
        assertEquals(6f, scaled.getZ());
    }

    @Test
    void testEquals() {
        Vector3 v = new Vector3(1f, 2f, 3f);
        assertTrue(v.equals(new Vector3(v)));
        assertTrue(!v.equals(5));
    }
}
