package model.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class FunctionDoesntExistExceptionTest {

    @Test
    void testIsSimulationException() {
        FunctionDoesntExistException ex = new FunctionDoesntExistException();
        assertTrue(ex instanceof SimulationException);
    }
}
