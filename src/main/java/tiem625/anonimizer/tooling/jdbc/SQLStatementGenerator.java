package tiem625.anonimizer.tooling.jdbc;

import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.generating.DataGenerator.DataFieldSpec;

import java.util.List;

public interface SQLStatementGenerator {

    SQLStatement createTableStatement(BatchName batchName, List<DataFieldSpec> fieldSpecs);

    SQLStatement checkTableExistsStatement(BatchName batchName);

    SQLStatement tableSizeStatement(BatchName batchName);

    SQLStatement fetchTableRowsStatement(BatchName batchName, Amount limit);
}
