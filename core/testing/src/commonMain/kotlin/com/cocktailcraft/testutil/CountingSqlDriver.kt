package com.cocktailcraft.testutil

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement

/**
 * Wraps a [SqlDriver] and counts the statements run through it, so tests can
 * pin down that a batch operation costs a fixed number of queries instead of
 * one (or two) per row.
 */
class CountingSqlDriver(private val delegate: SqlDriver) : SqlDriver by delegate {
    /** SELECTs run so far. */
    var queries = 0
        private set

    /** INSERT/UPDATE/DELETE statements run so far. */
    var statements = 0
        private set

    fun reset() {
        queries = 0
        statements = 0
    }

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<R> {
        queries++
        return delegate.executeQuery(identifier, sql, mapper, parameters, binders)
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<Long> {
        statements++
        return delegate.execute(identifier, sql, parameters, binders)
    }
}
