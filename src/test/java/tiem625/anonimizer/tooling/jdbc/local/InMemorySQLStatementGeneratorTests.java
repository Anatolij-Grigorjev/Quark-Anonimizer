package tiem625.anonimizer.tooling.jdbc.local;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldType;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;
import tiem625.anonimizer.testsupport.PrettyTestNames;
import tiem625.anonimizer.testsupport.TestData;
import tiem625.anonimizer.tooling.jdbc.SQLStatementGenerator;

import java.util.List;

@PrettyTestNames
public class InMemorySQLStatementGeneratorTests {

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
        sqlStatementGenerator = new InMemorySQLStatementGenerator(config);
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

        throw new UnsupportedOperationException("TODO");
    }

    @Test
    void create_table_sql_supports_nullability_uniq_constraints() {
        var batchName = data.TST_BATCH;
        var fieldSpecs = data.idEmailFieldsSpecs();

        throw new UnsupportedOperationException("TODO");
    }

    @Test
    void check_table_exists_fails_on_bad_args() {
        BatchName nullBatch = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> sqlStatementGenerator.checkTableExistsStatement(nullBatch));
    }

    @Test
    void check_table_exists_creates_select_1() {
        BatchName batchName = data.TST_BATCH;

        var statement = sqlStatementGenerator.checkTableExistsStatement(batchName);

        Assertions.assertEquals("SELECT 1 FROM ?", statement.queryText());
        Assertions.assertEquals(1, statement.queryParameters().size());
        Assertions.assertEquals(data.TST_BATCH, statement.queryParameters().get(0).value());
    }

    @Test
    void table_size_fails_on_bar_args() {
        BatchName nullBatch = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> sqlStatementGenerator.getTableSizeStatement(nullBatch));
    }

    @Test
    void table_size_makes_select_count_statement() {
        BatchName batchName = data.TST_BATCH;

        var statement = sqlStatementGenerator.getTableSizeStatement(batchName);

        Assertions.assertEquals("SELECT COUNT(*) FROM ?", statement.queryText());
        Assertions.assertEquals(1, statement.queryParameters().size());
        Assertions.assertEquals(data.TST_BATCH, statement.queryParameters().get(0).value());
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

        var statement = sqlStatementGenerator.fetchTableRowsStatement(batchName, amount);

        Assertions.assertEquals("SELECT * FROM ? LIMIT ?", statement.queryText());
        Assertions.assertEquals(2, statement.queryParameters().size());
        Assertions.assertEquals(data.TST_BATCH, statement.queryParameters().get(0).value());
        Assertions.assertEquals(amount, statement.queryParameters().get(1).value());
    }
}
