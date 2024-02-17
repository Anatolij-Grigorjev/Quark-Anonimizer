package tiem625.anonimizer.testsupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tiem625.anonimizer.commonterms.*;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;
import tiem625.anonimizer.generating.FieldConstraint;
import tiem625.anonimizer.testsupport.Wrappers.ThrowsCheckedFunc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class TestDbContext {

    public static final Set<Integer> NUMERIC_SQL_TYPES = Set.of(Types.NUMERIC, Types.INTEGER, Types.BIGINT, Types.TINYINT, Types.SMALLINT);
    public static final Set<Integer> TEXT_SQL_TYPES = Set.of(Types.CHAR, Types.VARCHAR, Types.NCHAR, Types.NVARCHAR, Types.LONGVARCHAR, Types.LONGNVARCHAR);
    @Inject
    DataSource dataSource;

    @ConfigProperty(name = "test.anonimizer.data.schema")
    String testDbSchema;

    private final TestData data = new TestData();

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
        try (var connection = dataSource.getConnection()) {
            var metaData = connection.getMetaData();
            var dbTableName = getUniqueTableName(metaData, batchName);
            return extractFieldSpecsFromColumns(metaData, dbTableName);
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
        throw new UnsupportedOperationException("TODO");
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

    private String buildInsertStatement(BatchName batchName, ArrayList<FieldName> fieldsNames) {
        Collector<CharSequence, ?, String> joinParamsList = Collectors.joining(", ", "(", ")");
        return "INSERT INTO " + batchName +
                " " +
                fieldsNames.stream().map(FieldName::asString).collect(joinParamsList) +
                " VALUES " +
                IntStream.range(0, fieldsNames.size()).mapToObj(idx -> "?").collect(joinParamsList) +
                ";";
    }

    private void setStatementVars(PreparedStatement statement, List<FieldName> varsSequence, DataObject vars) {
        IntStream.range(0, varsSequence.size()).forEachOrdered(idx -> {
            var varName = varsSequence.get(idx);
            var varValue = vars.getValue(varName);
            try {
                setStatementVarValue(statement, idx, varValue);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setStatementVarValue(PreparedStatement statement, int idx, FieldValue varValue) throws SQLException {
        switch (varValue.type()) {
            case TEXT -> {
                statement.setString(idx, (String) varValue.content());
            }
            case NUMBER -> {
                statement.setBigDecimal(idx, contentAsBigDecimal(varValue.content()));
            }
            default -> throw new IllegalStateException("Unexpected value type: " + varValue.type());
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

    private String getUniqueTableName(DatabaseMetaData metaData, BatchName batchName) throws SQLException {
        var matchingTables = metaData.getTables(null, testDbSchema, batchName.asString(), null);
        int numTables = getNumFetchedRows(matchingTables);
        if (numTables != 1) {
            throw new RuntimeException("schema " + testDbSchema + " does not have exactly 1 table with pattern " + batchName);
        }
        return matchingTables.getString("TABLE_NAME");
    }

    private List<DataFieldSpec> extractFieldSpecsFromColumns(DatabaseMetaData metaData, String dbTableName) throws SQLException {
        Set<String> uniqueColumnsNames = collectUniqueColumnsNames(metaData, dbTableName);
        var tableDbColumns = metaData.getColumns(null, testDbSchema, dbTableName, null);
        var fieldSpecs = new ArrayList<DataFieldSpec>();
        while (tableDbColumns.next()) {
            var columnName = tableDbColumns.getString("COLUMN_NAME");
            var columnType = tableDbColumns.getInt("DATA_TYPE");
            boolean columnNullable = Objects.equals(tableDbColumns.getString("NULLABLE"), "YES");
            boolean columnUnique = uniqueColumnsNames.contains(columnName);
            fieldSpecs.add(buildFieldSpec(columnName, columnType, columnNullable, columnUnique));
        }
        return fieldSpecs;
    }

    private DataFieldSpec buildFieldSpec(String columnName, int columnType, boolean columnNullable, boolean columnUnique) {
        List<FieldConstraint> fieldConstraints = new ArrayList<>();
        if (!columnNullable) {
            fieldConstraints.add(FieldConstraint.NOT_NULL);
        }
        if (columnUnique) {
            fieldConstraints.add(FieldConstraint.UNIQUE);
        }
        return new DataFieldSpec(FieldName.of(columnName), resolveFieldType(columnType), fieldConstraints);
    }

    private FieldType resolveFieldType(int columnSQLType) {
        if (NUMERIC_SQL_TYPES.contains(columnSQLType)) {
            return FieldType.NUMBER;
        }
        if (TEXT_SQL_TYPES.contains(columnSQLType)) {
            return FieldType.TEXT;
        }
        throw new IllegalStateException("Cannot resolve FieldType for SQL type " + columnSQLType);
    }

    private Set<String> collectUniqueColumnsNames(DatabaseMetaData metaData, String dbTableName) throws SQLException {
        var tableDbIndexes = metaData.getIndexInfo(null, testDbSchema, dbTableName, true, true);
        Set<String> uniqueColumnsNames = new HashSet<>();
        while(tableDbIndexes.next()) {
            uniqueColumnsNames.add(tableDbIndexes.getString("COLUMN_NAME"));
        }
        return uniqueColumnsNames;
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
        switch (fieldType) {
            case TEXT -> {
                return valuesStore::getString;
            }
            case NUMBER -> {
                return valuesStore::getBigDecimal;
            }
            default -> throw new IllegalStateException("Unexpected field type: " + fieldType);
        }
    }
}
