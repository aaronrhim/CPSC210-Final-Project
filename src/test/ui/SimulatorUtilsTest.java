package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.ScalarField;
import model.Simulation;
import org.junit.jupiter.api.Test;

public class SimulatorUtilsTest {

    @Test
    void testValidationHelpers() {
        assertTrue(SimulatorUtils.checkIfValidName("name"));
        assertFalse(SimulatorUtils.checkIfValidName(" name"));
        assertEquals(3.5f, SimulatorUtils.tryParseFloat("3.5"));
        assertTrue(SimulatorUtils.checkIfValidExpression("x+1"));
        assertFalse(SimulatorUtils.checkIfValidExpression(""));
    }

    @Test
    void testCreateScalarFieldFromExpression() {
        ScalarField field = SimulatorUtils.createScalarFieldFromExpression("x + y");
        assertEquals(5f, field.evaluate(2f, 3f));
    }

    @Test
    void testTransferSimData() {
        ScalarField field = new ScalarField("x^2", (x, y) -> x * x);
        Simulation src = new Simulation();
        src.setField(field);
        src.setInitialPoint(1f, 0f);
        src.runEpochs(2);
        src.setLearningRate(0.2f);
        src.setStopThreshold(0.3f);
        src.setMaxIterations(4);
        src.setEps(0.1f);
        src.setTimeElapsed(2f);

        Simulation dst = new Simulation();
        SimulatorUtils.transferSimData(src, dst);

        assertEquals(field, dst.getField());
        assertEquals(src.getPath().size(), dst.getPath().size());
        assertNotSame(src.getPath(), dst.getPath());
        assertEquals(0.2f, dst.getLearningRate());
        assertEquals(0.3f, dst.getStopThreshold());
        assertEquals(4, dst.getMaxIterations());
        assertEquals(0.1f, dst.getEps());
        assertEquals(2f, dst.getTimeElapsed());
    }

    @Test
    void testRandomFloatInRange() {
        float val = SimulatorUtils.randomFloatInRange(-1f, 1f);
        assertTrue(val >= -1f && val <= 1f);
        float gauss = SimulatorUtils.randomFloatGaussian(0f, 2f);
        assertFalse(Float.isNaN(gauss));
    }
}
