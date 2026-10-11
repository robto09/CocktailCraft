import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedReviewViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper.
 */
final class ReviewViewModelSKIE: SharedViewModelWrapper<ReviewUiState> {

    // Computed properties
    var reviews: [Review] {
        state.currentCocktailReviews
    }

    var hasReviews: Bool {
        !state.currentCocktailReviews.isEmpty
    }

    var reviewCount: Int {
        Int(state.reviewCount)
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedReviewViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedReviewViewModel()
        self.sharedViewModel = viewModel
        super.init(uiState: viewModel.uiState, errorFlow: viewModel.error)
    }

    deinit {
        // This wraps a Koin `factory` instance owned by this wrapper, so its
        // scope is cancelled here; the base deinit cancels observation tasks.
        // (Singleton-backed wrappers must NOT call onCleared().)
        sharedViewModel.onCleared()
    }

    // MARK: - Public Methods (using SKIE async/await)

    func loadReviews(for cocktailId: String) async {
        await run { try await sharedViewModel.loadReviewsForCocktail(cocktailId: cocktailId) }
    }

    /// Validation (rating 1-5, comment 10-500 chars, non-blank name) runs in
    /// the shared ViewModel; a rejection returns false and publishes the
    /// reason on the mirrored `error`.
    func submitReview(cocktailId: String, rating: Float, comment: String, userName: String) async -> Bool {
        return await run(fallback: false) {
            try await sharedViewModel.submitReview(
                cocktailId: cocktailId,
                rating: rating,
                comment: comment,
                userName: userName
            ).boolValue
        }
    }

    func deleteReview(_ reviewId: String) async -> Bool {
        return await run(fallback: false) { try await sharedViewModel.deleteReview(reviewId: reviewId).boolValue }
    }

    // MARK: - Synchronous Methods

    func clearError() {
        sharedViewModel.clearError()
    }
}
