package ui.panels;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import persistence.SimulationReadWriter;
import ui.SimulatorState;
import ui.SimulatorGUI;
import ui.SimulatorUtils;
import ui.Tickable;

import java.awt.*;
import java.awt.List;
import java.awt.event.*;

import javax.swing.*;

import org.junit.Ignore;

import model.ScalarField;
import model.Simulation;

import java.util.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * JPanel container for the secondary saved editor panel.
 */
@ExcludeFromJacocoGeneratedReport
public class SavedEditorPanel extends JPanel implements ActionListener, Tickable {
    private SavedListPanel parent;
    private JTextField renameField;
    private JButton loadButton;
    private JButton saveButton;
    private JButton newButton;
    private JButton deleteButton;

    // REQUIRES: parent non-null
    // MODIFIES: this
    // EFFECTS: initializes all UI elements (position, grids, buttons, fields etc)
    public SavedEditorPanel(SavedListPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        JLabel editorTitleLabel = SimulatorUtils.makeTitleLabel("Edit Save");
        add(editorTitleLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());

        // need a default to prevent null pointer exceptions and breaking the UI
        renameField = SimulatorUtils.initAndAddPropertyEditField(infoPanel, this, "Save Name:", 0);

        loadButton = new JButton("Load");
        loadButton.addActionListener(this);
        infoPanel.add(loadButton, SimulatorUtils.makeGbConstraints(1, 1, 1));

        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        infoPanel.add(saveButton, SimulatorUtils.makeGbConstraints(2, 1, 1));

        newButton = new JButton("Create New Save");
        newButton.addActionListener(this);
        infoPanel.add(newButton, SimulatorUtils.makeGbConstraints(1, 2, 2));

        deleteButton = new JButton("Delete Save");
        deleteButton.addActionListener(this);
        infoPanel.add(deleteButton, SimulatorUtils.makeGbConstraints(1, 3, 2));

        add(infoPanel, BorderLayout.CENTER);
    }

    // MODIFIES: this
    // EFFECTS: routes text and button events to handlers
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == renameField) {
            handleRenameSave();
        }
        if (actionEvent.getSource() instanceof JButton) {
            try {
                handleButtonPressed((JButton) actionEvent.getSource());
            } catch (FileNotFoundException e) {
                // stubbery
            }
        }
    }

    // MODIFIES: SimulatorState and filesystem
    // EFFECTS: executes the button's action while the simulation lock is held
    @SuppressWarnings("methodlength")
    private void handleButtonPressed(JButton source) throws FileNotFoundException {
        SimulatorState simState = SimulatorState.getInstance();
        String selectedSaveName = parent.swingList.getSelectedValue();

        simState.lock(); // mutex lock to prevent concurrent modification issues

        if (source == loadButton) {
            simState.setIsRunning(false);
            Simulation loadedSim = SimulationReadWriter.readSimulation(selectedSaveName);
            SimulatorUtils.transferSimData(loadedSim, simState.getSimulation());
            ScalarField loadedField = simState.getSimulation().getField();
            if (loadedField != null) {
                java.awt.List<ScalarField> fields = simState.getScalarFields();
                fields.clear();
                fields.add(loadedField);
                SimulatorGUI.getInstance()
                        .getMainWindow()
                        .getEditorTabPanel()
                        .getScalarFieldListPanel()
                        .selectField(loadedField);
            }
        }

        if (source == saveButton) {
            handleSaveSimulation(simState, selectedSaveName);
        }

        if (source == deleteButton) {
            File toDeleteFile = SimulationReadWriter.fileFromFileTitle(selectedSaveName);
            toDeleteFile.delete();
        }

        if (source == newButton) {
            DateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmmssSS");
            String newSimName = "Sim_" + dateFormat.format(new Date());
            handleSaveSimulation(simState, newSimName);
        }

        simState.unlock();
    }

    // MODIFIES: filesystem, simState
    // EFFECTS: ensure the simulation is paused while writing it to the specified file location
    private void handleSaveSimulation(SimulatorState simState, String fileDest) {
        boolean wasRunning = simState.getIsRunning();
        simState.setIsRunning(false);

        try {
            SimulationReadWriter.writeSimulation(simState.getSimulation(), fileDest);
        } catch (Exception exp) { 
            // stub
        }

        simState.setIsRunning(wasRunning);
    }

    // MODIFIES: filesystem
    // EFFECTS: handles renaming the simulation if valid and non-duplicate
    private void handleRenameSave() {
        System.out.println("reached");
        String selectedSaveName = parent.swingList.getSelectedValue();
        String newSaveName = renameField.getText();

        if (!SimulatorUtils.checkIfValidName(newSaveName)) {
            return;
        }
        if (parent.swingList.getSelectedValuesList().contains(newSaveName)) {
            return;
        }

        File renamedFile = SimulationReadWriter.fileFromFileTitle(newSaveName);
        File oldFile = SimulationReadWriter.fileFromFileTitle(selectedSaveName);
        oldFile.renameTo(renamedFile);
    }

    // MODIFIES: this
    // EFFECTS: updates self and all sub-components
    @Override
    public void tick() {
        String selectedSaveName = parent.swingList.getSelectedValue();
        handleShouldPanelsBeEditable(selectedSaveName);
        handleRenameFieldText(selectedSaveName);
    }

    // MODIFIES: this
    // EFFECTS: handles the text in the rename filed
    private void handleRenameFieldText(String selectedSave) {
        if (selectedSave == null) {
            renameField.setText("");
        } else {
            if (!renameField.hasFocus()) {
                renameField.setText(selectedSave);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: handles setting which buttons/fields can be edited
    private void handleShouldPanelsBeEditable(String selectedSave) {
        boolean hasSelected = (selectedSave != null);
        renameField.setEditable(hasSelected);
        loadButton.setEnabled(hasSelected);
        saveButton.setEnabled(hasSelected);
        deleteButton.setEnabled(hasSelected);
    }
}
