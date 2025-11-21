package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import model.Simulation;
import org.junit.jupiter.api.Test;

public class SimulatorStateTest {

    @Test
    void testSingletonAndDefaults() {
        SimulatorState state = SimulatorState.getInstance();
        assertSame(state, SimulatorState.getInstance());
        Simulation sim = state.getSimulation();
        assertNotNull(sim.getField());
        assertNotNull(sim.getCurrentPoint());
        int before = sim.getPath().size();
        state.tick();
        assertEquals(before, sim.getPath().size());
        assertFalse(state.getIsRunning());
    }

    @Test
    void testSetLearningRate() {
        SimulatorState state = SimulatorState.getInstance();
        float original = state.getLearningRate();
        try {
            float updated = state.setLearningRate(0.3f);
            assertEquals(0.3f, updated);
            assertEquals(0.3f, state.getSimulation().getLearningRate());
        } finally {
            state.setLearningRate(original);
        }
    }
}
