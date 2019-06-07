package com.dlock

import com.dlock.api.KeyLock
import com.dlock.infrastructure.jdbc.DatabaseType
import com.dlock.infrastructure.jdbc.builder.JDBCKeyLockBuilder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit


/**
 * Test with Oracle.
 *
 * @author Przemyslaw Malirz
 */
open class KeyLockCollisionOracleBenchmark {

    @State(Scope.Benchmark)
    open class ExecutionPlan {

        lateinit var keyLock: KeyLock

        var LOCK_KEY = "AAA"

        @Setup(Level.Iteration)
        fun setUp() {
            val config = HikariConfig()

            config.jdbcUrl = "jdbc:oracle:thin:@localhost:1521:XE"
            config.username = "dlock"
            config.password = "dlock"
            config.isAutoCommit = true
            config.addDataSourceProperty("maximumPoolSize", "1000")
            val dataSource = HikariDataSource(config)

            keyLock = JDBCKeyLockBuilder().dataSource(dataSource)
                    .databaseType(DatabaseType.ORACLE)
                    .createDatabase(false).build()

            keyLock.tryLock(LOCK_KEY, 100000)
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    fun tryLockAlwaysCollision(executionPlan: ExecutionPlan) {
        executionPlan.keyLock.tryLock(executionPlan.LOCK_KEY, 1)
    }

}