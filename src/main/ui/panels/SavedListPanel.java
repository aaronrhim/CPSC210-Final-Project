package ui.panels;

import java.util.*;
import java.io.File;

import persistence.SimulationReadWriter;

// Contains all the UI elements to represent the current list of saved simulations
public class SavedListPanel extends AbstractListPanel<String> {
    private SavedEditorPanel savedEditorPanel;

    // EFFECTS: initializes itself to record the list of contents in a new list of empty strings
    public SavedListPanel() {
        super(new ArrayList<String>()); // with tick updating
    }

    // MODIFIES: this
    // EFFECTS: instantiates a savedEditorPanel and returns it
    @Override
    public SavedEditorPanel initEditorPanel() {
        savedEditorPanel = new SavedEditorPanel(this);
        return savedEditorPanel;
    }

    // MODIFIES: this, super
    // EFFECTS: updates strings of save filenames and saves to ../../data/ directory
    @SuppressWarnings("methodlength")
    @Override
    public void tick() {
        super.tick();

        File saveDir = new File(SimulationReadWriter.SAVE_PATH);
        File[] subFiles = saveDir.listFiles();

        List<String> newFileNames = new ArrayList<String>(subFiles.length);
        for (File subFile : subFiles) {
            if (subFile.isDirectory() || !subFile.getName().endsWith(SimulationReadWriter.FILE_SUFFIX)) {
                continue;
            }

            String subFileName = subFile.getName();
            subFileName = subFileName.substring(0, subFileName.lastIndexOf("."));
            newFileNames.add(subFileName);
        }

        List<String> savedSimOptions = super.getListData();

        // Note: You need remove things twice cause some things continue to exist after first pass
        List<String> toRemove = new ArrayList<String>(savedSimOptions.size());
        for (String oldFileName : savedSimOptions) {
            if (!newFileNames.contains(oldFileName)) {
                toRemove.add(oldFileName);
            }
        }
        for (String toRemoveName : toRemove) {
            savedSimOptions.remove(toRemoveName);
        }

        // add everything not already there
        for (String newFileName : newFileNames) {
            if (!savedSimOptions.contains(newFileName)) {
                savedSimOptions.add(newFileName);
            }
        }

        savedEditorPanel.tick();
    }
}