package tiem625.anonimizer.commonterms;

import java.util.Objects;

public class FieldValue {

    public static FieldValue of(FieldType type, Object content) {
        if (content == null && type == null) {
            throw new IllegalArgumentException("Either type or content must be known for field value");
        }
        if (content == null) {
            return new FieldValue(type, content);
        }
        if (type == null) {
            return ofTyped(content);
        }
        var inferredType = ofTyped(content).type();
        if (type != inferredType) {
            throw new IllegalArgumentException("Supplied type " + type + "disagrees with actual content type " + inferredType);
        }
        return new FieldValue(type, content);
    }

    public static FieldValue ofTyped(Object content) {
        if (content == null) {
            throw new IllegalArgumentException("Cannot have untyped null field value");
        }
        if (content instanceof Number) {
            return new FieldValue(FieldType.NUMBER, content);
        }
        if (content instanceof String) {
            return new FieldValue(FieldType.TEXT, content);
        }
        throw new IllegalArgumentException("Not sure what to do with content of type " + content.getClass());
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
