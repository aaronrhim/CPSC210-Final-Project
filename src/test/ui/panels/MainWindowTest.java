package ui.panels;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class MainWindowTest {

    @Test
    void testConstruction() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        MainWindow window = new MainWindow("Test", new Dimension(200, 200));
        window.tick();
        assertNotNull(window.getEditorTabPanel());
        window.dispose();
    }
}
