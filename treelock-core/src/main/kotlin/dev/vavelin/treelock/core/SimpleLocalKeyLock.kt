package dev.vavelin.treelock.core

import dev.vavelin.treelock.core.expiration.LocalLockExpirationPolicy
import dev.vavelin.treelock.core.handle.LockHandleUUIDIdGenerator
import dev.vavelin.treelock.core.repository.LocalLockRepository
import dev.vavelin.treelock.core.util.time.DateTimeProvider

/**
 * Local, memory-based key lock (uses LocalLockRepository).
 *
 * @author Przemyslaw Malirz
 */
object SimpleLocalKeyLock: SimpleKeyLock(
        LocalLockRepository(DateTimeProvider),
        LockHandleUUIDIdGenerator(),
        LocalLockExpirationPolicy(),
        DateTimeProvider
)