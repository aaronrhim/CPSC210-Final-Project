package model.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import org.junit.jupiter.api.Test;

@ExcludeFromJacocoGeneratedReport
public class FunctionDoesntExistExceptionTest {

    @Test
    void testIsSimulationException() {
        FunctionDoesntExistException ex = new FunctionDoesntExistException();
        assertTrue(ex instanceof SimulationException);
    }
}
