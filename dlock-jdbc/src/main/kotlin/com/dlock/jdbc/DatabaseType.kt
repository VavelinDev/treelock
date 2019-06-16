package com.dlock.jdbc

/**
 * Supported RDBMS.
 *
 * @author Przemyslaw Malirz
 */
enum class DatabaseType {
    H2,
    ORACLE;

    companion object {
        @JvmStatic
        fun valuesAsString(): Array<String> = values().map { v -> v.toString() }.toTypedArray()
    }
}