package tiem625.anonimizer.commonterms;

import java.util.Objects;

public class FieldValue {

    public static FieldValue of(FieldType type, Object content) {
        throw new UnsupportedOperationException("TODO");
    }

    public static FieldValue ofTyped(Object content) {
        throw new UnsupportedOperationException("TODO");
    }

    private final FieldType type;
    private final Object content;

    private FieldValue(FieldType type, Object content) {
        this.type = type;
        this.content = content;
    }

    public FieldType type() {
        return type;
    }

    public Object content() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldValue that = (FieldValue) o;
        return type == that.type && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content);
    }
}
