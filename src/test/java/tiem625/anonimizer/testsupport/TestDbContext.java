package tiem625.anonimizer.testsupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.DataObject;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class TestDbContext {

    @Inject
    DataSource dataSource;

    @ConfigProperty(name = "test.anonimizer.data.schema")
    String testDbSchema;

    private final TestData data = new TestData();

    public boolean batchExists(BatchName batchName) {
        try (var statement = prepareStatement("SELECT 1 FROM " + batchName.asString())) {
            statement.execute();
            return true;
        } catch (SQLException missingTableEx) {
            return false;
        }
    }

    public Amount getBatchRecordsCount(BatchName batchName) {
        try (var statement = prepareStatement("SELECT COUNT(*) FROM " + batchName.asString())) {
            ResultSet rowsCount = statement.executeQuery();
            int numericCount = rowsCount.getInt(1);
            return Amount.of(numericCount);
        } catch (SQLException sqlEx) {
            throw new RuntimeException(sqlEx);
        }
    }

    public List<DataFieldSpec> getBatchFieldSpecs(BatchName batchName) {
        try (var connection = dataSource.getConnection()) {
            var metaData = connection.getMetaData();
            assertUniqueTable(metaData, batchName);
            throw new UnsupportedOperationException("TODO");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
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

    private PreparedStatement prepareStatement(String sql) {
        try (var connection = dataSource.getConnection()) {
            return connection.prepareStatement(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int getNumFetchedRows(ResultSet rows) {
        try {
            rows.afterLast();
            int numRows = rows.getRow();
            rows.beforeFirst();
            return numRows;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void assertUniqueTable(DatabaseMetaData metaData, BatchName batchName) throws SQLException {
        var matchingTables = metaData.getTables(null, testDbSchema, batchName.asString(), null);
        int numTables = getNumFetchedRows(matchingTables);
        if (numTables > 1) {
            throw new RuntimeException("schema " + testDbSchema + " has more than 1 table with pattern " + batchName);
        }
    }
}
