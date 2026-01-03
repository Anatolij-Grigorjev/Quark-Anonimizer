package tiem625.anonimizer.testsupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiem625.anonimizer.commonterms.*;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;
import tiem625.anonimizer.generating.DataGenerator.FieldConstraints;
import tiem625.anonimizer.tooling.sql.jdbc.DBMetadataReader;
import tiem625.anonimizer.tooling.streams.Wrappers.ThrowsCheckedFunc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class TestDbContext {
    private static final Logger LOG = LoggerFactory.getLogger(TestDbContext.class);

    private final DataSource dataSource;
    private final TestData data = new TestData();
    private final DBMetadataReader dbMetaReader;

    @Inject TestDbContext(
            DataSource dataSource,
            @ConfigProperty(name = "anonimizer.data.schema") String testDbSchema
    ) {
        this.dataSource = dataSource;
        this.dbMetaReader = new DBMetadataReader(dataSource, testDbSchema);
    }

    public void dropSchemaTables() {
        try {
            var tableNames = dbMetaReader.getTablesNamesInSchema();
            for (String tableName : tableNames) {
                try (var dropTableStatement = prepareStatement("DROP TABLE IF EXISTS " + tableName)) {
                    dropTableStatement.execute();
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean batchExists(BatchName batchName) {
        try (var statement = prepareStatement("SELECT 1 FROM " + batchName)) {
            statement.execute();
            return true;
        } catch (SQLException missingTableEx) {
            return false;
        }
    }

    public Amount getBatchRecordsCount(BatchName batchName) {
        try (var statement = prepareStatement("SELECT COUNT(*) FROM " + batchName)) {
            ResultSet rowsCount = statement.executeQuery();
            int numericCount = rowsCount.getInt(1);
            return Amount.of(numericCount);
        } catch (SQLException sqlEx) {
            throw new RuntimeException(sqlEx);
        }
    }

    public List<DataFieldSpec> getBatchFieldSpecs(BatchName batchName) {
        try {
            dbMetaReader.getUniqueTableNameForBatch(batchName);
            return dbMetaReader.extractFieldSpecsFromBatchColumns(batchName);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<DataObject> getAllBatchValues(BatchName batchName) {
        var batchFieldSpecs = getBatchFieldSpecs(batchName);
        try (var fetchValues = prepareStatement("SELECT * FROM " + batchName)) {
            var valuesCursor = fetchValues.executeQuery();
            var tableData = new ArrayList<DataObject>();
            while(valuesCursor.next()) {
                tableData.add(collectRowDataObject(valuesCursor, batchFieldSpecs));
            }
            return tableData;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void createBatch(BatchName batchName) {
        createBatch(batchName, data.idEmailFieldsSpecs());
    }

    public void createBatch(BatchName batchName, List<DataFieldSpec> fields) {
        var createQuery = "CREATE TABLE " + batchName + "("
                + columnDefinitionsLines(fields)
                + ");";
        try (var statement = prepareStatement(createQuery)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void insertRows(BatchName batchName, List<DataObject> rows) {
        if (!batchExists(batchName)) {
            throw new RuntimeException("batch " + batchName + " is missing");
        }
        if (rows.isEmpty()) {
            return;
        }
        var orderedFieldsNames = new ArrayList<>(rows.getFirst().fieldNames());
        String insertStatementTemplate = buildInsertStatement(batchName, orderedFieldsNames);
        for(var row: rows) {
            try(var statement = prepareStatement(insertStatementTemplate)) {
                setStatementVars(statement, orderedFieldsNames, row);
                statement.executeUpdate();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }



    /////////////IMPL HELPERS//////////

    private class SingleStatementConnection implements AutoCloseable {

        private final Connection connection;
        private final PreparedStatement statement;

        public SingleStatementConnection(String statementSQL) throws SQLException {
            LOG.info(statementSQL);
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(statementSQL);
        }

        @Override
        public void close() throws SQLException {
            statement.close();
            connection.close();
        }

        public void execute() throws SQLException {
            statement.execute();
        }

        public ResultSet executeQuery() throws SQLException {
            return statement.executeQuery();
        }

        public void executeUpdate() throws SQLException {
            statement.executeUpdate();
        }
    }

    private String buildInsertStatement(BatchName batchName, ArrayList<FieldName> fieldsNames) {
        Collector<CharSequence, ?, String> joinParamsList = Collectors.joining(", ", "(", ")");
        return "INSERT INTO " + batchName +
                " " +
                fieldsNames.stream().map(FieldName::asString).collect(joinParamsList) +
                " VALUES " +
                IntStream.range(0, fieldsNames.size()).mapToObj(idx -> "?").collect(joinParamsList) +
                ";";
    }

    private void setStatementVars(SingleStatementConnection wrapper, List<FieldName> varsSequence, DataObject vars) {
        IntStream.range(0, varsSequence.size()).forEachOrdered(idx -> {
            var varName = varsSequence.get(idx);
            var varValue = vars.getValue(varName);
            try {
                setStatementVarValue(wrapper.statement, idx, varValue);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setStatementVarValue(PreparedStatement statement, int idx, FieldValue varValue) throws SQLException {
        switch (varValue.type()) {
            case TEXT -> statement.setString(idx + 1, (String) varValue.content());
            case NUMBER -> statement.setBigDecimal(idx + 1, contentAsBigDecimal(varValue.content()));
        }
    }

    private BigDecimal contentAsBigDecimal(Object content) {
        if (content == null) {
            return null;
        }
        if (content instanceof BigDecimal) {
            return (BigDecimal) content;
        }
        if (content instanceof Integer) {
            return BigDecimal.valueOf((int) content);
        }
        throw new IllegalStateException("Cannot BigDecimal content of type " + content.getClass());
    }

    private SingleStatementConnection prepareStatement(String sql) throws SQLException {
        return new SingleStatementConnection(sql);
    }

    private DataObject collectRowDataObject(ResultSet tableData, List<DataFieldSpec> fieldSpecs) throws SQLException {
        var fieldValues = fieldSpecs.stream().map(fieldSpec -> {
            var valueExtractor = pickValueExtractor(fieldSpec.fieldType(), tableData);
            var fieldName = fieldSpec.fieldName();
            var fieldValue = FieldValue.of(fieldSpec.fieldType(), valueExtractor.apply(fieldName.asString()));
            return Map.entry(fieldName, fieldValue);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return DataObject.withFields(fieldValues);
    }

    private ThrowsCheckedFunc<String, ?> pickValueExtractor(FieldType fieldType, ResultSet valuesStore) {
        return switch (fieldType) {
            case TEXT -> valuesStore::getString;
            case NUMBER -> valuesStore::getBigDecimal;
        };
    }

    private String columnDefinitionsLines(List<DataFieldSpec> fields) {
        return fields.stream().map(field ->
                field.fieldName() + " " + fieldSQLType(field.fieldType()) + " " + constraintWords(field.fieldConstraints())
        ).collect(Collectors.joining(",\n"));
    }

    private String fieldSQLType(FieldType fieldType) {
        return switch (fieldType) {
            case TEXT -> "varchar(250)";
            case NUMBER -> "int";
        };
    }

    private String constraintWords(FieldConstraints fieldConstraints) {
        return (fieldConstraints.nullable() ? "NULL" : "NOT NULL")
                + " "
                + (fieldConstraints.unique() ? "UNIQUE" : "");
    }
}
