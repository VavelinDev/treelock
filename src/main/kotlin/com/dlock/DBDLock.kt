package com.dlock

import com.dlock.api.DLock
import com.dlock.api.DLockHandle
import com.dlock.exception.DLockTriedToBreakNotOwnedLockException
import com.dlock.expiration.DLockExpirationPolicy
import com.dlock.model.DLockHandleIdGenerator
import com.dlock.model.DLockRecord
import com.dlock.repository.JDBCDLockRepository
import com.dlock.time.DateTimeProvider
import java.util.*

/**
 * DLock based on plain JDBC.
 *
 * @author Przemyslaw Malirz
 */
class DBDLock(
        private val lockRepository: JDBCDLockRepository,
        private val lockHandleIdGenerator: DLockHandleIdGenerator,
        private val lockExpirationPolicy: DLockExpirationPolicy,
        private val dateTimeProvider: DateTimeProvider)
    : DLock {

    override fun tryLock(lockKey: String, expirationSeconds: Long): Optional<DLockHandle> {

        val currentLockRecord = lockRepository.findLock(lockKey)

        return if (expired(currentLockRecord)) {
            breakLockIfExists(currentLockRecord)
            val createNewLock = createNewLock(lockKey, expirationSeconds)
            Optional.of(createNewLock)
        } else {
            Optional.empty()
        }

    }

    override fun release(lockHandle: DLockHandle) {
        val existingLockRecord = lockRepository.findLock(lockHandle.lockKey)

        if (isOwner(existingLockRecord, lockHandle)) {
            breakLockIfExists(existingLockRecord)
        } else if (existingLockRecord.isPresent) {
            // two scenarios from client point of view :
            // 1. someone is cheating us (trying to release someone's other lock)
            // 2. the very old owner woke up suddenly and tried to release "its" lock (sorry it belongs to someone else now)
            throw DLockTriedToBreakNotOwnedLockException("Cannot break not owned lock. Requested handle: $lockHandle")
        }
    }

    private fun createNewLock(lockKey: String, expirationSeconds: Long): DLockHandle {
        val newDLockRecord = createDLockRecord(lockKey, expirationSeconds)
        lockRepository.createLock(newDLockRecord)

        return DLockHandle(newDLockRecord.lockKey, newDLockRecord.lockHandleId)
    }

    private fun createDLockRecord(lockKey: String, expirationSeconds: Long): DLockRecord {
        val lockHandleId = lockHandleIdGenerator.generate()

        return DLockRecord(lockKey, lockHandleId, dateTimeProvider.now(), expirationSeconds)
    }

    private fun expired(currentLockRecord: Optional<DLockRecord>): Boolean {
        return currentLockRecord.isPresent.not() ||
                lockExpirationPolicy.expired(currentLockRecord.get().createdTime,
                        currentLockRecord.get().expirationSeconds)
    }

    private fun isOwner(existingLockRecord: Optional<DLockRecord>, lockHandle: DLockHandle): Boolean {
        return existingLockRecord.isPresent && lockHandle == asHandle(existingLockRecord.get())
    }

    private fun breakLockIfExists(lockRecord: Optional<DLockRecord>) {
        if (lockRecord.isPresent) {
            lockRepository.removeLock(lockRecord.get().lockKey)
        }
    }

    private fun asHandle(lockRecord: DLockRecord): DLockHandle {
        return DLockHandle(lockRecord.lockKey, lockRecord.lockHandleId)
    }

}