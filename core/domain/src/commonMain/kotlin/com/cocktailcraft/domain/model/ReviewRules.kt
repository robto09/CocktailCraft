package com.cocktailcraft.domain.model

/**
 * The limits a review must meet: the single source for the shared
 * validation (ManageReviewsUseCase.validateReview) and for the review forms
 * on both platforms (star picker, comment cap and counter). Public and
 * exported so Swift reads the same values (`ReviewRules.shared`).
 */
object ReviewRules {
    /** Fewest stars a review can give. */
    const val MIN_RATING = 1

    /** Most stars a review can give: the star picker's size. */
    const val MAX_RATING = 5

    /** Shortest comment accepted, in characters. */
    const val MIN_COMMENT_LENGTH = 10

    /** Longest comment accepted, in characters: the forms stop input here. */
    const val MAX_COMMENT_LENGTH = 500
}
