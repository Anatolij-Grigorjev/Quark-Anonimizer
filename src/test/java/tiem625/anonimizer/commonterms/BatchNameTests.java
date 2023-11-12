package tiem625.anonimizer.commonterms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.testsupport.PrettyTestNames;

@PrettyTestNames
public class BatchNameTests {

    @Test
    void batch_name_cant_be_empty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> BatchName.of(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BatchName.of(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BatchName.of("\n  \t\t"));
    }

    @Test
    void batch_name_cant_start_with_number() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> BatchName.of("1_batch"));
    }

    @Test
    void batch_name_only_ascii_and_underscore() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> BatchName.of("мой_батч"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BatchName.of("myBatch/2"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BatchName.of("batch[8]"));
    }

    @Test
    void batch_name_ascii_underscores_capitalization_ok() {
        Assertions.assertDoesNotThrow(() -> BatchName.of("_batch1"));
        Assertions.assertDoesNotThrow(() -> BatchName.of("MyBaTcH"));
        Assertions.assertDoesNotThrow(() -> BatchName.of("batches3"));
        Assertions.assertDoesNotThrow(() -> BatchName.of("_batch_4_xxx_"));
    }
}
