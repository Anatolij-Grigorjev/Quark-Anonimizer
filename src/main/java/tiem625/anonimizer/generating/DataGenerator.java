package tiem625.anonimizer.generating;

import jakarta.annotation.Nonnull;
import tiem625.anonimizer.DataObject;
import tiem625.anonimizer.commonterms.*;

import java.util.List;

public interface DataGenerator {

    void generate(@Nonnull DataGenerationRules rules);

    List<DataObject> fetchSample(@Nonnull SampleSize desiredSize);

    class DataGenerationRules {
        BatchName batchName;
        List<DataFieldSpec> fieldSpecs;
        Amount amount;
    }

    class DataFieldSpec {
        FieldName fieldName;
        FieldType fieldType;
    }
}
