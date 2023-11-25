package tiem625.anonimizer.fetching;

import tiem625.anonimizer.DataObject;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;

import java.util.List;

public interface DataFetcher {

    List<DataObject> fetchWhere(Query dataQuery);

    record Query(
            BatchName batchName,
            Amount numRecords
    ) {}
}
