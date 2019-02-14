package com.dlock.api

import com.dlock.exception.DLockTriedToBreakNotOwnedLockException
import java.util.*

/**
 * DLock main interface. It represents the main API of the library (distributed lock).
 * DLock's implementation must be thread-safe.
 *
 * @author Przemyslaw Malirz
 */
interface DLock {

    /**
     * Gets a lock for a given amount of time, if available (providing the handle of that lock).
     * If the lock is taken by someone else {@link Optional#empty} is returned.
     */
    fun tryLock(lockKey: String, expirationSeconds: Long): Optional<DLockHandle>

    /**
     * Releases a given lock. In case it is taken by someone else already an exception is thrown.
     */
    @Throws(DLockTriedToBreakNotOwnedLockException::class)
    fun release(lockHandle: DLockHandle)

}