package com.dlock


import com.dlock.api.KeyLock
import com.dlock.api.LockHandle
import com.dlock.repository.JDBCLockRepository
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Specification

/**
 * Bunch of quick tests against DBKeyLock. They cover most of the cases we are interested in.
 * There is also dlock-benchmark (based on jmh) project which completes that integration test with more
 * concurrent usage scenarios.
 *
 * @author Przemyslaw Malirz
 */
class DBKeyLockTest extends Specification {

    private KeyLock keyLock
    private JDBCLockRepository repository

    def setup() {
        // H2 datasource
        def dataSource = new JdbcDataSource()
        dataSource.setURL("jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1")
        dataSource.setUser("sa")
        dataSource.setPassword("sa")

        // it help us to check if the lock exists in the database once created
        repository = new JDBCLockRepository(dataSource, DBKeyLockBuilder.DEFAULT_LOCK_TABLE_NAME)

        keyLock = new DBKeyLockBuilder()
                .dataSource(dataSource)
                .createDatabase(true)
                .build()
    }

    def "Try Lock"() {
        when:
        def lockHandle = keyLock.tryLock("a", 300)

        then:
        noExceptionThrown()
        assert lockHandle.present

        def createdLock = repository.findLockByKey("a")
        assert createdLock.get().lockHandleId == lockHandle.get().handleId
    }

    def "Release Lock"() {
        when:
        def lockHandle = keyLock.tryLock("b", 300)
        keyLock.unlock(lockHandle.get())

        then:
        noExceptionThrown()

        def createdLock = repository.findLockByKey("b")
        assert !createdLock.present
    }

    def "Release not existing Lock"() {
        when:
        keyLock.unlock new LockHandle("xyz")

        then:
        noExceptionThrown()
    }

    def "Collision Lock"() {
        when:
        def lockHandleA = keyLock.tryLock("c", 300)
        def lockHandleB = keyLock.tryLock("c", 300)

        then:
        noExceptionThrown()

        assert lockHandleA.present
        assert !lockHandleB.present
    }

}
