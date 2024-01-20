package tiem625.anonimizer.commonterms;

import java.util.Objects;

public class Amount {

    public static final Amount NONE = Amount.of(0);

    public static Amount of(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Provided negative amount: " + amount);
        }
        return new Amount(amount);
    }

    private final int amount;

    private Amount(int amount) {
        this.amount = amount;
    }

    public int asNumber() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Amount amount1 = (Amount) o;
        return amount == amount1.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return String.valueOf(asNumber());
    }
}
