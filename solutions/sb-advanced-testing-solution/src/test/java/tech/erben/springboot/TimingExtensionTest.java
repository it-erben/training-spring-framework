package tech.erben.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.erben.springboot.support.TimingExtension;

@ExtendWith(TimingExtension.class)
class TimingExtensionTest {

    @Test
    void measuresExecutionTime() {
        assertThat(1 + 1).isEqualTo(2);
    }
}
