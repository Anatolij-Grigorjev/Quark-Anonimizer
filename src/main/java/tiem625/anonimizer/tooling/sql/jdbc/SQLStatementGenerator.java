package tiem625.anonimizer.tooling.sql.jdbc;

import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;
import tiem625.anonimizer.tooling.sql.SQLStatement;

import java.util.List;
import java.util.stream.Collectors;

import static tiem625.anonimizer.tooling.validation.Parameters.assertParamCondition;
import static tiem625.anonimizer.tooling.validation.Parameters.assertParamPresent;

class SQLStatementGenerator {

    private final SQLStatementGeneratorConfig config;

    public SQLStatementGenerator(SQLStatementGeneratorConfig config) {
        this.config = config;
    }


    public SQLStatement createTableStatement(BatchName batchName, List<DataFieldSpec> fieldSpecs) {
        assertParamPresent(batchName, "batchName is null");
        assertParamPresent(fieldSpecs, "fieldSpecs was null");
        assertParamCondition(fieldSpecs, specsList -> !specsList.isEmpty(), "specs list was empty");

        StringBuilder statementBuilder = new StringBuilder();
        appendCreateTableHeader(statementBuilder, batchName);
        appendColumnDefinitionLines(statementBuilder, fieldSpecs);
        appendCreateTableFooter(statementBuilder);
        String preparedStatementText = statementBuilder.toString();

        return SQLStatement.forSqlAndParams(preparedStatementText);
    }


    public SQLStatement checkTableExistsStatement(BatchName batchName) {
        assertParamPresent(batchName, "got null batchName");

        return SQLStatement.forSqlAndParams(String.format("SELECT 1 FROM `%s`;", batchName));
    }


    public SQLStatement getTableSizeStatement(BatchName batchName) {
        assertParamPresent(batchName, "got null batchName");

        return SQLStatement.forSqlAndParams(String.format("SELECT COUNT(*) FROM `%s`;", batchName));
    }


    public SQLStatement fetchTableRowsStatement(BatchName batchName, Amount amount) {
        assertParamPresent(batchName, "got null batchName");
        assertParamPresent(amount, "got null amount");
        assertParamCondition(amount, thisAmount -> thisAmount.asNumber() > 0, "passed amount is not positive");

        return SQLStatement.forSqlAndParams(String.format("SELECT * FROM `%s` LIMIT ?;", batchName), amount);
    }

    private void appendCreateTableHeader(StringBuilder appender, BatchName batchName) {
        appender.append(String.format("CREATE TABLE `%s` (\n", batchName));
    }

    private void appendColumnDefinitionLines(StringBuilder appender, List<DataFieldSpec> fieldSpecs) {
        var columnDefinitions = fieldSpecs.stream()
                .map(this::columnDefinitionLine)
                .collect(Collectors.joining(",\n", "", ""));
        appender.append(columnDefinitions);
    }

    private String columnDefinitionLine(DataFieldSpec fieldSpec) {
        return "\t%s %s%s%s".formatted(
                fieldSpec.fieldName(),
                config.getSQLTypeFor(fieldSpec.fieldType()),
                fieldSpec.fieldConstraints().nullable() ? "" : " NOT NULL",
                fieldSpec.fieldConstraints().unique() ? " UNIQUE" : ""
        );
    }

    private void appendCreateTableFooter(StringBuilder appender) {
        appender.append("\n);");
    }
}
