package ui;

// Holds the entry point 
public class Main {
    // EFFECTS: constructs a simulation state and UI, then updates them forever
    public static void main(String[] args) throws Exception {
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