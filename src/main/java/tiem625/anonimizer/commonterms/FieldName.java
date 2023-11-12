package tiem625.anonimizer.commonterms;

import java.util.Objects;

import static tiem625.anonimizer.commonterms.namerules.SimpleASCIINameValidator.validOrThrow;

public class FieldName {

    public static FieldName of(String name) {
        return new FieldName(validOrThrow(name));
    }

    private FieldName(String name) {
        this.name = name;
    }

    private final String name;

    public String asString() {
        return name;
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldName batchName = (FieldName) o;
        return Objects.equals(name, batchName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
