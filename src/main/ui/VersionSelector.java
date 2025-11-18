package ui;

// Simplified launcher that immediately starts the modern simulator UI loop
public class VersionSelector {
    public VersionSelector() {
        SimulatorState simState = SimulatorState.getInstance();
        SimulatorGUI simGfx = SimulatorGUI.getInstance();

        new Thread(() -> {
            while (true) {
                simState.tick();
                simGfx.tick();
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
