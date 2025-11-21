package ui.panels;

import ui.*;
import model.*;
import ui.engine.CameraController;
import ui.engine.RenderEngine3D;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

// Viewport panel for rendering the 3D scalar field
public class ViewportPanel3D extends JPanel implements ActionListener, Tickable {

    private static final float SPLIT_WEIGHT_TOP = 0.0f;
    private static final float SPLIT_WEIGHT_BOTTOM = 1.0f;
    private static final int VIEWPORT_RESOLUTION = 350;
    private static final int PREVIEW_EPOCHS = 20;

    private JButton startButton;
    private JButton stopButton;
    private JButton resetButton; // crashes atm
    private JButton randomPointButton;
    private JLabel timeElapsedLabel;
    private JSlider timeScaleSlider;

    private RenderEngine3D renderEngine;
    private CameraController cameraController;
    private ActualViewport viewport;

    // Holds the drawing surface which the engine paints into
    private class ActualViewport extends JPanel {
        public ActualViewport() {
            setFocusable(true);
            setRequestFocusEnabled(true);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            renderEngine.drawCurrentFrame(g);
        }
    }

    // EFFECTS: initializes the control panel and render engine
    public ViewportPanel3D() {
        System.out.println("[DEBUG] ViewportPanel3D created");
        setLayout(new BorderLayout());

        // Top panel (simulation control buttons)
        JPanel topSimControlPanel = new JPanel(new FlowLayout());

        startButton = new JButton("Start");
        startButton.addActionListener(this);
        topSimControlPanel.add(startButton);

        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        topSimControlPanel.add(stopButton);

        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        topSimControlPanel.add(resetButton);

        randomPointButton = new JButton("Random Start");
        randomPointButton.addActionListener(this);
        topSimControlPanel.add(randomPointButton);

        timeElapsedLabel = new JLabel();
        topSimControlPanel.add(timeElapsedLabel);

        // viewport
        viewport = new ActualViewport();
        renderEngine = new RenderEngine3D(viewport, VIEWPORT_RESOLUTION);
        cameraController = new CameraController(renderEngine);

        JSplitPane topSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSimControlPanel, viewport);
        topSplitter.setResizeWeight(SPLIT_WEIGHT_TOP);
        topSplitter.setEnabled(false);

        // bottom panel (timescale slider)
        JPanel bottomSimControlPanel = new JPanel(new FlowLayout());
        JLabel timeScaleLabel = new JLabel("Simulation Timescale:");
        bottomSimControlPanel.add(timeScaleLabel);

        timeScaleSlider = new JSlider(
                (int) SimulatorState.TIMESCALE_MIN,
                (int) SimulatorState.TIMESCALE_MAX,
                (int) SimulatorState.getInstance().getTimeScale());
        timeScaleSlider.setMajorTickSpacing(4);
        timeScaleSlider.setPaintTicks(true);
        timeScaleSlider.setPaintLabels(true);
        bottomSimControlPanel.add(timeScaleSlider);

        JSplitPane bottomSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplitter, bottomSimControlPanel);
        bottomSplitter.setResizeWeight(SPLIT_WEIGHT_BOTTOM);
        bottomSplitter.setEnabled(false);

        add(bottomSplitter);
    }

    public RenderEngine3D getRenderEngine() {
        return renderEngine;
    }

    // MODIFIES: this
    // EFFECTS: listens to what JButton/Field/Slider has been interacted with and proceed accordingly
    @Override
    public void actionPerformed(ActionEvent e) {
        SimulatorState.getInstance().lock();

        if (e.getSource() == startButton) {
            SimulatorState.getInstance().setIsRunning(true);
        }
        if (e.getSource() == stopButton) {
            SimulatorState.getInstance().setIsRunning(false);
        }
        if (e.getSource() == resetButton) {
            SimulatorState.getInstance().setIsRunning(false);

            Simulation sim = SimulatorState.getInstance().getSimulation();
            ScalarField selected = SimulatorGUI.getInstance().getSelectedField();

            if (selected == null) {
                System.out.println("[DEBUG][ERROR] No scalar field selected during reset. Aborting reset.");
                return;
            }

            Simulation fresh = new Simulation();
            fresh.setField(selected);
            fresh.setInitialPoint(0f, 0f);

            SimulatorUtils.transferSimData(fresh, sim);
        }

        if (e.getSource() == randomPointButton) {
            handleRandomStartPoint();
        }

        SimulatorState.getInstance().unlock();
    }

    // MODIFIES: this
    // EFFECTS: tick update is called every frame
    @Override
    public void tick() {
        updateButtonAvailability();

        SimulatorState sim = SimulatorState.getInstance();
        sim.setTimeScale(timeScaleSlider.getValue());

        timeElapsedLabel.setText(String.format("Time Elapsed: %03.3fs", sim.getSimulation().getTimeElapsed()));

        cameraController.tick();
        renderEngine.tick();

        updateViewportBorder();
        viewport.repaint();
    }

    // MODIFIES: viewport
    // EFFECTS: visually signals focus ownership on the rendered panel
    private void updateViewportBorder() {
        if (viewport.isFocusOwner()) {
            viewport.setBorder(BorderFactory.createLoweredBevelBorder());
        } else {
            viewport.setBorder(BorderFactory.createRaisedBevelBorder());
        }
    }

    // MODIFIES: start/stop buttons
    // EFFECTS: toggles button availability based on simulation state
    private void updateButtonAvailability() {
        Simulation sim = SimulatorState.getInstance().getSimulation();

        boolean hasObjects = (sim.getField() != null && sim.getCurrentPoint() != null);
        boolean isRunning = SimulatorState.getInstance().getIsRunning();

        startButton.setEnabled(hasObjects && !isRunning);
        stopButton.setEnabled(hasObjects && isRunning);
    }

    // MODIFIES: sim
    // EFFECTS: seeds simulation with a random start point and previews a few epochs
    private void handleRandomStartPoint() {
        Simulation sim = SimulatorState.getInstance().getSimulation();
        ScalarField field = sim.getField();

        if (field == null) {
            field = SimulatorGUI.getInstance().getSelectedField();
            if (field != null) {
                sim.setField(field);
            }
        }

        if (field == null) {
            System.out.println("[DEBUG][ERROR] Cannot pick random point without a scalar field.");
            return;
        }

        float randomX = SimulatorUtils.randomFloatInRange(field.getXMin(), field.getXMax());
        float randomY = SimulatorUtils.randomFloatInRange(field.getYMin(), field.getYMax());

        System.out.println("[DEBUG] see random point: " + randomX + ", " + randomY);

        sim.setInitialPoint(randomX, randomY);
        sim.runEpochs(PREVIEW_EPOCHS);
        SimulatorState.getInstance().setIsRunning(false);
    }
}
