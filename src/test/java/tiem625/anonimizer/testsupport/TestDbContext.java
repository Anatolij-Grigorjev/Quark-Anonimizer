package tiem625.anonimizer.testsupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.DataObject;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;

import javax.sql.DataSource;
import java.util.List;

@ApplicationScoped
public class TestDbContext {

    @Inject
    DataSource dataSource;

    private final TestData data = new TestData();

    public boolean batchExists(BatchName table) {
        throw new UnsupportedOperationException("TODO");
    }

    public Amount getBatchRecordsCount(BatchName tableName) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<DataFieldSpec> getBatchFieldSpecs(BatchName batchName) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<DataObject> getAllBatchValues(BatchName batchName) {
        throw new UnsupportedOperationException("TODO");
    }

    public void createBatch(BatchName batchName) {
        createBatch(batchName, data.idEmailFieldsSpecs());
    }

    public void createBatch(BatchName batchName, List<DataFieldSpec> fields) {
        throw new UnsupportedOperationException("TODO");
    }

    public void insertRows(BatchName batchName, List<DataObject> rows) {
        throw new UnsupportedOperationException("TODO");
    }
}
