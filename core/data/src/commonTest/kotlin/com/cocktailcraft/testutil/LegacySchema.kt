package com.cocktailcraft.testutil

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

/**
 * The database schema as released versions created it, for migration tests
 * that need real rows of an old version: its tables are created as that
 * version had them (copied from the src/commonMain/sqldelight/databases
 * snapshots), then the test inserts rows and runs the real migrations.
 */
internal class LegacySchema(
    override val version: Long,
    private val statements: List<String>
) : SqlSchema<QueryResult.Value<Unit>> {

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
        statements.forEach { driver.execute(null, it, 0) }
        return QueryResult.Unit
    }

    override fun migrate(
        driver: SqlDriver,
        oldVersion: Long,
        newVersion: Long,
        vararg callbacks: AfterVersion
    ): QueryResult.Value<Unit> = error("a legacy schema only creates")

    companion object {
        private val catalogV2 = listOf(
            """
            CREATE TABLE cached_cocktail (
                id TEXT NOT NULL PRIMARY KEY,
                category TEXT,
                payload TEXT NOT NULL,
                cached_at INTEGER NOT NULL,
                last_accessed INTEGER NOT NULL
            )
            """,
            "CREATE INDEX cached_cocktail_category ON cached_cocktail(category)",
            "CREATE INDEX cached_cocktail_last_accessed ON cached_cocktail(last_accessed)",
            """
            CREATE TABLE recently_viewed_cocktail (
                id TEXT NOT NULL PRIMARY KEY,
                payload TEXT NOT NULL,
                viewed_at INTEGER NOT NULL
            )
            """
        )

        private val userDataV2 = listOf(
            """
            CREATE TABLE cart_item (
                cocktail_id TEXT NOT NULL PRIMARY KEY,
                payload TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                position INTEGER NOT NULL
            )
            """,
            """
            CREATE TABLE favorite_cocktail (
                id TEXT NOT NULL PRIMARY KEY,
                payload TEXT,
                position INTEGER NOT NULL
            )
            """,
            """
            CREATE TABLE cocktail_review (
                id TEXT NOT NULL PRIMARY KEY,
                cocktail_id TEXT NOT NULL,
                user_name TEXT NOT NULL,
                rating REAL NOT NULL,
                comment TEXT NOT NULL,
                date TEXT NOT NULL,
                position INTEGER NOT NULL
            )
            """
        )

        private val dollarOrders = listOf(
            """
            CREATE TABLE customer_order (
                id TEXT NOT NULL PRIMARY KEY,
                date TEXT NOT NULL,
                total REAL NOT NULL,
                status TEXT NOT NULL,
                position INTEGER NOT NULL
            )
            """,
            """
            CREATE TABLE customer_order_item (
                order_id TEXT NOT NULL REFERENCES customer_order(id) ON DELETE CASCADE,
                position INTEGER NOT NULL,
                name TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                price REAL NOT NULL,
                PRIMARY KEY (order_id, position)
            )
            """
        )

        private val centOrders = listOf(
            """
            CREATE TABLE customer_order (
                id TEXT NOT NULL PRIMARY KEY,
                date TEXT NOT NULL,
                total_cents INTEGER NOT NULL,
                status TEXT NOT NULL,
                position INTEGER NOT NULL
            )
            """,
            """
            CREATE TABLE customer_order_item (
                order_id TEXT NOT NULL REFERENCES customer_order(id) ON DELETE CASCADE,
                position INTEGER NOT NULL,
                cocktail_id TEXT,
                name TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                price_cents INTEGER NOT NULL,
                PRIMARY KEY (order_id, position)
            )
            """
        )

        private val ttlIndex = listOf("CREATE INDEX cached_cocktail_cached_at ON cached_cocktail(cached_at)")

        private val categoryPages = listOf(
            """
            CREATE TABLE category_page (
                category TEXT NOT NULL,
                cocktail_id TEXT NOT NULL,
                position INTEGER NOT NULL,
                fetched_at INTEGER NOT NULL,
                PRIMARY KEY (category, cocktail_id)
            )
            """,
            "CREATE INDEX category_page_position ON category_page(category, position)"
        )

        /** Version 3: before category_page. */
        val V3 = LegacySchema(3, catalogV2 + ttlIndex + userDataV2 + dollarOrders)

        /** Version 4: before money in cents. */
        val V4 = LegacySchema(4, catalogV2 + ttlIndex + categoryPages + userDataV2 + dollarOrders)

        /**
         * Version 5 (dev up to this change): favorites, cart lines and
         * recently viewed entries keep their own JSON copy of the cocktail.
         */
        val V5 = LegacySchema(5, catalogV2 + ttlIndex + categoryPages + userDataV2 + centOrders)
    }
}
