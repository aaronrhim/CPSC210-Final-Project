package ui.panels;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.File;

import persistence.SimulationReadWriter;

/**
 * Contains all the UI elements to represent the current list of saved simulations.
 */
@ExcludeFromJacocoGeneratedReport
public class SavedListPanel extends AbstractListPanel<String> {
    private SavedEditorPanel savedEditorPanel;

    // EFFECTS: initializes itself to record the list of contents in a new list of empty strings
    public SavedListPanel() {
        super(new ArrayList<String>());
    }

    // MODIFIES: this
    // EFFECTS: instantiates a savedEditorPanel and returns it
    @Override
    public SavedEditorPanel initEditorPanel() {
        savedEditorPanel = new SavedEditorPanel(this);
        return savedEditorPanel;
    }

    // MODIFIES: this
    // EFFECTS: syncs the backing list with files on disk, then updates the editor
    @Override
    public void tick() {
        reconcileWithFilesystem();
        refreshModel();
        savedEditorPanel.tick();
    }

    // MODIFIES: this
    // EFFECTS: rebuilds the backing list from save files on disk
    private void reconcileWithFilesystem() {
        Set<String> discovered = new TreeSet<>();
        File saveDir = new File(SimulationReadWriter.SAVE_PATH);
        saveDir.mkdirs();
        File[] subFiles = saveDir.listFiles();
        if (subFiles == null) {
            return;
        }
        for (File subFile : subFiles) {
            if (subFile.isFile() && subFile.getName().endsWith(SimulationReadWriter.FILE_SUFFIX)) {
                String name = stripExtension(subFile.getName());
                discovered.add(name);
            }
        }

        List<String> backing = getListData();
        backing.clear();
        backing.addAll(discovered);
    }

    // EFFECTS: removes the final extension from a filename
    private String stripExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? filename : filename.substring(0, lastDot);
    }
}
