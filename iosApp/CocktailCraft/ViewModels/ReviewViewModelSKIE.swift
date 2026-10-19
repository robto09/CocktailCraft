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
        super.init(viewModel: viewModel, uiState: viewModel.uiState, ownsViewModel: true)
    }

    // No deinit: this wraps a Koin `factory` instance it owns
    // (ownsViewModel), so the base deinit clears its scope.

    // MARK: - Public Methods (using SKIE async/await)

    func loadReviews(for cocktailId: String) {
        sharedViewModel.loadReviewsForCocktail(cocktailId: cocktailId)
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
}
