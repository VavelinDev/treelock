package com.dlock

import com.dlock.api.KeyLock
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.h2.tools.Server
import org.openjdk.jmh.annotations.*
import java.util.*
import java.util.concurrent.TimeUnit

open class KeyLockNoCollisionBenchmark {

    @State(Scope.Benchmark)
    open class ExecutionPlan {

        lateinit var keyLock: KeyLock
        lateinit var webServer: Server

        @Setup(Level.Trial)
        fun start() {
            webServer = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092")
            webServer.start()
        }

        @TearDown(Level.Trial)
        fun shutdown() {
            webServer.stop()
        }

        @Setup(Level.Iteration)
        fun setUp() {
            val config = HikariConfig()

            config.jdbcUrl = "jdbc:h2:tcp://localhost:9092/~/test"
            config.username = "sa"
            config.password = ""
            config.isAutoCommit = true

            config.addDataSourceProperty("cachePrepStmts", "true")
            config.addDataSourceProperty("prepStmtCacheSize", "250")
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            config.addDataSourceProperty("maximumPoolSize", "1000")

            val dataSource = HikariDataSource(config)

            keyLock = DBKeyLockBuilder().dataSource(dataSource).createDatabase(true).build()
        }

    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    fun tryAndReleaseLockNoCollision(executionPlan: ExecutionPlan) {
        val lockHandle = executionPlan.keyLock.tryLock(UUID.randomUUID().toString(), 1)
        executionPlan.keyLock.unlock(lockHandle.get())
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    fun tryLockNoCollision(executionPlan: ExecutionPlan) {
        executionPlan.keyLock.tryLock(UUID.randomUUID().toString(), 1)
    }

}
