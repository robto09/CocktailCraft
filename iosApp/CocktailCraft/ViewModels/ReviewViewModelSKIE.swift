import SwiftUI
import shared
import Observation

/**
 * iOS ViewModel wrapper for SharedReviewViewModel using pure SKIE integration.
 * Mirrors the consolidated uiState as Observation-tracked state.
 */
@MainActor
@Observable
class ReviewViewModelSKIE {
    // Consolidated UI state from the shared ViewModel
    private(set) var state: ReviewUiState
    // The single error channel from the shared ViewModel base class
    var error: ErrorHandler.UserFriendlyError? = nil

    // Computed properties
    var hasReviews: Bool {
        sharedViewModel.hasReviews
    }

    var isEmpty: Bool {
        sharedViewModel.isEmpty
    }

    /// state.reviews bridged to a native Swift dictionary.
    var reviews: [String: [Review]] {
        var swiftDict: [String: [Review]] = [:]
        for (key, value) in state.reviews {
            if let stringKey = key as? String, let reviewList = value as? [Review] {
                swiftDict[stringKey] = reviewList
            }
        }
        return swiftDict
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedReviewViewModel

    // Tasks for async observation
    @ObservationIgnored private var observationTasks: [Task<Void, Never>] = []

    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedReviewViewModel()

        // Seed synchronously so the first frame renders the current state
        self.state = sharedViewModel.uiState.value

        // Start observing StateFlows using SKIE async/await
        startObserving()
    }

    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        // This wraps a Koin `factory` instance owned by this wrapper, so its
        // scope is cancelled here. (Singleton-backed wrappers must NOT do this.)
        sharedViewModel.onCleared()
    }

    // MARK: - SKIE StateFlow Observation

    private func startObserving() {
        // These Tasks inherit @MainActor, so assignments land on the main thread.
        observationTasks.append(Task { [weak self] in
            guard let flow = self?.sharedViewModel.uiState else { return }
            for await state in flow {
                self?.state = state
            }
        })

        observationTasks.append(Task { [weak self] in
            guard let flow = self?.sharedViewModel.error else { return }
            for await errorValue in flow {
                self?.error = errorValue
            }
        })
    }

    // MARK: - Public Methods (using SKIE async/await)

    func submitReview(cocktailId: String, rating: Float, comment: String, userName: String) async -> Bool {
        do {
            let result = try await sharedViewModel.submitReview(
                cocktailId: cocktailId,
                rating: rating,
                comment: comment,
                userName: userName
            )
            return result.boolValue
        } catch {
            return false
        }
    }

    func loadReviewsForCocktail(_ cocktailId: String) async {
        do {
            try await sharedViewModel.loadReviewsForCocktail(cocktailId: cocktailId)
        } catch {
            // Error handling is done in the shared ViewModel
        }
    }

    func updateReview(_ reviewId: String, rating: Float, comment: String) async -> Bool {
        do {
            let result = try await sharedViewModel.updateReview(
                reviewId: reviewId,
                rating: rating,
                comment: comment
            )
            return result.boolValue
        } catch {
            return false
        }
    }

    func deleteReview(_ reviewId: String) async -> Bool {
        do {
            let result = try await sharedViewModel.deleteReview(reviewId: reviewId)
            return result.boolValue
        } catch {
            return false
        }
    }

    func loadAllReviews() async {
        do {
            try await sharedViewModel.loadAllReviews()
        } catch {
            // Error handling is done in the shared ViewModel
        }
    }

    // MARK: - Synchronous Methods

    func getReviewsForCocktail(_ cocktailId: String) -> [Review] {
        return sharedViewModel.getReviewsForCocktail(cocktailId: cocktailId)
    }

    func getAverageRating(_ cocktailId: String) -> Float {
        return sharedViewModel.getAverageRating(cocktailId: cocktailId)
    }

    func getReviewCount(_ cocktailId: String) -> Int {
        return Int(sharedViewModel.getReviewCount(cocktailId: cocktailId))
    }

    func validateReview(rating: Float, comment: String) -> Bool {
        return sharedViewModel.validateReview(rating: rating, comment: comment)
    }

    func getRatingDistribution(_ cocktailId: String) -> [Int: Int] {
        let kotlinMap = sharedViewModel.getRatingDistribution(cocktailId: cocktailId)
        var swiftDict: [Int: Int] = [:]
        for (key, value) in kotlinMap {
            if let intKey = key as? KotlinInt, let intValue = value as? KotlinInt {
                swiftDict[intKey.intValue] = intValue.intValue
            }
        }
        return swiftDict
    }

    func getReviewsSortedByRating(_ cocktailId: String) -> [Review] {
        return sharedViewModel.getReviewsSortedByRating(cocktailId: cocktailId)
    }

    func getReviewsSortedByDate(_ cocktailId: String) -> [Review] {
        return sharedViewModel.getReviewsSortedByDate(cocktailId: cocktailId)
    }

    func getRecentReviews(limit: Int = 10) -> [Review] {
        return sharedViewModel.getRecentReviews(limit: Int32(limit))
    }

    func searchReviews(query: String) -> [Review] {
        return sharedViewModel.searchReviews(query: query)
    }

    func refresh() {
        sharedViewModel.refresh()
    }

    // MARK: - Helper Methods for SwiftUI

    func getStarRating(_ rating: Float) -> String {
        let fullStars = Int(rating)
        let hasHalfStar = rating - Float(fullStars) >= 0.5
        let emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0)

        return String(repeating: "★", count: fullStars) +
               (hasHalfStar ? "☆" : "") +
               String(repeating: "☆", count: emptyStars)
    }

    func formatRating(_ rating: Float) -> String {
        return String(format: "%.1f", rating)
    }

    func getRatingColor(_ rating: Float) -> Color {
        switch rating {
        case 4.5...5.0: return .green
        case 3.5..<4.5: return .yellow
        case 2.5..<3.5: return .orange
        default: return .red
        }
    }

    func getReviewCountText() -> String {
        let count = Int(state.reviewCount)
        if count == 0 {
            return "No reviews"
        } else if count == 1 {
            return "1 review"
        } else {
            return "\(count) reviews"
        }
    }

    func canEditReview(_ review: Review, currentUserId: String) -> Bool {
        // In a real app, you'd check if the review belongs to the current user
        return review.userName == currentUserId
    }

    func getTimeSinceReview(_ review: Review) -> String {
        // Simple time formatting - in a real app you'd use proper date formatting
        return review.date
    }

    func getRatingPercentage(_ rating: Int, for cocktailId: String) -> Double {
        let distribution = getRatingDistribution(cocktailId)
        let totalCount = distribution.values.reduce(0, +)
        guard totalCount > 0 else { return 0.0 }
        let count = distribution[rating] ?? 0
        return Double(count) / Double(totalCount) * 100.0
    }
}
