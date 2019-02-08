package com.malirz.dlock.repository

import com.malirz.dlock.exception.DLockAlreadyExistsException
import com.malirz.dlock.model.DLockRecord
import java.util.*

/**
 * Access database DLockRecord.
 *
 * @author Przemyslaw Malirz
 */
interface DLockRepository {

    /**
     * Inserts a brand new lock into the database.
     *
     * @throws DLockAlreadyExistsException when a lock with the same lockKey already exists
     */
    @Throws(DLockAlreadyExistsException::class)
    fun createLock(lockRecord: DLockRecord)

    /** Find a given lock by its key.
     *
     *  @return lock if exists... if not then Optional.empty is returned
     */
    fun findLock(lockKey: String): Optional<DLockRecord>

    /**
     * Deletes a lock from the database by its key.
     * Does nothing when the lock with a given lockKey does not exist.
     */
    fun removeLock(lockKey: String)

}