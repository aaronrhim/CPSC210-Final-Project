package model;

// Represents a 4x4 affine transform matrix (yayy computer graphics!)
public class Transform {
    private static final float DEGREE_TO_RAD = 0.0174533f;
    private static final int ROW_COUNT = 4;
    private static final int COL_COUNT = 4;
    private float[][] components;

    // EFFECTS: creates an identity matrix
    public Transform() {
        components = new float[ROW_COUNT][COL_COUNT];
        components[0][0] = 1.0f;
        components[1][1] = 1.0f;
        components[2][2] = 1.0f;
        components[3][3] = 1.0f;
    }

    public float[][] getComponents() {
        return components;
    }

    // EFFECTS: creates a translation matrix
    public static Transform translation(Vector3 trl) {
        Transform matrix = new Transform();
        matrix.components[0][3] = trl.getX();
        matrix.components[1][3] = trl.getY();
        matrix.components[2][3] = trl.getZ();
        return matrix;
    }

    // EFFECTS: creates a scale matrix
    public static Transform scale(Vector3 scl) {
        Transform matrix = new Transform();
        matrix.components[0][0] = scl.getX();
        matrix.components[1][1] = scl.getY();
        matrix.components[2][2] = scl.getZ();
        return matrix;
    }

    // EFFECTS: creates a rotation matrix about the x axis
    public static Transform rotationX(float rotDegrees) {
        Transform matrix = new Transform();

        float cosDeg = cosDegrees(rotDegrees);
        float sinDeg = sinDegrees(rotDegrees);
        matrix.components[1][1] = cosDeg;
        matrix.components[2][1] = sinDeg;
        matrix.components[1][2] = -sinDeg;
        matrix.components[2][2] = cosDeg;
        return matrix;
    }

    // EFFECTS: creates a rotation matrix about the y axis
    public static Transform rotationY(float rotDegrees) {
        Transform matrix = new Transform();

        float cosDeg = cosDegrees(rotDegrees);
        float sinDeg = sinDegrees(rotDegrees);
        matrix.components[0][0] = cosDeg;
        matrix.components[0][2] = sinDeg;
        matrix.components[2][0] = -sinDeg;
        matrix.components[2][2] = cosDeg;
        return matrix;
    }

    // EFFECTS: creates a rotation matrix about the z axis
    public static Transform rotationZ(float rotDegrees) {
        Transform matrix = new Transform();

        float cosDeg = cosDegrees(rotDegrees);
        float sinDeg = sinDegrees(rotDegrees);
        matrix.components[0][0] = cosDeg;
        matrix.components[0][1] = sinDeg;
        matrix.components[1][0] = -sinDeg;
        matrix.components[1][1] = cosDeg;
        return matrix;
    }

    // EFFECTS: creates a 3D rotation matrix
    public static Transform rotation(Vector3 rot) {
        Transform matrix = new Transform();

        Transform rotX = rotationX(rot.getX());
        Transform rotY = rotationY(rot.getY());
        Transform rotZ = rotationZ(rot.getZ());

        matrix = multiply(matrix, rotX);
        matrix = multiply(matrix, rotY);
        matrix = multiply(matrix, rotZ);

        return matrix;
    }

    // EFFECTS: creates a TRS matrix
    public static Transform transform(Vector3 trl, Vector3 rot, Vector3 scl) {
        Transform tformMatrix = new Transform();
        Transform matScale = scale(scl);
        Transform matRot = rotation(rot);
        Transform matTrans = translation(trl);

        tformMatrix = multiply(tformMatrix, matScale);
        tformMatrix = multiply(tformMatrix, matRot);
        tformMatrix = multiply(tformMatrix, matTrans);

        return tformMatrix;
    }

    // EFFECTS: returns the multiplication of two matricies
    public static Transform multiply(Transform left, Transform right) {
        Transform out = new Transform();
        float[][] leftComponents = left.components;
        float[][] rightComponents = right.components;
        float[][] outComponents = out.components;

        for (int r = 0; r < ROW_COUNT; r++) {
            for (int c = 0; c < COL_COUNT; c++) {
                outComponents[r][c] =
                        leftComponents[r][0] * rightComponents[0][c]
                        + leftComponents[r][1] * rightComponents[1][c]
                        + leftComponents[r][2] * rightComponents[2][c]
                        + leftComponents[r][3] * rightComponents[3][c];
            }
        }
        return out;
    }

    // EFFECTS: returns the multiplication of a matricie and a vector
    public static Vector3 multiply(Transform matr, Vector3 v) {
        float[][] m = matr.getComponents();

        float x = m[0][0] * v.getX()
                + m[0][1] * v.getY()
                + m[0][2] * v.getZ()
                + m[0][3] * 1.0f;
        float y = m[1][0] * v.getX()
                + m[1][1] * v.getY()
                + m[1][2] * v.getZ()
                + m[1][3] * 1.0f;
        float z = m[2][0] * v.getX()
                + m[2][1] * v.getY()
                + m[2][2] * v.getZ()
                + m[2][3] * 1.0f;

        return new Vector3(x, y, z);
    }


    // EFFECTS: extracts a scale vector from the transform
    public static Vector3 extractScale(Transform matrix) {
        float[][] comp = matrix.components;
        float scaleX = new Vector3(comp[0][0], comp[0][1], comp[0][2]).magnitude();
        float scaleY = new Vector3(comp[1][0], comp[1][1], comp[1][2]).magnitude();
        float scaleZ = new Vector3(comp[2][0], comp[2][1], comp[2][2]).magnitude();
        return new Vector3(scaleX, scaleY, scaleZ);
    }

    // EFFECTS: extracts a translation vector from the transform
    public static Vector3 extractTranslation(Transform matrix) {
        float[][] comp = matrix.components;
        return new Vector3(comp[3][0], comp[3][1], comp[3][2]);
    }

    // EFFECTS: returns sinx in degrees
    private static float sinDegrees(float x) {
        return (float) Math.sin(x * DEGREE_TO_RAD);
    }

    // EFFECTS: returns cosx in degrees
    private static float cosDegrees(float x) {
        return (float) Math.cos(x * DEGREE_TO_RAD);
    }

    public static Transform lookAt(Vector3 eye, Vector3 center, Vector3 up) {
        Vector3 f = Vector3.normalize(Vector3.sub(center, eye));     // forward
        Vector3 s = Vector3.normalize(Vector3.cross(f, up));         // side
        Vector3 u = Vector3.cross(s, f);                             // corrected up

        Transform matrix = new Transform();
        float[][] m = matrix.getComponents();

        m[0][0] = s.getX();
        m[1][0] = s.getY();
        m[2][0] = s.getZ();
        m[3][0] = -Vector3.dotProduct(s, eye);

        m[0][1] = u.getX();
        m[1][1] = u.getY();
        m[2][1] = u.getZ();
        m[3][1] = -Vector3.dotProduct(u, eye);

        m[0][2] = -f.getX();
        m[1][2] = -f.getY();
        m[2][2] = -f.getZ();
        m[3][2] = Vector3.dotProduct(f, eye);

        m[0][3] = 0f;
        m[1][3] = 0f;
        m[2][3] = 0f;
        m[3][3] = 1f;

        return matrix;
    }

}
