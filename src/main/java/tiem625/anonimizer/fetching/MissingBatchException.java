package tiem625.anonimizer.fetching;

import tiem625.anonimizer.commonterms.BatchName;

public class MissingBatchException extends RuntimeException {

    private final BatchName batchName;

    public MissingBatchException(BatchName batchName) {
        super("Cannot fetch from missing batch '" + batchName + "'");
        this.batchName = batchName;
    }

    public BatchName batchName() {
        return batchName;
    }
}
