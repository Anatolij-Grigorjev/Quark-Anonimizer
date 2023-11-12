package tiem625.anonimizer.commonterms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.testsupport.PrettyTestNames;

@PrettyTestNames
public class AmountTests {

    @Test
    void negative_amount_not_allowed() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Amount.of(-8));
    }

    @Test
    void constructed_and_constant_amounts_equivalent() {
        Assertions.assertEquals(Amount.NONE, Amount.of(0));
    }

    @Test
    void amount_numeric_same_as_arg() {
        Assertions.assertEquals(5, Amount.of(5).asNumber());
    }
}
