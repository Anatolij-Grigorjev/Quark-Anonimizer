package tiem625.anonimizer.commonterms.namerules;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SimpleASCIINameValidator {

    private SimpleASCIINameValidator() {
        throw new UnsupportedOperationException("utility");
    }

    public static String validOrThrow(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (startsWithDigit(name)) {
            throw new IllegalArgumentException("name cannot start with digit");
        }
        if (hasInvalidCharacters(name)) {
            throw new IllegalArgumentException("name can only be made up of ASCII and underscores");
        }
        return name;
    }

    private static boolean isBlank(String name) {
        return name == null || name.isBlank();
    }

    private static boolean startsWithDigit(String name) {
        return Character.isDigit(name.charAt(0));
    }

    private static boolean hasInvalidCharacters(String name) {
        var allCharsValid = name.chars().allMatch(ALLOWED_CHARS_SET::contains);
        return !allCharsValid;
    }

    private static final Stream<Integer> ALLOWED_LETTERS_STREAM = Stream.concat(
            IntStream.rangeClosed('A', 'Z').boxed(),
            IntStream.rangeClosed('a', 'z').boxed()
    );
    private static final Stream<Integer> ALLOWED_SYMBOLS_STREAM = Stream.concat(
        IntStream.rangeClosed('0', '9').boxed(), Stream.of((int)'_')
    );
    private static final Set<Integer> ALLOWED_CHARS_SET = Stream.concat(ALLOWED_LETTERS_STREAM, ALLOWED_SYMBOLS_STREAM)
            .collect(Collectors.toSet());
}
