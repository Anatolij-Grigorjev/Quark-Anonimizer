package tiem625.anonimizer.testsupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import tiem625.anonimizer.DataObject;
import tiem625.anonimizer.commonterms.*;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<DataObject> getAllBatchValues(BatchName batchName) {
        var result = db.select(DSL.asterisk()).from(DSL.table(batchName.asString())).fetch();

        return result.stream().map(this::recordAsData).toList();
    }

    private DataObject recordAsData(Record dbRecord) {
        var fieldsArray = dbRecord.fields();
        var dataObjectFieldsMap = Arrays.stream(fieldsArray).map(field -> recordFieldAsData(field, dbRecord))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return DataObject.withFields(dataObjectFieldsMap);
    }

    private Map.Entry<FieldName, FieldValue> recordFieldAsData(Field<?> dbField, Record ownerRecord) {
        var value = dbField.get(ownerRecord);
        var name = dbField.getName();
        return Map.entry(FieldName.of(name), FieldValue.of(resolveFieldType(dbField), value));
    }

    private FieldType resolveFieldType(Field<?> dbField) {
        return resolveFieldType(dbField.getType());
    }

    private FieldType resolveFieldType(Class<?> fieldClazz) {
        if (fieldClazz.isAssignableFrom(String.class)) {
            return FieldType.TEXT;
        }
        if (fieldClazz.isAssignableFrom(Number.class)) {
            return FieldType.NUMBER;
        }
        throw new IllegalArgumentException("Cannot convert field class " + fieldClazz.getName() + " to FieldType");
    }
}
