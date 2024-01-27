package tiem625.anonimizer.tooling.jdbc;

import java.util.Objects;

import static tiem625.anonimizer.tooling.jdbc.SQLStatementParameterType.*;

public class SQLStatementParameter {

    public static final SQLStatementParameter NULL_PARAMETER = new SQLStatementParameter(NULL, null);

    private final SQLStatementParameterType type;
    private final Object value;

    public static SQLStatementParameter inferFrom(Object value) {
        if (value == null) {
            return NULL_PARAMETER;
        }
        return switch (value) {
            case String text -> new SQLStatementParameter(TEXT, text);
            case Number num -> new SQLStatementParameter(NUMBER, num);
            default -> throw new IllegalArgumentException("Cannot infer param of type " + value.getClass());
        };
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
}
