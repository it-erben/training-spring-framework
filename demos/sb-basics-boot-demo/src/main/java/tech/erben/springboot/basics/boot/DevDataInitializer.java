package tech.erben.springboot.basics.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Existiert nur, wenn das Profil {@code dev} aktiv ist — ohne
 * {@code --spring.profiles.active=dev} legt der Container diese Bean gar
 * nicht erst an. Typischer Einsatz: Testdaten einspielen, die in
 * Produktion nichts verloren haben.
 */
@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    @Override
    public void run(String... args) {
        log.info("Dev-Profil aktiv — hier würden jetzt Testdaten für die Buchhandlung eingespielt.");
    }
}
