package tiem625.anonimizer.generating.db;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.generating.DataGenerator;

import java.util.List;

@ApplicationScoped
public class DatabaseDataGenerator implements DataGenerator {

    private DSLContext dbContext;

    @Inject public DatabaseDataGenerator(DSLContext dbContext) {
        this.dbContext = dbContext;
    }

    @Override
    public void generate(@Nonnull DataGenerationRules rules) {
        assertRulesPartsPresent(rules);

        dropBatchIfExists(rules.batchName());
        createEmptyBatch(rules.batchName(), rules.fieldSpecs());
        generateDataInBatch(rules.batchName(), rules.fieldSpecs(), rules.amount());
    }

    private void generateDataInBatch(BatchName batchName, List<DataFieldSpec> fieldSpecs, Amount amountToGenerate) {
        throw new UnsupportedOperationException("TODO");
    }

    private void createEmptyBatch(BatchName batchName, List<DataFieldSpec> dataFieldSpecs) {
        throw new UnsupportedOperationException("TODO");
    }

    private void dropBatchIfExists(BatchName batchName) {
        if (batchExists(batchName)) {
            dropBatch(batchName);
        }
    }

    private void dropBatch(BatchName batchName) {
        dbContext.dropTable(batchName.asString()).execute();
    }

    private boolean batchExists(BatchName batchName) {
        return !dbContext.meta().getTables(batchName.asString()).isEmpty();
    }

    private static void assertRulesPartsPresent(DataGenerationRules rules) {
        if (rules == null) {
            throw new IllegalArgumentException("Null rules received!");
        }
        if (rules.batchName() == null) {
            throw new IllegalArgumentException("Received gen rules without batch name");
        }
        if (rules.fieldSpecs() == null) {
            throw new IllegalArgumentException("Received gen rules without field specs");
        }
        if (rules.fieldSpecs().isEmpty()) {
            throw new IllegalArgumentException("Received gen rules with empty specs list");
        }
        if (rules.amount() == null) {
            throw new IllegalArgumentException("Received gen rules with null amount");
        }
    }
}
