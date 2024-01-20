package tiem625.anonimizer.commonterms;

public enum FieldType {

    TEXT, NUMBER;

    public static FieldType forJavaClass(Class<?> fieldClazz) {
        if (fieldClazz.isAssignableFrom(String.class)) {
            return TEXT;
        }
        if (fieldClazz.isAssignableFrom(Number.class)) {
            return NUMBER;
        }
        throw new IllegalArgumentException("Cannot convert class " + fieldClazz.getName() + " to " + FieldType.class);
    }
}
