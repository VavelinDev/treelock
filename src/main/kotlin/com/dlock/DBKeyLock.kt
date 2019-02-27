package com.dlock

import com.dlock.api.KeyLock
import com.dlock.api.LockHandle
import com.dlock.expiration.LockExpirationPolicy
import com.dlock.model.LockHandleIdGenerator
import com.dlock.model.LockRecord
import com.dlock.repository.JDBCLockRepository
import com.dlock.time.DateTimeProvider
import java.util.*

/**
 * KeyLock based on plain JDBC.
 *
 * @author Przemyslaw Malirz
 */
class DBKeyLock(
        private val lockRepository: JDBCLockRepository,
        private val lockHandleIdGenerator: LockHandleIdGenerator,
        private val lockExpirationPolicy: LockExpirationPolicy,
        private val dateTimeProvider: DateTimeProvider)
    : KeyLock {

    override fun tryLock(lockKey: String, expirationSeconds: Long): Optional<LockHandle> {
        val currentLockRecord = lockRepository.findLockByKey(lockKey)

        return if (expired(currentLockRecord)) {
            breakLockIfExists(currentLockRecord)
            val createNewLock = createNewLock(lockKey, expirationSeconds)
            Optional.of(createNewLock)
        } else {
            Optional.empty()
        }

    }

    override fun unlock(lockHandle: LockHandle) {
        val lockRecord = lockRepository.findLockByHandleId(lockHandle.handleId)
        breakLockIfExists(lockRecord)
    }

    private fun createNewLock(lockKey: String, expirationSeconds: Long): LockHandle {
        val newLockRecord = createLockRecord(lockKey, expirationSeconds)
        lockRepository.createLock(newLockRecord)

        return LockHandle(newLockRecord.lockHandleId)
    }

    private fun createLockRecord(lockKey: String, expirationSeconds: Long): LockRecord {
        val lockHandleId = lockHandleIdGenerator.generate()

        return LockRecord(lockKey, lockHandleId, dateTimeProvider.now(), expirationSeconds)
    }

    private fun expired(currentLockRecord: Optional<LockRecord>): Boolean {
        return currentLockRecord.isPresent.not() ||
                lockExpirationPolicy.expired(currentLockRecord.get().createdTime,
                        currentLockRecord.get().expirationSeconds)
    }

    private fun breakLockIfExists(lockRecord: Optional<LockRecord>) {
        if (lockRecord.isPresent) {
            lockRepository.removeLock(lockRecord.get().lockHandleId)
        }
    }

}