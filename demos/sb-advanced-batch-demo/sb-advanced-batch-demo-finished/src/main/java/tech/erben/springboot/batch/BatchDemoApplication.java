package tech.erben.springboot.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BatchDemoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BatchDemoApplication.class, args);
        // Ohne diese Zeile endet der Prozess immer mit 0, auch wenn der Job
        // fehlgeschlagen ist: JobLauncherApplicationRunner wirft nicht, der
        // Fehler steht nur im JobRepository. SpringApplication.exit fragt
        // Boots JobExecutionExitCodeGenerator und übersetzt den BatchStatus
        // in einen Exit-Code, den ein Scheduler auswerten kann.
        System.exit(SpringApplication.exit(context));
    }
}
