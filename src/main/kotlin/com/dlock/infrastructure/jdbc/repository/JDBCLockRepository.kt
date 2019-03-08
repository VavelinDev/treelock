package com.dlock.infrastructure.jdbc.repository

import com.dlock.core.repository.LockRepository
import com.dlock.core.model.LockRecord
import com.dlock.infrastructure.jdbc.DatabaseType
import java.sql.Connection
import java.sql.Timestamp
import java.util.*
import javax.sql.DataSource

/**
 * JDBC access to the lock storage. Lock is represented by the {@link LockRecord} class.
 *
 * @author Przemyslaw Malirz
 */
class JDBCLockRepository(
        databaseType: DatabaseType,
        private val dataSource: DataSource,
        private val tableName: String) : LockRepository {

    private var insertSQL: String
    private var findByHandleSQL: String
    private var findByKeySQL: String
    private var removeByHandleSQL: String

    init {
        val sqlResource = Properties()
        val propertyFileName = "db/$databaseType-sql.properties"
        sqlResource.load(this.javaClass.classLoader.getResourceAsStream(propertyFileName))

        insertSQL = sqlResource.getProperty("lock.insert").replace("@@tableName@@", tableName)
        findByHandleSQL = sqlResource.getProperty("lock.findByHandle").replace("@@tableName@@", tableName)
        findByKeySQL = sqlResource.getProperty("lock.findByKey").replace("@@tableName@@", tableName)
        removeByHandleSQL = sqlResource.getProperty("lock.removeByHandle").replace("@@tableName@@", tableName)
    }

    override fun createLock(lockRecord: LockRecord): Boolean {
        dataSource.connection.use {
            val recordCreated = executeInsert(it, lockRecord)
            if (recordCreated) {
                commit(it)
            }
            return recordCreated
        }
    }

    override fun findLockByHandleId(lockHandleId: String): Optional<LockRecord> {
        dataSource.connection.use {
            return executeFindByHandleId(it, lockHandleId)
        }
    }

    override fun findLockByKey(lockKey: String): Optional<LockRecord> {
        dataSource.connection.use {
            return executeFindByKey(it, lockKey)
        }
    }

    override fun removeLock(lockHandleId: String) {
        dataSource.connection.use {
            val recordRemoved = executeRemove(it, lockHandleId)
            if (recordRemoved) {
                commit(it)
            }
        }
    }

    // SQL / JDBC -----------------------------------------------------------------------------------------------

    /** Select SQL PreparedStatement. */
    private fun executeFindByHandleId(connection: Connection, lockHandleId: String): Optional<LockRecord> {
        with(connection.prepareStatement(findByHandleSQL)) {
            setString(1, lockHandleId)

            val executeResult = execute() && resultSet.next()

            return if (executeResult) {
                val lockKey = resultSet.getString(1)
                val lockCreatedTime = resultSet.getTimestamp(3)
                val lockExpirationSeconds = resultSet.getLong(4)
                val lockRecord = LockRecord(lockKey, lockHandleId, lockCreatedTime.toLocalDateTime(), lockExpirationSeconds)
                Optional.of(lockRecord)
            } else {
                Optional.empty()
            }

        }
    }

    /** Select SQL PreparedStatement. */
    private fun executeFindByKey(connection: Connection, lockKey: String): Optional<LockRecord> {
        with(connection.prepareStatement(findByKeySQL)) {
            setString(1, lockKey)

            val executeResult = execute() && resultSet.next()

            return if (executeResult) {
                val lockHandleId = resultSet.getString(2)
                val lockCreatedTime = resultSet.getTimestamp(3)
                val lockExpirationSeconds = resultSet.getLong(4)
                val lockRecord = LockRecord(lockKey, lockHandleId, lockCreatedTime.toLocalDateTime(), lockExpirationSeconds)
                Optional.of(lockRecord)
            } else {
                Optional.empty()
            }

        }
    }

    /** Insert SQL PreparedStatement. */
    private fun executeInsert(connection: Connection, lockRecord: LockRecord): Boolean {
        with(connection.prepareStatement(insertSQL)) {
            setString(1, lockRecord.lockKey)
            setString(2, lockRecord.lockHandleId)
            setTimestamp(3, Timestamp.valueOf(lockRecord.createdTime))
            setLong(4, lockRecord.expirationSeconds)
            setString(5, lockRecord.lockKey)

            return executeUpdate() == 1
        }
    }

    /** Delete SQL PreparedStatement. */
    private fun executeRemove(connection: Connection, lockHandleId: String): Boolean {
        val deleteStatement = connection.prepareStatement(removeByHandleSQL)
        deleteStatement.setString(1, lockHandleId)
        return deleteStatement.executeUpdate() == 1
    }

    private fun commit(connection: Connection) {
        if (!connection.autoCommit) connection.commit()
    }

}