package tiem625.anonimizer.tooling.sql.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.generating.DataGenerator;
import tiem625.anonimizer.testsupport.PrettyTestNames;
import tiem625.anonimizer.testsupport.TestData;
import tiem625.anonimizer.tooling.sql.BatchQueriesResolver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static tiem625.anonimizer.testsupport.Wrappers.unchecked;

@PrettyTestNames
public class JdbcBatchQueriesResolverTests {

    private final TestData data = new TestData();
    private BatchQueriesResolver batchQueriesResolver;
    private PreparedStatement jdbcStatement;

    @BeforeEach
    void setup() {
        DataSource dataSource = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        jdbcStatement = Mockito.spy(PreparedStatement.class);
        Mockito.when(unchecked(dataSource::getConnection)).thenReturn(connection);
        Mockito.when(unchecked(connection::prepareStatement, Mockito.anyString())).thenReturn(jdbcStatement);
        batchQueriesResolver = new JdbcBatchQueriesResolver(dataSource);
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
    void check_batch_exists_fails_on_bad_args() {
        BatchName nullBatch = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> batchQueriesResolver.executeCheckBatchExistsQuery(nullBatch));
    }

    @Test
    void batch_size_fails_on_bar_args() {
        BatchName nullBatch = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> batchQueriesResolver.executeFetchBatchSizeQuery(nullBatch));
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
}
