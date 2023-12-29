package dev.vavelin.treelock.spring.annotation

/**
 * Define parameters for the Lock annotation.
 *
 * @author Przemyslaw Malirz
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class LockKeyParam(val name: String)