package tech.erben.springboot.vaultdemo;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatasourceBean {

    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    private void init() {
        System.out.println("------------------------------------------------");
        System.out.println(password);
    }
}
