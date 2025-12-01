package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;
import org.junit.jupiter.api.Test;

@ExcludeFromJacocoGeneratedReport
public class EventLogTest {

    @Test
    void testLogAndClear() {
        EventLog log = EventLog.getInstance();
        log.clear();
        List<Event> events = new ArrayList<>();
        for (Event e : log) {
            events.add(e);
        }
        assertEquals(1, events.size());

        log.logEvent(new Event("added"));
        events.clear();
        for (Event e : log) {
            events.add(e);
        }
        assertTrue(events.stream().anyMatch(e -> e.getDescription().equals("added")));
    }
}
