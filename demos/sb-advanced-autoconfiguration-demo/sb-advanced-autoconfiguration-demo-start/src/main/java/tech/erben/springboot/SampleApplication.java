package tech.erben.springboot;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SampleApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        DataSource ds = ctx.getBean(DataSource.class);

        try (Connection conn = ds.getConnection()) {
            System.out.println(conn.isValid(1000));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
