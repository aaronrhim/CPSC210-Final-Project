package ui.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RenderEngine3DTest {

    @BeforeAll
    static void enableHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void testTickAndDraw() {
        JPanel panel = new JPanel();
        RenderEngine3D engine = new RenderEngine3D(panel, 64);
        engine.tick();

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        engine.drawCurrentFrame(g);
        g.dispose();

        assertEquals(panel, engine.getPanel());
        assertNotNull(img);
    }
}
