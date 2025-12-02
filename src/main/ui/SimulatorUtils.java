package ui;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
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

/**
 * Miscellaneous parsing, math, and UI methods for the gradient-descent GUI.
 */
@ExcludeFromJacocoGeneratedReport
public class SimulatorUtils {

    private static final int EDIT_FIELD_COLUMNS = 20;
    private static final String IMAGE_PATH = "./data/image/";
    private static final Random RANDOM = new Random();

    // EFFECTS: loads an image by name from the data/image directory; throws IllegalStateException on failure
    public static BufferedImage loadImage(String imgName) {
        try {
            return ImageIO.read(new File(IMAGE_PATH + imgName));
        } catch (IOException err) {
            throw new IllegalStateException();
        }
    }

    // EFFECTS: creates a centered bold title label containing message
    public static JLabel makeTitleLabel(String message) {
        JLabel title = new JLabel(message);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setVerticalAlignment(JLabel.CENTER);
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 15));
        return title;
    }

    // EFFECTS: constructs GridBagConstraints for a component positioned at (gx, gy) with given width
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

    // REQUIRES: parent non-null; title non-null
    // MODIFIES: parent
    // EFFECTS: creates and adds a labeled text field row to the parent panel and returns the field
    public static JTextField initAndAddPropertyEditField(JPanel parent,
                                                         ActionListener listener,
                                                         String title,
                                                         int row) {
        JLabel label = new JLabel(title, JLabel.RIGHT);
        JTextField textField = new JTextField(EDIT_FIELD_COLUMNS);
        label.setLabelFor(textField);

        parent.add(label, makeGbConstraints(0, row, 1));
        if (listener != null) {
            textField.addActionListener(listener);
        }
        parent.add(textField, makeGbConstraints(1, row, 2));
        return textField;
    }

    // EFFECTS: returns true iff the provided name is non-null, non-empty, and not starting with a space
    public static boolean checkIfValidName(String str) {
        return (str != null && !str.isEmpty() && str.charAt(0) != ' ');
    }

    // EFFECTS: attempts to parse a float from str; returns null if parsing fails
    public static Float tryParseFloat(String str) {
        try {
            return Float.parseFloat(str.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // EFFECTS: returns true if expr is non-null/non-empty and contains an x or y variable
    public static boolean checkIfValidExpression(String expr) {
        if (expr == null || expr.isEmpty()) {
            return false;
        }
        return (expr.contains("x") || expr.contains("y"));
    }

    // EFFECTS: returns the stored expression placeholder for a scalar field (stub for persistence)
    public static String getStoredExpressionForField(ScalarField field) {
        return "<expr>";
    }

    // REQUIRES: expr valid per checkIfValidExpression
    // EFFECTS: builds a ScalarField from the given expression string using exp4j
    public static ScalarField createScalarFieldFromExpression(String expr) {
        if (!checkIfValidExpression(expr)) {
            throw new IllegalArgumentException("Invalid scalar field expression: " + expr);
        }

        return new ScalarField(expr, buildEvaluator(expr));
    }

    // REQUIRES: src and dst non-null
    // MODIFIES: dst
    // EFFECTS: copies all simulation data from src into dst, preserving paths/parameters where possible
    public static void transferSimData(Simulation src, Simulation dst) {
        synchronized (src) {
            copyField(src, dst);
            copyTrajectory(src, dst);
            copyParameters(src, dst);
        }
    }

    // REQUIRES: max > min
    // EFFECTS: returns a random float in [min, max)
    public static float randomFloatInRange(float min, float max) {
        return min + RANDOM.nextFloat() * (max - min);
    }

    // EFFECTS: returns gaussian-distributed random float using provided mean/deviation
    public static float randomFloatGaussian(float mean, float deviation) {
        return mean + deviation * (float) RANDOM.nextGaussian();
    }

    // EFFECTS: builds a bi-function evaluator for the given expression
    private static BiFunction<Float, Float, Float> buildEvaluator(String expr) {
        return (Float x, Float y) -> {
            try {
                Expression expression = new ExpressionBuilder(expr)
                        .variables("x", "y")
                        .build()
                        .setVariable("x", x)
                        .setVariable("y", y);
                return (float) expression.evaluate();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to evaluate expression: " + expr, ex);
            }
        };
    }

    // MODIFIES: dst
    // EFFECTS: copies the scalar field reference from src if present
    private static void copyField(Simulation src, Simulation dst) {
        ScalarField f = src.getField();
        if (f != null) {
            dst.setField(f);
        }
    }

    // MODIFIES: dst
    // EFFECTS: copies path or initial point from src into dst
    private static void copyTrajectory(Simulation src, Simulation dst) {
        java.util.List<Vector2> srcPath = src.getPath();
        if (!srcPath.isEmpty()) {
            dst.overwritePath(srcPath);
            System.out.println("[DEBUG] Transferred path with " + srcPath.size() + " points.");
            return;
        }
        Vector2 cp = src.getCurrentPoint();
        if (cp != null) {
            dst.setInitialPoint(cp.getX(), cp.getY());
            System.out.println("[DEBUG] Transferred current point: " + cp.toString());
        }
    }

    // MODIFIES: dst
    // EFFECTS: copies simulation numeric parameters from src to dst
    private static void copyParameters(Simulation src, Simulation dst) {
        dst.setLearningRate(src.getLearningRate());
        dst.setStopThreshold(src.getStopThreshold());
        dst.setMaxIterations(src.getMaxIterations());
        dst.setEps(src.getEps());
        dst.setTimeElapsed(src.getTimeElapsed());
    }
}
