package tech.erben.springboot.basics.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("prod")
@DisplayName("Profil prod überschreibt die Basis-Properties")
class ShopPropertiesProdTest {

    @Autowired
    private ShopProperties shopProperties;

    @Test
    void prodProfileOverridesPageSize() {
        assertThat(shopProperties.pageSize()).isEqualTo(50);
        assertThat(shopProperties.name()).isEqualTo("Buchhandlung Erben");
    }
}
