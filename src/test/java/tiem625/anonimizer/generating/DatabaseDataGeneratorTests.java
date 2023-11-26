package tiem625.anonimizer.generating;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.generating.DataGenerator.DataGenerationRules;
import tiem625.anonimizer.testsupport.PrettyTestNames;
import tiem625.anonimizer.testsupport.TestData;

import java.util.List;

@PrettyTestNames
public class DatabaseDataGeneratorTests {

    private TestData data;
    private DataGenerator dataGeneratorImpl;

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




}
