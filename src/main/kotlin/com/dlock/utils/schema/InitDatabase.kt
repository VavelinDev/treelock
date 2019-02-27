package com.dlock.utils.schema

import com.dlock.DBKeyLock
import javax.sql.DataSource

/**
 * Database initiator creates required structures in the database.
 * It can be used concurrently so we have to make sure it works properly when used a few times.
 * Anyway, mostly used for testing as production (and pre-production) regions should not rely on
 * automatic DDL run at start.
 *
 * @author Przemyslaw Malirz
 */
class InitDatabase(private val dataSource: DataSource,
                   private val tableName: String) {

    @Synchronized
    fun createDatabase() {
        // if H2:
        createH2Database()
    }


    private fun createH2Database() {
        val initScriptTemplate = DBKeyLock::class.java.getResource("/db/create-h2.sql").readText()

        val sql = initScriptTemplate.replace("@@tableName@@", tableName)

        dataSource.connection.use {
            val createStatement = it.createStatement()
            createStatement.execute(sql)
        }
    }

}