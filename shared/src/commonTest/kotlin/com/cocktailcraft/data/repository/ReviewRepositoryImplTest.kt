package com.cocktailcraft.data.repository

import app.cash.turbine.test
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.createTestLocalDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ReviewRepositoryImplTest {

    private val localDatabase = createTestLocalDatabase()

    // A new repository over the same database simulates an app relaunch.
    private fun repository() = ReviewRepositoryImpl(localDatabase)

    private fun review(id: String, cocktailId: String = "c1", rating: Float = 4.0f) = Review(
        id = id,
        cocktailId = cocktailId,
        userName = "Robert",
        rating = rating,
        comment = "A perfectly balanced cocktail.",
        date = "2026-07-01"
    )

    @Test
    fun addedReviewSurvivesRepositoryRecreation() = runTest {
        repository().addReview(review("r1", rating = 4.5f)).getOrThrow()

        val reloaded = repository().getReviews().getOrThrow()
        assertEquals(listOf(review("r1", rating = 4.5f)), reloaded)
    }

    @Test
    fun reviewsKeepInsertionOrder() = runTest {
        val repo = repository()
        listOf("r3", "r1", "r2").forEach { repo.addReview(review(it)).getOrThrow() }

        assertEquals(listOf("r3", "r1", "r2"), repository().getReviews().getOrThrow().map { it.id })
    }

    @Test
    fun addingADuplicateIdFails() = runTest {
        val repo = repository()
        repo.addReview(review("r1")).getOrThrow()

        assertIs<Result.Error>(repo.addReview(review("r1", rating = 1.0f)))
        assertEquals(listOf(4.0f), repo.getReviews().getOrThrow().map { it.rating })
    }

    @Test
    fun updateReviewPersistsChangesAndReportsMissingIds() = runTest {
        val repo = repository()
        repo.addReview(review("r1", rating = 2.0f)).getOrThrow()

        assertTrue(repo.updateReview("r1", 5.0f, "Even better on a second visit.", "2026-07-06").getOrThrow())
        assertFalse(repo.updateReview("missing", 1.0f, "n/a", "2026-07-06").getOrThrow())

        val reloaded = repository().getReviews().getOrThrow().single()
        assertEquals(5.0f, reloaded.rating)
        assertEquals("Even better on a second visit.", reloaded.comment)
        assertEquals("2026-07-06", reloaded.date)
    }

    @Test
    fun deleteReviewRemovesOnlyThatReviewAndPersists() = runTest {
        val repo = repository()
        repo.addReview(review("r1")).getOrThrow()
        repo.addReview(review("r2", cocktailId = "c2")).getOrThrow()

        assertTrue(repo.deleteReview("r1").getOrThrow())
        assertFalse(repo.deleteReview("r1").getOrThrow())

        val reloaded = repository().getReviews().getOrThrow()
        assertEquals(listOf("r2"), reloaded.map { it.id })
    }

    @Test
    fun observeReviewsEmitsAfterEveryChange() = runTest {
        val repo = repository()

        repo.observeReviews().test {
            assertEquals(emptyList(), awaitItem())

            repo.addReview(review("r1")).getOrThrow()
            assertEquals(listOf("r1"), awaitItem().map { it.id })

            repo.updateReview("r1", 2.0f, "Changed my mind about it.", "2026-07-02").getOrThrow()
            assertEquals(listOf(2.0f), awaitItem().map { it.rating })

            repo.deleteReview("r1").getOrThrow()
            assertEquals(emptyList(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
