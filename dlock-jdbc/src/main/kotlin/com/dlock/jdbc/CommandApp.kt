package com.dlock.jdbc

import com.dlock.jdbc.builder.JDBCKeyLockBuilder
import com.dlock.jdbc.tool.script.ScriptResolver

/**
 * Command Line Tool.
 *
 * @author Przemyslaw Malirz
 */
fun main(args: Array<String>) {

    if(args.isEmpty() || DatabaseType.values().toSet().stream().noneMatch{ v -> args[0].toUpperCase() == v.toString() }) {
        print(help())
        return
    }

    val databaseType = DatabaseType.valueOf(args[0].toUpperCase())
    val tableName = args.getOrElse(1) { JDBCKeyLockBuilder.DEFAULT_LOCK_TABLE_NAME }
    val ddlScript = ScriptResolver(databaseType, tableName).resolveDDLScript()
    println("// START SCRIPT")
    println(ddlScript)
    println("// END SCRIPT")
}

fun help(): String = "I'm producing DDL for your database. Please handover that script to your DBA.\n" +
        "Usage:\n" +
        "dlock-jdbc.jar DBTYPE [LOCKTABLE]\n" +
        "Where:\n" +
        "DBTYPE: " + DatabaseType.values().toSet().joinToString() + "\n" +
        "LOCKTABLE: Any SQL compliant table name. ${JDBCKeyLockBuilder.DEFAULT_LOCK_TABLE_NAME} is the default value." +
        "Example:\n" +
        "dlock-jdbc.jar ORACLE LOCK_TABLE"
