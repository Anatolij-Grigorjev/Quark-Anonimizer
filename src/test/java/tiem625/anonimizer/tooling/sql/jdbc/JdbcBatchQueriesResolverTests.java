package tiem625.anonimizer.tooling.sql.jdbc;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.generating.DataGenerator;
import tiem625.anonimizer.testsupport.PrettyTestNames;
import tiem625.anonimizer.testsupport.TestData;
import tiem625.anonimizer.testsupport.TestDbContext;
import tiem625.anonimizer.tooling.sql.BatchQueriesResolver;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.IntStream;

@PrettyTestNames
@QuarkusTest
public class JdbcBatchQueriesResolverTests {

    private final TestData data = new TestData();
    private BatchQueriesResolver batchQueriesResolver;

    @Inject
    private DataSource dataSource;

    @Inject
    private TestDbContext db;

    @BeforeEach
    void setup() {
        db.dropSchemaTables();
        batchQueriesResolver = new JdbcBatchQueriesResolver(dataSource);
    }

    @AfterEach
    void cleanDb() {
        db.dropSchemaTables();
    }

    @Test
    void create_batch_fails_for_empty_inputs() {
        BatchName nullBatchName = null;
        List<DataGenerator.DataFieldSpec> nullFieldSpecs = null;
        List<DataGenerator.DataFieldSpec> fieldSpecs = data.idEmailFieldsSpecs();

        Assertions.assertThrows(IllegalArgumentException.class, () -> batchQueriesResolver.executeBatchCreationQuery(nullBatchName, fieldSpecs));
        Assertions.assertThrows(IllegalArgumentException.class, () -> batchQueriesResolver.executeBatchCreationQuery(data.TST_BATCH, nullFieldSpecs));
        Assertions.assertThrows(IllegalArgumentException.class, () -> batchQueriesResolver.executeBatchCreationQuery(data.TST_BATCH, List.of()));
    }

    @Test
    void batch_creation_creates_db_table() {
        var batchName = data.TST_BATCH;
        var fieldSpecs = data.idEmailFieldsSpecs();

        batchQueriesResolver.executeBatchCreationQuery(batchName, fieldSpecs);

        Assertions.assertTrue(db.batchExists(batchName));
        List<DataGenerator.DataFieldSpec> batchFields = db.getBatchFieldSpecs(batchName);
        Assertions.assertEquals(fieldSpecs.size(), batchFields.size());
        IntStream.range(0, fieldSpecs.size()).forEachOrdered(idx -> {
            DataGenerator.DataFieldSpec expectedFieldSpec = fieldSpecs.get(idx);
            DataGenerator.DataFieldSpec dbFieldSpec = batchFields.get(idx);
            Assertions.assertEquals(expectedFieldSpec.fieldName(), dbFieldSpec.fieldName());
            Assertions.assertEquals(expectedFieldSpec.fieldType(), dbFieldSpec.fieldType());
            Assertions.assertEquals(expectedFieldSpec.fieldConstraints(), dbFieldSpec.fieldConstraints());
        });
    }

    @Test
    void batch_creation_throws_on_present_table() {
        var batchName = data.TST_BATCH;
        db.createBatch(batchName);

        Assertions.assertThrows(IllegalStateException.class,
                () -> batchQueriesResolver.executeBatchCreationQuery(batchName, data.idEmailFieldsSpecs()));
    }

    @Test
    void check_batch_exists_fails_on_bad_args() {
        BatchName nullBatch = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> batchQueriesResolver.executeCheckBatchExistsQuery(nullBatch));
    }

    @Test
    void check_batch_exists_returns_false_for_missing_table() {
        var batchName = BatchName.of("potato");

        Assertions.assertFalse(batchQueriesResolver.executeCheckBatchExistsQuery(batchName));
    }

    @Test
    void check_batch_exists_returns_true_for_present_table() {
        var batchName = data.TST_BATCH;
        db.createBatch(batchName);

        Assertions.assertTrue(batchQueriesResolver.executeCheckBatchExistsQuery(batchName));
    }

    @Test
    void batch_size_fails_on_bar_args() {
        BatchName nullBatch = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> batchQueriesResolver.executeFetchBatchSizeQuery(nullBatch));
    }

    @Test
    void batch_size_fails_on_missing_batch() {
        var batchName = data.TST_BATCH;

        Assertions.assertThrows(IllegalStateException.class, () -> batchQueriesResolver.executeFetchBatchSizeQuery(batchName));
    }

    @Test
    void batch_size_returns_table_rows_count() {
        var batchName = data.TST_BATCH;
        var fieldSpecs = data.idEmailFieldsSpecs();
        var numDesired = Amount.of(33);
        db.createBatch(batchName, fieldSpecs);
        db.insertRows(batchName, data.dataObjectsForFieldSpecs(numDesired, fieldSpecs));

        Assertions.assertEquals(numDesired, batchQueriesResolver.executeFetchBatchSizeQuery(batchName));
    }

    @Test
    void fetch_batch_objects_fails_on_bad_args() {
        BatchName nullBatch = null;
        Amount nullAmount = null;
        Amount _10Rows = Amount.of(10);

        Assertions.assertThrows(IllegalArgumentException.class, () -> batchQueriesResolver.executeFetchSomeBatchObjectsQuery(nullBatch, _10Rows));
        Assertions.assertThrows(IllegalArgumentException.class, () -> batchQueriesResolver.executeFetchSomeBatchObjectsQuery(data.TST_BATCH, nullAmount));
        Assertions.assertThrows(IllegalArgumentException.class, () -> batchQueriesResolver.executeFetchSomeBatchObjectsQuery(data.TST_BATCH, Amount.NONE));
    }

    @Test
    void fetch_batch_objects_gets_rows_from_table() {
        var batchName = data.TST_BATCH;
        var fieldSpecs = data.idEmailFieldsSpecs();
        var numDesired = Amount.of(33);
        db.createBatch(batchName, fieldSpecs);
        db.insertRows(batchName, data.dataObjectsForFieldSpecs(numDesired, fieldSpecs));

        var rows = batchQueriesResolver.executeFetchSomeBatchObjectsQuery(batchName, numDesired);

        Assertions.assertEquals(numDesired, Amount.of(rows.size()));
        rows.forEach(row -> {
            Assertions.assertEquals(row.fieldNames().size(), fieldSpecs.size());
            fieldSpecs.forEach(fieldSpec -> Assertions.assertDoesNotThrow(() -> row.getValue(fieldSpec.fieldName())));
        });
    }

    @Test
    void fetch_batch_objects_too_many_gets_all_rows_present() {
        var batchName = data.TST_BATCH;
        var fieldSpecs = data.idEmailFieldsSpecs();
        var numDesired = Amount.of(33);
        var numActual = Amount.of(10);
        db.createBatch(batchName, fieldSpecs);
        db.insertRows(batchName, data.dataObjectsForFieldSpecs(numActual, fieldSpecs));

        var rows = batchQueriesResolver.executeFetchSomeBatchObjectsQuery(batchName, numDesired);

        Assertions.assertEquals(numActual, Amount.of(rows.size()));
    }
}
