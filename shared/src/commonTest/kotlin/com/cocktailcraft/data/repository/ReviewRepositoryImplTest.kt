package com.cocktailcraft.data.repository

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.util.getOrThrow
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewRepositoryImplTest {

    private val settings = MapSettings()
    private val json = Json { ignoreUnknownKeys = true }

    private fun repository() = ReviewRepositoryImpl(settings, json, AppConfigImpl())

    private fun review(id: String, cocktailId: String = "c1", rating: Float = 4.0f) = Review(
        id = id,
        cocktailId = cocktailId,
        userName = "Robert",
        rating = rating,
        comment = "A perfectly balanced cocktail."
    )

    @Test
    fun addedReviewSurvivesRepositoryRecreation() = runTest {
        repository().addReview(review("r1")).getOrThrow()

        val reloaded = repository().getReviews().getOrThrow()
        assertEquals(1, reloaded.size)
        assertEquals("r1", reloaded.single().id)
        assertEquals("c1", reloaded.single().cocktailId)
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
}
