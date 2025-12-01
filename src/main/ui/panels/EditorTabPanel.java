package ui.panels;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import ui.Tickable;
import javax.swing.*;

/**
 * Left tab panel which is used to swap between saved sim states.
 */
@ExcludeFromJacocoGeneratedReport
public class EditorTabPanel extends JTabbedPane implements Tickable {
    private static final String FIELD_LIST_NAME = "Scalar Fields";
    private static final String SAVE_LIST_NAME = "Saved Simulations";

    private ScalarFieldListPanel scalarFieldListPanel;
    private SavedListPanel savedListPanel;

    // EFFECTS: initializes all editor tabs for the editor panel
    public EditorTabPanel() {
        scalarFieldListPanel = new ScalarFieldListPanel();
        savedListPanel = new SavedListPanel();

        addTab(FIELD_LIST_NAME, scalarFieldListPanel);
        addTab(SAVE_LIST_NAME, savedListPanel);

        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    // EFFECTS: returns the scalar field list panel
    public ScalarFieldListPanel getScalarFieldListPanel() {
        return scalarFieldListPanel;
    }

    // MODIFIES: this
    // EFFECTS: updates all the panels within the tab
    @Override
    public void tick() {
        scalarFieldListPanel.tick();
        savedListPanel.tick();
    }
}
