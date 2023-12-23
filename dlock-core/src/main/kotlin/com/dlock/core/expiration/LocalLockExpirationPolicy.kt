package com.dlock.core.expiration

import com.dlock.core.model.ReadLockRecord
import com.dlock.core.util.time.DateTimeProvider

/**
 * Expires the lock when createdTime + expirationSeconds > NOW.
 *
 * @author Przemyslaw Malirz
 */
class LocalLockExpirationPolicy(private val dataTimeProvider: DateTimeProvider) : LockExpirationPolicy {

    override fun expired(readLockRecord: ReadLockRecord): Boolean {
        val now = dataTimeProvider.now()

        return readLockRecord.createdTime.plusSeconds(readLockRecord.expirationSeconds).isBefore(now)
    }

}