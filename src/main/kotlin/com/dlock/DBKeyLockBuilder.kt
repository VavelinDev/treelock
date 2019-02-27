package com.dlock

import com.dlock.expiration.LockExpirationPolicy
import com.dlock.expiration.LocalLockExpirationPolicy
import com.dlock.model.LockHandleIdGenerator
import com.dlock.model.LockHandleUUIDIdGenerator
import com.dlock.repository.JDBCLockRepository
import com.dlock.time.DateTimeProvider
import com.dlock.utils.schema.InitDatabase
import javax.sql.DataSource

/**
 * Builder for DBKeyLock.
 *
 * @author Przemyslaw Malirz
 */
class DBKeyLockBuilder {

    companion object {
        const val DEFAULT_LOCK_TABLE_NAME = "DLCK"
    }

    private lateinit var dataSource: DataSource
    private var lockTableName: String = DEFAULT_LOCK_TABLE_NAME
    private var lockHandleIdGenerator: LockHandleIdGenerator = LockHandleUUIDIdGenerator()
    private var lockExpirationPolicy: LockExpirationPolicy = LocalLockExpirationPolicy(DateTimeProvider)
    private var lockDateTimeProvider: DateTimeProvider = DateTimeProvider.DefaultDateTimeProvider
    private var createDatabase = false

    /**
     * That is the only required property.
     */
    fun dataSource(dataSource: DataSource): DBKeyLockBuilder {
        this.dataSource = dataSource
        return this
    }

    fun lockTableName(lockTableName: String): DBKeyLockBuilder {
        this.lockTableName = lockTableName
        return this
    }

    fun lockHandleIdGenerator(lockHandleIdGenerator: LockHandleIdGenerator): DBKeyLockBuilder {
        this.lockHandleIdGenerator = lockHandleIdGenerator
        return this
    }

    fun lockExpirationPolicy(lockExpirationPolicy: LockExpirationPolicy): DBKeyLockBuilder {
        this.lockExpirationPolicy = lockExpirationPolicy
        return this
    }

    fun lockDateTimeProvider(lockDateTimeProvider: DateTimeProvider): DBKeyLockBuilder {
        this.lockDateTimeProvider = lockDateTimeProvider
        return this
    }

    fun createDatabase(createDatabase: Boolean): DBKeyLockBuilder {
        this.createDatabase = createDatabase
        return this
    }

    fun build(): DBKeyLock {
        val lockRepository = JDBCLockRepository(dataSource, lockTableName)
        val lockHandleUUIDGenerator = LockHandleUUIDIdGenerator()
        val dbdLock = DBKeyLock(lockRepository, lockHandleUUIDGenerator, lockExpirationPolicy, lockDateTimeProvider)

        if (createDatabase) {
            InitDatabase(dataSource, lockTableName).createDatabase()
        }

        return dbdLock
    }

}