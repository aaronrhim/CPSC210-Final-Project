package ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class SimulatorGUITest {

    @Test
    void testGetInstance() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        SimulatorGUI gui = SimulatorGUI.getInstance();
        assertSame(gui, SimulatorGUI.getInstance());
        assertNotNull(gui.getMainWindow());
    }
}
