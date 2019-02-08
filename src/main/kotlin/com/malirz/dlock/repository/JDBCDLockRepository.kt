package com.malirz.dlock.repository

import com.malirz.dlock.exception.DLockAlreadyExistsException
import com.malirz.dlock.model.DLockRecord
import java.sql.Connection
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.Timestamp
import java.util.*
import javax.sql.DataSource

/**
 * JDBC access to the lock storage. Lock is represented by the {@link DLockRecord} class.
 *
 * @author Przemyslaw Malirz
 */
class JDBCDLockRepository(
        private val dataSource: DataSource,
        private val tableName: String) : DLockRepository {

    override fun createLock(lockRecord: DLockRecord) {
        dataSource.connection.use {
            try {
                executeInsert(it, lockRecord)
            } catch (ex: SQLIntegrityConstraintViolationException) {
                throw DLockAlreadyExistsException("Lock cannot be created as it already exists: $lockRecord", ex)
            }
        }
    }

    override fun findLock(lockKey: String): Optional<DLockRecord> {
        dataSource.connection.use {
            return executeFind(it, lockKey)
        }
    }

    override fun removeLock(lockKey: String) {
        dataSource.connection.use {
            executeRemove(it, lockKey)
        }
    }

    // SQL / JDBC -----------------------------------------------------------------------------------------------

    /** Select SQL PreparedStatement. */
    private fun executeFind(connection: Connection, lockKey: String): Optional<DLockRecord> {
        val sql = "SELECT LCK_HNDL_ID, CREATED_TIME, EXPIRE_SEC FROM $tableName WHERE LCK_KEY = ?"

        with(connection.prepareStatement(sql)) {
            setString(1, lockKey)

            val executeResult = execute() && resultSet.next()

            return if (executeResult) {
                val lockHandleId = resultSet.getString(1)
                val lockCreatedTime = resultSet.getTimestamp(2)
                val lockExpirationSeconds = resultSet.getLong(3)
                val lockHandle = DLockRecord(lockKey, lockHandleId, lockCreatedTime.toLocalDateTime(), lockExpirationSeconds)
                Optional.of(lockHandle)
            } else {
                Optional.empty()
            }

        }
    }

    /** Insert SQL PreparedStatement. */
    private fun executeInsert(connection: Connection, lockRecord: DLockRecord) {
        val sql = "INSERT INTO $tableName (LCK_KEY, LCK_HNDL_ID, CREATED_TIME, EXPIRE_SEC) VALUES (?, ?, ?, ?)"

        with(connection.prepareStatement(sql)) {
            setString(1, lockRecord.lockKey)
            setString(2, lockRecord.lockHandleId)
            setTimestamp(3, Timestamp.valueOf(lockRecord.createdTime))
            setLong(4, lockRecord.expirationSeconds)

            executeUpdate()
        }

    }

    /** Delete SQL PreparedStatement. */
    private fun executeRemove(connection: Connection, lockKey: String) {
        val sql = "DELETE FROM $tableName WHERE LCK_KEY = ?"

        val deleteStatement = connection.prepareStatement(sql)
        deleteStatement.setString(1, lockKey)
        deleteStatement.executeUpdate()
    }


}