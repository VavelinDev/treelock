package com.dlock.time

import java.time.LocalDateTime

/**
 * Managed date/time provider.
 *
 * @author Przemyslaw Malirz
 */
interface DateTimeProvider {

    companion object DefaultDateTimeProvider : DateTimeProvider

    /**
     * Returns LocalDateTime.now(). It's not static so can be mocked / replaced by any NOW provider.
     * The hardest part in NOW in tests is it's always different ;)
     */
    fun now(): LocalDateTime = LocalDateTime.now()

}