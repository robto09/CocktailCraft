package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.CocktailRatingSummary
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.testutil.FakeReviewRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** [ManageReviewsUseCase.observeRatingSummaries], which Home ranks and labels cards by. */
class RatingSummariesTest {

    private val repository = FakeReviewRepository()
    private val useCase = ManageReviewsUseCase(repository)

    @Test
    fun ratingSummariesAverageEveryReviewPerCocktail() = runTest {
        repository.addReview(review("a", 5f))
        repository.addReview(review("a", 2f).copy(isMine = false))
        repository.addReview(review("b", 4f))

        assertEquals(
            mapOf("a" to CocktailRatingSummary(3.5f, 2), "b" to CocktailRatingSummary(4f, 1)),
            useCase.observeRatingSummaries().first()
        )
    }

    @Test
    fun ratingSummariesAreEmptyWithoutReviews() = runTest {
        assertEquals(emptyMap(), useCase.observeRatingSummaries().first())
    }

    private fun review(cocktailId: String, rating: Float) =
        Review(cocktailId = cocktailId, userName = "Tester", rating = rating, comment = "A fine drink indeed.")
}
