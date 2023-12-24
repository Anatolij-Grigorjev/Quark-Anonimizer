package tiem625.anonimizer.testsupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import tiem625.anonimizer.commonterms.*;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;
import tiem625.anonimizer.generating.FieldConstraint;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class TestDbContext {

    @Inject
    DSLContext db;

    private final TestData data = new TestData();

    public boolean batchExists(BatchName table) {
        return db.meta().getTables(table.asString()).size() > 0;
    }

    public Amount getBatchRecordsCount(BatchName tableName) {
        return Amount.of(db.fetchCount(DSL.table(tableName.asString())));
    }

    public List<DataFieldSpec> getBatchFieldSpecs(BatchName batchName) {
        var table = db.meta().getTables(batchName.asString()).get(0);
        return extractTableFieldSpecs(table);
    }

    public List<DataObject> getAllBatchValues(BatchName batchName) {
        var result = db.select(DSL.asterisk()).from(DSL.table(batchName.asString())).fetch();

        return result.stream().map(this::recordAsData).toList();
    }

    public void createBatch(BatchName batchName) {
        createBatch(batchName, List.of(data.idFieldSpec(), data.emailFieldSpec()));
    }

    public void createBatch(BatchName batchName, List<DataFieldSpec> fields) {
        var createTable = db.createTable(batchName.asString());
        for (var field: fields) {
            createTable = createTable.column(
                    field.fieldName().asString(),
                    resolveDataType(field.fieldType(), field.fieldConstraints().nullable())
            );
            if (field.fieldConstraints().unique()) {
                createTable = createTable.unique(field.fieldName().asString());
            }
        }
        createTable.execute();
    }

    private DataType<?> resolveDataType(FieldType fieldType, boolean isNullable) {
        switch (fieldType) {
            case TEXT -> {
                return SQLDataType.LONGNVARCHAR(50_000).nullable(isNullable);
            }
            case NUMBER -> {
                return SQLDataType.NUMERIC(20).nullable(isNullable);
            }
            default -> throw new UnsupportedOperationException("Cannot produce SQLDataType for " + fieldType);
        }
    }

    private List<DataFieldSpec> extractTableFieldSpecs(Table<?> dbTable) {
        var uniqueConstraints = dbTable.getUniqueKeys();
        return dbTable.fieldStream().map(dbField -> {
            var fieldConstraints = resolveFieldConstraints(dbField, uniqueConstraints);
            return new DataFieldSpec(FieldName.of(dbField.getName()), resolveFieldType(dbField), fieldConstraints);
        }).toList();
    }

    private List<FieldConstraint> resolveFieldConstraints(Field<?> dbField,
                                                          List<? extends UniqueKey<?>> tableUniqueConstrains) {
        var constraints = new ArrayList<FieldConstraint>();
        if (!fieldIsNullable(dbField)) {
            constraints.add(FieldConstraint.NOT_NULL);
        }
        if (fieldIsUniqueInTable(dbField, tableUniqueConstrains)) {
            constraints.add(FieldConstraint.UNIQUE);
        }

        return constraints;
    }

    private boolean fieldIsNullable(Field<?> dbField) {
        return dbField.getDataType().nullable();
    }

    private boolean fieldIsUniqueInTable(Field<?> dbField, List<? extends UniqueKey<?>> tableUniqueConstrains) {
        return tableUniqueConstrains.stream()
                .filter(uniqueKey -> uniqueKey.getFields().size() == 1)
                .anyMatch(uniqueKey -> {
                    var uniqueField = uniqueKey.getFields().get(0);
                    return Objects.equals(uniqueField.getName(), dbField.getName())
                            && Objects.equals(resolveFieldType(uniqueField), resolveFieldType(dbField));
                });
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
