package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class Vector2Test {

    @Test
    void addsVectorsComponentWise() {
        Vector2 a = new Vector2(1f, 2f);
        Vector2 b = new Vector2(3f, 4f);
        Vector2 sum = Vector2.add(a, b);
        assertEquals(4f, sum.getX());
        assertEquals(6f, sum.getY());
    }

    @Test
    void normalizesToUnitLength() {
        Vector2 v = new Vector2(3f, 4f);
        Vector2 unit = Vector2.normalize(v);
        assertTrue(Math.abs(unit.magnitude() - 1f) < 0.001f);
    }

    @Test
    void equalityUsesEpsilon() {
        Vector2 v = new Vector2(1f, 1f);
        assertTrue(v.equals(new Vector2(v)));
        assertTrue(!v.equals("nope"));
    }
}
