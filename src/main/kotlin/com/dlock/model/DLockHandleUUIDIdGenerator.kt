package com.dlock.model

import java.util.*

/**
 * UUID based handle ID generator.
 *
 * @author Przemyslaw Malirz
 */
class DLockHandleUUIDIdGenerator : DLockHandleIdGenerator {

    override fun generate(): String {
        return UUID.randomUUID().toString()
    }

}