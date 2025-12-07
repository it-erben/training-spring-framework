package tech.erben;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyModelSampleApplication implements CommandLineRunner {

    private final MyModel myModel;

    public MyModelSampleApplication(MyModel myModel) {
        this.myModel = myModel;
    }

    public static void main(String[] args) {
        SpringApplication.run(MyModelSampleApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println(myModel);
    }
}
