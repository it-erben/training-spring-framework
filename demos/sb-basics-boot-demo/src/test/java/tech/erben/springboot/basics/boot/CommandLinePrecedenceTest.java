package tech.erben.springboot.basics.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bildet die Demo-Pointe als Test nach: {@code args} reicht die Argumente
 * wie bei {@code java -jar ... --shop.page-size=99} an
 * {@code SpringApplication} weiter — es entsteht dieselbe Property-Quelle
 * ({@code commandLineArgs}) wie beim echten Start. Das ist ehrlicher als
 * {@code @SpringBootTest(properties = ...)}, denn das wäre eine rein
 * testspezifische Quelle mit eigener Rangfolge. Das Profil {@code dev}
 * ist absichtlich aktiv: Das Kommandozeilenargument schlägt nicht nur
 * die Basis-Datei (20), sondern auch die Profil-Datei (5).
 */
@SpringBootTest(args = "--shop.page-size=99")
@ActiveProfiles("dev")
@DisplayName("Kommandozeilenargumente schlagen Profil- und Basis-Properties")
class CommandLinePrecedenceTest {

    @Autowired
    private ShopProperties shopProperties;

    @Test
    void commandLineArgumentWins() {
        assertThat(shopProperties.pageSize()).isEqualTo(99);
    }
}
