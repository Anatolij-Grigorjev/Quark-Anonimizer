package tiem625.anonimizer.tooling.sql;

import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.DataObject;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;

import java.util.List;

public interface BatchQueriesResolver {

    void executeBatchCreationQuery(BatchName batchName, List<DataFieldSpec> fieldSpecs);

    boolean executeCheckBatchExistsQuery(BatchName batchName);

    Amount executeFetchBatchSizeQuery(BatchName batchName);

    List<DataObject> executeFetchSomeBatchObjectsQuery(BatchName batchName, Amount amount);
}
