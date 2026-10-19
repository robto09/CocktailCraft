package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.FakeCatalogRepository
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoadCocktailsByCategoryUseCaseTest {

    private val repository = FakeCatalogRepository(
        all = (1..25).map { testCocktail("c$it", category = "Cocktail") }
    )
    private val useCase = LoadCocktailsByCategoryUseCase(repository)

    @Test
    fun invokeRefreshesThenReturnsTheStoredPage() = runTest {
        val page = useCase("Cocktail").getOrThrow()

        assertEquals(25, page.size)
        assertEquals(page, useCase.observe("Cocktail").first(), "the result is what observers see")
        assertEquals(listOf("Cocktail" to false), repository.refreshCalls)
    }

    @Test
    fun invokePassesForceRefreshThrough() = runTest {
        useCase("Cocktail", forceRefresh = true)

        assertEquals(listOf("Cocktail" to true), repository.refreshCalls)
    }

    @Test
    fun failedRefreshFallsBackToTheStoredPage() = runTest {
        repository.store("Cocktail", listOf(testCocktail("cached")))
        repository.errorMessage = "api down"

        val page = useCase("Cocktail")

        assertEquals(listOf("cached"), (page as Result.Success).data.map { it.id })
    }

    @Test
    fun genuinelyEmptyCategoryIsAnEmptySuccess() = runTest {
        val page = useCase("Shot")

        assertTrue((page as Result.Success).data.isEmpty())
    }
}
