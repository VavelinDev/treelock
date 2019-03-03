package com.dlock.repository

import com.dlock.model.LockRecord
import java.util.*

/**
 * Access database LockRecord.
 *
 * @author Przemyslaw Malirz
 */
interface LockRepository {

    /**
     * Inserts a brand new lock into the database.
     *
     * @return {@code true} when record created
     */
    fun createLock(lockRecord: LockRecord): Boolean

    /** Find a given lock by its handle id.
     *
     *  @return lock if exists... if not then Optional.empty is returned
     */
    fun findLockByHandleId(lockHandleId: String): Optional<LockRecord>

    /** Find a given lock by its key.
     *
     *  @return lock if exists... if not then Optional.empty is returned
     */
    fun findLockByKey(lockKey: String): Optional<LockRecord>

    /**
     * Deletes a lock from the database by its key.
     * Does nothing when the lock with a given lockHandleId does not exist.
     */
    fun removeLock(lockHandleId: String)

}