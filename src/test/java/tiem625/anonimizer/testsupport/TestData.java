package tiem625.anonimizer.testsupport;

import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldName;
import tiem625.anonimizer.commonterms.FieldType;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;
import tiem625.anonimizer.generating.DataGenerator.DataGenerationRules;
import tiem625.anonimizer.generating.FieldConstraint;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class TestData {

    private final Random RNG = new Random();

    public TestData() {
    }

    public final BatchName TST_BATCH = BatchName.of("tst_batch");
    public final FieldName ID = FieldName.of("id");
    public final FieldName EMAIL = FieldName.of("email");

    public DataFieldSpec fieldSpec(String name, FieldType type, FieldConstraint... constraints) {
        return new DataFieldSpec(FieldName.of(name), type, constraints);
    }

    public DataFieldSpec textFieldSpec(String name) {
        return fieldSpec(name, FieldType.TEXT);
    }

    public DataFieldSpec idFieldSpec(FieldConstraint... constraints) {
        return new DataFieldSpec(ID, FieldType.NUMBER, constraints);
    }

    public DataFieldSpec emailFieldSpec(FieldConstraint... constraints) {
        return new DataFieldSpec(EMAIL, FieldType.TEXT, constraints);
    }

    public Amount randomAmount() {
        return Amount.of(RNG.nextInt(1, 15));
    }

    public List<DataFieldSpec> textFieldSpecsList(String... fieldNames) {
        return textFieldSpecsList(Arrays.stream(fieldNames).toList());
    }

    public List<DataFieldSpec> textFieldSpecsList(List<String> fieldNames) {
        return fieldSpecsList(
                fieldNames.stream().collect(toMap(identity(), name -> FieldType.TEXT))
        );
    }

    public List<DataFieldSpec> fieldSpecsList(Map<String, FieldType> fieldSpecsMap) {
        return fieldSpecsMap.entrySet().stream()
                .map(entry -> fieldSpec(entry.getKey(), entry.getValue()))
                .toList();
    }

    public DataGenerationRules dataGenerationRules(Map<String, FieldType> fieldSpecsMap) {
        return dataGenerationRules(fieldSpecsList(fieldSpecsMap));
    }

    public DataGenerationRules dataGenerationRules(List<DataFieldSpec> fieldSpecList) {
        return dataGenerationRules(TST_BATCH, fieldSpecList, randomAmount());
    }

    public DataGenerationRules dataGenerationRules(String batchName, Map<String, FieldType> fieldSpecsMap, int amount) {
        return dataGenerationRules(BatchName.of(batchName), fieldSpecsMap, Amount.of(amount));
    }

    public DataGenerationRules dataGenerationRules(BatchName batchName, Map<String, FieldType> fieldSpecsMap, Amount amount) {
        return dataGenerationRules(batchName, fieldSpecsList(fieldSpecsMap), amount);
    }

    public DataGenerationRules dataGenerationRules(BatchName batchName, List<DataFieldSpec> fieldSpecs, Amount amount) {
        return new DataGenerationRules(batchName, fieldSpecs, amount);
    }

    public List<DataFieldSpec> idEmailFieldsSpecs() {
        return List.of(
            idFieldSpec(FieldConstraint.NOT_NULL, FieldConstraint.UNIQUE),
            emailFieldSpec(FieldConstraint.UNIQUE)
        );
    }
}
