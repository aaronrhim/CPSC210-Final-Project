package ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class MainTest {

    @Test
    void testHasMain() throws Exception {
        Method mainMethod = Main.class.getMethod("main", String[].class);
        assertNotNull(mainMethod);
    }
}
