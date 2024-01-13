package tiem625.anonimizer.streams;

import java.util.function.BinaryOperator;

public class StreamOperators {

    private StreamOperators() {
        throw new UnsupportedOperationException("static helper");
    }

    public static <T> BinaryOperator<T> pickFirst() {
        return (a, b) -> a;
    }
}
