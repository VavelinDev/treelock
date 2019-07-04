package com.dlock.core.providers

import com.dlock.api.KeyLock
import com.dlock.api.LockHandle
import java.util.*

/**
 * Auto-closable {@link KeyLock} provider.
 *
 * @author Przemyslaw Malirz
 */
class ClosableKeyLockProvider(private val keyLock: KeyLock) {

    fun tryLock(lockKey: String, expirationSeconds: Long): ClosableLockHandle {
        val lock = keyLock.tryLock(lockKey, expirationSeconds)
        return ClosableLockHandle(lock)
    }

    /**
     * Auto-closable {@link LockHandle}.
     */
    inner class ClosableLockHandle(val lockHandle: Optional<LockHandle>) : AutoCloseable {
       override fun close() {
            if(lockHandle.isPresent) {
                keyLock.unlock(lockHandle.get())
            }
        }
    }

}