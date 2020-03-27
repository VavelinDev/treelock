package com.dlock.spring.annotation

/**
 * Lock annotation can be placed on a method.
 *
 * @author Przemyslaw Malirz
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Lock(val key: String, val expirationSeconds: Long)