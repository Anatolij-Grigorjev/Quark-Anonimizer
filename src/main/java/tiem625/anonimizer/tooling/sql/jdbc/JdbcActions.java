package tiem625.anonimizer.tooling.sql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcActions<T> {

    T useStatement(PreparedStatement statement) throws SQLException;
}
