package com.dlock.infrastructure.jdbc.tool.schema

import com.dlock.core.SimpleKeyLock
import com.dlock.infrastructure.jdbc.DatabaseType
import javax.sql.DataSource

/**
 * Database initiator creates required structures in the database.
 * It can be used concurrently so we have to make sure it works properly when used a few times.
 * Anyway, mostly used for testing as production (and pre-production) regions should not rely on
 * automatic DDL run at start.
 *
 * @author Przemyslaw Malirz
 */
class InitDatabase(private val databaseType: DatabaseType,
                   private val dataSource: DataSource,
                   private val tableName: String) {

    @Synchronized
    fun createDatabase() {
        when (databaseType) {
            DatabaseType.H2 -> createH2Database()
            else -> throw IllegalStateException("Unsupported database type: \"$databaseType\"")
        }
    }


    private fun createH2Database() {
        val initScriptTemplate = SimpleKeyLock::class.java.getResource("/db/H2-create.sql").readText()

        val sql = initScriptTemplate.replace("@@tableName@@", tableName)

        dataSource.connection.use {
            val createStatement = it.createStatement()
            createStatement.execute(sql)
        }
    }

}