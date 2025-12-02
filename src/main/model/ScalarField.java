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
    private float xxMin = -10.0f;
    private float xxMax = 10.0f;
    private float yyMin = -10.0f;
    private float yyMax = 10.0f;
    private float zzMin = -10.0f;
    private float zzMax = 10.0f;

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
        if (x < xxMin || x > xxMax || y < yyMin || y > yyMax) {
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

    // REQUIRES: xxMax > xxMin, yyMax > yyMin, zzMax > zzMin
    // MODIFIES: this
    // EFFECTS: sets the domain bounds of the scalar field and the display range on the Z axis
    public void setDomain(float xxMin, float xxMax, float yyMin, float yyMax, float zzMin, float zzMax) {
        if (xxMax <= xxMin || yyMax <= yyMin || zzMax <= zzMin) {
            throw new ArgumentOutOfBoundsException("Invalid domain bounds.");
        }

        this.xxMin = xxMin;
        this.xxMax = xxMax;
        this.yyMin = yyMin;
        this.yyMax = yyMax;
        this.zzMin = zzMin;
        this.zzMax = zzMax;
    }

    public float getXMin() { 
        return xxMin; 
    }

    public float getXMax() { 
        return xxMax; 
    }
    
    public float getYMin() { 
        return yyMin; 
    }

    public float getYMax() { 
        return yyMax; 
    }

    public float getZMin() { 
        return zzMin; 
    }

    public float getZMax() { 
        return zzMax; 
    }

    // EFFECTS: returns the field name
    @Override
    public String toString() {
        return name;
    }
}
