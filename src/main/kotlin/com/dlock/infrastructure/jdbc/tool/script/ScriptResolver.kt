package com.dlock.infrastructure.jdbc.tool.script

import com.dlock.infrastructure.jdbc.DatabaseType
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
        val propertyFileName = "db/$databaseType-sql.properties"
        sqlResource.load(this.javaClass.classLoader.getResourceAsStream(propertyFileName))
    }

    fun resolveScript(scriptPropertyKey: String): String {
        return sqlResource.getProperty(scriptPropertyKey).replace(tableNamePlaceholder, tableName)
    }

    fun resolveDDLScript(): String {
        val initScriptTemplate = this::class.java.getResource("/db/$databaseType-create.sql").readText()
        return initScriptTemplate.replace(tableNamePlaceholder, tableName)
    }

}