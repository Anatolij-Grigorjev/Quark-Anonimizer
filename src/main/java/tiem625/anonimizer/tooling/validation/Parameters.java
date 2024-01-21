package tiem625.anonimizer.tooling.validation;

import java.util.function.Predicate;

public class Parameters {

    private Parameters() {
        throw new UnsupportedOperationException("static helper");
    }

    public static void assertParamPresent(Object parameter, String message) {
        if (parameter == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> void assertParamCondition(T parameter, Predicate<T> condition, String message) {
        assertParamPresent(condition, "cannot check with null condition!");
        if (condition.negate().test(parameter)) {
            throw new IllegalArgumentException(message);
        }
    }
}
