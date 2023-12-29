package dev.vavelin.treelock.api

import java.util.*

/**
 * KeyLock is the main interface of the TreeLock. It represents the main API of the library.
 * KeyLock's implementation must be thread-safe.
 */
interface KeyLock {

    /**
     * Gets a lock for a given amount of time, if available (providing the handle of that lock).
     * If the lock is taken by someone there is no exception thrown but simply {@link Optional#empty} is returned.
     */
    fun tryLock(lockKey: String, expirationSeconds: Long): Optional<LockHandle>

    /**
     * Releases a given lock. If lock with a given handle does not exist nothings happen.
     */
    fun unlock(lockHandle: LockHandle)

}