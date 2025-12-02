package model;

// Represents a 2-component vector supporting a common set of vector operations
public class Vector2 {
    private static final float EPSILON = 0.001f;

    private final float compX;
    private final float compY;

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
        return new Vector2(leftVector.compX + rightVector.compX,
                           leftVector.compY + rightVector.compY);
    }

    // EFFECTS:
    // returns a vector whose components are leftVector minus rightVector
    public static Vector2 sub(Vector2 leftVector, Vector2 rightVector) {
        return new Vector2(leftVector.compX - rightVector.compX,
                           leftVector.compY - rightVector.compY);
    }

    // EFFECTS:
    // returns a vector whose components are vector scaled by scalar
    public static Vector2 multiply(Vector2 vector, float scalar) {
        return new Vector2(vector.compX * scalar, vector.compY * scalar);
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
        return leftVector.compX * rightVector.compX
             + leftVector.compY * rightVector.compY;
    }

    // EFFECTS:
    // returns true if all components differ by less than EPSILON
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector2)) {
            return false;
        }

        Vector2 otherVector = (Vector2) other;
        return almostEqual(compX, otherVector.compX)
            && almostEqual(compY, otherVector.compY);
    }

    // EFFECTS:
    // returns string of the form (x y)
    @Override
    public String toString() {
        return String.format("(%.2f %.2f)", compX, compY);
    }

    // EFFECTS: true if difference within EPSILON
    private static boolean almostEqual(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }
}
