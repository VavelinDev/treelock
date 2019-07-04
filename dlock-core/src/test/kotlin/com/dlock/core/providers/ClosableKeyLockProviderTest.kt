package com.dlock.core.providers

import com.dlock.api.KeyLock
import com.dlock.api.LockHandle
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
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
        Mockito.`when`(keyLock.tryLock("a", 1)).thenReturn(Optional.of(LockHandle("xyz")))

        val keyLockProvider = ClosableKeyLockProvider(keyLock)

        lateinit var lockHandle: LockHandle

        keyLockProvider.tryLock("a", 1).use {
            lockHandle = it.lockHandle.get()
            Assertions.assertEquals("xyz", lockHandle.handleId)
        }

        verify(keyLock).tryLock("a", 1)
        verify(keyLock).unlock(lockHandle)
        verifyNoMoreInteractions(keyLock)
    }

    @Test
    fun tryLock_LockNotAcquired() {
        val keyLock = mock(KeyLock::class.java)
        Mockito.`when`(keyLock.tryLock("a", 1)).thenReturn(Optional.empty())

        val keyLockProvider = ClosableKeyLockProvider(keyLock)

        keyLockProvider.tryLock("a", 1).use {
            Assertions.assertFalse(it.lockHandle.isPresent)
        }

        verify(keyLock).tryLock("a", 1)
        verifyNoMoreInteractions(keyLock)
    }

}