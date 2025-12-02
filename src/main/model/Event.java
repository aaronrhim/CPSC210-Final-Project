package model;

import java.util.Calendar;
import java.util.Date;

/**
 * Represents an alarm system event.
 */
public class Event {
    private static final int HASH_CONSTANT = 13;
    private final Date dateLogged;
    private final String description;

    // EFFECTS: Creates an event for persistence by recording 
    public Event(String description) {
        dateLogged = Calendar.getInstance().getTime();
        this.description = description;
    }

    // EFFECTS: returns dateLogged
    public Date getDate() {
        return dateLogged;
    }

    // EFFECTS: returns description
    public String getDescription() {
        return description;
    }

    // REQUIRES: other has elements and object classes must be equal to each other
    // MODIFIES: none
    // EFFECTS: checks if dateLogged and description are both consistent with this
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Event)) {
            return false;
        }
        Event otherEvent = (Event) other;
        return this.dateLogged.equals(otherEvent.dateLogged)
                && this.description.equals(otherEvent.description);
    }

    // EFFECTS: 
    @Override
    public int hashCode() {
        return (HASH_CONSTANT * dateLogged.hashCode() + description.hashCode());
    }

    @Override
    public String toString() {
        return dateLogged.toString() + "\n" + description;
    }
}
