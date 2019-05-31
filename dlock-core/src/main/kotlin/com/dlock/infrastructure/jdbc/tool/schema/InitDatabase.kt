package com.dlock.infrastructure.jdbc.tool.schema

import com.dlock.infrastructure.jdbc.tool.script.ScriptResolver
import javax.sql.DataSource

/**
 * Database initiator creates required structures in the database.
 * It can be used concurrently so we have to make sure it works properly when used a few times.
 * Anyway, mostly used for testing as production (and pre-production) regions should not rely on
 * automatic DDL run at start.
 *
 * @author Przemyslaw Malirz
 */
class InitDatabase(private val scriptResolver: ScriptResolver,
                   private val dataSource: DataSource) {

    @Synchronized
    fun createDatabase() {
        val sql = scriptResolver.resolveDDLScript()

        dataSource.connection.use {
            val createStatement = it.createStatement()
            createStatement.execute(sql)
        }
    }
}