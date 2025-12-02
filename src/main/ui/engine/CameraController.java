package ui.engine;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import model.*;
import ui.Tickable;
import java.awt.event.*;
import java.util.*;

/**
 * Virtual camera controller for the 3D viewer with movement and rotation controls.
 */
@ExcludeFromJacocoGeneratedReport
public class CameraController implements Tickable, KeyListener, MouseListener {
    private static final Vector3 INITIAL_POSITION = new Vector3(0, 0, 30.0f);

    private static final float MAX_VELOCITY = 300.0f;
    private static final float MAX_VELOCITY_SHIFT_FACTOR = 7.5f;
    private static final float MAX_VELOCITY_CTRL_FACTOR = 0.15f;
    private static final float ACCELERATION = 1000.0f;
    private static final float ACCELERATION_SHIFT_FACTOR = 10.0f;
    private static final float ACCELERATION_CTRL_FACTOR = 0.2f;
    private static final float DRAG = 0.97f;

    private static final float MAX_ANGULAR_VELOCITY = 90.0f;
    private static final float ANGULAR_ACCELERATION = 700.0f;
    private static final float ANGULAR_DRAG = 0.98f;

    private static final float PITCH_RANGE = 85.0f;

    private final RenderEngine3D parent;

    private final Set<Integer> keysDown;
    private long lastTickNanoseconds;

    private Vector3 position;
    private Vector3 velocity;

    private float yaw;
    private float yawVelocity;
    private float pitch;
    private float pitchVelocity;

    private Transform viewTransform;

    // EFFECTS: computes seconds since last tick and resets timestamp
    private float computeDeltaSeconds() {
        long now = System.nanoTime();
        float deltaTime = (now - lastTickNanoseconds) / 1_000_000_000.0f;
        lastTickNanoseconds = now;
        return deltaTime;
    }

    // EFFECTS: computes base acceleration factoring modifier keys
    private float baseAcceleration() {
        float accel = ACCELERATION;
        if (keysDown.contains(KeyEvent.VK_SHIFT)) {
            accel *= ACCELERATION_SHIFT_FACTOR;
        }
        if (keysDown.contains(KeyEvent.VK_CONTROL)) {
            accel *= ACCELERATION_CTRL_FACTOR;
        }
        return accel;
    }

    // MODIFIES: this
    // EFFECTS: clamps velocity magnitude and applies drag
    private void applySpeedLimitAndDrag(float deltaTime) {
        float maxVel = MAX_VELOCITY;
        if (keysDown.contains(KeyEvent.VK_SHIFT)) {
            maxVel *= MAX_VELOCITY_SHIFT_FACTOR;
        }
        if (keysDown.contains(KeyEvent.VK_CONTROL)) {
            maxVel *= MAX_VELOCITY_CTRL_FACTOR;
        }

        velocity = clampVector(velocity, maxVel);
        velocity = Vector3.multiply(velocity, (float) Math.pow(1.0f - DRAG, deltaTime));
    }

    // MODIFIES: this
    // EFFECTS: integrates position and orientation from velocities
    private void integratePose(float deltaTime) {
        Transform rotation = Transform.multiply(Transform.rotationX(pitch), Transform.rotationY(yaw));
        Vector3 move = Transform.multiply(rotation, velocity);
        position = Vector3.add(position, Vector3.multiply(move, deltaTime));

        yawVelocity = clamp(yawVelocity, MAX_ANGULAR_VELOCITY);
        pitchVelocity = clamp(pitchVelocity, MAX_ANGULAR_VELOCITY);

        yaw += yawVelocity * deltaTime;
        pitch += pitchVelocity * deltaTime;
        pitch = clamp(pitch, PITCH_RANGE);

        yawVelocity *= Math.pow(1.0f - ANGULAR_DRAG, deltaTime);
        pitchVelocity *= Math.pow(1.0f - ANGULAR_DRAG, deltaTime);
    }

    // MODIFIES: this
    // EFFECTS: rebuilds the view transform matrix from current pose
    private void buildViewMatrix() {
        viewTransform = Transform.translation(Vector3.multiply(position, -1.0f));
        viewTransform = Transform.multiply(viewTransform, Transform.rotationY(-yaw));
        viewTransform = Transform.multiply(viewTransform, Transform.rotationX(-pitch));
        parent.setViewTransform(viewTransform);
    }
    
    // EFFECTS: initializes and registers input listeners
    public CameraController(RenderEngine3D parent) {
        this.parent = parent;
        this.parent.getPanel().addKeyListener(this);
        this.parent.getPanel().addMouseListener(this);

        keysDown = new HashSet<>();
        resetCamera();
        lastTickNanoseconds = System.nanoTime();
    }

    // MODIFIES: this
    // EFFECTS: resets camera to default position and orientation
    public void resetCamera() {
        position = new Vector3(INITIAL_POSITION);
        velocity = new Vector3();
        yaw = 0.0f;
        yawVelocity = 0.0f;
        pitch = 0.0f;
        pitchVelocity = 0.0f;
        viewTransform = new Transform();
    }

    @Override
    @SuppressWarnings("methodlength")
    // MODIFIES: this
    // EFFECTS: polls time delta, processes input, integrates motion, and updates view transform
    public void tick() {
        float deltaTime = computeDeltaSeconds();
        clearKeysIfUnfocused();
        handleInputs(deltaTime);
        applySpeedLimitAndDrag(deltaTime);
        integratePose(deltaTime);
        buildViewMatrix();
    }

    // MODIFIES: this
    // EFFECTS: applies acceleration/rotation based on current input state
    @SuppressWarnings("methodlength")
    private void handleInputs(float deltaTime) {
        float accel = baseAcceleration();
        if (keysDown.contains(KeyEvent.VK_W)) {
            velocity = Vector3.add(velocity, new Vector3(0, 0, -accel * deltaTime));
        }
        if (keysDown.contains(KeyEvent.VK_S)) {
            velocity = Vector3.add(velocity, new Vector3(0, 0, accel * deltaTime));
        }
        if (keysDown.contains(KeyEvent.VK_A)) {
            velocity = Vector3.add(velocity, new Vector3(-accel * deltaTime, 0, 0));
        }
        if (keysDown.contains(KeyEvent.VK_D)) {
            velocity = Vector3.add(velocity, new Vector3(accel * deltaTime, 0, 0));
        }

        if (keysDown.contains(KeyEvent.VK_LEFT)) {
            yawVelocity -= ANGULAR_ACCELERATION * deltaTime;
        }
        if (keysDown.contains(KeyEvent.VK_RIGHT)) {
            yawVelocity += ANGULAR_ACCELERATION * deltaTime;
        }
        if (keysDown.contains(KeyEvent.VK_UP)) {
            pitchVelocity -= ANGULAR_ACCELERATION * deltaTime;
        }
        if (keysDown.contains(KeyEvent.VK_DOWN)) {
            pitchVelocity += ANGULAR_ACCELERATION * deltaTime;
        }

        if (keysDown.contains(KeyEvent.VK_R)) {
            resetCamera();
        }
    }

    // EFFECTS: removes any pressed keys if panel not focused
    private void clearKeysIfUnfocused() {
        if (!parent.getPanel().isFocusOwner()) {
            keysDown.clear();
        }
    }

    // EFFECTS: clamps vector magnitude to given bound
    private static Vector3 clampVector(Vector3 vec, float bound) {
        return vec.magnitude() < bound ? vec : Vector3.multiply(Vector3.normalize(vec), bound);
    }

    // EFFECTS: clamps scalar to +/- maxAbs
    private static float clamp(float val, float maxAbs) {
        return Math.max(Math.min(val, maxAbs), -maxAbs);
    }

    // Input events
    @Override
    public void keyPressed(KeyEvent e) {
        keysDown.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keysDown.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // unused
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        parent.getPanel().requestFocusInWindow();
    }

    @Override
    public void mousePressed(MouseEvent e) { 
        // stub
    }

    @Override
    public void mouseReleased(MouseEvent e) { 
        // stub
    }

    @Override
    public void mouseEntered(MouseEvent e) { 
        // stub
    }

    @Override
    public void mouseExited(MouseEvent e) { 
        // stub
    }
}
