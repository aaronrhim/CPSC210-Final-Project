package model.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ArgumentOutOfBoundsExceptionTest {

    @Test
    void testMessage() {
        ArgumentOutOfBoundsException ex = new ArgumentOutOfBoundsException("oops");
        assertEquals("oops", ex.getMessage());
    }
}
