package com.cocktailcraft.data.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppConfigImplTest {

    @Test
    fun baseUrlIsBuiltFromTheConfiguredKey() {
        assertEquals(
            "https://www.thecocktaildb.com/api/json/v1/abc123",
            AppConfigImpl(apiKey = "abc123").apiBaseUrl
        )
    }

    @Test
    fun defaultKeyIsThisBuildsConfiguredKey() {
        assertEquals(
            "https://www.thecocktaildb.com/api/json/v1/${BuildApiConfig.COCKTAILDB_API_KEY}",
            AppConfigImpl().apiBaseUrl
        )
    }

    @Test
    fun keyThatIsNotAPlainPathSegmentIsRejected() {
        listOf("", "1/../2", "key?x=1", "a b").forEach { key ->
            assertFailsWith<IllegalArgumentException>("\"$key\"") { AppConfigImpl(apiKey = key) }
        }
    }
}
