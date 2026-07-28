package tech.erben.springboot.basics.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Die eigentliche Pointe von {@code @Profile}: Es geht nicht nur um
 * andere Property-Werte, sondern darum, ob der Container eine Bean
 * ueberhaupt anlegt. Beide Faelle nebeneinander — derselbe Code, nur das
 * aktive Profil entscheidet.
 */
@DisplayName("DevDataInitializer existiert nur im Profil dev")
class DevDataInitializerProfileTest {

    @Nested
    @SpringBootTest
    @DisplayName("Ohne aktives Profil legt der Container die Bean nicht an")
    class WithoutDevProfile {

        @Autowired
        private ApplicationContext context;

        @Test
        void beanIsAbsent() {
            assertThat(context.getBeanProvider(DevDataInitializer.class).getIfAvailable())
                    .isNull();
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("dev")
    @DisplayName("Mit Profil dev existiert die Bean")
    class WithDevProfile {

        @Autowired
        private ApplicationContext context;

        @Test
        void beanIsPresent() {
            assertThat(context.getBeanProvider(DevDataInitializer.class).getIfAvailable())
                    .isNotNull();
        }
    }
}
