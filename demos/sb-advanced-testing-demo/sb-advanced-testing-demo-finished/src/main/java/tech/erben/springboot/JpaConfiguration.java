package tech.erben.springboot;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("tech.erben.springboot")
@EntityScan("tech.erben.springboot")
@Configuration
public class JpaConfiguration {}
