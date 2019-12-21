<img src="https://github.com/pmalirz/dlock/blob/master/doc/images/heart.png">

# dlock - synchronization backed by your database

dlock aims to be 100% reliable. Safety and simplicity is the top priority of dlock.
Performance, still important, is the on the second place of it's consideration.

But foremost, dlock is my first open source project and perhaps you may like it

dlock is a repository-based lock, meaning the locks are backed by a centrally placed database.
I've been using that simple approach in many business projects projects which became an inspiration for that project. 

Central database is what majority of standard business project has in place, out of the box we can say. 
Why not to use it as a distributed lock sync engine too? 
Do you need an another lock orchestrator / engine? Do you need to introduce yet another
block to your architecture, increasing the overall complexity? Perhaps not.
I believe this is as simple as sufficient approach in many cases.

The project is composed of 3 modules
* **dlock-api**
base interfaces for dlock implementations. It gives a good outline of dlock capabilities.
Contains the key interface *KeyLock*, which is a humble API for working with locks.
* **dlock-core**
base, abstract classes, common for all the specialized implementations (e.g. JDBC) such as: 
expiration policies, lock model, repository interfaces
* **dlock-jdbc**
JDBC implementation of *dlock* backed by your central database.
Can be adapted to any SQL-standard database.
Also, has a few unit and jmh tests to test performance, thread-safety and consistency  

All *dlock* implementations must be thread-safe.
That being said, a KeyLock instance can be shared by many threads.

What's *KeyLock*? *KeyLock* is the main interface for dlock library.
You get and release your named (named by key) locks.  

dlock is as reliable as your database transactions are.

_P.S. I don't mind using zookeeper, hazelcast and other great frameworks for distributed locking. 
However, you can simply make use of your current infrastructure for the sake of simplicity._

## Usage example

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
It's because we rely on the _unlock()_ part which is going to release the lock.

## Architecture

### Set up your project

1) Download *dlock* library

2) Create _DLOCK_ table in your database

By default *dlock* uses DLOCK for the main table name. 
However you are free to change it (see pt 3).
DDL scripts for different database types can by found inside the jar file or the dlock [sources](dlock-jdbc/src/main/resources/db).

DDL for H2:
```sql
CREATE TABLE IF NOT EXISTS "@@tableName@@" (
  "LCK_KEY" varchar(100) PRIMARY KEY,
  "LCK_HNDL_ID" varchar(100) not null,
  "CREATED_TIME" DATETIME not null,
  "EXPIRE_SEC" int not null
);
CREATE UNIQUE INDEX IF NOT EXISTS "@@tableName@@_HNDL_UX" ON  "@@tableName@@" ("LCK_HNDL_ID");
```

**LCK_KEY** column has to be unique!  

3) Create your *dlock* instance 

```java
@Bean
public KeyLock createDlock() {
    return JDBCKeyLockBuilder().dataSource(dataSource).databaseType(DatabaseType.H2).build();
}
```

Oh and remember again, *KeyLock* instance is thread-safe. 

### Afterword

I also use *dlock* approach thru the REST API.
In other words, *KeyLock* instance is exposed via simple RESTful methods.

