package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import org.junit.jupiter.api.Test;

@ExcludeFromJacocoGeneratedReport
public class EventTest {

    @Test
    void testEquality() {
        Event event = new Event("desc");
        assertEquals(event, event);
        assertNotEquals(event, new Event("desc"));
    }
}
