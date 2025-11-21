package ui.panels;

import model.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import ui.SimulatorState;

// Scalar field list view panel used to view and edit scalar fields
public class ScalarFieldListPanel extends AbstractListPanel<ScalarField> {
    private ScalarFieldEditorPanel editorPanel;

    // EFFECTS: constructs the list using the SimulatorState's scalar field list
    public ScalarFieldListPanel() {
        super(SimulatorState.getInstance().getScalarFields());
        swingList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    handleSelectionChanged();
                }
            }
        });

        if (!getListData().isEmpty()) {
            swingList.setSelectedIndex(0);
        }
    }

    // MODIFIES: this
    // EFFECTS: initializes and returns the editor panel for scalar fields
    @Override
    protected JPanel initEditorPanel() {
        editorPanel = new ScalarFieldEditorPanel(this);
        return editorPanel;
    }

    // MODIFIES: this
    // EFFECTS: updates itself and its editor panel
    @Override
    public void tick() {
        super.tick();
        editorPanel.tick();
    }

    // EFFECTS: returns the currently selected scalar field, or null if none selected
    public ScalarField getSelectedField() {
        return super.swingList.getSelectedValue();
    }

    // MODIFIES: this
    // EFFECTS: adds a new scalar field to the backing list
    public void addField(ScalarField field) {
        getListData().add(field);
        refresh();
    }

    // MODIFIES: this
    // EFFECTS: removes field from list if present
    public void removeField(ScalarField field) {
        getListData().remove(field);
        refresh();
    }

    // MODIFIES: this
    // EFFECTS: swaps an existing field with a new instance if found
    public void replaceField(ScalarField oldField, ScalarField newField) {
        int idx = getListData().indexOf(oldField);
        if (idx != -1) {
            getListData().set(idx, newField);
            refresh();
        }
    }

    // MODIFIES: swingList
    // EFFECTS: pushes backing list contents into JList model
    private void refresh() {
        swingList.setListData(getListData().toArray(new ScalarField[0]));
    }

    // MODIFIES: simulation
    // EFFECTS: syncs selected field with simulation engine and resets start point
    private void handleSelectionChanged() {
        ScalarField selected = getSelectedField();
        if (selected == null) {
            return;
        }

        Simulation sim = SimulatorState.getInstance().getSimulation();
        if (sim.getField() == selected) {
            return;
        }

        sim.setField(selected);
        sim.setInitialPoint(0f, 0f);
    }
}
