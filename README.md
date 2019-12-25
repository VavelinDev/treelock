<img src="https://github.com/pmalirz/dlock/blob/master/doc/images/dlock-logo.png">

# dlock - distributed locking backed by your database

## How to use it

Let's get a distributed lock using **auto-closable API**
```java
try (ClosableKeyLockProvider.ClosableLockHandle closableLockHandle = keyLockProvider.tryLock("/invoice/pay/4587", 900)) {
    lockHandle = closableLockHandle.getLockHandle().orElse(null);
}
```
or on a standard way
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

## What is dlock

**dlock** aims to be a deadly simple and reliable solution for distributed locking problem. 
Safety and simplicity is the top priority of dlock.

**dlock** is a repository-based lock, meaning the locks are backed by a centrally placed database.

Central database is what majority of standard business project has in place. 
Why not to use it also as a lock synchronization engine too? 
Do you need a separate lock orchestrator / solution? Do you have to introduce yet another
block to your architecture, increasing the overall complexity? Perhaps not.
I believe **dlock** ways is as simple as sufficient approach in many cases.

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
Also, has a few unit and jmh tests to test performance, thread-safety and consistency  

All **dlock** implementations must be thread-safe.
That being said, a KeyLock instance can be shared by many threads.

What's _KeyLock_? _KeyLock_ is the main interface for dlock library.
You get and release your named (named by key) locks.  

**dlock** is meant to be as reliable as your database transactions are.

_P.S. I don't mind using zookeeper, hazelcast and other great frameworks for distributed locking. 
However, you can simply make use of your current infrastructure for the sake of simplicity._

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

### Afterword

I also use **dlock** approach thru the REST API.
_KeyLock_ instance is exposed via simple RESTful methods.
It may be a good candidate for an another module REST auto-configurable controllers.

Also, _LockHandle_ is open for further extension, e.g. adding fencing token. 

