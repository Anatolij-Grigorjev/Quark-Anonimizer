package tiem625.anonimizer.generating;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.generating.DataGenerator.DataGenerationRules;
import tiem625.anonimizer.testsupport.PrettyTestNames;
import tiem625.anonimizer.testsupport.TestData;
import tiem625.anonimizer.testsupport.TestDbContext;

import java.util.List;

@PrettyTestNames
@QuarkusTest
public class DatabaseDataGeneratorTests {

    TestData data;

    private DataGenerator dataGeneratorImpl;

    @Inject
    TestDbContext db;

    @BeforeEach
    void setupService() {
        data = new TestData();
        dataGeneratorImpl = null;
    }

    @Test
    void incomplete_spec_causes_exception() {
        DataGenerationRules specNull = null;
        var specNoBatchName = new DataGenerationRules(null, List.of(), Amount.NONE);
        var specNoFieldsList = new DataGenerationRules(data.TST_BATCH, null, Amount.NONE);
        var specFieldsListEmpty = new DataGenerationRules(data.TST_BATCH, List.of(), Amount.NONE);
        var specAmountNull = new DataGenerationRules(data.TST_BATCH, List.of(data.textFieldSpec("field1")), null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> dataGeneratorImpl.generate(specNull));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataGeneratorImpl.generate(specNoBatchName));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataGeneratorImpl.generate(specNoFieldsList));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataGeneratorImpl.generate(specFieldsListEmpty));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataGeneratorImpl.generate(specAmountNull));
    }

    @Test
    void spec_with_no_amount_creates_empty_table() {
        var batch = data.TST_BATCH;
        var spec = new DataGenerationRules(batch, List.of(data.textFieldSpec("field1")), Amount.NONE);

        dataGeneratorImpl.generate(spec);

        Assertions.assertEquals(true, db.batchExists(batch));
        Assertions.assertEquals(0, db.getBatchRecordsCount(batch).asNumber());
    }
}
