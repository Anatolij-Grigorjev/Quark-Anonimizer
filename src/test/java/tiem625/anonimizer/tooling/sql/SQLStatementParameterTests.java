package tiem625.anonimizer.tooling.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.testsupport.PrettyTestNames;
import tiem625.anonimizer.testsupport.TestData;

import static tiem625.anonimizer.tooling.sql.SQLStatementParameter.NULL_PARAMETER;
import static tiem625.anonimizer.tooling.sql.SQLStatementParameterType.NUMBER;
import static tiem625.anonimizer.tooling.sql.SQLStatementParameterType.TEXT;

@PrettyTestNames
public class SQLStatementParameterTests {

    private final TestData data = new TestData();

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

    @Test
    void batch_name_creates_text_param() {
        var param = SQLStatementParameter.inferFrom(data.TST_BATCH);
        Assertions.assertEquals(TEXT, param.type());
        Assertions.assertEquals(data.TST_BATCH.asString(), param.value());
    }

    @Test
    void field_name_creates_text_param() {
        var param = SQLStatementParameter.inferFrom(data.ID);
        Assertions.assertEquals(TEXT, param.type());
        Assertions.assertEquals(data.ID.asString(), param.value());
    }

    @Test
    void amount_creates_num_param() {
        var param = SQLStatementParameter.inferFrom(Amount.NONE);
        Assertions.assertEquals(NUMBER, param.type());
        Assertions.assertEquals(0, param.value());
    }
}
