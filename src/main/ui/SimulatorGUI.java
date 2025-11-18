package ui;

import model.*;
import ui.panels.*;
import java.awt.*;

// Contains all the rendering-related data for the Swing-based GUI
public class SimulatorGUI implements Tickable {
    private static SimulatorGUI instance;

    private static final String WINDOW_TITLE = "Gradient Descent Visualizer";
    private static final Dimension WINDOW_DIMENSION = new Dimension(1000, 700);

    private MainWindow mainWindow;

    // EFFECTS: throws IllegalStateException if an instance already exists,
    //          initializes the main window
    private SimulatorGUI() {
        System.out.println("[DEBUG] SimulatorGUI constructor called");
        if (instance != null) {
            throw new IllegalStateException("SimulatorGUI already instantiated");
        }
        mainWindow = new MainWindow(WINDOW_TITLE, WINDOW_DIMENSION);
    }


    // EFFECTS: returns the singleton instance, constructing it if necessary
    public static SimulatorGUI getInstance() {
        if (instance == null) {
            instance = new SimulatorGUI();
        }
        return instance;
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
}
