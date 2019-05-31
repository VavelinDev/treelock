package com.dlock.core.model

import java.time.LocalDateTime

/**
 * DB representation of a lock.
 *
 * @author Przemyslaw Malirz
 */
data class LockRecord(val lockKey: String,
                      val lockHandleId: String,
                      val createdTime: LocalDateTime,
                      val expirationSeconds: Long)