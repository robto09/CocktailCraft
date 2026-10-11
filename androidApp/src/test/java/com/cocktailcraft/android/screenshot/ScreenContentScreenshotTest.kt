package com.cocktailcraft.android.screenshot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.screens.AccountSettingsCard
import com.cocktailcraft.android.screens.AppSettingsCard
import com.cocktailcraft.android.screens.AppearanceSettingsCard
import com.cocktailcraft.android.screens.HomeContentSection
import com.cocktailcraft.android.screens.ProfileHeaderCard
import com.cocktailcraft.android.screens.detail.DetailChipsSection
import com.cocktailcraft.android.screens.detail.DetailIngredientsSection
import com.cocktailcraft.android.screens.detail.DetailMainInfoCard
import com.cocktailcraft.android.screens.detail.DetailRecommendationsSection
import com.cocktailcraft.android.screens.detail.DetailReviewsSection
import com.cocktailcraft.designsystem.AccentColorTokens
import com.cocktailcraft.domain.model.Cocktail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * Stateless content of the main screens, rendered with fixed sample data
 * (the screen composables themselves are ViewModel-bound). AboutCard is
 * left out on purpose: it prints BuildConfig.VERSION_NAME, so every
 * version bump would invalidate its baseline.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class ScreenContentScreenshotTest(variant: ThemeVariant) : ScreenshotTest(variant) {

    // ---- Home ----

    @Test
    fun homeLoaded() = snapshot("Home_loaded") {
        HomeContent(cocktails = ScreenshotFixtures.cocktails)
    }

    @Test
    fun homeNoResults() = snapshot("Home_noResults") {
        HomeContent(cocktails = emptyList(), searchQuery = "Zombie", isSearchActive = true)
    }

    @Test
    fun homeOfflineError() = snapshot("Home_offlineError") {
        HomeContent(
            cocktails = emptyList(),
            errorMessage = "No internet connection",
            isNetworkAvailable = false
        )
    }

    // ---- Detail ----

    @Test
    fun detailMainInfo() = snapshot("Detail_mainInfo") {
        // The card overlaps the hero image by 20dp; pad so it isn't clipped
        Column(modifier = Modifier.padding(top = 20.dp)) {
            DetailMainInfoCard(
                cocktail = ScreenshotFixtures.cocktail,
                isFavorite = true,
                isInCart = false,
                onToggleFavorite = {},
                onAddToCartClick = {},
                onRefreshDetails = {}
            )
        }
    }

    @Test
    fun detailSections() = snapshot("Detail_sections") {
        Column {
            DetailIngredientsSection(ingredients = ScreenshotFixtures.cocktail.ingredients)
            DetailChipsSection(
                category = ScreenshotFixtures.cocktail.category,
                glass = ScreenshotFixtures.cocktail.glass,
                alcoholic = ScreenshotFixtures.cocktail.alcoholic
            )
        }
    }

    @Test
    fun detailReviews() = snapshot("Detail_reviews") {
        DetailReviewsSection(reviews = ScreenshotFixtures.reviews, onWriteReviewClick = {})
    }

    @Test
    fun detailRecommendations() = snapshot("Detail_recommendations") {
        DetailRecommendationsSection(
            category = ScreenshotFixtures.cocktail.category,
            similarCocktails = ScreenshotFixtures.cocktails,
            isLoadingRecommendations = false,
            onRecommendationClick = {}
        )
    }

    // ---- Profile ----

    @Test
    fun profileHeaderLoggedOut() = snapshot("Profile_headerLoggedOut") {
        ProfileHeaderCard(
            userName = "Guest User",
            userEmail = "guest@example.com",
            isLoggedIn = false,
            onSignInClick = {},
            onSignUpClick = {}
        )
    }

    @Test
    fun profileHeaderLoggedIn() = snapshot("Profile_headerLoggedIn") {
        ProfileHeaderCard(
            userName = "Alex",
            userEmail = "alex@example.com",
            isLoggedIn = true,
            onSignInClick = {},
            onSignUpClick = {}
        )
    }

    @Test
    fun profileSettings() = snapshot("Profile_settings") {
        Column {
            AccountSettingsCard()
            AppSettingsCard(
                isLoggedIn = true,
                isDarkMode = true,
                isSystemTheme = false,
                onOrderHistoryClick = {},
                onToggleFollowSystemTheme = {},
                onToggleDarkMode = {},
                onLogoutClick = {}
            )
        }
    }

    @Test
    fun profileAppearance() = snapshot("Profile_appearance") {
        AppearanceSettingsCard(
            accentColor = AccentColorTokens.DEFAULT,
            accentOptions = AccentColorTokens.names,
            fontSize = "medium",
            fontSizeOptions = listOf("small", "medium", "large", "xlarge"),
            accentLabel = { it.replaceFirstChar(Char::uppercase) },
            fontSizeLabel = { it.replaceFirstChar(Char::uppercase) },
            isHighContrast = false,
            isReducedMotion = true,
            onSelectAccent = {},
            onSelectFontSize = {},
            onToggleHighContrast = {},
            onToggleReducedMotion = {}
        )
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun variants() = ThemeVariant.lightAndDark()
    }
}

@Composable
private fun HomeContent(
    cocktails: List<Cocktail>,
    searchQuery: String = "",
    isSearchActive: Boolean = false,
    errorMessage: String = "",
    isNetworkAvailable: Boolean = true
) {
    HomeContentSection(
        isLoading = false,
        errorMessage = errorMessage,
        cocktails = cocktails,
        searchQuery = searchQuery,
        selectedCategory = null,
        hasActiveFilters = false,
        isSearchActive = isSearchActive,
        isLoadingMore = false,
        hasMoreData = false,
        favorites = listOf(ScreenshotFixtures.cocktail),
        isNetworkAvailable = isNetworkAvailable,
        onRetry = {},
        onClearSearch = {},
        onClearCategory = {},
        onCocktailClick = {},
        onAddToCart = {},
        onToggleFavorite = {},
        onLoadMore = {}
    )
}
