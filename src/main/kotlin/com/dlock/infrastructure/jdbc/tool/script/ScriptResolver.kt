package com.dlock.infrastructure.jdbc.tool.script

import com.dlock.infrastructure.jdbc.DatabaseType
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Paths
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
        val resourcePath = Paths.get(this.javaClass.classLoader.getResource("db/$databaseType-sql.properties").toURI())
        val fileContent = Files.readString(resourcePath)
        sqlResource.load(StringReader(fileContent.replace(tableNamePlaceholder, tableName)))
    }

    fun resolveScript(scriptPropertyKey: String): String {
        return sqlResource.getProperty(scriptPropertyKey)
    }

    fun resolveDDLScript(): String {
        val initScriptTemplate = this::class.java.getResource("/db/$databaseType-create.sql").readText()
        return initScriptTemplate.replace(tableNamePlaceholder, tableName)
    }
}