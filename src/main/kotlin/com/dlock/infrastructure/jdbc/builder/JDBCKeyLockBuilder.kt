package com.dlock.infrastructure.jdbc.builder

import com.dlock.core.SimpleKeyLock
import com.dlock.core.expiration.LocalLockExpirationPolicy
import com.dlock.core.expiration.LockExpirationPolicy
import com.dlock.core.handle.LockHandleIdGenerator
import com.dlock.core.handle.LockHandleUUIDIdGenerator
import com.dlock.infrastructure.jdbc.DatabaseType
import com.dlock.infrastructure.jdbc.repository.JDBCLockRepository
import com.dlock.infrastructure.jdbc.tool.schema.InitDatabase
import com.dlock.infrastructure.jdbc.tool.script.ScriptResolver
import com.dlock.util.time.DateTimeProvider
import javax.sql.DataSource

/**
 * Builder for {@link SimpleKeyLock} backed by the JDBC repository (database).
 *
 * @author Przemyslaw Malirz
 */
class JDBCKeyLockBuilder {

    companion object {
        const val DEFAULT_LOCK_TABLE_NAME = "DLCK"
    }

    private lateinit var dataSource: DataSource
    private lateinit var databaseType: DatabaseType
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

    fun databaseType(databaseType: DatabaseType): JDBCKeyLockBuilder {
        this.databaseType = databaseType
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

    fun build(): SimpleKeyLock {
        checkNotNull(databaseType) { "Please declare databaseType" }
        checkNotNull(dataSource) { "Please declare dataSource" }

        val scriptResolver = ScriptResolver(databaseType, lockTableName)

        val lockRepository = JDBCLockRepository(scriptResolver, dataSource)
        val lockHandleUUIDGenerator = LockHandleUUIDIdGenerator()
        val dbdLock = SimpleKeyLock(lockRepository, lockHandleUUIDGenerator, lockExpirationPolicy, lockDateTimeProvider)

        if (createDatabase) {
            InitDatabase(scriptResolver, dataSource).createDatabase()
        }

        return dbdLock
    }

}