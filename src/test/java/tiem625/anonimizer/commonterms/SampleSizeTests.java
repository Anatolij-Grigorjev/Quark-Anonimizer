package tiem625.anonimizer.commonterms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.testsupport.PrettyTestNames;

@PrettyTestNames
public class SampleSizeTests {

    @Test
    void sample_size_cannot_be_negative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> SampleSize.of(-5));
    }

    @Test
    void sample_size_zero_ok() {
        Assertions.assertDoesNotThrow(() -> SampleSize.of(0));
    }

    @Test
    void sample_size_equivalent_preset() {
        Assertions.assertEquals(SampleSize.of(0), SampleSize.EMPTY);
    }

    @Test
    void sample_size_has_amount() {
        Assertions.assertEquals(5, SampleSize.of(5).amount());
    }
}
