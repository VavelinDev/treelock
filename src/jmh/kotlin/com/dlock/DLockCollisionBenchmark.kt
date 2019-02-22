package com.dlock

import com.dlock.api.DLock
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

open class DLockCollisionBenchmark {

    @State(Scope.Benchmark)
    open class ExecutionPlan {

        lateinit var dLock: DLock

        var LOCK_KEY = "AAA"

        @Setup(Level.Iteration)
        fun setUp() {
            val config = HikariConfig()

            config.jdbcUrl = "jdbc:h2:mem:test"
            config.username = "sa"
            config.password = ""
            config.isAutoCommit = true
            config.addDataSourceProperty("maximumPoolSize", "1000")
            val dataSource = HikariDataSource(config)

            dLock = DBDLockBuilder().dataSource(dataSource).createDatabase(true).build()

            dLock.tryLock(LOCK_KEY, 100000)
        }

    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    fun tryLockAlwaysCollision(executionPlan: ExecutionPlan) {
        executionPlan.dLock.tryLock(executionPlan.LOCK_KEY, 1)
    }

}
