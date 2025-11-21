package ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class EditorTabPanelTest {

    @Test
    void testTabs() {
        EditorTabPanel panel = new EditorTabPanel();
        assertEquals(2, panel.getTabCount());
        assertNotNull(panel.getScalarFieldListPanel());
    }
}
