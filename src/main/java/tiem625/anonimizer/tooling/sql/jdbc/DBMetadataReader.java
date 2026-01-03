package tiem625.anonimizer.tooling.sql.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldName;
import tiem625.anonimizer.commonterms.FieldType;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;
import tiem625.anonimizer.generating.FieldConstraint;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;


public class DBMetadataReader {

    private static final Logger LOG = LoggerFactory.getLogger(DBMetadataReader.class);

    public static final Set<Integer> NUMERIC_SQL_TYPES = Set.of(Types.NUMERIC, Types.INTEGER, Types.BIGINT, Types.TINYINT, Types.SMALLINT);
    public static final Set<Integer> TEXT_SQL_TYPES = Set.of(Types.CHAR, Types.VARCHAR, Types.NCHAR, Types.NVARCHAR, Types.LONGVARCHAR, Types.LONGNVARCHAR);

    private final DataSource dataSource;
    private final String dbSchema;

    public DBMetadataReader(DataSource dbConnection, String dbSchema) {
        this.dataSource = dbConnection;
        this.dbSchema = dbSchema;
    }

    public Set<String> getTablesNamesInSchema() throws SQLException {
        var metaData = getConnectionMeta();
        var tablesRows = metaData.getTables(dbSchema, null, null, new String[]{"TABLE", "VIEW"});
        Set<String> tableNames = new HashSet<>();
        while (tablesRows.next()) {
            var nameString = tablesRows.getString("TABLE_NAME");
            tableNames.add(nameString);
        }
        LOG.info("In schema '{}' found tables: {}", dbSchema, String.join(", ", tableNames));
        return tableNames;
    }

    public String getUniqueTableNameForBatch(BatchName batchName) throws SQLException {
        var metaData = getConnectionMeta();
        var matchingTables = metaData.getTables(dbSchema, null, batchName.asString(), null);
        int numTables = getNumFetchedRows(matchingTables);
        if (numTables != 1) {
            throw new RuntimeException("schema " + dbSchema + " does not have exactly 1 table with pattern " + batchName);
        }
        return matchingTables.getString("TABLE_NAME");
    }

    public List<DataFieldSpec> extractFieldSpecsFromBatchColumns(BatchName batchName) throws SQLException {
        var metaData = getConnectionMeta();
        Set<String> uniqueColumnsNames = collectBatchUniqueColumnsNames(metaData, batchName);
        var tableDbColumns = metaData.getColumns(dbSchema, null, batchName.asString(), null);
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

    private Set<String> collectBatchUniqueColumnsNames(DatabaseMetaData metaData, BatchName batchName) throws SQLException {
        var tableDbIndexes = metaData.getIndexInfo(dbSchema, null, batchName.asString(), true, true);
        Set<String> uniqueColumnsNames = new HashSet<>();
        while(tableDbIndexes.next()) {
            uniqueColumnsNames.add(tableDbIndexes.getString("COLUMN_NAME"));
        }
        return uniqueColumnsNames;
    }

    private DatabaseMetaData getConnectionMeta() throws SQLException {
        return dataSource.getConnection().getMetaData();
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
}
