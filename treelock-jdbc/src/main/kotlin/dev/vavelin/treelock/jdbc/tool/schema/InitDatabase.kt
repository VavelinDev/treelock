package dev.vavelin.treelock.jdbc.tool.schema

import dev.vavelin.treelock.jdbc.tool.script.ScriptResolver
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
        val ddls = scriptResolver.resolveDDLScripts()

        dataSource.connection.use { conn ->
            ddls.forEach { ddl ->
                val createStatement = conn.createStatement()
                createStatement.execute(ddl)
                createStatement.close()
            }
        }
    }
}