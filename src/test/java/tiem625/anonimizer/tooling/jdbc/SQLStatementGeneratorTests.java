package tiem625.anonimizer.tooling.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldType;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;
import tiem625.anonimizer.testsupport.PrettyTestNames;
import tiem625.anonimizer.testsupport.TestData;

import java.util.List;

@PrettyTestNames
public class SQLStatementGeneratorTests {

    private static final int VARCHAR_LENGTH = 250;

    TestData data;

    private SQLStatementGenerator sqlStatementGenerator;

    @BeforeEach
    void setupData() {
        data = new TestData();
        var config = SQLStatementGeneratorConfig.builder()
                .sqlTypeForFieldType("int", FieldType.NUMBER)
                .sqlTypeForFieldType("varchar(%s)".formatted(VARCHAR_LENGTH), FieldType.TEXT)
                .build();
        sqlStatementGenerator = new SQLStatementGenerator(config);
    }

    @Test
    void create_table_fails_for_empty_inputs() {
        BatchName nullBatchName = null;
        List<DataFieldSpec> nullFieldSpecs = null;
        List<DataFieldSpec> fieldSpecs = data.idEmailFieldsSpecs();

        Assertions.assertThrows(IllegalArgumentException.class, () -> sqlStatementGenerator.createTableStatement(nullBatchName, fieldSpecs));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sqlStatementGenerator.createTableStatement(data.TST_BATCH, nullFieldSpecs));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sqlStatementGenerator.createTableStatement(data.TST_BATCH, List.of()));
    }

    @Test
    void create_table_returns_sql_for_fields() {
        var batchName = data.TST_BATCH;
        String field1 = "field1";
        String field2 = "field2";
        var fieldSpecs = data.textFieldSpecsList(field1, field2);

        String statement = sqlStatementGenerator.createTableStatement(batchName, fieldSpecs);

        assertStatementBlocksEqual(
                """
                        CREATE TABLE %s (
                            %s varchar(%d),
                            %s varchar(%d)
                        );"""
                        .formatted(batchName, field1, VARCHAR_LENGTH, field2, VARCHAR_LENGTH).stripIndent(), statement);
    }

    @Test
    void create_table_sql_supports_nullability_uniq_constraints() {
        var batchName = data.TST_BATCH;
        var fieldSpecs = data.idEmailFieldsSpecs();

        String statement = sqlStatementGenerator.createTableStatement(batchName, fieldSpecs);

        assertStatementBlocksEqual("""
                        CREATE TABLE %s (
                            %s int NOT NULL,
                            %s varchar(%d),
                            UNIQUE (%s, %s)
                        );"""
                .formatted(batchName, data.ID, data.EMAIL, VARCHAR_LENGTH, data.ID, data.EMAIL).stripIndent(),
                statement
        );
    }

    @Test
    void check_table_exists_fails_on_bad_args() {
        BatchName nullBatch = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> sqlStatementGenerator.checkTableExistsStatement(nullBatch));
    }

    @Test
    void check_table_exists_creates_select_1() {
        BatchName batchName = data.TST_BATCH;

        String statement = sqlStatementGenerator.checkTableExistsStatement(batchName);

        Assertions.assertEquals("SELECT 1 FROM %s;".formatted(batchName).stripIndent(), statement);
    }

    @Test
    void table_size_fails_on_bar_args() {
        BatchName nullBatch = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> sqlStatementGenerator.tableSizeStatement(nullBatch));
    }

    @Test
    void table_size_makes_select_count_statement() {
        BatchName batchName = data.TST_BATCH;

        String statement = sqlStatementGenerator.tableSizeStatement(batchName);

        Assertions.assertEquals("SELECT COUNT(*) FROM %s;".formatted(batchName), statement);
    }

    @Test
    void fetch_table_records_fails_on_bad_args() {
        BatchName nullBatch = null;
        Amount nullAmount = null;
        Amount _10Rows = Amount.of(10);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sqlStatementGenerator.fetchTableRowsStatement(nullBatch, _10Rows));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sqlStatementGenerator.fetchTableRowsStatement(data.TST_BATCH, nullAmount));
        Assertions.assertThrows(IllegalArgumentException.class, () -> sqlStatementGenerator.fetchTableRowsStatement(data.TST_BATCH, Amount.NONE));
    }

    @Test
    void fetch_table_records_makes_select_with_limit_statement() {
        BatchName batchName = data.TST_BATCH;
        Amount amount = Amount.of(10);

        String statement = sqlStatementGenerator.fetchTableRowsStatement(batchName, amount);

        Assertions.assertEquals("SELECT * FROM %s LIMIT %s;".formatted(batchName, amount), statement);
    }

    private void assertStatementBlocksEqual(String expected, String actual) {
        Assertions.assertEquals(
                expected.replaceAll("\\s{2,}", ""),
                actual.replaceAll("\\s{2,}", "")
        );
    }
}
