package model;

import model.exceptions.NonMatchingClassException;

// Represents a 2-component vector supporting a common set of vector operations
public class Vector2 {
    private static final float EPSILON = 0.001f;

    private float compX;
    private float compY;

    // EFFECTS:
    // creates a vector with components (0, 0)
    public Vector2() {
        this(0.0f, 0.0f);
    }

    // EFFECTS:
    // creates a copy of the target vector
    public Vector2(Vector2 target) {
        this(target.compX, target.compY);
    }

    // EFFECTS:
    // creates a vector with the specified x and y components
    public Vector2(float compX, float compY) {
        this.compX = compX;
        this.compY = compY;
    }

    public float getX() {
        return compX;
    }

    public float getY() {
        return compY;
    }

    // EFFECTS:
    // returns a vector whose components are the sum of leftVector and rightVector
    public static Vector2 add(Vector2 leftVector, Vector2 rightVector) {
        float x = leftVector.getX() + rightVector.getX();
        float y = leftVector.getY() + rightVector.getY();
        return new Vector2(x, y);
    }

    // EFFECTS:
    // returns a vector whose components are leftVector minus rightVector
    public static Vector2 sub(Vector2 leftVector, Vector2 rightVector) {
        float x = leftVector.getX() - rightVector.getX();
        float y = leftVector.getY() - rightVector.getY();
        return new Vector2(x, y);
    }

    // EFFECTS:
    // returns a vector whose components are vector scaled by scalar
    public static Vector2 multiply(Vector2 vector, float scalar) {
        float x = vector.getX() * scalar;
        float y = vector.getY() * scalar;
        return new Vector2(x, y);
    }

    // EFFECTS:
    // returns the magnitude of the vector
    public float magnitude() {
        return (float) Math.sqrt(compX * compX + compY * compY);
    }

    // EFFECTS:
    // returns a normalized version of the vector;
    // if the magnitude is near zero, returns (0, 0)
    public static Vector2 normalize(Vector2 vector) {
        float mag = vector.magnitude();
        if (Math.abs(mag) < EPSILON) {
            return new Vector2(0.0f, 0.0f);
        }
        return Vector2.multiply(vector, 1.0f / mag);
    }

    // EFFECTS:
    // returns the dot product of leftVector and rightVector
    public static float dotProduct(Vector2 leftVector, Vector2 rightVector) {
        return leftVector.getX() * rightVector.getX()
             + leftVector.getY() * rightVector.getY();
    }

    // EFFECTS:
    // returns true if all components differ by less than EPSILON
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Vector2)) {
            throw new NonMatchingClassException();
        }

        Vector2 otherVector = (Vector2) other;
        float dx = Math.abs(this.getX() - otherVector.getX());
        float dy = Math.abs(this.getY() - otherVector.getY());

        return (dx < EPSILON) && (dy < EPSILON);
    }

    // EFFECTS:
    // returns string of the form (x y)
    @Override
    public String toString() {
        return String.format("(%.2f %.2f)", compX, compY);
    }
}
