package tiem625.anonimizer.tooling.jdbc.local;

import tiem625.anonimizer.commonterms.FieldType;

import java.util.HashMap;
import java.util.Map;

import static tiem625.anonimizer.tooling.validation.Parameters.assertParamPresent;

public class SQLStatementGeneratorConfig {

    private final Map<FieldType, String> fieldTypesToSQLMapping;

    private SQLStatementGeneratorConfig(Map<FieldType, String> fieldTypesToSQLMapping) {
        this.fieldTypesToSQLMapping = fieldTypesToSQLMapping;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getSQLTypeFor(FieldType fieldType) {
        return fieldTypesToSQLMapping.get(fieldType);
    }

    public static class Builder {

        private final Map<FieldType, String> fieldToSQLMap;

        private Builder() {
            this.fieldToSQLMap = new HashMap<>();
            addDefaults();
        }

        public Builder sqlTypeForFieldType(String sqlType, FieldType fieldType) {
            assertParamPresent(sqlType, "Got null sqlType");
            assertParamPresent(fieldType, "Got null fieldType");
            fieldToSQLMap.put(fieldType, sqlType);
            return this;
        }

        public SQLStatementGeneratorConfig build() {
            return new SQLStatementGeneratorConfig(fieldToSQLMap);
        }

        private void addDefaults() {
            fieldToSQLMap.put(FieldType.TEXT, "varchar(250)");
            fieldToSQLMap.put(FieldType.NUMBER, "int");
        }
    }
}
