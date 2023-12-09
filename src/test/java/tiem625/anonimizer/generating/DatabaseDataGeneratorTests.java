package tiem625.anonimizer.generating;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldType;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;
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
        DataGenerationRules specNoBatchName = new DataGenerationRules(null, List.of(), Amount.NONE);
        DataGenerationRules specNoFieldsList = new DataGenerationRules(data.TST_BATCH, null, Amount.NONE);
        DataGenerationRules specFieldsListEmpty = new DataGenerationRules(data.TST_BATCH, List.of(), Amount.NONE);
        DataGenerationRules specAmountNull = new DataGenerationRules(data.TST_BATCH, List.of(data.textFieldSpec("field1")), null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> dataGeneratorImpl.generate(specNull));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataGeneratorImpl.generate(specNoBatchName));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataGeneratorImpl.generate(specNoFieldsList));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataGeneratorImpl.generate(specFieldsListEmpty));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataGeneratorImpl.generate(specAmountNull));
    }

    @Test
    void spec_with_no_amount_creates_empty_table() {
        var batch = data.TST_BATCH;
        var spec = data.dataGenerationRules(batch, data.textFieldSpecsList("field1"), Amount.NONE);

        dataGeneratorImpl.generate(spec);

        Assertions.assertEquals(true, db.batchExists(batch));
        Assertions.assertEquals(0, db.getBatchRecordsCount(batch).asNumber());
    }

    @Test
    void spec_creates_table_with_correct_field_names_and_types() {
        var idFieldSpec = data.fieldSpec("id", FieldType.NUMBER);
        var usernameFieldSpec = data.fieldSpec("username", FieldType.TEXT);
        var emailFieldSpec = data.fieldSpec("email", FieldType.TEXT);
        var rankFieldSpec = data.fieldSpec("rank", FieldType.NUMBER);
        var fieldSpecs = List.of(
            idFieldSpec,
            usernameFieldSpec,
            emailFieldSpec,
            rankFieldSpec
        );
        var dataGenRules = data.dataGenerationRules(fieldSpecs);
        BatchName batchName = dataGenRules.batchName();

        dataGeneratorImpl.generate(dataGenRules);

        Assertions.assertTrue(db.batchExists(batchName));
        List<DataFieldSpec> batchFields = db.getBatchFieldSpecs(batchName);
        Assertions.assertEquals(fieldSpecs, batchFields);
    }
}
