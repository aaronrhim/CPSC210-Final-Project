package model;

/**
 * Centralized logger for model events to keep EventLog usage within the model package.
 */
public final class ModelEventLogger {
    private static final EventLog LOG = EventLog.getInstance();

    private ModelEventLogger() {
        // utility
    }

    // EFFECTS: logs that a scalar field was added to the collection
    public static void logFieldAdded(ScalarField field) {
        if (field != null) {
            LOG.logEvent(new Event("Scalar field added: " + field.getName()));
        }
    }

    // EFFECTS: logs that a scalar field was removed from the collection
    public static void logFieldRemoved(String fieldName) {
        LOG.logEvent(new Event("Scalar field removed: " + fieldName));
    }

    // EFFECTS: logs that the simulation was reset
    public static void logSimulationReset() {
        LOG.logEvent(new Event("Simulation reset to defaults."));
    }

    // EFFECTS: logs that a random start point was selected
    public static void logRandomStart(float x, float y) {
        LOG.logEvent(new Event(String.format("Simulation random start set to (%.2f, %.2f)", x, y)));
    }

    // EFFECTS: logs that the simulation was started
    public static void logSimulationStarted() {
        LOG.logEvent(new Event("Simulation started."));
    }

    // EFFECTS: logs that the simulation was stopped
    public static void logSimulationStopped() {
        LOG.logEvent(new Event("Simulation stopped."));
    }
}
