package com.dlock.jdbc.repository

import com.dlock.core.model.LockRecord
import com.dlock.core.repository.LockRepository
import com.dlock.jdbc.tool.script.ScriptResolver
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
        scriptResolver: ScriptResolver,
        private val dataSource: DataSource) : LockRepository {

    private var insertSQL = scriptResolver.resolveScript("lock.insert")
    private var findByHandleSQL = scriptResolver.resolveScript("lock.findByHandle")
    private var findByKeySQL = scriptResolver.resolveScript("lock.findByKey")
    private var removeByHandleSQL = scriptResolver.resolveScript("lock.removeByHandle")

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
        connection.prepareStatement(findByHandleSQL).use {
            it.setString(1, lockHandleId)

            val executeResult = it.execute() && it.resultSet.next()

            return if (executeResult) {
                val lockKey = it.resultSet.getString(1)
                val lockCreatedTime = it.resultSet.getTimestamp(3)
                val lockExpirationSeconds = it.resultSet.getLong(4)
                val lockRecord = LockRecord(lockKey, lockHandleId, lockCreatedTime.toLocalDateTime(), lockExpirationSeconds)
                Optional.of(lockRecord)
            } else {
                Optional.empty()
            }

        }
    }

    /** Select SQL PreparedStatement. */
    private fun executeFindByKey(connection: Connection, lockKey: String): Optional<LockRecord> {
        connection.prepareStatement(findByKeySQL).use {
            it.setString(1, lockKey)

            val executeResult = it.execute() && it.resultSet.next()

            return if (executeResult) {
                val lockHandleId = it.resultSet.getString(2)
                val lockCreatedTime = it.resultSet.getTimestamp(3)
                val lockExpirationSeconds = it.resultSet.getLong(4)
                val lockRecord = LockRecord(lockKey, lockHandleId, lockCreatedTime.toLocalDateTime(), lockExpirationSeconds)
                Optional.of(lockRecord)
            } else {
                Optional.empty()
            }

        }
    }

    /** Insert SQL PreparedStatement. */
    private fun executeInsert(connection: Connection, lockRecord: LockRecord): Boolean {
        connection.prepareStatement(insertSQL).use {
            it.setString(1, lockRecord.lockKey)
            it.setString(2, lockRecord.lockHandleId)
            it.setTimestamp(3, Timestamp.valueOf(lockRecord.createdTime))
            it.setLong(4, lockRecord.expirationSeconds)
            it.setString(5, lockRecord.lockKey)

            return it.executeUpdate() == 1
        }
    }

    /** Delete SQL PreparedStatement. */
    private fun executeRemove(connection: Connection, lockHandleId: String): Boolean {
        connection.prepareStatement(removeByHandleSQL).use {
            it.setString(1, lockHandleId)
            return it.executeUpdate() == 1
        }
    }

    private fun commit(connection: Connection) {
        if (!connection.autoCommit) connection.commit()
    }

}