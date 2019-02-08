package com.malirz.dlock.model

import java.time.LocalDateTime

/**
 * DB representation of a lock.
 *
 * @author Przemyslaw Malirz
 */
data class DLockRecord(val lockKey: String,
                       val lockHandleId: String,
                       val createdTime: LocalDateTime,
                       val expirationSeconds: Long)