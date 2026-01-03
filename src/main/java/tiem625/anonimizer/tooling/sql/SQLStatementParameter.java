package tiem625.anonimizer.tooling.sql;

import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldName;

import java.util.Objects;

import static tiem625.anonimizer.tooling.sql.SQLStatementParameterType.*;

public class SQLStatementParameter {

    public static final SQLStatementParameter NULL_PARAMETER = new SQLStatementParameter(NULL, null);

    private final SQLStatementParameterType type;
    private final Object value;

    public static SQLStatementParameter inferFrom(Object value) {
        if (value == null) {
            return NULL_PARAMETER;
        }
        return pickInferenceRule(value);
    }

    private SQLStatementParameter(SQLStatementParameterType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public SQLStatementParameterType type() {
        return type;
    }

    public Object value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLStatementParameter that = (SQLStatementParameter) o;
        return type == that.type && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "(" + type + " " + value + ")";
    }

    private static SQLStatementParameter pickInferenceRule(Object value) {
        if (value instanceof String text) {
            return new SQLStatementParameter(TEXT, text);
        } else if (value instanceof Number num) {
            return new SQLStatementParameter(NUMBER, num);
        } else if (value instanceof BatchName batchName) {
            return new SQLStatementParameter(TABLE_NAME, batchName.asString());
        } else if (value instanceof Amount amount) {
            return pickInferenceRule(amount.asNumber());
        } else if (value instanceof FieldName fieldName) {
            return pickInferenceRule(fieldName.asString());
        } else {
            throw new IllegalArgumentException("Cannot infer param of type " + value.getClass());
        }
    }
}
