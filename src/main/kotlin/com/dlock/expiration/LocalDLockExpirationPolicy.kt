package com.dlock.expiration

import com.dlock.time.DateTimeProvider
import java.time.LocalDateTime

/**
 * Expires the lock when createdTime + expirationSeconds > NOW.
 *
 * @author Przemyslaw Malirz
 */
class LocalDLockExpirationPolicy(private val dataTimeProvider: DateTimeProvider) : DLockExpirationPolicy {

    override fun expired(createdTime: LocalDateTime, expirationSeconds: Long): Boolean {
        val now = dataTimeProvider.now()

        return createdTime.plusSeconds(expirationSeconds).isBefore(now)
    }

}