package com.cocktailcraft.resources

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.util.ErrorHandler
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import io.ktor.utils.io.errors.IOException
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/**
 * The shared strings (core/resources/src/commonMain/moko-resources) as Android
 * resolves them: copy, Android escapes, positional format arguments,
 * plurals, the messages shared code produces, and the locale fallback.
 * commonTest compares StringDescs; this pins the English text behind them.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class SharedStringsTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun StringDesc.text(): String = toString(context)

    @Test
    fun plainStringsKeepTheirEscapedCharacters() {
        assertEquals("Your cart is empty", MR.strings.cart_empty_title.desc().text())
        assertEquals("You're offline — showing saved cocktails.", MR.strings.offline_you_are_offline.desc().text())
        assertEquals("Help & Support", MR.strings.help_support.desc().text())
    }

    @Test
    fun formatArgumentsArePositional() {
        assertEquals(
            "No cocktails found matching \"Mojito\" in \"Shots\"",
            MR.strings.search_no_results_query_category.format("Mojito", "Shots").text()
        )
        assertEquals("3/500", MR.strings.review_character_count.format(3, 500).text())
    }

    @Test
    fun pluralsPickTheFormForTheCount() {
        assertEquals("(1 review)", MR.plurals.review_count.format(1, 1).text())
        assertEquals("(4 reviews)", MR.plurals.review_count.format(4, 4).text())
    }

    @Test
    fun sharedCodeMessagesResolveToTheirCopy() {
        val validator = ManageReviewsUseCase(mockk<ReviewRepository>())
        assertEquals(
            "Review comment must be at least 10 characters long",
            validator.validateReview(3.0f, "Too short")?.text()
        )
        assertEquals(
            "Unable to connect to the server. Please check your internet connection.",
            ErrorHandler.getErrorFromException(IOException("offline")).message.text()
        )
        assertEquals("15-20 minutes", DeliveryPolicy.estimatedDeliveryTime(2).text())
    }

    @Test
    @Config(qualifiers = "de")
    fun aLocaleWithoutTranslationsFallsBackToTheBaseStrings() {
        assertEquals("Your cart is empty", MR.strings.cart_empty_title.desc().text())
    }
}
