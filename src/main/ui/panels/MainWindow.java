package ui.panels;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import java.awt.*;
import java.awt.event.*;

import ui.SimulatorUtils;
import ui.Tickable;
import javax.swing.*;

import model.Event;
import model.EventLog;

/**
 * MainWindow JFrame which contains all other UI elements.
 */
@ExcludeFromJacocoGeneratedReport
public class MainWindow extends JFrame implements Tickable {
    public static final double SPLIT_WEIGHT = 0.01f;

    private final EditorTabPanel editorTabPanel;
    private final ViewportPanel3D viewportPanel;

    // REQUIRES: title and size non-null
    // MODIFIES: this
    // EFFECTS: initializes all window parameters, the editorTab and the 3D viewport
    public MainWindow(String title, Dimension size) {
        super(title);
        setLayout(new BorderLayout());
        setPreferredSize(size);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(SimulatorUtils.loadImage("icon.png"));
        addWindowListener(new WindowLogger());

        editorTabPanel = new EditorTabPanel();
        viewportPanel = new ViewportPanel3D();

        add(buildSplitPane(), BorderLayout.CENTER);
        finalizeWindow();
    }

    // EFFECTS: returns the editor tab panel
    public EditorTabPanel getEditorTabPanel() {
        return editorTabPanel;
    }

    // EFFECTS: returns the 3D viewport panel
    public ViewportPanel3D getViewportPanel() {
        return viewportPanel;
    }

    // MODIFIES: this
    // EFFECTS: updates the editorTab and the 3D viewport
    @Override
    public void tick() {
        editorTabPanel.tick();
        viewportPanel.tick();
    }

    // MODIFIES: this
    // EFFECTS: constructs a split pane between editor and viewport
    private JSplitPane buildSplitPane() {
        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorTabPanel, viewportPanel);
        splitter.setResizeWeight(SPLIT_WEIGHT);
        splitter.setEnabled(false);
        return splitter;
    }

    // MODIFIES: this
    // EFFECTS: packs and shows window
    private void finalizeWindow() {
        pack();
        setResizable(false);
        setVisible(true);
    }

    // Logs event history on close in a dedicated listener
    private static class WindowLogger extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent windowEvent) {
            System.out.println("EVENT LOG:");
            for (Event event : EventLog.getInstance()) {
                System.out.println(event.getDescription());
            }
        }
    }
}
