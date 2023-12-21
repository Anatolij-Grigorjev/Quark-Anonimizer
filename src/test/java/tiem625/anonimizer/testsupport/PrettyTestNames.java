package tiem625.anonimizer.testsupport;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;

/**
 * An annotation that sets for tests in the file to generate display names by replacing underscores
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public @interface PrettyTestNames {
}
