package ui.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

public class CameraControllerTest {

    @Test
    void testConstructionAndTick() {
        JPanel panel = new JPanel();
        RenderEngine3D engine = new RenderEngine3D(panel, 32);
        CameraController controller = new CameraController(engine);
        controller.tick();
        assertTrue(panel.getKeyListeners().length > 0);
    }
}
