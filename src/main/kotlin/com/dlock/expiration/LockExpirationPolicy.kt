package com.dlock.expiration

import java.time.LocalDateTime

/**
 * Lock expiration policy.
 *
 * @author Przemyslaw Malirz
 */
interface LockExpirationPolicy {

    /**
     * Returns true when the lock should expire.
     *
     * @param createdTime created time of the existing lock
     * @param expirationSeconds expiration seconds of the existing lock
     */
    fun expired(createdTime: LocalDateTime, expirationSeconds: Long): Boolean

}