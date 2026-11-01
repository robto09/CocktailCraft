package com.cocktailcraft.format

import com.cocktailcraft.domain.model.ReviewValidationError
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.review_error_comment_required
import com.cocktailcraft.resources.review_error_comment_too_long
import com.cocktailcraft.resources.review_error_comment_too_short
import com.cocktailcraft.resources.review_error_rating_range
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format

/** The message for a review that breaks this rule. */
val ReviewValidationError.message: StringDesc
    get() = when (this) {
        ReviewValidationError.RATING_OUT_OF_RANGE -> MR.strings.review_error_rating_range.desc()

        ReviewValidationError.COMMENT_REQUIRED -> MR.strings.review_error_comment_required.desc()

        ReviewValidationError.COMMENT_TOO_SHORT ->
            MR.strings.review_error_comment_too_short.format(ManageReviewsUseCase.MIN_COMMENT_LENGTH)

        ReviewValidationError.COMMENT_TOO_LONG ->
            MR.strings.review_error_comment_too_long.format(ManageReviewsUseCase.MAX_COMMENT_LENGTH)
    }
