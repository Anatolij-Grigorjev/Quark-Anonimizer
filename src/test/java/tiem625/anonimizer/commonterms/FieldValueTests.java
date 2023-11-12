package tiem625.anonimizer.commonterms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.testsupport.PrettyTestNames;

@PrettyTestNames
public class FieldValueTests {

    @Test
    void cant_create_untyped_null() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldValue.ofTyped(null));
    }

    @Test
    void field_value_typed_null_ok() {
        Assertions.assertDoesNotThrow(() -> FieldValue.of(FieldType.TEXT, null));
    }

    @Test
    void typed_value_infers_correctly() {
        Assertions.assertEquals(FieldType.NUMBER, FieldValue.ofTyped(78).type());
        Assertions.assertEquals(FieldType.NUMBER, FieldValue.ofTyped(-7.8).type());
        Assertions.assertEquals(FieldType.TEXT, FieldValue.ofTyped("hello").type());
    }

    @Test
    void field_values_other_type_not_supported() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldValue.ofTyped(new Object()));
    }

    @Test
    void typed_creation_wrong_type_throws() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldValue.of(FieldType.TEXT, 67));
        Assertions.assertThrows(IllegalArgumentException.class, () -> FieldValue.of(FieldType.NUMBER, "hello"));
    }

    @Test
    void typed_creation_null_type_can_infer() {
        Assertions.assertEquals(FieldType.TEXT, FieldValue.of(null, "hello").type());
    }
}
