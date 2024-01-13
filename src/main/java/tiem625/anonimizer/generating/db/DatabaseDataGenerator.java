package tiem625.anonimizer.generating.db;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.CreateTableElementListStep;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.impl.SQLDataType;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldType;
import tiem625.anonimizer.generating.DataGenerator;

import java.util.List;

import static tiem625.anonimizer.streams.StreamOperators.pickFirst;

@ApplicationScoped
public class DatabaseDataGenerator implements DataGenerator {

    public static final int DEFAULT_VARCHAR_LENGTH = 50_000;
    public static final int DEFAULT_NUMERIC_PRECISION = 20;
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
        var buildTableStatement = dbContext.createTable(batchName.asString());

        var createBatchStatement = dataFieldSpecs.stream()
                .reduce(buildTableStatement, this::addFieldSpecColumn, pickFirst());

        createBatchStatement.execute();
    }

    private CreateTableElementListStep addFieldSpecColumn(CreateTableElementListStep statementBuilder, DataFieldSpec columnSpecs) {
        return statementBuilder.column(
                columnSpecs.fieldName().asString(),
                resolveDataType(columnSpecs.fieldType(), columnSpecs.fieldConstraints())
        );
    }

    private DataType<?> resolveDataType(FieldType fieldType, FieldConstraints constraints) {
        boolean isNullable = resolveIsNullable(constraints);
        switch (fieldType) {
            case TEXT -> {
                return SQLDataType.LONGNVARCHAR(DEFAULT_VARCHAR_LENGTH).nullable(isNullable);
            }
            case NUMBER -> {
                return SQLDataType.NUMERIC(DEFAULT_NUMERIC_PRECISION).nullable(isNullable);
            }
            default -> throw new UnsupportedOperationException("Cannot produce SQLDataType for " + fieldType);
        }
    }

    private boolean resolveIsNullable(FieldConstraints constraints) {
        if (constraints == null) {
            return true;
        }
        return constraints.nullable();
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
