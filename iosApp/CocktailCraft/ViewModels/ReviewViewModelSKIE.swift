import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedReviewViewModel using pure SKIE integration.
 * No FlowCollector bridge needed - uses native Swift async/await.
 */
@MainActor
class ReviewViewModelSKIE: ObservableObject {
    // Published properties for SwiftUI
    @Published var reviews: [Review] = []
    @Published var currentCocktailReviews: [Review] = []
    @Published var userReviews: [Review] = []
    @Published var averageRating: Double = 0.0
    @Published var totalReviews: Int = 0
    @Published var ratingDistribution: [Int: Int] = [:]
    @Published var isSubmittingReview = false
    @Published var selectedCocktailId: String? = nil
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    
    // Computed properties
    var hasReviews: Bool {
        sharedViewModel.hasReviews
    }
    
    var canSubmitReview: Bool {
        sharedViewModel.canSubmitReview
    }
    
    var formattedAverageRating: String {
        sharedViewModel.formattedAverageRating
    }
    
    // Shared ViewModel instance
    private let sharedViewModel: SharedReviewViewModel
    
    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []
    
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = KoinHelper().getSharedReviewViewModel()
        
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
            for await reviewList in sharedViewModel.reviews {
                await MainActor.run {
                    self.reviews = reviewList
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
        
        // Observe user reviews
        observationTasks.append(Task {
            for await userReviewList in sharedViewModel.userReviews {
                await MainActor.run {
                    self.userReviews = userReviewList
                }
            }
        })
        
        // Observe average rating
        observationTasks.append(Task {
            for await rating in sharedViewModel.averageRating {
                await MainActor.run {
                    self.averageRating = rating
                }
            }
        })
        
        // Observe total reviews
        observationTasks.append(Task {
            for await total in sharedViewModel.totalReviews {
                await MainActor.run {
                    self.totalReviews = Int(total)
                }
            }
        })
        
        // Observe rating distribution
        observationTasks.append(Task {
            for await distribution in sharedViewModel.ratingDistribution {
                await MainActor.run {
                    // Convert Kotlin Map to Swift Dictionary
                    var swiftDict: [Int: Int] = [:]
                    for (key, value) in distribution {
                        if let intKey = key as? Int, let intValue = value as? Int {
                            swiftDict[intKey] = intValue
                        }
                    }
                    self.ratingDistribution = swiftDict
                }
            }
        })
        
        // Observe submitting review state
        observationTasks.append(Task {
            for await submitting in sharedViewModel.isSubmittingReview {
                await MainActor.run {
                    self.isSubmittingReview = submitting
                }
            }
        })
        
        // Observe selected cocktail ID
        observationTasks.append(Task {
            for await cocktailId in sharedViewModel.selectedCocktailId {
                await MainActor.run {
                    self.selectedCocktailId = cocktailId
                }
            }
        })
        
        // Observe loading state
        observationTasks.append(Task {
            for await loading in sharedViewModel.isLoading {
                await MainActor.run {
                    self.isLoading = loading
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
    
    func submitReview(cocktailId: String, rating: Int, comment: String, userName: String) async {
        await sharedViewModel.submitReview(
            cocktailId: cocktailId,
            rating: Int32(rating),
            comment: comment,
            userName: userName
        )
    }
    
    func loadReviewsForCocktail(_ cocktailId: String) async {
        await sharedViewModel.loadReviewsForCocktail(cocktailId: cocktailId)
    }
    
    func loadUserReviews(_ userId: String) async {
        await sharedViewModel.loadUserReviews(userId: userId)
    }
    
    func updateReview(_ reviewId: String, rating: Int, comment: String) async {
        await sharedViewModel.updateReview(
            reviewId: reviewId,
            rating: Int32(rating),
            comment: comment
        )
    }
    
    func deleteReview(_ reviewId: String) async {
        await sharedViewModel.deleteReview(reviewId: reviewId)
    }
    
    func likeReview(_ reviewId: String) async {
        await sharedViewModel.likeReview(reviewId: reviewId)
    }
    
    func unlikeReview(_ reviewId: String) async {
        await sharedViewModel.unlikeReview(reviewId: reviewId)
    }
    
    func reportReview(_ reviewId: String, reason: String) async {
        await sharedViewModel.reportReview(reviewId: reviewId, reason: reason)
    }
    
    // MARK: - Synchronous Methods
    
    func setSelectedCocktail(_ cocktailId: String) {
        sharedViewModel.setSelectedCocktail(cocktailId: cocktailId)
    }
    
    func getReviewsForCocktail(_ cocktailId: String) -> [Review] {
        return sharedViewModel.getReviewsForCocktail(cocktailId: cocktailId)
    }
    
    func getUserReviewForCocktail(_ cocktailId: String, userId: String) -> Review? {
        return sharedViewModel.getUserReviewForCocktail(cocktailId: cocktailId, userId: userId)
    }
    
    func hasUserReviewedCocktail(_ cocktailId: String, userId: String) -> Bool {
        return sharedViewModel.hasUserReviewedCocktail(cocktailId: cocktailId, userId: userId)
    }
    
    func calculateAverageRating(_ cocktailId: String) -> Double {
        return sharedViewModel.calculateAverageRating(cocktailId: cocktailId)
    }
    
    func getRatingDistribution(_ cocktailId: String) -> [Int: Int] {
        let kotlinMap = sharedViewModel.getRatingDistribution(cocktailId: cocktailId)
        var swiftDict: [Int: Int] = [:]
        for (key, value) in kotlinMap {
            if let intKey = key as? Int, let intValue = value as? Int {
                swiftDict[intKey] = intValue
            }
        }
        return swiftDict
    }
    
    func getTotalReviewCount(_ cocktailId: String) -> Int {
        return Int(sharedViewModel.getTotalReviewCount(cocktailId: cocktailId))
    }
    
    func isValidRating(_ rating: Int) -> Bool {
        return sharedViewModel.isValidRating(rating: Int32(rating))
    }
    
    func isValidComment(_ comment: String) -> Bool {
        return sharedViewModel.isValidComment(comment: comment)
    }
    
    func getReviewById(_ reviewId: String) -> Review? {
        return sharedViewModel.getReviewById(reviewId: reviewId)
    }
    
    func sortReviewsByRating(ascending: Bool = false) -> [Review] {
        return sharedViewModel.sortReviewsByRating(ascending: ascending)
    }
    
    func sortReviewsByDate(ascending: Bool = false) -> [Review] {
        return sharedViewModel.sortReviewsByDate(ascending: ascending)
    }
    
    func filterReviewsByRating(_ minRating: Int) -> [Review] {
        return sharedViewModel.filterReviewsByRating(minRating: Int32(minRating))
    }
    
    func getReviewsSummary(_ cocktailId: String) -> String {
        return sharedViewModel.getReviewsSummary(cocktailId: cocktailId)
    }
    
    func clearError() {
        sharedViewModel.clearError()
    }
    
    func clearSelectedCocktail() {
        sharedViewModel.clearSelectedCocktail()
    }
    
    func refreshReviews() {
        sharedViewModel.refreshReviews()
    }
    
    // MARK: - Helper Methods for SwiftUI
    
    func getStarRating(_ rating: Double) -> String {
        let fullStars = Int(rating)
        let hasHalfStar = rating - Double(fullStars) >= 0.5
        let emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0)
        
        return String(repeating: "★", count: fullStars) +
               (hasHalfStar ? "☆" : "") +
               String(repeating: "☆", count: emptyStars)
    }
    
    func formatRating(_ rating: Double) -> String {
        return String(format: "%.1f", rating)
    }
    
    func getRatingColor(_ rating: Double) -> Color {
        switch rating {
        case 4.5...5.0: return .green
        case 3.5..<4.5: return .yellow
        case 2.5..<3.5: return .orange
        default: return .red
        }
    }
    
    func getReviewCountText() -> String {
        let count = totalReviews
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
    
    func getRatingPercentage(_ rating: Int) -> Double {
        guard totalReviews > 0 else { return 0.0 }
        let count = ratingDistribution[rating] ?? 0
        return Double(count) / Double(totalReviews) * 100.0
    }
}