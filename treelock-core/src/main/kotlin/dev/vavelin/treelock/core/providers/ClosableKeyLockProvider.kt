package dev.vavelin.treelock.core.providers

import dev.vavelin.treelock.api.KeyLock
import dev.vavelin.treelock.api.LockHandle
import java.util.function.Consumer

/**
 * Auto-closable {@link KeyLock} provider.
 *
 * @author Przemyslaw Malirz
 */
class ClosableKeyLockProvider(private val keyLock: KeyLock) {

    fun withLock(lockKey: String, expirationSeconds: Long, f: Consumer<LockHandle>) {
        val lock = keyLock.tryLock(lockKey, expirationSeconds)
        if (lock.isPresent) {
            ClosableLockHandle(lock.get()).use {
                f.accept(it.lockHandle)
            }
        }
    }

    fun withLock(lockKey: String, expirationSeconds: Long, f: (LockHandle) -> Unit) {
        val lock = keyLock.tryLock(lockKey, expirationSeconds)
        if (lock.isPresent) {
            ClosableLockHandle(lock.get()).use {
                f(it.lockHandle)
            }
        }
    }

    /**
     * Auto-closable {@link LockHandle}.
     */
    private inner class ClosableLockHandle(val lockHandle: LockHandle) : AutoCloseable {
        override fun close() {
            keyLock.unlock(lockHandle)
        }
    }

}