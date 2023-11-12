package tiem625.anonimizer.commonterms;

import java.util.Objects;

public class SampleSize {

    public static final SampleSize EMPTY = SampleSize.of(0);

    public static SampleSize of(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Negative sample size provided: " + amount);
        }
        return new SampleSize(amount);
    }

    private final int amount;

    private SampleSize(int amount) {
        this.amount = amount;
    }

    public int amount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SampleSize that = (SampleSize) o;
        return amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
}
