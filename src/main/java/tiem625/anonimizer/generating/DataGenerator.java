package tiem625.anonimizer.generating;

import jakarta.annotation.Nonnull;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldName;
import tiem625.anonimizer.commonterms.FieldType;

import java.util.List;

public interface DataGenerator {

    void generate(@Nonnull DataGenerationRules rules);

    record DataGenerationRules(
            BatchName batchName,
            List<DataFieldSpec> fieldSpecs,
            Amount amount
    ) {}

    record DataFieldSpec(
            FieldName fieldName,
            FieldType fieldType
    ) {}
}
