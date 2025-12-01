package model.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import org.junit.jupiter.api.Test;

@ExcludeFromJacocoGeneratedReport
public class NonMatchingClassExceptionTest {

    @Test
    void testIsRuntimeException() {
        NonMatchingClassException ex = new NonMatchingClassException();
        assertTrue(ex instanceof RuntimeException);
    }
}
