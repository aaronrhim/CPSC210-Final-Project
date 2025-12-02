package ui.panels;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import ui.*;
import model.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

/**
 * JPanel editor used to input functions and modify axes with sliders.
 */
@ExcludeFromJacocoGeneratedReport
public class ScalarFieldEditorPanel extends JPanel implements ActionListener, ChangeListener, Tickable {

    private static final int DOMAIN_SLIDER_MIN = -30;
    private static final int DOMAIN_SLIDER_MAX = 30;
    private static final int DOMAIN_MIN_GAP = 1;

    private ScalarFieldListPanel parent;

    private JTextField fieldExpressionInput;
    private JButton addFieldButton;
    private JButton removeFieldButton;

    private JSlider learningRateSlider;
    private JSlider xxMinSlider;
    private JSlider xxMaxSlider;
    private JSlider yyMinSlider;
    private JSlider yyMaxSlider;
    private JSlider zzMinSlider;
    private JSlider zzMaxSlider;
    private JLabel xxBoundsSummary;
    private JLabel yyBoundsSummary;
    private JLabel zzBoundsSummary;

    private boolean updatingDomainControls;
    private ScalarField lastSelectedField;

    // REQUIRES: parent not null
    // MODIFIES: this
    // EFFECTS: builds UI controls for editing scalar fields and wires listeners
    @SuppressWarnings("methodlength")
    public ScalarFieldEditorPanel(ScalarFieldListPanel parent) {
        super(new BorderLayout());
        this.parent = parent;
        this.updatingDomainControls = false;

        JLabel editorTitleLabel = SimulatorUtils.makeTitleLabel("Edit Scalar Field");
        add(editorTitleLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridBagLayout());

        // Expression input field
        fieldExpressionInput = new JTextField();
        fieldExpressionInput.addActionListener(this);
        infoPanel.add(new JLabel("f(x,y) = "), SimulatorUtils.makeGbConstraints(0, 0, 1));
        infoPanel.add(fieldExpressionInput, SimulatorUtils.makeGbConstraints(1, 0, 2));

        // Add button
        addFieldButton = new JButton("Add Field");
        addFieldButton.addActionListener(this);
        infoPanel.add(addFieldButton, SimulatorUtils.makeGbConstraints(1, 3, 1));

        // Remove button
        removeFieldButton = new JButton("Remove Field");
        removeFieldButton.addActionListener(this);
        infoPanel.add(removeFieldButton, SimulatorUtils.makeGbConstraints(2, 3, 1));

        // Learning rate slider
        learningRateSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);
        learningRateSlider.addChangeListener(this);
        infoPanel.add(new JLabel("Learning Rate:"), SimulatorUtils.makeGbConstraints(0, 5, 1));
        infoPanel.add(learningRateSlider, SimulatorUtils.makeGbConstraints(1, 5, 2));

        // Domain sliders
        infoPanel.add(new JLabel("Domain Bounds"), SimulatorUtils.makeGbConstraints(0, 6, 3));

        xxMinSlider = buildDomainSlider(-10);
        xxMaxSlider = buildDomainSlider(10);
        yyMinSlider = buildDomainSlider(-10);
        yyMaxSlider = buildDomainSlider(10);
        zzMinSlider = buildDomainSlider(-10);
        zzMaxSlider = buildDomainSlider(10);

        infoPanel.add(new JLabel("X Min:"), SimulatorUtils.makeGbConstraints(0, 7, 1));
        infoPanel.add(xxMinSlider, SimulatorUtils.makeGbConstraints(1, 7, 2));

        infoPanel.add(new JLabel("X Max:"), SimulatorUtils.makeGbConstraints(0, 8, 1));
        infoPanel.add(xxMaxSlider, SimulatorUtils.makeGbConstraints(1, 8, 2));

        infoPanel.add(new JLabel("Y Min:"), SimulatorUtils.makeGbConstraints(0, 9, 1));
        infoPanel.add(yyMinSlider, SimulatorUtils.makeGbConstraints(1, 9, 2));

        infoPanel.add(new JLabel("Y Max:"), SimulatorUtils.makeGbConstraints(0, 10, 1));
        infoPanel.add(yyMaxSlider, SimulatorUtils.makeGbConstraints(1, 10, 2));

        infoPanel.add(new JLabel("Z Min:"), SimulatorUtils.makeGbConstraints(0, 11, 1));
        infoPanel.add(zzMinSlider, SimulatorUtils.makeGbConstraints(1, 11, 2));

        infoPanel.add(new JLabel("Z Max:"), SimulatorUtils.makeGbConstraints(0, 12, 1));
        infoPanel.add(zzMaxSlider, SimulatorUtils.makeGbConstraints(1, 12, 2));

        xxBoundsSummary = new JLabel("X Bounds: —");
        yyBoundsSummary = new JLabel("Y Bounds: —");
        zzBoundsSummary = new JLabel("Z Bounds: —");
        infoPanel.add(xxBoundsSummary, SimulatorUtils.makeGbConstraints(0, 13, 3));
        infoPanel.add(yyBoundsSummary, SimulatorUtils.makeGbConstraints(0, 14, 3));
        infoPanel.add(zzBoundsSummary, SimulatorUtils.makeGbConstraints(0, 15, 3));

        add(infoPanel, BorderLayout.CENTER);
    }

    // REQUIRES: event source is one of the registered controls
    // MODIFIES: SimulatorState (locks/unlocks), selected ScalarField, UI controls
    // EFFECTS: routes button/text actions to the appropriate handlers
    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        SimulatorState.getInstance().lock();

        // if (fieldExpressionInput.isFocusOwner()) {
        //     fieldExpressionInput.postActionEvent(); // This simulates pressing Enter (past debugging)
        // }

        Object src = actionEvent.getSource();

        if (src instanceof JTextField) {
            handleTextFieldSubmit((JTextField) src);
        }
        if (src instanceof JButton) {
            handleButtonPressed((JButton) src);
        }

        SimulatorState.getInstance().unlock();
    }

    // REQUIRES: fieldSrc not null and contains valid text input
    // MODIFIES: selected ScalarField, SimulatorState simulation field, parent list
    // EFFECTS: parses the expression in the text field and replaces the selected field with new one
    private void handleTextFieldSubmit(JTextField fieldSrc) {

        ScalarField selected = getSelectedField();
        if (selected == null) {
            return;
        }

        String expr = fieldSrc.getText().trim();
        if (!SimulatorUtils.checkIfValidExpression(expr)) {
            return;
        }

        ScalarField newField = SimulatorUtils.createScalarFieldFromExpression(expr);
        parent.replaceField(selected, newField);
        SimulatorState.getInstance().getSimulation().setField(newField);
    }

    // REQUIRES: buttonSrc is addFieldButton or removeFieldButton
    // MODIFIES: parent list contents, SimulatorState simulation field/initial point
    // EFFECTS: adds a new field from expression or removes the selected field
    @SuppressWarnings("methodlength")
    private void handleButtonPressed(JButton buttonSrc) {

        if (buttonSrc == addFieldButton) {
            performAddField();
        }

        if (buttonSrc == removeFieldButton) {
            performRemoveField();
        }
    }

    // MODIFIES: parent list, simulation
    // EFFECTS: creates a new field from the input text and selects it
    private void performAddField() {
        String expr = fieldExpressionInput.getText().trim();
        if (!SimulatorUtils.checkIfValidExpression(expr)) {
            return;
        }

        ScalarField newField = SimulatorUtils.createScalarFieldFromExpression(expr);
        parent.addField(newField);
        parent.getSwingList().setSelectedValue(newField, true);

        Simulation sim = SimulatorState.getInstance().getSimulation();
        sim.setField(newField);
        sim.setInitialPoint(0f, 0f);
    }

    // MODIFIES: parent list, simulation
    // EFFECTS: removes the selected field and updates selection/simulation accordingly
    private void performRemoveField() {
        ScalarField selected = getSelectedField();
        if (selected == null) {
            return;
        }

        parent.removeField(selected);
        if (SimulatorState.getInstance().getSimulation().getField() == selected) {
            SimulatorState.getInstance().getSimulation().setField(null);
        }

        int size = parent.getSwingList().getModel().getSize();
        if (size > 0) {
            parent.getSwingList().setSelectedIndex(size - 1);
        }
    }

    // REQUIRES: e source is learning rate or domain sliders
    // MODIFIES: SimulatorState learning rate or selected ScalarField domain
    // EFFECTS: updates learning rate or domain bounds based on slider movement
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == learningRateSlider) {
            float lr = learningRateSlider.getValue() / 100.0f; // normalize lr
            SimulatorState.getInstance().setLearningRate(lr);
            return;
        }

        Object src = e.getSource();
        if (src == xxMinSlider || src == xxMaxSlider || src == yyMinSlider || src == yyMaxSlider 
                        || src == zzMinSlider || src == zzMaxSlider) {
            handleDomainSliderChanged((JSlider) src);
        }
    }

    // REQUIRES: SimulatorState singleton initialized
    // MODIFIES: UI controls (enabled/disabled), domain slider positions
    // EFFECTS: syncs panel editability and slider values with current selection/state
    @Override
    public void tick() {
        SimulatorState simState = SimulatorState.getInstance();
        ScalarField selected = getSelectedField();

        handleShouldPanelsBeEditable(simState, selected);
        syncDomainControls(selected, simState);
    }

    // REQUIRES: simState not null
    // MODIFIES: fieldExpressionInput, addFieldButton, removeFieldButton
    // EFFECTS: enables or disables editing controls based on run state and selection
    private void handleShouldPanelsBeEditable(SimulatorState simState, ScalarField selected) {

        boolean isNotRunning = !simState.getIsRunning();

        fieldExpressionInput.setEditable(isNotRunning);
        addFieldButton.setEnabled(isNotRunning);
        removeFieldButton.setEnabled(selected != null && isNotRunning);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    // EFFECTS: returns the currently selected ScalarField in the list (may be null)
    private ScalarField getSelectedField() {
        return parent.getSwingList().getSelectedValue();
    }

    // REQUIRES: initialValue within slider bounds
    // MODIFIES: none
    // EFFECTS: constructs a standard domain slider with ticks and listener
    private JSlider buildDomainSlider(int initialValue) {
        JSlider slider = new JSlider(DOMAIN_SLIDER_MIN, DOMAIN_SLIDER_MAX, initialValue);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(this);
        return slider;
    }

    // REQUIRES: source is one of the domain sliders
    // MODIFIES: selected ScalarField domain, domain summary labels
    // EFFECTS: clamps/updates domain sliders and writes domain back to field
    private void handleDomainSliderChanged(JSlider source) {
        if (updatingDomainControls) {
            return;
        }

        ScalarField selected = getSelectedField();
        if (selected == null) {
            return;
        }

        ensureSliderOrdering(source);

        float xxMin = xxMinSlider.getValue();
        float xxMax = xxMaxSlider.getValue();
        float yyMin = yyMinSlider.getValue();
        float yyMax = yyMaxSlider.getValue();
        float zzMin = zzMinSlider.getValue();
        float zzMax = zzMaxSlider.getValue();

        selected.setDomain(xxMin, xxMax, yyMin, yyMax, zzMin, zzMax);
        updateDomainSummaryLabels(xxMin, xxMax, yyMin, yyMax, zzMin, zzMax);
    }

    // REQUIRES: changedSlider is a domain slider
    // MODIFIES: domain sliders
    // EFFECTS: enforces min<max ordering with a small gap on all axes
    private void ensureSliderOrdering(JSlider changedSlider) {
        if (xxMinSlider.getValue() >= xxMaxSlider.getValue()) {
            if (changedSlider == xxMinSlider) {
                xxMaxSlider.setValue(xxMinSlider.getValue() + DOMAIN_MIN_GAP);
            } else {
                xxMinSlider.setValue(xxMaxSlider.getValue() - DOMAIN_MIN_GAP);
            }
        }

        if (yyMinSlider.getValue() >= yyMaxSlider.getValue()) {
            if (changedSlider == yyMinSlider) {
                yyMaxSlider.setValue(yyMinSlider.getValue() + DOMAIN_MIN_GAP);
            } else {
                yyMinSlider.setValue(yyMaxSlider.getValue() - DOMAIN_MIN_GAP);
            }
        }

        if (zzMinSlider.getValue() >= zzMaxSlider.getValue()) {
            if (changedSlider == zzMinSlider) {
                zzMaxSlider.setValue(zzMinSlider.getValue() + DOMAIN_MIN_GAP);
            } else {
                zzMinSlider.setValue(zzMaxSlider.getValue() - DOMAIN_MIN_GAP);
            }
        }
    }

    // REQUIRES: simState not null
    // MODIFIES: domain sliders enabled state, labels, lastSelectedField
    // EFFECTS: disables sliders when no selection or running; syncs sliders to selected field once
    private void syncDomainControls(ScalarField selected, SimulatorState simState) {
        boolean hasSelection = (selected != null);
        boolean canEdit = hasSelection && !simState.getIsRunning();

        xxMinSlider.setEnabled(canEdit);
        xxMaxSlider.setEnabled(canEdit);
        yyMinSlider.setEnabled(canEdit);
        yyMaxSlider.setEnabled(canEdit);
        zzMinSlider.setEnabled(canEdit);
        zzMaxSlider.setEnabled(canEdit);

        if (!hasSelection) {
            updateDomainSummaryLabels(null, null, null, null, null, null);
            lastSelectedField = null;
            return;
        }

        if (selected != lastSelectedField) {
            setSlidersFromField(selected);
            lastSelectedField = selected;
        }
    }

    // REQUIRES: field not null
    // MODIFIES: domain sliders, summary labels
    // EFFECTS: sets slider positions from the field's domain values
    private void setSlidersFromField(ScalarField field) {
        updatingDomainControls = true;
        xxMinSlider.setValue(clampToSliderRange(field.getXMin()));
        xxMaxSlider.setValue(clampToSliderRange(field.getXMax()));
        yyMinSlider.setValue(clampToSliderRange(field.getYMin()));
        yyMaxSlider.setValue(clampToSliderRange(field.getYMax()));
        zzMinSlider.setValue(clampToSliderRange(field.getZMin()));
        zzMaxSlider.setValue(clampToSliderRange(field.getZMax()));
        updatingDomainControls = false;
        updateDomainSummaryLabels(field.getXMin(), field.getXMax(),
                field.getYMin(), field.getYMax(), field.getZMin(), field.getZMax());
    }

    // REQUIRES: value is finite
    // EFFECTS: returns value clamped to slider bounds, rounded to int
    private int clampToSliderRange(float value) {
        int rounded = Math.round(value);
        if (rounded < DOMAIN_SLIDER_MIN) {
            return DOMAIN_SLIDER_MIN;
        }
        if (rounded > DOMAIN_SLIDER_MAX) {
            return DOMAIN_SLIDER_MAX;
        }
        return rounded;
    }

    // EFFECTS: updates bounds summary labels with provided domain values; shows dash when null
    private void updateDomainSummaryLabels(Float xxMin, Float xxMax, Float yyMin, Float yyMax, 
                                           Float zzMin, Float zzMax) {
        if (xxMin == null || xxMax == null) {
            xxBoundsSummary.setText("X Bounds: —");
        } else {
            xxBoundsSummary.setText(String.format("X Bounds: %.1f to %.1f", xxMin, xxMax));
        }

        if (yyMin == null || yyMax == null) {
            yyBoundsSummary.setText("Y Bounds: —");
        } else {
            yyBoundsSummary.setText(String.format("Y Bounds: %.1f to %.1f", yyMin, yyMax));
        }

        if (zzMin == null || zzMax == null) {
            zzBoundsSummary.setText("Z Bounds: —");
        } else {
            zzBoundsSummary.setText(String.format("Z Bounds: %.1f to %.1f", zzMin, zzMax));
        }
    }

}
