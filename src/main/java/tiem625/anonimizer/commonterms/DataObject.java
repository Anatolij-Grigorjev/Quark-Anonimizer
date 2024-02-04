package tiem625.anonimizer.commonterms;

import java.util.*;

public class DataObject {

    public static DataObject withFields(Map<FieldName, FieldValue> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Data Object must have some fields");
        }

        return new DataObject(fields);
    }

    private final Map<FieldName, FieldValue> fields;

    private DataObject(Map<FieldName, FieldValue> fields) {
        this.fields = fields;
    }

    public FieldValue getValue(FieldName atName) {
        var presentFieldName = Optional.ofNullable(atName)
                .filter(fields::containsKey)
                .orElseThrow(() -> new NoSuchElementException("No field in object with name " + atName));
        return fields.get(presentFieldName);
    }

    public Set<FieldName> fieldNames() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataObject that = (DataObject) o;
        return Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }
}
