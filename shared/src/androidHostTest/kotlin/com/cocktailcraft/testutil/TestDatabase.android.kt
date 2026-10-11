package com.cocktailcraft.testutil

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase

internal actual fun createInMemorySqlDriver(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { CocktailCraftDatabase.Schema.create(it) }
