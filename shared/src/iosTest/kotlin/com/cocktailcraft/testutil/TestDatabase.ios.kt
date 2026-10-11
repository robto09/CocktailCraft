package com.cocktailcraft.testutil

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase

internal actual fun createInMemorySqlDriver(): SqlDriver = inMemoryDriver(CocktailCraftDatabase.Schema)
