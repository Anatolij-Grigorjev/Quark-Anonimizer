package tiem625.anonimizer.generating;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.commonterms.*;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;
import tiem625.anonimizer.generating.DataGenerator.DataGenerationRules;
import tiem625.anonimizer.testsupport.PrettyTestNames;
import tiem625.anonimizer.testsupport.TestData;
import tiem625.anonimizer.testsupport.TestDbContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static tiem625.anonimizer.generating.FieldConstraint.NOT_NULL;
import static tiem625.anonimizer.generating.FieldConstraint.UNIQUE;

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
    void spec_creates_table_with_correct_field_names_types_and_constraints() {
        var idFieldSpec = data.fieldSpec("id", FieldType.NUMBER, UNIQUE, NOT_NULL);
        var usernameFieldSpec = data.fieldSpec("username", FieldType.TEXT, UNIQUE);
        var emailFieldSpec = data.fieldSpec("email", FieldType.TEXT);
        var rankFieldSpec = data.fieldSpec("rank", FieldType.NUMBER, NOT_NULL);
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
        Assertions.assertEquals(fieldSpecs.size(), batchFields.size());
        IntStream.range(0, fieldSpecs.size()).forEachOrdered(idx -> {
            DataFieldSpec expectedFieldSpec = fieldSpecs.get(idx);
            DataFieldSpec dbFieldSpec = batchFields.get(idx);
            Assertions.assertEquals(expectedFieldSpec.fieldName(), dbFieldSpec.fieldName());
            Assertions.assertEquals(expectedFieldSpec.fieldType(), dbFieldSpec.fieldType());
            Assertions.assertEquals(expectedFieldSpec.fieldConstraints(), dbFieldSpec.fieldConstraints());
        });
    }

    @Test
    void spec_creates_correct_number_of_typed_records() {
        var idFieldSpec = data.fieldSpec("id", FieldType.NUMBER);
        var userNameFieldSpec = data.fieldSpec("username", FieldType.TEXT);
        var amount = Amount.of(15);
        var rules = data.dataGenerationRules(data.TST_BATCH, List.of(idFieldSpec, userNameFieldSpec), amount);

        dataGeneratorImpl.generate(rules);

        Assertions.assertEquals(db.getBatchRecordsCount(rules.batchName()), amount);
    }

    @Test
    void spec_creates_different_fake_values() {
        var idFieldSpec = data.fieldSpec("id", FieldType.NUMBER);
        var usernameFieldSpec = data.fieldSpec("username", FieldType.TEXT);
        var amount = Amount.of(15);
        var rules = data.dataGenerationRules(data.TST_BATCH, List.of(idFieldSpec, usernameFieldSpec), amount);

        dataGeneratorImpl.generate(rules);

        List<DataObject> batchValues = db.getAllBatchValues(rules.batchName());

        var differentIdValues = batchValues.stream()
                .map(data -> data.getValue(idFieldSpec.fieldName()))
                .map(FieldValue::content)
                .collect(Collectors.toSet());
        var differentUsernameValues = batchValues.stream()
                .map(data -> data.getValue(usernameFieldSpec.fieldName()))
                .map(FieldValue::content)
                .collect(Collectors.toSet());
        Assertions.assertTrue(differentIdValues.size() > 1);
        Assertions.assertTrue(differentUsernameValues.size() > 1);
    }
}
