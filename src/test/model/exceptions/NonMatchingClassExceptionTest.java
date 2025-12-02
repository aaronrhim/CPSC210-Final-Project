package model.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class NonMatchingClassExceptionTest {

    @Test
    void testIsRuntimeException() {
        NonMatchingClassException ex = new NonMatchingClassException();
        assertTrue(ex instanceof RuntimeException);
    }
}
