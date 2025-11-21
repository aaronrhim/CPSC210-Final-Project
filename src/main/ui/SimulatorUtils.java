package ui;

import model.*;

import java.awt.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.util.function.BiFunction;
import java.awt.event.ActionListener;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

// Miscellaneous parsing, math, and UI methods for the gradient-descent GUI
public class SimulatorUtils {

    private static final int EDIT_FIELD_COLUMNS = 20;
    private static final String IMAGE_PATH = "./data/image/";
    private static final Random RANDOM = new Random();

    // image helpers
    public static BufferedImage loadImage(String imgName) {
        try {
            return ImageIO.read(new File(IMAGE_PATH + imgName));
        } catch (IOException err) {
            throw new IllegalStateException();
        }
    }

    public static JLabel makeTitleLabel(String message) {
        JLabel title = new JLabel(message);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setVerticalAlignment(JLabel.CENTER);
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 15));
        return title;
    }

    public static GridBagConstraints makeGbConstraints(int gx, int gy, int width) {
        GridBagConstraints gbConst = new GridBagConstraints();
        gbConst.fill = GridBagConstraints.BOTH;
        gbConst.gridx = gx;
        gbConst.gridy = gy;
        gbConst.gridwidth = width;
        gbConst.weightx = 0.5;
        gbConst.insets = new Insets(1, 5, 1, 5);
        return gbConst;
    }

    public static JTextField initAndAddPropertyEditField(JPanel parent,
                                                         ActionListener listener,
                                                         String title,
                                                         int row) {
        parent.add(new JLabel(title, JLabel.RIGHT), makeGbConstraints(0, row, 1));
        JTextField textField = new JTextField(EDIT_FIELD_COLUMNS);
        if (listener != null) {
            textField.addActionListener(listener);
        }
        parent.add(textField, makeGbConstraints(1, row, 2));
        return textField;
    }

    // valid helpers (will move to tests later)
    public static boolean checkIfValidName(String str) {
        return (str != null && !str.isEmpty() && str.charAt(0) != ' ');
    }

    public static Float tryParseFloat(String str) {
        try {
            return Float.parseFloat(str.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean checkIfValidExpression(String expr) {
        if (expr == null || expr.isEmpty()) return false;
        return (expr.contains("x") || expr.contains("y"));
    }

    // EFFECTS: returns the stored expression placeholder for a scalar field (stub for persistence)
    public static String getStoredExpressionForField(ScalarField field) {
        return "<expr>";
    }

    public static ScalarField createScalarFieldFromExpression(String expr) {
        if (!checkIfValidExpression(expr)) {
            throw new IllegalArgumentException("Invalid scalar field expression: " + expr);
        }

        BiFunction<Float, Float, Float> fn = (Float x, Float y) -> {
            try {
                Expression e = new ExpressionBuilder(expr)
                        .variables("x", "y")
                        .build()
                        .setVariable("x", x)
                        .setVariable("y", y);

                return (float) e.evaluate();

            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to evaluate expression: " + expr);
            }
        };

        return new ScalarField(expr, fn);
    }

    // MODIFIES: dst
    // EFFECTS: copies all simulation data from src into dst
    public static void transferSimData(Simulation src, Simulation dst) {
        synchronized (src) {
            // Field
            ScalarField f = src.getField();
            if (f != null) {
                dst.setField(f);
            }

            java.util.List<Vector2> srcPath = src.getPath();
            if (!srcPath.isEmpty()) {
                dst.overwritePath(srcPath);
                System.out.println("[DEBUG] Transferred path with " + srcPath.size() + " points.");
            } else {
                Vector2 cp = src.getCurrentPoint();
                if (cp != null) {
                    dst.setInitialPoint(cp.getX(), cp.getY());
                    System.out.println("[DEBUG] Transferred current point: " + cp.toString());
                }
            }

            // parameters
            dst.setLearningRate(src.getLearningRate());
            dst.setStopThreshold(src.getStopThreshold());
            dst.setMaxIterations(src.getMaxIterations());
            dst.setEps(src.getEps());
            dst.setTimeElapsed(src.getTimeElapsed());
        }
    }

    // choose random point in scalar field
    public static float randomFloatInRange(float min, float max) {
        return min + RANDOM.nextFloat() * (max - min);
    }

    // EFFECTS: returns gaussian-distributed random float using provided mean/deviation
    public static float randomFloatGaussian(float mean, float deviation) {
        return mean + deviation * (float) RANDOM.nextGaussian();
    }
}
