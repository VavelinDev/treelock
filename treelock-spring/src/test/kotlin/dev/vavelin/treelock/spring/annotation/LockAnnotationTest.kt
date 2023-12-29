package dev.vavelin.treelock.spring.annotation

import dev.vavelin.treelock.api.KeyLock
import dev.vavelin.treelock.core.SimpleLocalKeyLock
import dev.vavelin.treelock.core.providers.ClosableKeyLockProvider
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.stereotype.Service
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig


/**
 * Tests of {@link LockAnnotation}
 *
 * @author Przemyslaw Malirz
 */
@SpringJUnitConfig(LockAnnotationTest.Config::class)
internal class LockAnnotationTest {

    @Autowired
    lateinit var a: AI

    @Autowired
    lateinit var keyLock: KeyLock

    companion object {
        // counts method executions
        var counterA = 0
    }

    @Test
    fun testLockAnnotation() {
        counterA = 0

        a.payInvoice(100L)
        a.payInvoice(100L)

        assertThat(counterA, equalTo(2))
    }

    @Test
    fun testLockAnnotationLocked_Collision() {
        counterA = 0

        val lock = keyLock.tryLock("/invoice/100", 1000L)
        a.payInvoice(100L)
        keyLock.unlock(lock.get())
        a.payInvoice(100L)

        assertThat(counterA, equalTo(1))
    }

    @Test
    fun testLockAnnotationLocked_NoCollision() {
        counterA = 0

        val lock = keyLock.tryLock("/invoice/100", 1000L)
        a.payInvoice(200L)
        keyLock.unlock(lock.get())
        a.payInvoice(100L)

        assertThat(counterA, equalTo(2))
    }

    // BEANS -------------------------------------------------

    @Configuration
    @ComponentScan(basePackages = ["dev.vavelin.treelock"])
    @EnableAspectJAutoProxy
    open class Config {

        @Bean
        open fun keyLock() = SimpleLocalKeyLock

        @Bean
        open fun createLocalKeyLock(keyLock: KeyLock) = ClosableKeyLockProvider(keyLock)

    }

    @Service
    open class A : AI {
        @Lock(key = "/invoice/{invoiceId}", expirationSeconds = 1000L)
        override fun payInvoice(@LockKeyParam("invoiceId") invoiceId: Long) {
            counterA++
        }
    }

    interface AI {
        fun payInvoice(invoiceId: Long)
    }

}

