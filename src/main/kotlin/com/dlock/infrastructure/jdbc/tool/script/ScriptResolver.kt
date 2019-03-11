package com.dlock.infrastructure.jdbc.tool.script

import com.dlock.infrastructure.jdbc.DatabaseType
import java.io.InputStreamReader
import java.io.StringReader
import java.util.*

/**
 * Loads database script for a given database and scrip type.
 *
 * @author Przemyslaw Malirz
 */
class ScriptResolver(private val databaseType: DatabaseType, private val tableName: String) {

    private val tableNamePlaceholder = "@@tableName@@"
    private val sqlResource = Properties()

    init {
        val rawContent = InputStreamReader(this.javaClass.classLoader.getResourceAsStream("db/$databaseType-sql.properties"))
                .readText()
        val fileContent = rawContent.replace(tableNamePlaceholder, tableName)
        sqlResource.load(StringReader(fileContent))
    }

    fun resolveScript(scriptPropertyKey: String): String {
        return sqlResource.getProperty(scriptPropertyKey)
    }

    fun resolveDDLScript(): String {
        val initScriptTemplate = this::class.java.getResource("/db/$databaseType-create.sql").readText()
        return initScriptTemplate.replace(tableNamePlaceholder, tableName)
    }
}