package tech.erben.springboot.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TimingExtension
    implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(
        TimingExtension.class
    );

    private static final String START_TIME = "startTime";

    private final Map<String, Long> durations = new ConcurrentHashMap<>();

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(NAMESPACE).put(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        long start = context.getStore(NAMESPACE).remove(START_TIME, long.class);
        long duration = System.currentTimeMillis() - start;
        durations.put(context.getRequiredTestMethod().getName(), duration);
        System.out.printf("Test %s took %d ms.%n", context.getDisplayName(), duration);
    }

    public Map<String, Long> getDurations() {
        return durations;
    }
}
