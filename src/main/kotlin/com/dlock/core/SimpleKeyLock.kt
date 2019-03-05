package com.dlock.core

import com.dlock.api.KeyLock
import com.dlock.api.LockHandle
import com.dlock.core.expiration.LockExpirationPolicy
import com.dlock.core.repository.LockRepository
import com.dlock.core.handle.LockHandleIdGenerator
import com.dlock.core.model.LockRecord
import com.dlock.util.time.DateTimeProvider
import java.util.*

/**
 * The simplest implementation of the {@link KeyLock} interface based on the repository.
 *
 * @author Przemyslaw Malirz
 */
class SimpleKeyLock(
        private val lockRepository: LockRepository,
        private val lockHandleIdGenerator: LockHandleIdGenerator,
        private val lockExpirationPolicy: LockExpirationPolicy,
        private val dateTimeProvider: DateTimeProvider)
    : KeyLock {

    override fun tryLock(lockKey: String, expirationSeconds: Long): Optional<LockHandle> {
        val currentLockRecord = lockRepository.findLockByKey(lockKey)
        return if (expired(currentLockRecord)) {
            breakLockIfExists(currentLockRecord)
            return createNewLock(lockKey, expirationSeconds)
        } else {
            Optional.empty()
        }
    }

    override fun unlock(lockHandle: LockHandle) {
        val lockRecord = lockRepository.findLockByHandleId(lockHandle.handleId)
        breakLockIfExists(lockRecord)
    }

    private fun createNewLock(lockKey: String, expirationSeconds: Long): Optional<LockHandle> {
        val newLockRecord = createLockRecord(lockKey, expirationSeconds)
        val lockCreated = lockRepository.createLock(newLockRecord)
        return if (lockCreated) {
            Optional.of(LockHandle(newLockRecord.lockHandleId))
        } else {
            Optional.empty()
        }
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