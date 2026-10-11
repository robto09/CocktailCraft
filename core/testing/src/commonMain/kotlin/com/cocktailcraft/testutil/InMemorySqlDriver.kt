package com.cocktailcraft.testutil

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

/**
 * A fresh, empty in-memory SQLite driver with [schema] already created:
 * the JDBC sqlite driver on the Android host, the native driver's in-memory
 * mode on iOS. Each call returns an independent database.
 *
 * Takes the schema (in practice CocktailCraftDatabase.Schema) so this module
 * needn't depend on :core:data: the data tests and the :shared DI graph tests
 * both build on it.
 */
expect fun createInMemorySqlDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver
