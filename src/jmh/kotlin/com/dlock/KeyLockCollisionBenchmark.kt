package com.dlock

import com.dlock.api.KeyLock
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

open class KeyLockCollisionBenchmark {

    @State(Scope.Benchmark)
    open class ExecutionPlan {

        lateinit var keyLock: KeyLock

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

            keyLock = DBKeyLockBuilder().dataSource(dataSource).createDatabase(true).build()

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
