package ui.panels;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ViewportPanel3DTest {

    @Test
    void testTick() {
        ViewportPanel3D panel = new ViewportPanel3D();
        panel.tick();
        assertNotNull(panel.getRenderEngine());
    }
}
