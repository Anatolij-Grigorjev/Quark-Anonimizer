package tiem625.anonimizer.testsupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldName;
import tiem625.anonimizer.commonterms.FieldType;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class TestDbContext {

    @Inject
    DSLContext db;

    public boolean batchExists(BatchName table) {
        try {
            db.selectOne().from(table.asString()).fetchOne();
            return true;
        } catch (DataAccessException sqlError) {
            sqlError.printStackTrace();
            return false;
        }
    }

    public Amount getBatchRecordsCount(BatchName tableName) {
        return Amount.of(db.fetchCount(DSL.table(tableName.asString())));
    }

    public List<DataFieldSpec> getBatchFieldSpecs(BatchName batchName) {
        var result = db.select(DSL.asterisk())
                .from(DSL.table(batchName.asString()))
                .limit(0)
                .fetch();
        return Arrays.stream(result.recordType().fields())
                .map(field -> new DataFieldSpec(FieldName.of(field.getName()), resolveFieldType(field)))
                .toList();
    }

    private FieldType resolveFieldType(Field<?> dbField) {
        var fieldClazz = dbField.getType();
        if (fieldClazz.isAssignableFrom(String.class)) {
            return FieldType.TEXT;
        }
        if (fieldClazz.isAssignableFrom(Number.class)) {
            return FieldType.NUMBER;
        }
        throw new IllegalArgumentException("Cannot convert field class " + fieldClazz.getName() + " to FieldType");
    }
}
