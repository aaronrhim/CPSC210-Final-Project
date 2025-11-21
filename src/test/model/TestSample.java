package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestSample {

    // EFFECTS: ensures sample initializes internal value
    @Test
    void testConstructor() {
        Sample sample = new Sample();
        assertEquals(4, sample.other);
    }
}
