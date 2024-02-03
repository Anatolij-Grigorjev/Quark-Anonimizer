package tiem625.anonimizer.tooling.sql.jdbc;

import jakarta.inject.Inject;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.DataObject;
import tiem625.anonimizer.generating.DataGenerator;
import tiem625.anonimizer.tooling.sql.BatchQueriesResolver;

import javax.sql.DataSource;
import java.util.List;

public class JdbcBatchQueriesResolver implements BatchQueriesResolver {

    private final DataSource dataSource;

    @Inject
    public JdbcBatchQueriesResolver(DataSource jdbcDataSource) {
        this.dataSource = jdbcDataSource;
    }

    @Override
    public void executeBatchCreationQuery(BatchName batchName, List<DataGenerator.DataFieldSpec> fieldSpecs) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean executeCheckBatchExistsQuery(BatchName batchName) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Amount executeFetchBatchSizeQuery(BatchName batchName) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<DataObject> executeFetchSomeBatchObjectsQuery(BatchName batchName, Amount amount) {
        throw new UnsupportedOperationException("TODO");
    }
}
