package tiem625.anonimizer.testsupport;

import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldName;
import tiem625.anonimizer.commonterms.FieldType;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;

public class TestData {

    public TestData() {
    }

    public final BatchName TST_BATCH = BatchName.of("tst_batch");

    public DataFieldSpec textFieldSpec(String name) {
        return new DataFieldSpec(FieldName.of(name), FieldType.TEXT);
    }
}
