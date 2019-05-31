package com.dlock.core.handle

import java.util.*

/**
 * UUID based handle ID generator.
 *
 * @author Przemyslaw Malirz
 */
class LockHandleUUIDIdGenerator : LockHandleIdGenerator {

    override fun generate(): String {
        return UUID.randomUUID().toString()
    }

}