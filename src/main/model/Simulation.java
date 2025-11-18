package model;

import java.util.*;

import model.exceptions.ArgumentOutOfBoundsException;

// Represents a gradient descent simulation on a scalar field f(x, y)
public class Simulation {
    private static final EventLog LOG = EventLog.getInstance();

    private ScalarField field;        
    private Vector2 currentPoint;      
    private List<Vector2> path;        

    private float learningRate;        
    private float stopThreshold;       
    private int maxIterations;         
    private float eps;                 

    private float timeElapsed;         

    // EFFECTS: creates an empty simulation with no field and default parameters
    public Simulation() {
        this.field = null;
        this.currentPoint = null;

        this.path = new ArrayList<>();

        this.learningRate = 0.01f;
        this.stopThreshold = 0.0001f;
        this.maxIterations = 5000;
        this.eps = 0.0f;

        this.timeElapsed = 0.0f;
    }

    public synchronized float getTimeElapsed() {
        return timeElapsed;
    }

    public synchronized float getLearningRate() { return learningRate; }
    public synchronized float getStopThreshold() { return stopThreshold; }
    public synchronized int getMaxIterations() { return maxIterations; }
    public synchronized float getEps() { return eps; }
    public synchronized void setStopThreshold(float newStopThreshold) {
        this.stopThreshold = newStopThreshold;
    }
    public synchronized void setMaxIterations(int newMaxIterations) {
        this.maxIterations = newMaxIterations;
    }
    public synchronized void setEps(float newEps) {
        this.eps = newEps;
    }

    public synchronized ScalarField getField() {
        return field;
    }

    public synchronized Vector2 getCurrentPoint() {
        return currentPoint;
    }

    public synchronized List<Vector2> getPath() {
        return new ArrayList<>(path);
    }

    // MODIFIES: this
    // EFFECTS: clears the current path and replaces it with the provided sequence.
    //          If the list is empty or null, the current point becomes null.
    public synchronized void overwritePath(List<Vector2> newPath) {
        path.clear();
        if (newPath == null || newPath.isEmpty()) {
            currentPoint = null;
            return;
        }

        for (Vector2 point : newPath) {
            path.add(new Vector2(point));
        }

        Vector2 last = path.get(path.size() - 1);
        currentPoint = new Vector2(last);
    }

    public synchronized void setTimeElapsed(float newTimeElapsed) {
        timeElapsed = Math.max(0f, newTimeElapsed);
    }

    // MODIFIES: this
    // REQUIRES: field and initial point are non-null
    // EFFECTS: performs a fixed number of gradient descent epochs without waiting for the realtime tick loop
    public synchronized void runEpochs(int epochs) {
        ensureFieldLoaded();
        ensurePointInitialized();

        int completedEpochs = 0;
        for (int i = 0; i < epochs; i++) {
            try {
                Vector2 grad = field.gradientAt(currentPoint.getX(), currentPoint.getY());
                Vector2 scaled = Vector2.multiply(grad, learningRate);
                currentPoint = Vector2.sub(currentPoint, scaled);
                path.add(new Vector2(currentPoint));
                completedEpochs++;
            } catch (RuntimeException ex) {
                logNewEvent("Manual epoch halted: " + ex.getMessage());
                break;
            }
        }

        logNewEvent("Ran " + completedEpochs + " manual epochs.");
    }

    // MODIFIES: this
    // EFFECTS: sets the scalar field to be optimized and clears existing data
    public synchronized void setField(ScalarField newField) {
        if (newField == null) {
            System.out.println("[DEBUG][ERROR] Tried to set null field in Simulation!");
            return;
        }
        this.field = newField;
        this.currentPoint = null;
        this.path.clear();

        logNewEvent("Loaded scalar field: " + newField.getName());
    }

    // MODIFIES: this
    // EFFECTS: sets the learning rate for gradient descent
    public synchronized void setLearningRate(float newLearningRate) {
        this.learningRate = newLearningRate;
    }

    // MODIFIES: this
    // REQUIRES: field is non-null
    // EFFECTS: sets the starting point for gradient descent and clears old path
    public synchronized void setInitialPoint(float x, float y) {
        ensureFieldLoaded();

        currentPoint = new Vector2(x, y);
        path.clear();
        path.add(new Vector2(currentPoint));

        logNewEvent("Set initial point to " + currentPoint.toString());
    }

    // MODIFIES: this
    // REQUIRES: field and currentPoint are non-null
    // EFFECTS: performs one gradient descent step; updates currentPoint, path, timeElapsed
    public synchronized void step(float deltaTime) {
        ensureFieldLoaded();
        ensurePointInitialized();

        Vector2 grad = field.gradientAt(currentPoint.getX(), currentPoint.getY());
        float mag = grad.magnitude();

        if (mag < stopThreshold) {
            logNewEvent("Convergence reached at " + currentPoint.toString());
            return;
        }

        Vector2 scaled = Vector2.multiply(grad, learningRate);
        currentPoint = Vector2.sub(currentPoint, scaled);

        path.add(new Vector2(currentPoint));
        timeElapsed += deltaTime;

        logNewEvent("Step taken to " + currentPoint.toString());
    }

    // MODIFIES: this
    // REQUIRES: field and initial point are non-null
    // EFFECTS: runs gradient descent until convergence or max iterations reached
    public synchronized void runUntilConverged(float deltaTime) {
        ensureFieldLoaded();
        ensurePointInitialized();

        int iterations = 0;
        while (iterations < maxIterations) {
            Vector2 grad = field.gradientAt(currentPoint.getX(), currentPoint.getY());
            if (grad.magnitude() < stopThreshold) {
                logNewEvent("Convergence reached at " + currentPoint.toString());
                break;
            }

            Vector2 scaled = Vector2.multiply(grad, learningRate);
            currentPoint = Vector2.sub(currentPoint, scaled);

            path.add(new Vector2(currentPoint));
            timeElapsed += deltaTime;

            iterations++;
        }

        if (iterations == maxIterations) {
            logNewEvent("Max iterations reached without convergence.");
        }
    }

    private void ensureFieldLoaded() {
        if (field == null) {
            throw new ArgumentOutOfBoundsException("No scalar field loaded into simulation.");
        }
    }

    private void ensurePointInitialized() {
        if (currentPoint == null) {
            throw new ArgumentOutOfBoundsException("Initial point not set for simulation.");
        }
    }

    private static void logNewEvent(String message) {
        LOG.logEvent(new Event(message));
    }
}
