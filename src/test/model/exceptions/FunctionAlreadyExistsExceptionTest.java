package model.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import org.junit.jupiter.api.Test;

@ExcludeFromJacocoGeneratedReport
public class FunctionAlreadyExistsExceptionTest {

    @Test
    void testIsSimulationException() {
        FunctionAlreadyExistsException ex = new FunctionAlreadyExistsException();
        assertTrue(ex instanceof SimulationException);
    }
}
