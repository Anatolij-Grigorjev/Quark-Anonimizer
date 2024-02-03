package tiem625.anonimizer.tooling.sql.jdbc;

import tiem625.anonimizer.commonterms.FieldType;

import java.util.HashMap;
import java.util.Map;

import static tiem625.anonimizer.tooling.validation.Parameters.assertParamPresent;

class SQLStatementGeneratorConfig {

    static final Integer DEFAULT_TEXT_LENGTH = 250;
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
            fieldToSQLMap.put(FieldType.TEXT, "varchar(" + DEFAULT_TEXT_LENGTH + ")");
            fieldToSQLMap.put(FieldType.NUMBER, "int");
        }
    }
}
