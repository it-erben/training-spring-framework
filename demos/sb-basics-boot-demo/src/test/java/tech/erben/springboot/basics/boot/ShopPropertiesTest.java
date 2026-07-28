package tech.erben.springboot.basics.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Profil dev ueberschreibt die Basis-Properties")
class ShopPropertiesTest {

    @Autowired
    private ShopProperties shopProperties;

    @Test
    void devProfileOverridesPageSize() {
        assertThat(shopProperties.pageSize()).isEqualTo(5);
        assertThat(shopProperties.name()).isEqualTo("Buchhandlung Erben");
    }
}
