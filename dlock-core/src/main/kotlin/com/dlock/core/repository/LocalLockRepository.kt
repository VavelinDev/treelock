package com.dlock.core.repository

import com.dlock.core.model.LockRecord
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * This is an in-memory repository implementation backed by the concurrent Set implementation
 * ({@link ConcurrentHashMap#newKeySet}).
 *
 * @author Przemyslaw Malirz
 */
class LocalLockRepository : LockRepository {

    private val NAMED_LOCK = ConcurrentHashMap.newKeySet<LockRecord>()

    override fun createLock(lockRecord: LockRecord): Boolean {
        return NAMED_LOCK.add(lockRecord)
    }

    override fun findLockByHandleId(lockHandleId: String): Optional<LockRecord> {
        return Optional.ofNullable(NAMED_LOCK.find { k -> k.lockHandleId == lockHandleId })
    }

    override fun findLockByKey(lockKey: String): Optional<LockRecord> {
        return Optional.ofNullable(NAMED_LOCK.find { k -> k.lockKey == lockKey })
    }

    override fun removeLock(lockHandleId: String) {
        NAMED_LOCK.removeIf { k -> k.lockHandleId == lockHandleId }
    }

}
