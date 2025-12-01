package model.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import org.junit.jupiter.api.Test;

@ExcludeFromJacocoGeneratedReport
public class ArgumentOutOfBoundsExceptionTest {

    @Test
    void testMessage() {
        ArgumentOutOfBoundsException ex = new ArgumentOutOfBoundsException("oops");
        assertEquals("oops", ex.getMessage());
    }
}
