package ui;

import ca.ubc.cs.ExcludeFromJacocoGeneratedReport;

/**
 * Marks a UI component that advances its state on each tick.
 */
@ExcludeFromJacocoGeneratedReport
public interface Tickable {
    // EFFECTS: performs update to self
    public void tick();
}
