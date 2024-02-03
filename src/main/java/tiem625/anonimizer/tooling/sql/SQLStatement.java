package tiem625.anonimizer.tooling.sql;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SQLStatement {

    private final String queryText;
    private final List<SQLStatementParameter> queryParameters;

    public static SQLStatement forSqlAndParams(String sql, Object... params) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("Query not provided");
        }

        return new SQLStatement(sql, inferQueryParams(params));
    }

    private SQLStatement(String queryText, List<SQLStatementParameter> queryParameters) {
        this.queryText = queryText;
        this.queryParameters = Optional.ofNullable(queryParameters).orElse(List.of());
    }

    public String queryText() {
        return queryText;
    }

    public List<SQLStatementParameter> queryParameters() {
        return queryParameters;
    }

    private static List<SQLStatementParameter> inferQueryParams(Object... params) {
        if (params == null || params.length == 0) {
            return List.of();
        }

        return Arrays.stream(params).map(SQLStatementParameter::inferFrom).toList();
    }
}
