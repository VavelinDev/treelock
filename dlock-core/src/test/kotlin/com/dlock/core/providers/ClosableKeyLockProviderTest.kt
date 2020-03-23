package com.dlock.core.providers

import com.dlock.api.KeyLock
import com.dlock.api.LockHandle
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.util.*

/**
 * Tests of {@link ClosableKeyLockProvider}
 *
 * @author Przemyslaw Malirz
 */
internal class ClosableKeyLockProviderTest {

    @Test
    fun tryLock_LockAcquired() {
        val keyLock = mock(KeyLock::class.java)
        `when`(keyLock.tryLock("a", 1)).thenReturn(Optional.of(LockHandle("xyz")))

        val keyLockProvider = ClosableKeyLockProvider(keyLock)

        lateinit var lockHandle: LockHandle

        keyLockProvider.withLock("a", 1) {
            lockHandle = it
            Assertions.assertEquals("xyz", it.handleId)
        }

        verify(keyLock).tryLock("a", 1)
        verify(keyLock).unlock(lockHandle)
        verifyNoMoreInteractions(keyLock)
    }

    @Test
    fun tryLock_LockNotAcquired() {
        val keyLock = mock(KeyLock::class.java)
        `when`(keyLock.tryLock("a", 1)).thenReturn(Optional.empty())

        val keyLockProvider = ClosableKeyLockProvider(keyLock)

        keyLockProvider.withLock("a", 1) {
            fail { "Lock should not be taken" }
        }

        verify(keyLock).tryLock("a", 1)
        verifyNoMoreInteractions(keyLock)
    }

}