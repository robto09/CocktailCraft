package com.cocktailcraft.android.widget

import app.cash.turbine.test
import com.cocktailcraft.domain.model.Cocktail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesWidgetRefreshTest {

    private val debounceMs = 300L

    private fun favorites(vararg ids: String) = ids.map { Cocktail(id = it, name = "Cocktail $it") }

    @Test
    fun `the list the widgets already show does not trigger a refresh`() = runTest {
        val source = MutableStateFlow(favorites("1", "2"))

        favoriteIdChanges(source, debounceMs).test {
            advanceTimeBy(debounceMs * 2)
            expectNoEvents()
        }
    }

    @Test
    fun `a change to the favorite ids triggers one refresh once it settles`() = runTest {
        val source = MutableStateFlow(favorites("1", "2"))

        favoriteIdChanges(source, debounceMs).test {
            source.value = favorites("1")
            // Undo right away: the burst settles into a single refresh
            source.value = favorites("1", "2", "3")
            assertEquals(listOf("1", "2", "3"), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `re-emitting the same ids with upgraded details does not trigger a refresh`() = runTest {
        val source = MutableStateFlow(favorites("1"))

        favoriteIdChanges(source, debounceMs).test {
            source.value = listOf(Cocktail(id = "1", name = "Cocktail 1", instructions = "Shake"))
            advanceTimeBy(debounceMs * 2)
            expectNoEvents()
        }
    }
}
