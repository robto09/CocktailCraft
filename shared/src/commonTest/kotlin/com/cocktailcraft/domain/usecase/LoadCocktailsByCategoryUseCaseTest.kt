package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.FakeSearchRepository
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LoadCocktailsByCategoryUseCaseTest {

    private val repository = FakeSearchRepository(
        all = (1..25).map { testCocktail("c$it", category = "Cocktail") }
    )
    private val useCase = LoadCocktailsByCategoryUseCase(repository)

    @Test
    fun loadMoreReturnsTheRequestedZeroIndexedPage() = runTest {
        // Page 1 follows the first PAGE_SIZE items served by invoke().
        val page = useCase.loadMore("Cocktail", page = 1, pageSize = 10).getOrThrow()

        assertEquals((11..20).map { "c$it" }, page.map { it.id })
    }

    @Test
    fun loadMoreReturnsPartialThenEmptyPagesPastTheEnd() = runTest {
        val lastPage = useCase.loadMore("Cocktail", page = 2, pageSize = 10).getOrThrow()
        assertEquals(listOf("c21", "c22", "c23", "c24", "c25"), lastPage.map { it.id })

        val pastEnd = useCase.loadMore("Cocktail", page = 3, pageSize = 10).getOrThrow()
        assertTrue(pastEnd.isEmpty(), "pages past the end are empty, not an error")
    }

    @Test
    fun loadMorePropagatesRepositoryFailureAsError() = runTest {
        // A failed fetch must come back as Result.Error — never as an empty
        // page, which callers would misread as the end of the list.
        repository.errorMessage = "api down"

        val page = useCase.loadMore("Cocktail", page = 1, pageSize = 10)

        assertIs<Result.Error>(page)
    }
}
