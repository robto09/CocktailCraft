package com.cocktailcraft.domain.usecase

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
    fun submitReviewTrimsAndStoresIt() = runTest {
        val stored = useCase.submitReview("1", 4f, "  Lovely drink, would order again  ", " Ann ").getOrThrow()

        assertEquals("Ann", stored.userName)
        assertEquals("Lovely drink, would order again", stored.comment)
        assertEquals(listOf(stored), repository.stored)
    }
}
