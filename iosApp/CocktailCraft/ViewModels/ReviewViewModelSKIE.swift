import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedReviewViewModel using pure SKIE integration.
 * No FlowCollector bridge needed - uses native Swift async/await.
 */
@MainActor
class ReviewViewModelSKIE: ObservableObject {
    // Published properties for SwiftUI - matching actual SharedReviewViewModel StateFlows
    @Published var reviews: [String: [shared.Review]] = [:]
    @Published var currentCocktailReviews: [shared.Review] = []
    @Published var averageRating: Float = 0.0
    @Published var reviewCount: Int = 0
    @Published var currentCocktailId: String? = nil
    @Published var isLoading = false
    @Published var error: shared.ErrorHandler.UserFriendlyError? = nil
    
    // Computed properties
    var hasReviews: Bool {
        sharedViewModel.hasReviews
    }
    
    var isEmpty: Bool {
        sharedViewModel.isEmpty
    }
    
    // Shared ViewModel instance
    private let sharedViewModel: shared.SharedReviewViewModel
    
    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []
    
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedReviewViewModel()
        
        // Start observing StateFlows using SKIE async/await
        startObserving()
    }
    
    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        sharedViewModel.onCleared()
    }
    
    // MARK: - SKIE StateFlow Observation
    
    private func startObserving() {
        // Observe reviews using SKIE async sequence
        observationTasks.append(Task {
            for await reviewMap in sharedViewModel.reviews {
                await MainActor.run {
                    // Convert Kotlin Map to Swift Dictionary
                    var swiftDict: [String: [shared.Review]] = [:]
                    for (key, value) in reviewMap {
                        if let stringKey = key as? String, let reviewList = value as? [shared.Review] {
                            swiftDict[stringKey] = reviewList
                        }
                    }
                    self.reviews = swiftDict
                }
            }
        })
        
        // Observe current cocktail reviews
        observationTasks.append(Task {
            for await cocktailReviews in sharedViewModel.currentCocktailReviews {
                await MainActor.run {
                    self.currentCocktailReviews = cocktailReviews
                }
            }
        })
        
        // Observe average rating
        observationTasks.append(Task {
            for await rating in sharedViewModel.averageRating {
                await MainActor.run {
                    self.averageRating = rating.floatValue
                }
            }
        })
        
        // Observe review count
        observationTasks.append(Task {
            for await count in sharedViewModel.reviewCount {
                await MainActor.run {
                    self.reviewCount = count.intValue
                }
            }
        })
        
        // Observe current cocktail ID
        observationTasks.append(Task {
            for await cocktailId in sharedViewModel.currentCocktailId {
                await MainActor.run {
                    self.currentCocktailId = cocktailId
                }
            }
        })
        
        // Observe loading state
        observationTasks.append(Task {
            for await loading in sharedViewModel.isLoading {
                await MainActor.run {
                    self.isLoading = loading.boolValue
                }
            }
        })
        
        // Observe error state
        observationTasks.append(Task {
            for await errorValue in sharedViewModel.error {
                await MainActor.run {
                    self.error = errorValue
                }
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
    
    func getReviewsForCocktail(_ cocktailId: String) -> [shared.Review] {
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
            if let intKey = key as? shared.KotlinInt, let intValue = value as? shared.KotlinInt {
                swiftDict[intKey.intValue] = intValue.intValue
            }
        }
        return swiftDict
    }
    
    func getReviewsSortedByRating(_ cocktailId: String) -> [shared.Review] {
        return sharedViewModel.getReviewsSortedByRating(cocktailId: cocktailId)
    }
    
    func getReviewsSortedByDate(_ cocktailId: String) -> [shared.Review] {
        return sharedViewModel.getReviewsSortedByDate(cocktailId: cocktailId)
    }
    
    func getRecentReviews(limit: Int = 10) -> [shared.Review] {
        return sharedViewModel.getRecentReviews(limit: Int32(limit))
    }
    
    func searchReviews(query: String) -> [shared.Review] {
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
        let count = reviewCount
        if count == 0 {
            return "No reviews"
        } else if count == 1 {
            return "1 review"
        } else {
            return "\(count) reviews"
        }
    }
    
    func canEditReview(_ review: shared.Review, currentUserId: String) -> Bool {
        // In a real app, you'd check if the review belongs to the current user
        return review.userName == currentUserId
    }
    
    func getTimeSinceReview(_ review: shared.Review) -> String {
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