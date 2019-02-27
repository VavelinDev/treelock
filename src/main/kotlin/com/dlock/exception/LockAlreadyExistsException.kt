package com.dlock.exception

import java.lang.Exception
import java.sql.SQLIntegrityConstraintViolationException

/**
 * Lock cannot be acquired as it's already been taken by someone else (other process)
 *
 * @author Przemyslaw Malirz
 */
class LockAlreadyExistsException(msg: String, ex: SQLIntegrityConstraintViolationException) : Exception(msg, ex)