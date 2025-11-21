package ui.engine;

import model.*;
import ui.Tickable;
import java.awt.event.*;
import java.util.*;

/*
    Virtual camera controller for the 3D viewer with movement and rotation controls
*/
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

    private RenderEngine3D parent;

    private Set<Integer> keysDown;
    private long lastTickNanoseconds;

    private Vector3 position;
    private Vector3 velocity;

    private float yaw;
    private float yawVelocity;
    private float pitch;
    private float pitchVelocity;

    private Transform viewTransform;

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
    // MODIFIES: this
    // EFFECTS: polls time delta, processes input, integrates motion, and updates view transform
    public void tick() {
        long now = System.nanoTime();
        float deltaTime = (now - lastTickNanoseconds) / 1_000_000_000.0f;
        lastTickNanoseconds = now;

        if (!parent.getPanel().isFocusOwner()) {
            keysDown.clear();
        }

        handleInputs(deltaTime);

        Transform rotation = Transform.multiply(Transform.rotationX(pitch), Transform.rotationY(yaw));
        float maxVel = MAX_VELOCITY;
        if (keysDown.contains(KeyEvent.VK_SHIFT)) maxVel *= MAX_VELOCITY_SHIFT_FACTOR;
        if (keysDown.contains(KeyEvent.VK_CONTROL)) maxVel *= MAX_VELOCITY_CTRL_FACTOR;

        velocity = clampVector(velocity, maxVel);
        velocity = Vector3.multiply(velocity, (float) Math.pow(1.0f - DRAG, deltaTime));

        Vector3 move = Transform.multiply(rotation, velocity);
        position = Vector3.add(position, Vector3.multiply(move, deltaTime));

        yawVelocity = clamp(yawVelocity, MAX_ANGULAR_VELOCITY);
        pitchVelocity = clamp(pitchVelocity, MAX_ANGULAR_VELOCITY);

        yaw += yawVelocity * deltaTime;
        pitch += pitchVelocity * deltaTime;

        pitch = clamp(pitch, PITCH_RANGE);

        yawVelocity *= Math.pow(1.0f - ANGULAR_DRAG, deltaTime);
        pitchVelocity *= Math.pow(1.0f - ANGULAR_DRAG, deltaTime);

        viewTransform = Transform.translation(Vector3.multiply(position, -1.0f));
        viewTransform = Transform.multiply(viewTransform, Transform.rotationY(-yaw));
        viewTransform = Transform.multiply(viewTransform, Transform.rotationX(-pitch));
        parent.setViewTransform(viewTransform);
    }

    // MODIFIES: this
    // EFFECTS: applies acceleration/rotation based on current input state
    private void handleInputs(float deltaTime) {
        float accel = ACCELERATION;
        if (keysDown.contains(KeyEvent.VK_SHIFT)) accel *= ACCELERATION_SHIFT_FACTOR;
        if (keysDown.contains(KeyEvent.VK_CONTROL)) accel *= ACCELERATION_CTRL_FACTOR;

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

        // Optional: Reset camera with R
        if (keysDown.contains(KeyEvent.VK_R)) {
            resetCamera();
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
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
}
