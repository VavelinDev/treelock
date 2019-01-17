package com.mli.dlock.exception

/**
 * Thrown when someone (some process) wants to break a lock owned by someone else (other process).
 * If are not the owner (you do not own the handle) you cannot break the lock.
 *
 * @author Przemyslaw Malirz
 */
class DLockTriedToBreakNotOwnedLockException(msg: String) : Exception(msg)