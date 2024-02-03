package tiem625.anonimizer.testsupport;

public class Wrappers {

    private Wrappers() {
        throw new UnsupportedOperationException("static helper");
    }

    public static <T> T unchecked(ThrowsCheckedSupplier<T> func) {
        try {
            return func.value();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <I, O> O unchecked(ThrowsCheckedFunc<I, O> func, I input) {
        try {
            return func.value(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface ThrowsCheckedSupplier<T> {
        T value() throws Exception;
    }

    public interface ThrowsCheckedFunc<I, O> {
        O value(I input) throws Exception;
    }
}
