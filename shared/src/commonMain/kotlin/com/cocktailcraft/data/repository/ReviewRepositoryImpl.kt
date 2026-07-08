package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.util.Result
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class ReviewRepositoryImpl(
    private val settings: Settings,
    private val json: Json,
    private val appConfig: AppConfig,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReviewRepository {

    // In-memory cache of reviews, backed by a JSON blob in Settings
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())

    private val loadMutex = Mutex()
    private var loaded = false

    // Persisted state loads lazily on first use — a Koin `single` must not
    // do disk I/O on whichever thread first resolves it.
    private suspend fun ensureLoaded() {
        if (loaded) return
        loadMutex.withLock {
            if (!loaded) {
                withContext(ioDispatcher) { loadReviewsFromStorage() }
                loaded = true
            }
        }
    }

    private fun loadReviewsFromStorage() {
        val reviewsJson = settings.getStringOrNull(appConfig.reviewsStorageKey) ?: "[]"
        _reviews.value = try {
            json.decodeFromString(reviewsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveReviewsToStorage() {
        settings.putString(appConfig.reviewsStorageKey, json.encodeToString(_reviews.value))
    }

    override fun observeReviews(): Flow<List<Review>> =
        _reviews.asStateFlow().onStart { ensureLoaded() }

    override suspend fun getReviews(): Result<List<Review>> {
        ensureLoaded()
        return Result.Success(_reviews.value)
    }

    override suspend fun addReview(review: Review): Result<Unit> = withContext(ioDispatcher) {
        ensureLoaded()
        try {
            _reviews.value = _reviews.value + review
            saveReviewsToStorage()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add review")
        }
    }

    override suspend fun updateReview(
        reviewId: String,
        rating: Float,
        comment: String,
        date: String
    ): Result<Boolean> = withContext(ioDispatcher) {
        ensureLoaded()
        try {
            var found = false
            _reviews.value = _reviews.value.map { review ->
                if (review.id == reviewId) {
                    found = true
                    review.copy(rating = rating, comment = comment, date = date)
                } else {
                    review
                }
            }
            if (found) saveReviewsToStorage()
            Result.Success(found)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update review")
        }
    }

    override suspend fun deleteReview(reviewId: String): Result<Boolean> = withContext(ioDispatcher) {
        ensureLoaded()
        try {
            val remaining = _reviews.value.filterNot { it.id == reviewId }
            val removed = remaining.size != _reviews.value.size
            if (removed) {
                _reviews.value = remaining
                saveReviewsToStorage()
            }
            Result.Success(removed)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete review")
        }
    }
}
