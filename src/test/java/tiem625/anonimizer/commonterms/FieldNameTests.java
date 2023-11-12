package tiem625.anonimizer.commonterms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.testsupport.PrettyTestNames;

@PrettyTestNames
public class FieldNameTests {

    @Test
    void field_name_cant_be_empty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldName.of(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldName.of(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldName.of("\n  \t\t"));
    }

    @Test
    void field_name_cant_start_with_number() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldName.of("1_field"));
    }

    @Test
    void field_name_only_ascii_and_underscore() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldName.of("мой_филд"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldName.of("myField/2"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldName.of("field[8]"));
    }

    @Test
    void field_name_ascii_underscores_capitalization_ok() {
        Assertions.assertDoesNotThrow(() -> FieldName.of("_field1"));
        Assertions.assertDoesNotThrow(() -> FieldName.of("MyFiElD"));
        Assertions.assertDoesNotThrow(() -> FieldName.of("fields3"));
        Assertions.assertDoesNotThrow(() -> FieldName.of("_field_4_xxx_"));
    }
}
