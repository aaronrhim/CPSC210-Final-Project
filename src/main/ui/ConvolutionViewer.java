package ui;

import model.Convolution;
import model.ImageUtils;
import model.KernelLibrary;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/*
 * Lightweight Swing window that shows an image before/after convolution
 */
public class ConvolutionViewer extends JFrame {
    private static final int PREVIEW_SIZE = 380;

    private final JLabel originalLabel;
    private final JLabel resultLabel;
    private final JComboBox<String> kernelSelector;
    private final JSpinner iterationSpinner;

    private BufferedImage originalImage;
    private BufferedImage convolvedImage;

    /*
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS: builds the window with image previews and control buttons
     */
    public ConvolutionViewer() {
        super("Convolution Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(900, 600));
        setLayout(new BorderLayout());

        originalLabel = new JLabel("Load an image to begin", SwingConstants.CENTER);
        resultLabel = new JLabel("Convolution result will appear here", SwingConstants.CENTER);

        add(createImagePanel(), BorderLayout.CENTER);

        kernelSelector = new JComboBox<>(KernelLibrary.getAll().keySet().toArray(new String[0]));
        iterationSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));

        add(createControlsPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS: constructs the side-by-side panels used to preview the images
     */
    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 12, 12));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        imagePanel.add(wrapWithTitle(originalLabel, "Original"));
        imagePanel.add(wrapWithTitle(resultLabel, "After convolution"));
        return imagePanel;
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS: builds the bar of buttons and controls at the bottom of the window
     */
    private JPanel createControlsPanel() {
        JButton loadButton = new JButton("Load image");
        loadButton.addActionListener(e -> openImage());

        JButton applyButton = new JButton("Apply convolution");
        applyButton.addActionListener(e -> applyConvolution());

        JButton resetButton = new JButton("Reset view");
        resetButton.addActionListener(e -> resetView());

        JPanel controls = new JPanel();
        controls.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        controls.add(loadButton);
        controls.add(applyButton);
        controls.add(resetButton);
        controls.add(new JLabel("Kernel:"));
        controls.add(kernelSelector);
        controls.add(new JLabel("Iterations:"));
        controls.add(iterationSpinner);

        return controls;
    }

    /*
     * REQUIRES: label and title are not null
     * MODIFIES: nothing
     * EFFECTS: returns a panel with a titled border wrapped around the supplied label
     */
    private JPanel wrapWithTitle(JLabel label, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS: prompts for an image file and loads it into memory
     */
    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "bmp", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                BufferedImage img = ImageUtils.readImage(file.getAbsolutePath());
                originalImage = img;
                convolvedImage = null;
                setImage(originalLabel, originalImage);
                updateResultLabel();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not read image: " + ex.getMessage(),
                        "Load error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /*
     * REQUIRES: an image has been loaded
     * MODIFIES: this
     * EFFECTS: applies the currently selected kernel to the loaded image
     */
    private void applyConvolution() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first.",
                    "No image", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double[][] kernel = KernelLibrary.getKernel((String) kernelSelector.getSelectedItem());
        int iterations = (Integer) iterationSpinner.getValue();

        double[][] matrix = ImageUtils.toGrayMatrix(originalImage);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        double[][] result = Convolution.padd2D(matrix, width, height, kernel,
                kernel[0].length, kernel.length, iterations);

        convolvedImage = ImageUtils.fromMatrix(result);
        updateResultLabel();
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS: clears any convolved preview so the original stands alone
     */
    private void resetView() {
        convolvedImage = null;
        updateResultLabel();
    }

    /*
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS: refreshes the right-hand preview with the latest state
     */
    private void updateResultLabel() {
        if (convolvedImage != null) {
            setImage(resultLabel, convolvedImage);
        } else if (originalImage != null) {
            resultLabel.setIcon(null);
            resultLabel.setText("Apply convolution to see the result");
        } else {
            resultLabel.setIcon(null);
            resultLabel.setText("Convolution result will appear here");
        }
        if (originalImage == null) {
            originalLabel.setIcon(null);
            originalLabel.setText("Load an image to begin");
        }
    }

    /*
     * REQUIRES: label and image are not null
     * MODIFIES: label state
     * EFFECTS: scales the image for display and attaches it to the label
     */
    private void setImage(JLabel label, BufferedImage image) {
        Image scaled = scaleToFit(image);
        label.setText(null);
        label.setIcon(new ImageIcon(scaled));
    }

    /*
     * REQUIRES: image is not null
     * MODIFIES: nothing
     * EFFECTS: returns an Image scaled down to fit the preview bounds while keeping aspect ratio
     */
    private Image scaleToFit(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        double scale = Math.min((double) PREVIEW_SIZE / w, (double) PREVIEW_SIZE / h);
        if (scale > 1) {
            scale = 1;
        }
        int newW = Math.max(1, (int) Math.round(w * scale));
        int newH = Math.max(1, (int) Math.round(h * scale));
        return image.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
    }
}
