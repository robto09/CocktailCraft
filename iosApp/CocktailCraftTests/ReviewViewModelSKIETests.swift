import XCTest
@testable import CocktailCraft
import shared

/**
 * ReviewViewModelSKIE over the real shared review ViewModel, resolved
 * through the Koin graph the test host already started (TestSetup).
 * Reviews persist in the host app's settings, so every test uses a fresh
 * cocktail id and deletes whatever it submits.
 */
@MainActor
final class ReviewViewModelSKIETests: TestSetup {

    // nonisolated: also used as a default argument, which is evaluated
    // outside the class's main-actor isolation
    private nonisolated static let validComment = "Bright, balanced and easy to make."

    /// Polls on the main actor. Each sleep suspends, so the wrapper's
    /// observation tasks get to run between checks. Bounded at 2s.
    private func waitUntil(timeout: TimeInterval = 2.0,
                           _ condition: () -> Bool) async -> Bool {
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            if condition() { return true }
            try? await Task.sleep(nanoseconds: 50_000_000)
        }
        return condition()
    }

    private func uniqueCocktailId() -> String {
        "ios-review-test-\(UUID().uuidString)"
    }

    /// Submits a review that must be accepted and registers its cleanup.
    private func submitAccepted(_ wrapper: ReviewViewModelSKIE,
                                cocktailId: String,
                                rating: Float,
                                userName: String = "Tester",
                                comment: String = validComment) async {
        let before = wrapper.state.reviews[cocktailId]?.count ?? 0
        let accepted = await wrapper.submitReview(
            cocktailId: cocktailId, rating: rating, comment: comment, userName: userName
        )
        XCTAssertTrue(accepted, "a valid review should be accepted")
        let stored = await waitUntil { (wrapper.state.reviews[cocktailId]?.count ?? 0) == before + 1 }
        XCTAssertTrue(stored, "the new review should mirror into the wrapper within 2s")
        addTeardownBlock { @MainActor in
            for review in wrapper.state.reviews[cocktailId] ?? [] {
                _ = await wrapper.deleteReview(review.id)
            }
        }
    }

    /// Submits an invalid review and returns the mirrored error message.
    private func rejectionMessage(_ wrapper: ReviewViewModelSKIE,
                                  rating: Float,
                                  comment: String,
                                  userName: String = "Tester") async -> String? {
        let cocktailId = uniqueCocktailId()
        let accepted = await wrapper.submitReview(
            cocktailId: cocktailId, rating: rating, comment: comment, userName: userName
        )
        XCTAssertFalse(accepted, "an invalid review must be rejected")
        _ = await waitUntil { wrapper.error != nil }
        let message = wrapper.error?.message
        XCTAssertNil(wrapper.state.reviews[cocktailId], "a rejected review must not be stored")
        wrapper.clearError()
        _ = await waitUntil { wrapper.error == nil }
        return message
    }

    // MARK: - Initial and empty state

    func testFreshWrapperHasNoSelectedCocktailOrError() {
        let wrapper = ReviewViewModelSKIE()

        XCTAssertNil(wrapper.state.currentCocktailId)
        XCTAssertTrue(wrapper.reviews.isEmpty)
        XCTAssertFalse(wrapper.hasReviews)
        XCTAssertEqual(wrapper.reviewCount, 0)
        XCTAssertNil(wrapper.error)
    }

    func testLoadReviewsSelectsCocktailWithEmptyState() async {
        let wrapper = ReviewViewModelSKIE()
        let cocktailId = uniqueCocktailId()

        await wrapper.loadReviews(for: cocktailId)

        let selected = await waitUntil { wrapper.state.currentCocktailId == cocktailId }
        XCTAssertTrue(selected, "state should switch to the requested cocktail")
        XCTAssertFalse(wrapper.hasReviews)
        XCTAssertEqual(wrapper.reviewCount, 0)
        XCTAssertEqual(wrapper.state.averageRating, 0)
    }

    // MARK: - State mapping

    func testSubmittedReviewMapsIntoWrapperState() async {
        let wrapper = ReviewViewModelSKIE()
        let cocktailId = uniqueCocktailId()
        await wrapper.loadReviews(for: cocktailId)

        await submitAccepted(wrapper, cocktailId: cocktailId, rating: 5,
                             userName: "  Tester  ", comment: "  \(Self.validComment)  ")

        let appeared = await waitUntil { wrapper.reviews.count == 1 }
        XCTAssertTrue(appeared)
        let review = wrapper.reviews[0]
        XCTAssertEqual(review.cocktailId, cocktailId)
        XCTAssertEqual(review.userName, "Tester", "the shared use case trims the name")
        XCTAssertEqual(review.comment, Self.validComment, "the shared use case trims the comment")
        XCTAssertEqual(review.rating, 5)
        XCTAssertFalse(review.date.isEmpty)
        XCTAssertTrue(wrapper.hasReviews)
        XCTAssertEqual(wrapper.reviewCount, 1)
        XCTAssertEqual(wrapper.state.averageRating, 5)
    }

    func testCountAndAverageAcrossMultipleReviews() async {
        let wrapper = ReviewViewModelSKIE()
        let cocktailId = uniqueCocktailId()
        await wrapper.loadReviews(for: cocktailId)

        await submitAccepted(wrapper, cocktailId: cocktailId, rating: 4, userName: "Ana")
        await submitAccepted(wrapper, cocktailId: cocktailId, rating: 2, userName: "Ben")

        let both = await waitUntil { wrapper.reviewCount == 2 }
        XCTAssertTrue(both)
        XCTAssertEqual(wrapper.reviews.count, 2)
        XCTAssertEqual(wrapper.state.averageRating, 3, accuracy: 0.001)
        XCTAssertEqual(Set(wrapper.reviews.map(\.userName)), ["Ana", "Ben"])
    }

    func testSwitchingCocktailsShowsOnlyThatCocktailsReviews() async {
        let wrapper = ReviewViewModelSKIE()
        let reviewed = uniqueCocktailId()
        let unreviewed = uniqueCocktailId()
        await wrapper.loadReviews(for: reviewed)
        await submitAccepted(wrapper, cocktailId: reviewed, rating: 3)

        await wrapper.loadReviews(for: unreviewed)
        let emptied = await waitUntil {
            wrapper.state.currentCocktailId == unreviewed && wrapper.reviews.isEmpty
        }
        XCTAssertTrue(emptied, "another cocktail's reviews must not leak into the selection")
        XCTAssertFalse(wrapper.hasReviews)

        await wrapper.loadReviews(for: reviewed)
        let restored = await waitUntil { wrapper.reviewCount == 1 }
        XCTAssertTrue(restored, "reselecting the cocktail should show its review again")
        XCTAssertEqual(wrapper.reviews.first?.cocktailId, reviewed)
    }

    func testDeletedReviewLeavesEmptyState() async {
        let wrapper = ReviewViewModelSKIE()
        let cocktailId = uniqueCocktailId()
        await wrapper.loadReviews(for: cocktailId)
        await submitAccepted(wrapper, cocktailId: cocktailId, rating: 4)
        guard let review = wrapper.reviews.first else { return XCTFail("review missing") }

        let deleted = await wrapper.deleteReview(review.id)

        XCTAssertTrue(deleted)
        let removed = await waitUntil { wrapper.reviews.isEmpty }
        XCTAssertTrue(removed)
        XCTAssertEqual(wrapper.reviewCount, 0)
        XCTAssertEqual(wrapper.state.averageRating, 0)
    }

    func testDeletingUnknownReviewReturnsFalseWithError() async {
        let wrapper = ReviewViewModelSKIE()

        let deleted = await wrapper.deleteReview("missing-\(UUID().uuidString)")

        XCTAssertFalse(deleted)
        let surfaced = await waitUntil { wrapper.error != nil }
        XCTAssertTrue(surfaced)
        XCTAssertEqual(wrapper.error?.title, "Review Not Found")
        wrapper.clearError()
    }

    // MARK: - Submit validation

    func testShortCommentIsRejected() async {
        let message = await rejectionMessage(ReviewViewModelSKIE(), rating: 4, comment: "Too short")
        XCTAssertEqual(message, "Review comment must be at least 10 characters long")
    }

    func testBlankCommentIsRejected() async {
        let message = await rejectionMessage(ReviewViewModelSKIE(), rating: 4, comment: "   ")
        XCTAssertEqual(message, "Please provide a comment for your review")
    }

    func testCommentOverFiveHundredCharactersIsRejected() async {
        let message = await rejectionMessage(ReviewViewModelSKIE(), rating: 4,
                                             comment: String(repeating: "a", count: 501))
        XCTAssertEqual(message, "Review comment must be less than 500 characters")
    }

    func testMissingRatingIsRejected() async {
        // The sheet starts at 0 stars; the shared range check is the backstop
        let message = await rejectionMessage(ReviewViewModelSKIE(), rating: 0, comment: Self.validComment)
        XCTAssertEqual(message, "Rating must be between 1 and 5 stars")
    }

    func testBlankNameIsRejected() async {
        let message = await rejectionMessage(ReviewViewModelSKIE(), rating: 4,
                                             comment: Self.validComment, userName: "  ")
        XCTAssertEqual(message, "User name is required to submit a review")
    }

    func testClearErrorResetsMirroredError() async {
        let wrapper = ReviewViewModelSKIE()
        _ = await wrapper.submitReview(cocktailId: uniqueCocktailId(), rating: 4,
                                       comment: "short", userName: "Tester")
        let surfaced = await waitUntil { wrapper.error != nil }
        XCTAssertTrue(surfaced)

        wrapper.clearError()

        let cleared = await waitUntil { wrapper.error == nil }
        XCTAssertTrue(cleared)
    }

    // MARK: - Lifecycle

    func testWrapperDeinitsWhenReleased() async {
        var wrapper: ReviewViewModelSKIE? = ReviewViewModelSKIE()
        weak var weakWrapper = wrapper
        XCTAssertNotNil(weakWrapper)

        wrapper = nil

        let released = await waitUntil { weakWrapper == nil }
        XCTAssertTrue(released, "wrapper should deinit promptly after release")
    }
}

/// Pure display rules used by the detail screen's Reviews card.
@MainActor
final class ReviewComponentsTests: XCTestCase {

    func testStarOpacityForWholeRating() {
        let opacities = (0..<5).map { StarRatingView.starOpacity(index: $0, rating: 3) }
        XCTAssertEqual(opacities, [1, 1, 1, 0.3, 0.3])
    }

    func testStarOpacityForFractionalRating() {
        let opacities = (0..<5).map { StarRatingView.starOpacity(index: $0, rating: 2.5) }
        XCTAssertEqual(opacities, [1, 1, 0.5, 0.3, 0.3])
    }

    func testStarOpacityForZeroAndFullRatings() {
        XCTAssertEqual((0..<5).map { StarRatingView.starOpacity(index: $0, rating: 0) },
                       [0.3, 0.3, 0.3, 0.3, 0.3])
        XCTAssertEqual((0..<5).map { StarRatingView.starOpacity(index: $0, rating: 5) },
                       [1, 1, 1, 1, 1])
    }

    func testReviewRowNameFallsBackToAnonymous() {
        XCTAssertEqual(ReviewRow.displayName(for: "Robert"), "Robert")
        XCTAssertEqual(ReviewRow.displayName(for: "   "), "Anonymous")
        XCTAssertEqual(ReviewRow.displayName(for: ""), "Anonymous")
    }

    func testReviewRowInitialIsUppercasedFirstLetter() {
        XCTAssertEqual(ReviewRow.initial(for: "robert"), "R")
        XCTAssertEqual(ReviewRow.initial(for: ""), "A", "blank names use Anonymous' initial")
    }

    func testReviewRowDateFallsBackToUnknown() {
        XCTAssertEqual(ReviewRow.displayDate(for: "2026-10-11"), "2026-10-11")
        XCTAssertEqual(ReviewRow.displayDate(for: ""), "Unknown date")
    }

    func testDetailShowsNewestThreeReviews() {
        XCTAssertEqual(CocktailDetailView.visibleReviewCount, 3)
    }
}
