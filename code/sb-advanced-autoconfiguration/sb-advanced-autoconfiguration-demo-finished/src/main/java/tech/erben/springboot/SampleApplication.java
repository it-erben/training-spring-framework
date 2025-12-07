package tech.erben.springboot;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;

public class SampleApplication {

    @Import(DefaultConfig.class)
    public static class MyConfig {}

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
            MyConfig.class
        );
        DataSource ds = ctx.getBean(DataSource.class);
        try (Connection connection = ds.getConnection()) {
            System.out.print(connection.isValid(1000));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
