package com.dlock.core.expiration

import com.dlock.util.time.DateTimeProvider
import java.time.LocalDateTime

/**
 * Expires the lock when createdTime + expirationSeconds > NOW.
 *
 * @author Przemyslaw Malirz
 */
class LocalLockExpirationPolicy(private val dataTimeProvider: DateTimeProvider) : LockExpirationPolicy {

    override fun expired(createdTime: LocalDateTime, expirationSeconds: Long): Boolean {
        val now = dataTimeProvider.now()

        return createdTime.plusSeconds(expirationSeconds).isBefore(now)
    }

}