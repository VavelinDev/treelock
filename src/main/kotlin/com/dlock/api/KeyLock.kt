package com.dlock.api

import com.dlock.exception.LockAlreadyExistsException
import java.util.*

/**
 * KeyLock main interface. It represents the main API of the library (distributed lock).
 * KeyLock's implementation must be thread-safe.
 *
 * @author Przemyslaw Malirz
 */
interface KeyLock {

    /**
     * Gets a lock for a given amount of time, if available (providing the handle of that lock).
     * If the lock is taken by someone else {@link Optional#empty} is returned.
     */
    @Throws(LockAlreadyExistsException::class)
    fun tryLock(lockKey: String, expirationSeconds: Long): Optional<LockHandle>

    /**
     * Releases a given lock. In case it is already taken by someone else an exception is thrown.
     */
    fun unlock(lockHandle: LockHandle)

}