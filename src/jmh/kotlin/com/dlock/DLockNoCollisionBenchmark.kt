package com.dlock

import com.dlock.api.DLock
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.h2.tools.Server
import org.openjdk.jmh.annotations.*
import java.util.*
import java.util.concurrent.TimeUnit

open class DLockNoCollisionBenchmark {

    @State(Scope.Benchmark)
    open class ExecutionPlan {

        lateinit var dLock: DLock
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

            dLock = DBDLockBuilder().dataSource(dataSource).createDatabase(true).build()
        }

    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    fun tryAndReleaseLockNoCollision(executionPlan: ExecutionPlan) {
        val lockHandle = executionPlan.dLock.tryLock(UUID.randomUUID().toString(), 1)
        executionPlan.dLock.release(lockHandle.get())
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    fun tryLockNoCollision(executionPlan: ExecutionPlan) {
        executionPlan.dLock.tryLock(UUID.randomUUID().toString(), 1)
    }

}
