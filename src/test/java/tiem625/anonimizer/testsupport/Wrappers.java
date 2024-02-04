package tiem625.anonimizer.testsupport;

import java.util.function.Function;
import java.util.function.Supplier;

public class Wrappers {

    private Wrappers() {
        throw new UnsupportedOperationException("static helper");
    }

    public static <T> T unchecked(ThrowsCheckedSupplier<T> func) {
        return func.get();
    }

    public static <I, O> O unchecked(ThrowsCheckedFunc<I, O> func, I input) {
        return func.apply(input);
    }

    public interface ThrowsCheckedSupplier<T> extends Supplier<T> {

        @Override
        default T get() {
            try {
                return value();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        T value() throws Exception;
    }

    public interface ThrowsCheckedFunc<I, O> extends Function<I, O> {

        @Override
        default O apply(I input) {
            try {
                return value(input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        O value(I input) throws Exception;
    }
}
