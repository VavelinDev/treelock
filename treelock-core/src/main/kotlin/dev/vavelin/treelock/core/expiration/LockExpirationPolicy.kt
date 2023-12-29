package dev.vavelin.treelock.core.expiration

import dev.vavelin.treelock.core.model.ReadLockRecord

/**
 * Lock expiration policy.
 *
 * @author Przemyslaw Malirz
 */
interface LockExpirationPolicy {

    /**
     * Returns true when the lock should expire.
     *
     * @param readLockRecord
     */
    fun expired(readLockRecord: ReadLockRecord): Boolean

}