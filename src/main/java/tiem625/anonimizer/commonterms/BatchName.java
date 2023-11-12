package tiem625.anonimizer.commonterms;

import java.util.Objects;

public class BatchName {

    public static BatchName of(String name) {
        return new BatchName(name);
    }

    private BatchName(String name) {
        this.name = name;
    }

    private final String name;

    public String asString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchName batchName = (BatchName) o;
        return Objects.equals(name, batchName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
