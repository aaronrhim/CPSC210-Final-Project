package model;

// Represents a 3-component vector which supports a common set of vector operations
public class Vector3 {
    private static final float EPSILON = 0.001f;

    private final float compX;
    private final float compY;
    private final float compZ;

    // EFFECTS:
    // creates a vector all components as 0
    public Vector3() {
        this(0.0f, 0.0f, 0.0f);
    }

    // EFFECTS:
    // creates a copy of the target vector
    public Vector3(Vector3 target) {
        this(target.compX, target.compY, target.compZ);
    }

    // EFFECTS:
    // creates a vector with the same xyz components as specified in the parameters
    public Vector3(float compX, float compY, float compZ) {
        this.compX = compX;
        this.compY = compY;
        this.compZ = compZ;
    }

    public float getX() {
        return compX;
    }

    public float getY() {
        return compY;
    }

    public float getZ() {
        return compZ;
    }

    // EFFECTS:
    // returns a vector where each component is the sum of leftVector and
    // rightVector's corresponding components
    public static Vector3 add(Vector3 leftVector, Vector3 rightVector) {
        return new Vector3(leftVector.compX + rightVector.compX,
                           leftVector.compY + rightVector.compY,
                           leftVector.compZ + rightVector.compZ);
    }

    // EFFECTS:
    // returns a vector where each component is the difference of leftVector and
    // rightVector's corresponding components
    public static Vector3 sub(Vector3 leftVector, Vector3 rightVector) {
        return new Vector3(leftVector.compX - rightVector.compX,
                           leftVector.compY - rightVector.compY,
                           leftVector.compZ - rightVector.compZ);
    }

    // EFFECTS:
    // returns a vector where each component is the same as vector's corresponding
    // components multiplied by scalar
    public static Vector3 multiply(Vector3 vector, float scalar) {
        return new Vector3(vector.compX * scalar,
                           vector.compY * scalar,
                           vector.compZ * scalar);
    }

    // EFFECTS:
    // returns the magnitude of the vector
    public float magnitude() {
        return (float) Math.sqrt(compX * compX + compY * compY + compZ * compZ);
    }

    public static Vector3 cross(Vector3 left, Vector3 right) {
        float x = left.compY * right.compZ - left.compZ * right.compY;
        float y = left.compZ * right.compX - left.compX * right.compZ;
        float z = left.compX * right.compY - left.compY * right.compX;
        return new Vector3(x, y, z);
    }


    // EFFECTS:
    // returns a vector with the same direction as the passed vector but the sum of
    // all components is now 1.0f
    public static Vector3 normalize(Vector3 vector) {
        float mag = vector.magnitude();
        if (Math.abs(mag) < EPSILON) {
            return new Vector3(0.0f, 0.0f, 0.0f);
        }
        return Vector3.multiply(vector, 1.0f / mag);
    }

    // EFFECTS:
    // returns the dot product of the left and right vectors
    public static float dotProduct(Vector3 leftVector, Vector3 rightVector) {
        return leftVector.compX * rightVector.compX
             + leftVector.compY * rightVector.compY
             + leftVector.compZ * rightVector.compZ;
    }

    // EFFECTS: returns whether all components of the vector are the same
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector3)) {
            return false;
        }
        Vector3 otherVector = (Vector3) other;
        return almostEqual(compX, otherVector.compX)
            && almostEqual(compY, otherVector.compY)
            && almostEqual(compZ, otherVector.compZ);
    }

    // EFFECTS: produces a string of the form (x, y z)
    @Override
    public String toString() {
        return String.format("(%.2f %.2f %.2f)", compX, compY, compZ);
    }

    private static boolean almostEqual(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }
}
