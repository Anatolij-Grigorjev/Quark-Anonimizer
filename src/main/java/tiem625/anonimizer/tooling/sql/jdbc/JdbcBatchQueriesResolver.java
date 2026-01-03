package tiem625.anonimizer.tooling.sql.jdbc;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiem625.anonimizer.commonterms.*;
import tiem625.anonimizer.generating.DataGenerator;
import tiem625.anonimizer.tooling.sql.BatchQueriesResolver;
import tiem625.anonimizer.tooling.sql.SQLStatement;
import tiem625.anonimizer.tooling.streams.Wrappers.ThrowsCheckedFunc;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class JdbcBatchQueriesResolver implements BatchQueriesResolver {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcBatchQueriesResolver.class);

    private final DataSource dataSource;
    private final SQLStatementGenerator sqlStatements;
    private final DBMetadataReader dbMetadataReader;

    @Inject
    public JdbcBatchQueriesResolver(
            DataSource jdbcDataSource,
            @ConfigProperty(name = "anonimizer.data.schema") String dbSchema
    ) {
        this.dataSource = jdbcDataSource;
        this.sqlStatements = new SQLStatementGenerator(SQLStatementGeneratorConfig.builder().build());
        this.dbMetadataReader = new DBMetadataReader(dataSource, dbSchema);
    }

    @Override
    public void executeBatchCreationQuery(BatchName batchName, List<DataGenerator.DataFieldSpec> fieldSpecs) {
        var statement = sqlStatements.createTableStatement(batchName, fieldSpecs);
        try {
            processJdbcStatement(statement);
        } catch (RuntimeException ex) {
            var sqlEx = ex.getCause();
            if (sqlEx instanceof SQLException && ((SQLException) sqlEx).getSQLState().startsWith("42")) {
                throw new IllegalStateException(String.format("Table %s already exists!", batchName), ex);
            } else {
                throw ex;
            }
        }
    }

    @Override
    public boolean executeCheckBatchExistsQuery(BatchName batchName) {
        var statement = sqlStatements.checkTableExistsStatement(batchName);
        return processJdbcStatement(statement, preparedStatement -> {
            try {
                preparedStatement.execute();
                return true;
            } catch (SQLException ex) {
                return false;
            }
        });
    }

    @Override
    public Amount executeFetchBatchSizeQuery(BatchName batchName) {
        var statement = sqlStatements.getTableSizeStatement(batchName);
        try {
            return processJdbcStatement(statement, preparedStatement -> {
                var results = preparedStatement.executeQuery();
                var canReadCount = results.next();
                if (canReadCount) {
                    return Amount.of(results.getInt(1));
                } else {
                    return Amount.NONE;
                }
            });
        } catch (RuntimeException ex) {
            var sqlEx = ex.getCause();
            if (sqlEx instanceof SQLException && ((SQLException) sqlEx).getSQLState().startsWith("42")) {
                throw new IllegalStateException(String.format("Table %s already exists!", batchName), ex);
            } else {
                throw ex;
            }
        }
    }

    @Override
    public List<DataObject> executeFetchSomeBatchObjectsQuery(BatchName batchName, Amount amount) {
        var statement = sqlStatements.fetchTableRowsStatement(batchName, amount);
        return processJdbcStatement(statement, preparedStatement -> {
            var batchFieldSpecs = dbMetadataReader.extractFieldSpecsFromBatchColumns(batchName);
            preparedStatement.setInt(1, (Integer) statement.queryParameters().get(0).value());

            var resultsCursor = preparedStatement.executeQuery();
            var batchObjects = new ArrayList<DataObject>();
            while(resultsCursor.next()) {
                batchObjects.add(collectRowDataObject(resultsCursor, batchFieldSpecs));
            }
            return batchObjects;
        });
    }

    private <T> T processJdbcStatement(SQLStatement sqlStatement, JdbcActions<T> actions) {
        Objects.requireNonNull(actions);
        LOG.info(sqlStatement.asSqlString());

        try (var dbConnection = dataSource.getConnection()) {
            try (var jdbcStatement = dbConnection.prepareStatement(sqlStatement.queryText())) {
                return actions.useStatement(jdbcStatement);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } // close jdbcStatement
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } // close dbConnection
    }

    private DataObject collectRowDataObject(ResultSet tableData, List<DataGenerator.DataFieldSpec> fieldSpecs) throws SQLException {
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

    private <T> void processJdbcStatement(SQLStatement statement) {
        processJdbcStatement(statement, PreparedStatement::execute);
    }
}
