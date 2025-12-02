package ui.engine;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import ui.*;
import model.*;

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.*;

/**
 * 3D render engine for gradient descent simulation visualization.
 */
@ExcludeFromJacocoGeneratedReport
public class RenderEngine3D implements Tickable {

    public static final float CLIP_Z = -0.1f;
    private static final int DEFAULT_MESH_RESOLUTION = 48;
    private static final float AXIS_DEPTH = -15f;
    private static final int COLOR_AXIS_X = 0xFFCC4444;
    private static final int COLOR_AXIS_Y = 0xFF44CC44;
    private static final int COLOR_AXIS_Z = 0xFF4488CC;
    private static final int COLOR_GRID = 0xFF3A3A3A;
    private static final int COLOR_PATH = 0xFFFFD200;

    private int bufferSize;
    private int[] colorBuffer;
    private float[] depthBuffer;
    private BufferedImage image;
    private ReentrantLock imageSync;

    private SimulatorState simState;
    private JPanel parent;

    private Transform viewTransform;
    private Vector3[][] meshGrid;
    private ScalarField lastFieldUsed;
    private float minHeight;
    private float maxHeight;
    private ArrayList<LineSegment> referenceLines;
    private float lastXMin;
    private float lastXMax;
    private float lastYMin;
    private float lastYMax;
    private float lastZMin;
    private float lastZMax;

    private static final int COLOR_BG = 0xFF000000; // black

    private static class LineSegment {
        Vector3 start;
        Vector3 end;
        int color;

        LineSegment(Vector3 start, Vector3 end, int color) {
            this.start = start;
            this.end = end;
            this.color = color;
        }
    }

    // REQUIRES: parent != null and size > 0
    // MODIFIES: this, parent
    // EFFECTS: initializes the 3D render engine in parent location in JFrame, and grabs sim state
    // and builds wireframe grid
    public RenderEngine3D(JPanel parent, int size) {
        this.parent = parent;
        parent.setFocusable(true); // keyboard listener

        simState = SimulatorState.getInstance(); // singleton state for concurrency

        bufferSize = size;
        depthBuffer = new float[size * size];
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        colorBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        imageSync = new ReentrantLock();

        viewTransform = buildDefaultViewTransform(); // 

        meshGrid = null;
        referenceLines = new ArrayList<>();
        buildReferenceLines();
        minHeight = 0f;
        maxHeight = 1f;
        lastXMin = Float.NaN;
        lastXMax = Float.NaN;
        lastYMin = Float.NaN;
        lastYMax = Float.NaN;
        lastZMin = Float.NaN;
        lastZMax = Float.NaN;
    }

    // EFFECTS: returns the Swing panel hosting the render target
    public JPanel getPanel() {
        return parent;
    }

    // REQUIRES: vt non-null
    // MODIFIES: this
    // EFFECTS: sets the view transform used for projecting world coordinates into camera space
    public void setViewTransform(Transform vt) {
        this.viewTransform = vt;
    }

    // REQUIRES: g not null
    // MODIFIES: none (reads from image under lock)
    // EFFECTS: draws the latest rendered frame into the provided Graphics context
    // NOTE: My "Y" axis is actually my Z axis. Standard in vt
    public void drawCurrentFrame(Graphics g) {
        Rectangle bounds = (g.getClipBounds() != null)
                ? g.getClipBounds()
                : new Rectangle(parent.getWidth(), parent.getHeight());
        int imgSize = (int) (Math.min(bounds.width, bounds.height) * 0.97f);
        int offX = (bounds.width - imgSize) / 2;
        int offY = (bounds.height - imgSize) / 2;

        imageSync.lock();
        try {
            g.drawImage(image, offX, offY, imgSize, imgSize, null);
        } finally {
            imageSync.unlock();
        }
    }

    // REQUIRES: simState != null and imageSync available
    // MODIFIES: image, depthBuffer, colorBuffer, simState (reads)
    @Override
    public void tick() {
        simState.lock();
        imageSync.lock();
        try {
            ensureMeshSynced();
            clearBuffers();
            renderScene();
        } finally {
            imageSync.unlock();
            simState.unlock();
        }
    }

    // MODIFIES: buffers
    // EFFECTS: draws axes, grid, and path onto the buffers
    private void renderScene() {
        drawReferenceLines();
        drawWireGrid();
        drawGradientPath();
    }

    // REQUIRES: meshGrid != null, meshGrid.length != 0
    // MODIFIES: simState
    // EFFECTS: Creates 3D vectors for x and y (still need to implement z grid but i've already 
    // tried and it looks bad so idk) depending on grid length (rn set to 10) and then draws them
    // which converts the 3D vectors into 2D vectors that will display with the viewTransform. Rn
    // this method is very convoluted but ima fix it at some point
    // SOURCE: I received a lot of help from my friend who built the OpenGL renderer but also this
    // tutorial really helped: https://www.opengl-tutorial.org/beginners-tutorials/tutorial-3-matrices/
    @SuppressWarnings("methodlength")
    private void drawWireGrid() {
        if (meshGrid == null || meshGrid.length == 0 || meshGrid[0].length == 0) {
            return;
        }

        int width = meshGrid.length;
        int height = meshGrid[0].length;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Vector3 current = meshGrid[i][j];
                if (current == null) {
                    continue;
                }

                if (i < width - 1) {
                    Vector3 right = meshGrid[i + 1][j];
                    if (right != null) {
                        draw3DLine(current, right, current.getY(), right.getY(), null);
                    }
                }

                if (j < height - 1) {
                    Vector3 up = meshGrid[i][j + 1];
                    if (up != null) {
                        draw3DLine(current, up, current.getY(), up.getY(), null);
                    }
                }
            }
        }
    }

    // Note: Since all my math is being done in 2D (Scalar Fields -> z = xy), i made this method
    // to transform my 2D vectors into 3D vectors which then get converted back into 2D vectors
    // with viewTransform which then gets projected into the JPanel through rasterization (drawPanel)
    // EFFECTS: returns a Vector3 obj that contains true pixel position on screen
    private Vector3 buildPathVertex(Vector2 point, ScalarField field) {
        try {
            float height = field.evaluate(point.getX(), point.getY());
            if (height < field.getZMin() || height > field.getZMax()) {
                return null;
            }

            float yrange = field.getYMax() - field.getYMin();
            if (Math.abs(yrange) < 0.0001f) {
                yrange = 1.0f;
            }
            // norm to panel size and proj to 2D
            float normalizedDepth = (point.getY() - field.getYMin()) / yrange; 
            float depth = -5.0f - normalizedDepth * 25.0f;

            return new Vector3(point.getX(), height, depth);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    // Note: Just for visualization purposes, currently facing problems with vanishing and explo grad
    // REQUIRES: field != null, gd must be computed beforehand (currently done with rand button)
    // MODIFIES: simState as Simulation
    // EFFECTS: draw lines connecting points in ArrayList of gradient descent points
    private void drawGradientPath() {
        Simulation simulation = simState.getSimulation();
        ScalarField field = simulation.getField();
        if (field == null) {
            return;
        }

        List<Vector2> pathPoints = simulation.getPath(); // computed beforehand
        if (pathPoints.isEmpty()) {
            return;
        }

        Vector3 previous = null;
        for (Vector2 point : pathPoints) {
            Vector3 worldPoint = buildPathVertex(point, field);
            if (worldPoint == null) {
                previous = null;
                continue;
            }

            if (previous == null) {
                draw3DLine(worldPoint, worldPoint, worldPoint.getY(), worldPoint.getY(), COLOR_PATH);
            } else {
                draw3DLine(previous, worldPoint, previous.getY(), worldPoint.getY(), COLOR_PATH);
            }

            previous = worldPoint;
        }
    }

    // MODIFIES: apply vt to arg1 and arg2 into Vector3
    // EFFECTS: draw in 2D space after vt and rasterization
    private void draw3DLine(Vector3 worldA, Vector3 worldB, Float heightA, Float heightB, Integer overrideColor) {
        Vector3 a = Transform.multiply(viewTransform, worldA);
        Vector3 b = Transform.multiply(viewTransform, worldB);

        // order by depth cause z axis is depth
        if (a.getZ() > b.getZ()) {
            Vector3 tmp = a;
            a = b;
            b = tmp;
            Float tmpH = heightA;
            heightA = heightB;
            heightB = tmpH;
        }

        if (a.getZ() >= CLIP_Z && b.getZ() >= CLIP_Z) { // slider border
            return;
        }

        // for z slider reasons
        if (b.getZ() >= CLIP_Z) {
            float f = (CLIP_Z - a.getZ()) / (b.getZ() - a.getZ());
            b = Vector3.add(Vector3.multiply(a, 1 - f), Vector3.multiply(b, f));
            if (heightA != null && heightB != null) {
                heightB = heightA + (heightB - heightA) * f;
            }
        }

        Vector3 p0 = project(a);
        Vector3 p1 = project(b);
        draw2DLine(p0, p1, heightA, heightB, overrideColor);
    }

    // 
    private Vector3 project(Vector3 p) {
        float x = p.getX() / p.getZ(); // divide by -Z because forward is negative (CHANGED)
        float y = p.getY() / p.getZ();

        x = ((x + 1) * 0.5f) * bufferSize;
        y = ((y + 1) * 0.5f) * bufferSize;

        return new Vector3(x, y, p.getZ());
    }

    // REQUIRES: a, b already projected to screen space
    // MODIFIES: colorBuffer, depthBuffer
    // EFFECTS: rasterizes a straight line between a and b using a simple linear
    // stepper; interpolates depth and optional height for coloring; writes pixels through drawPixel
    private void draw2DLine(Vector3 a, Vector3 b, Float heightA, Float heightB, Integer overrideColor) {
        // simple Bresenham-style stepper (not pixel-perfect, but fast and clean)
        float dx = b.getX() - a.getX();
        float dy = b.getY() - a.getY();
        int steps = (int) Math.max(Math.abs(dx), Math.abs(dy));
        if (steps < 1) {
            steps = 1;
        }

        float sx = dx / steps;
        float sy = dy / steps;

        float x = a.getX();
        float y = a.getY();
        float z = a.getZ();
        float dz = (b.getZ() - a.getZ()) / steps;

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / (float) steps;
            int color = (overrideColor != null)
                    ? overrideColor
                    : heightToColor(interpolateHeight(heightA, heightB, t));
            drawPixel((int) x, (int) y, z, color);
            x += sx;
            y += sy;
            z += dz;
        }
    }

    // REQUIRES: 0 <= x,y < bufferSize
    // MODIFIES: colorBuffer, depthBuffer
    // EFFECTS: depth-tests the pixel at (x,y); if incoming z is closer, stores z and writes color into colorBuffer
    private void drawPixel(int x, int y, float z, int color) {
        if (x < 0 || x >= bufferSize || y < 0 || y >= bufferSize) {
            return;
        }

        int idx = x + bufferSize * (bufferSize - 1 - y);
        if (depthBuffer[idx] >= z) {
            return;
        }

        depthBuffer[idx] = z;
        colorBuffer[idx] = color;
    }

    private void clearBuffers() {
        for (int i = 0; i < bufferSize * bufferSize; i++) {
            colorBuffer[i] = COLOR_BG;
            depthBuffer[i] = Float.NEGATIVE_INFINITY;
        }
    }

    // MODIFIES: meshGrid, lastFieldUsed, cached domain values, height range
    // EFFECTS: checks if the ScalarField or its domain changed; if so regenerates the
    // mesh grid using SurfaceMeshGenerator and updates cached bounds; otherwise leaves the existing mesh untouched
    @SuppressWarnings("methodlength")
    private void ensureMeshSynced() {
        ScalarField currentField = simState.getSimulation().getField();
        if (currentField == null) {
            if (lastFieldUsed != null) {
                meshGrid = null;
                lastFieldUsed = null;
            }
            return;
        }

        boolean domainChanged = currentField.getXMin() != lastXMin
                || currentField.getXMax() != lastXMax
                || currentField.getYMin() != lastYMin
                || currentField.getYMax() != lastYMax
                || currentField.getZMin() != lastZMin
                || currentField.getZMax() != lastZMax;

        if (currentField == lastFieldUsed && !domainChanged) {
            return;
        }

        Vector3[][] generated = SurfaceMeshGenerator.generateGrid(
                currentField,
                currentField.getXMin(),
                currentField.getXMax(),
                currentField.getYMin(),
                currentField.getYMax(),
                DEFAULT_MESH_RESOLUTION);

        meshGrid = generated;
        lastFieldUsed = currentField;
        lastXMin = currentField.getXMin();
        lastXMax = currentField.getXMax();
        lastYMin = currentField.getYMin();
        lastYMax = currentField.getYMax();
        lastZMin = currentField.getZMin();
        lastZMax = currentField.getZMax();
        computeHeightRangeFromGrid();
    }

    private Transform buildDefaultViewTransform() {
        Vector3 cameraPosition = new Vector3(0f, 20f, 35f);
        float yaw = 0f;
        float pitch = 25f;

        Transform vt = Transform.translation(Vector3.multiply(cameraPosition, -1.0f));
        vt = Transform.multiply(vt, Transform.rotationY(yaw));
        vt = Transform.multiply(vt, Transform.rotationX(-pitch));
        return vt;
    }

    // MODIFIES: minHeight, maxHeight
    // EFFECTS: scans meshGrid for minimum and maximum Y values; normalizes invalid or
    // degenerate ranges; falls back to field Z-bounds when mesh is empty
    @SuppressWarnings("methodlength")
    private void computeHeightRangeFromGrid() {
        if (meshGrid == null || meshGrid.length == 0 || meshGrid[0].length == 0) {
            minHeight = 0f;
            maxHeight = 1f;
            return;
        }

        minHeight = Float.POSITIVE_INFINITY;
        maxHeight = Float.NEGATIVE_INFINITY;

        for (Vector3[] column : meshGrid) {
            for (Vector3 v : column) {
                if (v == null) {
                    continue;
                }
                float h = v.getY();
                if (h < minHeight) {
                    minHeight = h;
                }
                if (h > maxHeight) {
                    maxHeight = h;
                }
            }
        }

        if (minHeight == Float.POSITIVE_INFINITY || maxHeight == Float.NEGATIVE_INFINITY) {
            minHeight = lastZMin;
            maxHeight = lastZMax;
        }
        if (Float.isNaN(minHeight) || Float.isNaN(maxHeight)) {
            minHeight = 0f;
            maxHeight = 1f;
        }
        if (Math.abs(maxHeight - minHeight) < 0.0001f) {
            maxHeight = minHeight + 1.0f;
        }
    }

    private float interpolateHeight(Float a, Float b, float t) {
        if (a == null || b == null) {
            return minHeight;
        }
        return a + (b - a) * t;
    }

    private int heightToColor(float height) {
        float normalized = (height - minHeight) / (maxHeight - minHeight);
        normalized = Math.max(0f, Math.min(1f, normalized));

        int startR = 30;
        int startG = 110;
        int startB = 255;

        int endR = 255;
        int endG = 70;
        int endB = 60;

        int r = (int) (startR + (endR - startR) * normalized);
        int g = (int) (startG + (endG - startG) * normalized);
        int b = (int) (startB + (endB - startB) * normalized);

        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    // MODIFIES: referenceLines
    // EFFECTS: constructs the static world-space axis lines and grid lines used for
    // orientation reference in the 3D scene
    @SuppressWarnings("methodlength")
    private void buildReferenceLines() {
        referenceLines.clear();
        float axisLength = 12f;
        float gridHalf = 12f;
        float gridStep = 3f;

        // X axis (horizontal)
        referenceLines.add(new LineSegment(
                new Vector3(-axisLength, 0f, AXIS_DEPTH),
                new Vector3(axisLength, 0f, AXIS_DEPTH),
                COLOR_AXIS_X));

        // Y axis (vertical)
        referenceLines.add(new LineSegment(
                new Vector3(0f, -axisLength * 0.2f, AXIS_DEPTH),
                new Vector3(0f, axisLength, AXIS_DEPTH),
                COLOR_AXIS_Y));

        // Z axis (depth)
        referenceLines.add(new LineSegment(
                new Vector3(0f, 0f, AXIS_DEPTH - axisLength),
                new Vector3(0f, 0f, AXIS_DEPTH + axisLength),
                COLOR_AXIS_Z));

        for (float i = -gridHalf; i <= gridHalf; i += gridStep) {
            referenceLines.add(new LineSegment(
                    new Vector3(-gridHalf, 0f, AXIS_DEPTH + i),
                    new Vector3(gridHalf, 0f, AXIS_DEPTH + i),
                    COLOR_GRID));
            referenceLines.add(new LineSegment(
                    new Vector3(i, 0f, AXIS_DEPTH - gridHalf),
                    new Vector3(i, 0f, AXIS_DEPTH + gridHalf),
                    COLOR_GRID));
        }
    }

    private void drawReferenceLines() {
        for (LineSegment line : referenceLines) {
            draw3DLine(line.start, line.end, null, null, line.color);
        }
    }
}
