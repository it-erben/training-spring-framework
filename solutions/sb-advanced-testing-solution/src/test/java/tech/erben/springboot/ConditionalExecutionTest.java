package tech.erben.springboot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

class ConditionalExecutionTest {

    @Test
    @EnabledOnOs(OS.MAC)
    void onlyOnMac() {
        assertTrue(System.getProperty("os.name").toLowerCase().contains("mac"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_SLOW", matches = "true")
    void onlyWhenEnvSet() {
        assertTrue(true);
    }

    @Test
    @EnabledIf("customCondition")
    void enabledByCustomCondition() {
        assertTrue(true);
    }

    static boolean customCondition() {
        return true;
    }
}
