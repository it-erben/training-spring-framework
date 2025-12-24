package tech.erben.springboot.support;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Platzhalter für eine eigene JUnit-Extension aus der Übung.
 * Implementiere Start- und Endzeitmessung in beforeTestExecution/afterTestExecution.
 */
public class TimingExtension
    implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        // TODO: Zeitstempel vor dem Test speichern
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        // TODO: Dauer berechnen und ggf. loggen/ausgeben
    }
}
