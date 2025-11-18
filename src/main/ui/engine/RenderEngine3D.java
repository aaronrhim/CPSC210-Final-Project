package ui.engine;

import ui.*;
import model.*;

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.*;

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

    private static final int COLOR_BG = 0xFF000000;       // black

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

    public RenderEngine3D(JPanel parent, int size) {
        this.parent = parent;
        parent.setFocusable(true);

        simState = SimulatorState.getInstance();

        bufferSize = size;
        depthBuffer = new float[size * size];
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        colorBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        imageSync = new ReentrantLock();

        viewTransform = buildDefaultViewTransform();

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

    // panel → RenderEngine contract
    public JPanel getPanel() {
        return parent;
    }

    public void setViewTransform(Transform vt) {
        this.viewTransform = vt;
    }

    // ------------------------------------------------------------------------
    //  PUBLIC DRAW INTERFACE
    // ------------------------------------------------------------------------

    public void drawCurrentFrame(Graphics g) {
        Rectangle bounds = g.getClipBounds();
        int imgSize = (int)((float) Math.min(bounds.width, bounds.height) * 0.97f);
        int offX = (bounds.width - imgSize) / 2;
        int offY = (bounds.height - imgSize) / 2;

        imageSync.lock();
        g.drawImage(image, offX, offY, imgSize, imgSize, null);
        imageSync.unlock();
    }

    @Override
    public void tick() {
        simState.lock();
        imageSync.lock();

        ensureMeshSynced();
        clearBuffers();

        drawReferenceLines();
        drawWireGrid();
        drawGradientPath();

        imageSync.unlock();
        simState.unlock();
    }

    // ------------------------------------------------------------------------
    //  CORE WIREFRAME DRAWING
    // ------------------------------------------------------------------------

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

    private Vector3 buildPathVertex(Vector2 point, ScalarField field) {
        try {
            float height = field.evaluate(point.getX(), point.getY());
            if (height < field.getZMin() || height > field.getZMax()) {
                return null;
            }

            float yRange = field.getYMax() - field.getYMin();
            if (Math.abs(yRange) < 0.0001f) {
                yRange = 1.0f;
            }
            float normalizedDepth = (point.getY() - field.getYMin()) / yRange;
            float depth = -5.0f - normalizedDepth * 25.0f;

            return new Vector3(point.getX(), height, depth);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private void drawGradientPath() {
        Simulation simulation = simState.getSimulation();
        ScalarField field = simulation.getField();
        if (field == null) {
            return;
        }

        List<Vector2> pathPoints = simulation.getPath();
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

    private void draw3DLine(Vector3 worldA, Vector3 worldB, Float heightA, Float heightB, Integer overrideColor) {
        Vector3 a = Transform.multiply(viewTransform, worldA);
        Vector3 b = Transform.multiply(viewTransform, worldB);

        // Order by depth
        if (a.getZ() > b.getZ()) {
            Vector3 tmp = a; a = b; b = tmp;
            Float tmpH = heightA; heightA = heightB; heightB = tmpH;
        }

        if (a.getZ() >= CLIP_Z && b.getZ() >= CLIP_Z)
            return;

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

    private Vector3 project(Vector3 p) {
        float x = p.getX() / p.getZ();   // divide by -Z because forward is negative
        float y = p.getY() / p.getZ();

        x = ((x + 1) * 0.5f) * bufferSize;
        y = ((y + 1) * 0.5f) * bufferSize;

        return new Vector3(x, y, p.getZ());
    }


    private void draw2DLine(Vector3 a, Vector3 b, Float heightA, Float heightB, Integer overrideColor) {
        // simple Bresenham-style stepper (not pixel-perfect, but fast and clean)
        float dx = b.getX() - a.getX();
        float dy = b.getY() - a.getY();
        int steps = (int) Math.max(Math.abs(dx), Math.abs(dy));
        if (steps < 1) steps = 1;

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
            drawPixel((int)x, (int)y, z, color);
            x += sx;
            y += sy;
            z += dz;
        }
    }

    // ------------------------------------------------------------------------
    //  FRAMEBUFFER HELPERS
    // ------------------------------------------------------------------------

    private void drawPixel(int x, int y, float z, int color) {
        if (x < 0 || x >= bufferSize || y < 0 || y >= bufferSize)
            return;

        int idx = x + bufferSize * (bufferSize - 1 - y);
        if (depthBuffer[idx] >= z) return;

        depthBuffer[idx] = z;
        colorBuffer[idx] = color;
    }

    private void clearBuffers() {
        for (int i = 0; i < bufferSize * bufferSize; i++) {
            colorBuffer[i] = COLOR_BG;
            depthBuffer[i] = Float.NEGATIVE_INFINITY;
        }
    }

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
