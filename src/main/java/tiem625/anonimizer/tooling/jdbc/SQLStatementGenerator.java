package tiem625.anonimizer.tooling.jdbc;

import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.generating.DataGenerator;

import java.util.List;

public class SQLStatementGenerator {


    private final SQLStatementGeneratorConfig config;

    public SQLStatementGenerator(SQLStatementGeneratorConfig config) {
        this.config = config;
    }


    public String createTableStatement(BatchName batchName, List<DataGenerator.DataFieldSpec> fieldSpecs) {
        throw new UnsupportedOperationException("TODO");
    }


    public String checkTableExistsStatement(BatchName batchName) {
        throw new UnsupportedOperationException("TODO");
    }


    public String tableSizeStatement(BatchName batchName) {
        throw new UnsupportedOperationException("TODO");
    }


    public String fetchTableRowsStatement(BatchName batchName, Amount amount) {
        throw new UnsupportedOperationException("TODO");
    }
}
