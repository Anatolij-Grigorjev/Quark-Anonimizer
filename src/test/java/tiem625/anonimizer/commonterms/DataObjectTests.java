package tiem625.anonimizer.commonterms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.testsupport.PrettyTestNames;

import java.util.Map;

@PrettyTestNames
public class DataObjectTests {

    @Test
    void create_empty_object_not_allowed() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> DataObject.withFields(Map.of()));
    }
}
