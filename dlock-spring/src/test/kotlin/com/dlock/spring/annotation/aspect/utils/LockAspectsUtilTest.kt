package com.dlock.spring.annotation.aspect.utils

import com.dlock.spring.annotation.LockKeyParam
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.javaMethod

/**
 * Tests of the AspectsUtil class.
 *
 * @author Przemyslaw Malirz
 */
internal class LockAspectsUtilTest {

    @Test
    fun getLockKeyMethodParameters_AllParameters() {
        // given:
        val targetMethod = LockAspectsUtilTest::testMethodParams.javaMethod
        checkNotNull(targetMethod)

        // when:
        val parameters = LockAspectsUtil.getLockKeyMethodParameters(targetMethod)

        // then:
        assertThat(parameters.size, equalTo(2))
        val containsAllParams = parameters.containsAll(
                listOf(Pair(0, "aParam"), Pair(1, "bParam"))
        )
        assertThat(containsAllParams, `is`(true))
    }

    @Test
    fun getLockKeyMethodParameters_PartParameters() {
        // given:
        val targetMethod = LockAspectsUtilTest::testMethodPartParams.javaMethod
        checkNotNull(targetMethod)

        // when:
        val parameters = LockAspectsUtil.getLockKeyMethodParameters(targetMethod)

        // then:
        assertThat(parameters.size, equalTo(1))
        val containsAllParams = parameters.containsAll(
                listOf(Pair(0, "aParam"))
        )
        assertThat(containsAllParams, `is`(true))
    }

    @Test
    fun getLockKeyMethodParameters_NoParameters() {
        // given:
        val targetMethod = LockAspectsUtilTest::testMethodNoParams.javaMethod
        checkNotNull(targetMethod)

        // when:
        val parameters = LockAspectsUtil.getLockKeyMethodParameters(targetMethod)

        // then:
        assertThat(parameters.size, equalTo(0))
    }

    fun testMethodParams(@LockKeyParam("aParam") a: Int, @LockKeyParam("bParam") b: String) {}

    fun testMethodPartParams(@LockKeyParam("aParam") a: Int, b: String) {}

    fun testMethodNoParams() {}

}