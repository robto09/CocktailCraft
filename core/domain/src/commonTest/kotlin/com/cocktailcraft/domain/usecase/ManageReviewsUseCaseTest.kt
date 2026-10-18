package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.FakeReviewRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ManageReviewsUseCaseTest {

    private val repository = FakeReviewRepository()
    private val useCase = ManageReviewsUseCase(repository)

    private fun review(rating: Float) =
        Review(cocktailId = "1", userName = "Ann", rating = rating, comment = "Lovely drink, would order again")

    @Test
    fun distributionCountsWholeStarsAndListsEveryStar() {
        val distribution = useCase.getRatingDistribution(listOf(review(5f), review(4.5f), review(4f), review(1f)))

        assertEquals(mapOf(1 to 1, 2 to 0, 3 to 0, 4 to 2, 5 to 1), distribution)
    }

    @Test
    fun distributionSkipsOutOfRangeRatingsInsteadOfCrashing() {
        // These used to hit `distribution[rating]!!` with a missing key
        val distribution = useCase.getRatingDistribution(
            listOf(review(0f), review(0.5f), review(-1f), review(6f), review(Float.NaN), review(3f))
        )

        assertEquals(mapOf(1 to 0, 2 to 0, 3 to 1, 4 to 0, 5 to 0), distribution)
    }

    @Test
    fun distributionOfNoReviewsIsAllZeros() {
        assertEquals((1..5).associateWith { 0 }, useCase.getRatingDistribution(emptyList()))
    }

    @Test
    fun validateReviewRejectsRatingsOutsideOneToFive() {
        val comment = "Lovely drink, would order again"
        assertNotNull(useCase.validateReview(0.5f, comment))
        assertNotNull(useCase.validateReview(5.5f, comment))
        assertNull(useCase.validateReview(1f, comment))
        assertNull(useCase.validateReview(5f, comment))
    }

    @Test
    fun submitReviewTrimsAndStoresIt() = runTest {
        val stored = useCase.submitReview("1", 4f, "  Lovely drink, would order again  ", " Ann ").getOrThrow()

        assertEquals("Ann", stored.userName)
        assertEquals("Lovely drink, would order again", stored.comment)
        assertEquals(listOf(stored), repository.stored)
    }
}
