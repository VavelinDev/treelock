<img src="https://github.com/pmalirz/dlock/blob/master/doc/images/dlock-logo.png">

# dlock - distributed lock backed by your database
[![CircleCI](https://circleci.com/gh/pmalirz/dlock.svg?style=svg)](https://circleci.com/gh/pmalirz/dlock)
[![Known Vulnerabilities](https://snyk.io/test/github/pmalirz/dlock/badge.svg)](https://snyk.io/test/github/pmalirz/dlock)
[![codecov](https://codecov.io/gh/pmalirz/dlock/branch/master/graph/badge.svg)](https://codecov.io/gh/pmalirz/dlock)
[![Download](https://api.bintray.com/packages/pmalirz/malitools/dlock-api/images/download.svg)](https://bintray.com/pmalirz/malitools)

## Declarative
You can enable distributed lock mechanism by adding the _@Lock_  annotation at the method level:
```java
@Lock(key = "/invoice/pay/{invoiceId}", expirationSeconds = 900L)
public void payInvoice(@LockKeyParam("invoiceId") final Long invoiceId) {
    // do processing...
}
```
The **dlock-spring** module enables this feature.

## Using dlock API
Declarative style depicted above is  the way to go for guarding the critical parts of your application, usually enclosed in methods.
Nevertheless nothing can stop you from using pure dlock API, applying the lock to any given fragment of your code.

Get a distributed lock using **autocloseable API**
```java
final ClosableKeyLockProvider keyLockProvider = new ClosableKeyLockProvider(keyLock);
keyLockProvider.withLock("/invoice/pay/4587", 900, lockHandle -> {
    // do processing...
});
```
or in a standard way
```java
final Optional<LockHandle> lockHandle = keyLock.tryLock("/invoice/pay/4587", 900);
if(lockHandle.isPresent()) {
    try {
        // perform some business logic
    } finally {
        handle.get().unlock();
    }
}
```

Of course, before we use a _KeyLock_ instance, we have to initialized. 
Create the _KeyLock_ global instance (one _KeyLock_ instance can be shared across multiple threads)
```kotlin
val config = HikariConfig()

config.jdbcUrl = "jdbc:oracle:thin:@localhost:1521:XE"
config.username = "myshop"
config.password = "*****"
config.isAutoCommit = true
config.addDataSourceProperty("maximumPoolSize", "1000")
val dataSource = HikariDataSource(config)

val keyLock = JDBCKeyLockBuilder().dataSource(dataSource)
        .databaseType(DatabaseType.ORACLE)
        .createDatabase(false).build()

val keyLockProvider = ClosableKeyLockProvider(keyLock)
```
The _KeyLock_ instance works on a separate database connection / transaction. 

## How the JDBC implementation works

### The short story of _tryLock_ and _ulnock_

The **_tryLock()_** method performs the following _INSERT_ query on the _DLCK_ table.
```java
final Optional<LockHandle> lockHandle = keyLock.tryLock("/invoice/pay/4587", 900);
```
```sql
INSERT INTO DLCK (LCK_KEY, LCK_HNDL_ID, CREATED_TIME, EXPIRE_SEC) 
SELECT '/invoice/pay/4587', 'da74f856-27d0-11ea-978f-2e728ce88125', '2019-12-31T06:40:12.623', '900' FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM DLCK WHERE LCK_KEY = '/invoice/pay/4587')
```

The **_unlock()_** method performs the following _DELETE_ query on the _DLCK_ table.
```java
keyLock.unlock(lockHandle.get())
```

```sql
DELETE FROM DLCK WHERE LCK_HNDL_ID = 'da74f856-27d0-11ea-978f-2e728ce88125'
```

The handle identifier (LCK_HNDL_ID) _'da74f856-27d0-11ea-978f-2e728ce88125'_ 
is known only to the lock's owner and is generated by the
implementation of the _LockHandleIdGenerator_ interface (_LockHandleUUIDIdGenerator_ by default).

The **dlock** idea relies on the fact that the _DLCK_ table cannot have more than one record with 
a given LCK_KEY (the name of your lock).

## What is dlock

**dlock** aims to be a deadly simple and reliable solution for distributed locking problem. 
Safety and simplicity is the top priority of dlock.

**dlock** is a repository-based lock, meaning the locks are backed by a centrally placed database.

Central database is what majority of standard business project has in place. 
Why not to use it as a lock synchronization engine (distributed lock manager) too? 
**dlock** way is simple and sufficient approach in many cases.

The project is composed of 3 modules
* **dlock-api**
base interfaces for dlock implementations. It gives a good outline of dlock capabilities.
Contains the key interface _KeyLock_, which is a humble API for working with locks.
* **dlock-core**
base, abstract classes, common for all the specialized implementations (e.g. JDBC) such as: 
expiration policies, lock model, repository interfaces
* **dlock-jdbc**
JDBC implementation of **dlock** backed by your central database.
Can be adapted to any SQL-standard database.
Also, has a few unit and jmh tests to test performance, thread-safety and consistency.
* **dlock-spring**
Support for spring framework. @Lock annotation allows to declare a distributed lock at the method level. 

All **dlock** implementations must be thread-safe.
That being said, a KeyLock instance can be shared by many threads.

What's _KeyLock_? _KeyLock_ is the main interface for dlock library.
You get and release your named (named by key) locks.  

The usage of JDBC implementation of the _KeyLock_ interface (_SimpleKeyLock_ class with the instance of _JDBCLockRepository_ injected as the repository property)  
should not take part in the long running business transaction, demarcated by a business method.
_KeyLock_ must work inside its own transaction, flushing and committing internal SQL instructions
immediately once _tryLock()_ or _unlock()_ is called.   

## Yet another usage example

One of the good use cases for **dlock** is synchronizing schedules which run concurrently on all your nodes.
_Quartz_ does that synchronization in a very similar way, thru the central database structures.
We can still run our schedules without _Quartz_, synchronizing them with **dlock**  

Here is the sample code for the static schedule:

```java
// Somewhere in the initialization part / config code (e.g. singleton bean)
KeyLock keyLock = JDBCKeyLockBuilder().dataSource(dataSource).databaseType(DatabaseType.H2).build();

// Somewhere in the class
@Schedule("* */15 * * * *")
void generateInvoice() {
    // Request a named ("create-invoice") lock which may last no longer that 300 seconds.
    // After 300 seconds "create-invoice" lock expires (can be taken by an another thread / process)
    final Optional<LockHandle> handle = keyLock.tryLock("create-invoice", 300);

    // Test wheter we got a lock successfully. dlock does not throw exceptions in case lock is taken by other process,
    // as such a situation is no an exceptional one
    if(handle.isPresent()) {
        try {
            // generate an invoice...
        } finally {
            // Self explanatory, let's just tidy up after ourselves
            handle.get().unlock();
        }
    }
}
``` 

As you have noticed, you had to declare an estimated time of how long your lock should be active.
This estimation should be as pessimistic as possible. 
It's because we rely on the _unlock()_ part which is going to release the lock eventually.
If the _unlock()_ execution fails the lock with a given name will be available again after the declared expiration time.

## Set up your project

1) Download **dlock** library

2) Create **DLOCK** table in your database

By default **dlock** uses DLOCK for the main table name. 
However you are free to change it (see pt 3).
DDL scripts for different database types can by found inside the jar file or the dlock [sources](dlock-jdbc/src/main/resources/db).

DDL for H2:
```sql
CREATE TABLE IF NOT EXISTS "@@tableName@@" (
  "LCK_KEY" varchar(1000) PRIMARY KEY,
  "LCK_HNDL_ID" varchar(100) not null,
  "CREATED_TIME" DATETIME not null,
  "EXPIRE_SEC" int not null
);
CREATE UNIQUE INDEX IF NOT EXISTS "@@tableName@@_HNDL_UX" ON  "@@tableName@@" ("LCK_HNDL_ID");
```

**LCK_KEY** column has to be unique!  

3) Create your _KeyLock_ instance 

```java
@Bean
public KeyLock createKeyLock() {
    return JDBCKeyLockBuilder().dataSource(dataSource).databaseType(DatabaseType.H2).build();
}
```

Remember, _KeyLock_ instance is thread-safe. 

