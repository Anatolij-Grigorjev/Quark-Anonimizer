package tiem625.anonimizer.tooling.jdbc;

import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldName;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;

import java.util.List;
import java.util.stream.Collectors;

import static tiem625.anonimizer.tooling.validation.Parameters.assertParamCondition;
import static tiem625.anonimizer.tooling.validation.Parameters.assertParamPresent;

public class SQLStatementGenerator {


    private final SQLStatementGeneratorConfig config;

    public SQLStatementGenerator(SQLStatementGeneratorConfig config) {
        this.config = config;
    }


    public String createTableStatement(BatchName batchName, List<DataFieldSpec> fieldSpecs) {
        assertParamPresent(batchName, "batchName is null");
        assertParamPresent(fieldSpecs, "fieldSpecs was null");
        assertParamCondition(fieldSpecs, specsList -> !specsList.isEmpty(), "specs list was empty");

        StringBuilder statementBuilder = new StringBuilder("CREATE TABLE %s (\n".formatted(batchName));
        appendColumnDefinitionLines(statementBuilder, fieldSpecs);
        statementBuilder.append("\n);");
        return statementBuilder.toString();
    }


    public String checkTableExistsStatement(BatchName batchName) {
        assertParamPresent(batchName, "got null batchName");

        return "SELECT 1 FROM %s;".formatted(batchName);
    }


    public String tableSizeStatement(BatchName batchName) {
        assertParamPresent(batchName, "got null batchName");

        return "SELECT COUNT(*) FROM %s;".formatted(batchName);
    }


    public String fetchTableRowsStatement(BatchName batchName, Amount amount) {
        assertParamPresent(batchName, "got null batchName");
        assertParamPresent(amount, "got null amount");
        assertParamCondition(amount, thisAmount -> thisAmount.asNumber() > 0, "passed amount is not positive");

        return "SELECT * FROM %s LIMIT %s;".formatted(batchName, amount);
    }

    private void appendColumnDefinitionLines(StringBuilder appender, List<DataFieldSpec> fieldSpecs) {
        var columnDefinitions = fieldSpecs.stream()
                .map(this::columnDefinitionLine)
                .collect(Collectors.joining(",\n", "", ""));
        appender.append(columnDefinitions);

        var uniqueFieldsNames = fieldSpecs.stream()
                .filter(fieldSpec -> fieldSpec.fieldConstraints().unique())
                .map(DataFieldSpec::fieldName)
                .toList();
        if (!uniqueFieldsNames.isEmpty()) {
            var uniqueFieldNamesString = uniqueFieldsNames.stream().map(FieldName::asString).collect(Collectors.joining(", "));
            appender.append(",\n");
            appender.append("\tUNIQUE (%s)".formatted(uniqueFieldNamesString));
        }
    }

    private String columnDefinitionLine(DataFieldSpec fieldSpec) {
        return "\t%s %s%s".formatted(
                fieldSpec.fieldName(),
                config.getSQLTypeFor(fieldSpec.fieldType()),
                fieldSpec.fieldConstraints().nullable() ? "" : " NOT NULL"
        );
    }
}
