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
 * Builder for JDBCKeyLock.
 *
 * @author Przemyslaw Malirz
 */
class JDBCKeyLockBuilder {

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
    fun dataSource(dataSource: DataSource): JDBCKeyLockBuilder {
        this.dataSource = dataSource
        return this
    }

    fun lockTableName(lockTableName: String): JDBCKeyLockBuilder {
        this.lockTableName = lockTableName
        return this
    }

    fun lockHandleIdGenerator(lockHandleIdGenerator: LockHandleIdGenerator): JDBCKeyLockBuilder {
        this.lockHandleIdGenerator = lockHandleIdGenerator
        return this
    }

    fun lockExpirationPolicy(lockExpirationPolicy: LockExpirationPolicy): JDBCKeyLockBuilder {
        this.lockExpirationPolicy = lockExpirationPolicy
        return this
    }

    fun lockDateTimeProvider(lockDateTimeProvider: DateTimeProvider): JDBCKeyLockBuilder {
        this.lockDateTimeProvider = lockDateTimeProvider
        return this
    }

    fun createDatabase(createDatabase: Boolean): JDBCKeyLockBuilder {
        this.createDatabase = createDatabase
        return this
    }

    fun build(): JDBCKeyLock {
        val lockRepository = JDBCLockRepository(dataSource, lockTableName)
        val lockHandleUUIDGenerator = LockHandleUUIDIdGenerator()
        val dbdLock = JDBCKeyLock(lockRepository, lockHandleUUIDGenerator, lockExpirationPolicy, lockDateTimeProvider)

        if (createDatabase) {
            InitDatabase(dataSource, lockTableName).createDatabase()
        }

        return dbdLock
    }

}