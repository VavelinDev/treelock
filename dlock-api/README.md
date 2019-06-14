# DLock API

DLock has minimal API. 

<img src="https://github.com/pmalirz/dlock/blob/master/dlock-api/doc/images/dlock-keylock-api.png">

##Classes and interfaces

######KeyLock
The main interface for a key based locks. Key locks are named and time-based locks. 
One can acquire a lock by a name with declared expiration seconds.
Caller is free to make the name even and foremost dynamically.
Expiration seconds is assumed / predicted (by the lock's owner) pessimistic expiration time.

######LockHandle
The handle is handover to the lock's owner (the one who acquired the lock).
Only lock's owner has the handle which uniquely identifies the lock.
Therefore only lock's owner can release it (still, the lock may expire before it is explicitly released).

##Usual usage

```java
final KeyLock dlock = <<create KeyLock instance>>
final Optional<DLockHandle> handle = dlock.tryLock("processing-reports-125", 300);
if(handle.isPresent()) {
    try {
        // process...
    } finally {
        handle.get().unlock();
    }
}
```