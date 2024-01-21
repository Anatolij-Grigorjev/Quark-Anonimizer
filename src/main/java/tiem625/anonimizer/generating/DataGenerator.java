package tiem625.anonimizer.generating;

import jakarta.annotation.Nonnull;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldName;
import tiem625.anonimizer.commonterms.FieldType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public interface DataGenerator {

    void generate(@Nonnull DataGenerationRules rules);

    record DataGenerationRules(
            BatchName batchName,
            List<DataFieldSpec> fieldSpecs,
            Amount amount
    ) {}

    record DataFieldSpec(
            FieldName fieldName,
            FieldType fieldType,
            FieldConstraints fieldConstraints
    ) {
        public DataFieldSpec(FieldName fieldName, FieldType fieldType) {
            this(fieldName, fieldType, FieldConstraints.NONE);
        }

        public DataFieldSpec(FieldName fieldName, FieldType fieldType, FieldConstraint... constraints) {
            this(fieldName, fieldType, FieldConstraints.of(constraints));
        }

        public DataFieldSpec(FieldName fieldName, FieldType fieldType, List<FieldConstraint> constraints) {
            this(fieldName, fieldType, FieldConstraints.of(constraints));
        }
    }

    class FieldConstraints {
        public static final FieldConstraints NONE = new FieldConstraints(EnumSet.noneOf(FieldConstraint.class));

        public static FieldConstraints of(FieldConstraint... constraints) {
            if (constraints.length == 0) {
                return NONE;
            }
            return new FieldConstraints(EnumSet.copyOf(Set.of(constraints)));
        }

        public static FieldConstraints of(List<FieldConstraint> constraints) {
            if (constraints == null || constraints.isEmpty()) {
                return NONE;
            }
            return new FieldConstraints(EnumSet.copyOf(constraints));
        }

        private final EnumSet<FieldConstraint> constraints;

        private FieldConstraints(EnumSet<FieldConstraint> constraints) {
            this.constraints = constraints;
        }

        public boolean nullable() {
            return !constraints.contains(FieldConstraint.NOT_NULL);
        }

        public boolean unique() {
            return constraints.contains(FieldConstraint.UNIQUE);
        }
    }
}
