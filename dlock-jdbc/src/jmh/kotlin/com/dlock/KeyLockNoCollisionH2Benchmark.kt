package com.dlock

import com.dlock.api.KeyLock
import com.dlock.jdbc.DatabaseType
import com.dlock.jdbc.builder.JDBCKeyLockBuilder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.h2.tools.Server
import org.openjdk.jmh.annotations.*
import java.util.*
import java.util.concurrent.TimeUnit

open class KeyLockNoCollisionH2Benchmark {

    @State(Scope.Benchmark)
    open class ExecutionPlan {

        lateinit var keyLock: KeyLock
        lateinit var h2Server: Server

        @Setup(Level.Trial)
        fun start() {
            h2Server = Server.createTcpServer("-ifNotExists", "-tcp", "-tcpAllowOthers", "-tcpPort", "9079")
            h2Server.start()
        }

        @TearDown(Level.Trial)
        fun shutdown() {
            Server.shutdownTcpServer("tcp://localhost:9079", "", true, true)
        }

        @Setup(Level.Iteration)
        fun setUp() {
            val config = HikariConfig()

            config.jdbcUrl = "jdbc:h2:tcp://localhost:9079/~/dlockjmh"
            config.username = "sa"
            config.password = ""
            config.isAutoCommit = true
            config.addDataSourceProperty("maximumPoolSize", "1000")
            val dataSource = HikariDataSource(config)

            keyLock = JDBCKeyLockBuilder().dataSource(dataSource)
                    .databaseType(DatabaseType.H2)
                    .createDatabase(true).build()

        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    fun tryLockNoCollision(executionPlan: ExecutionPlan) {
        executionPlan.keyLock.tryLock(UUID.randomUUID().toString(), 1)
    }

}
