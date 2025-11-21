package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;

import model.exceptions.ArgumentOutOfBoundsException;
import org.junit.jupiter.api.Test;

public class ScalarFieldTest {

    @Test
    void testEvaluateAndGradient() {
        BiFunction<Float, Float, Float> fn = (x, y) -> x * x + y * y;
        ScalarField field = new ScalarField("quad", fn);
        assertEquals(5f, field.evaluate(1f, 2f));
        Vector2 grad = field.gradientAt(1f, 2f);
        assertTrue(Math.abs(grad.getX() - 2f) < 0.02f);
        assertTrue(Math.abs(grad.getY() - 4f) < 0.02f);
    }

    @Test
    void testSetDomainValidation() {
        BiFunction<Float, Float, Float> fn = (x, y) -> x + y;
        ScalarField field = new ScalarField("linear", fn);
        assertThrows(ArgumentOutOfBoundsException.class, () -> field.setDomain(1f, 0f, -1f, 1f, -1f, 1f));
    }
}
