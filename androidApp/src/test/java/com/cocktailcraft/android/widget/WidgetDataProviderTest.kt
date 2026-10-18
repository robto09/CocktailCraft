package com.cocktailcraft.android.widget

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class WidgetDataProviderTest {

    private val detail = mockk<CocktailDetailRepository>()
    private val offline = mockk<CocktailOfflineRepository>()
    private val favorites = mockk<CocktailFavoritesRepository>()

    // Deterministic fallback pick: the first recently viewed cocktail
    private val provider = WidgetDataProvider(detail, offline, favorites, pickFallback = { it.first() })

    private val live = Cocktail(id = "11007", name = "Margarita")
    private val cached = Cocktail(id = "11000", name = "Mojito")

    @Test
    fun `random cocktail comes from the API when it succeeds`() = runTest {
        coEvery { detail.getRandomCocktail() } returns Result.Success(live)

        assertEquals(live, provider.getRandomCocktail())
    }

    @Test
    fun `random cocktail falls back to recently viewed when the API returns an error`() = runTest {
        coEvery { detail.getRandomCocktail() } returns Result.Error("offline", ErrorCode.NETWORK)
        coEvery { offline.getRecentlyViewedCocktails() } returns Result.Success(listOf(cached))

        assertEquals(cached, provider.getRandomCocktail())
    }

    @Test
    fun `random cocktail falls back when the API returns no cocktail`() = runTest {
        coEvery { detail.getRandomCocktail() } returns Result.Success(null)
        coEvery { offline.getRecentlyViewedCocktails() } returns Result.Success(listOf(cached))

        assertEquals(cached, provider.getRandomCocktail())
    }

    @Test
    fun `random cocktail falls back when the API call throws`() = runTest {
        coEvery { detail.getRandomCocktail() } throws IOException("boom")
        coEvery { offline.getRecentlyViewedCocktails() } returns Result.Success(listOf(cached))

        assertEquals(cached, provider.getRandomCocktail())
    }

    @Test
    fun `random cocktail is null when neither the API nor the cache has one`() = runTest {
        coEvery { detail.getRandomCocktail() } returns Result.Error("offline", ErrorCode.NETWORK)
        coEvery { offline.getRecentlyViewedCocktails() } returns Result.Success(emptyList())

        assertNull(provider.getRandomCocktail())
    }

    @Test
    fun `cancellation is never swallowed`() = runTest {
        coEvery { detail.getRandomCocktail() } throws CancellationException("stopped")

        assertFailsWith<CancellationException> { provider.getRandomCocktail() }
    }

    @Test
    fun `favorites are returned as stored, empty included`() = runTest {
        coEvery { favorites.getFavoriteCocktails() } returns Result.Success(listOf(live))
        assertEquals(listOf(live), provider.getFavoriteCocktails())

        coEvery { favorites.getFavoriteCocktails() } returns Result.Success(emptyList())
        assertEquals(emptyList(), provider.getFavoriteCocktails())
    }

    @Test
    fun `a failed favorites read is null, not an empty list`() = runTest {
        coEvery { favorites.getFavoriteCocktails() } returns Result.Error("db", ErrorCode.UNKNOWN)

        assertNull(provider.getFavoriteCocktails())
    }
}
