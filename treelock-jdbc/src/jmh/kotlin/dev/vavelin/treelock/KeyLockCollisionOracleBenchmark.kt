package dev.vavelin.treelock

import dev.vavelin.treelock.api.KeyLock
import dev.vavelin.treelock.jdbc.DatabaseType
import dev.vavelin.treelock.jdbc.builder.JDBCKeyLockBuilder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Setup


/**
 * Test with Oracle.
 *
 * @author Przemyslaw Malirz
 */
open class KeyLockCollisionOracleBenchmark {

    //@State(Scope.Benchmark)
    open class ExecutionPlan {

        lateinit var keyLock: KeyLock

        var LOCK_KEY = "AAA"

        @Setup(Level.Iteration)
        fun setUp() {
            val config = HikariConfig()

            config.jdbcUrl = "jdbc:oracle:thin:@localhost:1521:XE"
            config.username = "treelock"
            config.password = "treelock"
            config.isAutoCommit = true
            config.addDataSourceProperty("maximumPoolSize", "1000")
            val dataSource = HikariDataSource(config)

            keyLock = JDBCKeyLockBuilder().dataSource(dataSource)
                    .databaseType(DatabaseType.ORACLE)
                    .createDatabase(false).build()

            keyLock.tryLock(LOCK_KEY, 100000)
        }
    }

 //   @Benchmark
 //   @BenchmarkMode(Mode.Throughput)
 //   @OutputTimeUnit(TimeUnit.MILLISECONDS)
    fun tryLockAlwaysCollision(executionPlan: ExecutionPlan) {
        executionPlan.keyLock.tryLock(executionPlan.LOCK_KEY, 1)
    }

}