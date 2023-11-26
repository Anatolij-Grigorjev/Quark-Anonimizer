package tiem625.anonimizer.testsupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import tiem625.anonimizer.commonterms.Amount;
import tiem625.anonimizer.commonterms.BatchName;

@ApplicationScoped
public class TestDbContext {

    @Inject
    DSLContext db;

    public boolean batchExists(BatchName table) {
        try {
            db.selectOne().from(table.asString()).fetchOne();
            return true;
        } catch (DataAccessException sqlError) {
            sqlError.printStackTrace();
            return false;
        }
    }

    public Amount getBatchRecordsCount(BatchName tableName) {
        return Amount.of(db.fetchCount(DSL.table(tableName.asString())));
    }
}
