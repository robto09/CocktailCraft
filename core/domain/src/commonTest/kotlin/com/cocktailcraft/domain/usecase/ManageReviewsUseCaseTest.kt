package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.ReviewValidationError
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

    @Test
    fun validateReviewRejectsRatingsOutsideOneToFive() {
        val comment = "Lovely drink, would order again"
        assertNotNull(useCase.validateReview(0.5f, comment))
        assertNotNull(useCase.validateReview(5.5f, comment))
        assertNull(useCase.validateReview(1f, comment))
        assertNull(useCase.validateReview(5f, comment))
    }

    @Test
    fun validateReviewNamesTheBrokenRule() {
        val comment = "Lovely drink, would order again"
        assertEquals(ReviewValidationError.RATING_OUT_OF_RANGE, useCase.validateReview(0f, comment))
        assertEquals(ReviewValidationError.COMMENT_REQUIRED, useCase.validateReview(3f, "   "))
        assertEquals(ReviewValidationError.COMMENT_TOO_SHORT, useCase.validateReview(3f, "a".repeat(9)))
        assertEquals(ReviewValidationError.COMMENT_TOO_LONG, useCase.validateReview(3f, "a".repeat(501)))
        assertNull(useCase.validateReview(3f, "a".repeat(10)))
        assertNull(useCase.validateReview(3f, "a".repeat(500)))
    }

    @Test
    fun submitReviewTrimsAndStoresIt() = runTest {
        val stored = useCase.submitReview("1", 4f, "  Lovely drink, would order again  ", " Ann ").getOrThrow()

        assertEquals("Ann", stored.userName)
        assertEquals("Lovely drink, would order again", stored.comment)
        assertEquals(listOf(stored), repository.stored)
    }
}
