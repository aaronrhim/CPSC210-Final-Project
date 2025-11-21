package ui;

import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.*;

// Contains all simulation state related data
public class SimulatorState implements Tickable {
    public static final float TIMESCALE_MIN = 1.0f;
    public static final float TIMESCALE_MAX = 20.0f;
    private static final float MAX_DELTATIME = 0.1f;

    private static SimulatorState instance;

    private Simulation simulation;          // gradient descent engine
    private List<ScalarField> scalarFields; // backing list for ScalarFieldListPanel

    private float timeScale;
    private boolean isRunning;

    private long lastTickNanoseconds;
    private Lock lock;

    private float learningRate;

    // EFFECTS: creates a new simulation state and initializes defaults
    private SimulatorState() {
        if (instance != null) {
            throw new IllegalStateException();
        }

        simulation = new Simulation();
        scalarFields = new ArrayList<>();

        timeScale = 1.0f;
        isRunning = false;

        lastTickNanoseconds = System.nanoTime();
        lock = new ReentrantLock();

        learningRate = 0.01f;

        initializeDefaultField();
    }

    // EFFECTS: returns global singleton instance
    public static SimulatorState getInstance() {
        if (instance == null) {
            instance = new SimulatorState();
        }
        return instance;
    }

    // EFFECTS: returns the gradient descent simulation engine
    public Simulation getSimulation() {
        return simulation;
    }

    // EFFECTS: returns backing list of scalar fields for the list panel
    public List<ScalarField> getScalarFields() {
        return scalarFields;
    }

    public boolean getIsRunning() {
        return isRunning;
    }

    // MODIFIES: this
    // EFFECTS: starts or stops simulation (UI-controlled)
    public void setIsRunning(boolean val) {
        isRunning = val;
    }

    // EFFECTS: returns current simulation timescale factor
    public float getTimeScale() {
        return timeScale;
    }

    // MODIFIES: this
    // EFFECTS: updates timescale multiplier used during tick integration
    public void setTimeScale(float newTimeScale) {
        timeScale = newTimeScale;
    }

    public float getLearningRate() {
        return learningRate;
    }

    // MODIFIES: this
    // EFFECTS: creates a default scalar field so the UI has something to display
    private void initializeDefaultField() {
        try {
            ScalarField defaultField = SimulatorUtils.createScalarFieldFromExpression("x^2 + y^2");
            scalarFields.add(defaultField);
            simulation.setField(defaultField);
            simulation.setInitialPoint(0f, 0f);
        } catch (Exception e) {
            System.out.println("[DEBUG][ERROR] Failed to initialize default field: " + e.getMessage());
        }
    }

    // MODIFIES: this, simulation
    // EFFECTS: sets the learning rate and updates the simulation engine
    public float setLearningRate(float newLearningRate) {
        learningRate = newLearningRate;

        if (simulation.getField() != null) {
            simulation.setLearningRate(newLearningRate);
        }

        return learningRate;
    }

    // MODIFIES: this
    // EFFECTS: acquires state lock
    public void lock() {
        lock.lock();
    }

    // MODIFIES: this
    // EFFECTS: releases state lock
    public void unlock() {
        lock.unlock();
    }

    // MODIFIES: this, simulation
    // EFFECTS: advances simulation by real time → sim time, whenever running
    @Override
    public void tick() {
        long now = System.nanoTime();
        float deltaTimeSeconds = (now - lastTickNanoseconds) / 1_000_000_000.0f;
        deltaTimeSeconds = Math.min(deltaTimeSeconds, MAX_DELTATIME);
        lastTickNanoseconds = now;

        if (!isRunning) {
            return;
        }

        // No simulation step without a field or initial start point
        if (simulation.getField() == null || simulation.getCurrentPoint() == null) {
            isRunning = false;
            return;
        }

        lock();
        simulation.step(deltaTimeSeconds * timeScale);
        unlock();
    }
}
