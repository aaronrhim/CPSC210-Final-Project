package ui.panels;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import java.awt.*;
import javax.swing.*;
import ui.Tickable;

/**
 * Abstract list panel used to view and edit elements in a list.
 * Uses a backing {@link java.util.List} plus a {@link DefaultListModel}
 * to keep Swing and domain data loosely coupled.
 */
@ExcludeFromJacocoGeneratedReport
public abstract class AbstractListPanel<T> extends JPanel implements Tickable {
    public static final double SPLIT_WEIGHT = 0.9;

    private final java.util.List<T> backingData;
    private final DefaultListModel<T> listModel;
    protected final JList<T> swingList;
    protected final JScrollPane listScroller;
    protected final JPanel editorPanel;

    // REQUIRES: listData non-null
    // MODIFIES: this
    // EFFECTS: wires a scrolling list to the provided backing data and creates an editor panel
    public AbstractListPanel(java.util.List<T> listData) {
        super(new BorderLayout());
        backingData = listData;
        listModel = new DefaultListModel<>();
        swingList = new JList<>(listModel);
        listScroller = new JScrollPane(swingList);
        editorPanel = initEditorPanel();

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listScroller, editorPanel);
        splitter.setResizeWeight(SPLIT_WEIGHT);
        splitter.setEnabled(false);
        add(splitter, BorderLayout.CENTER);

        refreshModel();
    }

    // EFFECTS: returns the Swing list widget
    public JList<T> getSwingList() {
        return swingList;
    }

    // EFFECTS: returns the editor panel for subclasses
    public JPanel getEditorPanel() {
        return editorPanel;
    }

    // EFFECTS: returns the live backing data list
    public java.util.List<T> getListData() {
        return backingData;
    }

    // EFFECTS: expected that the user defines a means to initialize the editor panel in this method, and returns it
    protected abstract JPanel initEditorPanel();

    // MODIFIES: this
    // EFFECTS: reloads Swing list content from backing data and triggers any editor updates
    @Override
    public void tick() {
        refreshModel();
    }

    // MODIFIES: listModel
    // EFFECTS: replaces all elements in the Swing model with the backing data snapshot
    protected void refreshModel() {
        listModel.clear();
        for (T item : backingData) {
            listModel.addElement(item);
        }
    }
}
