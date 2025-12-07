package erben.digital.demo;

import erben.digital.CustomService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomStarterDemoApplication implements CommandLineRunner {

    private final CustomService customService;

    public CustomStarterDemoApplication(CustomService customService) {
        this.customService = customService;
    }

    public static void main(String[] args) {
        SpringApplication.run(CustomStarterDemoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        customService.printMessage();
    }
}
