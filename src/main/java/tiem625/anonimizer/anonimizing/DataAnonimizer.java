package tiem625.anonimizer.anonimizing;

import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.FieldName;

import java.util.List;

public interface DataAnonimizer {

    AnonimizationResults anonimizeBatch(BatchName batchName, AnonimizationRules rules);

    record AnonimizationResults(
            BatchName batchName,
            Amount recordsProcessed,
            List<FieldAnonimizationResult> resultsByField
    ) {}

    record FieldAnonimizationResult(
            FieldName fieldName,
            AnonimizationBehavior triedRule,
            AnonimizerError error
    ) {}

    record AnonimizationRules(
            List<AnonimizationRule> rulesList
    ) {}

    record AnonimizationRule(
            FieldName field,
            AnonimizationBehavior desiredBehavior
    ) {}
}
