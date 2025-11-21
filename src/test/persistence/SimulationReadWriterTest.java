package persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import model.ScalarField;
import model.Simulation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class SimulationReadWriterTest {

    private File lastFile;

    @AfterEach
    void cleanup() {
        if (lastFile != null && lastFile.isFile()) {
            lastFile.delete();
        }
    }

    @Test
    void testWriteAndRead() throws IOException {
        String title = "test_sim_" + System.nanoTime();
        ScalarField field = new ScalarField("x*y", (x, y) -> x * y);
        Simulation sim = new Simulation();
        sim.setField(field);
        sim.setInitialPoint(1f, 2f);
        sim.runEpochs(1);

        SimulationReadWriter.writeSimulation(sim, title);
        lastFile = SimulationReadWriter.fileFromFileTitle(title);
        assertTrue(lastFile.isFile());

        Simulation loaded = SimulationReadWriter.readSimulation(title);
        assertEquals(sim.getPath().size(), loaded.getPath().size());
    }
}
