package tiem625.anonimizer.testsupport;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that sets for tests in the file to generate display names by replacing underscores
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public @interface PrettyTestNames {
}
