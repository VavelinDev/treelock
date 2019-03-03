package com.dlock.repository

import com.dlock.model.LockRecord
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
        private val dataSource: DataSource,
        private val tableName: String) : LockRepository {

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
            executeRemove(it, lockHandleId)
            commit(it)
        }
    }

    // SQL / JDBC -----------------------------------------------------------------------------------------------

    /** Select SQL PreparedStatement. */
    private fun executeFindByHandleId(connection: Connection, lockHandleId: String): Optional<LockRecord> {
        val sql = "SELECT  LCK_KEY, LCK_HNDL_ID, CREATED_TIME, EXPIRE_SEC FROM $tableName WHERE LCK_HNDL_ID = ?"

        with(connection.prepareStatement(sql)) {
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
        val sql = "SELECT LCK_KEY, LCK_HNDL_ID, CREATED_TIME, EXPIRE_SEC FROM $tableName WHERE LCK_KEY = ?"

        with(connection.prepareStatement(sql)) {
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
        val sql = "INSERT INTO $tableName (LCK_KEY, LCK_HNDL_ID, CREATED_TIME, EXPIRE_SEC) " +
                "SELECT ?, ?, ?, ? FROM DUAL " +
                "WHERE NOT EXISTS (SELECT 1 FROM $tableName WHERE LCK_KEY = ?)"

        with(connection.prepareStatement(sql)) {
            setString(1, lockRecord.lockKey)
            setString(2, lockRecord.lockHandleId)
            setTimestamp(3, Timestamp.valueOf(lockRecord.createdTime))
            setLong(4, lockRecord.expirationSeconds)
            setString(5, lockRecord.lockKey)

            return executeUpdate() == 1
        }

    }

    /** Delete SQL PreparedStatement. */
    private fun executeRemove(connection: Connection, lockHandleId: String) {
        val sql = "DELETE FROM $tableName WHERE LCK_HNDL_ID = ?"

        val deleteStatement = connection.prepareStatement(sql)
        deleteStatement.setString(1, lockHandleId)
        deleteStatement.executeUpdate()
    }

    private fun commit(connection: Connection) {
        if (!connection.autoCommit) connection.commit()
    }

}