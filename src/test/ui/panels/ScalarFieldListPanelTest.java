package ui.panels;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ScalarFieldListPanelTest {

    @Test
    void testSelection() {
        ScalarFieldListPanel panel = new ScalarFieldListPanel();
        assertFalse(panel.getListData().isEmpty());
        assertNotNull(panel.getSelectedField());
    }
}
