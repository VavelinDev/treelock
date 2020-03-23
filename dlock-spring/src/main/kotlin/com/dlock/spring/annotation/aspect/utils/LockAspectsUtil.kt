package com.dlock.spring.annotation.aspect.utils

import com.dlock.spring.annotation.LockKeyParam
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.Method

/**
 * Helps to work with Lock annotations.
 *
 * @author Przemyslaw Malirz
 */
object LockAspectsUtil {

    /**
     * Gets an actual / target method. When the join-point method points to the interface then the method of
     * the actual object / bean is returned.
     *
     * @return target method of an object (bean)
     */
    fun getMethod(joinPoint: ProceedingJoinPoint): Method? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method

        return if (method.declaringClass.isInterface) {
            joinPoint.target.javaClass.getDeclaredMethod(joinPoint.signature.name, *method.parameterTypes)
        } else {
            signature.method
        }
    }

    /**
     * Returns all the method parameters annotated with the @LockKeyParam annotation.
     *
     * @return Pair (parameterIndex, @LockKeyParam.name)
     */
    fun getLockKeyMethodParameters(method: Method): List<Pair<Int, String>> {
        return method.parameters.withIndex().mapNotNull {
            val paramIndex = it.index
            val paramName = AnnotationUtils.findAnnotation(method.parameters[it.index], LockKeyParam::class.java)?.name
            paramName?.let { paramNameNotNull -> Pair(paramIndex, paramNameNotNull) }
        }
    }


}