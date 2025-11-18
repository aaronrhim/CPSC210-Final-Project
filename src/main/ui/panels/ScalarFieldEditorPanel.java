package ui.panels;

import ui.*;
import model.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

public class ScalarFieldEditorPanel extends JPanel implements ActionListener, ChangeListener, Tickable {

    private static final int DOMAIN_SLIDER_MIN = -30;
    private static final int DOMAIN_SLIDER_MAX = 30;
    private static final int DOMAIN_MIN_GAP = 1;

    private ScalarFieldListPanel parent;

    private JTextField fieldExpressionInput;
    private JButton addFieldButton;
    private JButton removeFieldButton;

    private JSlider learningRateSlider;
    private JSlider xMinSlider;
    private JSlider xMaxSlider;
    private JSlider yMinSlider;
    private JSlider yMaxSlider;
    private JSlider zMinSlider;
    private JSlider zMaxSlider;
    private JLabel xBoundsSummary;
    private JLabel yBoundsSummary;
    private JLabel zBoundsSummary;

    private boolean updatingDomainControls;
    private ScalarField lastSelectedField;

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

        xMinSlider = buildDomainSlider(-10);
        xMaxSlider = buildDomainSlider(10);
        yMinSlider = buildDomainSlider(-10);
        yMaxSlider = buildDomainSlider(10);
        zMinSlider = buildDomainSlider(-10);
        zMaxSlider = buildDomainSlider(10);

        infoPanel.add(new JLabel("X Min:"), SimulatorUtils.makeGbConstraints(0, 7, 1));
        infoPanel.add(xMinSlider, SimulatorUtils.makeGbConstraints(1, 7, 2));

        infoPanel.add(new JLabel("X Max:"), SimulatorUtils.makeGbConstraints(0, 8, 1));
        infoPanel.add(xMaxSlider, SimulatorUtils.makeGbConstraints(1, 8, 2));

        infoPanel.add(new JLabel("Y Min:"), SimulatorUtils.makeGbConstraints(0, 9, 1));
        infoPanel.add(yMinSlider, SimulatorUtils.makeGbConstraints(1, 9, 2));

        infoPanel.add(new JLabel("Y Max:"), SimulatorUtils.makeGbConstraints(0, 10, 1));
        infoPanel.add(yMaxSlider, SimulatorUtils.makeGbConstraints(1, 10, 2));

        infoPanel.add(new JLabel("Z Min:"), SimulatorUtils.makeGbConstraints(0, 11, 1));
        infoPanel.add(zMinSlider, SimulatorUtils.makeGbConstraints(1, 11, 2));

        infoPanel.add(new JLabel("Z Max:"), SimulatorUtils.makeGbConstraints(0, 12, 1));
        infoPanel.add(zMaxSlider, SimulatorUtils.makeGbConstraints(1, 12, 2));

        xBoundsSummary = new JLabel("X Bounds: —");
        yBoundsSummary = new JLabel("Y Bounds: —");
        zBoundsSummary = new JLabel("Z Bounds: —");
        infoPanel.add(xBoundsSummary, SimulatorUtils.makeGbConstraints(0, 13, 3));
        infoPanel.add(yBoundsSummary, SimulatorUtils.makeGbConstraints(0, 14, 3));
        infoPanel.add(zBoundsSummary, SimulatorUtils.makeGbConstraints(0, 15, 3));

        add(infoPanel, BorderLayout.CENTER);
    }

    // action events
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

    // When user presses Enter inside the expression box
    private void handleTextFieldSubmit(JTextField fieldSrc) {

        ScalarField selected = getSelectedField();
        if (selected == null) return;

        String expr = fieldSrc.getText().trim();
        if (!SimulatorUtils.checkIfValidExpression(expr)) return;

        System.out.println("[DEBUG] Parsing expression...");
        ScalarField newField = null;
        try {
            newField = SimulatorUtils.createScalarFieldFromExpression(expr);
        } catch (Exception ex) {
            System.out.println("[DEBUG][ERROR] Failed to create scalar field: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
        System.out.println("[DEBUG] ScalarField created successfully.");

        parent.replaceField(selected, newField);
        SimulatorState.getInstance().getSimulation().setField(newField);
    }

    // Add/remove field buttons
    private void handleButtonPressed(JButton buttonSrc) {

        if (buttonSrc == addFieldButton) {

            // debugging
            System.out.println("[DEBUG] Add Field button clicked.");

            String expr = fieldExpressionInput.getText().trim();
            System.out.println("[DEBUG] User typed expression: '" + expr + "'");

            if (!SimulatorUtils.checkIfValidExpression(expr)) return;

            ScalarField newField = SimulatorUtils.createScalarFieldFromExpression(expr);

            // debugging
            parent.addField(newField);
            System.out.println("[DEBUG] Field added to parent list. Total fields now: "
                    + parent.getListData().size());

            parent.getSwingList().setSelectedValue(newField, true);

            Simulation sim = SimulatorState.getInstance().getSimulation();

            // Set field
            sim.setField(newField);

            // IMPORTANT FIX: automatically choose a sensible initial point
            sim.setInitialPoint(0f, 0f);   // or any center point of your domain
        }

        if (buttonSrc == removeFieldButton) {

            ScalarField selected = getSelectedField();
            if (selected == null) return;

            parent.removeField(selected);

            if (SimulatorState.getInstance().getSimulation().getField() == selected) {
                SimulatorState.getInstance().getSimulation().setField(null);
            }

            int size = parent.getSwingList().getModel().getSize();
            if (size > 0)
                parent.getSwingList().setSelectedIndex(size - 1);
        }
    }

    // sliders
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == learningRateSlider) {
            float lr = learningRateSlider.getValue() / 100.0f; // normalize lr
            SimulatorState.getInstance().setLearningRate(lr);
            return;
        }

        Object src = e.getSource();
        if (src == xMinSlider || src == xMaxSlider || 
            src == yMinSlider || src == yMaxSlider ||
            src == zMinSlider || src == zMaxSlider) {
            handleDomainSliderChanged((JSlider) src);
        }
    }

    @Override
    public void tick() {
        SimulatorState simState = SimulatorState.getInstance();
        ScalarField selected = getSelectedField();

        handleShouldPanelsBeEditable(simState, selected);
        syncDomainControls(selected, simState);
    }

    // Enable/disable editing correctly
    private void handleShouldPanelsBeEditable(SimulatorState simState, ScalarField selected) {

        boolean isNotRunning = !simState.getIsRunning();

        fieldExpressionInput.setEditable(isNotRunning);
        addFieldButton.setEnabled(isNotRunning);
        removeFieldButton.setEnabled(selected != null && isNotRunning);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private ScalarField getSelectedField() {
        return parent.getSwingList().getSelectedValue();
    }

    private JSlider buildDomainSlider(int initialValue) {
        JSlider slider = new JSlider(DOMAIN_SLIDER_MIN, DOMAIN_SLIDER_MAX, initialValue);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(this);
        return slider;
    }

    private void handleDomainSliderChanged(JSlider source) {
        if (updatingDomainControls) {
            return;
        }

        ScalarField selected = getSelectedField();
        if (selected == null) {
            return;
        }

        ensureSliderOrdering(source);

        float xMin = xMinSlider.getValue();
        float xMax = xMaxSlider.getValue();
        float yMin = yMinSlider.getValue();
        float yMax = yMaxSlider.getValue();
        float zMin = zMinSlider.getValue();
        float zMax = zMaxSlider.getValue();

        selected.setDomain(xMin, xMax, yMin, yMax, zMin, zMax);
        updateDomainSummaryLabels(xMin, xMax, yMin, yMax, zMin, zMax);
    }

    private void ensureSliderOrdering(JSlider changedSlider) {
        if (xMinSlider.getValue() >= xMaxSlider.getValue()) {
            if (changedSlider == xMinSlider) {
                xMaxSlider.setValue(xMinSlider.getValue() + DOMAIN_MIN_GAP);
            } else {
                xMinSlider.setValue(xMaxSlider.getValue() - DOMAIN_MIN_GAP);
            }
        }

        if (yMinSlider.getValue() >= yMaxSlider.getValue()) {
            if (changedSlider == yMinSlider) {
                yMaxSlider.setValue(yMinSlider.getValue() + DOMAIN_MIN_GAP);
            } else {
                yMinSlider.setValue(yMaxSlider.getValue() - DOMAIN_MIN_GAP);
            }
        }

        if (zMinSlider.getValue() >= zMaxSlider.getValue()) {
            if (changedSlider == zMinSlider) {
                zMaxSlider.setValue(zMinSlider.getValue() + DOMAIN_MIN_GAP);
            } else {
                zMinSlider.setValue(zMaxSlider.getValue() - DOMAIN_MIN_GAP);
            }
        }
    }

    private void syncDomainControls(ScalarField selected, SimulatorState simState) {
        boolean hasSelection = (selected != null);
        boolean canEdit = hasSelection && !simState.getIsRunning();

        xMinSlider.setEnabled(canEdit);
        xMaxSlider.setEnabled(canEdit);
        yMinSlider.setEnabled(canEdit);
        yMaxSlider.setEnabled(canEdit);
        zMinSlider.setEnabled(canEdit);
        zMaxSlider.setEnabled(canEdit);

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

    private void setSlidersFromField(ScalarField field) {
        updatingDomainControls = true;
        xMinSlider.setValue(clampToSliderRange(field.getXMin()));
        xMaxSlider.setValue(clampToSliderRange(field.getXMax()));
        yMinSlider.setValue(clampToSliderRange(field.getYMin()));
        yMaxSlider.setValue(clampToSliderRange(field.getYMax()));
        zMinSlider.setValue(clampToSliderRange(field.getZMin()));
        zMaxSlider.setValue(clampToSliderRange(field.getZMax()));
        updatingDomainControls = false;
        updateDomainSummaryLabels(field.getXMin(), field.getXMax(),
                field.getYMin(), field.getYMax(), field.getZMin(), field.getZMax());
    }

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

    private void updateDomainSummaryLabels(Float xMin, Float xMax, Float yMin, Float yMax, Float zMin, Float zMax) {
        if (xMin == null || xMax == null) {
            xBoundsSummary.setText("X Bounds: —");
        } else {
            xBoundsSummary.setText(String.format("X Bounds: %.1f to %.1f", xMin, xMax));
        }

        if (yMin == null || yMax == null) {
            yBoundsSummary.setText("Y Bounds: —");
        } else {
            yBoundsSummary.setText(String.format("Y Bounds: %.1f to %.1f", yMin, yMax));
        }

        if (zMin == null || zMax == null) {
            zBoundsSummary.setText("Z Bounds: —");
        } else {
            zBoundsSummary.setText(String.format("Z Bounds: %.1f to %.1f", zMin, zMax));
        }
    }

}
