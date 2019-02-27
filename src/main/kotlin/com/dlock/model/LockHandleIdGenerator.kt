package com.dlock.model

/**
 * Generate unique handle ID when a lock is created.
 *
 * @author Przemyslaw Malirz
 */
interface LockHandleIdGenerator {
    fun generate(): String
}