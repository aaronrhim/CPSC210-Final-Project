package ui.panels;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class SavedListPanelTest {

    @Test
    void testTick() {
        SavedListPanel panel = new SavedListPanel();
        panel.tick();
        assertNotNull(panel.getListData());
    }
}
