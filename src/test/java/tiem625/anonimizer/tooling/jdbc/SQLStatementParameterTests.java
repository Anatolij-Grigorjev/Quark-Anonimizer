package tiem625.anonimizer.tooling.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.testsupport.PrettyTestNames;

import static tiem625.anonimizer.tooling.jdbc.SQLStatementParameter.NULL_PARAMETER;
import static tiem625.anonimizer.tooling.jdbc.SQLStatementParameterType.NUMBER;
import static tiem625.anonimizer.tooling.jdbc.SQLStatementParameterType.TEXT;

@PrettyTestNames
public class SQLStatementParameterTests {

    @Test
    void null_object_creates_null_param() {
        Assertions.assertEquals(NULL_PARAMETER, SQLStatementParameter.inferFrom(null));
    }

    @Test
    void text_object_creates_text_param() {
        var param = SQLStatementParameter.inferFrom("text");
        Assertions.assertEquals(TEXT, param.type());
        Assertions.assertEquals("text", param.value());
    }

    @Test
    void numeric_object_creates_numeric_param() {
        var param = SQLStatementParameter.inferFrom(5);
        Assertions.assertEquals(NUMBER, param.type());
        Assertions.assertEquals(5, param.value());
    }
}
