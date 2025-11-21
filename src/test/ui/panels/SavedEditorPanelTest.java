package ui.panels;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class SavedEditorPanelTest {

    @Test
    void testTick() {
        SavedListPanel parent = new SavedListPanel();
        SavedEditorPanel panel = new SavedEditorPanel(parent);
        panel.tick();
        assertNotNull(panel);
    }
}
