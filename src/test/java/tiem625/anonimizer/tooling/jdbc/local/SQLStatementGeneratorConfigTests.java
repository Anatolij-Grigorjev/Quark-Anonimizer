package tiem625.anonimizer.tooling.jdbc.local;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.commonterms.FieldType;
import tiem625.anonimizer.testsupport.PrettyTestNames;

@PrettyTestNames
public class SQLStatementGeneratorConfigTests {

    private SQLStatementGeneratorConfig config;

    @Test
    void default_config_uses_int_varchar_250() {
        var config = SQLStatementGeneratorConfig.builder().build();

        Assertions.assertEquals("int", config.getSQLTypeFor(FieldType.NUMBER));
        Assertions.assertEquals("varchar(250)", config.getSQLTypeFor(FieldType.TEXT));
    }

    @Test
    void editing_mapping_null_not_allowed() {
        var builder = SQLStatementGeneratorConfig.builder();

        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.sqlTypeForFieldType(null, FieldType.TEXT));
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.sqlTypeForFieldType("text", null));
    }

    @Test
    void editing_mapping_in_builder_changes_config() {
        var config = SQLStatementGeneratorConfig.builder()
                .sqlTypeForFieldType("bigint", FieldType.NUMBER)
                .sqlTypeForFieldType("text", FieldType.TEXT)
                .build();

        Assertions.assertEquals("bigint", config.getSQLTypeFor(FieldType.NUMBER));
        Assertions.assertEquals("text", config.getSQLTypeFor(FieldType.TEXT));
    }
}
