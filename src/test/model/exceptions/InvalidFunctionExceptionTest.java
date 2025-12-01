package model.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import org.junit.jupiter.api.Test;

@ExcludeFromJacocoGeneratedReport
public class InvalidFunctionExceptionTest {

    @Test
    void testMessage() {
        InvalidFunctionException ex = new InvalidFunctionException("bad");
        assertEquals("bad", ex.getMessage());
    }
}
