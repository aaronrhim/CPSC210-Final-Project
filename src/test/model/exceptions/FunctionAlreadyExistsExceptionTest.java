package model.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class FunctionAlreadyExistsExceptionTest {

    @Test
    void testIsSimulationException() {
        FunctionAlreadyExistsException ex = new FunctionAlreadyExistsException();
        assertTrue(ex instanceof SimulationException);
    }
}
