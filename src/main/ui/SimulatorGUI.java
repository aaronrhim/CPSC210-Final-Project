package ui;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import model.*;
import ui.panels.*;
import java.awt.*;

/**
 * Contains all the rendering-related data for the Swing-based GUI.
 */
@ExcludeFromJacocoGeneratedReport
public class SimulatorGUI implements Tickable {
    private static final String WINDOW_TITLE = "Gradient Descent Visualizer";
    private static final Dimension WINDOW_DIMENSION = new Dimension(1000, 700);

    private final MainWindow mainWindow;

    // EFFECTS: initializes the main window
    private SimulatorGUI() {
        System.out.println("[DEBUG] SimulatorGUI constructor called");
        mainWindow = new MainWindow(WINDOW_TITLE, WINDOW_DIMENSION);
    }

    // EFFECTS: returns the singleton instance, constructing it on first access
    public static SimulatorGUI getInstance() {
        return Holder.INSTANCE;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    // MODIFIES: this
    // EFFECTS: updates self and all relevant sub-components
    @Override
    public void tick() {
        mainWindow.tick();
    }

    // EFFECTS: returns the currently selected scalar field in the editor
    public ScalarField getSelectedField() {
        return mainWindow
                .getEditorTabPanel()
                .getScalarFieldListPanel()
                .getSelectedField();
    }

    // Lazy-loaded holder to avoid double-checked locking
    private static class Holder {
        private static final SimulatorGUI INSTANCE = new SimulatorGUI();
    }
}
