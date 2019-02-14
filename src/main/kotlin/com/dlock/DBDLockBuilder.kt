package com.dlock

import com.dlock.expiration.DLockExpirationPolicy
import com.dlock.expiration.LocalDLockExpirationPolicy
import com.dlock.model.DLockHandleIdGenerator
import com.dlock.model.DLockHandleUUIDIdGenerator
import com.dlock.repository.JDBCDLockRepository
import com.dlock.time.DateTimeProvider
import com.dlock.utils.schema.InitDatabase
import javax.sql.DataSource

/**
 * Builder for DBDLock.
 *
 * @author Przemyslaw Malirz
 */
class DBDLockBuilder {

    companion object {
        const val DEFAULT_LOCK_TABLE_NAME = "DLCK"
    }

    private lateinit var dataSource: DataSource
    private var lockTableName: String = DEFAULT_LOCK_TABLE_NAME
    private var lockHandleIdGenerator: DLockHandleIdGenerator = DLockHandleUUIDIdGenerator()
    private var lockExpirationPolicy: DLockExpirationPolicy = LocalDLockExpirationPolicy(DateTimeProvider)
    private var lockDateTimeProvider: DateTimeProvider = DateTimeProvider.DefaultDateTimeProvider
    private var createDatabase = false

    /**
     * That is the only required property.
     */
    fun dataSource(dataSource: DataSource): DBDLockBuilder {
        this.dataSource = dataSource
        return this
    }

    fun lockTableName(lockTableName: String): DBDLockBuilder {
        this.lockTableName = lockTableName
        return this
    }

    fun lockHandleIdGenerator(lockHandleIdGenerator: DLockHandleIdGenerator): DBDLockBuilder {
        this.lockHandleIdGenerator = lockHandleIdGenerator
        return this
    }

    fun lockExpirationPolicy(lockExpirationPolicy: DLockExpirationPolicy): DBDLockBuilder {
        this.lockExpirationPolicy = lockExpirationPolicy
        return this
    }

    fun lockDateTimeProvider(lockDateTimeProvider: DateTimeProvider): DBDLockBuilder {
        this.lockDateTimeProvider = lockDateTimeProvider
        return this
    }

    fun createDatabase(createDatabase: Boolean): DBDLockBuilder {
        this.createDatabase = createDatabase
        return this
    }

    fun build(): DBDLock {
        val lockRepository = JDBCDLockRepository(dataSource, lockTableName)
        val lockHandleUUIDGenerator = DLockHandleUUIDIdGenerator()
        val dbdLock = DBDLock(lockRepository, lockHandleUUIDGenerator, lockExpirationPolicy, lockDateTimeProvider)

        if (createDatabase) {
            InitDatabase(dataSource, lockTableName).createDatabase()
        }

        return dbdLock
    }

}