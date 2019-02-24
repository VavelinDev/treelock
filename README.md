<img src="https://github.com/pmalirz/dlock/blob/master/doc/images/heart.png">

# DLock is a distributed lock backed by the database you use.

**D** stands for a **Distributed** and a **Database**

## General concept

So the concept is very simple. 

So you need a **distributed lock** to make your critical logic synchronous on all your nodes.
Here is the lock library which gives you both reliability and simplicity.
Synchronization of nodes is backed by your database, which most likely, you already use in your project.
Obviously, **DLock** needs to be connected to the same database on all your nodes to ensure syncing mechanism.  

One of the good use cases for **DLock** is synchronizing schedules which runs concurrently on all your nodes.
_Quartz_ does that in the similar way, thru the central database structures.
Without Quartz we can still run our schedules synchronizing them with **DLock**  

Here is the sample code for the static schedule:

```java
@Schedule("* */15 * * * *")
void generateInvoice() {
    final Optional<DLockHandle> handle = dlock.tryLock("create-invoice", 300);
    if(handle.isPresent()) {
        try {
            // generate an invoice...
        } finally {
            handle.get().unlock();
        }
    }
}
```

## Architecture

## Set up your project

1) Download DLock library

2) Create DLOCK table in your database

By default DLock uses DLOCK for its table name. But you easily change it (see pt 3).
DDL scripts for different databases can by found inside the jar file or the DLock [sources](src/main/resources/db).

DDL for H2:
```sql
CREATE TABLE IF NOT EXISTS @@tableName@@ (
  LCK_KEY varchar(100) PRIMARY KEY,
  LCK_HNDL_ID varchar(100) not null,
  CREATED_TIME DATETIME not null,
  EXPIRE_SEC int not null
);
```

**LCK_KEY** column has to be unique!  

3) Build up your DLock instance 

```java
@Bean
public DLock createDLock() {
    return new DBDLockBuilder().dataSource(dataSource).lockTableName("MY_D_LOCK").build();
}
```

Oh and remember. DLock instance is thread-safe. 
 
  
 

 
  



