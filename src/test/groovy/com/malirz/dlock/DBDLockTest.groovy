package com.malirz.dlock

import com.malirz.dlock.api.DLock
import com.malirz.dlock.api.DLockHandle
import com.malirz.dlock.exception.DLockTriedToBreakNotOwnedLockException
import com.malirz.dlock.repository.JDBCDLockRepository
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Specification

/**
 * Bunch of quick tests against DBDLock. They cover most of the cases we are interested in.
 * There is also dlock-benchmark (based on jmh) project which completes that integration test with more
 * concurrent usage scenarios.
 *
 * @author Przemyslaw Malirz
 */
class DBDLockTest extends Specification {

    private DLock dLock
    private JDBCDLockRepository repository

    def setup() {
        // H2 datasource
        def dataSource = new JdbcDataSource()
        dataSource.setURL("jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1")
        dataSource.setUser("sa")
        dataSource.setPassword("sa")

        // it help us to check if the lock exists in the database once created
        repository = new JDBCDLockRepository(dataSource, DBDLockBuilder.DEFAULT_LOCK_TABLE_NAME)

        dLock = new DBDLockBuilder()
                .dataSource(dataSource)
                .createDatabase(true)
                .build()
    }

    def "Try Lock"() {
        when:
        def lockHandle = dLock.tryLock("a", 300)

        then:
        noExceptionThrown()
        assert lockHandle.present

        def createdLock = repository.findLock(lockHandle.get().lockKey)
        assert createdLock.get().lockHandleId == lockHandle.get().handleId
    }

    def "Release Lock"() {
        when:
        def lockHandle = dLock.tryLock("b", 300)
        dLock.release(lockHandle.get())

        then:
        noExceptionThrown()

        def createdLock = repository.findLock(lockHandle.get().lockKey)
        assert !createdLock.present
    }

    def "Release not owned Lock"() {
        when:
        def lockHandle = dLock.tryLock("b", 300)
        dLock.release new DLockHandle(lockHandle.get().getLockKey(), "Fake Handle")

        then:
        thrown DLockTriedToBreakNotOwnedLockException

        def existingLock = repository.findLock(lockHandle.get().lockKey)
        assert existingLock.present
    }

    def "Release not existing Lock"() {
        when:
        dLock.release new DLockHandle("xyz", "Fake Handle")

        then:
        noExceptionThrown()
    }

    def "Collision Lock"() {
        when:
        def lockHandleA = dLock.tryLock("c", 300)
        def lockHandleB = dLock.tryLock("c", 300)

        then:
        noExceptionThrown()

        assert lockHandleA.present
        assert !lockHandleB.present
    }

}
