package model.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class InvalidFunctionExceptionTest {

    @Test
    void testMessage() {
        InvalidFunctionException ex = new InvalidFunctionException("bad");
        assertEquals("bad", ex.getMessage());
    }
}
