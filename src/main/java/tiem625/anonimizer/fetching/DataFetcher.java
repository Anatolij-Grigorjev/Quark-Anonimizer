package tiem625.anonimizer.fetching;

import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;
import tiem625.anonimizer.commonterms.DataObject;

import java.util.List;

public interface DataFetcher {

    List<DataObject> fetchWhere(Query dataQuery);

    record Query(
            BatchName batchName,
            Amount numRecords
    ) {}
}
