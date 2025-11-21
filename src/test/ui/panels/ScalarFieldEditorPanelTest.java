package ui.panels;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ScalarFieldEditorPanelTest {

    @Test
    void testTick() {
        ScalarFieldListPanel parent = new ScalarFieldListPanel();
        ScalarFieldEditorPanel panel = new ScalarFieldEditorPanel(parent);
        panel.tick();
        assertNotNull(panel);
    }
}
