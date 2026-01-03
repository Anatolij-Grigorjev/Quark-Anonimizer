package tiem625.anonimizer.fetching;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.fetching.DataFetcher.Query;
import tiem625.anonimizer.testsupport.PrettyTestNames;
import tiem625.anonimizer.testsupport.TestData;
import tiem625.anonimizer.testsupport.TestDbContext;

import java.util.List;

@PrettyTestNames
@QuarkusTest
@Disabled("No implementation yet for DataFetcher")
public class DatabaseDataFetcherTests {

    TestData data;

    private DataFetcher dataFetcherImpl;

    @Inject
    TestDbContext db;

    @BeforeEach
    void setupService() {
        data = new TestData();
        dataFetcherImpl = null;
    }

    @Test
    void incomplete_query_throws_error() {
        Query queryNull = null;
        Query queryNoBatchName = new Query(null, Amount.NONE);
        Query queryAmountNotSpecified = new Query(data.TST_BATCH, null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> dataFetcherImpl.fetchWhere(queryNull));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataFetcherImpl.fetchWhere(queryNoBatchName));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataFetcherImpl.fetchWhere(queryAmountNotSpecified));
    }

    @Test
    void query_for_none_in_present_batch_returns_empty() {
        db.createBatch(data.TST_BATCH);
        Assertions.assertEquals(List.of(), dataFetcherImpl.fetchWhere(new Query(data.TST_BATCH, Amount.NONE)));
    }

    @Test
    void query_for_none_in_missing_batch_throws_error() {
        var exception = Assertions.assertThrows(MissingBatchException.class, () -> dataFetcherImpl.fetchWhere(new Query(data.TST_BATCH, Amount.NONE)));
        Assertions.assertEquals(data.TST_BATCH, exception.batchName());
    }
}
