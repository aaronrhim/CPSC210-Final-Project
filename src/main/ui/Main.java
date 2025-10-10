package ui;

import javax.swing.SwingUtilities;

/*
 * Entry point that launches either the console app or the Swing viewer
 */
public class Main {

    /*
     * REQUIRES: nothing
     * MODIFIES: ui state, console
     * EFFECTS: runs the requested UI. Pass "viewer" to launch the Swing interface; otherwise the
     * console workflow starts
     */
    public static void main(String[] args) {
        if (args.length > 0 && "viewer".equalsIgnoreCase(args[0])) {
            SwingUtilities.invokeLater(() -> {
                ConvolutionViewer viewer = new ConvolutionViewer();
                viewer.setVisible(true);
            });
        } else {
            ConvolutionApp app = new ConvolutionApp();
            app.run();
        }
    }
}
