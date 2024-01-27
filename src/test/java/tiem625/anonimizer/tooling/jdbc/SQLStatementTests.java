package tiem625.anonimizer.tooling.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.testsupport.PrettyTestNames;

@PrettyTestNames
public class SQLStatementTests {

    @Test
    void throws_when_no_sql_text_provided() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> SQLStatement.forSqlAndParams(null));
    }

    @Test
    void statement_without_params_has_empty_list() {
        var statement = SQLStatement.forSqlAndParams("sql");
        Assertions.assertNotNull(statement.queryParameters());
        Assertions.assertEquals(0, statement.queryParameters().size());
    }

    @Test
    void statement_with_params_preserves_order() {
        var statement = SQLStatement.forSqlAndParams("sql", "name", 78);
        Assertions.assertEquals(2, statement.queryParameters().size());
        Assertions.assertEquals("name", statement.queryParameters().get(0).value());
        Assertions.assertEquals(78, statement.queryParameters().get(1).value());
    }
}
