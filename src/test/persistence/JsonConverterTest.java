package persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import model.ScalarField;
import model.Simulation;
import model.Vector2;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

@ExcludeFromJacocoGeneratedReport
public class JsonConverterTest {

    @Test
    void testSimulationRoundTrip() {
        ScalarField field = new ScalarField("x+y", (x, y) -> x + y);
        Simulation sim = new Simulation();
        sim.setField(field);
        sim.setLearningRate(0.5f);
        sim.setStopThreshold(0.2f);
        sim.setMaxIterations(5);
        sim.setEps(0.1f);
        sim.setInitialPoint(1f, 1f);
        sim.runEpochs(2);
        sim.setTimeElapsed(1.5f);

        JSONObject json = JsonConverter.simulationToJsonObject(sim);
        Simulation restored = JsonConverter.jsonObjectToSimulation(json);

        assertNotNull(restored.getField());
        assertEquals(sim.getLearningRate(), restored.getLearningRate());
        assertEquals(sim.getStopThreshold(), restored.getStopThreshold());
        assertEquals(sim.getMaxIterations(), restored.getMaxIterations());
        assertEquals(sim.getEps(), restored.getEps());
        assertEquals(sim.getPath().size(), restored.getPath().size());
        Vector2 end = sim.getPath().get(sim.getPath().size() - 1);
        assertEquals(end, restored.getCurrentPoint());
    }
}
