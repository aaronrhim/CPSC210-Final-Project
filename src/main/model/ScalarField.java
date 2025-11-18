package model;

import java.util.function.BiFunction;

import model.exceptions.ArgumentOutOfBoundsException;
import model.exceptions.InvalidFunctionException;

// Represents a scalar field f(x, y) -> z for visualization and gradient descent.
public class ScalarField {
    private static final float DEFAULT_EPS = 0.0005f;

    private final String name;
    private final BiFunction<Float, Float, Float> function;

    // Domain bounds for valid evaluation
    private float xMin = -10.0f;
    private float xMax = 10.0f;
    private float yMin = -10.0f;
    private float yMax = 10.0f;
    private float zMin = -10.0f;
    private float zMax = 10.0f;

    // REQUIRES: name is non-null and non-empty; function is non-null
    // EFFECTS: creates a scalar field with the given name and function
    public ScalarField(String name, BiFunction<Float, Float, Float> function) {
        if (name == null || name.isEmpty()) {
            throw new InvalidFunctionException("Field must have a valid name.");
        }
        if (function == null) {
            throw new InvalidFunctionException("Function cannot be null.");
        }

        this.name = name;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    // REQUIRES: x and y lie within the domain bounds
    // EFFECTS: returns f(x, y)
    public float evaluate(float x, float y) {
        if (x < xMin || x > xMax || y < yMin || y > yMax) {
            throw new ArgumentOutOfBoundsException("Input (x, y) outside domain.");
        }

        Float result = function.apply(x, y);

        if (result == null || Float.isNaN(result) || Float.isInfinite(result)) {
            throw new InvalidFunctionException("Function returned invalid numeric value.");
        }

        return result;
    }

    // REQUIRES: x and y lie within domain bounds and function must be evaluatable
    // EFFECTS: returns the gradient vector ∇f(x, y) computed using central finite differences
    public Vector2 gradientAt(float x, float y) {
        float eps = DEFAULT_EPS;

        float fxh = evaluate(x + eps, y);
        float fxh2 = evaluate(x - eps, y);
        float fyh = evaluate(x, y + eps);
        float fyh2 = evaluate(x, y - eps);

        float dfdx = (fxh - fxh2) / (2.0f * eps);
        float dfdy = (fyh - fyh2) / (2.0f * eps);

        return new Vector2(dfdx, dfdy);
    }

    // REQUIRES: xMax > xMin, yMax > yMin, zMax > zMin
    // MODIFIES: this
    // EFFECTS: sets the domain bounds of the scalar field and the display range on the Z axis
    public void setDomain(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax) {
        if (xMax <= xMin || yMax <= yMin || zMax <= zMin) {
            throw new ArgumentOutOfBoundsException("Invalid domain bounds.");
        }

        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.zMin = zMin;
        this.zMax = zMax;
    }

    public float getXMin() { return xMin; }
    public float getXMax() { return xMax; }
    public float getYMin() { return yMin; }
    public float getYMax() { return yMax; }
    public float getZMin() { return zMin; }
    public float getZMax() { return zMax; }

    // EFFECTS: returns the field name
    @Override
    public String toString() {
        return name;
    }
}
