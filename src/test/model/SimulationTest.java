package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import model.exceptions.ArgumentOutOfBoundsException;
import org.junit.jupiter.api.Test;

@ExcludeFromJacocoGeneratedReport
public class SimulationTest {

    private ScalarField makeField() {
        BiFunction<Float, Float, Float> fn = (x, y) -> x + y;
        return new ScalarField("linear", fn);
    }

    @Test
    void testStep() {
        Simulation sim = new Simulation();
        sim.setField(makeField());
        sim.setInitialPoint(1f, 1f);
        sim.setLearningRate(0.01f);

        sim.step(0.5f);

        assertEquals(0.5f, sim.getTimeElapsed());
        List<Vector2> path = sim.getPath();
        assertEquals(2, path.size());
        Vector2 head = path.get(path.size() - 1);
        assertTrue(head.getX() < 1f && head.getY() < 1f);
    }

    @Test
    void testRunEpochs() {
        Simulation sim = new Simulation();
        sim.setField(makeField());
        sim.setInitialPoint(0.5f, -0.5f);
        sim.runEpochs(3);
        assertEquals(4, sim.getPath().size());
    }

    @Test
    void testOverwritePath() {
        Simulation sim = new Simulation();
        sim.overwritePath(null);
        assertNull(sim.getCurrentPoint());

        Vector2 a = new Vector2(1f, 1f);
        Vector2 b = new Vector2(2f, 2f);
        sim.overwritePath(Arrays.asList(a, b));
        assertEquals(b, sim.getCurrentPoint());
        assertEquals(2, sim.getPath().size());
    }

    @Test
    void testRequiresFieldAndPoint() {
        Simulation sim = new Simulation();
        assertThrows(ArgumentOutOfBoundsException.class, () -> sim.step(0.1f));
    }
}
