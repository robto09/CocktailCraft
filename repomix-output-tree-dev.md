This file is a merged representation of the entire codebase, combined into a single document by Repomix.
The content has been processed where line numbers have been added, security check has been disabled.

# File Summary

## Purpose
This file contains a packed representation of the entire repository's contents.
It is designed to be easily consumable by AI systems for analysis, code review,
or other automated processes.

## File Format
The content is organized as follows:
1. This summary section
2. Repository information
3. Directory structure
4. Repository files (if enabled)
5. Multiple file entries, each consisting of:
  a. A header with the file path (## File: path/to/file)
  b. The full contents of the file in a code block

## Usage Guidelines
- This file should be treated as read-only. Any changes should be made to the
  original repository files, not this packed version.
- When processing this file, use the file path to distinguish
  between different files in the repository.
- Be aware that this file may contain sensitive information. Handle it with
  the same level of security as you would the original repository.

## Notes
- Some files may have been excluded based on .gitignore rules and Repomix's configuration
- Binary files are not included in this packed representation. Please refer to the Repository Structure section for a complete list of file paths, including binary files
- Files matching patterns in .gitignore are excluded
- Files matching default ignore patterns are excluded
- Line numbers have been added to the beginning of each line
- Security check has been disabled - content may contain sensitive information
- Files are sorted by Git change count (files with more changes are at the bottom)

# Directory Structure
```
.kotlin/
  metadata/
    kotlinTransformedCInteropMetadataLibraries/
      .shared-iosMain.cinteropLibraries.json
androidApp/
  src/
    androidTest/
      java/
        com/
          cocktailcraft/
            ui/
              components/
                ErrorComponentsTest.kt
            CocktailCraftTestRunner.kt
      AndroidManifest.xml
    main/
      java/
        com/
          cocktailcraft/
            di/
              RecommendationModule.kt
            domain/
              recommendation/
                CocktailRecommendationEngine.kt
            model/
              SortOption.kt
            navigation/
              NavigationManager.kt
              Screen.kt
            screens/
              CartScreen.kt
              CocktailDetailScreen.kt
              FavoritesScreen.kt
              HomeScreen.kt
              OfflineModeScreen.kt
              OrderListScreen.kt
              ProfileScreen.kt
            ui/
              animation/
                AnimationUtils.kt
              components/
                AdvancedSearchPanel.kt
                AnimatedButtons.kt
                AnimatedCocktailItem.kt
                AnimatedCocktailList.kt
                AnimatedThemeSwitch.kt
                AuthDialog.kt
                CartItemCard.kt
                CategoryFilterRow.kt
                CocktailItem.kt
                CocktailLoadingShimmer.kt
                CocktailSearchBar.kt
                ConfirmationDialog.kt
                DetailHeaderImage.kt
                DetailInfoCard.kt
                EmptySearchResultsMessage.kt
                EmptyStateComponent.kt
                EndOfListMessage.kt
                ErrorDialog.kt
                ExpandableAdvancedSearchPanel.kt
                FilterChip.kt
                InfoCard.kt
                LoadingMoreIndicator.kt
                LoadingStateComponent.kt
                NetworkErrorStateDisplay.kt
                NetworkStatusCard.kt
                OfflineModeIndicator.kt
                OptimizedImage.kt
                OrderSummaryCard.kt
                ProfileCard.kt
                RatingBar.kt
                RatingDisplay.kt
                RecommendationsSection.kt
                SearchFilterChips.kt
                SectionHeader.kt
                SettingsCard.kt
                ShimmerLoading.kt
                StatusIndicator.kt
                ToggleSettingItem.kt
                WriteReviewDialog.kt
              main/
                MainScreen.kt
              theme/
                AnimatedTheme.kt
                Theme.kt
                ThemeAssets.kt
            util/
              ErrorUtils.kt
              FilterOptionsLoader.kt
              ImageLoaderSingleton.kt
              ImageUtils.kt
              ListOptimizations.kt
            viewmodel/
              BaseViewModel.kt
              CartViewModel.kt
              CocktailDetailViewModel.kt
              FavoritesViewModel.kt
              HomeViewModel.kt
              KoinViewModel.kt
              OfflineModeViewModel.kt
              OrderViewModel.kt
              ProfileViewModel.kt
              ReviewViewModel.kt
              ThemeViewModel.kt
            CocktailCraftApplication.kt
            MainActivity.kt
      res/
        drawable/
          bg_pattern_dark.xml
          bg_pattern_light.xml
          cocktail_placeholder_dark.xml
          cocktail_placeholder_light.xml
          empty_state_dark.xml
          empty_state_light.xml
          ic_launcher_foreground.xml
          logo_dark.xml
          logo_light.xml
        mipmap-anydpi-v26/
          ic_launcher_round.xml
          ic_launcher.xml
        values/
          colors.xml
          ic_launcher_background.xml
          strings.xml
          themes.xml
      AndroidManifest.xml
    test/
      java/
        com/
          cocktailcraft/
            data/
              repository/
                AuthRepositoryImplTest.kt
            di/
              TestModule.kt
            domain/
              usecase/
                PlaceOrderUseCaseTest.kt
            navigation/
              NavigationManagerTest.kt
            util/
              ErrorUtilsTest.kt
            viewmodel/
              BaseViewModelTest.kt
              CartViewModelTest.kt
              HomeViewModelTest.kt
              KoinThemeViewModelTest.kt
              OrderViewModelTest.kt
              ProfileViewModelTest.kt
              ThemeViewModelTest.kt
            BaseKoinTest.kt
  build.gradle.kts
docs/
  images/
    mermaid/
      class_diagram.md
      component_diagram.md
      data_flow_diagram.md
      high_level_architecture.md
      package_diagram.md
      README.md
      sequence_diagram_place_order.md
      use_case_diagram.md
  AdvancedSearch.md
  animations.md
  DependencyInjection.md
  DependencyInjectionMigration.md
  Libraries.md
  README.md
  RecommendationSystem.md
  UI_Components.md
gradle/
  wrapper/
    gradle-wrapper.properties
shared/
  src/
    androidMain/
      kotlin/
        com/
          cocktailcraft/
            di/
              PlatformModule.kt
            util/
              CocktailDebugLogger.kt
              NetworkMonitor.kt
    commonMain/
      kotlin/
        com/
          cocktailcraft/
            data/
              cache/
                CocktailCache.kt
              config/
                AppConfigImpl.kt
              remote/
                CocktailApi.kt
                CocktailApiImpl.kt
                CocktailDto.kt
              repository/
                AuthRepositoryImpl.kt
                CartRepositoryImpl.kt
                CocktailRepositoryImpl.kt
                FavoritesRepositoryImpl.kt
                OrderRepositoryImpl.kt
            di/
              AppModule.kt
              DataModule.kt
              DomainModule.kt
              NetworkModule.kt
              PlatformModule.kt
            domain/
              config/
                AppConfig.kt
              model/
                Cocktail.kt
                CocktailCartItem.kt
                CocktailIngredient.kt
                Order.kt
                Review.kt
                SearchFilters.kt
                User.kt
              repository/
                AuthRepository.kt
                CartRepository.kt
                CocktailRepository.kt
                FavoritesRepository.kt
                OrderRepository.kt
              usecase/
                PlaceOrderUseCase.kt
                ToggleFavoriteUseCase.kt
              util/
                ErrorCode.kt
                Result.kt
            util/
              CocktailDebugLogger.kt
              NetworkMonitor.kt
    iosMain/
      kotlin/
        com/
          cocktailcraft/
            di/
              PlatformModule.kt
  build.gradle.kts
.gitignore
build.gradle.kts
gradle.properties
gradlew
gradlew.bat
libraries.toml
README.md
settings.gradle.kts
TEST_CASES.md
versions.toml
```

# Files

## File: .kotlin/metadata/kotlinTransformedCInteropMetadataLibraries/.shared-iosMain.cinteropLibraries.json
````json
1: []
````

## File: androidApp/src/androidTest/java/com/cocktailcraft/ui/components/ErrorComponentsTest.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.ui.test.assertIsDisplayed
  4: import androidx.compose.ui.test.junit4.createAndroidComposeRule
  5: import androidx.compose.ui.test.onNodeWithText
  6: import androidx.compose.ui.test.performClick
  7: import androidx.test.ext.junit.runners.AndroidJUnit4
  8: import androidx.test.filters.LargeTest
  9: import com.cocktailcraft.MainActivity
 10: import com.cocktailcraft.util.ErrorUtils
 11: import org.junit.Rule
 12: import org.junit.Test
 13: import org.junit.runner.RunWith
 14: import org.mockito.Mockito.mock
 15: import org.mockito.Mockito.verify
 16: 
 17: @RunWith(AndroidJUnit4::class)
 18: @LargeTest
 19: class ErrorComponentsTest {
 20: 
 21:     @get:Rule
 22:     val composeTestRule = createAndroidComposeRule<MainActivity>()
 23: 
 24:     @Test
 25:     fun errorBanner_shouldDisplayErrorInfo() {
 26:         // Given
 27:         val error = ErrorUtils.createUserFriendlyError(
 28:             title = "Test Error",
 29:             message = "This is a test error message",
 30:             category = ErrorUtils.ErrorCategory.NETWORK
 31:         )
 32: 
 33:         // When
 34:         composeTestRule.setContent {
 35:             ErrorBanner(
 36:                 error = error,
 37:                 onDismiss = {},
 38:                 onAction = {}
 39:             )
 40:         }
 41: 
 42:         // Then
 43:         composeTestRule.onNodeWithText("Test Error").assertIsDisplayed()
 44:         composeTestRule.onNodeWithText("This is a test error message").assertIsDisplayed()
 45:     }
 46: 
 47:     @Test
 48:     fun errorBanner_shouldCallOnDismissWhenDismissed() {
 49:         // Given
 50:         val error = ErrorUtils.createUserFriendlyError(
 51:             title = "Test Error",
 52:             message = "This is a test error message"
 53:         )
 54:         val onDismiss = mock(Runnable::class.java)
 55: 
 56:         // When
 57:         composeTestRule.setContent {
 58:             ErrorBanner(
 59:                 error = error,
 60:                 onDismiss = { onDismiss.run() },
 61:                 onAction = {}
 62:             )
 63:         }
 64: 
 65:         // Then
 66:         composeTestRule.onNodeWithText("Dismiss").performClick()
 67:         verify(onDismiss).run()
 68:     }
 69: 
 70:     @Test
 71:     fun errorBanner_shouldCallOnActionWhenActionClicked() {
 72:         // Given
 73:         val recoveryAction = ErrorUtils.RecoveryAction("Retry") { /* no-op */ }
 74:         val error = ErrorUtils.createUserFriendlyError(
 75:             title = "Test Error",
 76:             message = "This is a test error message",
 77:             recoveryAction = recoveryAction
 78:         )
 79:         val onAction = mock(Runnable::class.java)
 80: 
 81:         // When
 82:         composeTestRule.setContent {
 83:             ErrorBanner(
 84:                 error = error,
 85:                 onDismiss = {},
 86:                 onAction = { onAction.run() }
 87:             )
 88:         }
 89: 
 90:         // Then
 91:         composeTestRule.onNodeWithText("Retry").performClick()
 92:         verify(onAction).run()
 93:     }
 94: 
 95:     @Test
 96:     fun errorDialog_shouldDisplayErrorInfo() {
 97:         // Given
 98:         val error = ErrorUtils.createUserFriendlyError(
 99:             title = "Test Error",
100:             message = "This is a test error message",
101:             category = ErrorUtils.ErrorCategory.NETWORK
102:         )
103: 
104:         // When
105:         composeTestRule.setContent {
106:             ErrorDialog(
107:                 error = error,
108:                 onDismiss = {},
109:                 onRetry = {}
110:             )
111:         }
112: 
113:         // Then
114:         composeTestRule.onNodeWithText("Test Error").assertIsDisplayed()
115:         composeTestRule.onNodeWithText("This is a test error message").assertIsDisplayed()
116:     }
117: 
118:     @Test
119:     fun errorDialog_shouldCallOnDismissWhenDismissed() {
120:         // Given
121:         val error = ErrorUtils.createUserFriendlyError(
122:             title = "Test Error",
123:             message = "This is a test error message"
124:         )
125:         val onDismiss = mock(Runnable::class.java)
126: 
127:         // When
128:         composeTestRule.setContent {
129:             ErrorDialog(
130:                 error = error,
131:                 onDismiss = { onDismiss.run() },
132:                 onRetry = {}
133:             )
134:         }
135: 
136:         // Then
137:         composeTestRule.onNodeWithText("Dismiss").performClick()
138:         verify(onDismiss).run()
139:     }
140: 
141:     @Test
142:     fun errorDialog_shouldCallOnRetryWhenRetryClicked() {
143:         // Given
144:         val error = ErrorUtils.createUserFriendlyError(
145:             title = "Test Error",
146:             message = "This is a test error message"
147:         )
148:         val onRetry = mock(Runnable::class.java)
149: 
150:         // When
151:         composeTestRule.setContent {
152:             ErrorDialog(
153:                 error = error,
154:                 onDismiss = {},
155:                 onRetry = { onRetry.run() }
156:             )
157:         }
158: 
159:         // Then
160:         composeTestRule.onNodeWithText("Retry").performClick()
161:         verify(onRetry).run()
162:     }
163: }
````

## File: androidApp/src/androidTest/java/com/cocktailcraft/CocktailCraftTestRunner.kt
````kotlin
 1: package com.cocktailcraft
 2: 
 3: import android.app.Application
 4: import android.content.Context
 5: import androidx.test.runner.AndroidJUnitRunner
 6: import org.koin.android.ext.koin.androidContext
 7: import org.koin.core.context.startKoin
 8: import org.koin.core.context.stopKoin
 9: 
10: /**
11:  * Custom test runner for instrumented tests.
12:  * This ensures Koin is properly initialized for tests.
13:  */
14: class CocktailCraftTestRunner : AndroidJUnitRunner() {
15:     override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
16:         return super.newApplication(cl, TestApplication::class.java.name, context)
17:     }
18: }
19: 
20: /**
21:  * Test application class for instrumented tests.
22:  */
23: class TestApplication : Application() {
24:     override fun onCreate() {
25:         super.onCreate()
26:         
27:         // Stop Koin if it's already started
28:         try {
29:             stopKoin()
30:         } catch (e: Exception) {
31:             // Ignore if Koin wasn't started
32:         }
33:         
34:         // Start Koin with minimal configuration for tests
35:         startKoin {
36:             androidContext(this@TestApplication)
37:             // No modules needed for UI component tests
38:         }
39:     }
40: }
````

## File: androidApp/src/androidTest/AndroidManifest.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <manifest xmlns:android="http://schemas.android.com/apk/res/android"
 3:     package="com.cocktailcraft.test">
 4: 
 5:     <application
 6:         android:name="com.cocktailcraft.TestApplication"
 7:         android:debuggable="true">
 8:         <uses-library android:name="android.test.runner" />
 9:     </application>
10: 
11:     <instrumentation
12:         android:name="com.cocktailcraft.CocktailCraftTestRunner"
13:         android:targetPackage="com.cocktailcraft" />
14: 
15: </manifest>
````

## File: androidApp/src/main/java/com/cocktailcraft/di/RecommendationModule.kt
````kotlin
 1: package com.cocktailcraft.di
 2: 
 3: import com.cocktailcraft.domain.recommendation.CocktailRecommendationEngine
 4: import com.cocktailcraft.viewmodel.CocktailDetailViewModel
 5: import org.koin.androidx.viewmodel.dsl.viewModel
 6: import org.koin.dsl.module
 7: 
 8: /**
 9:  * Koin module for recommendation-related dependencies.
10:  */
11: val recommendationModule = module {
12:     // Recommendation Engine
13:     single { CocktailRecommendationEngine(get(), get()) }
14: 
15:     // ViewModels
16:     viewModel { CocktailDetailViewModel(get(), get()) }
17: }
````

## File: androidApp/src/main/java/com/cocktailcraft/domain/recommendation/CocktailRecommendationEngine.kt
````kotlin
  1: package com.cocktailcraft.domain.recommendation
  2: 
  3: import com.cocktailcraft.domain.model.Cocktail
  4: import com.cocktailcraft.domain.repository.CocktailRepository
  5: import com.cocktailcraft.domain.repository.FavoritesRepository
  6: import kotlinx.coroutines.flow.first
  7: import kotlinx.coroutines.flow.firstOrNull
  8: 
  9: /**
 10:  * Engine for generating cocktail recommendations based on various strategies.
 11:  * This client-side implementation works within the constraints of the free TheCocktailDB API.
 12:  */
 13: class CocktailRecommendationEngine(
 14:     private val cocktailRepository: CocktailRepository,
 15:     private val favoritesRepository: FavoritesRepository
 16: ) {
 17:     /**
 18:      * Get recommendations for a given cocktail.
 19:      * Uses a hybrid approach combining multiple recommendation strategies.
 20:      *
 21:      * @param cocktail The current cocktail to generate recommendations for
 22:      * @param limit Maximum number of recommendations to return
 23:      * @return List of recommended cocktails
 24:      */
 25:     suspend fun getRecommendations(cocktail: Cocktail, limit: Int = 3): List<Cocktail> {
 26:         val recommendations = mutableListOf<Cocktail>()
 27:         val excludeIds = mutableSetOf(cocktail.id)
 28: 
 29:         // Strategy 1: Category-based recommendations
 30:         cocktail.category?.let { category ->
 31:             val categorySimilar = getCocktailsByCategory(category, excludeIds)
 32:             recommendations.addAll(categorySimilar)
 33:             excludeIds.addAll(categorySimilar.map { it.id })
 34:         }
 35: 
 36:         // Strategy 2: Ingredient-based recommendations (if we need more)
 37:         if (recommendations.size < limit && cocktail.ingredients.isNotEmpty()) {
 38:             val mainIngredient = cocktail.ingredients.firstOrNull()?.name
 39:             if (!mainIngredient.isNullOrBlank()) {
 40:                 val ingredientSimilar = getCocktailsByIngredient(mainIngredient, excludeIds)
 41:                 recommendations.addAll(ingredientSimilar)
 42:                 excludeIds.addAll(ingredientSimilar.map { it.id })
 43:             }
 44:         }
 45: 
 46:         // Strategy 3: User preferences (favorites)
 47:         if (recommendations.size < limit) {
 48:             val favorites = favoritesRepository.getFavorites().first()
 49:             val favoriteCategories = favorites
 50:                 .mapNotNull { it.category }
 51:                 .groupBy { it }
 52:                 .maxByOrNull { it.value.size }
 53:                 ?.key
 54: 
 55:             if (favoriteCategories != null && favoriteCategories != cocktail.category) {
 56:                 val favoriteCategorySimilar = getCocktailsByCategory(favoriteCategories, excludeIds)
 57:                 recommendations.addAll(favoriteCategorySimilar)
 58:                 excludeIds.addAll(favoriteCategorySimilar.map { it.id })
 59:             }
 60:         }
 61: 
 62:         // Strategy 4: Alcoholic/Non-alcoholic matching
 63:         cocktail.alcoholic?.let { alcoholicFilter ->
 64:             if (recommendations.size < limit) {
 65:                 val alcoholicSimilar = getCocktailsByAlcoholicFilter(alcoholicFilter, excludeIds)
 66:                 recommendations.addAll(alcoholicSimilar)
 67:             }
 68:         }
 69: 
 70:         return recommendations.take(limit)
 71:     }
 72: 
 73:     /**
 74:      * Get cocktails from the same category.
 75:      */
 76:     private suspend fun getCocktailsByCategory(
 77:         category: String,
 78:         excludeIds: Set<String> = emptySet(),
 79:         limit: Int = 2
 80:     ): List<Cocktail> {
 81:         return cocktailRepository.getCocktailsByCategory(category)
 82:             .filter { it.id !in excludeIds }
 83:             .shuffled() // Add some randomness
 84:             .take(limit)
 85:     }
 86: 
 87:     /**
 88:      * Get cocktails with the specified ingredient.
 89:      */
 90:     private suspend fun getCocktailsByIngredient(
 91:         ingredient: String,
 92:         excludeIds: Set<String> = emptySet(),
 93:         limit: Int = 2
 94:     ): List<Cocktail> {
 95:         return cocktailRepository.getCocktailsByIngredient(ingredient)
 96:             .filter { it.id !in excludeIds }
 97:             .shuffled() // Add some randomness
 98:             .take(limit)
 99:     }
100: 
101:     /**
102:      * Get cocktails matching the alcoholic/non-alcoholic filter.
103:      */
104:     private suspend fun getCocktailsByAlcoholicFilter(
105:         alcoholicFilter: String,
106:         excludeIds: Set<String> = emptySet(),
107:         limit: Int = 2
108:     ): List<Cocktail> {
109:         return cocktailRepository.getCocktailsByAlcoholicFilter(alcoholicFilter)
110:             .filter { it.id !in excludeIds }
111:             .shuffled() // Add some randomness
112:             .take(limit)
113:     }
114: }
````

## File: androidApp/src/main/java/com/cocktailcraft/model/SortOption.kt
````kotlin
1: package com.cocktailcraft.model
2: 
3: enum class SortOption(val displayName: String) {
4:     NAME_ASC("Name (A-Z)"),
5:     NAME_DESC("Name (Z-A)"),
6: }
````

## File: androidApp/src/main/java/com/cocktailcraft/navigation/NavigationManager.kt
````kotlin
  1: package com.cocktailcraft.navigation
  2: 
  3: import androidx.navigation.NavController
  4: import androidx.navigation.NavGraph.Companion.findStartDestination
  5: import com.cocktailcraft.domain.model.Cocktail
  6: 
  7: /**
  8:  * Centralized navigation manager for the app.
  9:  * All navigation actions should go through this class to maintain consistency.
 10:  */
 11: class NavigationManager(private val navController: NavController) {
 12: 
 13:     /**
 14:      * Navigate to the cocktail detail screen
 15:      */
 16:     fun navigateToCocktailDetail(cocktailId: String) {
 17:         navController.navigate(Screen.CocktailDetail.createRoute(cocktailId))
 18:     }
 19: 
 20:     /**
 21:      * Navigate to the cocktail detail screen from a cocktail object
 22:      */
 23:     fun navigateToCocktailDetail(cocktail: Cocktail) {
 24:         navigateToCocktailDetail(cocktail.id)
 25:     }
 26: 
 27:     /**
 28:      * Navigate to the cart screen
 29:      */
 30:     fun navigateToCart() {
 31:         navController.navigate(Screen.Cart.route)
 32:     }
 33: 
 34:     /**
 35:      * Navigate to the home screen
 36:      */
 37:     fun navigateToHome() {
 38:         navController.navigate(Screen.Home.route) {
 39:             popUpTo(navController.graph.findStartDestination().id)
 40:             launchSingleTop = true
 41:         }
 42:     }
 43: 
 44:     /**
 45:      * Navigate to the favorites screen
 46:      */
 47:     fun navigateToFavorites() {
 48:         navController.navigate(Screen.Favorites.route) {
 49:             popUpTo(navController.graph.findStartDestination().id)
 50:             launchSingleTop = true
 51:         }
 52:     }
 53: 
 54:     /**
 55:      * Navigate to the profile screen
 56:      */
 57:     fun navigateToProfile() {
 58:         navController.navigate(Screen.Profile.route) {
 59:             popUpTo(navController.graph.findStartDestination().id)
 60:             launchSingleTop = true
 61:         }
 62:     }
 63: 
 64:     /**
 65:      * Navigate to the order list screen
 66:      */
 67:     fun navigateToOrderList() {
 68:         navController.navigate(Screen.OrderList.route) {
 69:             popUpTo(navController.graph.findStartDestination().id)
 70:             launchSingleTop = true
 71:         }
 72:     }
 73: 
 74:     /**
 75:      * Navigate back to the previous screen
 76:      */
 77:     fun navigateBack() {
 78:         navController.popBackStack()
 79:     }
 80: 
 81:     /**
 82:      * Navigate to the offline mode screen
 83:      */
 84:     fun navigateToOfflineMode() {
 85:         navController.navigate(Screen.OfflineMode.route)
 86:     }
 87: 
 88:     /**
 89:      * Navigate to the reviews screen for a specific cocktail
 90:      */
 91:     fun navigateToReviews(cocktailId: String) {
 92:         navController.navigate(Screen.Reviews.createRoute(cocktailId))
 93:     }
 94: 
 95:     /**
 96:      * Navigate to a bottom navigation destination
 97:      */
 98:     fun navigateToBottomNavDestination(screen: Screen) {
 99:         navController.navigate(screen.route) {
100:             popUpTo(navController.graph.findStartDestination().id)
101:             launchSingleTop = true
102:         }
103:     }
104: }
````

## File: androidApp/src/main/java/com/cocktailcraft/navigation/Screen.kt
````kotlin
 1: package com.cocktailcraft.navigation
 2: 
 3: import androidx.compose.material.icons.Icons
 4: import androidx.compose.material.icons.filled.Favorite
 5: import androidx.compose.material.icons.filled.Home
 6: import androidx.compose.material.icons.filled.List
 7: import androidx.compose.material.icons.filled.Person
 8: import androidx.compose.material.icons.filled.ShoppingCart
 9: import androidx.compose.material.icons.filled.WifiOff
10: import androidx.compose.ui.graphics.vector.ImageVector
11: 
12: sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
13:     object Home : Screen("home", "Home", Icons.Filled.Home)
14:     object Cart : Screen("cart", "Cart", Icons.Filled.ShoppingCart)
15:     object Profile : Screen("profile", "Profile", Icons.Filled.Person)
16:     object Favorites : Screen("favorites", "Favorites", Icons.Filled.Favorite)
17:     object OrderList : Screen("orders", "Orders", Icons.Filled.List)
18:     object CocktailDetail : Screen("cocktail_detail/{cocktailId}", "Cocktail Detail", Icons.Filled.Home) {
19:         fun createRoute(cocktailId: String) = "cocktail_detail/$cocktailId"
20:     }
21: 
22:     object Reviews : Screen("reviews/{cocktailId}", "Reviews", Icons.Filled.List) {
23:         fun createRoute(cocktailId: String) = "reviews/$cocktailId"
24:     }
25: 
26:     object OfflineMode : Screen("offline_mode", "Offline Mode", Icons.Filled.WifiOff)
27: }
````

## File: androidApp/src/main/java/com/cocktailcraft/screens/CartScreen.kt
````kotlin
  1: package com.cocktailcraft.screens
  2: 
  3: import androidx.compose.foundation.background
  4: import androidx.compose.foundation.layout.Arrangement
  5: import androidx.compose.foundation.layout.Column
  6: import androidx.compose.foundation.layout.PaddingValues
  7: import androidx.compose.foundation.layout.Spacer
  8: import androidx.compose.foundation.layout.fillMaxSize
  9: import androidx.compose.foundation.layout.fillMaxWidth
 10: import androidx.compose.foundation.layout.height
 11: import androidx.compose.foundation.layout.padding
 12: import androidx.compose.foundation.lazy.LazyColumn
 13: import androidx.compose.foundation.lazy.itemsIndexed
 14: import androidx.compose.foundation.shape.RoundedCornerShape
 15: import androidx.compose.material.icons.Icons
 16: import androidx.compose.material.icons.filled.ShoppingCart
 17: import androidx.compose.material3.Button
 18: import androidx.compose.material3.ButtonDefaults
 19: import androidx.compose.material3.Text
 20: import androidx.compose.runtime.Composable
 21: import androidx.compose.runtime.collectAsState
 22: import androidx.compose.runtime.getValue
 23: import androidx.compose.runtime.mutableStateOf
 24: import androidx.compose.runtime.remember
 25: import androidx.compose.runtime.setValue
 26: import androidx.compose.ui.Modifier
 27: import androidx.compose.ui.text.font.FontWeight
 28: import androidx.compose.ui.unit.dp
 29: import androidx.compose.ui.unit.sp
 30: import com.cocktailcraft.ui.components.CartItemCard
 31: import com.cocktailcraft.ui.components.ConfirmationDialog
 32: import com.cocktailcraft.ui.components.EmptyStateComponent
 33: import com.cocktailcraft.ui.components.LoadingStateComponent
 34: import com.cocktailcraft.ui.components.OrderSummaryCard
 35: import com.cocktailcraft.ui.components.SectionHeader
 36: import com.cocktailcraft.ui.theme.AppColors
 37: import com.cocktailcraft.viewmodel.CartViewModel
 38: import com.cocktailcraft.viewmodel.FavoritesViewModel
 39: import com.cocktailcraft.viewmodel.OrderViewModel
 40: import com.cocktailcraft.navigation.NavigationManager
 41: import java.text.NumberFormat
 42: import java.util.Locale
 43: 
 44: @Composable
 45: fun CartScreen(
 46:     viewModel: CartViewModel,
 47:     orderViewModel: OrderViewModel,
 48:     navigationManager: NavigationManager,
 49:     onStartShopping: () -> Unit,
 50:     favoritesViewModel: FavoritesViewModel
 51: ) {
 52:     val cartItems by viewModel.cartItems.collectAsState()
 53:     val totalPrice by viewModel.totalPrice.collectAsState()
 54:     val isLoading by viewModel.isLoading.collectAsState()
 55:     val error by viewModel.error.collectAsState()
 56:     val favorites by favoritesViewModel.favorites.collectAsState()
 57: 
 58:     val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
 59: 
 60:     var showPlaceOrderDialog by remember { mutableStateOf(false) }
 61: 
 62:     Column(
 63:         modifier = Modifier
 64:             .fillMaxSize()
 65:             .background(AppColors.Background)
 66:             .padding(16.dp)
 67:     ) {
 68:         // Show loading state
 69:         LoadingStateComponent(isLoading = isLoading)
 70: 
 71:         // Show error state
 72:         if (!isLoading && error != null) {
 73:             EmptyStateComponent(
 74:                 title = "Error",
 75:                 message = error ?: "An unknown error occurred",
 76:                 actionButtonText = "Try Again",
 77:                 onActionButtonClick = { /* Add retry logic here */ }
 78:             )
 79:         }
 80:         // Show empty cart state
 81:         else if (!isLoading && cartItems.isEmpty()) {
 82:             EmptyStateComponent(
 83:                 title = "Your cart is empty",
 84:                 message = "Add some cocktails to your cart and they will appear here",
 85:                 actionButtonText = "Start Shopping",
 86:                 onActionButtonClick = onStartShopping,
 87:                 icon = Icons.Filled.ShoppingCart
 88:             )
 89:         }
 90:         // Show cart items
 91:         else if (!isLoading) {
 92:             SectionHeader(
 93:                 title = "Your Cart",
 94:                 fontSize = 20,
 95:                 modifier = Modifier.padding(bottom = 8.dp)
 96:             )
 97: 
 98:             LazyColumn(
 99:                 modifier = Modifier
100:                     .weight(1f)
101:                     .fillMaxWidth(),
102:                 verticalArrangement = Arrangement.spacedBy(12.dp),
103:                 contentPadding = PaddingValues(bottom = 8.dp)
104:             ) {
105:                 itemsIndexed(cartItems) { _, item ->
106:                     CartItemCard(
107:                         item = item,
108:                         onIncreaseQuantity = { viewModel.updateQuantity(item.cocktail.id, item.quantity + 1) },
109:                         onDecreaseQuantity = {
110:                             if (item.quantity > 1) {
111:                                 viewModel.updateQuantity(item.cocktail.id, item.quantity - 1)
112:                             } else {
113:                                 viewModel.removeFromCart(item.cocktail.id)
114:                             }
115:                         },
116:                         onRemove = { viewModel.removeFromCart(item.cocktail.id) },
117:                         isFavorite = favorites.any { it.id == item.cocktail.id },
118:                         onToggleFavorite = { favoritesViewModel.toggleFavorite(item.cocktail) }
119:                     )
120:                 }
121:             }
122: 
123:             Spacer(modifier = Modifier.height(16.dp))
124: 
125:             // Order Summary
126:             OrderSummaryCard(
127:                 subtotal = totalPrice,
128:                 deliveryFee = 5.99,
129:                 modifier = Modifier.padding(vertical = 8.dp),
130:                 currencyFormatter = currencyFormatter
131:             )
132: 
133:             Spacer(modifier = Modifier.height(16.dp))
134: 
135:             // Checkout Button
136:             Button(
137:                 onClick = { showPlaceOrderDialog = true },
138:                 modifier = Modifier
139:                     .fillMaxWidth()
140:                     .height(54.dp),
141:                 colors = ButtonDefaults.buttonColors(
142:                     containerColor = AppColors.Primary
143:                 ),
144:                 shape = RoundedCornerShape(12.dp)
145:             ) {
146:                 Text(
147:                     text = "Place Order",
148:                     fontWeight = FontWeight.Bold,
149:                     fontSize = 16.sp
150:                 )
151:             }
152:         }
153:     }
154: 
155:     // Order Confirmation Dialog
156:     ConfirmationDialog(
157:         showDialog = showPlaceOrderDialog,
158:         title = "Confirm Order",
159:         message = "Are you sure you want to place this order for ${currencyFormatter.format(totalPrice + 5.99)}?",
160:         confirmButtonText = "Confirm",
161:         dismissButtonText = "Cancel",
162:         onConfirm = {
163:             orderViewModel.placeOrder(cartItems, totalPrice)
164:             viewModel.clearCart()
165:             showPlaceOrderDialog = false
166:             navigationManager.navigateToOrderList()
167:         },
168:         onDismiss = { showPlaceOrderDialog = false }
169:     )
170: }
````

## File: androidApp/src/main/java/com/cocktailcraft/screens/CocktailDetailScreen.kt
````kotlin
  1: package com.cocktailcraft.screens
  2: 
  3: import androidx.compose.animation.AnimatedVisibility
  4: import androidx.compose.animation.core.tween
  5: import androidx.compose.animation.fadeIn
  6: import androidx.compose.animation.fadeOut
  7: import androidx.compose.foundation.BorderStroke
  8: import androidx.compose.foundation.background
  9: import androidx.compose.foundation.clickable
 10: import androidx.compose.foundation.layout.Arrangement
 11: import androidx.compose.foundation.layout.PaddingValues
 12: import androidx.compose.foundation.layout.Box
 13: import androidx.compose.foundation.layout.Column
 14: import androidx.compose.foundation.layout.Row
 15: import androidx.compose.foundation.layout.Spacer
 16: import androidx.compose.foundation.layout.fillMaxSize
 17: import androidx.compose.foundation.layout.fillMaxWidth
 18: import androidx.compose.foundation.layout.height
 19: import androidx.compose.foundation.layout.offset
 20: import androidx.compose.foundation.layout.padding
 21: import androidx.compose.foundation.layout.size
 22: import androidx.compose.foundation.layout.width
 23: import androidx.compose.foundation.lazy.LazyColumn
 24: import androidx.compose.foundation.lazy.LazyRow
 25: import androidx.compose.foundation.lazy.items
 26: import androidx.compose.foundation.shape.CircleShape
 27: import androidx.compose.foundation.shape.RoundedCornerShape
 28: import androidx.compose.material.icons.Icons
 29: import androidx.compose.material.icons.filled.ArrowBack
 30: import androidx.compose.material.icons.filled.Add
 31: import androidx.compose.material.icons.filled.Edit
 32: import androidx.compose.material.icons.filled.Favorite
 33: import androidx.compose.material.icons.filled.FavoriteBorder
 34: import androidx.compose.material.icons.filled.LocalBar
 35: import androidx.compose.material.icons.filled.Remove
 36: import androidx.compose.material.icons.filled.ShoppingCart
 37: import androidx.compose.material.icons.filled.Star
 38: import androidx.compose.material.icons.outlined.FavoriteBorder
 39: import androidx.compose.material.icons.outlined.Star
 40: import androidx.compose.material3.AlertDialog
 41: import androidx.compose.material3.Button
 42: import androidx.compose.material3.ButtonDefaults
 43: import androidx.compose.material3.Card
 44: import androidx.compose.material3.CardDefaults
 45: import androidx.compose.material3.CircularProgressIndicator
 46: import androidx.compose.material3.Divider
 47: import androidx.compose.material3.ExperimentalMaterial3Api
 48: import androidx.compose.material3.Icon
 49: import androidx.compose.material3.IconButton
 50: import androidx.compose.material3.MaterialTheme
 51: import androidx.compose.material3.OutlinedButton
 52: import androidx.compose.material3.OutlinedTextField
 53: import androidx.compose.material3.Scaffold
 54: import androidx.compose.material3.SnackbarDuration
 55: import androidx.compose.material3.SnackbarHost
 56: import androidx.compose.material3.SnackbarHostState
 57: import androidx.compose.material3.SuggestionChip
 58: import androidx.compose.material3.SuggestionChipDefaults
 59: import androidx.compose.material3.Surface
 60: import androidx.compose.material3.Text
 61: import androidx.compose.material3.TextButton
 62: import androidx.compose.material3.TopAppBar
 63: import androidx.compose.material3.TopAppBarDefaults
 64: import androidx.compose.runtime.Composable
 65: import androidx.compose.runtime.LaunchedEffect
 66: import androidx.compose.runtime.collectAsState
 67: import androidx.compose.runtime.getValue
 68: import androidx.compose.runtime.mutableStateOf
 69: import androidx.compose.runtime.remember
 70: import androidx.compose.runtime.rememberCoroutineScope
 71: import androidx.compose.runtime.setValue
 72: import androidx.compose.ui.Alignment
 73: import androidx.compose.ui.Modifier
 74: import androidx.compose.ui.draw.clip
 75: import androidx.compose.ui.graphics.Brush
 76: import androidx.compose.ui.graphics.Color
 77: import androidx.compose.ui.layout.ContentScale
 78: import androidx.compose.ui.platform.LocalContext
 79: import androidx.compose.ui.text.font.FontWeight
 80: import androidx.compose.ui.text.style.TextAlign
 81: import androidx.compose.ui.text.style.TextOverflow
 82: import androidx.compose.ui.unit.dp
 83: import androidx.compose.ui.unit.sp
 84: import com.cocktailcraft.domain.model.Cocktail
 85: import com.cocktailcraft.domain.model.CocktailIngredient
 86: import com.cocktailcraft.domain.model.Review
 87: import com.cocktailcraft.ui.theme.AppColors
 88: import com.cocktailcraft.ui.components.DetailHeaderImage
 89: import com.cocktailcraft.ui.components.DetailInfoCard
 90: import com.cocktailcraft.ui.components.LoadingStateComponent
 91: import com.cocktailcraft.ui.components.OptimizedImage
 92: import com.cocktailcraft.ui.components.RatingBar
 93: import com.cocktailcraft.ui.components.RatingDisplay
 94: import com.cocktailcraft.ui.components.RecommendationsSection
 95: import com.cocktailcraft.ui.components.SectionHeader
 96: import com.cocktailcraft.ui.components.WriteReviewDialog
 97: import com.cocktailcraft.viewmodel.CartViewModel
 98: import com.cocktailcraft.viewmodel.CocktailDetailViewModel
 99: import com.cocktailcraft.viewmodel.FavoritesViewModel
100: import com.cocktailcraft.viewmodel.HomeViewModel
101: import com.cocktailcraft.viewmodel.ReviewViewModel
102: import com.cocktailcraft.navigation.NavigationManager
103: import kotlinx.coroutines.CoroutineScope
104: import kotlinx.coroutines.launch
105: import org.koin.androidx.compose.koinViewModel
106: import org.koin.core.parameter.parametersOf
107: import androidx.compose.animation.core.*
108: import com.cocktailcraft.ui.components.shimmerEffect
109: 
110: @OptIn(ExperimentalMaterial3Api::class)
111: @Composable
112: fun CocktailDetailScreen(
113:     cocktailId: String,
114:     homeViewModel: HomeViewModel,
115:     cartViewModel: CartViewModel,
116:     reviewViewModel: ReviewViewModel,
117:     favoritesViewModel: FavoritesViewModel,
118:     navigationManager: NavigationManager,
119:     onBackClick: () -> Unit,
120:     onAddToCart: (Cocktail) -> Unit
121: ) {
122:     // Add a loading state to track when data is being fetched
123:     var isLoading by remember { mutableStateOf(true) }
124: 
125:     // Use collectAsState instead of collectAsStateWithLifecycle
126:     val cocktailFlow = remember { homeViewModel.getCocktailById(cocktailId) }
127:     val cocktail by cocktailFlow.collectAsState(initial = null)
128: 
129:     // Properly collect reviews as a state
130:     val reviewsMap by reviewViewModel.reviews.collectAsState()
131:     // Safely get reviews for this cocktail
132:     val reviews = reviewsMap[cocktailId] ?: emptyList()
133:     val favorites by favoritesViewModel.favorites.collectAsState()
134:     val isFavorite = cocktail?.let { c -> favorites.any { fav -> fav.id == c.id } } ?: false
135: 
136:     // Check if the cocktail is in cart
137:     val cartItems by cartViewModel.cartItems.collectAsState()
138:     val isInCart = cocktail?.let { c -> cartItems.any { it.cocktail.id == c.id } } ?: false
139: 
140:     // Add snackbar state
141:     val snackbarHostState = remember { SnackbarHostState() }
142:     val coroutineScope = rememberCoroutineScope()
143: 
144:     // Update loading state when cocktail data changes
145:     LaunchedEffect(cocktail) {
146:         if (cocktail != null) {
147:             isLoading = false
148:         }
149:     }
150: 
151:     var showReviewDialog by remember { mutableStateOf(false) }
152: 
153:     WriteReviewDialog(
154:         showDialog = showReviewDialog,
155:         onDismiss = { showReviewDialog = false },
156:         onSubmit = { userName, rating, comment ->
157:             try {
158:                 reviewViewModel.createAndAddReview(
159:                     cocktailId = cocktailId,
160:                     userName = userName,
161:                     rating = rating,
162:                     comment = comment
163:                 )
164:                 showReviewDialog = false
165:             } catch (e: Exception) {
166:                 e.printStackTrace()
167:             }
168:         }
169:     )
170: 
171:     Scaffold(
172:         topBar = {
173:             Column {
174:                 TopAppBar(
175:                     title = {
176:                         Text(
177:                             text = cocktail?.name ?: "Cocktail Detail",
178:                             color = Color.White,
179:                             fontSize = 24.sp,
180:                             fontWeight = FontWeight.Bold
181:                         )
182:                     },
183:                     navigationIcon = {
184:                         IconButton(onClick = onBackClick) {
185:                             Icon(
186:                                 imageVector = Icons.Filled.ArrowBack,
187:                                 contentDescription = "Back",
188:                                 tint = Color.White,
189:                                 modifier = Modifier.size(28.dp)
190:                             )
191:                         }
192:                     },
193:                     colors = TopAppBarDefaults.mediumTopAppBarColors(
194:                         containerColor = AppColors.Primary,
195:                         titleContentColor = Color.White,
196:                         navigationIconContentColor = Color.White
197:                     )
198:                 )
199: 
200:                 // Add a divider to create separation between top bar and content
201:                 Divider(
202:                     color = Color.White.copy(alpha = 0.2f),
203:                     thickness = 1.dp
204:                 )
205:             }
206:         },
207:         snackbarHost = { SnackbarHost(snackbarHostState) }
208:     ) { paddingValues ->
209:         // Show loading indicator with animation to prevent flashing
210:         LoadingStateComponent(
211:             isLoading = isLoading,
212:             paddingValues = paddingValues
213:         )
214: 
215:         // Show content only when cocktail is loaded
216:         AnimatedVisibility(
217:             visible = !isLoading && cocktail != null,
218:             enter = fadeIn(),
219:             exit = fadeOut()
220:         ) {
221:             // Only proceed if cocktail is not null
222:             cocktail?.let { cocktailData ->
223:                 val imageUrl = cocktailData.imageUrl ?: ""
224: 
225:                 LazyColumn(
226:                     modifier = Modifier
227:                         .fillMaxSize()
228:                         .padding(paddingValues)
229:                 ) {
230:                     // Cocktail image
231:                     item {
232:                         DetailHeaderImage(
233:                             imageUrl = imageUrl,
234:                             contentDescription = cocktailData.name,
235:                             height = 250,
236:                             targetSize = 800 // Higher resolution for detail view
237:                         )
238:                     }
239: 
240:                     // Cocktail details
241:                     item {
242:                         Card(
243:                             modifier = Modifier
244:                                 .fillMaxWidth()
245:                                 .offset(y = (-20).dp)
246:                                 .padding(horizontal = 16.dp),
247:                             shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
248:                             colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
249:                             elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
250:                         ) {
251:                             Column(
252:                                 modifier = Modifier.padding(20.dp)
253:                             ) {
254:                                 // Price and favorite row
255:                                 Row(
256:                                     modifier = Modifier.fillMaxWidth(),
257:                                     horizontalArrangement = Arrangement.SpaceBetween,
258:                                     verticalAlignment = Alignment.CenterVertically
259:                                 ) {
260:                                     Text(
261:                                         text = "$${String.format("%.2f", cocktailData.price)}",
262:                                         fontSize = 24.sp,
263:                                         fontWeight = FontWeight.Bold,
264:                                         color = AppColors.Primary
265:                                     )
266: 
267:                                     // Favorite button
268:                                     IconButton(
269:                                         onClick = { favoritesViewModel.toggleFavorite(cocktailData) },
270:                                         modifier = Modifier.size(36.dp)
271:                                     ) {
272:                                         Icon(
273:                                             imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
274:                                             contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
275:                                             tint = if (isFavorite) AppColors.Secondary else AppColors.Gray,
276:                                             modifier = Modifier.size(24.dp)
277:                                         )
278:                                     }
279:                                 }
280: 
281:                                 Spacer(modifier = Modifier.height(4.dp))
282: 
283:                                 // Category and alcoholic info subtitle
284:                                 Text(
285:                                     text = buildString {
286:                                         append(cocktailData.alcoholic ?: "Unknown")
287:                                         cocktailData.category?.let {
288:                                             append(" • ")
289:                                             append(it)
290:                                         }
291:                                     },
292:                                     fontSize = 16.sp,
293:                                     color = AppColors.TextSecondary,
294:                                     fontWeight = FontWeight.Medium
295:                                 )
296: 
297:                                 Spacer(modifier = Modifier.height(8.dp))
298: 
299:                                 // Stock status
300:                                 Row(
301:                                     verticalAlignment = Alignment.CenterVertically
302:                                 ) {
303:                                     val inStock = cocktailData.stockCount > 0
304:                                     Box(
305:                                         modifier = Modifier
306:                                             .size(10.dp)
307:                                             .background(
308:                                                 if (inStock) Color(0xFF4CAF50) else Color(0xFFE57373),
309:                                                 CircleShape
310:                                             )
311:                                     )
312: 
313:                                     Spacer(modifier = Modifier.width(8.dp))
314: 
315:                                     Text(
316:                                         text = if (inStock) "In Stock (${cocktailData.stockCount} available)" else "Out of Stock",
317:                                         fontSize = 14.sp,
318:                                         color = if (inStock) Color(0xFF4CAF50) else Color(0xFFE57373),
319:                                         fontWeight = FontWeight.Medium
320:                                     )
321:                                 }
322: 
323:                                 Spacer(modifier = Modifier.height(24.dp))
324: 
325:                                 // Add to cart button
326:                                 Button(
327:                                     onClick = {
328:                                         cocktailData.let {
329:                                             // Only call onAddToCart, which will handle adding to cart
330:                                             onAddToCart(it)
331:                                             // Show snackbar confirmation
332:                                             coroutineScope.launch {
333:                                                 snackbarHostState.showSnackbar(
334:                                                     message = "${cocktailData.name} added to cart",
335:                                                     duration = SnackbarDuration.Short
336:                                                 )
337:                                             }
338:                                         }
339:                                     },
340:                                     modifier = Modifier.fillMaxWidth(),
341:                                     colors = ButtonDefaults.buttonColors(
342:                                         containerColor = AppColors.Primary,
343:                                         disabledContainerColor = AppColors.Gray
344:                                     ),
345:                                     shape = RoundedCornerShape(12.dp),
346:                                     enabled = cocktailData.stockCount > 0
347:                                 ) {
348:                                     Icon(
349:                                         imageVector = Icons.Default.ShoppingCart,
350:                                         contentDescription = null,
351:                                         modifier = Modifier.size(20.dp)
352:                                     )
353: 
354:                                     Spacer(modifier = Modifier.width(8.dp))
355: 
356:                                     Text(
357:                                         text = if (isInCart) "Update Cart" else "Add to Cart",
358:                                         fontSize = 16.sp,
359:                                         fontWeight = FontWeight.SemiBold
360:                                     )
361:                                 }
362: 
363:                                 Spacer(modifier = Modifier.height(24.dp))
364: 
365:                                 // Instructions
366:                                 DetailInfoCard(
367:                                     title = "How to Prepare",
368:                                     modifier = Modifier.padding(vertical = 8.dp),
369:                                     content = {
370:                                     // Add debug print to console to verify instructions are available
371:                                     val instructionsText = cocktailData.instructions ?: ""
372: 
373:                                     if (instructionsText.isNotBlank()) {
374:                                         Text(
375:                                             text = instructionsText,
376:                                             fontSize = 15.sp,
377:                                             color = AppColors.TextPrimary,
378:                                             lineHeight = 24.sp
379:                                         )
380:                                     } else {
381:                                         Column(
382:                                             modifier = Modifier.fillMaxWidth(),
383:                                             horizontalAlignment = Alignment.CenterHorizontally
384:                                         ) {
385:                                             Text(
386:                                                 text = "No instructions available for this cocktail.",
387:                                                 fontSize = 15.sp,
388:                                                 color = AppColors.TextSecondary,
389:                                                 fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
390:                                                 textAlign = TextAlign.Center
391:                                             )
392: 
393:                                             Spacer(modifier = Modifier.height(8.dp))
394: 
395:                                             OutlinedButton(
396:                                                 onClick = {
397:                                                     // Attempt to reload the data
398:                                                     homeViewModel.forceRefreshCocktailDetails(cocktailId)
399:                                                 },
400:                                                 border = BorderStroke(1.dp, AppColors.Primary),
401:                                                 shape = RoundedCornerShape(8.dp)
402:                                             ) {
403:                                                 Text(
404:                                                     text = "Refresh Details",
405:                                                     color = AppColors.Primary
406:                                                 )
407:                                             }
408:                                         }
409:                                     }
410:                                 },
411:                                     backgroundColor = AppColors.Surface.copy(alpha = 0.8f)
412:                                 )
413: 
414:                                 Spacer(modifier = Modifier.height(16.dp))
415:                             }
416:                         }
417:                     }
418: 
419:                     // Ingredients section
420:                     item {
421:                         DetailInfoCard(
422:                             title = "Ingredients",
423:                             modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
424:                             content = {
425: 
426:                                 // Handle different types of ingredients list
427:                                     (cocktailData.ingredients as? List<CocktailIngredient>)?.forEach { ingredient ->
428:                                         Row(
429:                                             modifier = Modifier
430:                                                 .fillMaxWidth()
431:                                                 .padding(vertical = 6.dp),
432:                                             verticalAlignment = Alignment.CenterVertically
433:                                         ) {
434:                                             // Ingredient bullet point
435:                                             Box(
436:                                                 modifier = Modifier
437:                                                     .size(8.dp)
438:                                                     .background(AppColors.Primary, CircleShape)
439:                                             )
440: 
441:                                             Spacer(modifier = Modifier.width(12.dp))
442: 
443:                                             // Ingredient name and measure
444:                                             Text(
445:                                                 text = "${ingredient.name} ${ingredient.measure}",
446:                                                 fontSize = 16.sp,
447:                                                 color = AppColors.TextSecondary
448:                                             )
449:                                         }
450:                                         }
451: 
452:                             },
453:                             elevation = 0
454:                         )
455:                     }
456: 
457:                     // Details chips section
458:                     item {
459:                         DetailInfoCard(
460:                             title = "Details",
461:                             modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
462:                             content = {
463:                             LazyRow(
464:                                 horizontalArrangement = Arrangement.spacedBy(8.dp)
465:                             ) {
466:                                     // Category chip
467:                                     cocktailData.category?.let {
468:                                         item {
469:                                             SuggestionChip(
470:                                                 onClick = {},
471:                                                 label = { Text("Category: $it") },
472:                                                 colors = SuggestionChipDefaults.suggestionChipColors(
473:                                                     containerColor = AppColors.ChipBackground.copy(alpha = 0.2f),
474:                                                     labelColor = AppColors.TextPrimary
475:                                                 )
476:                                             )
477:                                         }
478:                                     }
479: 
480:                                     // Glass type chip
481:                                     cocktailData.glass?.let {
482:                                         item {
483:                                             SuggestionChip(
484:                                                 onClick = {},
485:                                                 label = { Text("Glass: $it") },
486:                                                 colors = SuggestionChipDefaults.suggestionChipColors(
487:                                                     containerColor = AppColors.ChipBackground.copy(alpha = 0.2f),
488:                                                     labelColor = AppColors.TextPrimary
489:                                                 )
490:                                             )
491:                                         }
492:                                     }
493: 
494:                                     // Alcoholic chip
495:                                     item {
496:                                         SuggestionChip(
497:                                             onClick = {},
498:                                             label = { Text(if (cocktailData.alcoholic == "Alcoholic") "Alcoholic" else "Non-Alcoholic") },
499:                                             colors = SuggestionChipDefaults.suggestionChipColors(
500:                                                 containerColor = AppColors.ChipBackground.copy(alpha = 0.2f),
501:                                                 labelColor = AppColors.TextPrimary
502:                                             )
503:                                         )
504:                                     }
505:                                 }
506:                             },
507:                             elevation = 0
508:                         )
509:                     }
510: 
511:                     // Recommendations section - ONLY SHOW WHEN WE HAVE REAL RECOMMENDATIONS
512:                     item {
513:                         // Get recommendations for this cocktail
514:                         var similarCocktails by remember { mutableStateOf<List<Cocktail>>(emptyList()) }
515:                         var isLoadingRecommendations by remember { mutableStateOf(true) }
516: 
517:                         // Load recommendations when the cocktail data is available
518:                         LaunchedEffect(cocktailData) {
519:                             isLoadingRecommendations = true
520:                             try {
521:                                 // Get the category or use a default
522:                                 val category = cocktailData.category ?: "Cocktail"
523:                                 val currentId = cocktailData.id
524: 
525:                                 // Use the repository directly to get recommendations from the API
526:                                 // This bypasses any caching or filtering in the ViewModel
527:                                 val apiRecommendations = homeViewModel.repository.getCocktailsByCategory(category)
528:                                     .filter { it.id != currentId } // Filter out current cocktail
529:                                     .take(3) // Limit to 3 recommendations
530: 
531:                                 // If we got recommendations from the API, use them
532:                                 if (apiRecommendations.isNotEmpty()) {
533:                                     similarCocktails = apiRecommendations
534:                                 } else {
535:                                     // If no recommendations from API, try to get any cocktails from the ViewModel
536:                                     val viewModelRecommendations = homeViewModel.getCocktailsByCategory("Cocktail", 6)
537:                                         .filter { it.id != currentId }
538:                                         .take(3)
539: 
540:                                     similarCocktails = viewModelRecommendations
541:                                 }
542:                             } catch (e: Exception) {
543:                                 // Just log the error, don't show to user
544:                                 println("Failed to load recommendations: ${e.message}")
545:                                 // Use empty list - the UI will handle this
546:                                 similarCocktails = emptyList()
547:                             } finally {
548:                                 isLoadingRecommendations = false
549:                             }
550:                         }
551: 
552:                         // Always show the recommendations section - we'll have fallback data if needed
553:                         // Add a spacer before recommendations
554:                         Spacer(modifier = Modifier.height(16.dp))
555: 
556:                         // Display recommendations with a Card wrapper for consistent styling
557:                         Card(
558:                                 modifier = Modifier
559:                                     .fillMaxWidth()
560:                                     .padding(horizontal = 16.dp, vertical = 8.dp),
561:                                 shape = RoundedCornerShape(16.dp),
562:                                 colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
563:                                 elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
564:                             ) {
565:                                 Column(
566:                                     modifier = Modifier.padding(20.dp)
567:                                 ) {
568:                                     // Section title with category if available
569:                                     val titleText = if (cocktailData.category != null) {
570:                                         "More ${cocktailData.category} Cocktails"
571:                                     } else {
572:                                         "You might also like"
573:                                     }
574: 
575:                                     Text(
576:                                         text = titleText,
577:                                         style = MaterialTheme.typography.titleMedium.copy(
578:                                             fontWeight = FontWeight.Bold,
579:                                             fontSize = 18.sp
580:                                         ),
581:                                         color = AppColors.TextPrimary
582:                                     )
583: 
584:                                     Spacer(modifier = Modifier.height(16.dp))
585: 
586:                                     // Show loading state or recommendations
587:                                     if (isLoadingRecommendations) {
588:                                         // Show loading shimmer
589:                                         LazyRow(
590:                                             contentPadding = PaddingValues(horizontal = 0.dp),
591:                                             horizontalArrangement = Arrangement.spacedBy(12.dp)
592:                                         ) {
593:                                             items(3) {
594:                                                 // Loading shimmer
595:                                                 Box(
596:                                                     modifier = Modifier
597:                                                         .width(140.dp)
598:                                                         .height(200.dp)
599:                                                         .clip(RoundedCornerShape(12.dp))
600:                                                         .shimmerEffect()
601:                                                 )
602:                                             }
603:                                         }
604:                                     } else if (similarCocktails.isEmpty()) {
605:                                         // Show a message if no recommendations
606:                                         Text(
607:                                             text = "Loading recommendations...",
608:                                             style = MaterialTheme.typography.bodyMedium,
609:                                             color = AppColors.TextSecondary,
610:                                             modifier = Modifier.padding(vertical = 16.dp)
611:                                         )
612:                                     } else {
613:                                         // Show recommendations
614:                                         LazyRow(
615:                                             contentPadding = PaddingValues(horizontal = 0.dp),
616:                                             horizontalArrangement = Arrangement.spacedBy(12.dp)
617:                                         ) {
618:                                             items(similarCocktails) { recommendation ->
619:                                                 // Simple recommendation card
620:                                                 Card(
621:                                                 modifier = Modifier
622:                                                     .width(140.dp)
623:                                                     .clickable {
624:                                                         navigationManager.navigateToCocktailDetail(recommendation.id)
625:                                                     },
626:                                                 shape = RoundedCornerShape(12.dp),
627:                                                 colors = CardDefaults.cardColors(
628:                                                     containerColor = AppColors.Surface
629:                                                 ),
630:                                                 elevation = CardDefaults.cardElevation(
631:                                                     defaultElevation = 2.dp
632:                                                 )
633:                                             ) {
634:                                                 Column {
635:                                                     // Cocktail image with fallback
636:                                                     Box(
637:                                                         modifier = Modifier
638:                                                             .height(140.dp)
639:                                                             .fillMaxWidth()
640:                                                             .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
641:                                                             .background(AppColors.LightGray),
642:                                                         contentAlignment = Alignment.Center
643:                                                     ) {
644:                                                         // Show a placeholder icon if image URL is empty
645:                                                         if (recommendation.imageUrl.isNullOrEmpty()) {
646:                                                             Icon(
647:                                                                 imageVector = Icons.Default.LocalBar,
648:                                                                 contentDescription = null,
649:                                                                 modifier = Modifier.size(48.dp),
650:                                                                 tint = AppColors.Gray
651:                                                             )
652:                                                         } else {
653:                                                             // Use our optimized image component
654:                                                             OptimizedImage(
655:                                                                 url = recommendation.imageUrl,
656:                                                                 contentDescription = recommendation.name,
657:                                                                 modifier = Modifier.fillMaxSize(),
658:                                                                 contentScale = ContentScale.Crop,
659:                                                                 targetSize = 200,
660:                                                                 showLoadingIndicator = true
661:                                                             )
662:                                                         }
663:                                                     }
664: 
665:                                                     // Cocktail details
666:                                                     Column(
667:                                                         modifier = Modifier.padding(8.dp)
668:                                                     ) {
669:                                                         // Cocktail name
670:                                                         Text(
671:                                                             text = recommendation.name,
672:                                                             style = MaterialTheme.typography.bodyMedium.copy(
673:                                                                 fontWeight = FontWeight.Bold
674:                                                             ),
675:                                                             color = AppColors.TextPrimary,
676:                                                             maxLines = 1,
677:                                                             overflow = TextOverflow.Ellipsis
678:                                                         )
679: 
680:                                                         Spacer(modifier = Modifier.height(4.dp))
681: 
682:                                                         // Price
683:                                                         Text(
684:                                                             text = "$${String.format("%.2f", recommendation.price)}",
685:                                                             style = MaterialTheme.typography.bodyMedium.copy(
686:                                                                 fontWeight = FontWeight.Bold
687:                                                             ),
688:                                                             color = AppColors.Primary
689:                                                         )
690:                                                     }
691:                                                 }
692:                                             }
693:                                             }
694:                                         }
695:                                     }
696:                                 }
697:                             }
698:                     }
699:                     
700:                     // Reviews section
701:                     item {
702:                         DetailInfoCard(
703:                             title = "",
704:                             modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
705:                             content = {
706:                                 Row(
707:                                     modifier = Modifier.fillMaxWidth(),
708:                                     horizontalArrangement = Arrangement.SpaceBetween,
709:                                     verticalAlignment = Alignment.CenterVertically
710:                                 ) {
711:                                     SectionHeader(
712:                                         title = "Reviews (${reviews.size})",
713:                                         modifier = Modifier.weight(1f),
714:                                         fontSize = 18
715:                                     )
716: 
717:                                     TextButton(
718:                                         onClick = { showReviewDialog = true },
719:                                         colors = ButtonDefaults.textButtonColors(
720:                                             contentColor = AppColors.Primary
721:                                         )
722:                                     ) {
723:                                         Icon(
724:                                             imageVector = Icons.Default.Edit,
725:                                             contentDescription = null,
726:                                             modifier = Modifier.size(16.dp)
727:                                         )
728:                                         Spacer(modifier = Modifier.width(4.dp))
729:                                         Text("Write a Review")
730:                                     }
731:                                 }
732: 
733:                                 Spacer(modifier = Modifier.height(12.dp))
734: 
735:                                 if (reviews.isEmpty()) {
736:                                     Box(
737:                                         modifier = Modifier
738:                                             .fillMaxWidth()
739:                                             .padding(vertical = 24.dp),
740:                                         contentAlignment = Alignment.Center
741:                                     ) {
742:                                         Text(
743:                                             text = "Be the first to review this cocktail",
744:                                             color = AppColors.Gray,
745:                                             fontSize = 16.sp
746:                                         )
747:                                     }
748:                                 } else {
749:                                     reviews.take(3).forEachIndexed { index, review ->
750:                                         CocktailReviewItem(review = review)
751: 
752:                                         if (index < reviews.take(3).size - 1) {
753:                                             Divider(
754:                                                 modifier = Modifier.padding(vertical = 12.dp),
755:                                                 color = AppColors.LightGray.copy(alpha = 0.5f)
756:                                             )
757:                                         }
758:                                     }
759:                                 }
760:                             },
761:                             elevation = 0
762:                         )
763:                     }
764: 
765:                     // Bottom padding
766:                     item {
767:                         Spacer(modifier = Modifier.height(24.dp))
768:                     }
769:                 }
770:             }
771:         }
772:     }
773: }
774: 
775: /**
776:  * Extension function to create a shimmer loading effect
777:  */
778: @Composable
779: fun Modifier.shimmerEffect(): Modifier {
780:     val transition = rememberInfiniteTransition(label = "shimmer")
781:     val alpha = transition.animateFloat(
782:         initialValue = 0.2f,
783:         targetValue = 0.9f,
784:         animationSpec = infiniteRepeatable(
785:             animation = tween(durationMillis = 1000),
786:             repeatMode = RepeatMode.Reverse
787:         ),
788:         label = "shimmer alpha"
789:     ).value
790: 
791:     return this.then(
792:         Modifier.background(
793:             brush = Brush.linearGradient(
794:                 colors = listOf(
795:                     Color.LightGray.copy(alpha = alpha),
796:                     Color.LightGray.copy(alpha = 0.3f),
797:                     Color.LightGray.copy(alpha = alpha)
798:                 )
799:             )
800:         )
801:     )
802: }
803: 
804: @Composable
805: fun CocktailReviewItem(review: Review) {
806:     Column(
807:         modifier = Modifier.fillMaxWidth()
808:     ) {
809:         Row(
810:             verticalAlignment = Alignment.CenterVertically
811:         ) {
812:             // User initial in circle - safely handle potential null/empty username
813:             Box(
814:                 modifier = Modifier
815:                     .size(40.dp)
816:                     .background(AppColors.Primary.copy(alpha = 0.2f), CircleShape),
817:                 contentAlignment = Alignment.Center
818:             ) {
819:                 Text(
820:                     text = (review.userName.takeIf { it.isNotBlank() } ?: "A").take(1).uppercase(),
821:                     color = AppColors.Primary,
822:                     fontWeight = FontWeight.Bold
823:                 )
824:             }
825: 
826:             Spacer(modifier = Modifier.width(12.dp))
827: 
828:             Column {
829:                 Text(
830:                     text = review.userName.takeIf { it.isNotBlank() } ?: "Anonymous",
831:                     fontWeight = FontWeight.SemiBold,
832:                     color = AppColors.TextPrimary,
833:                     fontSize = 16.sp
834:                 )
835: 
836:                 Row(
837:                     verticalAlignment = Alignment.CenterVertically
838:                 ) {
839:                     RatingBar(
840:                         rating = review.rating.coerceIn(0f, 5f),
841:                         modifier = Modifier.padding(end = 8.dp, top = 4.dp),
842:                         stars = 5,
843:                         starsColor = AppColors.Secondary
844:                     )
845: 
846:                     Text(
847:                         text = review.date.takeIf { it.isNotBlank() } ?: "Unknown date",
848:                         fontSize = 12.sp,
849:                         color = AppColors.Gray
850:                     )
851:                 }
852:             }
853:         }
854: 
855:         if (review.comment.isNotBlank()) {
856:             Text(
857:                 text = review.comment,
858:                 modifier = Modifier.padding(start = 52.dp, top = 8.dp),
859:                 color = AppColors.TextPrimary,
860:                 fontSize = 14.sp,
861:                 lineHeight = 20.sp
862:             )
863:         }
864:     }
865: }
````

## File: androidApp/src/main/java/com/cocktailcraft/screens/FavoritesScreen.kt
````kotlin
  1: package com.cocktailcraft.screens
  2: 
  3: import androidx.compose.foundation.background
  4: import androidx.compose.foundation.layout.Arrangement
  5: import androidx.compose.foundation.layout.Column
  6: import androidx.compose.foundation.layout.PaddingValues
  7: import androidx.compose.foundation.layout.fillMaxSize
  8: import androidx.compose.foundation.layout.padding
  9: import androidx.compose.foundation.lazy.LazyColumn
 10: import androidx.compose.foundation.lazy.itemsIndexed
 11: import androidx.compose.material.icons.Icons
 12: import androidx.compose.material.icons.filled.Favorite
 13: import androidx.compose.runtime.Composable
 14: import androidx.compose.runtime.collectAsState
 15: import androidx.compose.runtime.getValue
 16: import androidx.compose.ui.Modifier
 17: import androidx.compose.ui.unit.dp
 18: import com.cocktailcraft.domain.model.Cocktail
 19: import com.cocktailcraft.ui.components.CocktailItem
 20: import com.cocktailcraft.ui.components.EmptyStateComponent
 21: import com.cocktailcraft.ui.components.LoadingStateComponent
 22: import com.cocktailcraft.ui.components.SectionHeader
 23: import com.cocktailcraft.ui.theme.AppColors
 24: import com.cocktailcraft.viewmodel.CartViewModel
 25: import com.cocktailcraft.viewmodel.FavoritesViewModel
 26: 
 27: @Composable
 28: fun FavoritesScreen(
 29:     cartViewModel: CartViewModel,
 30:     favoritesViewModel: FavoritesViewModel,
 31:     onBrowseProducts: () -> Unit,
 32:     onAddToCart: (Cocktail) -> Unit
 33: ) {
 34:     val favorites by favoritesViewModel.favorites.collectAsState()
 35:     val isLoading by favoritesViewModel.isLoading.collectAsState()
 36:     val error by favoritesViewModel.error.collectAsState()
 37: 
 38:     Column(
 39:         modifier = Modifier
 40:             .fillMaxSize()
 41:             .background(AppColors.Background)
 42:     ) {
 43:         // Show loading state
 44:         LoadingStateComponent(isLoading = isLoading)
 45: 
 46:         // Show error state
 47:         if (!isLoading && error?.isNotEmpty() == true) {
 48:             EmptyStateComponent(
 49:                 title = "Error",
 50:                 message = error ?: "An unknown error occurred",
 51:                 actionButtonText = "Try Again",
 52:                 onActionButtonClick = { /* Add retry logic here */ }
 53:             )
 54:         }
 55:         // Show empty favorites state
 56:         else if (!isLoading && favorites.isEmpty()) {
 57:             EmptyStateComponent(
 58:                 title = "No favorites yet",
 59:                 message = "Add cocktails to your favorites to see them here",
 60:                 actionButtonText = "Browse Cocktails",
 61:                 onActionButtonClick = onBrowseProducts,
 62:                 icon = Icons.Filled.Favorite
 63:             )
 64:         }
 65:         // Show favorites list
 66:         else if (!isLoading) {
 67:             // Favorites list
 68:             LazyColumn(
 69:                 modifier = Modifier
 70:                     .fillMaxSize()
 71:                     .padding(horizontal = 16.dp),
 72:                 contentPadding = PaddingValues(vertical = 16.dp),
 73:                 verticalArrangement = Arrangement.spacedBy(16.dp)
 74:             ) {
 75:                 item {
 76:                     SectionHeader(
 77:                         title = "Your Favorite Cocktails",
 78:                         fontSize = 20,
 79:                         modifier = Modifier.padding(bottom = 8.dp)
 80:                     )
 81:                 }
 82: 
 83:                 itemsIndexed(favorites) { _, cocktail ->
 84:                     CocktailItem(
 85:                         cocktail = cocktail,
 86:                         onClick = { /* Navigate to detail */ },
 87:                         onAddToCart = { _ ->
 88:                             cartViewModel.addToCart(cocktail)
 89:                             onAddToCart(cocktail)
 90:                         },
 91:                         isFavorite = true,
 92:                         onToggleFavorite = { _ ->
 93:                             favoritesViewModel.toggleFavorite(cocktail)
 94:                         }
 95:                     )
 96:                 }
 97:             }
 98:         }
 99:     }
100: }
````

## File: androidApp/src/main/java/com/cocktailcraft/screens/HomeScreen.kt
````kotlin
  1: package com.cocktailcraft.screens
  2: 
  3: import androidx.compose.animation.AnimatedVisibility
  4: import androidx.compose.animation.expandVertically
  5: import androidx.compose.animation.fadeIn
  6: import androidx.compose.animation.fadeOut
  7: import androidx.compose.animation.scaleIn
  8: import androidx.compose.animation.scaleOut
  9: import androidx.compose.animation.shrinkVertically
 10: import androidx.compose.animation.slideInVertically
 11: import androidx.compose.animation.core.FastOutSlowInEasing
 12: import androidx.compose.animation.core.animateFloatAsState
 13: import androidx.compose.animation.core.tween
 14: import androidx.compose.foundation.background
 15: import androidx.compose.foundation.clickable
 16: import androidx.compose.foundation.layout.Arrangement
 17: import androidx.compose.foundation.layout.Box
 18: import androidx.compose.foundation.layout.Column
 19: import androidx.compose.foundation.layout.PaddingValues
 20: import androidx.compose.foundation.layout.Row
 21: import androidx.compose.foundation.layout.Spacer
 22: import androidx.compose.foundation.layout.fillMaxSize
 23: import androidx.compose.foundation.layout.fillMaxWidth
 24: import androidx.compose.foundation.layout.height
 25: import androidx.compose.foundation.layout.offset
 26: import androidx.compose.foundation.layout.padding
 27: import androidx.compose.foundation.layout.size
 28: import androidx.compose.foundation.layout.width
 29: import androidx.compose.foundation.lazy.LazyColumn
 30: import androidx.compose.foundation.lazy.LazyRow
 31: import androidx.compose.foundation.lazy.items
 32: import androidx.compose.foundation.lazy.itemsIndexed
 33: import androidx.compose.foundation.lazy.rememberLazyListState
 34: import androidx.compose.foundation.shape.RoundedCornerShape
 35: import androidx.compose.material.ExperimentalMaterialApi
 36: import androidx.compose.material.icons.Icons
 37: import androidx.compose.material.icons.filled.AirplanemodeActive
 38: import androidx.compose.material.icons.filled.Close
 39: import androidx.compose.material.icons.filled.Error
 40: import androidx.compose.material.icons.filled.FilterAlt
 41: import androidx.compose.material.icons.filled.Search
 42: import androidx.compose.material.icons.filled.Warning
 43: import androidx.compose.material.icons.filled.Wifi
 44: import androidx.compose.material.icons.filled.WifiOff
 45: import androidx.compose.material.pullrefresh.PullRefreshIndicator
 46: import androidx.compose.material.pullrefresh.pullRefresh
 47: import androidx.compose.material.pullrefresh.rememberPullRefreshState
 48: import androidx.compose.material3.Button
 49: import androidx.compose.material3.ButtonDefaults
 50: import androidx.compose.material3.CircularProgressIndicator
 51: import androidx.compose.material3.Icon
 52: import androidx.compose.material3.IconButton
 53: import androidx.compose.material3.OutlinedTextField
 54: import androidx.compose.material3.Text
 55: import androidx.compose.material3.TextFieldDefaults
 56: import androidx.compose.runtime.Composable
 57: import androidx.compose.runtime.LaunchedEffect
 58: import androidx.compose.runtime.collectAsState
 59: import androidx.compose.runtime.derivedStateOf
 60: import androidx.compose.runtime.getValue
 61: import androidx.compose.runtime.mutableStateOf
 62: import androidx.compose.runtime.remember
 63: import androidx.compose.runtime.saveable.rememberSaveable
 64: import androidx.compose.runtime.setValue
 65: import androidx.compose.ui.Alignment
 66: import androidx.compose.ui.Modifier
 67: import androidx.compose.ui.draw.alpha
 68: import androidx.compose.ui.draw.clip
 69: import androidx.compose.ui.graphics.Color
 70: import androidx.compose.ui.graphics.vector.ImageVector
 71: import androidx.compose.ui.text.font.FontWeight
 72: import androidx.compose.ui.text.style.TextAlign
 73: import androidx.compose.ui.unit.dp
 74: import androidx.compose.ui.unit.sp
 75: import androidx.navigation.NavController
 76: import kotlin.math.abs
 77: import kotlinx.coroutines.delay
 78: import com.cocktailcraft.navigation.Screen
 79: import com.cocktailcraft.domain.model.Cocktail
 80: import com.cocktailcraft.ui.components.AdvancedSearchPanel
 81: import com.cocktailcraft.ui.components.AnimatedCocktailItem
 82: import com.cocktailcraft.ui.components.AnimatedCocktailList
 83: import com.cocktailcraft.ui.components.CategoryFilterRow
 84: import com.cocktailcraft.ui.components.CocktailItem
 85: import com.cocktailcraft.ui.components.CocktailItemShimmer
 86: import com.cocktailcraft.ui.components.CocktailLoadingShimmer
 87: import com.cocktailcraft.ui.components.CocktailSearchBar
 88: import com.cocktailcraft.ui.components.EmptySearchResultsMessage
 89: import com.cocktailcraft.ui.components.ErrorBanner
 90: import com.cocktailcraft.ui.components.ErrorDialog
 91: import com.cocktailcraft.ui.components.ExpandableAdvancedSearchPanel
 92: import com.cocktailcraft.ui.components.FilterChip
 93: import com.cocktailcraft.ui.components.NetworkErrorStateDisplay
 94: import com.cocktailcraft.ui.components.SearchFilterChips
 95: import com.cocktailcraft.ui.components.shimmerEffect
 96: import com.cocktailcraft.util.FilterOptionsLoader
 97: import com.cocktailcraft.ui.theme.AppColors
 98: import com.cocktailcraft.util.ErrorUtils
 99: import com.cocktailcraft.util.ListOptimizations.OnBottomReached
100: import com.cocktailcraft.util.ListOptimizations.OnScrollPastThreshold
101: import com.cocktailcraft.util.ListOptimizations.itemKey
102: import com.cocktailcraft.viewmodel.CartViewModel
103: import com.cocktailcraft.viewmodel.FavoritesViewModel
104: import com.cocktailcraft.viewmodel.HomeViewModel
105: import com.cocktailcraft.util.CocktailDebugLogger
106: 
107: @OptIn(ExperimentalMaterialApi::class)
108: @Composable
109: fun HomeScreen(
110:     viewModel: HomeViewModel,
111:     favoritesViewModel: FavoritesViewModel,
112:     onAddToCart: (Cocktail) -> Unit,
113:     onCocktailClick: (Cocktail) -> Unit
114: ) {
115:     val cocktails by viewModel.cocktails.collectAsState()
116:     val isLoading by viewModel.isLoading.collectAsState()
117:     val isLoadingMore by viewModel.isLoadingMore.collectAsState()
118:     val hasMoreData by viewModel.hasMoreData.collectAsState()
119:     val legacyErrorString by viewModel.errorString.collectAsState()
120:     val isSearchActive by viewModel.isSearchActive.collectAsState()
121:     val searchQuery by viewModel.searchQuery.collectAsState()
122:     val favorites by favoritesViewModel.favorites.collectAsState()
123:     val isOfflineMode by viewModel.isOfflineMode.collectAsState()
124:     val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
125:     val searchFilters by viewModel.searchFilters.collectAsState()
126:     val isAdvancedSearchActive by viewModel.isAdvancedSearchActive.collectAsState()
127: 
128:     // State for advanced search panel
129:     var showAdvancedSearch by remember { mutableStateOf(false) }
130: 
131:     // Add state for selected category - use rememberSaveable to persist across navigation
132:     // Default to "Cocktail" to match lazy loading behavior
133:     var selectedCategory by rememberSaveable { mutableStateOf<String?>("Cocktail") }
134: 
135:     // Function to handle category selection
136:     val onCategorySelected: (String?) -> Unit = { category ->
137:         selectedCategory = category
138:     }
139: 
140:     // Add pull-to-refresh state
141:     val pullRefreshState = rememberPullRefreshState(
142:         refreshing = isLoading,
143:         onRefresh = { viewModel.retry() }
144:     )
145: 
146:     // Track if this is the first composition
147:     var isFirstComposition by rememberSaveable { mutableStateOf(true) }
148:     
149:     // Effect to load cocktails by category when selected category changes
150:     LaunchedEffect(selectedCategory) {
151:         CocktailDebugLogger.log("🏠 HomeScreen LaunchedEffect(selectedCategory): category=$selectedCategory, isSearchActive=$isSearchActive, isFirst=$isFirstComposition")
152:         
153:         // Skip loading on first composition if we already have data
154:         if (isFirstComposition && cocktails.isNotEmpty()) {
155:             CocktailDebugLogger.log("   ✅ Skipping load on first composition - already have ${cocktails.size} cocktails")
156:             isFirstComposition = false
157:             return@LaunchedEffect
158:         }
159:         
160:         isFirstComposition = false
161:         
162:         // Only load if not in search mode and category has changed
163:         if (!isSearchActive) {
164:             CocktailDebugLogger.log("   🏷️ Loading cocktails for category: $selectedCategory")
165:             viewModel.loadCocktailsByCategory(selectedCategory)
166:         }
167:     }
168:     
169:     // Effect to clear errors when screen is displayed with data
170:     LaunchedEffect(cocktails) {
171:         CocktailDebugLogger.log("🏠 HomeScreen LaunchedEffect(cocktails): count=${cocktails.size}, error='$legacyErrorString'")
172:         if (cocktails.isNotEmpty() && legacyErrorString.isNotBlank()) {
173:             viewModel.clearLegacyError()
174:         }
175:     }
176: 
177:     Column(
178:         modifier = Modifier
179:             .fillMaxSize()
180:             .background(AppColors.Background)
181:     ) {
182:         // For now, we'll skip the error banner since we're using legacy error handling
183:         // We'll implement it in the future when we fully migrate to the new error system
184: 
185:         // Search Bar with Advanced Search Button
186:         CocktailSearchBar(
187:             searchQuery = searchQuery,
188:             isAdvancedSearchActive = isAdvancedSearchActive,
189:             hasActiveFilters = searchFilters.hasActiveFilters(),
190:             onSearchQueryChange = { viewModel.searchCocktails(it) },
191:             onClearSearch = { viewModel.toggleSearchMode(false) },
192:             onToggleAdvancedSearch = { viewModel.toggleAdvancedSearchMode(!isAdvancedSearchActive) },
193:             onShowAdvancedSearchDialog = { showAdvancedSearch = true }
194:         )
195: 
196:         // Active filters display
197:         SearchFilterChips(
198:             filters = searchFilters,
199:             onClearFilter = { filterType ->
200:                 // Create a copy of current filters with the specified filter cleared
201:                 val updatedFilters = when (filterType) {
202:                     "category" -> searchFilters.copy(category = null)
203:                     "ingredient" -> searchFilters.copy(ingredient = null)
204:                     "ingredients" -> searchFilters.copy(ingredients = emptyList())
205:                     "excludeIngredients" -> searchFilters.copy(excludeIngredients = emptyList())
206:                     "alcoholic" -> searchFilters.copy(alcoholic = null)
207:                     "glass" -> searchFilters.copy(glass = null)
208:                     "priceRange" -> searchFilters.copy(priceRange = null)
209:                     "tasteProfile" -> searchFilters.copy(tasteProfile = null)
210:                     "complexity" -> searchFilters.copy(complexity = null)
211:                     "preparationTime" -> searchFilters.copy(preparationTime = null)
212:                     else -> searchFilters
213:                 }
214:                 viewModel.updateSearchFilters(updatedFilters)
215:             },
216:             onClearAllFilters = {
217:                 viewModel.clearSearchFilters()
218:             }
219:         )
220: 
221:         // Advanced search panel
222: 
223:         // Load filter options using the utility
224:         val filterOptions = FilterOptionsLoader.rememberFilterOptions(repository = viewModel.repository)
225:         val categories = filterOptions.categories
226:         val ingredients = filterOptions.ingredients
227:         val glasses = filterOptions.glasses
228: 
229:         // Use the dialog version when in dialog mode
230:         if (showAdvancedSearch) {
231:             AdvancedSearchPanel(
232:                 isVisible = true,
233:                 currentFilters = searchFilters,
234:                 categories = categories,
235:                 ingredients = ingredients,
236:                 glasses = glasses,
237:                 onApplyFilters = { filters ->
238:                     viewModel.updateSearchFilters(filters)
239:                     showAdvancedSearch = false
240:                 },
241:                 onClearFilters = {
242:                     viewModel.clearSearchFilters()
243:                 },
244:                 onDismiss = {
245:                     showAdvancedSearch = false
246:                 }
247:             )
248:         }
249: 
250:         // Use the expandable panel version for inline display
251:         ExpandableAdvancedSearchPanel(
252:             isExpanded = isAdvancedSearchActive,
253:             currentFilters = searchFilters,
254:             categories = categories,
255:             ingredients = ingredients,
256:             glasses = glasses,
257:             onApplyFilters = { filters ->
258:                 viewModel.updateSearchFilters(filters)
259:             },
260:             onClearFilters = {
261:                 viewModel.clearSearchFilters()
262:             }
263:         )
264: 
265:         // Add Category Filter Chips - only shown when not searching
266:         if (!isSearchActive) {
267:             CategoryFilterRow(
268:                 categories = categories,
269:                 selectedCategory = selectedCategory,
270:                 onCategorySelected = onCategorySelected
271:             )
272:         }
273: 
274:         Spacer(modifier = Modifier.height(8.dp))
275: 
276:         // Main content wrapped in pull-to-refresh
277:         Box(
278:             modifier = Modifier
279:                 .fillMaxSize()
280:                 .pullRefresh(pullRefreshState)
281:         ) {
282:             if (isLoading && cocktails.isEmpty()) {
283:                 // Show shimmer loading effect instead of spinner
284:                 CocktailLoadingShimmer()
285:             } else if (legacyErrorString.isNotBlank()) {
286:                 NetworkErrorStateDisplay(
287:                     errorMessage = legacyErrorString,
288:                     isOfflineMode = isOfflineMode,
289:                     isNetworkAvailable = isNetworkAvailable,
290:                     hasContent = cocktails.isNotEmpty(),
291:                     onRetry = { viewModel.retry() },
292:                     onEnableOfflineMode = {
293:                         viewModel.setOfflineMode(true)
294:                         viewModel.retry()
295:                     },
296:                     onGoOnline = {
297:                         viewModel.setOfflineMode(false)
298:                         viewModel.retry()
299:                     }
300:                 )
301:             } else if (cocktails.isEmpty()) {
302:                 // Enhanced empty state message with animations
303:                 EmptySearchResultsMessage(
304:                     searchQuery = searchQuery,
305:                     selectedCategory = selectedCategory,
306:                     onClearSearch = {
307:                         viewModel.toggleSearchMode(false)
308:                     },
309:                     onClearCategory = {
310:                         onCategorySelected(null)
311:                     }
312:                 )
313:             } else {
314:                 // Use the reusable animated cocktail list component
315:                 AnimatedCocktailList(
316:                     cocktails = cocktails,
317:                     isSearchActive = isSearchActive,
318:                     selectedCategory = selectedCategory,
319:                     isLoadingMore = isLoadingMore,
320:                     hasMoreData = hasMoreData,
321:                     favorites = favorites,
322:                     onCocktailClick = onCocktailClick,
323:                     onAddToCart = onAddToCart,
324:                     onToggleFavorite = { cocktailToToggle ->
325:                         favoritesViewModel.toggleFavorite(cocktailToToggle)
326:                     },
327:                     onLoadMore = {
328:                         viewModel.loadMoreCocktails()
329:                     }
330:                 )
331:             }
332: 
333:             // Pull refresh indicator
334:             PullRefreshIndicator(
335:                 refreshing = isLoading,
336:                 state = pullRefreshState,
337:                 modifier = Modifier.align(Alignment.TopCenter),
338:                 backgroundColor = Color.White,
339:                 contentColor = AppColors.Primary
340:             )
341:         }
342:     }
343: }
````

## File: androidApp/src/main/java/com/cocktailcraft/screens/OfflineModeScreen.kt
````kotlin
  1: package com.cocktailcraft.screens
  2: 
  3: import androidx.compose.foundation.background
  4: import androidx.compose.foundation.layout.Arrangement
  5: import androidx.compose.foundation.layout.Box
  6: import androidx.compose.foundation.layout.Column
  7: import androidx.compose.foundation.layout.PaddingValues
  8: import androidx.compose.foundation.layout.Row
  9: import androidx.compose.foundation.layout.Spacer
 10: import androidx.compose.foundation.layout.fillMaxSize
 11: import androidx.compose.foundation.layout.fillMaxWidth
 12: import androidx.compose.foundation.layout.height
 13: import androidx.compose.foundation.layout.padding
 14: import androidx.compose.foundation.layout.size
 15: import androidx.compose.foundation.layout.width
 16: import androidx.compose.foundation.lazy.LazyColumn
 17: import androidx.compose.foundation.lazy.items
 18: import androidx.compose.foundation.shape.RoundedCornerShape
 19: import androidx.compose.material.icons.Icons
 20: import androidx.compose.material.icons.filled.ArrowBack
 21: import androidx.compose.material.icons.filled.AirplanemodeActive
 22: import androidx.compose.material.icons.filled.Delete
 23: import androidx.compose.material.icons.filled.History
 24: import androidx.compose.material.icons.filled.Home
 25: import androidx.compose.material.icons.filled.Info
 26: import androidx.compose.material.icons.filled.Storage
 27: import androidx.compose.material.icons.filled.Wifi
 28: import androidx.compose.material.icons.filled.WifiOff
 29: import androidx.compose.material3.AlertDialog
 30: import androidx.compose.material3.Button
 31: import androidx.compose.material3.ButtonDefaults
 32: import androidx.compose.material3.Card
 33: import androidx.compose.material3.CardDefaults
 34: import androidx.compose.material3.Divider
 35: import androidx.compose.material3.ExperimentalMaterial3Api
 36: import androidx.compose.material3.Icon
 37: import androidx.compose.material3.IconButton
 38: import androidx.compose.material3.MaterialTheme
 39: import androidx.compose.material3.Scaffold
 40: import androidx.compose.material3.Switch
 41: import androidx.compose.material3.SwitchDefaults
 42: import androidx.compose.material3.Text
 43: import androidx.compose.material3.TextButton
 44: import androidx.compose.material3.TopAppBar
 45: import androidx.compose.material3.TopAppBarDefaults
 46: import androidx.compose.runtime.Composable
 47: import androidx.compose.runtime.DisposableEffect
 48: import androidx.compose.runtime.SideEffect
 49: import androidx.compose.runtime.collectAsState
 50: import androidx.compose.runtime.getValue
 51: import androidx.compose.runtime.mutableStateOf
 52: import androidx.compose.runtime.remember
 53: import androidx.compose.runtime.setValue
 54: import androidx.compose.ui.Alignment
 55: import androidx.compose.ui.Modifier
 56: import androidx.compose.ui.graphics.Color
 57: import androidx.compose.ui.text.font.FontWeight
 58: import androidx.compose.ui.unit.dp
 59: import androidx.compose.ui.unit.sp
 60: import com.cocktailcraft.domain.model.Cocktail
 61: import com.cocktailcraft.ui.components.CocktailItem
 62: import com.cocktailcraft.ui.theme.AppColors
 63: import com.cocktailcraft.viewmodel.OfflineModeViewModel
 64: import com.google.accompanist.systemuicontroller.rememberSystemUiController
 65: 
 66: @OptIn(ExperimentalMaterial3Api::class)
 67: @Composable
 68: fun OfflineModeScreen(
 69:     viewModel: OfflineModeViewModel,
 70:     onBackClick: () -> Unit,
 71:     onCocktailClick: (Cocktail) -> Unit
 72: ) {
 73:     val isOfflineModeEnabled by viewModel.isOfflineModeEnabled.collectAsState()
 74:     val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
 75:     val recentlyViewedCocktails by viewModel.recentlyViewedCocktails.collectAsState()
 76: 
 77:     // Hide the system navigation bar
 78:     val systemUiController = rememberSystemUiController()
 79:     SideEffect {
 80:         // Hide the navigation bar and make it immersive
 81:         systemUiController.isNavigationBarVisible = false
 82:     }
 83: 
 84:     // Restore the system navigation bar when leaving the screen
 85:     DisposableEffect(Unit) {
 86:         onDispose {
 87:             systemUiController.isNavigationBarVisible = true
 88:         }
 89:     }
 90: 
 91:     var showClearCacheDialog by remember { mutableStateOf(false) }
 92: 
 93:     if (showClearCacheDialog) {
 94:         AlertDialog(
 95:             onDismissRequest = { showClearCacheDialog = false },
 96:             title = { Text("Clear Offline Cache") },
 97:             text = { Text("Are you sure you want to clear all cached cocktails? You won't be able to view them offline.") },
 98:             confirmButton = {
 99:                 TextButton(
100:                     onClick = {
101:                         viewModel.clearCache()
102:                         showClearCacheDialog = false
103:                     }
104:                 ) {
105:                     Text("Clear Cache")
106:                 }
107:             },
108:             dismissButton = {
109:                 TextButton(
110:                     onClick = { showClearCacheDialog = false }
111:                 ) {
112:                     Text("Cancel")
113:                 }
114:             }
115:         )
116:     }
117: 
118:     Scaffold(
119:         // Remove the topBar completely as it's already handled in MainScreen.kt
120:     ) { paddingValues ->
121:         Column(
122:             modifier = Modifier
123:                 .fillMaxSize()
124:                 .padding(paddingValues)
125:                 .background(AppColors.Background)
126:         ) {
127:             // Network status card
128:             Card(
129:                 modifier = Modifier
130:                     .fillMaxWidth()
131:                     .padding(horizontal = 16.dp, vertical = 8.dp),
132:                 colors = CardDefaults.cardColors(
133:                     containerColor = if (isNetworkAvailable) Color(0xFF4CAF50) else Color(0xFFE57373)
134:                 ),
135:                 shape = RoundedCornerShape(12.dp),
136:                 elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
137:             ) {
138:                 Row(
139:                     modifier = Modifier
140:                         .fillMaxWidth()
141:                         .padding(vertical = 16.dp, horizontal = 16.dp),
142:                     verticalAlignment = Alignment.CenterVertically,
143:                     horizontalArrangement = Arrangement.Center
144:                 ) {
145:                     Icon(
146:                         imageVector = if (isNetworkAvailable)
147:                             Icons.Default.Wifi
148:                         else
149:                             Icons.Default.WifiOff,
150:                         contentDescription = if (isNetworkAvailable) "Online" else "Offline",
151:                         tint = Color.White,
152:                         modifier = Modifier.size(28.dp)
153:                     )
154: 
155:                     Spacer(modifier = Modifier.width(12.dp))
156: 
157:                     Text(
158:                         text = if (isNetworkAvailable) "Network Available" else "Network Unavailable",
159:                         color = Color.White,
160:                         fontWeight = FontWeight.Bold,
161:                         fontSize = 18.sp
162:                     )
163:                 }
164:             }
165: 
166:             LazyColumn(
167:                 modifier = Modifier.fillMaxSize(),
168:                 contentPadding = PaddingValues(
169:                     start = 16.dp,
170:                     end = 16.dp,
171:                     top = 16.dp,
172:                     bottom = 80.dp  // Add extra padding at the bottom to avoid system navigation bar
173:                 ),
174:                 verticalArrangement = Arrangement.spacedBy(16.dp)
175:             ) {
176:                 // Offline mode toggle
177:                 item {
178:                     Card(
179:                         modifier = Modifier.fillMaxWidth(),
180:                         colors = CardDefaults.cardColors(
181:                             containerColor = AppColors.Surface
182:                         ),
183:                         shape = RoundedCornerShape(12.dp),
184:                         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
185:                     ) {
186:                         Column(
187:                             modifier = Modifier.padding(16.dp)
188:                         ) {
189:                             Row(
190:                                 modifier = Modifier.fillMaxWidth(),
191:                                 verticalAlignment = Alignment.CenterVertically,
192:                                 horizontalArrangement = Arrangement.SpaceBetween
193:                             ) {
194:                                 Row(
195:                                     verticalAlignment = Alignment.CenterVertically
196:                                 ) {
197:                                     Icon(
198:                                         imageVector = Icons.Default.AirplanemodeActive,
199:                                         contentDescription = "Offline Mode",
200:                                         tint = if (isOfflineModeEnabled) AppColors.Primary else AppColors.Gray,
201:                                         modifier = Modifier.size(24.dp)
202:                                     )
203: 
204:                                     Text(
205:                                         text = "Offline Mode",
206:                                         fontSize = 18.sp,
207:                                         fontWeight = FontWeight.Bold,
208:                                         modifier = Modifier.padding(start = 16.dp)
209:                                     )
210:                                 }
211: 
212:                                 Switch(
213:                                     checked = isOfflineModeEnabled,
214:                                     onCheckedChange = { viewModel.toggleOfflineMode() },
215:                                     thumbContent = if (isOfflineModeEnabled) {
216:                                         {
217:                                             Icon(
218:                                                 imageVector = Icons.Default.AirplanemodeActive,
219:                                                 contentDescription = null,
220:                                                 modifier = Modifier.size(SwitchDefaults.IconSize)
221:                                             )
222:                                         }
223:                                     } else null
224:                                 )
225:                             }
226: 
227:                             Spacer(modifier = Modifier.height(8.dp))
228: 
229:                             Text(
230:                                 text = "When enabled, the app will only use cached data and won't make network requests.",
231:                                 fontSize = 14.sp,
232:                                 color = AppColors.TextSecondary
233:                             )
234:                         }
235:                     }
236:                 }
237: 
238:                 // Cache info
239:                 item {
240:                     Card(
241:                         modifier = Modifier.fillMaxWidth(),
242:                         colors = CardDefaults.cardColors(
243:                             containerColor = AppColors.Surface
244:                         ),
245:                         shape = RoundedCornerShape(12.dp),
246:                         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
247:                     ) {
248:                         Column(
249:                             modifier = Modifier.padding(16.dp)
250:                         ) {
251:                             Row(
252:                                 verticalAlignment = Alignment.CenterVertically
253:                             ) {
254:                                 Icon(
255:                                     imageVector = Icons.Default.Storage,
256:                                     contentDescription = "Cache Info",
257:                                     tint = AppColors.Primary,
258:                                     modifier = Modifier.size(24.dp)
259:                                 )
260: 
261:                                 Text(
262:                                     text = "Cached Cocktails",
263:                                     fontSize = 18.sp,
264:                                     fontWeight = FontWeight.Bold,
265:                                     modifier = Modifier.padding(start = 16.dp)
266:                                 )
267:                             }
268: 
269:                             Spacer(modifier = Modifier.height(16.dp))
270: 
271:                             Row(
272:                                 modifier = Modifier.fillMaxWidth(),
273:                                 horizontalArrangement = Arrangement.SpaceBetween
274:                             ) {
275:                                 Text(
276:                                     text = "Cocktails available offline:",
277:                                     fontSize = 14.sp,
278:                                     color = AppColors.TextSecondary
279:                                 )
280: 
281:                                 Text(
282:                                     text = "${recentlyViewedCocktails.size}",
283:                                     fontSize = 14.sp,
284:                                     fontWeight = FontWeight.Bold,
285:                                     color = AppColors.TextPrimary
286:                                 )
287:                             }
288: 
289:                             Spacer(modifier = Modifier.height(16.dp))
290: 
291:                             Button(
292:                                 onClick = { showClearCacheDialog = true },
293:                                 modifier = Modifier.fillMaxWidth(),
294:                                 colors = ButtonDefaults.buttonColors(
295:                                     containerColor = Color(0xFFE57373)
296:                                 )
297:                             ) {
298:                                 Icon(
299:                                     imageVector = Icons.Default.Delete,
300:                                     contentDescription = "Clear Cache",
301:                                     modifier = Modifier.padding(end = 8.dp)
302:                                 )
303:                                 Text("Clear Cache")
304:                             }
305:                         }
306:                     }
307:                 }
308: 
309:                 // Recently viewed section
310:                 item {
311:                     Row(
312:                         modifier = Modifier
313:                             .fillMaxWidth()
314:                             .padding(vertical = 8.dp),
315:                         verticalAlignment = Alignment.CenterVertically
316:                     ) {
317:                         Icon(
318:                             imageVector = Icons.Default.History,
319:                             contentDescription = "Recently Viewed",
320:                             tint = AppColors.Primary,
321:                             modifier = Modifier.size(24.dp)
322:                         )
323: 
324:                         Text(
325:                             text = "Recently Viewed",
326:                             fontSize = 18.sp,
327:                             fontWeight = FontWeight.Bold,
328:                             modifier = Modifier.padding(start = 16.dp)
329:                         )
330:                     }
331:                 }
332: 
333:                 // Recently viewed cocktails
334:                 if (recentlyViewedCocktails.isEmpty()) {
335:                     item {
336:                         Box(
337:                             modifier = Modifier
338:                                 .fillMaxWidth()
339:                                 .padding(32.dp),
340:                             contentAlignment = Alignment.Center
341:                         ) {
342:                             Column(
343:                                 horizontalAlignment = Alignment.CenterHorizontally
344:                             ) {
345:                                 Icon(
346:                                     imageVector = Icons.Default.Info,
347:                                     contentDescription = "No cocktails",
348:                                     tint = AppColors.Gray,
349:                                     modifier = Modifier.size(48.dp)
350:                                 )
351: 
352:                                 Spacer(modifier = Modifier.height(16.dp))
353: 
354:                                 Text(
355:                                     text = "No recently viewed cocktails",
356:                                     fontSize = 16.sp,
357:                                     color = AppColors.TextSecondary,
358:                                     textAlign = androidx.compose.ui.text.style.TextAlign.Center
359:                                 )
360: 
361:                                 Spacer(modifier = Modifier.height(8.dp))
362: 
363:                                 Text(
364:                                     text = "Browse cocktails in the Home screen to cache them for offline viewing",
365:                                     fontSize = 14.sp,
366:                                     color = AppColors.TextSecondary,
367:                                     textAlign = androidx.compose.ui.text.style.TextAlign.Center
368:                                 )
369: 
370:                                 Spacer(modifier = Modifier.height(16.dp))
371: 
372:                                 Button(
373:                                     onClick = { onBackClick() },
374:                                     modifier = Modifier.padding(top = 8.dp)
375:                                 ) {
376:                                     Icon(
377:                                         imageVector = Icons.Default.Home,
378:                                         contentDescription = "Go to Home",
379:                                         modifier = Modifier.padding(end = 8.dp)
380:                                     )
381:                                     Text("Go to Home")
382:                                 }
383:                             }
384:                         }
385:                     }
386:                 } else {
387:                     items(recentlyViewedCocktails) { cocktail ->
388:                         CocktailItem(
389:                             cocktail = cocktail,
390:                             onClick = { onCocktailClick(cocktail) },
391:                             onAddToCart = { /* Not needed here */ },
392:                             isFavorite = false, // We don't have this info here
393:                             onToggleFavorite = { /* Not needed here */ }
394:                         )
395:                     }
396:                 }
397:             }
398:         }
399:     }
400: }
````

## File: androidApp/src/main/java/com/cocktailcraft/screens/OrderListScreen.kt
````kotlin
  1: package com.cocktailcraft.screens
  2: 
  3: import androidx.compose.foundation.background
  4: import androidx.compose.foundation.layout.*
  5: import androidx.compose.foundation.lazy.LazyColumn
  6: import androidx.compose.foundation.lazy.itemsIndexed
  7: import androidx.compose.foundation.shape.CircleShape
  8: import androidx.compose.foundation.shape.RoundedCornerShape
  9: import androidx.compose.material.icons.Icons
 10: import androidx.compose.material.icons.filled.List
 11: import androidx.compose.material3.*
 12: import androidx.compose.runtime.Composable
 13: import androidx.compose.runtime.collectAsState
 14: import androidx.compose.runtime.getValue
 15: import androidx.compose.ui.Alignment
 16: import androidx.compose.ui.Modifier
 17: import androidx.compose.ui.graphics.Color
 18: import androidx.compose.ui.text.font.FontWeight
 19: import androidx.compose.ui.text.style.TextAlign
 20: import androidx.compose.ui.unit.dp
 21: import androidx.compose.ui.unit.sp
 22: import com.cocktailcraft.domain.model.Order
 23: import com.cocktailcraft.ui.theme.AppColors
 24: import com.cocktailcraft.navigation.Screen
 25: import com.cocktailcraft.navigation.NavigationManager
 26: import androidx.compose.material3.Divider
 27: 
 28: @Composable
 29: fun OrderListScreen(
 30:     orderViewModel: com.cocktailcraft.viewmodel.OrderViewModel,
 31:     navigationManager: NavigationManager
 32: ) {
 33:     val orders by orderViewModel.orders.collectAsState()
 34:     val isLoading by orderViewModel.isLoading.collectAsState()
 35:     val error by orderViewModel.error.collectAsState()
 36: 
 37:     Column(
 38:         modifier = Modifier
 39:             .fillMaxSize()
 40:             .background(AppColors.Background)
 41:     ) {
 42:         if (isLoading) {
 43:             Box(
 44:                 modifier = Modifier.fillMaxSize(),
 45:                 contentAlignment = Alignment.Center
 46:             ) {
 47:                 CircularProgressIndicator(color = AppColors.Primary)
 48:             }
 49:         } else if (error?.isNotEmpty() == true) {
 50:             Box(
 51:                 modifier = Modifier.fillMaxSize(),
 52:                 contentAlignment = Alignment.Center
 53:             ) {
 54:                 Text(
 55:                     text = error ?: "",
 56:                     color = AppColors.Error,
 57:                     textAlign = TextAlign.Center,
 58:                     modifier = Modifier.padding(16.dp)
 59:                 )
 60:             }
 61:         } else if (orders.isEmpty()) {
 62:             // Empty state
 63:             Box(
 64:                 modifier = Modifier.fillMaxSize(),
 65:                 contentAlignment = Alignment.Center
 66:             ) {
 67:                 Column(
 68:                     horizontalAlignment = Alignment.CenterHorizontally,
 69:                     modifier = Modifier.padding(16.dp)
 70:                 ) {
 71:                     Icon(
 72:                         imageVector = Icons.Filled.List,
 73:                         contentDescription = null,
 74:                         tint = AppColors.Gray,
 75:                         modifier = Modifier.size(64.dp)
 76:                     )
 77: 
 78:                     Spacer(modifier = Modifier.height(16.dp))
 79: 
 80:                     Text(
 81:                         text = "No orders yet",
 82:                         fontSize = 18.sp,
 83:                         fontWeight = FontWeight.Bold,
 84:                         color = AppColors.TextPrimary
 85:                     )
 86: 
 87:                     Spacer(modifier = Modifier.height(8.dp))
 88: 
 89:                     Text(
 90:                         text = "Your order history will appear here",
 91:                         fontSize = 14.sp,
 92:                         color = AppColors.TextSecondary,
 93:                         textAlign = TextAlign.Center
 94:                     )
 95: 
 96:                     Spacer(modifier = Modifier.height(24.dp))
 97: 
 98:                     Button(
 99:                         onClick = { navigationManager.navigateToHome() },
100:                         colors = ButtonDefaults.buttonColors(
101:                             containerColor = AppColors.Primary
102:                         ),
103:                         shape = RoundedCornerShape(12.dp)
104:                     ) {
105:                         Text("Browse Cocktails")
106:                     }
107:                 }
108:             }
109:         } else {
110:             // Order list
111:             LazyColumn(
112:                 modifier = Modifier
113:                     .fillMaxSize()
114:                     .padding(horizontal = 16.dp),
115:                 contentPadding = PaddingValues(vertical = 16.dp),
116:                 verticalArrangement = Arrangement.spacedBy(16.dp)
117:             ) {
118:                 item {
119:                     Text(
120:                         text = "Your Orders",
121:                         fontSize = 20.sp,
122:                         fontWeight = FontWeight.Bold,
123:                         color = AppColors.TextPrimary,
124:                         modifier = Modifier.padding(bottom = 8.dp)
125:                     )
126:                 }
127: 
128:                 itemsIndexed(orders) { _, order ->
129:                     OrderItem(order = order)
130:                 }
131:             }
132:         }
133:     }
134: }
135: 
136: @Composable
137: fun OrderItem(order: Order) {
138:     Card(
139:         modifier = Modifier
140:             .fillMaxWidth(),
141:         shape = RoundedCornerShape(12.dp),
142:         colors = CardDefaults.cardColors(
143:             containerColor = AppColors.Surface
144:         ),
145:         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
146:     ) {
147:         Column(
148:             modifier = Modifier.padding(16.dp)
149:         ) {
150:             // Order header
151:             Row(
152:                 modifier = Modifier.fillMaxWidth(),
153:                 horizontalArrangement = Arrangement.SpaceBetween,
154:                 verticalAlignment = Alignment.CenterVertically
155:             ) {
156:                 Text(
157:                     text = "Order #${order.id.takeLast(5)}",
158:                     fontSize = 16.sp,
159:                     fontWeight = FontWeight.Bold,
160:                     color = AppColors.TextPrimary
161:                 )
162: 
163:                 Text(
164:                     text = order.date,
165:                     fontSize = 14.sp,
166:                     color = AppColors.TextSecondary
167:                 )
168:             }
169: 
170:             Spacer(modifier = Modifier.height(12.dp))
171: 
172:             // Order items
173:             Column {
174:                 order.items.forEach { item ->
175:                     Row(
176:                         modifier = Modifier
177:                             .fillMaxWidth()
178:                             .padding(vertical = 4.dp),
179:                         horizontalArrangement = Arrangement.SpaceBetween
180:                     ) {
181:                         Text(
182:                             text = "${item.quantity}x ${item.name}",
183:                             fontSize = 14.sp,
184:                             color = AppColors.TextPrimary
185:                         )
186: 
187:                         Text(
188:                             text = "$${String.format("%.2f", item.price * item.quantity)}",
189:                             fontSize = 14.sp,
190:                             fontWeight = FontWeight.Medium,
191:                             color = AppColors.TextPrimary
192:                         )
193:                     }
194:                 }
195:             }
196: 
197:             Divider(
198:                 modifier = Modifier.padding(vertical = 12.dp),
199:                 color = AppColors.LightGray
200:             )
201: 
202:             // Total row
203:             Row(
204:                 modifier = Modifier.fillMaxWidth(),
205:                 horizontalArrangement = Arrangement.SpaceBetween,
206:                 verticalAlignment = Alignment.CenterVertically
207:             ) {
208:                 Text(
209:                     text = "Total",
210:                     fontSize = 16.sp,
211:                     fontWeight = FontWeight.Bold,
212:                     color = AppColors.TextPrimary
213:                 )
214: 
215:                 Text(
216:                     text = "$${String.format("%.2f", order.total)}",
217:                     fontSize = 16.sp,
218:                     fontWeight = FontWeight.Bold,
219:                     color = AppColors.Primary
220:                 )
221:             }
222: 
223:             Spacer(modifier = Modifier.height(12.dp))
224: 
225:             // Order status
226:             Row(
227:                 verticalAlignment = Alignment.CenterVertically
228:             ) {
229:                 Box(
230:                     modifier = Modifier
231:                         .size(10.dp)
232:                         .background(
233:                             when (order.status) {
234:                                 "Completed" -> Color(0xFF4CAF50)
235:                                 "In Progress" -> Color(0xFFFFA000)
236:                                 else -> AppColors.Gray
237:                             },
238:                             CircleShape
239:                         )
240:                 )
241: 
242:                 Spacer(modifier = Modifier.width(8.dp))
243: 
244:                 Text(
245:                     text = order.status,
246:                     fontSize = 14.sp,
247:                     color = when (order.status) {
248:                         "Completed" -> Color(0xFF4CAF50)
249:                         "In Progress" -> Color(0xFFFFA000)
250:                         else -> AppColors.Gray
251:                     }
252:                 )
253:             }
254:         }
255:     }
256: }
````

## File: androidApp/src/main/java/com/cocktailcraft/screens/ProfileScreen.kt
````kotlin
  1: package com.cocktailcraft.screens
  2: 
  3: import androidx.compose.foundation.background
  4: import androidx.compose.foundation.clickable
  5: import androidx.compose.foundation.layout.Arrangement
  6: import androidx.compose.foundation.layout.Box
  7: import androidx.compose.foundation.layout.Column
  8: import androidx.compose.foundation.layout.Row
  9: import androidx.compose.foundation.layout.Spacer
 10: import androidx.compose.foundation.layout.fillMaxSize
 11: import androidx.compose.foundation.layout.fillMaxWidth
 12: import androidx.compose.foundation.layout.height
 13: import androidx.compose.foundation.layout.padding
 14: import androidx.compose.foundation.layout.size
 15: import androidx.compose.foundation.layout.width
 16: import androidx.compose.foundation.rememberScrollState
 17: import androidx.compose.foundation.shape.CircleShape
 18: import androidx.compose.foundation.shape.RoundedCornerShape
 19: import androidx.compose.foundation.text.KeyboardOptions
 20: import androidx.compose.foundation.verticalScroll
 21: import androidx.compose.material.icons.Icons
 22: import androidx.compose.material.icons.filled.ChevronRight
 23: import androidx.compose.material.icons.filled.DarkMode
 24: import androidx.compose.material.icons.filled.CloudOff
 25: import androidx.compose.material.icons.filled.DateRange
 26: import androidx.compose.material.icons.filled.Email
 27: import androidx.compose.material.icons.filled.ExitToApp
 28: import androidx.compose.material.icons.filled.Help
 29: import androidx.compose.material.icons.filled.LightMode
 30: import androidx.compose.material.icons.filled.Lock
 31: import androidx.compose.material.icons.filled.Notifications
 32: import androidx.compose.material.icons.filled.Person
 33: import androidx.compose.material.icons.filled.Visibility
 34: import androidx.compose.material.icons.filled.VisibilityOff
 35: import androidx.compose.material3.AlertDialog
 36: import androidx.compose.material3.Button
 37: import androidx.compose.material3.ButtonDefaults
 38: import androidx.compose.material3.Card
 39: import androidx.compose.material3.CardDefaults
 40: import androidx.compose.material3.CircularProgressIndicator
 41: import androidx.compose.material3.Icon
 42: import androidx.compose.material3.IconButton
 43: import androidx.compose.material3.OutlinedButton
 44: import androidx.compose.material3.OutlinedTextField
 45: import androidx.compose.material3.Switch
 46: import androidx.compose.material3.Text
 47: import androidx.compose.runtime.Composable
 48: import androidx.compose.runtime.collectAsState
 49: import androidx.compose.runtime.getValue
 50: import androidx.compose.runtime.mutableStateOf
 51: import androidx.compose.runtime.remember
 52: import androidx.compose.runtime.setValue
 53: import androidx.compose.ui.Alignment
 54: import androidx.compose.ui.Modifier
 55: import androidx.compose.ui.draw.alpha
 56: import androidx.compose.ui.graphics.Color
 57: import androidx.compose.ui.graphics.vector.ImageVector
 58: import androidx.compose.ui.platform.LocalContext
 59: import androidx.compose.ui.text.font.FontWeight
 60: import androidx.compose.ui.text.input.KeyboardType
 61: import androidx.compose.ui.text.input.PasswordVisualTransformation
 62: import androidx.compose.ui.text.input.VisualTransformation
 63: import androidx.compose.ui.unit.dp
 64: import androidx.compose.ui.unit.sp
 65: import androidx.lifecycle.viewmodel.compose.viewModel
 66: import com.cocktailcraft.navigation.NavigationManager
 67: import com.cocktailcraft.ui.components.AnimatedThemeToggleRow
 68: import com.cocktailcraft.ui.theme.AppColors
 69: import com.cocktailcraft.viewmodel.ProfileViewModel
 70: import com.cocktailcraft.viewmodel.ThemeViewModel
 71: 
 72: @Composable
 73: fun ProfileScreen(
 74:     navigationManager: NavigationManager,
 75:     profileViewModel: ProfileViewModel = viewModel(),
 76:     themeViewModel: ThemeViewModel = viewModel()
 77: ) {
 78:     // Get user data from ViewModel
 79:     val user by profileViewModel.user.collectAsState()
 80:     val isSignedIn by profileViewModel.isSignedIn.collectAsState()
 81:     val isLoading by profileViewModel.isLoading.collectAsState()
 82:     val error by profileViewModel.error.collectAsState()
 83: 
 84:     // Get theme data from ThemeViewModel
 85:     val isDarkMode by themeViewModel.isDarkMode.collectAsState()
 86:     val followSystemTheme by themeViewModel.followSystemTheme.collectAsState()
 87: 
 88:     // Dialog states
 89:     var showLogoutDialog by remember { mutableStateOf(false) }
 90:     var showSignInDialog by remember { mutableStateOf(false) }
 91:     var showSignUpDialog by remember { mutableStateOf(false) }
 92: 
 93:     // Sample profile data (in a real app, this would come from a ViewModel)
 94:     val userName = user?.name ?: "Guest User"
 95:     val userEmail = user?.email ?: "guest@example.com"
 96: 
 97:     // Sign in dialog state
 98:     if (showSignInDialog) {
 99:         SignInDialog(
100:             onDismiss = { showSignInDialog = false },
101:             onSignIn = { email, password ->
102:                 profileViewModel.signIn(email, password)
103:                 showSignInDialog = false
104:             }
105:         )
106:     }
107: 
108:     // Sign up dialog state
109:     if (showSignUpDialog) {
110:         SignUpDialog(
111:             onDismiss = { showSignUpDialog = false },
112:             onSignUp = { name, email, password ->
113:                 profileViewModel.signUp(name, email, password)
114:                 showSignUpDialog = false
115:             }
116:         )
117:     }
118: 
119:     // Logout dialog state
120:     if (showLogoutDialog) {
121:         AlertDialog(
122:             onDismissRequest = { showLogoutDialog = false },
123:             title = { Text("Logout") },
124:             text = { Text("Are you sure you want to logout?") },
125:             confirmButton = {
126:                 Button(
127:                     onClick = {
128:                         profileViewModel.signOut()
129:                         showLogoutDialog = false
130:                         // Navigate to Home screen after logout
131:                         navigationManager.navigateToHome()
132:                     },
133:                     colors = ButtonDefaults.buttonColors(
134:                         containerColor = AppColors.Primary
135:                     )
136:                 ) {
137:                     Text("Logout")
138:                 }
139:             },
140:             dismissButton = {
141:                 OutlinedButton(
142:                     onClick = { showLogoutDialog = false }
143:                 ) {
144:                     Text("Cancel")
145:                 }
146:             }
147:         )
148:     }
149: 
150:     Column(
151:         modifier = Modifier
152:             .fillMaxSize()
153:             .background(AppColors.Background)
154:             .verticalScroll(rememberScrollState())
155:             .padding(16.dp)
156:     ) {
157:         // Profile header
158:         Card(
159:             modifier = Modifier
160:                 .fillMaxWidth()
161:                 .padding(bottom = 16.dp),
162:             shape = RoundedCornerShape(16.dp),
163:             colors = CardDefaults.cardColors(
164:                 containerColor = AppColors.Surface
165:             ),
166:             elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
167:         ) {
168:             Column(
169:                 modifier = Modifier
170:                     .padding(16.dp)
171:                     .fillMaxWidth(),
172:                 horizontalAlignment = Alignment.CenterHorizontally
173:             ) {
174:                 // Profile picture
175:                 Box(
176:                     modifier = Modifier
177:                         .size(80.dp)
178:                         .background(AppColors.Primary.copy(alpha = 0.2f), CircleShape),
179:                     contentAlignment = Alignment.Center
180:                 ) {
181:                     Text(
182:                         text = userName.take(1).uppercase(),
183:                         fontSize = 32.sp,
184:                         fontWeight = FontWeight.Bold,
185:                         color = AppColors.Primary
186:                     )
187:                 }
188: 
189:                 Spacer(modifier = Modifier.height(16.dp))
190: 
191:                 // User name
192:                 Text(
193:                     text = userName,
194:                     fontSize = 20.sp,
195:                     fontWeight = FontWeight.Bold,
196:                     color = AppColors.TextPrimary
197:                 )
198: 
199:                 Spacer(modifier = Modifier.height(4.dp))
200: 
201:                 // User email
202:                 Text(
203:                     text = userEmail,
204:                     fontSize = 14.sp,
205:                     color = AppColors.TextSecondary
206:                 )
207: 
208:                 // Show login/signup buttons if not signed in
209:                 if (!isSignedIn) {
210:                     Spacer(modifier = Modifier.height(24.dp))
211: 
212:                     Text(
213:                         text = "Sign in to access your profile",
214:                         fontSize = 16.sp,
215:                         color = AppColors.TextSecondary,
216:                         modifier = Modifier.padding(bottom = 16.dp)
217:                     )
218: 
219:                     Button(
220:                         onClick = { showSignInDialog = true },
221:                         colors = ButtonDefaults.buttonColors(
222:                             containerColor = AppColors.Primary
223:                         ),
224:                         modifier = Modifier
225:                             .fillMaxWidth(0.8f)
226:                             .padding(vertical = 4.dp)
227:                     ) {
228:                         Text("Sign In")
229:                     }
230: 
231:                     Spacer(modifier = Modifier.height(8.dp))
232: 
233:                     OutlinedButton(
234:                         onClick = { showSignUpDialog = true },
235:                         modifier = Modifier
236:                             .fillMaxWidth(0.8f)
237:                             .padding(vertical = 4.dp)
238:                     ) {
239:                         Text("Create Account")
240:                     }
241:                 }
242:             }
243:         }
244: 
245:         // Only show account settings and logout option if signed in
246:         if (isSignedIn) {
247:             // Account settings
248:             Card(
249:                 modifier = Modifier
250:                     .fillMaxWidth()
251:                     .padding(bottom = 16.dp),
252:                 shape = RoundedCornerShape(16.dp),
253:                 colors = CardDefaults.cardColors(
254:                     containerColor = AppColors.Surface
255:                 ),
256:                 elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
257:             ) {
258:                 Column(
259:                     modifier = Modifier.padding(16.dp)
260:                 ) {
261:                     Text(
262:                         text = "Account Settings",
263:                         fontSize = 18.sp,
264:                         fontWeight = FontWeight.Bold,
265:                         color = AppColors.TextPrimary,
266:                         modifier = Modifier.padding(bottom = 16.dp)
267:                     )
268: 
269:                     SettingsItem(
270:                         icon = Icons.Default.Person,
271:                         title = "Edit Profile",
272:                         onClick = { /* Handle edit profile */ }
273:                     )
274: 
275:                     SettingsItem(
276:                         icon = Icons.Default.Lock,
277:                         title = "Change Password",
278:                         onClick = { /* Handle change password */ }
279:                     )
280: 
281:                     SettingsItem(
282:                         icon = Icons.Default.Email,
283:                         title = "Email Preferences",
284:                         onClick = { /* Handle email preferences */ }
285:                     )
286: 
287:                     SettingsItem(
288:                         icon = Icons.Default.Notifications,
289:                         title = "Notification Settings",
290:                         onClick = { /* Handle notification settings */ }
291:                     )
292:                 }
293:             }
294:         }
295: 
296:         // App settings
297:         Card(
298:             modifier = Modifier
299:                 .fillMaxWidth()
300:                 .padding(bottom = 16.dp),
301:             shape = RoundedCornerShape(16.dp),
302:             colors = CardDefaults.cardColors(
303:                 containerColor = AppColors.Surface
304:             ),
305:             elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
306:         ) {
307:             Column(
308:                 modifier = Modifier.padding(16.dp)
309:             ) {
310:                 Text(
311:                     text = "App Settings",
312:                     fontSize = 18.sp,
313:                     fontWeight = FontWeight.Bold,
314:                     color = AppColors.TextPrimary,
315:                     modifier = Modifier.padding(bottom = 16.dp)
316:                 )
317: 
318:                 SettingsItem(
319:                     icon = Icons.Default.DateRange,
320:                     title = "Order History",
321:                     onClick = { navigationManager.navigateToOrderList() }
322:                 )
323: 
324:                 SettingsItem(
325:                     icon = Icons.Default.Help,
326:                     title = "Help & Support",
327:                     onClick = { /* Handle help & support */ }
328:                 )
329: 
330:                 SettingsItem(
331:                     icon = Icons.Default.CloudOff,
332:                     title = "Offline Mode",
333:                     onClick = { navigationManager.navigateToOfflineMode() }
334:                 )
335: 
336:                 // Follow System Theme Toggle with animated switch
337:                 AnimatedThemeToggleRow(
338:                     title = "Follow System Theme",
339:                     subtitle = if (followSystemTheme) "On" else "Off",
340:                     icon = Icons.Default.DateRange,
341:                     isChecked = followSystemTheme,
342:                     onToggle = { themeViewModel.toggleFollowSystemTheme() },
343:                     modifier = Modifier.fillMaxWidth()
344:                 )
345: 
346:                 // Dark Mode Toggle with animated switch (only enabled if not following system theme)
347:                 AnimatedThemeToggleRow(
348:                     title = "Dark Mode",
349:                     subtitle = if (followSystemTheme)
350:                         "Controlled by system"
351:                     else
352:                         if (isDarkMode) "On" else "Off",
353:                     icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
354:                     isChecked = isDarkMode,
355:                     onToggle = {
356:                         if (!followSystemTheme) themeViewModel.toggleDarkMode()
357:                     },
358:                     enabled = !followSystemTheme,
359:                     modifier = Modifier.fillMaxWidth()
360:                 )
361: 
362:                 // Only show logout if signed in
363:                 if (isSignedIn) {
364:                     SettingsItem(
365:                         icon = Icons.Default.ExitToApp,
366:                         title = "Logout",
367:                         onClick = { showLogoutDialog = true },
368:                         textColor = AppColors.Error
369:                     )
370:                 }
371:             }
372:         }
373: 
374:         // App information
375:         Card(
376:             modifier = Modifier.fillMaxWidth(),
377:             shape = RoundedCornerShape(16.dp),
378:             colors = CardDefaults.cardColors(
379:                 containerColor = AppColors.Surface
380:             ),
381:             elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
382:         ) {
383:             Column(
384:                 modifier = Modifier.padding(16.dp)
385:             ) {
386:                 Text(
387:                     text = "About",
388:                     fontSize = 18.sp,
389:                     fontWeight = FontWeight.Bold,
390:                     color = AppColors.TextPrimary,
391:                     modifier = Modifier.padding(bottom = 16.dp)
392:                 )
393: 
394:                 Text(
395:                     text = "Cocktail Bar App",
396:                     fontSize = 16.sp,
397:                     fontWeight = FontWeight.Medium,
398:                     color = AppColors.TextPrimary
399:                 )
400: 
401:                 Spacer(modifier = Modifier.height(4.dp))
402: 
403:                 Text(
404:                     text = "Version 1.0.0",
405:                     fontSize = 14.sp,
406:                     color = AppColors.TextSecondary
407:                 )
408: 
409:                 Spacer(modifier = Modifier.height(16.dp))
410: 
411:                 Text(
412:                     text = "© 2023 Cocktail Bar. All rights reserved.",
413:                     fontSize = 12.sp,
414:                     color = AppColors.TextSecondary
415:                 )
416:             }
417:         }
418:     }
419: 
420:     // Show loading indicator if needed
421:     if (isLoading) {
422:         Box(
423:             modifier = Modifier
424:                 .fillMaxSize()
425:                 .background(Color.Black.copy(alpha = 0.5f)),
426:             contentAlignment = Alignment.Center
427:         ) {
428:             CircularProgressIndicator(color = AppColors.Primary)
429:         }
430:     }
431: 
432:     // Show error message if needed
433:     error?.let { errorMessage ->
434:         AlertDialog(
435:             onDismissRequest = { profileViewModel.clearError() },
436:             title = { Text("Error") },
437:             text = { Text(errorMessage) },
438:             confirmButton = {
439:                 Button(
440:                     onClick = { profileViewModel.clearError() },
441:                     colors = ButtonDefaults.buttonColors(
442:                         containerColor = AppColors.Primary
443:                     )
444:                 ) {
445:                     Text("OK")
446:                 }
447:             }
448:         )
449:     }
450: }
451: 
452: @Composable
453: fun SignInDialog(
454:     onDismiss: () -> Unit,
455:     onSignIn: (email: String, password: String) -> Unit
456: ) {
457:     var email by remember { mutableStateOf("") }
458:     var password by remember { mutableStateOf("") }
459:     var passwordVisible by remember { mutableStateOf(false) }
460: 
461:     AlertDialog(
462:         onDismissRequest = onDismiss,
463:         title = { Text("Sign In") },
464:         text = {
465:             Column {
466:                 OutlinedTextField(
467:                     value = email,
468:                     onValueChange = { email = it },
469:                     label = { Text("Email") },
470:                     singleLine = true,
471:                     modifier = Modifier
472:                         .fillMaxWidth()
473:                         .padding(vertical = 8.dp),
474:                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
475:                 )
476: 
477:                 OutlinedTextField(
478:                     value = password,
479:                     onValueChange = { password = it },
480:                     label = { Text("Password") },
481:                     singleLine = true,
482:                     visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
483:                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
484:                     modifier = Modifier
485:                         .fillMaxWidth()
486:                         .padding(vertical = 8.dp),
487:                     trailingIcon = {
488:                         IconButton(onClick = { passwordVisible = !passwordVisible }) {
489:                             Icon(
490:                                 imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
491:                                 contentDescription = if (passwordVisible) "Hide password" else "Show password"
492:                             )
493:                         }
494:                     }
495:                 )
496:             }
497:         },
498:         confirmButton = {
499:             Button(
500:                 onClick = { onSignIn(email, password) },
501:                 enabled = email.isNotBlank() && password.isNotBlank(),
502:                 colors = ButtonDefaults.buttonColors(
503:                     containerColor = AppColors.Primary
504:                 )
505:             ) {
506:                 Text("Sign In")
507:             }
508:         },
509:         dismissButton = {
510:             OutlinedButton(onClick = onDismiss) {
511:                 Text("Cancel")
512:             }
513:         }
514:     )
515: }
516: 
517: @Composable
518: fun SignUpDialog(
519:     onDismiss: () -> Unit,
520:     onSignUp: (name: String, email: String, password: String) -> Unit
521: ) {
522:     var name by remember { mutableStateOf("") }
523:     var email by remember { mutableStateOf("") }
524:     var password by remember { mutableStateOf("") }
525:     var passwordVisible by remember { mutableStateOf(false) }
526: 
527:     AlertDialog(
528:         onDismissRequest = onDismiss,
529:         title = { Text("Create Account") },
530:         text = {
531:             Column {
532:                 OutlinedTextField(
533:                     value = name,
534:                     onValueChange = { name = it },
535:                     label = { Text("Name") },
536:                     singleLine = true,
537:                     modifier = Modifier
538:                         .fillMaxWidth()
539:                         .padding(vertical = 8.dp)
540:                 )
541: 
542:                 OutlinedTextField(
543:                     value = email,
544:                     onValueChange = { email = it },
545:                     label = { Text("Email") },
546:                     singleLine = true,
547:                     modifier = Modifier
548:                         .fillMaxWidth()
549:                         .padding(vertical = 8.dp),
550:                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
551:                 )
552: 
553:                 OutlinedTextField(
554:                     value = password,
555:                     onValueChange = { password = it },
556:                     label = { Text("Password") },
557:                     singleLine = true,
558:                     visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
559:                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
560:                     modifier = Modifier
561:                         .fillMaxWidth()
562:                         .padding(vertical = 8.dp),
563:                     trailingIcon = {
564:                         IconButton(onClick = { passwordVisible = !passwordVisible }) {
565:                             Icon(
566:                                 imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
567:                                 contentDescription = if (passwordVisible) "Hide password" else "Show password"
568:                             )
569:                         }
570:                     }
571:                 )
572:             }
573:         },
574:         confirmButton = {
575:             Button(
576:                 onClick = { onSignUp(name, email, password) },
577:                 enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
578:                 colors = ButtonDefaults.buttonColors(
579:                     containerColor = AppColors.Primary
580:                 )
581:             ) {
582:                 Text("Create Account")
583:             }
584:         },
585:         dismissButton = {
586:             OutlinedButton(onClick = onDismiss) {
587:                 Text("Cancel")
588:             }
589:         }
590:     )
591: }
592: 
593: @Composable
594: fun SettingsItem(
595:     icon: ImageVector,
596:     title: String,
597:     onClick: () -> Unit,
598:     textColor: Color = AppColors.TextPrimary
599: ) {
600:     Row(
601:         modifier = Modifier
602:             .fillMaxWidth()
603:             .clickable(onClick = onClick)
604:             .padding(vertical = 12.dp),
605:         verticalAlignment = Alignment.CenterVertically
606:     ) {
607:         Icon(
608:             imageVector = icon,
609:             contentDescription = null,
610:             tint = textColor,
611:             modifier = Modifier.size(24.dp)
612:         )
613: 
614:         Spacer(modifier = Modifier.width(16.dp))
615: 
616:         Text(
617:             text = title,
618:             fontSize = 16.sp,
619:             color = textColor,
620:             modifier = Modifier.weight(1f)
621:         )
622: 
623:         Icon(
624:             imageVector = Icons.Default.ChevronRight,
625:             contentDescription = null,
626:             tint = AppColors.Gray,
627:             modifier = Modifier.size(20.dp)
628:         )
629:     }
630: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/animation/AnimationUtils.kt
````kotlin
  1: package com.cocktailcraft.ui.animation
  2: 
  3: import androidx.compose.animation.AnimatedContentTransitionScope
  4: import androidx.compose.animation.ContentTransform
  5: import androidx.compose.animation.EnterTransition
  6: import androidx.compose.animation.ExitTransition
  7: import androidx.compose.animation.core.FastOutSlowInEasing
  8: import androidx.compose.animation.core.LinearOutSlowInEasing
  9: import androidx.compose.animation.core.Spring
 10: import androidx.compose.animation.core.spring
 11: import androidx.compose.animation.core.tween
 12: import androidx.compose.animation.fadeIn
 13: import androidx.compose.animation.fadeOut
 14: import androidx.compose.animation.scaleIn
 15: import androidx.compose.animation.scaleOut
 16: import androidx.compose.animation.slideInHorizontally
 17: import androidx.compose.animation.slideInVertically
 18: import androidx.compose.animation.slideOutHorizontally
 19: import androidx.compose.animation.slideOutVertically
 20: import androidx.compose.animation.togetherWith
 21: 
 22: /**
 23:  * Utility object containing reusable animations for the app
 24:  */
 25: object AnimationUtils {
 26:     // Duration constants
 27:     const val ANIMATION_DURATION_SHORT = 150
 28:     const val ANIMATION_DURATION_MEDIUM = 300
 29:     const val ANIMATION_DURATION_LONG = 500
 30: 
 31:     // Spring constants
 32:     val SPRING_STIFFNESS_LOW = Spring.StiffnessLow
 33:     val SPRING_STIFFNESS_MEDIUM = Spring.StiffnessMedium
 34:     val SPRING_STIFFNESS_HIGH = Spring.StiffnessHigh
 35: 
 36:     val SPRING_DAMPENING_LOW_BOUNCY = Spring.DampingRatioLowBouncy
 37:     val SPRING_DAMPENING_MEDIUM_BOUNCY = Spring.DampingRatioMediumBouncy
 38:     val SPRING_DAMPENING_HIGH_BOUNCY = Spring.DampingRatioHighBouncy
 39:     val SPRING_DAMPENING_NO_BOUNCE = Spring.DampingRatioNoBouncy
 40: 
 41:     // Fade animations
 42:     val fadeInMedium = fadeIn(
 43:         animationSpec = tween(
 44:             durationMillis = ANIMATION_DURATION_MEDIUM,
 45:             easing = LinearOutSlowInEasing
 46:         )
 47:     )
 48: 
 49:     val fadeOutMedium = fadeOut(
 50:         animationSpec = tween(
 51:             durationMillis = ANIMATION_DURATION_MEDIUM,
 52:             easing = FastOutSlowInEasing
 53:         )
 54:     )
 55: 
 56:     // Scale animations
 57:     val scaleInMedium = scaleIn(
 58:         animationSpec = tween(
 59:             durationMillis = ANIMATION_DURATION_MEDIUM,
 60:             easing = LinearOutSlowInEasing
 61:         ),
 62:         initialScale = 0.9f
 63:     )
 64: 
 65:     val scaleOutMedium = scaleOut(
 66:         animationSpec = tween(
 67:             durationMillis = ANIMATION_DURATION_MEDIUM,
 68:             easing = FastOutSlowInEasing
 69:         ),
 70:         targetScale = 0.9f
 71:     )
 72: 
 73:     // Bounce scale animation
 74:     val bounceScaleIn = scaleIn(
 75:         animationSpec = spring(
 76:             dampingRatio = SPRING_DAMPENING_MEDIUM_BOUNCY,
 77:             stiffness = SPRING_STIFFNESS_MEDIUM
 78:         ),
 79:         initialScale = 0.8f
 80:     )
 81: 
 82:     // Slide animations
 83:     val slideInFromBottom = slideInVertically(
 84:         animationSpec = tween(
 85:             durationMillis = ANIMATION_DURATION_MEDIUM,
 86:             easing = LinearOutSlowInEasing
 87:         ),
 88:         initialOffsetY = { it }
 89:     )
 90: 
 91:     val slideOutToBottom = slideOutVertically(
 92:         animationSpec = tween(
 93:             durationMillis = ANIMATION_DURATION_MEDIUM,
 94:             easing = FastOutSlowInEasing
 95:         ),
 96:         targetOffsetY = { it }
 97:     )
 98: 
 99:     // Combined animations
100:     val enterWithFadeAndScale: EnterTransition = fadeInMedium + scaleInMedium
101:     val exitWithFadeAndScale: ExitTransition = fadeOutMedium + scaleOutMedium
102: 
103:     val enterWithFadeAndSlideFromBottom: EnterTransition = fadeInMedium + slideInFromBottom
104:     val exitWithFadeAndSlideToBottom: ExitTransition = fadeOutMedium + slideOutToBottom
105: 
106:     // Content transform for animated content
107:     val fadeAndSlideContentTransform: ContentTransform = fadeInMedium.togetherWith(fadeOutMedium)
108:     val scaleAndFadeContentTransform: ContentTransform = (fadeInMedium + scaleInMedium).togetherWith(fadeOutMedium + scaleOutMedium)
109: 
110:     // Navigation transitions
111:     fun slideIntoContainerFromRight(): EnterTransition {
112:         return slideInHorizontally(
113:             animationSpec = tween(
114:                 durationMillis = ANIMATION_DURATION_MEDIUM,
115:                 easing = LinearOutSlowInEasing
116:             ),
117:             initialOffsetX = { fullWidth -> fullWidth }
118:         ) + fadeIn(
119:             animationSpec = tween(ANIMATION_DURATION_MEDIUM)
120:         )
121:     }
122: 
123:     fun slideOutOfContainerToLeft(): ExitTransition {
124:         return slideOutHorizontally(
125:             animationSpec = tween(
126:                 durationMillis = ANIMATION_DURATION_MEDIUM,
127:                 easing = FastOutSlowInEasing
128:             ),
129:             targetOffsetX = { fullWidth -> -fullWidth }
130:         ) + fadeOut(
131:             animationSpec = tween(ANIMATION_DURATION_MEDIUM)
132:         )
133:     }
134: 
135:     fun slideIntoContainerFromLeft(): EnterTransition {
136:         return slideInHorizontally(
137:             animationSpec = tween(
138:                 durationMillis = ANIMATION_DURATION_MEDIUM,
139:                 easing = LinearOutSlowInEasing
140:             ),
141:             initialOffsetX = { fullWidth -> -fullWidth }
142:         ) + fadeIn(
143:             animationSpec = tween(ANIMATION_DURATION_MEDIUM)
144:         )
145:     }
146: 
147:     fun slideOutOfContainerToRight(): ExitTransition {
148:         return slideOutHorizontally(
149:             animationSpec = tween(
150:                 durationMillis = ANIMATION_DURATION_MEDIUM,
151:                 easing = FastOutSlowInEasing
152:             ),
153:             targetOffsetX = { fullWidth -> fullWidth }
154:         ) + fadeOut(
155:             animationSpec = tween(ANIMATION_DURATION_MEDIUM)
156:         )
157:     }
158: 
159:     // Function to create staggered animation delay based on index
160:     fun calculateStaggeredDelay(index: Int, baseDelay: Int = 50): Int {
161:         return index * baseDelay
162:     }
163: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/AdvancedSearchPanel.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.AnimatedVisibility
  4: import androidx.compose.animation.expandVertically
  5: import androidx.compose.animation.fadeIn
  6: import androidx.compose.animation.fadeOut
  7: import androidx.compose.animation.shrinkVertically
  8: import androidx.compose.foundation.ExperimentalFoundationApi
  9: import androidx.compose.foundation.background
 10: import androidx.compose.foundation.border
 11: import androidx.compose.foundation.clickable
 12: import androidx.compose.foundation.layout.Arrangement
 13: import androidx.compose.foundation.layout.Box
 14: import androidx.compose.foundation.layout.Column
 15: import androidx.compose.foundation.layout.ExperimentalLayoutApi
 16: import androidx.compose.foundation.layout.FlowRow
 17: import androidx.compose.foundation.layout.Row
 18: import androidx.compose.foundation.layout.Spacer
 19: import androidx.compose.foundation.layout.fillMaxHeight
 20: import androidx.compose.foundation.layout.fillMaxWidth
 21: import androidx.compose.foundation.layout.height
 22: import androidx.compose.foundation.layout.padding
 23: import androidx.compose.foundation.layout.width
 24: import androidx.compose.foundation.rememberScrollState
 25: import androidx.compose.foundation.shape.RoundedCornerShape
 26: import androidx.compose.foundation.verticalScroll
 27: import androidx.compose.material.icons.Icons
 28: import androidx.compose.material.icons.filled.Add
 29: import androidx.compose.material.icons.filled.Clear
 30: import androidx.compose.material.icons.filled.Close
 31: import androidx.compose.material.icons.filled.ExpandLess
 32: import androidx.compose.material.icons.filled.ExpandMore
 33: import androidx.compose.material.icons.filled.FilterAlt
 34: import androidx.compose.material.icons.filled.FilterList
 35: import androidx.compose.material.icons.filled.Search
 36: import androidx.compose.material3.Button
 37: import androidx.compose.material3.ButtonDefaults
 38: import androidx.compose.material3.Checkbox
 39: import androidx.compose.material3.Divider
 40: import androidx.compose.material3.DropdownMenu
 41: import androidx.compose.material3.DropdownMenuItem
 42: import androidx.compose.material3.ExperimentalMaterial3Api
 43: import androidx.compose.material3.Icon
 44: import androidx.compose.material3.IconButton
 45: import androidx.compose.material3.MaterialTheme
 46: import androidx.compose.material3.OutlinedTextField
 47: import androidx.compose.material3.RangeSlider
 48: import androidx.compose.material3.Slider
 49: import androidx.compose.material3.SliderDefaults
 50: import androidx.compose.material3.Surface
 51: import androidx.compose.material3.Switch
 52: import androidx.compose.material3.Text
 53: import androidx.compose.material3.TextButton
 54: import androidx.compose.material3.TextFieldDefaults
 55: import androidx.compose.runtime.Composable
 56: import androidx.compose.runtime.getValue
 57: import androidx.compose.runtime.mutableStateListOf
 58: import androidx.compose.runtime.mutableStateOf
 59: import androidx.compose.runtime.remember
 60: import androidx.compose.runtime.setValue
 61: import androidx.compose.ui.Alignment
 62: import androidx.compose.ui.Modifier
 63: import androidx.compose.ui.draw.clip
 64: import androidx.compose.ui.graphics.Color
 65: import androidx.compose.ui.graphics.vector.ImageVector
 66: import androidx.compose.ui.text.font.FontWeight
 67: import androidx.compose.ui.unit.dp
 68: import androidx.compose.ui.unit.sp
 69: import androidx.compose.ui.window.Dialog
 70: import com.cocktailcraft.domain.model.Complexity
 71: import com.cocktailcraft.domain.model.PreparationTime
 72: import com.cocktailcraft.domain.model.SearchFilters
 73: import com.cocktailcraft.domain.model.TasteProfile
 74: import com.cocktailcraft.ui.theme.AppColors
 75: import com.cocktailcraft.ui.components.FilterChip
 76: 
 77: /**
 78:  * Section component for filter categories
 79:  */
 80: @Composable
 81: fun FilterSection(
 82:     title: String,
 83:     content: @Composable () -> Unit
 84: ) {
 85:     var expanded by remember { mutableStateOf(true) }
 86: 
 87:     Column(modifier = Modifier.padding(vertical = 8.dp)) {
 88:         Row(
 89:             modifier = Modifier
 90:                 .fillMaxWidth()
 91:                 .clickable { expanded = !expanded },
 92:             verticalAlignment = Alignment.CenterVertically
 93:         ) {
 94:             Text(
 95:                 text = title,
 96:                 style = MaterialTheme.typography.titleSmall,
 97:                 fontWeight = FontWeight.Bold
 98:             )
 99: 
100:             Spacer(modifier = Modifier.weight(1f))
101: 
102:             Icon(
103:                 imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
104:                 contentDescription = if (expanded) "Collapse" else "Expand",
105:                 tint = AppColors.TextSecondary
106:             )
107:         }
108: 
109:         AnimatedVisibility(
110:             visible = expanded,
111:             enter = fadeIn() + expandVertically(),
112:             exit = fadeOut() + shrinkVertically()
113:         ) {
114:             Column(
115:                 modifier = Modifier
116:                     .fillMaxWidth()
117:                     .padding(top = 8.dp, bottom = 4.dp)
118:             ) {
119:                 content()
120:             }
121:         }
122: 
123:         Divider(modifier = Modifier.padding(top = if (expanded) 8.dp else 16.dp))
124:     }
125: }
126: 
127: /**
128:  * Category selector component
129:  */
130: @Composable
131: fun CategorySelector(
132:     categories: List<String>,
133:     selectedCategory: String?,
134:     onCategorySelected: (String?) -> Unit
135: ) {
136:     var expanded by remember { mutableStateOf(false) }
137: 
138:     Column {
139:         Row(
140:             modifier = Modifier
141:                 .fillMaxWidth()
142:                 .border(
143:                     width = 1.dp,
144:                     color = AppColors.LightGray,
145:                     shape = RoundedCornerShape(8.dp)
146:                 )
147:                 .clip(RoundedCornerShape(8.dp))
148:                 .clickable { expanded = true }
149:                 .padding(16.dp),
150:             horizontalArrangement = Arrangement.SpaceBetween,
151:             verticalAlignment = Alignment.CenterVertically
152:         ) {
153:             Text(
154:                 text = selectedCategory ?: "Select a category",
155:                 style = MaterialTheme.typography.bodyMedium,
156:                 color = if (selectedCategory != null) AppColors.TextPrimary else AppColors.TextSecondary
157:             )
158: 
159:             Icon(
160:                 imageVector = Icons.Default.ExpandMore,
161:                 contentDescription = "Expand",
162:                 tint = AppColors.TextSecondary
163:             )
164:         }
165: 
166:         DropdownMenu(
167:             expanded = expanded,
168:             onDismissRequest = { expanded = false },
169:             modifier = Modifier.fillMaxWidth(0.9f)
170:         ) {
171:             // Add "All" option to clear the category filter
172:             DropdownMenuItem(
173:                 text = { Text("All Categories") },
174:                 onClick = {
175:                     onCategorySelected(null)
176:                     expanded = false
177:                 }
178:             )
179: 
180:             // Add all categories
181:             categories.forEach { category ->
182:                 DropdownMenuItem(
183:                     text = { Text(category) },
184:                     onClick = {
185:                         onCategorySelected(category)
186:                         expanded = false
187:                     }
188:                 )
189:             }
190:         }
191:     }
192: }
193: 
194: /**
195:  * Glass selector component
196:  */
197: @Composable
198: fun GlassSelector(
199:     glasses: List<String>,
200:     selectedGlass: String?,
201:     onGlassSelected: (String?) -> Unit
202: ) {
203:     var expanded by remember { mutableStateOf(false) }
204: 
205:     Column {
206:         Row(
207:             modifier = Modifier
208:                 .fillMaxWidth()
209:                 .border(
210:                     width = 1.dp,
211:                     color = AppColors.LightGray,
212:                     shape = RoundedCornerShape(8.dp)
213:                 )
214:                 .clip(RoundedCornerShape(8.dp))
215:                 .clickable { expanded = true }
216:                 .padding(16.dp),
217:             horizontalArrangement = Arrangement.SpaceBetween,
218:             verticalAlignment = Alignment.CenterVertically
219:         ) {
220:             Text(
221:                 text = selectedGlass ?: "Select a glass type",
222:                 style = MaterialTheme.typography.bodyMedium,
223:                 color = if (selectedGlass != null) AppColors.TextPrimary else AppColors.TextSecondary
224:             )
225: 
226:             Icon(
227:                 imageVector = Icons.Default.ExpandMore,
228:                 contentDescription = "Expand",
229:                 tint = AppColors.TextSecondary
230:             )
231:         }
232: 
233:         DropdownMenu(
234:             expanded = expanded,
235:             onDismissRequest = { expanded = false },
236:             modifier = Modifier.fillMaxWidth(0.9f)
237:         ) {
238:             // Add "All" option to clear the glass filter
239:             DropdownMenuItem(
240:                 text = { Text("All Glass Types") },
241:                 onClick = {
242:                     onGlassSelected(null)
243:                     expanded = false
244:                 }
245:             )
246: 
247:             // Add all glass types
248:             glasses.forEach { glass ->
249:                 DropdownMenuItem(
250:                     text = { Text(glass) },
251:                     onClick = {
252:                         onGlassSelected(glass)
253:                         expanded = false
254:                     }
255:                 )
256:             }
257:         }
258:     }
259: }
260: 
261: /**
262:  * Alcoholic filter content component
263:  */
264: @Composable
265: fun AlcoholicFilterContent(
266:     alcoholic: Boolean?,
267:     onAlcoholicChanged: (Boolean?) -> Unit
268: ) {
269:     Column {
270:         Row(
271:             verticalAlignment = Alignment.CenterVertically,
272:             modifier = Modifier.fillMaxWidth()
273:         ) {
274:             Text(
275:                 text = "Show only alcoholic drinks",
276:                 style = MaterialTheme.typography.bodyMedium
277:             )
278:             Spacer(modifier = Modifier.weight(1f))
279:             Switch(
280:                 checked = alcoholic == true,
281:                 onCheckedChange = { isChecked ->
282:                     onAlcoholicChanged(if (isChecked) true else null)
283:                 }
284:             )
285:         }
286: 
287:         Row(
288:             verticalAlignment = Alignment.CenterVertically,
289:             modifier = Modifier.fillMaxWidth()
290:         ) {
291:             Text(
292:                 text = "Show only non-alcoholic drinks",
293:                 style = MaterialTheme.typography.bodyMedium
294:             )
295:             Spacer(modifier = Modifier.weight(1f))
296:             Switch(
297:                 checked = alcoholic == false,
298:                 onCheckedChange = { isChecked ->
299:                     onAlcoholicChanged(if (isChecked) false else null)
300:                 }
301:             )
302:         }
303:     }
304: }
305: 
306: /**
307:  * Price range filter content component
308:  */
309: @Composable
310: fun PriceRangeFilterContent(
311:     priceRange: ClosedFloatingPointRange<Float>?,
312:     onPriceRangeChanged: (ClosedFloatingPointRange<Float>?) -> Unit
313: ) {
314:     var currentPriceRange by remember {
315:         mutableStateOf(priceRange ?: 5f..15f)
316:     }
317:     var isPriceFilterActive by remember {
318:         mutableStateOf(priceRange != null)
319:     }
320: 
321:     Column {
322:         Row(
323:             verticalAlignment = Alignment.CenterVertically,
324:             modifier = Modifier.fillMaxWidth()
325:         ) {
326:             Text(
327:                 text = "Filter by price",
328:                 style = MaterialTheme.typography.bodyMedium
329:             )
330:             Spacer(modifier = Modifier.weight(1f))
331:             Switch(
332:                 checked = isPriceFilterActive,
333:                 onCheckedChange = { isChecked ->
334:                     isPriceFilterActive = isChecked
335:                     onPriceRangeChanged(if (isChecked) currentPriceRange else null)
336:                 }
337:             )
338:         }
339: 
340:         if (isPriceFilterActive) {
341:             Column(modifier = Modifier.padding(vertical = 8.dp)) {
342:                 RangeSlider(
343:                     value = currentPriceRange,
344:                     onValueChange = { range ->
345:                         currentPriceRange = range
346:                         onPriceRangeChanged(range)
347:                     },
348:                     valueRange = 5f..30f,
349:                     steps = 25,
350:                     colors = SliderDefaults.colors(
351:                         thumbColor = AppColors.Primary,
352:                         activeTrackColor = AppColors.Primary
353:                     )
354:                 )
355: 
356:                 Row(
357:                     modifier = Modifier.fillMaxWidth(),
358:                     horizontalArrangement = Arrangement.SpaceBetween
359:                 ) {
360:                     Text(
361:                         text = "$${currentPriceRange.start.toInt()}",
362:                         style = MaterialTheme.typography.bodyMedium
363:                     )
364:                     Text(
365:                         text = "$${currentPriceRange.endInclusive.toInt()}",
366:                         style = MaterialTheme.typography.bodyMedium
367:                     )
368:                 }
369:             }
370:         }
371:     }
372: }
373: 
374: /**
375:  * Taste profile selector component
376:  */
377: @OptIn(ExperimentalLayoutApi::class)
378: @Composable
379: fun TasteProfileSelector(
380:     selectedProfile: TasteProfile?,
381:     onProfileSelected: (TasteProfile?) -> Unit
382: ) {
383:     Column {
384:         FlowRow(
385:             modifier = Modifier.fillMaxWidth(),
386:             horizontalArrangement = Arrangement.spacedBy(8.dp),
387:             verticalArrangement = Arrangement.spacedBy(8.dp)
388:         ) {
389:             // Add "All" option
390:             FilterChip(
391:                 selected = selectedProfile == null,
392:                 onClick = { onProfileSelected(null) },
393:                 label = "All"
394:             )
395: 
396:             // Add all taste profiles
397:             TasteProfile.values().forEach { profile ->
398:                 FilterChip(
399:                     selected = selectedProfile == profile,
400:                     onClick = { onProfileSelected(profile) },
401:                     label = profile.toString()
402:                 )
403:             }
404:         }
405:     }
406: }
407: 
408: /**
409:  * Complexity selector component
410:  */
411: @OptIn(ExperimentalLayoutApi::class)
412: @Composable
413: fun ComplexitySelector(
414:     selectedComplexity: Complexity?,
415:     onComplexitySelected: (Complexity?) -> Unit
416: ) {
417:     Column {
418:         FlowRow(
419:             modifier = Modifier.fillMaxWidth(),
420:             horizontalArrangement = Arrangement.spacedBy(8.dp),
421:             verticalArrangement = Arrangement.spacedBy(8.dp)
422:         ) {
423:             // Add "All" option
424:             FilterChip(
425:                 selected = selectedComplexity == null,
426:                 onClick = { onComplexitySelected(null) },
427:                 label = "All"
428:             )
429: 
430:             // Add all complexity levels
431:             Complexity.values().forEach { complexity ->
432:                 FilterChip(
433:                     selected = selectedComplexity == complexity,
434:                     onClick = { onComplexitySelected(complexity) },
435:                     label = complexity.toString()
436:                 )
437:             }
438:         }
439:     }
440: }
441: 
442: /**
443:  * Preparation time selector component
444:  */
445: @OptIn(ExperimentalLayoutApi::class)
446: @Composable
447: fun PrepTimeSelector(
448:     selectedPrepTime: PreparationTime?,
449:     onPrepTimeSelected: (PreparationTime?) -> Unit
450: ) {
451:     Column {
452:         FlowRow(
453:             modifier = Modifier.fillMaxWidth(),
454:             horizontalArrangement = Arrangement.spacedBy(8.dp),
455:             verticalArrangement = Arrangement.spacedBy(8.dp)
456:         ) {
457:             // Add "All" option
458:             FilterChip(
459:                 selected = selectedPrepTime == null,
460:                 onClick = { onPrepTimeSelected(null) },
461:                 label = "All"
462:             )
463: 
464:             // Add all preparation times
465:             PreparationTime.values().forEach { prepTime ->
466:                 FilterChip(
467:                     selected = selectedPrepTime == prepTime,
468:                     onClick = { onPrepTimeSelected(prepTime) },
469:                     label = prepTime.toString()
470:                 )
471:             }
472:         }
473:     }
474: }
475: 
476: /**
477:  * Ingredient selection dialog component
478:  */
479: @Composable
480: fun IngredientSelectionDialog(
481:     ingredients: List<String>,
482:     selectedIngredients: List<String>,
483:     dialogTitle: String,
484:     onDismiss: () -> Unit,
485:     onIngredientsSelected: (List<String>) -> Unit
486: ) {
487:     val selected = remember { mutableStateListOf<String>().apply { addAll(selectedIngredients) } }
488:     var searchQuery by remember { mutableStateOf("") }
489: 
490:     Dialog(onDismissRequest = onDismiss) {
491:         Surface(
492:             shape = RoundedCornerShape(16.dp),
493:             color = AppColors.Surface
494:         ) {
495:             Column(
496:                 modifier = Modifier
497:                     .padding(16.dp)
498:                     .fillMaxWidth()
499:             ) {
500:                 // Dialog title
501:                 Text(
502:                     text = dialogTitle,
503:                     style = MaterialTheme.typography.titleMedium,
504:                     fontWeight = FontWeight.Bold
505:                 )
506: 
507:                 Spacer(modifier = Modifier.height(16.dp))
508: 
509:                 // Search field
510:                 OutlinedTextField(
511:                     value = searchQuery,
512:                     onValueChange = { searchQuery = it },
513:                     modifier = Modifier.fillMaxWidth(),
514:                     placeholder = { Text("Search ingredients") },
515:                     leadingIcon = {
516:                         Icon(
517:                             Icons.Default.Search,
518:                             contentDescription = "Search",
519:                             tint = AppColors.Gray
520:                         )
521:                     },
522:                     singleLine = true,
523:                     colors = TextFieldDefaults.outlinedTextFieldColors(
524:                         focusedBorderColor = AppColors.Primary,
525:                         unfocusedBorderColor = Color.Gray,
526:                         cursorColor = AppColors.Primary,
527:                         focusedLeadingIconColor = AppColors.Primary,
528:                         unfocusedLeadingIconColor = Color.Gray,
529:                         containerColor = Color.White
530:                     )
531:                 )
532: 
533:                 Spacer(modifier = Modifier.height(16.dp))
534: 
535:                 // Filtered ingredients list
536:                 val filteredIngredients = ingredients.filter {
537:                     it.contains(searchQuery, ignoreCase = true)
538:                 }
539: 
540:                 Column(
541:                     modifier = Modifier
542:                         .weight(1f)
543:                         .verticalScroll(rememberScrollState())
544:                 ) {
545:                     filteredIngredients.forEach { ingredient ->
546:                         Row(
547:                             modifier = Modifier
548:                                 .fillMaxWidth()
549:                                 .padding(vertical = 4.dp)
550:                                 .clickable {
551:                                     if (selected.contains(ingredient)) {
552:                                         selected.remove(ingredient)
553:                                     } else {
554:                                         selected.add(ingredient)
555:                                     }
556:                                 },
557:                             verticalAlignment = Alignment.CenterVertically
558:                         ) {
559:                             Checkbox(
560:                                 checked = selected.contains(ingredient),
561:                                 onCheckedChange = { isChecked ->
562:                                     if (isChecked) {
563:                                         selected.add(ingredient)
564:                                     } else {
565:                                         selected.remove(ingredient)
566:                                     }
567:                                 }
568:                             )
569: 
570:                             Text(
571:                                 text = ingredient,
572:                                 style = MaterialTheme.typography.bodyMedium,
573:                                 modifier = Modifier.padding(start = 8.dp)
574:                             )
575:                         }
576:                     }
577:                 }
578: 
579:                 Spacer(modifier = Modifier.height(16.dp))
580: 
581:                 // Action buttons
582:                 Row(
583:                     modifier = Modifier.fillMaxWidth(),
584:                     horizontalArrangement = Arrangement.End
585:                 ) {
586:                     TextButton(onClick = onDismiss) {
587:                         Text("Cancel")
588:                     }
589: 
590:                     Spacer(modifier = Modifier.width(8.dp))
591: 
592:                     Button(
593:                         onClick = { onIngredientsSelected(selected.toList()) },
594:                         colors = ButtonDefaults.buttonColors(
595:                             containerColor = AppColors.Primary
596:                         )
597:                     ) {
598:                         Text("Apply")
599:                     }
600:                 }
601:             }
602:         }
603:     }
604: }
605: 
606: /**
607:  * Ingredient selector component
608:  */
609: @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
610: @Composable
611: fun IngredientSelector(
612:     ingredients: List<String>,
613:     selectedIngredients: List<String>,
614:     excludedIngredients: List<String>,
615:     onIngredientsChanged: (List<String>, List<String>) -> Unit
616: ) {
617:     var showIngredientDialog by remember { mutableStateOf(false) }
618:     var dialogMode by remember { mutableStateOf("include") }
619: 
620:     Column {
621:         // Selected ingredients
622:         if (selectedIngredients.isNotEmpty()) {
623:             Text(
624:                 text = "Must include:",
625:                 style = MaterialTheme.typography.bodyMedium,
626:                 fontWeight = FontWeight.Bold,
627:                 modifier = Modifier.padding(bottom = 4.dp)
628:             )
629: 
630:             FlowRow(
631:                 modifier = Modifier.fillMaxWidth(),
632:                 horizontalArrangement = Arrangement.spacedBy(8.dp),
633:                 verticalArrangement = Arrangement.spacedBy(8.dp)
634:             ) {
635:                 selectedIngredients.forEach { ingredient ->
636:                     FilterChip(
637:                         selected = true,
638:                         onClick = {
639:                             val updated = selectedIngredients.toMutableList().apply {
640:                                 remove(ingredient)
641:                             }
642:                             onIngredientsChanged(updated, excludedIngredients)
643:                         },
644:                         label = ingredient,
645:                         trailingIcon = Icons.Default.Clear
646:                     )
647:                 }
648:             }
649: 
650:             Spacer(modifier = Modifier.height(8.dp))
651:         }
652: 
653:         // Excluded ingredients
654:         if (excludedIngredients.isNotEmpty()) {
655:             Text(
656:                 text = "Must exclude:",
657:                 style = MaterialTheme.typography.bodyMedium,
658:                 fontWeight = FontWeight.Bold,
659:                 modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
660:             )
661: 
662:             FlowRow(
663:                 modifier = Modifier.fillMaxWidth(),
664:                 horizontalArrangement = Arrangement.spacedBy(8.dp),
665:                 verticalArrangement = Arrangement.spacedBy(8.dp)
666:             ) {
667:                 excludedIngredients.forEach { ingredient ->
668:                     FilterChip(
669:                         selected = true,
670:                         onClick = {
671:                             val updated = excludedIngredients.toMutableList().apply {
672:                                 remove(ingredient)
673:                             }
674:                             onIngredientsChanged(selectedIngredients, updated)
675:                         },
676:                         label = ingredient,
677:                         trailingIcon = Icons.Default.Clear,
678:                         selectedColor = Color.Red.copy(alpha = 0.1f),
679:                         selectedTextColor = Color.Red,
680:                         selectedIconColor = Color.Red
681:                     )
682:                 }
683:             }
684: 
685:             Spacer(modifier = Modifier.height(8.dp))
686:         }
687: 
688:         // Add ingredient buttons
689:         Row(
690:             modifier = Modifier.fillMaxWidth(),
691:             horizontalArrangement = Arrangement.spacedBy(8.dp)
692:         ) {
693:             Button(
694:                 onClick = {
695:                     dialogMode = "include"
696:                     showIngredientDialog = true
697:                 },
698:                 colors = ButtonDefaults.buttonColors(
699:                     containerColor = AppColors.Primary.copy(alpha = 0.1f)
700:                 ),
701:                 modifier = Modifier.weight(1f)
702:             ) {
703:                 Icon(
704:                     imageVector = Icons.Default.Add,
705:                     contentDescription = "Add Ingredient",
706:                     tint = AppColors.Primary
707:                 )
708:                 Spacer(modifier = Modifier.width(4.dp))
709:                 Text(
710:                     text = "Include",
711:                     color = AppColors.Primary
712:                 )
713:             }
714: 
715:             Button(
716:                 onClick = {
717:                     dialogMode = "exclude"
718:                     showIngredientDialog = true
719:                 },
720:                 colors = ButtonDefaults.buttonColors(
721:                     containerColor = Color.Red.copy(alpha = 0.1f)
722:                 ),
723:                 modifier = Modifier.weight(1f)
724:             ) {
725:                 Icon(
726:                     imageVector = Icons.Default.Add,
727:                     contentDescription = "Exclude Ingredient",
728:                     tint = Color.Red
729:                 )
730:                 Spacer(modifier = Modifier.width(4.dp))
731:                 Text(
732:                     text = "Exclude",
733:                     color = Color.Red
734:                 )
735:             }
736:         }
737: 
738:         // Ingredient selection dialog
739:         if (showIngredientDialog) {
740:             IngredientSelectionDialog(
741:                 ingredients = ingredients,
742:                 selectedIngredients = if (dialogMode == "include") selectedIngredients else excludedIngredients,
743:                 dialogTitle = if (dialogMode == "include") "Include Ingredients" else "Exclude Ingredients",
744:                 onDismiss = { showIngredientDialog = false },
745:                 onIngredientsSelected = { selected: List<String> ->
746:                     if (dialogMode == "include") {
747:                         onIngredientsChanged(selected, excludedIngredients)
748:                     } else {
749:                         onIngredientsChanged(selectedIngredients, selected)
750:                     }
751:                     showIngredientDialog = false
752:                 }
753:             )
754:         }
755:     }
756: }
757: 
758: /**
759:  * Advanced search panel that allows users to filter cocktails by various criteria
760:  */
761: @OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
762: @Composable
763: fun AdvancedSearchPanel(
764:     isVisible: Boolean,
765:     currentFilters: SearchFilters,
766:     categories: List<String>,
767:     ingredients: List<String>,
768:     glasses: List<String>,
769:     onApplyFilters: (SearchFilters) -> Unit,
770:     onClearFilters: () -> Unit,
771:     onDismiss: () -> Unit
772: ) {
773:     if (isVisible) {
774:         Dialog(onDismissRequest = onDismiss) {
775:             Surface(
776:                 modifier = Modifier
777:                     .fillMaxWidth()
778:                     .fillMaxHeight(0.9f),
779:                 shape = RoundedCornerShape(16.dp),
780:                 color = AppColors.Surface,
781:                 shadowElevation = 8.dp
782:             ) {
783:                 Column(
784:                     modifier = Modifier
785:                         .fillMaxWidth()
786:                         .padding(16.dp)
787:                         .verticalScroll(rememberScrollState())
788:                 ) {
789:                 // Header with title and close button
790:                 Row(
791:                     modifier = Modifier.fillMaxWidth(),
792:                     horizontalArrangement = Arrangement.SpaceBetween,
793:                     verticalAlignment = Alignment.CenterVertically
794:                 ) {
795:                     Row(
796:                         verticalAlignment = Alignment.CenterVertically
797:                     ) {
798:                         Icon(
799:                             imageVector = Icons.Default.FilterList,
800:                             contentDescription = "Advanced Search",
801:                             tint = AppColors.Primary
802:                         )
803:                         Spacer(modifier = Modifier.width(8.dp))
804:                         Text(
805:                             text = "Advanced Search",
806:                             style = MaterialTheme.typography.titleMedium,
807:                             fontWeight = FontWeight.Bold
808:                         )
809:                     }
810: 
811:                     IconButton(onClick = onDismiss) {
812:                         Icon(
813:                             imageVector = Icons.Default.Close,
814:                             contentDescription = "Close",
815:                             tint = AppColors.TextSecondary
816:                         )
817:                     }
818:                 }
819: 
820:                 Divider(modifier = Modifier.padding(vertical = 8.dp))
821: 
822:                 // Filter sections
823:                 var filters by remember { mutableStateOf(currentFilters) }
824: 
825:                 // Category filter
826:                 FilterSection(title = "Category") {
827:                     CategorySelector(
828:                         categories = categories,
829:                         selectedCategory = filters.category,
830:                         onCategorySelected = { category: String? ->
831:                             filters = filters.copy(category = category)
832:                         }
833:                     )
834:                 }
835: 
836:                 // Ingredient filter
837:                 FilterSection(title = "Ingredients") {
838:                     IngredientSelector(
839:                         ingredients = ingredients,
840:                         selectedIngredients = filters.ingredients,
841:                         excludedIngredients = filters.excludeIngredients,
842:                         onIngredientsChanged = { included: List<String>, excluded: List<String> ->
843:                             filters = filters.copy(
844:                                 ingredients = included,
845:                                 excludeIngredients = excluded
846:                             )
847:                         }
848:                     )
849:                 }
850: 
851:                 // Alcoholic filter
852:                 FilterSection(title = "Alcoholic") {
853:                     AlcoholicFilterContent(
854:                         alcoholic = filters.alcoholic,
855:                         onAlcoholicChanged = { alcoholicValue ->
856:                             filters = filters.copy(alcoholic = alcoholicValue)
857:                         }
858:                     )
859:                 }
860: 
861:                 // Glass filter
862:                 FilterSection(title = "Glass Type") {
863:                     GlassSelector(
864:                         glasses = glasses,
865:                         selectedGlass = filters.glass,
866:                         onGlassSelected = { glass: String? ->
867:                             filters = filters.copy(glass = glass)
868:                         }
869:                     )
870:                 }
871: 
872:                 // Price range filter
873:                 FilterSection(title = "Price Range") {
874:                     PriceRangeFilterContent(
875:                         priceRange = filters.priceRange,
876:                         onPriceRangeChanged = { newPriceRange ->
877:                             filters = filters.copy(priceRange = newPriceRange)
878:                         }
879:                     )
880:                 }
881: 
882:                 // Taste profile filter
883:                 FilterSection(title = "Taste Profile") {
884:                     TasteProfileSelector(
885:                         selectedProfile = filters.tasteProfile,
886:                         onProfileSelected = { profile: TasteProfile? ->
887:                             filters = filters.copy(tasteProfile = profile)
888:                         }
889:                     )
890:                 }
891: 
892:                 // Complexity filter
893:                 FilterSection(title = "Complexity") {
894:                     ComplexitySelector(
895:                         selectedComplexity = filters.complexity,
896:                         onComplexitySelected = { complexity: Complexity? ->
897:                             filters = filters.copy(complexity = complexity)
898:                         }
899:                     )
900:                 }
901: 
902:                 // Preparation time filter
903:                 FilterSection(title = "Preparation Time") {
904:                     PrepTimeSelector(
905:                         selectedPrepTime = filters.preparationTime,
906:                         onPrepTimeSelected = { prepTime: PreparationTime? ->
907:                             filters = filters.copy(preparationTime = prepTime)
908:                         }
909:                     )
910:                 }
911: 
912:                 Spacer(modifier = Modifier.height(16.dp))
913: 
914:                 // Action buttons
915:                 Row(
916:                     modifier = Modifier.fillMaxWidth(),
917:                     horizontalArrangement = Arrangement.End
918:                 ) {
919:                     TextButton(
920:                         onClick = {
921:                             onClearFilters()
922:                             filters = SearchFilters(query = filters.query)
923:                         }
924:                     ) {
925:                         Text("Clear All")
926:                     }
927: 
928:                     Spacer(modifier = Modifier.width(8.dp))
929: 
930:                     Button(
931:                         onClick = { onApplyFilters(filters) },
932:                         colors = ButtonDefaults.buttonColors(
933:                             containerColor = AppColors.Primary
934:                         )
935:                     ) {
936:                         Text("Apply Filters")
937:                     }
938:                 }
939:             }
940:         }
941:     }
942: }}
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/AnimatedButtons.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.core.animateFloatAsState
  4: import androidx.compose.animation.core.spring
  5: import androidx.compose.foundation.interaction.MutableInteractionSource
  6: import androidx.compose.foundation.interaction.collectIsPressedAsState
  7: import androidx.compose.foundation.layout.PaddingValues
  8: import androidx.compose.foundation.layout.size
  9: import androidx.compose.material.icons.Icons
 10: import androidx.compose.material.icons.filled.Add
 11: import androidx.compose.material3.Button
 12: import androidx.compose.material3.ButtonDefaults
 13: import androidx.compose.material3.FloatingActionButton
 14: import androidx.compose.material3.Icon
 15: import androidx.compose.material3.IconButton
 16: import androidx.compose.material3.Text
 17: import androidx.compose.runtime.Composable
 18: import androidx.compose.runtime.getValue
 19: import androidx.compose.runtime.remember
 20: import androidx.compose.ui.Modifier
 21: import androidx.compose.ui.draw.scale
 22: import androidx.compose.ui.graphics.Color
 23: import androidx.compose.ui.graphics.vector.ImageVector
 24: import androidx.compose.ui.unit.dp
 25: import com.cocktailcraft.ui.animation.AnimationUtils
 26: import com.cocktailcraft.ui.theme.AppColors
 27: 
 28: /**
 29:  * An animated button that scales when pressed
 30:  */
 31: @Composable
 32: fun AnimatedButton(
 33:     onClick: () -> Unit,
 34:     modifier: Modifier = Modifier,
 35:     enabled: Boolean = true,
 36:     contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
 37:     content: @Composable () -> Unit
 38: ) {
 39:     val interactionSource = remember { MutableInteractionSource() }
 40:     val isPressed by interactionSource.collectIsPressedAsState()
 41:     
 42:     val scale by animateFloatAsState(
 43:         targetValue = if (isPressed) 0.95f else 1f,
 44:         animationSpec = spring(
 45:             dampingRatio = AnimationUtils.SPRING_DAMPENING_MEDIUM_BOUNCY,
 46:             stiffness = AnimationUtils.SPRING_STIFFNESS_MEDIUM
 47:         ),
 48:         label = "button_scale"
 49:     )
 50:     
 51:     Button(
 52:         onClick = onClick,
 53:         modifier = modifier.scale(scale),
 54:         enabled = enabled,
 55:         contentPadding = contentPadding,
 56:         interactionSource = interactionSource
 57:     ) {
 58:         content()
 59:     }
 60: }
 61: 
 62: /**
 63:  * An animated text button with scale animation
 64:  */
 65: @Composable
 66: fun AnimatedTextButton(
 67:     text: String,
 68:     onClick: () -> Unit,
 69:     modifier: Modifier = Modifier,
 70:     enabled: Boolean = true
 71: ) {
 72:     AnimatedButton(
 73:         onClick = onClick,
 74:         modifier = modifier,
 75:         enabled = enabled
 76:     ) {
 77:         Text(text = text)
 78:     }
 79: }
 80: 
 81: /**
 82:  * An animated icon button with scale and rotation effects
 83:  */
 84: @Composable
 85: fun AnimatedIconButton(
 86:     onClick: () -> Unit,
 87:     icon: ImageVector,
 88:     contentDescription: String?,
 89:     modifier: Modifier = Modifier,
 90:     tint: Color = AppColors.Primary,
 91:     enabled: Boolean = true
 92: ) {
 93:     val interactionSource = remember { MutableInteractionSource() }
 94:     val isPressed by interactionSource.collectIsPressedAsState()
 95:     
 96:     val scale by animateFloatAsState(
 97:         targetValue = if (isPressed) 0.85f else 1f,
 98:         animationSpec = spring(
 99:             dampingRatio = AnimationUtils.SPRING_DAMPENING_MEDIUM_BOUNCY,
100:             stiffness = AnimationUtils.SPRING_STIFFNESS_MEDIUM
101:         ),
102:         label = "icon_button_scale"
103:     )
104:     
105:     IconButton(
106:         onClick = onClick,
107:         modifier = modifier,
108:         enabled = enabled,
109:         interactionSource = interactionSource
110:     ) {
111:         Icon(
112:             imageVector = icon,
113:             contentDescription = contentDescription,
114:             tint = if (enabled) tint else AppColors.Gray,
115:             modifier = Modifier.scale(scale)
116:         )
117:     }
118: }
119: 
120: /**
121:  * An animated floating action button with scale effect
122:  */
123: @Composable
124: fun AnimatedFloatingActionButton(
125:     onClick: () -> Unit,
126:     modifier: Modifier = Modifier,
127:     icon: ImageVector = Icons.Default.Add,
128:     contentDescription: String? = null
129: ) {
130:     val interactionSource = remember { MutableInteractionSource() }
131:     val isPressed by interactionSource.collectIsPressedAsState()
132:     
133:     val scale by animateFloatAsState(
134:         targetValue = if (isPressed) 0.92f else 1f,
135:         animationSpec = spring(
136:             dampingRatio = AnimationUtils.SPRING_DAMPENING_MEDIUM_BOUNCY,
137:             stiffness = AnimationUtils.SPRING_STIFFNESS_MEDIUM
138:         ),
139:         label = "fab_scale"
140:     )
141:     
142:     FloatingActionButton(
143:         onClick = onClick,
144:         modifier = modifier.scale(scale),
145:         interactionSource = interactionSource
146:     ) {
147:         Icon(
148:             imageVector = icon,
149:             contentDescription = contentDescription,
150:             modifier = Modifier.size(24.dp)
151:         )
152:     }
153: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/AnimatedCocktailItem.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.AnimatedVisibility
  4: import androidx.compose.animation.core.MutableTransitionState
  5: import androidx.compose.animation.core.Spring
  6: import androidx.compose.animation.core.animateFloatAsState
  7: import androidx.compose.animation.core.spring
  8: import androidx.compose.animation.core.tween
  9: import androidx.compose.animation.fadeIn
 10: import androidx.compose.animation.fadeOut
 11: import androidx.compose.animation.scaleIn
 12: import androidx.compose.animation.slideInVertically
 13: import androidx.compose.foundation.background
 14: import androidx.compose.foundation.clickable
 15: import androidx.compose.foundation.layout.Box
 16: import androidx.compose.foundation.layout.Column
 17: import androidx.compose.foundation.layout.Row
 18: import androidx.compose.foundation.layout.Spacer
 19: import androidx.compose.foundation.layout.fillMaxSize
 20: import androidx.compose.foundation.layout.fillMaxWidth
 21: import androidx.compose.foundation.layout.height
 22: import androidx.compose.foundation.layout.padding
 23: import androidx.compose.foundation.layout.size
 24: import androidx.compose.foundation.layout.width
 25: import androidx.compose.foundation.shape.RoundedCornerShape
 26: import androidx.compose.material.icons.Icons
 27: import androidx.compose.material.icons.filled.Favorite
 28: import androidx.compose.material.icons.filled.ShoppingCart
 29: import androidx.compose.material.icons.outlined.FavoriteBorder
 30: import androidx.compose.material3.Card
 31: import androidx.compose.material3.CardDefaults
 32: import androidx.compose.material3.Text
 33: import androidx.compose.runtime.Composable
 34: import androidx.compose.runtime.LaunchedEffect
 35: import androidx.compose.runtime.getValue
 36: import androidx.compose.runtime.mutableStateOf
 37: import androidx.compose.runtime.remember
 38: import androidx.compose.runtime.setValue
 39: import androidx.compose.ui.Alignment
 40: import androidx.compose.ui.Modifier
 41: import androidx.compose.ui.draw.clip
 42: import androidx.compose.ui.draw.scale
 43: import androidx.compose.ui.graphics.Color
 44: import androidx.compose.ui.layout.ContentScale
 45: import androidx.compose.ui.text.font.FontStyle
 46: import androidx.compose.ui.text.font.FontWeight
 47: import androidx.compose.ui.text.style.TextAlign
 48: import androidx.compose.ui.text.style.TextOverflow
 49: import androidx.compose.ui.unit.dp
 50: import androidx.compose.ui.unit.sp
 51: import com.cocktailcraft.domain.model.Cocktail
 52: import com.cocktailcraft.ui.animation.AnimationUtils
 53: import com.cocktailcraft.ui.theme.AppColors
 54: 
 55: /**
 56:  * An enhanced version of CocktailItem with animations
 57:  */
 58: @Composable
 59: fun AnimatedCocktailItem(
 60:     cocktail: Cocktail,
 61:     onClick: () -> Unit,
 62:     onAddToCart: (Cocktail) -> Unit,
 63:     isFavorite: Boolean = false,
 64:     onToggleFavorite: (Cocktail) -> Unit = {},
 65:     modifier: Modifier = Modifier
 66: ) {
 67:     // Animation states
 68:     var isHovered by remember { mutableStateOf(false) }
 69: 
 70:     // Scale animation for hover effect
 71:     val scale by animateFloatAsState(
 72:         targetValue = if (isHovered) 1.03f else 1f,
 73:         animationSpec = spring(
 74:             dampingRatio = Spring.DampingRatioMediumBouncy,
 75:             stiffness = Spring.StiffnessLow
 76:         ),
 77:         label = "hover_scale"
 78:     )
 79: 
 80:     Card(
 81:         modifier = modifier
 82:             .fillMaxWidth()
 83:             .scale(scale)
 84:             .clickable(
 85:                 onClick = onClick,
 86:                 onClickLabel = "View ${cocktail.name} details"
 87:             ),
 88:         shape = RoundedCornerShape(12.dp),
 89:         colors = CardDefaults.cardColors(
 90:             containerColor = AppColors.Surface
 91:         ),
 92:         elevation = CardDefaults.cardElevation(
 93:             defaultElevation = 2.dp,
 94:             pressedElevation = 4.dp,
 95:             hoveredElevation = 3.dp
 96:         )
 97:     ) {
 98:         CocktailItemContent(
 99:             cocktail = cocktail,
100:             isFavorite = isFavorite,
101:             onAddToCart = onAddToCart,
102:             onToggleFavorite = onToggleFavorite
103:         )
104:     }
105: }
106: 
107: /**
108:  * Extracted content of the cocktail item to avoid duplication
109:  */
110: @Composable
111: fun CocktailItemContent(
112:     cocktail: Cocktail,
113:     isFavorite: Boolean,
114:     onAddToCart: (Cocktail) -> Unit,
115:     onToggleFavorite: (Cocktail) -> Unit
116: ) {
117:     Row(
118:         modifier = Modifier
119:             .fillMaxWidth()
120:             .padding(12.dp),
121:         verticalAlignment = Alignment.CenterVertically
122:     ) {
123:         // Cocktail image with placeholder
124:         Box(
125:             modifier = Modifier
126:                 .size(100.dp)
127:                 .clip(RoundedCornerShape(8.dp))
128:                 .background(AppColors.LightGray)
129:         ) {
130:             // Use our optimized image component
131:             OptimizedImage(
132:                 url = cocktail.imageUrl,
133:                 contentDescription = cocktail.name,
134:                 modifier = Modifier.fillMaxSize(),
135:                 contentScale = ContentScale.Crop,
136:                 targetSize = 200, // Target size for better memory usage
137:                 showLoadingIndicator = false // Disable loading indicator for better performance
138:             )
139: 
140:             // Stock badge for out of stock items
141:             if (cocktail.stockCount <= 0) {
142:                 Box(
143:                     modifier = Modifier
144:                         .fillMaxSize()
145:                         .background(Color.Black.copy(alpha = 0.5f)),
146:                     contentAlignment = Alignment.Center
147:                 ) {
148:                     Text(
149:                         text = "Out of Stock",
150:                         color = Color.White,
151:                         fontSize = 12.sp,
152:                         fontWeight = FontWeight.Bold,
153:                         textAlign = TextAlign.Center,
154:                         modifier = Modifier.padding(4.dp)
155:                     )
156:                 }
157:             }
158:         }
159: 
160:         Spacer(modifier = Modifier.width(12.dp))
161: 
162:         // Cocktail details
163:         Column(
164:             modifier = Modifier.weight(1f)
165:         ) {
166:             Text(
167:                 text = cocktail.name,
168:                 fontWeight = FontWeight.Bold,
169:                 fontSize = 16.sp,
170:                 color = AppColors.TextPrimary,
171:                 maxLines = 1,
172:                 overflow = TextOverflow.Ellipsis
173:             )
174: 
175:             Spacer(modifier = Modifier.height(4.dp))
176: 
177:             // Display alcoholic info with category info
178:             Text(
179:                 text = buildString {
180:                     append(cocktail.alcoholic ?: "Unknown")
181:                     cocktail.category?.let {
182:                         append(" • ")
183:                         append(it)
184:                     }
185:                 },
186:                 fontSize = 14.sp,
187:                 color = AppColors.TextSecondary,
188:                 maxLines = 1,
189:                 overflow = TextOverflow.Ellipsis
190:             )
191: 
192:             Spacer(modifier = Modifier.height(4.dp))
193: 
194:             // Use safe call for ingredients that might be null or empty
195:             Text(
196:                 text = if (cocktail.ingredients.isNotEmpty()) {
197:                     val firstIngredient = cocktail.ingredients.first().name
198:                     // Check if it's a placeholder
199:                     if (firstIngredient == "Tap to view ingredients") {
200:                         firstIngredient
201:                     } else {
202:                         // Join first 2 ingredients with safe operators
203:                         cocktail.ingredients.take(2).joinToString(", ") { it.name }
204:                             .let { if (cocktail.ingredients.size > 2) "$it..." else it }
205:                     }
206:                 } else {
207:                     "Tap to view ingredients"
208:                 },
209:                 fontSize = 12.sp,
210:                 color = AppColors.TextSecondary,
211:                 fontStyle = FontStyle.Italic,
212:                 maxLines = 1,
213:                 overflow = TextOverflow.Ellipsis
214:             )
215: 
216:             Spacer(modifier = Modifier.height(8.dp))
217: 
218:             Row(
219:                 verticalAlignment = Alignment.CenterVertically,
220:                 modifier = Modifier.fillMaxWidth()
221:             ) {
222:                 // Price
223:                 Text(
224:                     text = "$${String.format("%.2f", cocktail.price)}",
225:                     fontWeight = FontWeight.Bold,
226:                     fontSize = 16.sp,
227:                     color = AppColors.Primary
228:                 )
229: 
230:                 Spacer(modifier = Modifier.weight(1f))
231: 
232:                 // Favorite button with animation
233:                 AnimatedIconButton(
234:                     onClick = { onToggleFavorite(cocktail) },
235:                     icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
236:                     contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
237:                     tint = if (isFavorite) AppColors.Secondary else AppColors.Gray
238:                 )
239: 
240:                 // Add to cart button with animation
241:                 AnimatedIconButton(
242:                     onClick = { onAddToCart(cocktail) },
243:                     icon = Icons.Default.ShoppingCart,
244:                     contentDescription = "Add to Cart",
245:                     tint = if (cocktail.stockCount > 0) AppColors.Primary else AppColors.Gray,
246:                     enabled = cocktail.stockCount > 0
247:                 )
248:             }
249:         }
250:     }
251: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/AnimatedCocktailList.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.core.FastOutSlowInEasing
  4: import androidx.compose.animation.core.animateFloatAsState
  5: import androidx.compose.animation.core.tween
  6: import androidx.compose.foundation.layout.Arrangement
  7: import androidx.compose.foundation.layout.Box
  8: import androidx.compose.foundation.layout.PaddingValues
  9: import androidx.compose.foundation.layout.fillMaxSize
 10: import androidx.compose.foundation.layout.fillMaxWidth
 11: import androidx.compose.foundation.layout.offset
 12: import androidx.compose.foundation.layout.padding
 13: import androidx.compose.foundation.layout.size
 14: import androidx.compose.foundation.lazy.LazyColumn
 15: import androidx.compose.foundation.lazy.itemsIndexed
 16: import androidx.compose.foundation.lazy.rememberLazyListState
 17: import androidx.compose.material3.CircularProgressIndicator
 18: import androidx.compose.material3.Text
 19: import androidx.compose.runtime.Composable
 20: import androidx.compose.runtime.LaunchedEffect
 21: import androidx.compose.runtime.getValue
 22: import androidx.compose.runtime.mutableStateOf
 23: import androidx.compose.runtime.remember
 24: import androidx.compose.runtime.setValue
 25: import androidx.compose.ui.Alignment
 26: import androidx.compose.ui.Modifier
 27: import androidx.compose.ui.draw.alpha
 28: import androidx.compose.ui.text.font.FontWeight
 29: import androidx.compose.ui.text.style.TextAlign
 30: import androidx.compose.ui.unit.dp
 31: import androidx.compose.ui.unit.sp
 32: import com.cocktailcraft.domain.model.Cocktail
 33: import com.cocktailcraft.ui.theme.AppColors
 34: import com.cocktailcraft.util.ListOptimizations.OnBottomReached
 35: import kotlinx.coroutines.delay
 36: 
 37: /**
 38:  * A reusable component for displaying a list of cocktails with animations.
 39:  *
 40:  * @param cocktails The list of cocktails to display
 41:  * @param isSearchActive Whether search is currently active
 42:  * @param selectedCategory The currently selected category, if any
 43:  * @param isLoadingMore Whether more items are being loaded
 44:  * @param hasMoreData Whether there is more data to load
 45:  * @param favorites The list of favorite cocktails
 46:  * @param onCocktailClick Callback when a cocktail is clicked
 47:  * @param onAddToCart Callback when a cocktail is added to the cart
 48:  * @param onToggleFavorite Callback when a cocktail's favorite status is toggled
 49:  * @param onLoadMore Callback to load more cocktails
 50:  * @param modifier The modifier for the component
 51:  * @param headerFontSize The font size of the header
 52:  * @param itemSpacing The spacing between items
 53:  * @param contentPadding The padding for the content
 54:  * @param loadingIndicatorSize The size of the loading indicator
 55:  * @param endOfListMessageFontSize The font size of the end of list message
 56:  */
 57: @Composable
 58: fun AnimatedCocktailList(
 59:     cocktails: List<Cocktail>,
 60:     isSearchActive: Boolean,
 61:     selectedCategory: String?,
 62:     isLoadingMore: Boolean,
 63:     hasMoreData: Boolean,
 64:     favorites: List<Cocktail>,
 65:     onCocktailClick: (Cocktail) -> Unit,
 66:     onAddToCart: (Cocktail) -> Unit,
 67:     onToggleFavorite: (Cocktail) -> Unit,
 68:     onLoadMore: () -> Unit,
 69:     modifier: Modifier = Modifier,
 70:     headerFontSize: Int = 18,
 71:     itemSpacing: Int = 16,
 72:     contentPadding: PaddingValues = PaddingValues(16.dp),
 73:     loadingIndicatorSize: Int = 32,
 74:     endOfListMessageFontSize: Int = 14
 75: ) {
 76:     // Remember scroll state for optimizations
 77:     val listState = rememberLazyListState()
 78: 
 79:     // Detect when we're near the bottom to load more items
 80:     listState.OnBottomReached(buffer = 5) {
 81:         // Only load more if not already loading and not searching
 82:         if (!isLoadingMore && !isSearchActive) {
 83:             onLoadMore()
 84:         }
 85:     }
 86: 
 87:     // Track which items have been loaded for batched animation
 88:     val visibleItemsCount = remember { mutableStateOf(0) }
 89: 
 90:     // Update visible items based on scroll position
 91:     LaunchedEffect(listState.firstVisibleItemIndex, cocktails.size) {
 92:         // Calculate how many items should be visible based on current scroll position
 93:         // We add a buffer of 9 items (3 batches of 3) to ensure smooth scrolling
 94:         val targetVisible = minOf(
 95:             cocktails.size,
 96:             listState.firstVisibleItemIndex + 12 // Current visible + 3 batches ahead
 97:         )
 98: 
 99:         // If we need to show more items, increase the count in batches of 3
100:         if (targetVisible > visibleItemsCount.value) {
101:             // Animate in batches of 3 items
102:             val batchSize = 3
103:             val currentBatch = visibleItemsCount.value / batchSize
104:             val targetBatch = targetVisible / batchSize
105: 
106:             // For each batch we need to show
107:             for (batch in currentBatch until targetBatch) {
108:                 val newCount = minOf((batch + 1) * batchSize, cocktails.size)
109:                 visibleItemsCount.value = newCount
110:                 delay(100) // Small delay between batches for staggered effect
111:             }
112: 
113:             // Make sure we show any remaining items
114:             visibleItemsCount.value = targetVisible
115:         }
116:     }
117: 
118:     // Main content with optimized list rendering
119:     LazyColumn(
120:         state = listState,
121:         modifier = modifier.fillMaxSize(),
122:         verticalArrangement = Arrangement.spacedBy(itemSpacing.dp),
123:         contentPadding = contentPadding
124:     ) {
125:         // Header item
126:         item(key = "header") {
127:             Text(
128:                 text = when {
129:                     isSearchActive -> "Search Results"
130:                     selectedCategory != null -> "$selectedCategory Cocktails"
131:                     else -> "All Cocktails"
132:                 },
133:                 fontSize = headerFontSize.sp,
134:                 fontWeight = FontWeight.Bold,
135:                 color = AppColors.TextPrimary,
136:                 modifier = Modifier.padding(bottom = 8.dp)
137:             )
138:         }
139: 
140:         // Cocktail items with animations
141:         itemsIndexed(
142:             items = cocktails,
143:             key = { index, cocktail -> "cocktail_${index}_${cocktail.id}" }
144:         ) { index, cocktail ->
145:             // Only show items that have been loaded in our batched approach
146:             val isVisible = index < visibleItemsCount.value
147: 
148:             // Calculate the animation index based on the batch
149:             // This ensures items within a batch animate together
150:             val batchSize = 3
151:             val batchIndex = index % batchSize
152: 
153:             // If the item should be visible, show it with animation
154:             if (isVisible) {
155:                 // Calculate animation parameters based on batch
156:                 val delayMillis = batchIndex * 100
157:                 val animationDuration = 300
158: 
159:                 // Animate alpha and offset for entry
160:                 val animatedAlpha by animateFloatAsState(
161:                     targetValue = 1f,
162:                     animationSpec = tween(
163:                         durationMillis = animationDuration,
164:                         delayMillis = delayMillis
165:                     ),
166:                     label = "item_alpha_$index"
167:                 )
168: 
169:                 val animatedOffset by animateFloatAsState(
170:                     targetValue = 0f,
171:                     animationSpec = tween(
172:                         durationMillis = animationDuration,
173:                         delayMillis = delayMillis
174:                     ),
175:                     label = "item_offset_$index"
176:                 )
177: 
178:                 Box(
179:                     modifier = Modifier
180:                         .alpha(animatedAlpha)
181:                         .offset(y = animatedOffset.dp)
182:                 ) {
183:                     AnimatedCocktailItem(
184:                         cocktail = cocktail,
185:                         onClick = {
186:                             onCocktailClick(cocktail)
187:                         },
188:                         onAddToCart = {
189:                             onAddToCart(it)
190:                         },
191:                         isFavorite = favorites.any { it.id == cocktail.id },
192:                         onToggleFavorite = { cocktailToToggle ->
193:                             onToggleFavorite(cocktailToToggle)
194:                         }
195:                     )
196:                 }
197:             }
198:         }
199: 
200:         // Show loading indicator at the bottom when loading more items
201:         if (isLoadingMore) {
202:             item(key = "loading_more") {
203:                 Box(
204:                     modifier = Modifier
205:                         .fillMaxWidth()
206:                         .padding(16.dp),
207:                     contentAlignment = Alignment.Center
208:                 ) {
209:                     // Use a simple fade-in effect for the loading indicator
210:                     val animatedAlpha by animateFloatAsState(
211:                         targetValue = 1f,
212:                         animationSpec = tween(durationMillis = 300),
213:                         label = "loading_alpha"
214:                     )
215: 
216:                     CircularProgressIndicator(
217:                         modifier = Modifier
218:                             .size(loadingIndicatorSize.dp)
219:                             .alpha(animatedAlpha),
220:                         color = AppColors.Primary
221:                     )
222:                 }
223:             }
224:         }
225: 
226:         // Show end of list message when no more data
227:         if (!hasMoreData && !isSearchActive && cocktails.isNotEmpty()) {
228:             item(key = "end_of_list") {
229:                 // Use a simple animation for the end of list message
230:                 val animatedOffset by animateFloatAsState(
231:                     targetValue = 0f,
232:                     animationSpec = tween(
233:                         durationMillis = 500,
234:                         delayMillis = 300,
235:                         easing = FastOutSlowInEasing
236:                     ),
237:                     label = "end_of_list_offset"
238:                 )
239: 
240:                 val animatedAlpha by animateFloatAsState(
241:                     targetValue = 1f,
242:                     animationSpec = tween(
243:                         durationMillis = 500,
244:                         delayMillis = 300
245:                     ),
246:                     label = "end_of_list_alpha"
247:                 )
248: 
249:                 Text(
250:                     text = "You've reached the end of the list",
251:                     modifier = Modifier
252:                         .fillMaxWidth()
253:                         .padding(16.dp)
254:                         .offset(y = animatedOffset.dp)
255:                         .alpha(animatedAlpha),
256:                     textAlign = TextAlign.Center,
257:                     color = AppColors.TextSecondary,
258:                     fontSize = endOfListMessageFontSize.sp
259:                 )
260:             }
261:         }
262:     }
263: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/AnimatedThemeSwitch.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.animateColorAsState
  4: import androidx.compose.animation.core.FastOutSlowInEasing
  5: import androidx.compose.animation.core.LinearOutSlowInEasing
  6: import androidx.compose.animation.core.Spring
  7: import androidx.compose.animation.core.animateDpAsState
  8: import androidx.compose.animation.core.animateFloat
  9: import androidx.compose.animation.core.spring
 10: import androidx.compose.animation.core.tween
 11: import androidx.compose.animation.core.updateTransition
 12: import androidx.compose.foundation.background
 13: import androidx.compose.foundation.border
 14: import androidx.compose.foundation.clickable
 15: import androidx.compose.foundation.interaction.MutableInteractionSource
 16: import androidx.compose.foundation.layout.Box
 17: import androidx.compose.foundation.layout.Row
 18: import androidx.compose.foundation.layout.Spacer
 19: import androidx.compose.foundation.layout.fillMaxHeight
 20: import androidx.compose.foundation.layout.height
 21: import androidx.compose.foundation.layout.offset
 22: import androidx.compose.foundation.layout.padding
 23: import androidx.compose.foundation.layout.size
 24: import androidx.compose.foundation.layout.width
 25: import androidx.compose.foundation.shape.CircleShape
 26: import androidx.compose.foundation.shape.RoundedCornerShape
 27: import androidx.compose.material.icons.Icons
 28: import androidx.compose.material.icons.filled.DarkMode
 29: import androidx.compose.material.icons.filled.LightMode
 30: import androidx.compose.material3.Icon
 31: import androidx.compose.material3.MaterialTheme
 32: import androidx.compose.material3.Surface
 33: import androidx.compose.material3.Text
 34: import androidx.compose.runtime.Composable
 35: import androidx.compose.runtime.getValue
 36: import androidx.compose.runtime.remember
 37: import androidx.compose.ui.Alignment
 38: import androidx.compose.ui.Modifier
 39: import androidx.compose.ui.draw.alpha
 40: import androidx.compose.ui.draw.clip
 41: import androidx.compose.ui.draw.scale
 42: import androidx.compose.ui.graphics.Color
 43: import androidx.compose.ui.graphics.vector.ImageVector
 44: import androidx.compose.ui.unit.dp
 45: import com.cocktailcraft.ui.theme.AppColors
 46: 
 47: /**
 48:  * A custom animated switch for toggling between light and dark themes.
 49:  *
 50:  * @param isDarkMode Whether the dark mode is currently enabled
 51:  * @param onToggle Callback when the switch is toggled
 52:  * @param modifier Optional modifier for the switch
 53:  * @param enabled Whether the switch is enabled
 54:  */
 55: @Composable
 56: fun AnimatedThemeSwitch(
 57:     isDarkMode: Boolean,
 58:     onToggle: () -> Unit,
 59:     modifier: Modifier = Modifier,
 60:     enabled: Boolean = true
 61: ) {
 62:     val transition = updateTransition(targetState = isDarkMode, label = "theme_switch_transition")
 63: 
 64:     // Animate the track color
 65:     val trackColor by animateColorAsState(
 66:         targetValue = if (isDarkMode) Color(0xFF3C4043) else Color(0xFFE4E4E4),
 67:         animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
 68:         label = "track_color"
 69:     )
 70: 
 71:     // Animate the thumb color
 72:     val thumbColor by animateColorAsState(
 73:         targetValue = if (isDarkMode) AppColors.PrimaryDark else AppColors.PrimaryLight,
 74:         animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
 75:         label = "thumb_color"
 76:     )
 77: 
 78:     // Animate the thumb position
 79:     val thumbPosition by transition.animateFloat(
 80:         label = "thumb_position",
 81:         transitionSpec = {
 82:             spring(
 83:                 dampingRatio = Spring.DampingRatioMediumBouncy,
 84:                 stiffness = Spring.StiffnessLow
 85:             )
 86:         }
 87:     ) { if (it) 1f else 0f }
 88: 
 89:     // Animate the icon scale
 90:     val iconScale by transition.animateFloat(
 91:         label = "icon_scale",
 92:         transitionSpec = {
 93:             spring(
 94:                 dampingRatio = Spring.DampingRatioMediumBouncy,
 95:                 stiffness = Spring.StiffnessMedium
 96:             )
 97:         }
 98:     ) { if (it) 1f else 0.7f }
 99: 
100:     val interactionSource = remember { MutableInteractionSource() }
101: 
102:     Box(
103:         modifier = modifier
104:             .clip(RoundedCornerShape(24.dp))
105:             .background(trackColor)
106:             .clickable(
107:                 interactionSource = interactionSource,
108:                 indication = null,
109:                 enabled = enabled,
110:                 onClick = onToggle
111:             )
112:             .padding(4.dp)
113:             .height(32.dp)
114:             .width(64.dp)
115:     ) {
116:         // Thumb with icon
117:         Box(
118:             modifier = Modifier
119:                 .size(24.dp)
120:                 .offset(x = (36 * thumbPosition).dp)
121:                 .clip(CircleShape)
122:                 .background(thumbColor)
123:                 .align(Alignment.CenterStart),
124:             contentAlignment = Alignment.Center
125:         ) {
126:             Icon(
127:                 imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
128:                 contentDescription = if (isDarkMode) "Dark Mode" else "Light Mode",
129:                 tint = Color.White,
130:                 modifier = Modifier
131:                     .size(16.dp)
132:                     .scale(iconScale)
133:             )
134:         }
135:     }
136: }
137: 
138: /**
139:  * A custom animated theme toggle row with icon, text, and switch.
140:  */
141: @Composable
142: fun AnimatedThemeToggleRow(
143:     title: String,
144:     subtitle: String,
145:     icon: ImageVector,
146:     isChecked: Boolean,
147:     onToggle: () -> Unit,
148:     modifier: Modifier = Modifier,
149:     enabled: Boolean = true
150: ) {
151:     val alpha = if (enabled) 1f else 0.5f
152: 
153:     Row(
154:         modifier = modifier
155:             .clickable(enabled = enabled) { onToggle() }
156:             .padding(vertical = 12.dp),
157:         verticalAlignment = Alignment.CenterVertically
158:     ) {
159:         Icon(
160:             imageVector = icon,
161:             contentDescription = null,
162:             tint = AppColors.TextPrimary,
163:             modifier = Modifier
164:                 .size(24.dp)
165:                 .alpha(alpha)
166:         )
167: 
168:         Spacer(modifier = Modifier.width(16.dp))
169: 
170:         androidx.compose.foundation.layout.Column(
171:             modifier = Modifier
172:                 .weight(1f)
173:                 .alpha(alpha)
174:         ) {
175:             Text(
176:                 text = title,
177:                 style = MaterialTheme.typography.bodyLarge,
178:                 color = AppColors.TextPrimary
179:             )
180: 
181:             Text(
182:                 text = subtitle,
183:                 style = MaterialTheme.typography.bodySmall,
184:                 color = AppColors.TextSecondary
185:             )
186:         }
187: 
188:         AnimatedThemeSwitch(
189:             isDarkMode = isChecked,
190:             onToggle = onToggle,
191:             enabled = enabled
192:         )
193:     }
194: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/AuthDialog.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.foundation.layout.Column
  4: import androidx.compose.foundation.layout.fillMaxWidth
  5: import androidx.compose.foundation.layout.padding
  6: import androidx.compose.foundation.text.KeyboardOptions
  7: import androidx.compose.material.icons.Icons
  8: import androidx.compose.material.icons.filled.Visibility
  9: import androidx.compose.material.icons.filled.VisibilityOff
 10: import androidx.compose.material3.AlertDialog
 11: import androidx.compose.material3.Button
 12: import androidx.compose.material3.ButtonDefaults
 13: import androidx.compose.material3.Icon
 14: import androidx.compose.material3.IconButton
 15: import androidx.compose.material3.OutlinedButton
 16: import androidx.compose.material3.OutlinedTextField
 17: import androidx.compose.material3.Text
 18: import androidx.compose.runtime.Composable
 19: import androidx.compose.runtime.getValue
 20: import androidx.compose.runtime.mutableStateOf
 21: import androidx.compose.runtime.remember
 22: import androidx.compose.runtime.setValue
 23: import androidx.compose.ui.Modifier
 24: import androidx.compose.ui.text.input.KeyboardType
 25: import androidx.compose.ui.text.input.PasswordVisualTransformation
 26: import androidx.compose.ui.text.input.VisualTransformation
 27: import androidx.compose.ui.unit.dp
 28: import com.cocktailcraft.ui.theme.AppColors
 29: 
 30: /**
 31:  * The type of authentication dialog to display.
 32:  */
 33: enum class AuthDialogType {
 34:     SIGN_IN, SIGN_UP
 35: }
 36: 
 37: /**
 38:  * A reusable authentication dialog component that can be used for sign-in or sign-up.
 39:  *
 40:  * @param type The type of authentication dialog to display
 41:  * @param onDismiss The callback for when the dialog is dismissed
 42:  * @param onSubmit The callback for when the form is submitted
 43:  * @param signInButtonText The text for the sign-in button
 44:  * @param signUpButtonText The text for the sign-up button
 45:  * @param cancelButtonText The text for the cancel button
 46:  * @param nameLabel The label for the name field
 47:  * @param emailLabel The label for the email field
 48:  * @param passwordLabel The label for the password field
 49:  */
 50: @Composable
 51: fun AuthDialog(
 52:     type: AuthDialogType,
 53:     onDismiss: () -> Unit,
 54:     onSubmit: (name: String?, email: String, password: String) -> Unit,
 55:     signInButtonText: String = "Sign In",
 56:     signUpButtonText: String = "Create Account",
 57:     cancelButtonText: String = "Cancel",
 58:     nameLabel: String = "Name",
 59:     emailLabel: String = "Email",
 60:     passwordLabel: String = "Password"
 61: ) {
 62:     var name by remember { mutableStateOf("") }
 63:     var email by remember { mutableStateOf("") }
 64:     var password by remember { mutableStateOf("") }
 65:     var passwordVisible by remember { mutableStateOf(false) }
 66: 
 67:     val isSignUp = type == AuthDialogType.SIGN_UP
 68:     val title = if (isSignUp) "Create Account" else "Sign In"
 69:     val confirmButtonText = if (isSignUp) signUpButtonText else signInButtonText
 70:     val isFormValid = if (isSignUp) {
 71:         name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
 72:     } else {
 73:         email.isNotBlank() && password.isNotBlank()
 74:     }
 75: 
 76:     AlertDialog(
 77:         onDismissRequest = onDismiss,
 78:         title = { Text(title) },
 79:         text = {
 80:             Column {
 81:                 if (isSignUp) {
 82:                     OutlinedTextField(
 83:                         value = name,
 84:                         onValueChange = { name = it },
 85:                         label = { Text(nameLabel) },
 86:                         singleLine = true,
 87:                         modifier = Modifier
 88:                             .fillMaxWidth()
 89:                             .padding(vertical = 8.dp)
 90:                     )
 91:                 }
 92: 
 93:                 OutlinedTextField(
 94:                     value = email,
 95:                     onValueChange = { email = it },
 96:                     label = { Text(emailLabel) },
 97:                     singleLine = true,
 98:                     modifier = Modifier
 99:                         .fillMaxWidth()
100:                         .padding(vertical = 8.dp),
101:                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
102:                 )
103: 
104:                 OutlinedTextField(
105:                     value = password,
106:                     onValueChange = { password = it },
107:                     label = { Text(passwordLabel) },
108:                     singleLine = true,
109:                     visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
110:                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
111:                     modifier = Modifier
112:                         .fillMaxWidth()
113:                         .padding(vertical = 8.dp),
114:                     trailingIcon = {
115:                         IconButton(onClick = { passwordVisible = !passwordVisible }) {
116:                             Icon(
117:                                 imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
118:                                 contentDescription = if (passwordVisible) "Hide password" else "Show password"
119:                             )
120:                         }
121:                     }
122:                 )
123:             }
124:         },
125:         confirmButton = {
126:             Button(
127:                 onClick = { 
128:                     onSubmit(if (isSignUp) name else null, email, password) 
129:                 },
130:                 enabled = isFormValid,
131:                 colors = ButtonDefaults.buttonColors(
132:                     containerColor = AppColors.Primary
133:                 )
134:             ) {
135:                 Text(confirmButtonText)
136:             }
137:         },
138:         dismissButton = {
139:             OutlinedButton(onClick = onDismiss) {
140:                 Text(cancelButtonText)
141:             }
142:         }
143:     )
144: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/CartItemCard.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.foundation.background
  4: import androidx.compose.foundation.clickable
  5: import androidx.compose.foundation.layout.*
  6: import androidx.compose.foundation.shape.CircleShape
  7: import androidx.compose.foundation.shape.RoundedCornerShape
  8: import androidx.compose.material.icons.Icons
  9: import androidx.compose.material.icons.filled.Add
 10: import androidx.compose.material.icons.filled.Delete
 11: import androidx.compose.material.icons.filled.Favorite
 12: import androidx.compose.material.icons.filled.FavoriteBorder
 13: import androidx.compose.material.icons.filled.Remove
 14: import androidx.compose.material3.*
 15: import androidx.compose.runtime.Composable
 16: import androidx.compose.ui.Alignment
 17: import androidx.compose.ui.Modifier
 18: import androidx.compose.ui.draw.clip
 19: import androidx.compose.ui.graphics.Color
 20: import androidx.compose.ui.layout.ContentScale
 21: import androidx.compose.ui.platform.LocalContext
 22: import androidx.compose.ui.text.font.FontWeight
 23: import androidx.compose.ui.text.style.TextOverflow
 24: import androidx.compose.ui.unit.Dp
 25: import androidx.compose.ui.unit.dp
 26: import androidx.compose.ui.unit.sp
 27: import com.cocktailcraft.domain.model.CocktailCartItem
 28: import com.cocktailcraft.ui.theme.AppColors
 29: import com.cocktailcraft.ui.components.LightweightOptimizedImage
 30: import java.text.NumberFormat
 31: import java.util.Locale
 32: 
 33: /**
 34:  * A reusable card component to display items in the shopping cart.
 35:  *
 36:  * @param item The cart item to display
 37:  * @param onIncreaseQuantity Callback when the user increases the quantity
 38:  * @param onDecreaseQuantity Callback when the user decreases the quantity
 39:  * @param onRemove Callback when the user removes the item from cart
 40:  * @param isFavorite Whether the item is in favorites
 41:  * @param onToggleFavorite Callback when the user toggles favorite status
 42:  * @param showFavoriteButton Whether to show the favorite button (default: true)
 43:  * @param showDeleteButton Whether to show the delete button (default: true)
 44:  * @param showQuantityControls Whether to show the quantity controls (default: true)
 45:  * @param cardElevation Elevation for the card (default: 1.dp)
 46:  * @param backgroundColor Background color for the card (default: Color.White)
 47:  * @param cornerRadius Corner radius for the card (default: 12.dp)
 48:  * @param imageSize Size of the product image (default: 80.dp)
 49:  * @param maxLines Maximum lines for the product name (default: 1)
 50:  * @param onClick Optional callback when the user clicks on the card (default: null)
 51:  */
 52: @Composable
 53: fun CartItemCard(
 54:     item: CocktailCartItem,
 55:     onIncreaseQuantity: () -> Unit,
 56:     onDecreaseQuantity: () -> Unit,
 57:     onRemove: () -> Unit,
 58:     isFavorite: Boolean,
 59:     onToggleFavorite: () -> Unit,
 60:     showFavoriteButton: Boolean = true,
 61:     showDeleteButton: Boolean = true,
 62:     showQuantityControls: Boolean = true,
 63:     cardElevation: Dp = 1.dp,
 64:     backgroundColor: Color = Color.White,
 65:     cornerRadius: Dp = 12.dp,
 66:     imageSize: Dp = 80.dp,
 67:     maxLines: Int = 1,
 68:     onClick: (() -> Unit)? = null
 69: ) {
 70:     val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
 71: 
 72:     Card(
 73:         modifier = Modifier
 74:             .fillMaxWidth()
 75:             .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
 76:         colors = CardDefaults.cardColors(
 77:             containerColor = backgroundColor
 78:         ),
 79:         elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
 80:         shape = RoundedCornerShape(cornerRadius)
 81:     ) {
 82:         Row(
 83:             modifier = Modifier
 84:                 .fillMaxWidth()
 85:                 .padding(12.dp),
 86:             verticalAlignment = Alignment.CenterVertically
 87:         ) {
 88:             // Product Image - using lightweight optimized image
 89:             LightweightOptimizedImage(
 90:                 url = item.cocktail.imageUrl,
 91:                 contentDescription = item.cocktail.name,
 92:                 modifier = Modifier
 93:                     .size(imageSize)
 94:                     .clip(RoundedCornerShape(8.dp)),
 95:                 contentScale = ContentScale.Crop,
 96:                 targetSize = 150 // Target size for better memory usage
 97:             )
 98: 
 99:             // Product Details and Controls
100:             Column(
101:                 modifier = Modifier
102:                     .weight(1f)
103:                     .padding(horizontal = 12.dp)
104:             ) {
105:                 // Product Name and Favorite Button
106:                 Row(
107:                     verticalAlignment = Alignment.CenterVertically,
108:                     horizontalArrangement = Arrangement.SpaceBetween,
109:                     modifier = Modifier.fillMaxWidth()
110:                 ) {
111:                     Text(
112:                         text = item.cocktail.name,
113:                         fontWeight = FontWeight.Bold,
114:                         fontSize = 16.sp,
115:                         color = AppColors.TextPrimary,
116:                         modifier = Modifier.weight(1f),
117:                         maxLines = maxLines,
118:                         overflow = TextOverflow.Ellipsis
119:                     )
120: 
121:                     // Favorites button (optional)
122:                     if (showFavoriteButton) {
123:                         IconButton(
124:                             onClick = onToggleFavorite,
125:                             modifier = Modifier.size(32.dp)
126:                         ) {
127:                             Icon(
128:                                 imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
129:                                 contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
130:                                 tint = if (isFavorite) AppColors.Secondary else AppColors.Gray,
131:                                 modifier = Modifier.size(20.dp)
132:                             )
133:                         }
134:                     }
135:                 }
136: 
137:                 Spacer(modifier = Modifier.height(4.dp))
138: 
139:                 // Unit Price
140:                 Text(
141:                     text = currencyFormatter.format(item.cocktail.price),
142:                     fontWeight = FontWeight.Bold,
143:                     color = AppColors.Primary,
144:                     fontSize = 14.sp
145:                 )
146: 
147:                 // Optional alcoholic status
148:                 item.cocktail.alcoholic?.let { alcoholicStatus ->
149:                     Spacer(modifier = Modifier.height(4.dp))
150:                     Text(
151:                         text = alcoholicStatus,
152:                         fontSize = 12.sp,
153:                         color = AppColors.TextSecondary
154:                     )
155:                 }
156: 
157:                 Spacer(modifier = Modifier.height(12.dp))
158: 
159:                 // Price & Quantity Controls Row
160:                 Row(
161:                     verticalAlignment = Alignment.CenterVertically,
162:                     horizontalArrangement = Arrangement.SpaceBetween,
163:                     modifier = Modifier.fillMaxWidth()
164:                 ) {
165:                     // Quantity Controls (optional)
166:                     if (showQuantityControls) {
167:                         Row(
168:                             verticalAlignment = Alignment.CenterVertically
169:                         ) {
170:                             IconButton(
171:                                 onClick = onDecreaseQuantity,
172:                                 modifier = Modifier
173:                                     .size(36.dp)
174:                                     .background(AppColors.LightGray, CircleShape)
175:                             ) {
176:                                 Icon(
177:                                     imageVector = Icons.Default.Remove,
178:                                     contentDescription = "Decrease",
179:                                     tint = AppColors.TextPrimary,
180:                                     modifier = Modifier.size(16.dp)
181:                                 )
182:                             }
183: 
184:                             Text(
185:                                 text = item.quantity.toString(),
186:                                 modifier = Modifier.padding(horizontal = 16.dp),
187:                                 fontWeight = FontWeight.Bold,
188:                                 fontSize = 16.sp
189:                             )
190: 
191:                             IconButton(
192:                                 onClick = onIncreaseQuantity,
193:                                 modifier = Modifier
194:                                     .size(36.dp)
195:                                     .background(AppColors.Primary, CircleShape)
196:                             ) {
197:                                 Icon(
198:                                     imageVector = Icons.Default.Add,
199:                                     contentDescription = "Increase",
200:                                     tint = Color.White,
201:                                     modifier = Modifier.size(16.dp)
202:                                 )
203:                             }
204:                         }
205:                     } else {
206:                         // If quantity controls are not shown, at least show the quantity
207:                         Text(
208:                             text = "Quantity: ${item.quantity}",
209:                             fontSize = 14.sp,
210:                             color = AppColors.TextSecondary
211:                         )
212:                     }
213: 
214:                     // Total Price and Remove
215:                     Row(
216:                         verticalAlignment = Alignment.CenterVertically
217:                     ) {
218:                         Text(
219:                             text = currencyFormatter.format(item.cocktail.price * item.quantity),
220:                             fontWeight = FontWeight.Bold,
221:                             fontSize = 16.sp,
222:                             color = AppColors.TextPrimary
223:                         )
224: 
225:                         // Delete button (optional)
226:                         if (showDeleteButton) {
227:                             IconButton(
228:                                 onClick = onRemove,
229:                                 modifier = Modifier
230:                                     .padding(start = 8.dp)
231:                                     .size(32.dp)
232:                             ) {
233:                                 Icon(
234:                                     imageVector = Icons.Default.Delete,
235:                                     contentDescription = "Remove",
236:                                     tint = AppColors.Error,
237:                                     modifier = Modifier.size(20.dp)
238:                                 )
239:                             }
240:                         }
241:                     }
242:                 }
243:             }
244:         }
245:     }
246: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/CategoryFilterRow.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.layout.Arrangement
 4: import androidx.compose.foundation.layout.fillMaxWidth
 5: import androidx.compose.foundation.layout.padding
 6: import androidx.compose.foundation.lazy.LazyRow
 7: import androidx.compose.foundation.lazy.items
 8: import androidx.compose.runtime.Composable
 9: import androidx.compose.ui.Modifier
10: import androidx.compose.ui.unit.dp
11: 
12: /**
13:  * A reusable component for displaying a horizontal row of category filter chips.
14:  *
15:  * @param categories The list of categories to display
16:  * @param selectedCategory The currently selected category, if any
17:  * @param onCategorySelected Callback when a category is selected
18:  * @param modifier The modifier for the component
19:  * @param horizontalPadding The horizontal padding for the row
20:  * @param verticalPadding The vertical padding for the row
21:  * @param chipSpacing The spacing between chips
22:  * @param allCategoryLabel The label for the "All" category
23:  */
24: @Composable
25: fun CategoryFilterRow(
26:     categories: List<String>,
27:     selectedCategory: String?,
28:     onCategorySelected: (String?) -> Unit,
29:     modifier: Modifier = Modifier,
30:     horizontalPadding: Int = 16,
31:     verticalPadding: Int = 8,
32:     chipSpacing: Int = 8,
33:     allCategoryLabel: String = "All"
34: ) {
35:     LazyRow(
36:         modifier = modifier
37:             .fillMaxWidth()
38:             .padding(vertical = verticalPadding.dp, horizontal = horizontalPadding.dp),
39:         horizontalArrangement = Arrangement.spacedBy(chipSpacing.dp)
40:     ) {
41:         items(categories) { category ->
42:             val isSelected = if (category == allCategoryLabel) {
43:                 selectedCategory == null
44:             } else {
45:                 selectedCategory == category
46:             }
47:             
48:             FilterChip(
49:                 selected = isSelected,
50:                 onClick = {
51:                     onCategorySelected(if (category == allCategoryLabel) null else category)
52:                 },
53:                 label = category
54:             )
55:         }
56:     }
57: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/CocktailItem.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import android.graphics.drawable.ColorDrawable
  4: import androidx.compose.foundation.background
  5: import androidx.compose.foundation.clickable
  6: import androidx.compose.foundation.layout.*
  7: import androidx.compose.foundation.shape.RoundedCornerShape
  8: import androidx.compose.material.icons.Icons
  9: import androidx.compose.material.icons.filled.*
 10: import androidx.compose.material.icons.filled.LocalBar
 11: import androidx.compose.material.icons.filled.ShoppingCart
 12: import androidx.compose.material.icons.outlined.FavoriteBorder
 13: import androidx.compose.material3.*
 14: import androidx.compose.runtime.Composable
 15: import androidx.compose.ui.Alignment
 16: import androidx.compose.ui.Modifier
 17: import androidx.compose.ui.draw.clip
 18: import androidx.compose.ui.graphics.Color
 19: import androidx.compose.ui.graphics.toArgb
 20: import androidx.compose.ui.layout.ContentScale
 21: import androidx.compose.ui.platform.LocalContext
 22: import androidx.compose.ui.text.font.FontStyle
 23: import androidx.compose.ui.text.font.FontWeight
 24: import androidx.compose.ui.text.style.TextAlign
 25: import androidx.compose.ui.text.style.TextOverflow
 26: import androidx.compose.ui.unit.dp
 27: import androidx.compose.ui.unit.sp
 28: import com.cocktailcraft.domain.model.Cocktail
 29: import com.cocktailcraft.ui.theme.AppColors
 30: import com.cocktailcraft.ui.components.OptimizedImage
 31: 
 32: @Composable
 33: fun CocktailItem(
 34:     cocktail: Cocktail,
 35:     onClick: () -> Unit,
 36:     onAddToCart: (Cocktail) -> Unit,
 37:     isFavorite: Boolean = false,
 38:     onToggleFavorite: (Cocktail) -> Unit = {}
 39: ) {
 40:     Card(
 41:         modifier = Modifier
 42:             .fillMaxWidth()
 43:             .clickable(onClick = onClick),
 44:         shape = RoundedCornerShape(12.dp),
 45:         colors = CardDefaults.cardColors(
 46:             containerColor = AppColors.Surface
 47:         ),
 48:         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
 49:     ) {
 50:         Row(
 51:             modifier = Modifier
 52:                 .fillMaxWidth()
 53:                 .padding(12.dp),
 54:             verticalAlignment = Alignment.CenterVertically
 55:         ) {
 56:             // Cocktail image with placeholder
 57:             Box(
 58:                 modifier = Modifier
 59:                     .size(100.dp)
 60:                     .clip(RoundedCornerShape(8.dp))
 61:                     .background(AppColors.LightGray)
 62:             ) {
 63:                 // Use our optimized image component
 64:                 OptimizedImage(
 65:                     url = cocktail.imageUrl,
 66:                     contentDescription = cocktail.name,
 67:                     modifier = Modifier.fillMaxSize(),
 68:                     contentScale = ContentScale.Crop,
 69:                     targetSize = 200 // Target size for better memory usage
 70:                 )
 71: 
 72:                 // Stock badge for out of stock items
 73:                 if (cocktail.stockCount <= 0) {
 74:                     Box(
 75:                         modifier = Modifier
 76:                             .fillMaxSize()
 77:                             .background(Color.Black.copy(alpha = 0.5f)),
 78:                         contentAlignment = Alignment.Center
 79:                     ) {
 80:                         Text(
 81:                             text = "Out of Stock",
 82:                             color = Color.White,
 83:                             fontSize = 12.sp,
 84:                             fontWeight = FontWeight.Bold,
 85:                             textAlign = TextAlign.Center,
 86:                             modifier = Modifier.padding(4.dp)
 87:                         )
 88:                     }
 89:                 }
 90:             }
 91: 
 92:             Spacer(modifier = Modifier.width(12.dp))
 93: 
 94:             // Cocktail details
 95:             Column(
 96:                 modifier = Modifier.weight(1f)
 97:             ) {
 98:                 Text(
 99:                     text = cocktail.name,
100:                     fontWeight = FontWeight.Bold,
101:                     fontSize = 16.sp,
102:                     color = AppColors.TextPrimary,
103:                     maxLines = 1,
104:                     overflow = TextOverflow.Ellipsis
105:                 )
106: 
107:                 Spacer(modifier = Modifier.height(4.dp))
108: 
109:                 // Display alcoholic info with category info
110:                 Text(
111:                     text = buildString {
112:                         append(cocktail.alcoholic ?: "Unknown")
113:                         cocktail.category?.let {
114:                             append(" • ")
115:                             append(it)
116:                         }
117:                     },
118:                     fontSize = 14.sp,
119:                     color = AppColors.TextSecondary,
120:                     maxLines = 1,
121:                     overflow = TextOverflow.Ellipsis
122:                 )
123: 
124:                 Spacer(modifier = Modifier.height(4.dp))
125: 
126:                 // Use safe call for ingredients that might be null or empty
127:                 Text(
128:                     text = if (cocktail.ingredients.isNotEmpty()) {
129:                         val firstIngredient = cocktail.ingredients.first().name
130:                         // Check if it's a placeholder
131:                         if (firstIngredient == "Tap to view ingredients") {
132:                             firstIngredient
133:                         } else {
134:                             // Join first 2 ingredients with safe operators
135:                             cocktail.ingredients.take(2).joinToString(", ") { it.name }
136:                                 .let { if (cocktail.ingredients.size > 2) "$it..." else it }
137:                         }
138:                     } else {
139:                         "Tap to view ingredients"
140:                     },
141:                     fontSize = 12.sp,
142:                     color = AppColors.TextSecondary,
143:                     fontStyle = FontStyle.Italic,
144:                     maxLines = 1,
145:                     overflow = TextOverflow.Ellipsis
146:                 )
147: 
148:                 Spacer(modifier = Modifier.height(8.dp))
149: 
150:                 Row(
151:                     verticalAlignment = Alignment.CenterVertically,
152:                     modifier = Modifier.fillMaxWidth()
153:                 ) {
154:                     // Price
155:                     Text(
156:                         text = "$${String.format("%.2f", cocktail.price)}",
157:                         fontWeight = FontWeight.Bold,
158:                         fontSize = 16.sp,
159:                         color = AppColors.Primary
160:                     )
161: 
162:                     Spacer(modifier = Modifier.weight(1f))
163: 
164:                     // Favorite button with actual functionality
165:                     IconButton(
166:                         onClick = { onToggleFavorite(cocktail) },
167:                         modifier = Modifier.size(32.dp)
168:                     ) {
169:                         Icon(
170:                             imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
171:                             contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
172:                             tint = if (isFavorite) AppColors.Secondary else AppColors.Gray,
173:                             modifier = Modifier.size(20.dp)
174:                         )
175:                     }
176: 
177:                     // Add to cart button
178:                     IconButton(
179:                         onClick = { onAddToCart(cocktail) },
180:                         modifier = Modifier.size(32.dp),
181:                         enabled = cocktail.stockCount > 0
182:                     ) {
183:                         Icon(
184:                             imageVector = Icons.Default.ShoppingCart,
185:                             contentDescription = "Add to Cart",
186:                             tint = if (cocktail.stockCount > 0) AppColors.Primary else AppColors.Gray,
187:                             modifier = Modifier.size(20.dp)
188:                         )
189:                     }
190:                 }
191:             }
192:         }
193:     }
194: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/CocktailLoadingShimmer.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.layout.Arrangement
 4: import androidx.compose.foundation.layout.Box
 5: import androidx.compose.foundation.layout.PaddingValues
 6: import androidx.compose.foundation.layout.fillMaxSize
 7: import androidx.compose.foundation.layout.fillMaxWidth
 8: import androidx.compose.foundation.layout.height
 9: import androidx.compose.foundation.layout.padding
10: import androidx.compose.foundation.lazy.LazyColumn
11: import androidx.compose.foundation.lazy.items
12: import androidx.compose.foundation.shape.RoundedCornerShape
13: import androidx.compose.runtime.Composable
14: import androidx.compose.ui.Modifier
15: import androidx.compose.ui.draw.clip
16: import androidx.compose.ui.unit.dp
17: 
18: /**
19:  * A reusable component for displaying a shimmer loading effect for cocktails.
20:  *
21:  * @param itemCount The number of shimmer items to display
22:  * @param modifier The modifier for the component
23:  * @param headerHeight The height of the shimmer header
24:  * @param headerWidthFraction The width fraction of the shimmer header
25:  * @param itemSpacing The spacing between shimmer items
26:  * @param contentPadding The padding for the content
27:  */
28: @Composable
29: fun CocktailLoadingShimmer(
30:     itemCount: Int = 5,
31:     modifier: Modifier = Modifier,
32:     headerHeight: Int = 24,
33:     headerWidthFraction: Float = 0.5f,
34:     itemSpacing: Int = 16,
35:     contentPadding: PaddingValues = PaddingValues(16.dp)
36: ) {
37:     LazyColumn(
38:         modifier = modifier
39:             .fillMaxSize()
40:             .padding(horizontal = 16.dp),
41:         verticalArrangement = Arrangement.spacedBy(itemSpacing.dp),
42:         contentPadding = contentPadding
43:     ) {
44:         // Shimmer header
45:         item(key = "shimmer_header") {
46:             Box(
47:                 modifier = Modifier
48:                     .fillMaxWidth(headerWidthFraction)
49:                     .height(headerHeight.dp)
50:                     .clip(RoundedCornerShape(4.dp))
51:                     .shimmerEffect()
52:             )
53:         }
54: 
55:         // Shimmer items
56:         items(List(itemCount) { it }) { _ ->
57:             CocktailItemShimmer()
58:         }
59:     }
60: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/CocktailSearchBar.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.foundation.background
  4: import androidx.compose.foundation.layout.Row
  5: import androidx.compose.foundation.layout.Spacer
  6: import androidx.compose.foundation.layout.fillMaxWidth
  7: import androidx.compose.foundation.layout.padding
  8: import androidx.compose.foundation.layout.size
  9: import androidx.compose.foundation.layout.width
 10: import androidx.compose.foundation.shape.RoundedCornerShape
 11: import androidx.compose.material.icons.Icons
 12: import androidx.compose.material.icons.filled.Close
 13: import androidx.compose.material.icons.filled.FilterAlt
 14: import androidx.compose.material.icons.filled.Search
 15: import androidx.compose.material3.Icon
 16: import androidx.compose.material3.IconButton
 17: import androidx.compose.material3.OutlinedTextField
 18: import androidx.compose.material3.Text
 19: import androidx.compose.material3.TextFieldDefaults
 20: import androidx.compose.runtime.Composable
 21: import androidx.compose.ui.Alignment
 22: import androidx.compose.ui.Modifier
 23: import androidx.compose.ui.graphics.Color
 24: import androidx.compose.ui.unit.dp
 25: import com.cocktailcraft.ui.theme.AppColors
 26: 
 27: /**
 28:  * A reusable search bar component with an advanced search button.
 29:  *
 30:  * @param searchQuery The current search query
 31:  * @param isAdvancedSearchActive Whether advanced search is currently active
 32:  * @param hasActiveFilters Whether there are any active filters
 33:  * @param onSearchQueryChange Callback when the search query changes
 34:  * @param onClearSearch Callback when the search is cleared
 35:  * @param onToggleAdvancedSearch Callback when the advanced search button is clicked and filters are active
 36:  * @param onShowAdvancedSearchDialog Callback when the advanced search button is clicked and no filters are active
 37:  * @param modifier The modifier for the component
 38:  * @param placeholder The placeholder text for the search field
 39:  * @param searchIconTint The tint color for the search icon
 40:  * @param clearIconTint The tint color for the clear icon
 41:  * @param activeFilterButtonColor The background color for the filter button when active
 42:  * @param inactiveFilterButtonColor The background color for the filter button when inactive
 43:  * @param activeFilterIconTint The tint color for the filter icon when active
 44:  * @param inactiveFilterIconTint The tint color for the filter icon when inactive
 45:  */
 46: @Composable
 47: fun CocktailSearchBar(
 48:     searchQuery: String,
 49:     isAdvancedSearchActive: Boolean,
 50:     hasActiveFilters: Boolean,
 51:     onSearchQueryChange: (String) -> Unit,
 52:     onClearSearch: () -> Unit,
 53:     onToggleAdvancedSearch: () -> Unit,
 54:     onShowAdvancedSearchDialog: () -> Unit,
 55:     modifier: Modifier = Modifier,
 56:     placeholder: String = "Search cocktails...",
 57:     searchIconTint: Color = AppColors.Gray,
 58:     clearIconTint: Color = AppColors.Gray,
 59:     activeFilterButtonColor: Color = AppColors.Primary,
 60:     inactiveFilterButtonColor: Color = AppColors.LightGray,
 61:     activeFilterIconTint: Color = Color.White,
 62:     inactiveFilterIconTint: Color = AppColors.TextSecondary
 63: ) {
 64:     Row(
 65:         modifier = modifier
 66:             .fillMaxWidth()
 67:             .padding(horizontal = 16.dp, vertical = 8.dp),
 68:         verticalAlignment = Alignment.CenterVertically
 69:     ) {
 70:         OutlinedTextField(
 71:             value = searchQuery,
 72:             onValueChange = onSearchQueryChange,
 73:             modifier = Modifier.weight(1f),
 74:             placeholder = { Text(placeholder) },
 75:             leadingIcon = {
 76:                 Icon(
 77:                     Icons.Filled.Search,
 78:                     contentDescription = "Search",
 79:                     tint = searchIconTint
 80:                 )
 81:             },
 82:             trailingIcon = {
 83:                 if (searchQuery.isNotEmpty()) {
 84:                     IconButton(onClick = onClearSearch) {
 85:                         Icon(
 86:                             Icons.Filled.Close,
 87:                             contentDescription = "Clear search",
 88:                             tint = clearIconTint
 89:                         )
 90:                     }
 91:                 }
 92:             },
 93:             colors = TextFieldDefaults.outlinedTextFieldColors(
 94:                 focusedBorderColor = AppColors.Primary,
 95:                 unfocusedBorderColor = Color.Gray,
 96:                 cursorColor = AppColors.Primary,
 97:                 focusedLeadingIconColor = AppColors.Primary,
 98:                 unfocusedLeadingIconColor = Color.Gray,
 99:                 containerColor = Color.White
100:             ),
101:             shape = RoundedCornerShape(8.dp),
102:             singleLine = true
103:         )
104: 
105:         Spacer(modifier = Modifier.width(8.dp))
106: 
107:         // Advanced search button
108:         IconButton(
109:             onClick = {
110:                 if (isAdvancedSearchActive) {
111:                     // If already expanded, just collapse it
112:                     onToggleAdvancedSearch()
113:                 } else {
114:                     // If not expanded, toggle between dialog and expandable panel
115:                     if (hasActiveFilters) {
116:                         // If filters are already applied, just expand the panel
117:                         onToggleAdvancedSearch()
118:                     } else {
119:                         // If no filters applied, show the dialog for better UX
120:                         onShowAdvancedSearchDialog()
121:                     }
122:                 }
123:             },
124:             modifier = Modifier
125:                 .background(
126:                     color = if (isAdvancedSearchActive || hasActiveFilters)
127:                         activeFilterButtonColor
128:                     else
129:                         inactiveFilterButtonColor,
130:                     shape = RoundedCornerShape(8.dp)
131:                 )
132:                 .size(48.dp)
133:         ) {
134:             Icon(
135:                 imageVector = Icons.Default.FilterAlt,
136:                 contentDescription = "Advanced Search",
137:                 tint = if (isAdvancedSearchActive || hasActiveFilters)
138:                     activeFilterIconTint
139:                 else
140:                     inactiveFilterIconTint
141:             )
142:         }
143:     }
144: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/ConfirmationDialog.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.BorderStroke
 4: import androidx.compose.foundation.layout.padding
 5: import androidx.compose.foundation.shape.RoundedCornerShape
 6: import androidx.compose.material3.AlertDialog
 7: import androidx.compose.material3.Button
 8: import androidx.compose.material3.ButtonDefaults
 9: import androidx.compose.material3.OutlinedButton
10: import androidx.compose.material3.Text
11: import androidx.compose.runtime.Composable
12: import androidx.compose.ui.Modifier
13: import androidx.compose.ui.text.font.FontWeight
14: import androidx.compose.ui.unit.dp
15: import androidx.compose.ui.unit.sp
16: import com.cocktailcraft.ui.theme.AppColors
17: 
18: /**
19:  * A reusable confirmation dialog component.
20:  *
21:  * @param showDialog Whether to show the dialog
22:  * @param title The title text of the dialog
23:  * @param message The message text of the dialog
24:  * @param confirmButtonText The text for the confirm button
25:  * @param dismissButtonText The text for the dismiss button
26:  * @param onConfirm The callback for when the confirm button is clicked
27:  * @param onDismiss The callback for when the dialog is dismissed
28:  * @param titleFontSize The font size of the title
29:  * @param messageFontSize The font size of the message
30:  * @param confirmButtonColor The color of the confirm button
31:  * @param dismissButtonColor The color of the dismiss button text
32:  */
33: @Composable
34: fun ConfirmationDialog(
35:     showDialog: Boolean,
36:     title: String,
37:     message: String,
38:     confirmButtonText: String = "Confirm",
39:     dismissButtonText: String = "Cancel",
40:     onConfirm: () -> Unit,
41:     onDismiss: () -> Unit,
42:     titleFontSize: Int = 18,
43:     messageFontSize: Int = 16,
44:     confirmButtonColor: androidx.compose.ui.graphics.Color = AppColors.Primary,
45:     dismissButtonColor: androidx.compose.ui.graphics.Color = AppColors.Primary
46: ) {
47:     if (showDialog) {
48:         AlertDialog(
49:             onDismissRequest = onDismiss,
50:             title = {
51:                 Text(
52:                     text = title,
53:                     fontSize = titleFontSize.sp,
54:                     fontWeight = FontWeight.Bold
55:                 )
56:             },
57:             text = {
58:                 Text(
59:                     text = message,
60:                     fontSize = messageFontSize.sp,
61:                     lineHeight = (messageFontSize + 6).sp
62:                 )
63:             },
64:             confirmButton = {
65:                 Button(
66:                     onClick = onConfirm,
67:                     colors = ButtonDefaults.buttonColors(containerColor = confirmButtonColor),
68:                     shape = RoundedCornerShape(8.dp)
69:                 ) {
70:                     Text(
71:                         text = confirmButtonText,
72:                         fontWeight = FontWeight.Medium
73:                     )
74:                 }
75:             },
76:             dismissButton = {
77:                 OutlinedButton(
78:                     onClick = onDismiss,
79:                     border = BorderStroke(1.dp, dismissButtonColor),
80:                     colors = ButtonDefaults.outlinedButtonColors(
81:                         contentColor = dismissButtonColor
82:                     ),
83:                     shape = RoundedCornerShape(8.dp)
84:                 ) {
85:                     Text(
86:                         text = dismissButtonText,
87:                         fontWeight = FontWeight.Medium
88:                     )
89:                 }
90:             },
91:             containerColor = AppColors.Surface,
92:             shape = RoundedCornerShape(16.dp)
93:         )
94:     }
95: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/DetailHeaderImage.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.background
 4: import androidx.compose.foundation.layout.Box
 5: import androidx.compose.foundation.layout.fillMaxSize
 6: import androidx.compose.foundation.layout.fillMaxWidth
 7: import androidx.compose.foundation.layout.height
 8: import androidx.compose.runtime.Composable
 9: import androidx.compose.ui.Modifier
10: import androidx.compose.ui.graphics.Brush
11: import androidx.compose.ui.graphics.Color
12: import androidx.compose.ui.layout.ContentScale
13: import androidx.compose.ui.unit.dp
14: 
15: /**
16:  * A reusable component for displaying a header image with a gradient overlay.
17:  *
18:  * @param imageUrl The URL of the image to display
19:  * @param contentDescription The content description for the image
20:  * @param modifier The modifier for the component
21:  * @param height The height of the image
22:  * @param targetSize The target size for image optimization
23:  * @param gradientColors The colors for the gradient overlay
24:  * @param gradientStartY The starting Y position for the gradient (0f to 1f)
25:  */
26: @Composable
27: fun DetailHeaderImage(
28:     imageUrl: String,
29:     contentDescription: String,
30:     modifier: Modifier = Modifier,
31:     height: Int = 250,
32:     targetSize: Int = 800,
33:     gradientColors: List<Color> = listOf(
34:         Color.Transparent,
35:         Color.Black.copy(alpha = 0.5f)
36:     ),
37:     gradientStartY: Float = 150f
38: ) {
39:     Box(
40:         modifier = modifier
41:             .fillMaxWidth()
42:             .height(height.dp)
43:     ) {
44:         OptimizedImage(
45:             url = imageUrl,
46:             contentDescription = contentDescription,
47:             modifier = Modifier.fillMaxSize(),
48:             contentScale = ContentScale.Crop,
49:             targetSize = targetSize
50:         )
51: 
52:         // Gradient overlay
53:         Box(
54:             modifier = Modifier
55:                 .fillMaxSize()
56:                 .background(
57:                     brush = Brush.verticalGradient(
58:                         colors = gradientColors,
59:                         startY = gradientStartY
60:                     )
61:                 )
62:         )
63:     }
64: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/DetailInfoCard.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.layout.Column
 4: import androidx.compose.foundation.layout.fillMaxWidth
 5: import androidx.compose.foundation.layout.padding
 6: import androidx.compose.foundation.shape.RoundedCornerShape
 7: import androidx.compose.material3.Card
 8: import androidx.compose.material3.CardDefaults
 9: import androidx.compose.material3.Text
10: import androidx.compose.runtime.Composable
11: import androidx.compose.ui.Modifier
12: import androidx.compose.ui.text.font.FontWeight
13: import androidx.compose.ui.unit.dp
14: import androidx.compose.ui.unit.sp
15: import com.cocktailcraft.ui.theme.AppColors
16: 
17: /**
18:  * A reusable detail info card component.
19:  *
20:  * @param title The title text of the card
21:  * @param modifier The modifier for the component
22:  * @param content The content to display in the card
23:  * @param titleFontSize The font size of the title
24:  * @param titleFontWeight The font weight of the title
25:  * @param titleColor The color of the title text
26:  * @param backgroundColor The background color of the card
27:  * @param elevation The elevation of the card
28:  * @param cornerRadius The corner radius of the card
29:  * @param contentPadding The padding for the content
30:  */
31: @Composable
32: fun DetailInfoCard(
33:     title: String,
34:     modifier: Modifier = Modifier,
35:     content: @Composable () -> Unit,
36:     titleFontSize: Int = 18,
37:     titleFontWeight: FontWeight = FontWeight.Bold,
38:     titleColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
39:     backgroundColor: androidx.compose.ui.graphics.Color = AppColors.Surface,
40:     elevation: Int = 2,
41:     cornerRadius: Int = 12,
42:     contentPadding: Int = 16
43: ) {
44:     Card(
45:         modifier = modifier.fillMaxWidth(),
46:         colors = CardDefaults.cardColors(containerColor = backgroundColor),
47:         elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
48:         shape = RoundedCornerShape(cornerRadius.dp)
49:     ) {
50:         Column(
51:             modifier = Modifier
52:                 .fillMaxWidth()
53:                 .padding(contentPadding.dp)
54:         ) {
55:             Text(
56:                 text = title,
57:                 fontSize = titleFontSize.sp,
58:                 fontWeight = titleFontWeight,
59:                 color = titleColor,
60:                 modifier = Modifier.padding(bottom = 12.dp)
61:             )
62:             
63:             content()
64:         }
65:     }
66: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/EmptySearchResultsMessage.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.AnimatedVisibility
  4: import androidx.compose.animation.core.MutableTransitionState
  5: import androidx.compose.animation.core.Spring
  6: import androidx.compose.animation.core.animateFloatAsState
  7: import androidx.compose.animation.core.spring
  8: import androidx.compose.animation.core.tween
  9: import androidx.compose.animation.fadeIn
 10: import androidx.compose.animation.slideInVertically
 11: import androidx.compose.foundation.Image
 12: import androidx.compose.foundation.layout.Arrangement
 13: import androidx.compose.foundation.layout.Box
 14: import androidx.compose.foundation.layout.Column
 15: import androidx.compose.foundation.layout.Spacer
 16: import androidx.compose.foundation.layout.fillMaxSize
 17: import androidx.compose.foundation.layout.fillMaxWidth
 18: import androidx.compose.foundation.layout.height
 19: import androidx.compose.foundation.layout.padding
 20: import androidx.compose.foundation.layout.size
 21: import androidx.compose.material.icons.Icons
 22: import androidx.compose.material.icons.filled.Search
 23: import androidx.compose.material.icons.outlined.Category
 24: import androidx.compose.material.icons.outlined.SearchOff
 25: import androidx.compose.material3.Button
 26: import androidx.compose.material3.ButtonDefaults
 27: import androidx.compose.material3.Icon
 28: import androidx.compose.material3.MaterialTheme
 29: import androidx.compose.material3.OutlinedButton
 30: import androidx.compose.material3.Text
 31: import androidx.compose.runtime.Composable
 32: import androidx.compose.runtime.LaunchedEffect
 33: import androidx.compose.runtime.getValue
 34: import androidx.compose.runtime.mutableStateOf
 35: import androidx.compose.runtime.remember
 36: import androidx.compose.runtime.setValue
 37: import androidx.compose.ui.Alignment
 38: import androidx.compose.ui.Modifier
 39: import androidx.compose.ui.draw.alpha
 40: import androidx.compose.ui.graphics.Color
 41: import androidx.compose.ui.graphics.vector.ImageVector
 42: import androidx.compose.ui.text.font.FontWeight
 43: import androidx.compose.ui.text.style.TextAlign
 44: import androidx.compose.ui.unit.dp
 45: import androidx.compose.ui.unit.sp
 46: import com.cocktailcraft.ui.theme.AppColors
 47: 
 48: /**
 49:  * A visually appealing message displayed when search results are empty
 50:  */
 51: @Composable
 52: fun EmptySearchResultsMessage(
 53:     searchQuery: String,
 54:     selectedCategory: String? = null,
 55:     onClearSearch: () -> Unit,
 56:     onClearCategory: (() -> Unit)? = null,
 57:     modifier: Modifier = Modifier
 58: ) {
 59:     // Animation states
 60:     val visibleState = remember { MutableTransitionState(false) }
 61: 
 62:     // Set visible to trigger the animation
 63:     LaunchedEffect(Unit) {
 64:         visibleState.targetState = true
 65:     }
 66: 
 67:     Box(
 68:         modifier = modifier.fillMaxSize(),
 69:         contentAlignment = Alignment.Center
 70:     ) {
 71:         AnimatedVisibility(
 72:             visibleState = visibleState,
 73:             enter = fadeIn(
 74:                 animationSpec = tween(durationMillis = 400)
 75:             ) + slideInVertically(
 76:                 animationSpec = spring(
 77:                     dampingRatio = Spring.DampingRatioMediumBouncy,
 78:                     stiffness = Spring.StiffnessLow
 79:                 ),
 80:                 initialOffsetY = { it / 4 }
 81:             )
 82:         ) {
 83:             Column(
 84:                 horizontalAlignment = Alignment.CenterHorizontally,
 85:                 verticalArrangement = Arrangement.Center,
 86:                 modifier = Modifier
 87:                     .fillMaxWidth(0.85f)
 88:                     .padding(16.dp)
 89:             ) {
 90:                 // Icon based on context
 91:                 val icon = when {
 92:                     selectedCategory != null -> Icons.Outlined.Category
 93:                     searchQuery.isNotBlank() -> Icons.Outlined.SearchOff
 94:                     else -> Icons.Filled.Search
 95:                 }
 96: 
 97:                 EmptyStateIcon(icon)
 98: 
 99:                 Spacer(modifier = Modifier.height(24.dp))
100: 
101:                 // Main message
102:                 Text(
103:                     text = when {
104:                         selectedCategory != null && searchQuery.isNotBlank() ->
105:                             "No cocktails found matching \"$searchQuery\" in \"$selectedCategory\""
106:                         selectedCategory != null ->
107:                             "No cocktails found in category \"$selectedCategory\""
108:                         searchQuery.isNotBlank() ->
109:                             "No cocktails found matching \"$searchQuery\""
110:                         else ->
111:                             "No cocktails found"
112:                     },
113:                     fontSize = 20.sp,
114:                     fontWeight = FontWeight.Bold,
115:                     color = AppColors.TextPrimary,
116:                     textAlign = TextAlign.Center
117:                 )
118: 
119:                 Spacer(modifier = Modifier.height(12.dp))
120: 
121:                 // Subtitle with suggestions
122:                 Text(
123:                     text = when {
124:                         selectedCategory != null && searchQuery.isNotBlank() ->
125:                             "Try a different search term or category"
126:                         selectedCategory != null ->
127:                             "This category doesn't have any cocktails yet"
128:                         searchQuery.isNotBlank() ->
129:                             "Try a different search term or check your spelling"
130:                         else ->
131:                             "We couldn't find any cocktails"
132:                     },
133:                     fontSize = 16.sp,
134:                     color = AppColors.TextSecondary,
135:                     textAlign = TextAlign.Center
136:                 )
137: 
138:                 Spacer(modifier = Modifier.height(32.dp))
139: 
140:                 // Action buttons
141:                 when {
142:                     selectedCategory != null && searchQuery.isNotBlank() -> {
143:                         // Both category and search are active
144:                         Button(
145:                             onClick = onClearSearch,
146:                             colors = ButtonDefaults.buttonColors(
147:                                 containerColor = AppColors.Primary
148:                             )
149:                         ) {
150:                             Text("Clear Search")
151:                         }
152: 
153:                         Spacer(modifier = Modifier.height(8.dp))
154: 
155:                         OutlinedButton(
156:                             onClick = { onClearCategory?.invoke() },
157:                             colors = ButtonDefaults.outlinedButtonColors(
158:                                 contentColor = AppColors.Primary
159:                             )
160:                         ) {
161:                             Text("Clear Category Filter")
162:                         }
163:                     }
164:                     selectedCategory != null -> {
165:                         // Only category is active
166:                         Button(
167:                             onClick = { onClearCategory?.invoke() },
168:                             colors = ButtonDefaults.buttonColors(
169:                                 containerColor = AppColors.Primary
170:                             )
171:                         ) {
172:                             Text("Browse All Cocktails")
173:                         }
174:                     }
175:                     searchQuery.isNotBlank() -> {
176:                         // Only search is active
177:                         Button(
178:                             onClick = onClearSearch,
179:                             colors = ButtonDefaults.buttonColors(
180:                                 containerColor = AppColors.Primary
181:                             )
182:                         ) {
183:                             Text("Clear Search")
184:                         }
185:                     }
186:                     else -> {
187:                         // Neither is active
188:                         Button(
189:                             onClick = onClearSearch, // This will refresh the list
190:                             colors = ButtonDefaults.buttonColors(
191:                                 containerColor = AppColors.Primary
192:                             )
193:                         ) {
194:                             Text("Refresh")
195:                         }
196:                     }
197:                 }
198: 
199: 
200:             }
201:         }
202:     }
203: }
204: 
205: @Composable
206: private fun EmptyStateIcon(icon: ImageVector) {
207:     Box(
208:         modifier = Modifier
209:             .size(80.dp)
210:             .padding(8.dp),
211:         contentAlignment = Alignment.Center
212:     ) {
213:         Icon(
214:             imageVector = icon,
215:             contentDescription = "No results",
216:             tint = AppColors.Primary.copy(alpha = 0.2f),
217:             modifier = Modifier.size(80.dp)
218:         )
219: 
220:         Icon(
221:             imageVector = icon,
222:             contentDescription = null,
223:             tint = AppColors.Primary,
224:             modifier = Modifier.size(40.dp)
225:         )
226:     }
227: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/EmptyStateComponent.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.foundation.Image
  4: import androidx.compose.foundation.layout.Arrangement
  5: import androidx.compose.foundation.layout.Column
  6: import androidx.compose.foundation.layout.Spacer
  7: import androidx.compose.foundation.layout.fillMaxSize
  8: import androidx.compose.foundation.layout.height
  9: import androidx.compose.foundation.layout.padding
 10: import androidx.compose.foundation.layout.size
 11: import androidx.compose.foundation.shape.RoundedCornerShape
 12: import androidx.compose.material3.Button
 13: import androidx.compose.material3.ButtonDefaults
 14: import androidx.compose.material3.Icon
 15: import androidx.compose.material3.Text
 16: import androidx.compose.runtime.Composable
 17: import androidx.compose.ui.Alignment
 18: import androidx.compose.ui.Modifier
 19: import androidx.compose.ui.graphics.painter.Painter
 20: import androidx.compose.ui.graphics.vector.ImageVector
 21: import androidx.compose.ui.text.font.FontWeight
 22: import androidx.compose.ui.text.style.TextAlign
 23: import androidx.compose.ui.unit.dp
 24: import androidx.compose.ui.unit.sp
 25: import com.cocktailcraft.ui.theme.AppColors
 26: import com.cocktailcraft.ui.theme.ThemeAssets
 27: 
 28: /**
 29:  * A reusable empty state component that can be used across different screens.
 30:  *
 31:  * @param title The title text to display
 32:  * @param message The message text to display
 33:  * @param actionButtonText The text for the action button (null to hide button)
 34:  * @param onActionButtonClick The callback for when the action button is clicked
 35:  * @param modifier The modifier for the component
 36:  * @param icon Optional icon to display (will be used if painter is null)
 37:  * @param painter Optional painter to display (takes precedence over icon)
 38:  * @param iconSize The size of the icon or image
 39:  * @param iconTint The tint color for the icon (not applied to painter)
 40:  */
 41: @Composable
 42: fun EmptyStateComponent(
 43:     title: String,
 44:     message: String,
 45:     actionButtonText: String? = null,
 46:     onActionButtonClick: () -> Unit = {},
 47:     modifier: Modifier = Modifier,
 48:     icon: ImageVector? = null,
 49:     painter: Painter? = ThemeAssets.emptyStateIllustration(),
 50:     iconSize: Int = 120,
 51:     iconTint: androidx.compose.ui.graphics.Color = AppColors.Gray
 52: ) {
 53:     Column(
 54:         modifier = modifier
 55:             .fillMaxSize()
 56:             .padding(24.dp),
 57:         horizontalAlignment = Alignment.CenterHorizontally,
 58:         verticalArrangement = Arrangement.Center
 59:     ) {
 60:         // Display either the painter or the icon
 61:         if (painter != null) {
 62:             Image(
 63:                 painter = painter,
 64:                 contentDescription = title,
 65:                 modifier = Modifier.size(iconSize.dp)
 66:             )
 67:         } else if (icon != null) {
 68:             Icon(
 69:                 imageVector = icon,
 70:                 contentDescription = title,
 71:                 tint = iconTint,
 72:                 modifier = Modifier.size(iconSize.dp)
 73:             )
 74:         }
 75: 
 76:         Spacer(modifier = Modifier.height(24.dp))
 77: 
 78:         Text(
 79:             text = title,
 80:             fontSize = 22.sp,
 81:             fontWeight = FontWeight.Bold,
 82:             color = AppColors.TextPrimary,
 83:             textAlign = TextAlign.Center
 84:         )
 85: 
 86:         Spacer(modifier = Modifier.height(12.dp))
 87: 
 88:         Text(
 89:             text = message,
 90:             fontSize = 16.sp,
 91:             color = AppColors.TextSecondary,
 92:             textAlign = TextAlign.Center
 93:         )
 94: 
 95:         if (actionButtonText != null) {
 96:             Spacer(modifier = Modifier.height(32.dp))
 97: 
 98:             Button(
 99:                 onClick = onActionButtonClick,
100:                 colors = ButtonDefaults.buttonColors(
101:                     containerColor = AppColors.Primary
102:                 ),
103:                 shape = RoundedCornerShape(12.dp),
104:                 modifier = Modifier.height(48.dp)
105:             ) {
106:                 Text(
107:                     text = actionButtonText,
108:                     fontWeight = FontWeight.Bold,
109:                     fontSize = 16.sp
110:                 )
111:             }
112:         }
113:     }
114: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/EndOfListMessage.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.animation.AnimatedVisibility
 4: import androidx.compose.animation.core.FastOutSlowInEasing
 5: import androidx.compose.animation.core.animateFloatAsState
 6: import androidx.compose.animation.core.tween
 7: import androidx.compose.animation.fadeIn
 8: import androidx.compose.animation.fadeOut
 9: import androidx.compose.foundation.layout.fillMaxWidth
10: import androidx.compose.foundation.layout.offset
11: import androidx.compose.foundation.layout.padding
12: import androidx.compose.material3.Text
13: import androidx.compose.runtime.Composable
14: import androidx.compose.runtime.getValue
15: import androidx.compose.ui.Modifier
16: import androidx.compose.ui.draw.alpha
17: import androidx.compose.ui.text.style.TextAlign
18: import androidx.compose.ui.unit.dp
19: import androidx.compose.ui.unit.sp
20: import com.cocktailcraft.ui.theme.AppColors
21: 
22: /**
23:  * A reusable component for displaying an end of list message with animation.
24:  *
25:  * @param visible Whether the message is visible
26:  * @param message The message to display
27:  * @param modifier The modifier for the component
28:  * @param fontSize The font size of the message
29:  * @param animationDuration The duration of the animation in milliseconds
30:  * @param animationDelay The delay before starting the animation in milliseconds
31:  */
32: @Composable
33: fun EndOfListMessage(
34:     visible: Boolean,
35:     message: String = "You've reached the end of the list",
36:     modifier: Modifier = Modifier,
37:     fontSize: Int = 14,
38:     animationDuration: Int = 500,
39:     animationDelay: Int = 300
40: ) {
41:     AnimatedVisibility(
42:         visible = visible,
43:         enter = fadeIn(
44:             animationSpec = tween(
45:                 durationMillis = animationDuration,
46:                 delayMillis = animationDelay
47:             )
48:         ),
49:         exit = fadeOut(
50:             animationSpec = tween(
51:                 durationMillis = animationDuration
52:             )
53:         )
54:     ) {
55:         // Animate offset for entry
56:         val animatedOffset by animateFloatAsState(
57:             targetValue = 0f,
58:             animationSpec = tween(
59:                 durationMillis = animationDuration,
60:                 delayMillis = animationDelay,
61:                 easing = FastOutSlowInEasing
62:             ),
63:             label = "end_of_list_offset"
64:         )
65: 
66:         Text(
67:             text = message,
68:             modifier = modifier
69:                 .fillMaxWidth()
70:                 .padding(16.dp)
71:                 .offset(y = animatedOffset.dp),
72:             textAlign = TextAlign.Center,
73:             color = AppColors.TextSecondary,
74:             fontSize = fontSize.sp
75:         )
76:     }
77: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/ErrorDialog.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.AnimatedVisibility
  4: import androidx.compose.animation.core.tween
  5: import androidx.compose.animation.fadeIn
  6: import androidx.compose.animation.fadeOut
  7: import androidx.compose.animation.slideInVertically
  8: import androidx.compose.animation.slideOutVertically
  9: import androidx.compose.foundation.background
 10: import androidx.compose.foundation.layout.Arrangement
 11: import androidx.compose.foundation.layout.Box
 12: import androidx.compose.foundation.layout.Column
 13: import androidx.compose.foundation.layout.Row
 14: import androidx.compose.foundation.layout.Spacer
 15: import androidx.compose.foundation.layout.fillMaxWidth
 16: import androidx.compose.foundation.layout.height
 17: import androidx.compose.foundation.layout.padding
 18: import androidx.compose.foundation.layout.size
 19: import androidx.compose.foundation.layout.width
 20: import androidx.compose.foundation.shape.RoundedCornerShape
 21: import androidx.compose.material.icons.Icons
 22: import androidx.compose.material.icons.filled.Error
 23: import androidx.compose.material.icons.filled.Warning
 24: import androidx.compose.material.icons.filled.WifiOff
 25: import androidx.compose.material3.Button
 26: import androidx.compose.material3.ButtonDefaults
 27: import androidx.compose.material3.Card
 28: import androidx.compose.material3.CardDefaults
 29: import androidx.compose.material3.Icon
 30: import androidx.compose.material3.MaterialTheme
 31: import androidx.compose.material3.OutlinedButton
 32: import androidx.compose.material3.Text
 33: import androidx.compose.material3.TextButton
 34: import androidx.compose.runtime.Composable
 35: import androidx.compose.ui.Alignment
 36: import androidx.compose.ui.Modifier
 37: import androidx.compose.ui.graphics.Color
 38: import androidx.compose.ui.graphics.vector.ImageVector
 39: import androidx.compose.ui.text.font.FontWeight
 40: import androidx.compose.ui.text.style.TextAlign
 41: import androidx.compose.ui.unit.dp
 42: import androidx.compose.ui.unit.sp
 43: import androidx.compose.ui.window.Dialog
 44: import androidx.compose.ui.window.DialogProperties
 45: import com.cocktailcraft.ui.theme.AppColors
 46: import com.cocktailcraft.util.ErrorUtils
 47: 
 48: /**
 49:  * A reusable error dialog component that displays user-friendly error messages
 50:  * with appropriate icons and recovery actions.
 51:  */
 52: @Composable
 53: fun ErrorDialog(
 54:     error: ErrorUtils.UserFriendlyError,
 55:     onDismiss: () -> Unit,
 56:     onRetry: (() -> Unit)? = null,
 57:     showRecoveryAction: Boolean = true
 58: ) {
 59:     Dialog(
 60:         onDismissRequest = onDismiss,
 61:         properties = DialogProperties(
 62:             dismissOnBackPress = true,
 63:             dismissOnClickOutside = true
 64:         )
 65:     ) {
 66:         Card(
 67:             shape = RoundedCornerShape(16.dp),
 68:             colors = CardDefaults.cardColors(
 69:                 containerColor = Color.White
 70:             ),
 71:             elevation = CardDefaults.cardElevation(
 72:                 defaultElevation = 6.dp
 73:             )
 74:         ) {
 75:             Column(
 76:                 modifier = Modifier
 77:                     .fillMaxWidth()
 78:                     .padding(24.dp),
 79:                 horizontalAlignment = Alignment.CenterHorizontally
 80:             ) {
 81:                 // Error icon based on category
 82:                 Icon(
 83:                     imageVector = ErrorUtils.getErrorIcon(error.category),
 84:                     contentDescription = "Error",
 85:                     tint = ErrorUtils.getErrorColor(error.category),
 86:                     modifier = Modifier.size(48.dp)
 87:                 )
 88: 
 89:                 Spacer(modifier = Modifier.height(16.dp))
 90: 
 91:                 // Error title
 92:                 Text(
 93:                     text = error.title,
 94:                     fontSize = 20.sp,
 95:                     fontWeight = FontWeight.Bold,
 96:                     color = AppColors.TextPrimary
 97:                 )
 98: 
 99:                 Spacer(modifier = Modifier.height(8.dp))
100: 
101:                 // Error message
102:                 Text(
103:                     text = error.message,
104:                     fontSize = 16.sp,
105:                     color = AppColors.TextSecondary,
106:                     textAlign = TextAlign.Center
107:                 )
108: 
109:                 Spacer(modifier = Modifier.height(24.dp))
110: 
111:                 // Action buttons
112:                 Row(
113:                     modifier = Modifier.fillMaxWidth(),
114:                     horizontalArrangement = Arrangement.End
115:                 ) {
116:                     // Dismiss button
117:                     TextButton(
118:                         onClick = onDismiss
119:                     ) {
120:                         Text("Dismiss")
121:                     }
122: 
123:                     Spacer(modifier = Modifier.width(8.dp))
124: 
125:                     // Recovery action button
126:                     if (showRecoveryAction) {
127:                         if (error.recoveryAction != null) {
128:                             Button(
129:                                 onClick = {
130:                                     error.recoveryAction.action()
131:                                     onDismiss()
132:                                 },
133:                                 colors = ButtonDefaults.buttonColors(
134:                                     containerColor = AppColors.Primary
135:                                 )
136:                             ) {
137:                                 Text(error.recoveryAction.actionLabel)
138:                             }
139:                         } else if (onRetry != null) {
140:                             Button(
141:                                 onClick = {
142:                                     onRetry()
143:                                     onDismiss()
144:                                 },
145:                                 colors = ButtonDefaults.buttonColors(
146:                                     containerColor = AppColors.Primary
147:                                 )
148:                             ) {
149:                                 Text("Try Again")
150:                             }
151:                         }
152:                     }
153:                 }
154:             }
155:         }
156:     }
157: }
158: 
159: /**
160:  * A non-modal error banner that appears at the top of the screen
161:  */
162: @Composable
163: fun ErrorBanner(
164:     error: ErrorUtils.UserFriendlyError?,
165:     onDismiss: () -> Unit,
166:     onAction: (() -> Unit)? = null,
167:     modifier: Modifier = Modifier
168: ) {
169:     AnimatedVisibility(
170:         visible = error != null,
171:         enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(300)),
172:         exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(300))
173:     ) {
174:         error?.let {
175:             Card(
176:                 modifier = modifier
177:                     .fillMaxWidth()
178:                     .padding(16.dp),
179:                 colors = CardDefaults.cardColors(
180:                     containerColor = getErrorBackgroundColor(it.category)
181:                 ),
182:                 elevation = CardDefaults.cardElevation(
183:                     defaultElevation = 4.dp
184:                 )
185:             ) {
186:                 Row(
187:                     modifier = Modifier
188:                         .fillMaxWidth()
189:                         .padding(16.dp),
190:                     verticalAlignment = Alignment.CenterVertically
191:                 ) {
192:                     Icon(
193:                         imageVector = ErrorUtils.getErrorIcon(it.category),
194:                         contentDescription = "Error",
195:                         tint = Color.White,
196:                         modifier = Modifier.size(24.dp)
197:                     )
198: 
199:                     Spacer(modifier = Modifier.width(16.dp))
200: 
201:                     Column(
202:                         modifier = Modifier.weight(1f)
203:                     ) {
204:                         Text(
205:                             text = it.title,
206:                             color = Color.White,
207:                             fontWeight = FontWeight.Bold
208:                         )
209: 
210:                         Text(
211:                             text = it.message,
212:                             color = Color.White.copy(alpha = 0.9f),
213:                             fontSize = 14.sp
214:                         )
215:                     }
216: 
217:                     if (onAction != null && it.recoveryAction != null) {
218:                         TextButton(
219:                             onClick = {
220:                                 it.recoveryAction.action()
221:                                 onAction()
222:                                 onDismiss()
223:                             },
224:                             colors = ButtonDefaults.textButtonColors(
225:                                 contentColor = Color.White
226:                             )
227:                         ) {
228:                             Text(
229:                                 text = it.recoveryAction.actionLabel,
230:                                 fontWeight = FontWeight.Bold
231:                             )
232:                         }
233:                     }
234:                 }
235:             }
236:         }
237:     }
238: }
239: 
240: /**
241:  * Get an appropriate background color for the error banner
242:  */
243: @Composable
244: private fun getErrorBackgroundColor(category: ErrorUtils.ErrorCategory): Color {
245:     // Use the ErrorUtils.getErrorColor function and apply alpha
246:     return ErrorUtils.getErrorColor(category).copy(alpha = 0.9f)
247: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/ExpandableAdvancedSearchPanel.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.AnimatedVisibility
  4: import androidx.compose.animation.expandVertically
  5: import androidx.compose.animation.fadeIn
  6: import androidx.compose.animation.fadeOut
  7: import androidx.compose.animation.shrinkVertically
  8: import androidx.compose.foundation.background
  9: import androidx.compose.foundation.layout.Arrangement
 10: import androidx.compose.foundation.layout.Column
 11: import androidx.compose.foundation.layout.Row
 12: import androidx.compose.foundation.layout.Spacer
 13: import androidx.compose.foundation.layout.fillMaxWidth
 14: import androidx.compose.foundation.layout.height
 15: import androidx.compose.foundation.layout.padding
 16: import androidx.compose.foundation.layout.width
 17: import androidx.compose.foundation.rememberScrollState
 18: import androidx.compose.foundation.shape.RoundedCornerShape
 19: import androidx.compose.foundation.verticalScroll
 20: import androidx.compose.material.icons.Icons
 21: import androidx.compose.material.icons.filled.FilterList
 22: import androidx.compose.material3.Button
 23: import androidx.compose.material3.ButtonDefaults
 24: import androidx.compose.material3.Card
 25: import androidx.compose.material3.CardDefaults
 26: import androidx.compose.material3.Divider
 27: import androidx.compose.material3.Icon
 28: import androidx.compose.material3.MaterialTheme
 29: import androidx.compose.material3.Text
 30: import androidx.compose.material3.TextButton
 31: import androidx.compose.runtime.Composable
 32: import androidx.compose.runtime.getValue
 33: import androidx.compose.runtime.mutableStateOf
 34: import androidx.compose.runtime.remember
 35: import androidx.compose.runtime.setValue
 36: import androidx.compose.ui.Alignment
 37: import androidx.compose.ui.Modifier
 38: import androidx.compose.ui.unit.dp
 39: import com.cocktailcraft.domain.model.Complexity
 40: import com.cocktailcraft.domain.model.PreparationTime
 41: import com.cocktailcraft.domain.model.SearchFilters
 42: import com.cocktailcraft.domain.model.TasteProfile
 43: import com.cocktailcraft.ui.theme.AppColors
 44: 
 45: /**
 46:  * An expandable advanced search panel that can be integrated directly into the HomeScreen.
 47:  * This component provides the same functionality as the dialog-based AdvancedSearchPanel
 48:  * but is designed to be embedded in the screen layout.
 49:  *
 50:  * @param isExpanded Whether the panel is expanded
 51:  * @param currentFilters The current search filters
 52:  * @param categories List of available categories
 53:  * @param ingredients List of available ingredients
 54:  * @param glasses List of available glasses
 55:  * @param onApplyFilters Callback when filters are applied
 56:  * @param onClearFilters Callback when filters are cleared
 57:  * @param modifier The modifier for the component
 58:  */
 59: @Composable
 60: fun ExpandableAdvancedSearchPanel(
 61:     isExpanded: Boolean,
 62:     currentFilters: SearchFilters,
 63:     categories: List<String>,
 64:     ingredients: List<String>,
 65:     glasses: List<String>,
 66:     onApplyFilters: (SearchFilters) -> Unit,
 67:     onClearFilters: () -> Unit,
 68:     modifier: Modifier = Modifier
 69: ) {
 70:     AnimatedVisibility(
 71:         visible = isExpanded,
 72:         enter = expandVertically() + fadeIn(),
 73:         exit = shrinkVertically() + fadeOut()
 74:     ) {
 75:         Card(
 76:             modifier = modifier
 77:                 .fillMaxWidth()
 78:                 .padding(horizontal = 16.dp, vertical = 8.dp),
 79:             shape = RoundedCornerShape(16.dp),
 80:             colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
 81:             elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
 82:         ) {
 83:             Column(
 84:                 modifier = Modifier
 85:                     .fillMaxWidth()
 86:                     .padding(16.dp)
 87:                     .verticalScroll(rememberScrollState())
 88:             ) {
 89:                 // Header with title
 90:                 Row(
 91:                     modifier = Modifier.fillMaxWidth(),
 92:                     verticalAlignment = Alignment.CenterVertically
 93:                 ) {
 94:                     Icon(
 95:                         imageVector = Icons.Default.FilterList,
 96:                         contentDescription = "Advanced Search",
 97:                         tint = AppColors.Primary
 98:                     )
 99:                     Spacer(modifier = Modifier.width(8.dp))
100:                     Text(
101:                         text = "Advanced Search",
102:                         style = MaterialTheme.typography.titleMedium,
103:                         color = AppColors.TextPrimary
104:                     )
105:                 }
106: 
107:                 Divider(modifier = Modifier.padding(vertical = 8.dp))
108: 
109:                 // Filter sections
110:                 var filters by remember { mutableStateOf(currentFilters) }
111: 
112:                 // Category filter
113:                 FilterSection(title = "Category") {
114:                     CategorySelector(
115:                         categories = categories,
116:                         selectedCategory = filters.category,
117:                         onCategorySelected = { category: String? ->
118:                             filters = filters.copy(category = category)
119:                         }
120:                     )
121:                 }
122: 
123:                 // Ingredient filter
124:                 FilterSection(title = "Ingredients") {
125:                     IngredientSelector(
126:                         ingredients = ingredients,
127:                         selectedIngredients = filters.ingredients,
128:                         excludedIngredients = filters.excludeIngredients,
129:                         onIngredientsChanged = { included: List<String>, excluded: List<String> ->
130:                             filters = filters.copy(
131:                                 ingredients = included,
132:                                 excludeIngredients = excluded
133:                             )
134:                         }
135:                     )
136:                 }
137: 
138:                 // Alcoholic filter
139:                 FilterSection(title = "Alcoholic") {
140:                     AlcoholicFilterContent(
141:                         alcoholic = filters.alcoholic,
142:                         onAlcoholicChanged = { alcoholicValue ->
143:                             filters = filters.copy(alcoholic = alcoholicValue)
144:                         }
145:                     )
146:                 }
147: 
148:                 // Glass filter
149:                 FilterSection(title = "Glass Type") {
150:                     GlassSelector(
151:                         glasses = glasses,
152:                         selectedGlass = filters.glass,
153:                         onGlassSelected = { glass: String? ->
154:                             filters = filters.copy(glass = glass)
155:                         }
156:                     )
157:                 }
158: 
159:                 // Price range filter
160:                 FilterSection(title = "Price Range") {
161:                     PriceRangeFilterContent(
162:                         priceRange = filters.priceRange,
163:                         onPriceRangeChanged = { newPriceRange ->
164:                             filters = filters.copy(priceRange = newPriceRange)
165:                         }
166:                     )
167:                 }
168: 
169:                 // Taste profile filter
170:                 FilterSection(title = "Taste Profile") {
171:                     TasteProfileSelector(
172:                         selectedProfile = filters.tasteProfile,
173:                         onProfileSelected = { profile: TasteProfile? ->
174:                             filters = filters.copy(tasteProfile = profile)
175:                         }
176:                     )
177:                 }
178: 
179:                 // Complexity filter
180:                 FilterSection(title = "Complexity") {
181:                     ComplexitySelector(
182:                         selectedComplexity = filters.complexity,
183:                         onComplexitySelected = { complexity: Complexity? ->
184:                             filters = filters.copy(complexity = complexity)
185:                         }
186:                     )
187:                 }
188: 
189:                 // Preparation time filter
190:                 FilterSection(title = "Preparation Time") {
191:                     PrepTimeSelector(
192:                         selectedPrepTime = filters.preparationTime,
193:                         onPrepTimeSelected = { prepTime: PreparationTime? ->
194:                             filters = filters.copy(preparationTime = prepTime)
195:                         }
196:                     )
197:                 }
198: 
199:                 Spacer(modifier = Modifier.height(16.dp))
200: 
201:                 // Action buttons
202:                 Row(
203:                     modifier = Modifier.fillMaxWidth(),
204:                     horizontalArrangement = Arrangement.End
205:                 ) {
206:                     TextButton(
207:                         onClick = {
208:                             onClearFilters()
209:                             filters = SearchFilters(query = filters.query)
210:                         }
211:                     ) {
212:                         Text("Clear All")
213:                     }
214: 
215:                     Spacer(modifier = Modifier.width(8.dp))
216: 
217:                     Button(
218:                         onClick = { onApplyFilters(filters) },
219:                         colors = ButtonDefaults.buttonColors(
220:                             containerColor = AppColors.Primary
221:                         )
222:                     ) {
223:                         Text("Apply Filters")
224:                     }
225:                 }
226:             }
227:         }
228:     }
229: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/FilterChip.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.clickable
 4: import androidx.compose.foundation.layout.Box
 5: import androidx.compose.foundation.layout.Row
 6: import androidx.compose.foundation.layout.Spacer
 7: import androidx.compose.foundation.layout.height
 8: import androidx.compose.foundation.layout.padding
 9: import androidx.compose.foundation.layout.size
10: import androidx.compose.foundation.layout.width
11: import androidx.compose.foundation.shape.RoundedCornerShape
12: import androidx.compose.material3.Icon
13: import androidx.compose.material3.Surface
14: import androidx.compose.material3.Text
15: import androidx.compose.runtime.Composable
16: import androidx.compose.ui.Alignment
17: import androidx.compose.ui.Modifier
18: import androidx.compose.ui.graphics.Color
19: import androidx.compose.ui.graphics.vector.ImageVector
20: import androidx.compose.ui.text.font.FontWeight
21: import androidx.compose.ui.unit.dp
22: import androidx.compose.ui.unit.sp
23: import com.cocktailcraft.ui.theme.AppColors
24: 
25: @Composable
26: fun FilterChip(
27:     selected: Boolean,
28:     onClick: () -> Unit,
29:     label: String,
30:     selectedColor: Color = AppColors.Primary,
31:     unselectedColor: Color = Color.White,
32:     selectedTextColor: Color = Color.White,
33:     unselectedTextColor: Color = AppColors.Primary,
34:     trailingIcon: ImageVector? = null,
35:     selectedIconColor: Color = Color.White
36: ) {
37:     Surface(
38:         shape = RoundedCornerShape(16.dp),
39:         color = if (selected) selectedColor else unselectedColor,
40:         contentColor = if (selected) selectedTextColor else unselectedTextColor,
41:         shadowElevation = if (selected) 2.dp else 0.dp,
42:         modifier = Modifier.height(32.dp)
43:     ) {
44:         Row(
45:             modifier = Modifier
46:                 .clickable(onClick = onClick)
47:                 .padding(horizontal = 16.dp, vertical = 6.dp),
48:             verticalAlignment = Alignment.CenterVertically
49:         ) {
50:             Text(
51:                 text = label,
52:                 fontSize = 14.sp,
53:                 fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
54:                 color = if (selected) selectedTextColor else unselectedTextColor
55:             )
56: 
57:             if (trailingIcon != null) {
58:                 Spacer(modifier = Modifier.width(4.dp))
59:                 Icon(
60:                     imageVector = trailingIcon,
61:                     contentDescription = null,
62:                     tint = if (selected) selectedIconColor else unselectedTextColor,
63:                     modifier = Modifier.size(16.dp)
64:                 )
65:             }
66:         }
67:     }
68: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/InfoCard.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.layout.Column
 4: import androidx.compose.foundation.layout.Row
 5: import androidx.compose.foundation.layout.Spacer
 6: import androidx.compose.foundation.layout.fillMaxWidth
 7: import androidx.compose.foundation.layout.height
 8: import androidx.compose.foundation.layout.padding
 9: import androidx.compose.foundation.layout.size
10: import androidx.compose.foundation.shape.RoundedCornerShape
11: import androidx.compose.material3.Card
12: import androidx.compose.material3.CardDefaults
13: import androidx.compose.material3.Icon
14: import androidx.compose.material3.Text
15: import androidx.compose.runtime.Composable
16: import androidx.compose.ui.Alignment
17: import androidx.compose.ui.Modifier
18: import androidx.compose.ui.graphics.vector.ImageVector
19: import androidx.compose.ui.text.font.FontWeight
20: import androidx.compose.ui.unit.dp
21: import androidx.compose.ui.unit.sp
22: import com.cocktailcraft.ui.theme.AppColors
23: 
24: /**
25:  * A reusable info card component that displays an icon, title, and content.
26:  *
27:  * @param title The title text of the card
28:  * @param icon The icon to display next to the title
29:  * @param modifier The modifier for the component
30:  * @param content The content to display in the card
31:  * @param iconTint The tint color for the icon
32:  * @param titleFontSize The font size of the title
33:  * @param titleFontWeight The font weight of the title
34:  * @param titleColor The color of the title text
35:  * @param backgroundColor The background color of the card
36:  * @param elevation The elevation of the card
37:  * @param cornerRadius The corner radius of the card
38:  * @param contentPadding The padding for the content
39:  * @param iconSize The size of the icon
40:  * @param contentTopPadding The padding between the title and content
41:  */
42: @Composable
43: fun InfoCard(
44:     title: String,
45:     icon: ImageVector,
46:     modifier: Modifier = Modifier,
47:     content: @Composable () -> Unit,
48:     iconTint: androidx.compose.ui.graphics.Color = AppColors.Primary,
49:     titleFontSize: Int = 18,
50:     titleFontWeight: FontWeight = FontWeight.Bold,
51:     titleColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
52:     backgroundColor: androidx.compose.ui.graphics.Color = AppColors.Surface,
53:     elevation: Int = 2,
54:     cornerRadius: Int = 12,
55:     contentPadding: Int = 16,
56:     iconSize: Int = 24,
57:     contentTopPadding: Int = 16
58: ) {
59:     Card(
60:         modifier = modifier.fillMaxWidth(),
61:         colors = CardDefaults.cardColors(containerColor = backgroundColor),
62:         elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
63:         shape = RoundedCornerShape(cornerRadius.dp)
64:     ) {
65:         Column(
66:             modifier = Modifier.padding(contentPadding.dp)
67:         ) {
68:             Row(
69:                 verticalAlignment = Alignment.CenterVertically
70:             ) {
71:                 Icon(
72:                     imageVector = icon,
73:                     contentDescription = title,
74:                     tint = iconTint,
75:                     modifier = Modifier.size(iconSize.dp)
76:                 )
77: 
78:                 Text(
79:                     text = title,
80:                     fontSize = titleFontSize.sp,
81:                     fontWeight = titleFontWeight,
82:                     color = titleColor,
83:                     modifier = Modifier.padding(start = 16.dp)
84:                 )
85:             }
86: 
87:             Spacer(modifier = Modifier.height(contentTopPadding.dp))
88:             
89:             content()
90:         }
91:     }
92: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/LoadingMoreIndicator.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.animation.AnimatedVisibility
 4: import androidx.compose.animation.core.animateFloatAsState
 5: import androidx.compose.animation.core.tween
 6: import androidx.compose.animation.fadeIn
 7: import androidx.compose.animation.fadeOut
 8: import androidx.compose.foundation.layout.Box
 9: import androidx.compose.foundation.layout.fillMaxWidth
10: import androidx.compose.foundation.layout.padding
11: import androidx.compose.foundation.layout.size
12: import androidx.compose.material3.CircularProgressIndicator
13: import androidx.compose.runtime.Composable
14: import androidx.compose.runtime.getValue
15: import androidx.compose.ui.Alignment
16: import androidx.compose.ui.Modifier
17: import androidx.compose.ui.draw.alpha
18: import androidx.compose.ui.unit.dp
19: import com.cocktailcraft.ui.theme.AppColors
20: 
21: /**
22:  * A reusable component for displaying a loading indicator at the bottom of a list.
23:  *
24:  * @param isLoading Whether the loading indicator is visible
25:  * @param modifier The modifier for the component
26:  * @param indicatorSize The size of the loading indicator
27:  * @param animationDuration The duration of the animation in milliseconds
28:  * @param indicatorColor The color of the loading indicator
29:  */
30: @Composable
31: fun LoadingMoreIndicator(
32:     isLoading: Boolean,
33:     modifier: Modifier = Modifier,
34:     indicatorSize: Int = 32,
35:     animationDuration: Int = 300,
36:     indicatorColor: androidx.compose.ui.graphics.Color = AppColors.Primary
37: ) {
38:     AnimatedVisibility(
39:         visible = isLoading,
40:         enter = fadeIn(animationSpec = tween(durationMillis = animationDuration)),
41:         exit = fadeOut(animationSpec = tween(durationMillis = animationDuration))
42:     ) {
43:         Box(
44:             modifier = modifier
45:                 .fillMaxWidth()
46:                 .padding(16.dp),
47:             contentAlignment = Alignment.Center
48:         ) {
49:             // Use a simple fade-in effect for the loading indicator
50:             val animatedAlpha by animateFloatAsState(
51:                 targetValue = 1f,
52:                 animationSpec = tween(durationMillis = animationDuration),
53:                 label = "loading_alpha"
54:             )
55: 
56:             CircularProgressIndicator(
57:                 modifier = Modifier
58:                     .size(indicatorSize.dp)
59:                     .alpha(animatedAlpha),
60:                 color = indicatorColor
61:             )
62:         }
63:     }
64: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/LoadingStateComponent.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.animation.AnimatedVisibility
 4: import androidx.compose.animation.fadeIn
 5: import androidx.compose.animation.fadeOut
 6: import androidx.compose.foundation.layout.Box
 7: import androidx.compose.foundation.layout.fillMaxSize
 8: import androidx.compose.foundation.layout.padding
 9: import androidx.compose.foundation.layout.size
10: import androidx.compose.material3.CircularProgressIndicator
11: import androidx.compose.runtime.Composable
12: import androidx.compose.ui.Alignment
13: import androidx.compose.ui.Modifier
14: import androidx.compose.ui.unit.dp
15: import com.cocktailcraft.ui.theme.AppColors
16: 
17: /**
18:  * A reusable loading state component that displays a circular progress indicator.
19:  *
20:  * @param isLoading Whether the loading state is active
21:  * @param modifier The modifier for the component
22:  * @param contentAlignment The alignment of the progress indicator
23:  * @param indicatorSize The size of the progress indicator
24:  * @param indicatorColor The color of the progress indicator
25:  * @param paddingValues Optional padding values
26:  */
27: @Composable
28: fun LoadingStateComponent(
29:     isLoading: Boolean,
30:     modifier: Modifier = Modifier,
31:     contentAlignment: Alignment = Alignment.Center,
32:     indicatorSize: Int = 40,
33:     indicatorColor: androidx.compose.ui.graphics.Color = AppColors.Primary,
34:     paddingValues: androidx.compose.foundation.layout.PaddingValues? = null
35: ) {
36:     AnimatedVisibility(
37:         visible = isLoading,
38:         enter = fadeIn(),
39:         exit = fadeOut()
40:     ) {
41:         Box(
42:             modifier = modifier
43:                 .fillMaxSize()
44:                 .let {
45:                     if (paddingValues != null) {
46:                         it.padding(paddingValues)
47:                     } else {
48:                         it
49:                     }
50:                 },
51:             contentAlignment = contentAlignment
52:         ) {
53:             CircularProgressIndicator(
54:                 color = indicatorColor,
55:                 modifier = Modifier.size(indicatorSize.dp)
56:             )
57:         }
58:     }
59: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/NetworkErrorStateDisplay.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.foundation.layout.Arrangement
  4: import androidx.compose.foundation.layout.Box
  5: import androidx.compose.foundation.layout.Column
  6: import androidx.compose.foundation.layout.Row
  7: import androidx.compose.foundation.layout.Spacer
  8: import androidx.compose.foundation.layout.fillMaxSize
  9: import androidx.compose.foundation.layout.height
 10: import androidx.compose.foundation.layout.padding
 11: import androidx.compose.foundation.layout.size
 12: import androidx.compose.material.icons.Icons
 13: import androidx.compose.material.icons.filled.Error
 14: import androidx.compose.material.icons.filled.WifiOff
 15: import androidx.compose.material3.Button
 16: import androidx.compose.material3.ButtonDefaults
 17: import androidx.compose.material3.Icon
 18: import androidx.compose.material3.Text
 19: import androidx.compose.runtime.Composable
 20: import androidx.compose.ui.Alignment
 21: import androidx.compose.ui.Modifier
 22: import androidx.compose.ui.graphics.Color
 23: import androidx.compose.ui.graphics.vector.ImageVector
 24: import androidx.compose.ui.text.font.FontWeight
 25: import androidx.compose.ui.text.style.TextAlign
 26: import androidx.compose.ui.unit.dp
 27: import androidx.compose.ui.unit.sp
 28: import com.cocktailcraft.ui.theme.AppColors
 29: 
 30: /**
 31:  * A reusable component for displaying network and offline error states.
 32:  *
 33:  * @param errorMessage The error message to display
 34:  * @param isOfflineMode Whether the app is in offline mode
 35:  * @param isNetworkAvailable Whether the network is available
 36:  * @param hasContent Whether there is cached content available
 37:  * @param onRetry Callback when the retry button is clicked
 38:  * @param onEnableOfflineMode Callback when the enable offline mode button is clicked
 39:  * @param onGoOnline Callback when the go online button is clicked
 40:  * @param modifier The modifier for the component
 41:  * @param iconSize The size of the error icon
 42:  * @param titleFontSize The font size of the title
 43:  * @param messageFontSize The font size of the message
 44:  * @param primaryButtonColor The color of the primary button
 45:  * @param secondaryButtonColor The color of the secondary button
 46:  */
 47: @Composable
 48: fun NetworkErrorStateDisplay(
 49:     errorMessage: String,
 50:     isOfflineMode: Boolean,
 51:     isNetworkAvailable: Boolean,
 52:     hasContent: Boolean,
 53:     onRetry: () -> Unit,
 54:     onEnableOfflineMode: () -> Unit,
 55:     onGoOnline: () -> Unit,
 56:     modifier: Modifier = Modifier,
 57:     iconSize: Int = 48,
 58:     titleFontSize: Int = 18,
 59:     messageFontSize: Int = 14,
 60:     primaryButtonColor: Color = AppColors.Primary,
 61:     secondaryButtonColor: Color = AppColors.Secondary
 62: ) {
 63:     Box(
 64:         modifier = modifier.fillMaxSize(),
 65:         contentAlignment = Alignment.Center
 66:     ) {
 67:         Column(
 68:             horizontalAlignment = Alignment.CenterHorizontally,
 69:             modifier = Modifier.padding(16.dp)
 70:         ) {
 71:             // Error icon based on state
 72:             val icon: ImageVector
 73:             val iconTint: Color
 74:             val title: String
 75:             val message: String
 76: 
 77:             if (!isNetworkAvailable || isOfflineMode) {
 78:                 icon = Icons.Default.WifiOff
 79:                 iconTint = AppColors.Primary
 80:                 title = when {
 81:                     isOfflineMode -> "Offline Mode Active"
 82:                     else -> "Network Unavailable"
 83:                 }
 84:                 message = when {
 85:                     isOfflineMode && !hasContent ->
 86:                         "No cached cocktails available. Connect to the internet to download cocktails."
 87:                     !isNetworkAvailable ->
 88:                         "You're currently offline. Enable Offline Mode to browse cached cocktails."
 89:                     else -> errorMessage
 90:                 }
 91:             } else {
 92:                 icon = Icons.Default.Error
 93:                 iconTint = Color.Red
 94:                 title = "Unable to load cocktails"
 95:                 message = errorMessage
 96:             }
 97: 
 98:             Icon(
 99:                 imageVector = icon,
100:                 contentDescription = "Error",
101:                 tint = iconTint,
102:                 modifier = Modifier.size(iconSize.dp)
103:             )
104: 
105:             Spacer(modifier = Modifier.height(16.dp))
106: 
107:             Text(
108:                 text = title,
109:                 color = iconTint,
110:                 textAlign = TextAlign.Center,
111:                 fontWeight = FontWeight.Bold,
112:                 fontSize = titleFontSize.sp
113:             )
114: 
115:             Spacer(modifier = Modifier.height(8.dp))
116: 
117:             Text(
118:                 text = message,
119:                 color = AppColors.TextSecondary,
120:                 textAlign = TextAlign.Center,
121:                 fontSize = messageFontSize.sp
122:             )
123: 
124:             Spacer(modifier = Modifier.height(24.dp))
125: 
126:             Row(
127:                 horizontalArrangement = Arrangement.spacedBy(8.dp)
128:             ) {
129:                 // Primary action button (always shown)
130:                 Button(
131:                     onClick = onRetry,
132:                     colors = ButtonDefaults.buttonColors(
133:                         containerColor = primaryButtonColor
134:                     )
135:                 ) {
136:                     Text("Retry")
137:                 }
138: 
139:                 // Conditional secondary action button
140:                 when {
141:                     // If network is unavailable and offline mode is not enabled
142:                     !isNetworkAvailable && !isOfflineMode -> {
143:                         Button(
144:                             onClick = onEnableOfflineMode,
145:                             colors = ButtonDefaults.buttonColors(
146:                                 containerColor = secondaryButtonColor
147:                             )
148:                         ) {
149:                             Text("Enable Offline Mode")
150:                         }
151:                     }
152:                     // If offline mode is enabled but we want to go back online
153:                     isOfflineMode && isNetworkAvailable -> {
154:                         Button(
155:                             onClick = onGoOnline,
156:                             colors = ButtonDefaults.buttonColors(
157:                                 containerColor = secondaryButtonColor
158:                             )
159:                         ) {
160:                             Text("Go Online")
161:                         }
162:                     }
163:                 }
164:             }
165:         }
166:     }
167: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/NetworkStatusCard.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.layout.Arrangement
 4: import androidx.compose.foundation.layout.Row
 5: import androidx.compose.foundation.layout.Spacer
 6: import androidx.compose.foundation.layout.fillMaxWidth
 7: import androidx.compose.foundation.layout.padding
 8: import androidx.compose.foundation.layout.size
 9: import androidx.compose.foundation.layout.width
10: import androidx.compose.foundation.shape.RoundedCornerShape
11: import androidx.compose.material.icons.Icons
12: import androidx.compose.material.icons.filled.Wifi
13: import androidx.compose.material.icons.filled.WifiOff
14: import androidx.compose.material3.Card
15: import androidx.compose.material3.CardDefaults
16: import androidx.compose.material3.Icon
17: import androidx.compose.material3.Text
18: import androidx.compose.runtime.Composable
19: import androidx.compose.ui.Alignment
20: import androidx.compose.ui.Modifier
21: import androidx.compose.ui.graphics.Color
22: import androidx.compose.ui.text.font.FontWeight
23: import androidx.compose.ui.unit.dp
24: import androidx.compose.ui.unit.sp
25: 
26: /**
27:  * A reusable network status card component that displays the current network status.
28:  *
29:  * @param isNetworkAvailable Whether the network is available
30:  * @param modifier The modifier for the component
31:  * @param onlineColor The background color when network is available
32:  * @param offlineColor The background color when network is unavailable
33:  * @param textColor The color of the text
34:  * @param iconSize The size of the icon
35:  * @param fontSize The font size of the text
36:  * @param cornerRadius The corner radius of the card
37:  * @param elevation The elevation of the card
38:  * @param contentPadding The padding for the content
39:  */
40: @Composable
41: fun NetworkStatusCard(
42:     isNetworkAvailable: Boolean,
43:     modifier: Modifier = Modifier,
44:     onlineColor: Color = Color(0xFF4CAF50), // Green
45:     offlineColor: Color = Color(0xFFE57373), // Red
46:     textColor: Color = Color.White,
47:     iconSize: Int = 28,
48:     fontSize: Int = 18,
49:     cornerRadius: Int = 12,
50:     elevation: Int = 2,
51:     contentPadding: Int = 16
52: ) {
53:     Card(
54:         modifier = modifier
55:             .fillMaxWidth()
56:             .padding(horizontal = 16.dp, vertical = 8.dp),
57:         colors = CardDefaults.cardColors(
58:             containerColor = if (isNetworkAvailable) onlineColor else offlineColor
59:         ),
60:         shape = RoundedCornerShape(cornerRadius.dp),
61:         elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
62:     ) {
63:         Row(
64:             modifier = Modifier
65:                 .fillMaxWidth()
66:                 .padding(vertical = contentPadding.dp, horizontal = contentPadding.dp),
67:             verticalAlignment = Alignment.CenterVertically,
68:             horizontalArrangement = Arrangement.Center
69:         ) {
70:             Icon(
71:                 imageVector = if (isNetworkAvailable)
72:                     Icons.Default.Wifi
73:                 else
74:                     Icons.Default.WifiOff,
75:                 contentDescription = if (isNetworkAvailable) "Online" else "Offline",
76:                 tint = textColor,
77:                 modifier = Modifier.size(iconSize.dp)
78:             )
79: 
80:             Spacer(modifier = Modifier.width(12.dp))
81: 
82:             Text(
83:                 text = if (isNetworkAvailable) "Network Available" else "Network Unavailable",
84:                 color = textColor,
85:                 fontWeight = FontWeight.Bold,
86:                 fontSize = fontSize.sp
87:             )
88:         }
89:     }
90: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/OfflineModeIndicator.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.animation.AnimatedVisibility
 4: import androidx.compose.animation.expandVertically
 5: import androidx.compose.animation.fadeIn
 6: import androidx.compose.animation.fadeOut
 7: import androidx.compose.animation.shrinkVertically
 8: import androidx.compose.foundation.background
 9: import androidx.compose.foundation.clickable
10: import androidx.compose.foundation.layout.Arrangement
11: import androidx.compose.foundation.layout.Box
12: import androidx.compose.foundation.layout.Row
13: import androidx.compose.foundation.layout.fillMaxWidth
14: import androidx.compose.foundation.layout.padding
15: import androidx.compose.material.icons.Icons
16: import androidx.compose.material.icons.filled.AirplanemodeActive
17: import androidx.compose.material.icons.filled.WifiOff
18: import androidx.compose.material3.Icon
19: import androidx.compose.material3.MaterialTheme
20: import androidx.compose.material3.Text
21: import androidx.compose.runtime.Composable
22: import androidx.compose.ui.Alignment
23: import androidx.compose.ui.Modifier
24: import androidx.compose.ui.graphics.Color
25: import androidx.compose.ui.text.font.FontWeight
26: import androidx.compose.ui.unit.dp
27: import com.cocktailcraft.ui.theme.AppColors
28: 
29: /**
30:  * A banner that indicates when the app is in offline mode.
31:  *
32:  * @param isOffline Whether the app is currently offline
33:  * @param isOfflineModeEnabled Whether offline mode is enabled by user preference
34:  * @param onClick Optional callback when the banner is clicked
35:  */
36: @Composable
37: fun OfflineModeIndicator(
38:     isOffline: Boolean,
39:     isOfflineModeEnabled: Boolean = false,
40:     onClick: (() -> Unit)? = null
41: ) {
42:     AnimatedVisibility(
43:         visible = isOffline,
44:         enter = fadeIn() + expandVertically(),
45:         exit = fadeOut() + shrinkVertically()
46:     ) {
47:         Box(
48:             modifier = Modifier
49:                 .fillMaxWidth()
50:                 .background(
51:                     if (isOfflineModeEnabled) AppColors.Primary.copy(alpha = 0.8f)
52:                     else Color(0xFFE57373) // Light red for network offline
53:                 )
54:                 .clickable(enabled = onClick != null) { onClick?.invoke() }
55:                 .padding(vertical = 8.dp, horizontal = 16.dp)
56:         ) {
57:             Row(
58:                 modifier = Modifier.fillMaxWidth(),
59:                 horizontalArrangement = Arrangement.Center,
60:                 verticalAlignment = Alignment.CenterVertically
61:             ) {
62:                 Icon(
63:                     imageVector = if (isOfflineModeEnabled) Icons.Default.AirplanemodeActive else Icons.Default.WifiOff,
64:                     contentDescription = "Offline",
65:                     tint = Color.White,
66:                     modifier = Modifier.padding(end = 8.dp)
67:                 )
68: 
69:                 Text(
70:                     text = if (isOfflineModeEnabled) "Offline Mode Enabled" else "You are offline",
71:                     color = Color.White,
72:                     fontWeight = FontWeight.Medium
73:                 )
74:             }
75:         }
76:     }
77: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/OptimizedImage.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import android.graphics.drawable.ColorDrawable
  4: import androidx.compose.foundation.background
  5: import androidx.compose.foundation.layout.Box
  6: import androidx.compose.foundation.layout.fillMaxSize
  7: import androidx.compose.foundation.layout.size
  8: import androidx.compose.material.icons.Icons
  9: import androidx.compose.material.icons.filled.LocalBar
 10: import androidx.compose.material3.CircularProgressIndicator
 11: import androidx.compose.material3.Icon
 12: import androidx.compose.runtime.Composable
 13: import androidx.compose.ui.Alignment
 14: import androidx.compose.ui.Modifier
 15: import androidx.compose.ui.graphics.toArgb
 16: import androidx.compose.ui.layout.ContentScale
 17: import androidx.compose.ui.platform.LocalContext
 18: import androidx.compose.ui.unit.dp
 19: import coil.compose.AsyncImage
 20: import coil.compose.SubcomposeAsyncImage
 21: import coil.request.ImageRequest
 22: import com.cocktailcraft.ui.theme.AppColors
 23: import com.cocktailcraft.util.ImageLoaderSingleton
 24: import com.cocktailcraft.util.ImageUtils
 25: 
 26: /**
 27:  * An optimized image component that uses our custom image loading system.
 28:  *
 29:  * @param url The image URL to load
 30:  * @param contentDescription Content description for accessibility
 31:  * @param modifier Modifier for the image
 32:  * @param contentScale Content scaling mode
 33:  * @param showLoadingIndicator Whether to show a loading indicator
 34:  * @param targetSize Target size for image resizing (null for no resizing)
 35:  */
 36: @Composable
 37: fun OptimizedImage(
 38:     url: String?,
 39:     contentDescription: String?,
 40:     modifier: Modifier = Modifier,
 41:     contentScale: ContentScale = ContentScale.Crop,
 42:     showLoadingIndicator: Boolean = true,
 43:     targetSize: Int? = null
 44: ) {
 45:     val context = LocalContext.current
 46:     val imageLoader = ImageLoaderSingleton.getImageLoader(context)
 47: 
 48:     // Create placeholder and error drawables
 49:     val placeholderColor = AppColors.LightGray.toArgb()
 50:     val placeholder = ColorDrawable(placeholderColor)
 51: 
 52:     // Build the optimized image request
 53:     val request = ImageUtils.buildOptimizedImageRequest(
 54:         url = url,
 55:         size = targetSize,
 56:         placeholder = placeholder,
 57:         error = placeholder
 58:     )
 59: 
 60:     SubcomposeAsyncImage(
 61:         model = request,
 62:         contentDescription = contentDescription,
 63:         imageLoader = imageLoader,
 64:         modifier = modifier,
 65:         contentScale = contentScale,
 66:         loading = {
 67:             if (showLoadingIndicator) {
 68:                 Box(
 69:                     modifier = Modifier.fillMaxSize(),
 70:                     contentAlignment = Alignment.Center
 71:                 ) {
 72:                     CircularProgressIndicator(
 73:                         color = AppColors.Primary,
 74:                         modifier = Modifier.size(24.dp)
 75:                     )
 76:                 }
 77:             } else {
 78:                 Box(
 79:                     modifier = Modifier
 80:                         .fillMaxSize()
 81:                         .background(AppColors.LightGray)
 82:                 )
 83:             }
 84:         },
 85:         error = {
 86:             Box(
 87:                 modifier = Modifier.fillMaxSize(),
 88:                 contentAlignment = Alignment.Center
 89:             ) {
 90:                 Icon(
 91:                     imageVector = Icons.Default.LocalBar,
 92:                     contentDescription = "Image not available",
 93:                     tint = AppColors.Gray,
 94:                     modifier = Modifier.size(48.dp)
 95:                 )
 96:             }
 97:         }
 98:     )
 99: }
100: 
101: /**
102:  * A lightweight optimized image component without loading indicators.
103:  * Use this for smaller images or when loading indicators aren't needed.
104:  */
105: @Composable
106: fun LightweightOptimizedImage(
107:     url: String?,
108:     contentDescription: String?,
109:     modifier: Modifier = Modifier,
110:     contentScale: ContentScale = ContentScale.Crop,
111:     targetSize: Int? = null
112: ) {
113:     val context = LocalContext.current
114:     val imageLoader = ImageLoaderSingleton.getImageLoader(context)
115: 
116:     // Create placeholder and error drawables
117:     val placeholderColor = AppColors.LightGray.toArgb()
118:     val placeholder = ColorDrawable(placeholderColor)
119: 
120:     // Build the optimized image request
121:     val request = ImageUtils.buildOptimizedImageRequest(
122:         url = url,
123:         size = targetSize,
124:         placeholder = placeholder,
125:         error = placeholder
126:     )
127: 
128:     // Use AsyncImage instead of rememberAsyncImagePainter for simplicity
129:     AsyncImage(
130:         model = request,
131:         contentDescription = contentDescription,
132:         imageLoader = imageLoader,
133:         modifier = modifier,
134:         contentScale = contentScale
135:     )
136: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/OrderSummaryCard.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.foundation.layout.Arrangement
  4: import androidx.compose.foundation.layout.Column
  5: import androidx.compose.foundation.layout.Row
  6: import androidx.compose.foundation.layout.Spacer
  7: import androidx.compose.foundation.layout.fillMaxWidth
  8: import androidx.compose.foundation.layout.height
  9: import androidx.compose.foundation.layout.padding
 10: import androidx.compose.foundation.shape.RoundedCornerShape
 11: import androidx.compose.material3.Card
 12: import androidx.compose.material3.CardDefaults
 13: import androidx.compose.material3.Divider
 14: import androidx.compose.material3.Text
 15: import androidx.compose.runtime.Composable
 16: import androidx.compose.ui.Modifier
 17: import androidx.compose.ui.text.font.FontWeight
 18: import androidx.compose.ui.unit.dp
 19: import androidx.compose.ui.unit.sp
 20: import com.cocktailcraft.ui.theme.AppColors
 21: import java.text.NumberFormat
 22: import java.util.Locale
 23: 
 24: /**
 25:  * A reusable order summary card component.
 26:  *
 27:  * @param subtotal The subtotal amount
 28:  * @param deliveryFee The delivery fee amount
 29:  * @param total The total amount (if null, calculated as subtotal + deliveryFee)
 30:  * @param modifier The modifier for the component
 31:  * @param currencyFormatter The formatter for currency values
 32:  * @param showDeliveryFee Whether to show the delivery fee row
 33:  * @param additionalItems Optional list of additional items to display
 34:  */
 35: @Composable
 36: fun OrderSummaryCard(
 37:     subtotal: Double,
 38:     deliveryFee: Double = 5.99,
 39:     total: Double? = null,
 40:     modifier: Modifier = Modifier,
 41:     currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US),
 42:     showDeliveryFee: Boolean = true,
 43:     additionalItems: List<Pair<String, Double>>? = null
 44: ) {
 45:     val calculatedTotal = total ?: (subtotal + deliveryFee)
 46:     
 47:     Card(
 48:         modifier = modifier.fillMaxWidth(),
 49:         colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
 50:         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
 51:         shape = RoundedCornerShape(12.dp)
 52:     ) {
 53:         Column(
 54:             modifier = Modifier
 55:                 .fillMaxWidth()
 56:                 .padding(16.dp)
 57:         ) {
 58:             Text(
 59:                 text = "Order Summary",
 60:                 fontSize = 18.sp,
 61:                 fontWeight = FontWeight.Bold,
 62:                 color = AppColors.TextPrimary
 63:             )
 64: 
 65:             Spacer(modifier = Modifier.height(12.dp))
 66: 
 67:             // Subtotal row
 68:             SummaryRow(
 69:                 label = "Subtotal",
 70:                 value = currencyFormatter.format(subtotal)
 71:             )
 72:             
 73:             // Delivery fee row
 74:             if (showDeliveryFee) {
 75:                 Spacer(modifier = Modifier.height(8.dp))
 76:                 SummaryRow(
 77:                     label = "Delivery Fee",
 78:                     value = currencyFormatter.format(deliveryFee)
 79:                 )
 80:             }
 81:             
 82:             // Additional items
 83:             additionalItems?.forEach { (label, value) ->
 84:                 Spacer(modifier = Modifier.height(8.dp))
 85:                 SummaryRow(
 86:                     label = label,
 87:                     value = currencyFormatter.format(value)
 88:                 )
 89:             }
 90: 
 91:             Spacer(modifier = Modifier.height(12.dp))
 92:             
 93:             Divider(color = AppColors.LightGray)
 94:             
 95:             Spacer(modifier = Modifier.height(12.dp))
 96:             
 97:             // Total row
 98:             Row(
 99:                 modifier = Modifier.fillMaxWidth(),
100:                 horizontalArrangement = Arrangement.SpaceBetween
101:             ) {
102:                 Text(
103:                     text = "Total",
104:                     fontSize = 16.sp,
105:                     fontWeight = FontWeight.Bold,
106:                     color = AppColors.TextPrimary
107:                 )
108:                 Text(
109:                     text = currencyFormatter.format(calculatedTotal),
110:                     fontWeight = FontWeight.Bold,
111:                     fontSize = 16.sp,
112:                     color = AppColors.Primary
113:                 )
114:             }
115:         }
116:     }
117: }
118: 
119: @Composable
120: private fun SummaryRow(
121:     label: String,
122:     value: String
123: ) {
124:     Row(
125:         modifier = Modifier.fillMaxWidth(),
126:         horizontalArrangement = Arrangement.SpaceBetween
127:     ) {
128:         Text(
129:             text = label,
130:             fontSize = 15.sp,
131:             color = AppColors.TextSecondary
132:         )
133:         Text(
134:             text = value,
135:             fontWeight = FontWeight.Medium,
136:             fontSize = 15.sp,
137:             color = AppColors.TextPrimary
138:         )
139:     }
140: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/ProfileCard.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.foundation.background
  4: import androidx.compose.foundation.layout.Box
  5: import androidx.compose.foundation.layout.Column
  6: import androidx.compose.foundation.layout.Spacer
  7: import androidx.compose.foundation.layout.fillMaxWidth
  8: import androidx.compose.foundation.layout.height
  9: import androidx.compose.foundation.layout.padding
 10: import androidx.compose.foundation.layout.size
 11: import androidx.compose.foundation.shape.CircleShape
 12: import androidx.compose.foundation.shape.RoundedCornerShape
 13: import androidx.compose.material3.Button
 14: import androidx.compose.material3.ButtonDefaults
 15: import androidx.compose.material3.Card
 16: import androidx.compose.material3.CardDefaults
 17: import androidx.compose.material3.OutlinedButton
 18: import androidx.compose.material3.Text
 19: import androidx.compose.runtime.Composable
 20: import androidx.compose.ui.Alignment
 21: import androidx.compose.ui.Modifier
 22: import androidx.compose.ui.text.font.FontWeight
 23: import androidx.compose.ui.text.style.TextAlign
 24: import androidx.compose.ui.unit.dp
 25: import androidx.compose.ui.unit.sp
 26: import com.cocktailcraft.ui.theme.AppColors
 27: 
 28: /**
 29:  * A reusable profile card component that displays user information and sign-in/sign-up buttons if not signed in.
 30:  *
 31:  * @param userName The name of the user
 32:  * @param userEmail The email of the user
 33:  * @param isSignedIn Whether the user is signed in
 34:  * @param onSignIn The callback for when the sign-in button is clicked
 35:  * @param onSignUp The callback for when the sign-up button is clicked
 36:  * @param modifier The modifier for the component
 37:  * @param avatarSize The size of the avatar
 38:  * @param avatarBackgroundColor The background color of the avatar
 39:  * @param avatarTextColor The color of the avatar text
 40:  * @param nameTextSize The font size of the name text
 41:  * @param emailTextSize The font size of the email text
 42:  * @param signInButtonText The text for the sign-in button
 43:  * @param signUpButtonText The text for the sign-up button
 44:  * @param notSignedInMessage The message to display when not signed in
 45:  */
 46: @Composable
 47: fun ProfileCard(
 48:     userName: String,
 49:     userEmail: String,
 50:     isSignedIn: Boolean,
 51:     onSignIn: () -> Unit,
 52:     onSignUp: () -> Unit,
 53:     modifier: Modifier = Modifier,
 54:     avatarSize: Int = 80,
 55:     avatarBackgroundColor: androidx.compose.ui.graphics.Color = AppColors.Primary.copy(alpha = 0.2f),
 56:     avatarTextColor: androidx.compose.ui.graphics.Color = AppColors.Primary,
 57:     nameTextSize: Int = 20,
 58:     emailTextSize: Int = 14,
 59:     signInButtonText: String = "Sign In",
 60:     signUpButtonText: String = "Create Account",
 61:     notSignedInMessage: String = "Sign in to access your profile"
 62: ) {
 63:     Card(
 64:         modifier = modifier
 65:             .fillMaxWidth()
 66:             .padding(bottom = 16.dp),
 67:         shape = RoundedCornerShape(16.dp),
 68:         colors = CardDefaults.cardColors(
 69:             containerColor = AppColors.Surface
 70:         ),
 71:         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
 72:     ) {
 73:         Column(
 74:             modifier = Modifier
 75:                 .padding(16.dp)
 76:                 .fillMaxWidth(),
 77:             horizontalAlignment = Alignment.CenterHorizontally
 78:         ) {
 79:             // Profile picture
 80:             Box(
 81:                 modifier = Modifier
 82:                     .size(avatarSize.dp)
 83:                     .background(avatarBackgroundColor, CircleShape),
 84:                 contentAlignment = Alignment.Center
 85:             ) {
 86:                 Text(
 87:                     text = userName.take(1).uppercase(),
 88:                     fontSize = (avatarSize / 2.5).sp,
 89:                     fontWeight = FontWeight.Bold,
 90:                     color = avatarTextColor
 91:                 )
 92:             }
 93: 
 94:             Spacer(modifier = Modifier.height(16.dp))
 95: 
 96:             // User name
 97:             Text(
 98:                 text = userName,
 99:                 fontSize = nameTextSize.sp,
100:                 fontWeight = FontWeight.Bold,
101:                 color = AppColors.TextPrimary
102:             )
103: 
104:             Spacer(modifier = Modifier.height(4.dp))
105: 
106:             // User email
107:             Text(
108:                 text = userEmail,
109:                 fontSize = emailTextSize.sp,
110:                 color = AppColors.TextSecondary
111:             )
112: 
113:             // Show login/signup buttons if not signed in
114:             if (!isSignedIn) {
115:                 Spacer(modifier = Modifier.height(24.dp))
116: 
117:                 Text(
118:                     text = notSignedInMessage,
119:                     fontSize = 16.sp,
120:                     color = AppColors.TextSecondary,
121:                     textAlign = TextAlign.Center,
122:                     modifier = Modifier.padding(bottom = 16.dp)
123:                 )
124: 
125:                 Button(
126:                     onClick = onSignIn,
127:                     colors = ButtonDefaults.buttonColors(
128:                         containerColor = AppColors.Primary
129:                     ),
130:                     modifier = Modifier
131:                         .fillMaxWidth(0.8f)
132:                         .padding(vertical = 4.dp)
133:                 ) {
134:                     Text(signInButtonText)
135:                 }
136: 
137:                 Spacer(modifier = Modifier.height(8.dp))
138: 
139:                 OutlinedButton(
140:                     onClick = onSignUp,
141:                     modifier = Modifier
142:                         .fillMaxWidth(0.8f)
143:                         .padding(vertical = 4.dp)
144:                 ) {
145:                     Text(signUpButtonText)
146:                 }
147:             }
148:         }
149:     }
150: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/RatingBar.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.layout.Row
 4: import androidx.compose.foundation.layout.Spacer
 5: import androidx.compose.foundation.layout.size
 6: import androidx.compose.foundation.layout.width
 7: import androidx.compose.material.icons.Icons
 8: import androidx.compose.material.icons.filled.Star
 9: import androidx.compose.material.icons.filled.StarHalf
10: import androidx.compose.material.icons.filled.StarOutline
11: import androidx.compose.material3.Icon
12: import androidx.compose.runtime.Composable
13: import androidx.compose.ui.Modifier
14: import androidx.compose.ui.graphics.Color
15: import androidx.compose.ui.unit.Dp
16: import androidx.compose.ui.unit.dp
17: import com.cocktailcraft.ui.theme.AppColors
18: 
19: /**
20:  * A reusable rating bar component that displays a row of stars representing a rating.
21:  * 
22:  * @param rating The rating value to display (0.0 to max number of stars)
23:  * @param modifier Optional Modifier for the component
24:  * @param stars Number of total stars to display
25:  * @param starsColor Color for the stars
26:  * @param starSize Size of each star
27:  * @param spaceBetween Space between stars
28:  * @param useHalfStars Whether to use half-star icons for more precise display
29:  */
30: @Composable
31: fun RatingBar(
32:     rating: Float,
33:     modifier: Modifier = Modifier,
34:     stars: Int = 5,
35:     starsColor: Color = AppColors.Secondary,
36:     starSize: Dp = 16.dp,
37:     spaceBetween: Dp = 2.dp,
38:     useHalfStars: Boolean = false
39: ) {
40:     Row(modifier = modifier) {
41:         if (useHalfStars) {
42:             // Implementation with half stars for more precise display
43:             repeat(stars) { index ->
44:                 val starValue = index + 1
45:                 val starIcon = when {
46:                     starValue <= rating -> Icons.Filled.Star
47:                     starValue - 0.5f <= rating -> Icons.Filled.StarHalf
48:                     else -> Icons.Filled.StarOutline
49:                 }
50:                 
51:                 Icon(
52:                     imageVector = starIcon,
53:                     contentDescription = null,
54:                     tint = starsColor,
55:                     modifier = Modifier.size(starSize)
56:                 )
57:                 
58:                 if (index < stars - 1) {
59:                     Spacer(modifier = Modifier.width(spaceBetween))
60:                 }
61:             }
62:         } else {
63:             // Original implementation with alpha for partial stars
64:             repeat(stars) { index ->
65:                 val starAlpha = when {
66:                     index < rating.toInt() -> 1f
67:                     index == rating.toInt() && rating % 1 != 0f -> rating % 1
68:                     else -> 0.3f
69:                 }
70:                 
71:                 Icon(
72:                     imageVector = Icons.Filled.Star,
73:                     contentDescription = null,
74:                     tint = starsColor.copy(alpha = starAlpha),
75:                     modifier = Modifier.size(starSize)
76:                 )
77:                 
78:                 if (index < stars - 1) {
79:                     Spacer(modifier = Modifier.width(spaceBetween))
80:                 }
81:             }
82:         }
83:     }
84: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/RatingDisplay.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.layout.Arrangement
 4: import androidx.compose.foundation.layout.Row
 5: import androidx.compose.foundation.layout.Spacer
 6: import androidx.compose.foundation.layout.width
 7: import androidx.compose.material3.Text
 8: import androidx.compose.runtime.Composable
 9: import androidx.compose.ui.Alignment
10: import androidx.compose.ui.Modifier
11: import androidx.compose.ui.text.font.FontWeight
12: import androidx.compose.ui.unit.dp
13: import androidx.compose.ui.unit.sp
14: import com.cocktailcraft.ui.theme.AppColors
15: 
16: /**
17:  * A reusable rating display component that shows a rating with stars and review count.
18:  *
19:  * @param rating The rating value to display
20:  * @param reviewCount The number of reviews
21:  * @param modifier The modifier for the component
22:  * @param showReviewCount Whether to show the review count
23:  * @param starSize The size of each star
24:  * @param ratingTextSize The font size of the rating text
25:  * @param reviewCountTextSize The font size of the review count text
26:  * @param starsColor The color of the stars
27:  * @param textColor The color of the text
28:  * @param useHalfStars Whether to use half-star icons for more precise display
29:  */
30: @Composable
31: fun RatingDisplay(
32:     rating: Float,
33:     reviewCount: Int,
34:     modifier: Modifier = Modifier,
35:     showReviewCount: Boolean = true,
36:     starSize: Int = 16,
37:     ratingTextSize: Int = 14,
38:     reviewCountTextSize: Int = 14,
39:     starsColor: androidx.compose.ui.graphics.Color = AppColors.Secondary,
40:     textColor: androidx.compose.ui.graphics.Color = AppColors.TextSecondary,
41:     useHalfStars: Boolean = true
42: ) {
43:     Row(
44:         modifier = modifier,
45:         verticalAlignment = Alignment.CenterVertically,
46:         horizontalArrangement = Arrangement.Start
47:     ) {
48:         // Display the rating value
49:         Text(
50:             text = String.format("%.1f", rating),
51:             fontSize = ratingTextSize.sp,
52:             fontWeight = FontWeight.Bold,
53:             color = textColor
54:         )
55:         
56:         Spacer(modifier = Modifier.width(4.dp))
57:         
58:         // Display the stars
59:         RatingBar(
60:             rating = rating,
61:             starSize = starSize.dp,
62:             starsColor = starsColor,
63:             useHalfStars = useHalfStars
64:         )
65:         
66:         // Display the review count if requested
67:         if (showReviewCount && reviewCount > 0) {
68:             Spacer(modifier = Modifier.width(4.dp))
69:             
70:             Text(
71:                 text = "($reviewCount ${if (reviewCount == 1) "review" else "reviews"})",
72:                 fontSize = reviewCountTextSize.sp,
73:                 color = textColor
74:             )
75:         }
76:     }
77: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/RecommendationsSection.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.AnimatedVisibility
  4: import androidx.compose.animation.core.tween
  5: import androidx.compose.animation.fadeIn
  6: import androidx.compose.animation.slideInHorizontally
  7: import androidx.compose.foundation.background
  8: import androidx.compose.foundation.clickable
  9: import androidx.compose.foundation.layout.Arrangement
 10: import androidx.compose.foundation.layout.Box
 11: import androidx.compose.foundation.layout.Column
 12: import androidx.compose.foundation.layout.PaddingValues
 13: import androidx.compose.foundation.layout.Row
 14: import androidx.compose.foundation.layout.Spacer
 15: import androidx.compose.foundation.layout.fillMaxSize
 16: import androidx.compose.foundation.layout.fillMaxWidth
 17: import androidx.compose.foundation.layout.height
 18: import androidx.compose.foundation.layout.padding
 19: import androidx.compose.foundation.layout.size
 20: import androidx.compose.foundation.layout.width
 21: import androidx.compose.foundation.lazy.LazyRow
 22: import androidx.compose.foundation.lazy.items
 23: import androidx.compose.foundation.lazy.itemsIndexed
 24: import androidx.compose.foundation.shape.RoundedCornerShape
 25: import androidx.compose.material.icons.Icons
 26: import androidx.compose.material.icons.filled.LocalBar
 27: import androidx.compose.material3.Card
 28: import androidx.compose.material3.CardDefaults
 29: import androidx.compose.material3.Icon
 30: import androidx.compose.material3.MaterialTheme
 31: import androidx.compose.material3.Text
 32: import androidx.compose.runtime.Composable
 33: import androidx.compose.runtime.collectAsState
 34: import androidx.compose.runtime.getValue
 35: import androidx.compose.ui.Alignment
 36: import androidx.compose.ui.Modifier
 37: import androidx.compose.ui.draw.alpha
 38: import androidx.compose.ui.draw.clip
 39: import androidx.compose.ui.graphics.Color
 40: import androidx.compose.ui.layout.ContentScale
 41: import androidx.compose.ui.text.font.FontWeight
 42: import androidx.compose.ui.text.style.TextAlign
 43: import androidx.compose.ui.text.style.TextOverflow
 44: import androidx.compose.ui.unit.dp
 45: import androidx.compose.ui.unit.sp
 46: import com.cocktailcraft.domain.model.Cocktail
 47: import com.cocktailcraft.ui.theme.AppColors
 48: import com.cocktailcraft.viewmodel.CocktailDetailViewModel
 49: 
 50: /**
 51:  * A section that displays cocktail recommendations.
 52:  */
 53: @Composable
 54: fun RecommendationsSection(
 55:     viewModel: CocktailDetailViewModel,
 56:     onCocktailClick: (Cocktail) -> Unit,
 57:     modifier: Modifier = Modifier
 58: ) {
 59:     val recommendations by viewModel.recommendations.collectAsState()
 60:     val isLoading by viewModel.isLoadingRecommendations.collectAsState()
 61: 
 62:     // Determine visibility based on loading state and recommendations
 63:     val isVisible = isLoading || recommendations.isNotEmpty()
 64: 
 65:     // If nothing to show, return an empty Box to maintain layout stability
 66:     if (!isVisible) {
 67:         return Box(modifier = modifier.fillMaxWidth().height(0.dp))
 68:     }
 69: 
 70:     // Use a simpler animation approach that works better in LazyColumn
 71:     Box(
 72:         modifier = modifier
 73:             .fillMaxWidth()
 74:             .alpha(1f) // Always fully visible once we reach this point
 75:     ) {
 76:         Column(
 77:             modifier = modifier
 78:                 .fillMaxWidth()
 79:                 .padding(top = 16.dp)
 80:         ) {
 81:             // Section title
 82:             Text(
 83:                 text = "You might also like",
 84:                 style = MaterialTheme.typography.titleMedium.copy(
 85:                     fontWeight = FontWeight.Bold,
 86:                     fontSize = 18.sp
 87:                 ),
 88:                 color = AppColors.TextPrimary,
 89:                 modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
 90:             )
 91: 
 92:             Spacer(modifier = Modifier.height(8.dp))
 93: 
 94:             if (isLoading) {
 95:                 // Show loading state
 96:                 LazyRow(
 97:                     contentPadding = PaddingValues(horizontal = 16.dp),
 98:                     horizontalArrangement = Arrangement.spacedBy(12.dp)
 99:                 ) {
100:                     items(3) {
101:                         RecommendationItemShimmer()
102:                     }
103:                 }
104:             } else {
105:                 // Show recommendations
106:                 LazyRow(
107:                     contentPadding = PaddingValues(horizontal = 16.dp),
108:                     horizontalArrangement = Arrangement.spacedBy(12.dp)
109:                 ) {
110:                     items(recommendations) { cocktail ->
111:                         RecommendationItem(
112:                             cocktail = cocktail,
113:                             onClick = { onCocktailClick(cocktail) }
114:                         )
115:                     }
116:                 }
117:             }
118:         }
119:     }
120: }
121: 
122: /**
123:  * A card displaying a recommended cocktail.
124:  */
125: @Composable
126: fun RecommendationItem(
127:     cocktail: Cocktail,
128:     onClick: () -> Unit,
129:     modifier: Modifier = Modifier
130: ) {
131:     Card(
132:         modifier = modifier
133:             .width(140.dp)
134:             .clickable(onClick = onClick),
135:         shape = RoundedCornerShape(12.dp),
136:         colors = CardDefaults.cardColors(
137:             containerColor = AppColors.Surface
138:         ),
139:         elevation = CardDefaults.cardElevation(
140:             defaultElevation = 2.dp
141:         )
142:     ) {
143:         Column {
144:             // Cocktail image
145:             Box(
146:                 modifier = Modifier
147:                     .height(140.dp)
148:                     .fillMaxWidth()
149:                     .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
150:                     .background(AppColors.LightGray)
151:             ) {
152:                 // Use our optimized image component
153:                 OptimizedImage(
154:                     url = cocktail.imageUrl,
155:                     contentDescription = cocktail.name,
156:                     modifier = Modifier.fillMaxSize(),
157:                     contentScale = ContentScale.Crop,
158:                     targetSize = 200,
159:                     showLoadingIndicator = false
160:                 )
161: 
162:                 // Show alcoholic badge if applicable
163:                 if (cocktail.alcoholic == "Alcoholic") {
164:                     Box(
165:                         modifier = Modifier
166:                             .align(Alignment.TopEnd)
167:                             .padding(4.dp)
168:                             .size(24.dp)
169:                             .clip(RoundedCornerShape(4.dp))
170:                             .background(AppColors.Primary.copy(alpha = 0.8f)),
171:                         contentAlignment = Alignment.Center
172:                     ) {
173:                         Icon(
174:                             imageVector = Icons.Default.LocalBar,
175:                             contentDescription = "Alcoholic",
176:                             tint = Color.White,
177:                             modifier = Modifier.size(16.dp)
178:                         )
179:                     }
180:                 }
181:             }
182: 
183:             // Cocktail details
184:             Column(
185:                 modifier = Modifier.padding(8.dp)
186:             ) {
187:                 // Cocktail name
188:                 Text(
189:                     text = cocktail.name,
190:                     style = MaterialTheme.typography.bodyMedium.copy(
191:                         fontWeight = FontWeight.Bold
192:                     ),
193:                     color = AppColors.TextPrimary,
194:                     maxLines = 1,
195:                     overflow = TextOverflow.Ellipsis
196:                 )
197: 
198:                 Spacer(modifier = Modifier.height(4.dp))
199: 
200:                 // Cocktail category
201:                 cocktail.category?.let {
202:                     Text(
203:                         text = it,
204:                         style = MaterialTheme.typography.bodySmall,
205:                         color = AppColors.TextSecondary,
206:                         maxLines = 1,
207:                         overflow = TextOverflow.Ellipsis
208:                     )
209:                 }
210: 
211:                 Spacer(modifier = Modifier.height(4.dp))
212: 
213:                 // Price
214:                 Text(
215:                     text = "$${String.format("%.2f", cocktail.price)}",
216:                     style = MaterialTheme.typography.bodyMedium.copy(
217:                         fontWeight = FontWeight.Bold
218:                     ),
219:                     color = AppColors.Primary
220:                 )
221:             }
222:         }
223:     }
224: }
225: 
226: /**
227:  * A shimmer loading placeholder for recommendation items.
228:  */
229: @Composable
230: fun RecommendationItemShimmer(
231:     modifier: Modifier = Modifier
232: ) {
233:     Card(
234:         modifier = modifier.width(140.dp),
235:         shape = RoundedCornerShape(12.dp),
236:         colors = CardDefaults.cardColors(
237:             containerColor = AppColors.Surface
238:         ),
239:         elevation = CardDefaults.cardElevation(
240:             defaultElevation = 2.dp
241:         )
242:     ) {
243:         Column {
244:             // Image placeholder
245:             Box(
246:                 modifier = Modifier
247:                     .height(140.dp)
248:                     .fillMaxWidth()
249:                     .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
250:                     .shimmerEffect()
251:             )
252: 
253:             // Content placeholders
254:             Column(
255:                 modifier = Modifier.padding(8.dp)
256:             ) {
257:                 // Title placeholder
258:                 Box(
259:                     modifier = Modifier
260:                         .fillMaxWidth(0.8f)
261:                         .height(16.dp)
262:                         .clip(RoundedCornerShape(4.dp))
263:                         .shimmerEffect()
264:                 )
265: 
266:                 Spacer(modifier = Modifier.height(8.dp))
267: 
268:                 // Category placeholder
269:                 Box(
270:                     modifier = Modifier
271:                         .fillMaxWidth(0.6f)
272:                         .height(12.dp)
273:                         .clip(RoundedCornerShape(4.dp))
274:                         .shimmerEffect()
275:                 )
276: 
277:                 Spacer(modifier = Modifier.height(8.dp))
278: 
279:                 // Price placeholder
280:                 Box(
281:                     modifier = Modifier
282:                         .width(40.dp)
283:                         .height(16.dp)
284:                         .clip(RoundedCornerShape(4.dp))
285:                         .shimmerEffect()
286:                 )
287:             }
288:         }
289:     }
290: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/SearchFilterChips.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.AnimatedVisibility
  4: import androidx.compose.animation.fadeIn
  5: import androidx.compose.animation.fadeOut
  6: import androidx.compose.foundation.ExperimentalFoundationApi
  7: import androidx.compose.foundation.background
  8: import androidx.compose.foundation.clickable
  9: import androidx.compose.foundation.layout.Arrangement
 10: import androidx.compose.foundation.layout.Box
 11: import androidx.compose.foundation.layout.ExperimentalLayoutApi
 12: import androidx.compose.foundation.layout.FlowRow
 13: import androidx.compose.foundation.layout.Row
 14: import androidx.compose.foundation.layout.Spacer
 15: import androidx.compose.foundation.layout.fillMaxWidth
 16: import androidx.compose.foundation.layout.height
 17: import androidx.compose.foundation.layout.padding
 18: import androidx.compose.foundation.layout.size
 19: import androidx.compose.foundation.layout.width
 20: import androidx.compose.foundation.shape.RoundedCornerShape
 21: import androidx.compose.material.icons.Icons
 22: import androidx.compose.material.icons.filled.Clear
 23: import androidx.compose.material.icons.filled.FilterAlt
 24: import androidx.compose.material3.Icon
 25: import androidx.compose.material3.MaterialTheme
 26: import androidx.compose.material3.Surface
 27: import androidx.compose.material3.Text
 28: import androidx.compose.runtime.Composable
 29: import androidx.compose.ui.Alignment
 30: import androidx.compose.ui.Modifier
 31: import androidx.compose.ui.draw.clip
 32: import androidx.compose.ui.graphics.Color
 33: import androidx.compose.ui.graphics.vector.ImageVector
 34: import androidx.compose.ui.text.style.TextOverflow
 35: import androidx.compose.ui.unit.dp
 36: import com.cocktailcraft.domain.model.SearchFilters
 37: import com.cocktailcraft.ui.theme.AppColors
 38: 
 39: /**
 40:  * Component to display active search filters as chips
 41:  */
 42: @OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
 43: @Composable
 44: fun SearchFilterChips(
 45:     filters: SearchFilters,
 46:     onClearFilter: (String) -> Unit,
 47:     onClearAllFilters: () -> Unit
 48: ) {
 49:     // Only show if we have active filters
 50:     AnimatedVisibility(
 51:         visible = filters.hasActiveFilters(),
 52:         enter = fadeIn(),
 53:         exit = fadeOut()
 54:     ) {
 55:         Surface(
 56:             modifier = Modifier
 57:                 .fillMaxWidth()
 58:                 .padding(horizontal = 16.dp, vertical = 8.dp),
 59:             color = AppColors.Surface,
 60:             shape = RoundedCornerShape(8.dp)
 61:         ) {
 62:             Column(
 63:                 modifier = Modifier
 64:                     .fillMaxWidth()
 65:                     .padding(8.dp)
 66:             ) {
 67:                 // Header with filter icon and clear all button
 68:                 Row(
 69:                     modifier = Modifier.fillMaxWidth(),
 70:                     verticalAlignment = Alignment.CenterVertically
 71:                 ) {
 72:                     Icon(
 73:                         imageVector = Icons.Default.FilterAlt,
 74:                         contentDescription = "Filters",
 75:                         tint = AppColors.Primary,
 76:                         modifier = Modifier.size(16.dp)
 77:                     )
 78: 
 79:                     Spacer(modifier = Modifier.width(4.dp))
 80: 
 81:                     Text(
 82:                         text = "Active Filters",
 83:                         style = MaterialTheme.typography.bodySmall,
 84:                         color = AppColors.TextSecondary
 85:                     )
 86: 
 87:                     Spacer(modifier = Modifier.weight(1f))
 88: 
 89:                     Text(
 90:                         text = "Clear All",
 91:                         style = MaterialTheme.typography.bodySmall,
 92:                         color = AppColors.Primary,
 93:                         modifier = Modifier.clickable { onClearAllFilters() }
 94:                     )
 95:                 }
 96: 
 97:                 Spacer(modifier = Modifier.height(8.dp))
 98: 
 99:                 // Filter chips
100:                 FlowRow(
101:                     modifier = Modifier.fillMaxWidth(),
102:                     horizontalArrangement = Arrangement.spacedBy(8.dp),
103:                     verticalArrangement = Arrangement.spacedBy(8.dp)
104:                 ) {
105:                     // Category filter
106:                     if (filters.category != null) {
107:                         ActiveFilterChip(
108:                             label = "Category: ${filters.category}",
109:                             onClear = { onClearFilter("category") }
110:                         )
111:                     }
112: 
113:                     // Ingredient filter
114:                     if (filters.ingredient != null) {
115:                         ActiveFilterChip(
116:                             label = "Ingredient: ${filters.ingredient}",
117:                             onClear = { onClearFilter("ingredient") }
118:                         )
119:                     }
120: 
121:                     // Multiple ingredients
122:                     if (filters.ingredients.isNotEmpty()) {
123:                         ActiveFilterChip(
124:                             label = "Ingredients: ${filters.ingredients.size}",
125:                             onClear = { onClearFilter("ingredients") }
126:                         )
127:                     }
128: 
129:                     // Excluded ingredients
130:                     if (filters.excludeIngredients.isNotEmpty()) {
131:                         ActiveFilterChip(
132:                             label = "Excluded: ${filters.excludeIngredients.size}",
133:                             onClear = { onClearFilter("excludeIngredients") }
134:                         )
135:                     }
136: 
137:                     // Alcoholic filter
138:                     if (filters.alcoholic != null) {
139:                         val alcoholicLabel = when (filters.alcoholic) {
140:                             true -> "Alcoholic"
141:                             false -> "Non-Alcoholic"
142:                             else -> "Alcoholic Filter"
143:                         }
144:                         ActiveFilterChip(
145:                             label = alcoholicLabel,
146:                             onClear = { onClearFilter("alcoholic") }
147:                         )
148:                     }
149: 
150:                     // Glass filter
151:                     if (filters.glass != null) {
152:                         ActiveFilterChip(
153:                             label = "Glass: ${filters.glass}",
154:                             onClear = { onClearFilter("glass") }
155:                         )
156:                     }
157: 
158:                     // Price range filter
159:                     if (filters.priceRange != null) {
160:                         val priceRange = filters.priceRange!! // Safe to use !! here as we've checked for null
161:                         val priceLabel = "Price: $${priceRange.start.toInt()}-$${priceRange.endInclusive.toInt()}"
162:                         ActiveFilterChip(
163:                             label = priceLabel,
164:                             onClear = { onClearFilter("priceRange") }
165:                         )
166:                     }
167: 
168:                     // Taste profile filter
169:                     if (filters.tasteProfile != null) {
170:                         ActiveFilterChip(
171:                             label = "Taste: ${filters.tasteProfile}",
172:                             onClear = { onClearFilter("tasteProfile") }
173:                         )
174:                     }
175: 
176:                     // Complexity filter
177:                     if (filters.complexity != null) {
178:                         ActiveFilterChip(
179:                             label = "Complexity: ${filters.complexity}",
180:                             onClear = { onClearFilter("complexity") }
181:                         )
182:                     }
183: 
184:                     // Preparation time filter
185:                     if (filters.preparationTime != null) {
186:                         ActiveFilterChip(
187:                             label = "Prep Time: ${filters.preparationTime}",
188:                             onClear = { onClearFilter("preparationTime") }
189:                         )
190:                     }
191:                 }
192:             }
193:         }
194:     }
195: }
196: 
197: @Composable
198: fun ActiveFilterChip(
199:     label: String,
200:     onClear: () -> Unit
201: ) {
202:     Box(
203:         modifier = Modifier
204:             .clip(RoundedCornerShape(16.dp))
205:             .background(AppColors.Primary.copy(alpha = 0.1f))
206:             .padding(horizontal = 12.dp, vertical = 6.dp)
207:     ) {
208:         Row(
209:             verticalAlignment = Alignment.CenterVertically
210:         ) {
211:             Text(
212:                 text = label,
213:                 style = MaterialTheme.typography.bodySmall,
214:                 color = AppColors.Primary,
215:                 maxLines = 1,
216:                 overflow = TextOverflow.Ellipsis
217:             )
218: 
219:             Spacer(modifier = Modifier.width(4.dp))
220: 
221:             Icon(
222:                 imageVector = Icons.Default.Clear,
223:                 contentDescription = "Clear",
224:                 tint = AppColors.Primary,
225:                 modifier = Modifier
226:                     .size(16.dp)
227:                     .clickable { onClear() }
228:             )
229:         }
230:     }
231: }
232: 
233: @Composable
234: fun Column(
235:     modifier: Modifier = Modifier,
236:     content: @Composable () -> Unit
237: ) {
238:     androidx.compose.foundation.layout.Column(
239:         modifier = modifier
240:     ) {
241:         content()
242:     }
243: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/SectionHeader.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.layout.Arrangement
 4: import androidx.compose.foundation.layout.Row
 5: import androidx.compose.foundation.layout.Spacer
 6: import androidx.compose.foundation.layout.fillMaxWidth
 7: import androidx.compose.foundation.layout.padding
 8: import androidx.compose.foundation.layout.width
 9: import androidx.compose.material3.Text
10: import androidx.compose.material3.TextButton
11: import androidx.compose.runtime.Composable
12: import androidx.compose.ui.Alignment
13: import androidx.compose.ui.Modifier
14: import androidx.compose.ui.text.font.FontWeight
15: import androidx.compose.ui.unit.dp
16: import androidx.compose.ui.unit.sp
17: import com.cocktailcraft.ui.theme.AppColors
18: 
19: /**
20:  * A reusable section header component with optional action button.
21:  *
22:  * @param title The title text to display
23:  * @param modifier The modifier for the component
24:  * @param actionText Optional text for the action button (null to hide)
25:  * @param onActionClick Optional callback for when the action button is clicked
26:  * @param titleColor The color of the title text
27:  * @param actionColor The color of the action text
28:  * @param fontSize The font size of the title
29:  * @param fontWeight The font weight of the title
30:  */
31: @Composable
32: fun SectionHeader(
33:     title: String,
34:     modifier: Modifier = Modifier,
35:     actionText: String? = null,
36:     onActionClick: (() -> Unit)? = null,
37:     titleColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
38:     actionColor: androidx.compose.ui.graphics.Color = AppColors.Primary,
39:     fontSize: Int = 18,
40:     fontWeight: FontWeight = FontWeight.Bold
41: ) {
42:     Row(
43:         modifier = modifier
44:             .fillMaxWidth()
45:             .padding(vertical = 8.dp),
46:         horizontalArrangement = Arrangement.SpaceBetween,
47:         verticalAlignment = Alignment.CenterVertically
48:     ) {
49:         Text(
50:             text = title,
51:             fontSize = fontSize.sp,
52:             fontWeight = fontWeight,
53:             color = titleColor
54:         )
55:         
56:         if (actionText != null && onActionClick != null) {
57:             Spacer(modifier = Modifier.width(8.dp))
58:             
59:             TextButton(onClick = onActionClick) {
60:                 Text(
61:                     text = actionText,
62:                     color = actionColor,
63:                     fontWeight = FontWeight.Medium
64:                 )
65:             }
66:         }
67:     }
68: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/SettingsCard.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.layout.Column
 4: import androidx.compose.foundation.layout.fillMaxWidth
 5: import androidx.compose.foundation.layout.padding
 6: import androidx.compose.foundation.shape.RoundedCornerShape
 7: import androidx.compose.material3.Card
 8: import androidx.compose.material3.CardDefaults
 9: import androidx.compose.material3.Text
10: import androidx.compose.runtime.Composable
11: import androidx.compose.ui.Modifier
12: import androidx.compose.ui.text.font.FontWeight
13: import androidx.compose.ui.unit.dp
14: import androidx.compose.ui.unit.sp
15: import com.cocktailcraft.ui.theme.AppColors
16: 
17: /**
18:  * A reusable settings card component that displays a title and content.
19:  *
20:  * @param title The title text of the card
21:  * @param modifier The modifier for the component
22:  * @param content The content to display in the card
23:  * @param titleFontSize The font size of the title
24:  * @param titleFontWeight The font weight of the title
25:  * @param titleColor The color of the title text
26:  * @param backgroundColor The background color of the card
27:  * @param elevation The elevation of the card
28:  * @param cornerRadius The corner radius of the card
29:  * @param contentPadding The padding for the content
30:  * @param titleBottomPadding The padding between the title and content
31:  */
32: @Composable
33: fun SettingsCard(
34:     title: String,
35:     modifier: Modifier = Modifier,
36:     content: @Composable () -> Unit,
37:     titleFontSize: Int = 18,
38:     titleFontWeight: FontWeight = FontWeight.Bold,
39:     titleColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
40:     backgroundColor: androidx.compose.ui.graphics.Color = AppColors.Surface,
41:     elevation: Int = 2,
42:     cornerRadius: Int = 16,
43:     contentPadding: Int = 16,
44:     titleBottomPadding: Int = 16
45: ) {
46:     Card(
47:         modifier = modifier
48:             .fillMaxWidth()
49:             .padding(vertical = 8.dp),
50:         shape = RoundedCornerShape(cornerRadius.dp),
51:         colors = CardDefaults.cardColors(containerColor = backgroundColor),
52:         elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
53:     ) {
54:         Column(
55:             modifier = Modifier.padding(contentPadding.dp)
56:         ) {
57:             Text(
58:                 text = title,
59:                 fontSize = titleFontSize.sp,
60:                 fontWeight = titleFontWeight,
61:                 color = titleColor,
62:                 modifier = Modifier.padding(bottom = titleBottomPadding.dp)
63:             )
64:             
65:             content()
66:         }
67:     }
68: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/ShimmerLoading.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.animation.core.RepeatMode
  4: import androidx.compose.animation.core.animateFloat
  5: import androidx.compose.animation.core.infiniteRepeatable
  6: import androidx.compose.animation.core.rememberInfiniteTransition
  7: import androidx.compose.animation.core.tween
  8: import androidx.compose.foundation.background
  9: import androidx.compose.foundation.layout.Box
 10: import androidx.compose.foundation.layout.Column
 11: import androidx.compose.foundation.layout.Row
 12: import androidx.compose.foundation.layout.Spacer
 13: import androidx.compose.foundation.layout.fillMaxSize
 14: import androidx.compose.foundation.layout.fillMaxWidth
 15: import androidx.compose.foundation.layout.height
 16: import androidx.compose.foundation.layout.padding
 17: import androidx.compose.foundation.layout.size
 18: import androidx.compose.foundation.layout.width
 19: import androidx.compose.foundation.shape.CircleShape
 20: import androidx.compose.foundation.shape.RoundedCornerShape
 21: import androidx.compose.runtime.Composable
 22: import androidx.compose.runtime.getValue
 23: import androidx.compose.ui.Alignment
 24: import androidx.compose.ui.Modifier
 25: import androidx.compose.ui.composed
 26: import androidx.compose.ui.draw.clip
 27: import androidx.compose.ui.geometry.Offset
 28: import androidx.compose.ui.graphics.Brush
 29: import androidx.compose.ui.graphics.Color
 30: import androidx.compose.ui.unit.dp
 31: import com.cocktailcraft.ui.theme.AppColors
 32: 
 33: /**
 34:  * Creates a shimmer effect modifier that can be applied to any composable
 35:  */
 36: fun Modifier.shimmerEffect() = composed {
 37:     val shimmerColors = listOf(
 38:         AppColors.Surface.copy(alpha = 0.6f),
 39:         AppColors.Surface.copy(alpha = 0.2f),
 40:         AppColors.Surface.copy(alpha = 0.6f)
 41:     )
 42:     
 43:     val transition = rememberInfiniteTransition(label = "shimmer")
 44:     val translateAnim by transition.animateFloat(
 45:         initialValue = 0f,
 46:         targetValue = 1000f,
 47:         animationSpec = infiniteRepeatable(
 48:             animation = tween(
 49:                 durationMillis = 1200,
 50:                 delayMillis = 300
 51:             ),
 52:             repeatMode = RepeatMode.Restart
 53:         ),
 54:         label = "shimmer_translate"
 55:     )
 56:     
 57:     val brush = Brush.linearGradient(
 58:         colors = shimmerColors,
 59:         start = Offset(10f, 10f),
 60:         end = Offset(translateAnim, translateAnim)
 61:     )
 62:     
 63:     background(brush)
 64: }
 65: 
 66: /**
 67:  * A shimmer loading placeholder for a cocktail item
 68:  */
 69: @Composable
 70: fun CocktailItemShimmer(
 71:     modifier: Modifier = Modifier
 72: ) {
 73:     Row(
 74:         modifier = modifier
 75:             .fillMaxWidth()
 76:             .padding(12.dp),
 77:         verticalAlignment = Alignment.CenterVertically
 78:     ) {
 79:         // Image placeholder
 80:         Box(
 81:             modifier = Modifier
 82:                 .size(100.dp)
 83:                 .clip(RoundedCornerShape(8.dp))
 84:                 .shimmerEffect()
 85:         )
 86:         
 87:         Spacer(modifier = Modifier.width(12.dp))
 88:         
 89:         // Content placeholders
 90:         Column(
 91:             modifier = Modifier.weight(1f)
 92:         ) {
 93:             // Title placeholder
 94:             Box(
 95:                 modifier = Modifier
 96:                     .fillMaxWidth(0.7f)
 97:                     .height(16.dp)
 98:                     .clip(RoundedCornerShape(4.dp))
 99:                     .shimmerEffect()
100:             )
101:             
102:             Spacer(modifier = Modifier.height(8.dp))
103:             
104:             // Subtitle placeholder
105:             Box(
106:                 modifier = Modifier
107:                     .fillMaxWidth(0.5f)
108:                     .height(14.dp)
109:                     .clip(RoundedCornerShape(4.dp))
110:                     .shimmerEffect()
111:             )
112:             
113:             Spacer(modifier = Modifier.height(16.dp))
114:             
115:             // Action row
116:             Row(
117:                 modifier = Modifier.fillMaxWidth(),
118:                 verticalAlignment = Alignment.CenterVertically
119:             ) {
120:                 // Price placeholder
121:                 Box(
122:                     modifier = Modifier
123:                         .width(60.dp)
124:                         .height(16.dp)
125:                         .clip(RoundedCornerShape(4.dp))
126:                         .shimmerEffect()
127:                 )
128:                 
129:                 Spacer(modifier = Modifier.weight(1f))
130:                 
131:                 // Action buttons placeholders
132:                 Box(
133:                     modifier = Modifier
134:                         .size(24.dp)
135:                         .clip(CircleShape)
136:                         .shimmerEffect()
137:                 )
138:                 
139:                 Spacer(modifier = Modifier.width(16.dp))
140:                 
141:                 Box(
142:                     modifier = Modifier
143:                         .size(24.dp)
144:                         .clip(CircleShape)
145:                         .shimmerEffect()
146:                 )
147:             }
148:         }
149:     }
150: }
151: 
152: /**
153:  * A shimmer loading placeholder for a cocktail detail
154:  */
155: @Composable
156: fun CocktailDetailShimmer(
157:     modifier: Modifier = Modifier
158: ) {
159:     Column(
160:         modifier = modifier
161:             .fillMaxSize()
162:             .padding(16.dp)
163:     ) {
164:         // Image placeholder
165:         Box(
166:             modifier = Modifier
167:                 .fillMaxWidth()
168:                 .height(250.dp)
169:                 .clip(RoundedCornerShape(12.dp))
170:                 .shimmerEffect()
171:         )
172:         
173:         Spacer(modifier = Modifier.height(16.dp))
174:         
175:         // Title placeholder
176:         Box(
177:             modifier = Modifier
178:                 .fillMaxWidth(0.8f)
179:                 .height(24.dp)
180:                 .clip(RoundedCornerShape(4.dp))
181:                 .shimmerEffect()
182:         )
183:         
184:         Spacer(modifier = Modifier.height(8.dp))
185:         
186:         // Rating placeholder
187:         Row(
188:             verticalAlignment = Alignment.CenterVertically
189:         ) {
190:             Box(
191:                 modifier = Modifier
192:                     .width(120.dp)
193:                     .height(20.dp)
194:                     .clip(RoundedCornerShape(4.dp))
195:                     .shimmerEffect()
196:             )
197:             
198:             Spacer(modifier = Modifier.width(8.dp))
199:             
200:             Box(
201:                 modifier = Modifier
202:                     .width(40.dp)
203:                     .height(20.dp)
204:                     .clip(RoundedCornerShape(4.dp))
205:                     .shimmerEffect()
206:             )
207:         }
208:         
209:         Spacer(modifier = Modifier.height(16.dp))
210:         
211:         // Chips row
212:         Row {
213:             Box(
214:                 modifier = Modifier
215:                     .width(100.dp)
216:                     .height(32.dp)
217:                     .clip(RoundedCornerShape(16.dp))
218:                     .shimmerEffect()
219:             )
220:             
221:             Spacer(modifier = Modifier.width(8.dp))
222:             
223:             Box(
224:                 modifier = Modifier
225:                     .width(100.dp)
226:                     .height(32.dp)
227:                     .clip(RoundedCornerShape(16.dp))
228:                     .shimmerEffect()
229:             )
230:         }
231:         
232:         Spacer(modifier = Modifier.height(24.dp))
233:         
234:         // Description placeholder
235:         repeat(3) {
236:             Box(
237:                 modifier = Modifier
238:                     .fillMaxWidth()
239:                     .height(16.dp)
240:                     .clip(RoundedCornerShape(4.dp))
241:                     .shimmerEffect()
242:             )
243:             
244:             Spacer(modifier = Modifier.height(8.dp))
245:         }
246:         
247:         Spacer(modifier = Modifier.height(16.dp))
248:         
249:         // Ingredients title
250:         Box(
251:             modifier = Modifier
252:                 .width(120.dp)
253:                 .height(20.dp)
254:                 .clip(RoundedCornerShape(4.dp))
255:                 .shimmerEffect()
256:         )
257:         
258:         Spacer(modifier = Modifier.height(8.dp))
259:         
260:         // Ingredients list
261:         repeat(4) {
262:             Box(
263:                 modifier = Modifier
264:                     .fillMaxWidth(0.7f)
265:                     .height(16.dp)
266:                     .clip(RoundedCornerShape(4.dp))
267:                     .shimmerEffect()
268:             )
269:             
270:             Spacer(modifier = Modifier.height(8.dp))
271:         }
272:     }
273: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/StatusIndicator.kt
````kotlin
 1: package com.cocktailcraft.ui.components
 2: 
 3: import androidx.compose.foundation.background
 4: import androidx.compose.foundation.layout.Box
 5: import androidx.compose.foundation.layout.Row
 6: import androidx.compose.foundation.layout.Spacer
 7: import androidx.compose.foundation.layout.size
 8: import androidx.compose.foundation.layout.width
 9: import androidx.compose.foundation.shape.CircleShape
10: import androidx.compose.material3.Icon
11: import androidx.compose.material3.Text
12: import androidx.compose.runtime.Composable
13: import androidx.compose.ui.Alignment
14: import androidx.compose.ui.Modifier
15: import androidx.compose.ui.graphics.Color
16: import androidx.compose.ui.graphics.vector.ImageVector
17: import androidx.compose.ui.text.font.FontWeight
18: import androidx.compose.ui.unit.dp
19: import androidx.compose.ui.unit.sp
20: import com.cocktailcraft.ui.theme.AppColors
21: 
22: /**
23:  * A reusable status indicator component that displays a colored dot or icon with status text.
24:  *
25:  * @param status The status text to display
26:  * @param isActive Whether the status is active
27:  * @param activeColor The color to use when status is active
28:  * @param inactiveColor The color to use when status is inactive
29:  * @param icon Optional icon to display instead of a dot
30:  * @param modifier The modifier for the component
31:  * @param textSize The font size of the status text
32:  * @param indicatorSize The size of the dot or icon
33:  */
34: @Composable
35: fun StatusIndicator(
36:     status: String,
37:     isActive: Boolean,
38:     activeColor: Color,
39:     inactiveColor: Color = AppColors.Gray,
40:     icon: ImageVector? = null,
41:     modifier: Modifier = Modifier,
42:     textSize: Int = 14,
43:     indicatorSize: Int = 10
44: ) {
45:     val color = if (isActive) activeColor else inactiveColor
46:     
47:     Row(
48:         modifier = modifier,
49:         verticalAlignment = Alignment.CenterVertically
50:     ) {
51:         if (icon != null) {
52:             Icon(
53:                 imageVector = icon,
54:                 contentDescription = status,
55:                 tint = color,
56:                 modifier = Modifier.size(indicatorSize.dp)
57:             )
58:         } else {
59:             Box(
60:                 modifier = Modifier
61:                     .size(indicatorSize.dp)
62:                     .background(color, CircleShape)
63:             )
64:         }
65: 
66:         Spacer(modifier = Modifier.width(8.dp))
67: 
68:         Text(
69:             text = status,
70:             fontSize = textSize.sp,
71:             fontWeight = FontWeight.Medium,
72:             color = color
73:         )
74:     }
75: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/ToggleSettingItem.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.foundation.layout.Column
  4: import androidx.compose.foundation.layout.Row
  5: import androidx.compose.foundation.layout.Spacer
  6: import androidx.compose.foundation.layout.fillMaxWidth
  7: import androidx.compose.foundation.layout.height
  8: import androidx.compose.foundation.layout.padding
  9: import androidx.compose.foundation.layout.size
 10: import androidx.compose.material3.Icon
 11: import androidx.compose.material3.Switch
 12: import androidx.compose.material3.SwitchDefaults
 13: import androidx.compose.material3.Text
 14: import androidx.compose.runtime.Composable
 15: import androidx.compose.ui.Alignment
 16: import androidx.compose.ui.Modifier
 17: import androidx.compose.ui.graphics.vector.ImageVector
 18: import androidx.compose.ui.text.font.FontWeight
 19: import androidx.compose.ui.unit.dp
 20: import androidx.compose.ui.unit.sp
 21: import com.cocktailcraft.ui.theme.AppColors
 22: 
 23: /**
 24:  * A reusable toggle setting item component that displays a title, description, icon, and toggle switch.
 25:  *
 26:  * @param title The title text of the setting
 27:  * @param description The description text of the setting
 28:  * @param icon The icon to display next to the title
 29:  * @param isEnabled Whether the toggle is enabled
 30:  * @param onToggle The callback for when the toggle is clicked
 31:  * @param modifier The modifier for the component
 32:  * @param enabled Whether the toggle is interactive
 33:  * @param iconTint The tint color for the icon
 34:  * @param titleFontSize The font size of the title
 35:  * @param descriptionFontSize The font size of the description
 36:  * @param titleColor The color of the title text
 37:  * @param descriptionColor The color of the description text
 38:  * @param iconSize The size of the icon
 39:  * @param thumbContent Optional content for the switch thumb
 40:  */
 41: @Composable
 42: fun ToggleSettingItem(
 43:     title: String,
 44:     description: String,
 45:     icon: ImageVector,
 46:     isEnabled: Boolean,
 47:     onToggle: () -> Unit,
 48:     modifier: Modifier = Modifier,
 49:     enabled: Boolean = true,
 50:     iconTint: androidx.compose.ui.graphics.Color = if (isEnabled) AppColors.Primary else AppColors.Gray,
 51:     titleFontSize: Int = 18,
 52:     descriptionFontSize: Int = 14,
 53:     titleColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
 54:     descriptionColor: androidx.compose.ui.graphics.Color = AppColors.TextSecondary,
 55:     iconSize: Int = 24,
 56:     thumbContent: @Composable (() -> Unit)? = null
 57: ) {
 58:     Row(
 59:         modifier = modifier.fillMaxWidth(),
 60:         verticalAlignment = Alignment.Top
 61:     ) {
 62:         Column(
 63:             modifier = Modifier
 64:                 .weight(1f)
 65:                 .padding(end = 16.dp)
 66:         ) {
 67:             Row(
 68:                 verticalAlignment = Alignment.CenterVertically
 69:             ) {
 70:                 Icon(
 71:                     imageVector = icon,
 72:                     contentDescription = title,
 73:                     tint = iconTint,
 74:                     modifier = Modifier.size(iconSize.dp)
 75:                 )
 76: 
 77:                 Text(
 78:                     text = title,
 79:                     fontSize = titleFontSize.sp,
 80:                     fontWeight = FontWeight.Bold,
 81:                     color = titleColor,
 82:                     modifier = Modifier.padding(start = 16.dp)
 83:                 )
 84:             }
 85: 
 86:             Spacer(modifier = Modifier.height(8.dp))
 87: 
 88:             Text(
 89:                 text = description,
 90:                 fontSize = descriptionFontSize.sp,
 91:                 color = descriptionColor
 92:             )
 93:         }
 94: 
 95:         Switch(
 96:             checked = isEnabled,
 97:             onCheckedChange = { onToggle() },
 98:             enabled = enabled,
 99:             thumbContent = thumbContent
100:         )
101:     }
102: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/components/WriteReviewDialog.kt
````kotlin
  1: package com.cocktailcraft.ui.components
  2: 
  3: import androidx.compose.foundation.BorderStroke
  4: import androidx.compose.foundation.background
  5: import androidx.compose.foundation.layout.*
  6: import androidx.compose.foundation.shape.RoundedCornerShape
  7: import androidx.compose.material.icons.Icons
  8: import androidx.compose.material.icons.filled.Star
  9: import androidx.compose.material3.*
 10: import androidx.compose.runtime.*
 11: import androidx.compose.ui.Alignment
 12: import androidx.compose.ui.Modifier
 13: import androidx.compose.ui.text.font.FontWeight
 14: import androidx.compose.ui.unit.dp
 15: import androidx.compose.ui.unit.sp
 16: import com.cocktailcraft.ui.theme.AppColors
 17: import com.cocktailcraft.viewmodel.ReviewViewModel
 18: import androidx.compose.ui.graphics.Color
 19: 
 20: @Composable
 21: fun WriteReviewDialog(
 22:     showDialog: Boolean,
 23:     onDismiss: () -> Unit,
 24:     onSubmit: (userName: String, rating: Float, comment: String) -> Unit
 25: ) {
 26:     var userName by remember { mutableStateOf("") }
 27:     var userRating by remember { mutableStateOf(0f) }
 28:     var userComment by remember { mutableStateOf("") }
 29:     var hasError by remember { mutableStateOf(false) }
 30: 
 31:     if (showDialog) {
 32:         AlertDialog(
 33:             onDismissRequest = onDismiss,
 34:             title = {
 35:                 Text(
 36:                     text = "Write a Review",
 37:                     fontSize = 24.sp,
 38:                     fontWeight = FontWeight.Bold,
 39:                     color = AppColors.Primary
 40:                 )
 41:             },
 42:             text = {
 43:                 Column(
 44:                     modifier = Modifier
 45:                         .fillMaxWidth()
 46:                         .padding(vertical = 8.dp)
 47:                 ) {
 48:                     // Name input with validation
 49:                     Column {
 50:                         Text(
 51:                             text = "Your Name",
 52:                             fontSize = 16.sp,
 53:                             fontWeight = FontWeight.Medium,
 54:                             color = AppColors.Primary,
 55:                             modifier = Modifier.padding(bottom = 8.dp)
 56:                         )
 57:                         OutlinedTextField(
 58:                             value = userName,
 59:                             onValueChange = {
 60:                                 userName = it
 61:                                 hasError = false
 62:                             },
 63:                             placeholder = { Text("Enter your name", color = AppColors.Gray) },
 64:                             isError = hasError && userName.isBlank(),
 65:                             supportingText = {
 66:                                 if (hasError && userName.isBlank()) {
 67:                                     Text(
 68:                                         text = "Name is required",
 69:                                         color = MaterialTheme.colorScheme.error
 70:                                     )
 71:                                 }
 72:                             },
 73:                             modifier = Modifier
 74:                                 .fillMaxWidth()
 75:                                 .padding(bottom = 16.dp),
 76:                             colors = TextFieldDefaults.outlinedTextFieldColors(
 77:                                 focusedBorderColor = AppColors.Primary,
 78:                                 unfocusedBorderColor = AppColors.LightGray,
 79:                                 errorBorderColor = MaterialTheme.colorScheme.error,
 80:                                 containerColor = AppColors.Surface
 81:                             )
 82:                         )
 83:                     }
 84: 
 85:                     // Rating section with enhanced UI
 86:                     Column {
 87:                         Text(
 88:                             text = "Your Rating",
 89:                             fontSize = 16.sp,
 90:                             fontWeight = FontWeight.Medium,
 91:                             color = AppColors.Primary,
 92:                             modifier = Modifier.padding(bottom = 8.dp)
 93:                         )
 94: 
 95:                         Row(
 96:                             modifier = Modifier
 97:                                 .fillMaxWidth()
 98:                                 .background(AppColors.Surface, RoundedCornerShape(12.dp))
 99:                                 .padding(8.dp),
100:                             horizontalArrangement = Arrangement.Center
101:                         ) {
102:                             repeat(5) { index ->
103:                                 IconButton(
104:                                     onClick = { userRating = index + 1f },
105:                                     modifier = Modifier.size(48.dp)
106:                                 ) {
107:                                     Icon(
108:                                         imageVector = Icons.Filled.Star,
109:                                         contentDescription = "Rate ${index + 1}",
110:                                         tint = if (index < userRating) AppColors.Secondary else AppColors.LightGray,
111:                                         modifier = Modifier.size(36.dp)
112:                                     )
113:                                 }
114:                             }
115:                         }
116: 
117:                         if (hasError && userRating == 0f) {
118:                             Text(
119:                                 text = "Please select a rating",
120:                                 color = MaterialTheme.colorScheme.error,
121:                                 fontSize = 12.sp,
122:                                 modifier = Modifier.padding(top = 4.dp)
123:                             )
124:                         }
125:                     }
126: 
127:                     Spacer(modifier = Modifier.height(16.dp))
128: 
129:                     // Review text with character count
130:                     Column {
131:                         Text(
132:                             text = "Your Review",
133:                             fontSize = 16.sp,
134:                             fontWeight = FontWeight.Medium,
135:                             color = AppColors.Primary,
136:                             modifier = Modifier.padding(bottom = 8.dp)
137:                         )
138:                         OutlinedTextField(
139:                             value = userComment,
140:                             onValueChange = {
141:                                 if (it.length <= 500) {
142:                                     userComment = it
143:                                 }
144:                             },
145:                             placeholder = { Text("Share your experience (Optional)", color = AppColors.Gray) },
146:                             modifier = Modifier
147:                                 .fillMaxWidth()
148:                                 .height(120.dp),
149:                             colors = TextFieldDefaults.outlinedTextFieldColors(
150:                                 focusedBorderColor = AppColors.Primary,
151:                                 unfocusedBorderColor = AppColors.LightGray,
152:                                 containerColor = AppColors.Surface
153:                             )
154:                         )
155: 
156:                         Box(
157:                             modifier = Modifier.fillMaxWidth()
158:                         ) {
159:                             Text(
160:                                 text = "${userComment.length}/500",
161:                                 fontSize = 12.sp,
162:                                 color = AppColors.Gray,
163:                                 modifier = Modifier
164:                                     .align(Alignment.CenterEnd)
165:                                     .padding(top = 4.dp)
166:                             )
167:                         }
168:                     }
169:                 }
170:             },
171:             confirmButton = {
172:                 Button(
173:                     onClick = {
174:                         if (userName.isBlank() || userRating == 0f) {
175:                             hasError = true
176:                         } else {
177:                             onSubmit(userName, userRating, userComment)
178:                             // Reset fields
179:                             userName = ""
180:                             userRating = 0f
181:                             userComment = ""
182:                             hasError = false
183:                         }
184:                     },
185:                     colors = ButtonDefaults.buttonColors(
186:                         containerColor = AppColors.Primary
187:                     ),
188:                     shape = RoundedCornerShape(12.dp),
189:                     modifier = Modifier.padding(horizontal = 8.dp)
190:                 ) {
191:                     Text(
192:                         text = "Submit Review",
193:                         fontSize = 16.sp,
194:                         fontWeight = FontWeight.SemiBold
195:                     )
196:                 }
197:             },
198:             dismissButton = {
199:                 OutlinedButton(
200:                     onClick = onDismiss,
201:                     border = BorderStroke(1.dp, AppColors.Primary),
202:                     colors = ButtonDefaults.outlinedButtonColors(
203:                         contentColor = AppColors.Primary
204:                     ),
205:                     shape = RoundedCornerShape(12.dp)
206:                 ) {
207:                     Text("Cancel")
208:                 }
209:             },
210:             containerColor = Color.White,
211:             shape = RoundedCornerShape(24.dp),
212:             tonalElevation = 8.dp
213:         )
214:     }
215: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/main/MainScreen.kt
````kotlin
  1: package com.cocktailcraft.ui.main
  2: 
  3: import androidx.compose.animation.AnimatedContentTransitionScope
  4: import androidx.compose.animation.core.tween
  5: import androidx.compose.animation.fadeIn
  6: import androidx.compose.animation.fadeOut
  7: import androidx.compose.foundation.layout.Column
  8: import androidx.compose.foundation.layout.padding
  9: import androidx.compose.material.icons.Icons
 10: import androidx.compose.material.icons.filled.ArrowBack
 11: import androidx.compose.material3.ExperimentalMaterial3Api
 12: import androidx.compose.material3.Divider
 13: import androidx.compose.material3.Icon
 14: import androidx.compose.material3.IconButton
 15: import androidx.compose.material3.NavigationBar
 16: import androidx.compose.material3.NavigationBarItem
 17: import androidx.compose.material3.NavigationBarItemDefaults
 18: import androidx.compose.material3.Scaffold
 19: import androidx.compose.material3.Text
 20: import androidx.compose.material3.TopAppBar
 21: import androidx.compose.material3.TopAppBarDefaults
 22: import androidx.compose.runtime.Composable
 23: import androidx.compose.runtime.getValue
 24: import androidx.compose.runtime.remember
 25: import androidx.compose.ui.Modifier
 26: import androidx.compose.ui.graphics.Color
 27: import androidx.compose.ui.text.font.FontWeight
 28: import androidx.compose.ui.unit.dp
 29: import androidx.compose.ui.unit.sp
 30: import androidx.lifecycle.viewmodel.compose.viewModel
 31: import androidx.navigation.NavDestination.Companion.hierarchy
 32: import androidx.navigation.NavType
 33: import androidx.navigation.compose.NavHost
 34: import androidx.navigation.compose.composable
 35: import androidx.navigation.compose.currentBackStackEntryAsState
 36: import androidx.navigation.compose.rememberNavController
 37: import androidx.navigation.navArgument
 38: import com.cocktailcraft.navigation.NavigationManager
 39: import com.cocktailcraft.navigation.Screen
 40: import com.cocktailcraft.screens.CartScreen
 41: import com.cocktailcraft.screens.CocktailDetailScreen
 42: import com.cocktailcraft.screens.FavoritesScreen
 43: import com.cocktailcraft.screens.HomeScreen
 44: import com.cocktailcraft.screens.OfflineModeScreen
 45: import com.cocktailcraft.screens.OrderListScreen
 46: import com.cocktailcraft.screens.ProfileScreen
 47: import com.cocktailcraft.ui.theme.AppColors
 48: import com.cocktailcraft.viewmodel.CartViewModel
 49: import com.cocktailcraft.viewmodel.FavoritesViewModel
 50: import com.cocktailcraft.viewmodel.HomeViewModel
 51: import com.cocktailcraft.viewmodel.OfflineModeViewModel
 52: import com.cocktailcraft.viewmodel.OrderViewModel
 53: import com.cocktailcraft.viewmodel.ReviewViewModel
 54: import com.cocktailcraft.viewmodel.ThemeViewModel
 55: import com.cocktailcraft.ui.components.OfflineModeIndicator
 56: import androidx.compose.runtime.collectAsState
 57: 
 58: @OptIn(ExperimentalMaterial3Api::class)
 59: @Composable
 60: fun MainScreen() {
 61:     val navController = rememberNavController()
 62:     // Create the navigation manager
 63:     val navigationManager = remember { NavigationManager(navController) }
 64: 
 65:     val items = listOf(
 66:         Screen.Home,
 67:         Screen.Cart,
 68:         Screen.Favorites,
 69:         Screen.OrderList,
 70:         Screen.Profile
 71:     )
 72: 
 73:     // Create shared ViewModels for the entire app
 74:     val sharedOrderViewModel: OrderViewModel = viewModel()
 75:     val sharedCartViewModel: CartViewModel = viewModel()
 76:     val sharedReviewViewModel: ReviewViewModel = viewModel()
 77:     val sharedHomeViewModel: HomeViewModel = viewModel()
 78:     val sharedFavoritesViewModel: FavoritesViewModel = viewModel()
 79:     val sharedThemeViewModel: ThemeViewModel = viewModel()
 80:     val sharedOfflineModeViewModel: OfflineModeViewModel = viewModel()
 81: 
 82:     // Get the current route for conditional rendering
 83:     val navBackStackEntry by navController.currentBackStackEntryAsState()
 84:     val currentRoute = navBackStackEntry?.destination?.route
 85:     val isDetailScreen = currentRoute?.startsWith("cocktail_detail") == true
 86: 
 87:     // Get offline mode state
 88:     val isOfflineModeEnabled by sharedOfflineModeViewModel.isOfflineModeEnabled.collectAsState()
 89:     val isNetworkAvailable by sharedOfflineModeViewModel.isNetworkAvailable.collectAsState()
 90: 
 91:     Scaffold(
 92:         topBar = {
 93:             // Only show the main top bar if we're NOT on the detail screen
 94:             if (!isDetailScreen) {
 95:                 Column {
 96:                     // Show offline mode indicator if offline and not already on the offline mode screen
 97:                     if (currentRoute != Screen.OfflineMode.route) {
 98:                         OfflineModeIndicator(
 99:                             isOffline = !isNetworkAvailable || isOfflineModeEnabled,
100:                             isOfflineModeEnabled = isOfflineModeEnabled,
101:                             onClick = {
102:                                 // Navigate to offline mode settings
103:                                 navigationManager.navigateToOfflineMode()
104:                             }
105:                         )
106:                     }
107: 
108:                     TopAppBar(
109:                         title = {
110:                             // Normal title without search functionality
111:                             Text(
112:                                 text = when (currentRoute) {
113:                                     Screen.Home.route -> "My Bar"
114:                                     Screen.Cart.route -> "Cart"
115:                                     Screen.Favorites.route -> "Favorites"
116:                                     Screen.OrderList.route -> "Recipes"
117:                                     Screen.Profile.route -> "Profile"
118:                                     Screen.OfflineMode.route -> "Offline Mode"
119:                                     else -> "Cocktail Bar"
120:                                 },
121:                                 color = Color.White,
122:                                 fontSize = 24.sp,
123:                                 fontWeight = FontWeight.Bold
124:                             )
125:                         },
126:                         navigationIcon = {
127:                             // Show back button only for the OfflineMode screen
128:                             if (currentRoute == Screen.OfflineMode.route) {
129:                                 IconButton(onClick = { navigationManager.navigateBack() }) {
130:                                     Icon(
131:                                         imageVector = Icons.Filled.ArrowBack,
132:                                         contentDescription = "Back",
133:                                         tint = Color.White
134:                                     )
135:                                 }
136:                             }
137:                         },
138:                         actions = {
139:                             // Removed search button/functionality
140:                         },
141:                         colors = TopAppBarDefaults.mediumTopAppBarColors(
142:                             containerColor = AppColors.Primary,
143:                             titleContentColor = Color.White,
144:                             navigationIconContentColor = Color.White,
145:                             actionIconContentColor = Color.White
146:                         )
147:                     )
148: 
149:                     // Add a divider to create separation between top bar and content
150:                     Divider(
151:                         color = Color.White.copy(alpha = 0.2f),
152:                         thickness = 1.dp
153:                     )
154:                 }
155:             }
156:         },
157:         bottomBar = {
158:             // Only show the bottom navigation bar if we're NOT on the detail screen or offline mode screen
159:             val currentScreenRoute = navController.currentDestination?.route
160:             if (!isDetailScreen && currentScreenRoute != Screen.OfflineMode.route) {
161:                 NavigationBar(
162:                     containerColor = AppColors.Surface,
163:                     contentColor = AppColors.Primary,
164:                     tonalElevation = 8.dp
165:                 ) {
166:                     val currentDestination = navBackStackEntry?.destination
167: 
168:                     items.forEach { screen ->
169:                         NavigationBarItem(
170:                             icon = {
171:                                 Icon(screen.icon, contentDescription = screen.title)
172:                             },
173:                             label = {
174:                                 Text(screen.title, fontSize = 12.sp)
175:                             },
176:                             selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
177:                             onClick = {
178:                                 navigationManager.navigateToBottomNavDestination(screen)
179:                             },
180:                             colors = NavigationBarItemDefaults.colors(
181:                                 selectedIconColor = AppColors.Primary,
182:                                 selectedTextColor = AppColors.Primary,
183:                                 indicatorColor = AppColors.Surface,
184:                                 unselectedIconColor = AppColors.Gray,
185:                                 unselectedTextColor = AppColors.Gray
186:                             )
187:                         )
188:                     }
189:                 }
190:             }
191:         },
192:         containerColor = AppColors.Background
193:     ) { innerPadding ->
194:         NavHost(
195:             navController = navController,
196:             startDestination = Screen.Home.route,
197:             modifier = Modifier.padding(innerPadding),
198:             enterTransition = {
199:                 slideIntoContainer(
200:                     towards = AnimatedContentTransitionScope.SlideDirection.Left,
201:                     animationSpec = tween(300)
202:                 ) + fadeIn(animationSpec = tween(300))
203:             },
204:             exitTransition = {
205:                 slideOutOfContainer(
206:                     towards = AnimatedContentTransitionScope.SlideDirection.Left,
207:                     animationSpec = tween(300)
208:                 ) + fadeOut(animationSpec = tween(300))
209:             },
210:             popEnterTransition = {
211:                 slideIntoContainer(
212:                     towards = AnimatedContentTransitionScope.SlideDirection.Right,
213:                     animationSpec = tween(300)
214:                 ) + fadeIn(animationSpec = tween(300))
215:             },
216:             popExitTransition = {
217:                 slideOutOfContainer(
218:                     towards = AnimatedContentTransitionScope.SlideDirection.Right,
219:                     animationSpec = tween(300)
220:                 ) + fadeOut(animationSpec = tween(300))
221:             }
222:         ) {
223:             composable(Screen.Home.route) {
224:                 HomeScreen(
225:                     viewModel = sharedHomeViewModel,
226:                     favoritesViewModel = sharedFavoritesViewModel,
227:                     onAddToCart = { cocktail ->
228:                         // Add to cart and then navigate to cart
229:                         sharedCartViewModel.addToCart(cocktail)
230:                         navigationManager.navigateToCart()
231:                     },
232:                     onCocktailClick = { cocktail ->
233:                         // Navigate to cocktail detail screen
234:                         navigationManager.navigateToCocktailDetail(cocktail)
235:                     }
236:                 )
237:             }
238:             composable(Screen.Cart.route) {
239:                 CartScreen(
240:                     viewModel = sharedCartViewModel,
241:                     onStartShopping = {
242:                         navigationManager.navigateToHome()
243:                     },
244:                     navigationManager = navigationManager,
245:                     orderViewModel = sharedOrderViewModel,
246:                     favoritesViewModel = sharedFavoritesViewModel
247:                 )
248:             }
249:             composable(Screen.Profile.route) {
250:                 ProfileScreen(
251:                     navigationManager = navigationManager,
252:                     themeViewModel = sharedThemeViewModel
253:                 )
254:             }
255:             composable(Screen.Favorites.route) {
256:                 FavoritesScreen(
257:                     cartViewModel = sharedCartViewModel,
258:                     favoritesViewModel = sharedFavoritesViewModel,
259:                     onBrowseProducts = {
260:                         navigationManager.navigateToHome()
261:                     },
262:                     onAddToCart = { cocktail ->
263:                         // Add to cart and then navigate to cart
264:                         sharedCartViewModel.addToCart(cocktail)
265:                         navigationManager.navigateToCart()
266:                     }
267:                 )
268:             }
269:             composable(Screen.OrderList.route) {
270:                 OrderListScreen(
271:                     orderViewModel = sharedOrderViewModel,
272:                     navigationManager = navigationManager
273:                 )
274:             }
275:             composable(Screen.OfflineMode.route) {
276:                 OfflineModeScreen(
277:                     viewModel = sharedOfflineModeViewModel,
278:                     onBackClick = { navigationManager.navigateBack() },
279:                     onCocktailClick = { cocktail ->
280:                         navigationManager.navigateToCocktailDetail(cocktail)
281:                     }
282:                 )
283:             }
284: 
285:             composable(
286:                 route = Screen.CocktailDetail.route,
287:                 arguments = listOf(navArgument("cocktailId") { type = NavType.StringType })
288:             ) { backStackEntry ->
289:                 val cocktailId = backStackEntry.arguments?.getString("cocktailId") ?: ""
290:                 CocktailDetailScreen(
291:                     cocktailId = cocktailId,
292:                     homeViewModel = sharedHomeViewModel,
293:                     cartViewModel = sharedCartViewModel,
294:                     reviewViewModel = sharedReviewViewModel,
295:                     favoritesViewModel = sharedFavoritesViewModel,
296:                     navigationManager = navigationManager,
297:                     onBackClick = { navigationManager.navigateBack() },
298:                     onAddToCart = { cocktailToAdd ->
299:                         sharedCartViewModel.addToCart(cocktailToAdd)
300:                         navigationManager.navigateToCart()
301:                     }
302:                 )
303:             }
304:         }
305:     }
306: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/theme/AnimatedTheme.kt
````kotlin
  1: package com.cocktailcraft.ui.theme
  2: 
  3: import androidx.compose.animation.animateColorAsState
  4: import androidx.compose.animation.core.AnimationSpec
  5: import androidx.compose.animation.core.tween
  6: import androidx.compose.foundation.isSystemInDarkTheme
  7: import androidx.compose.material3.MaterialTheme
  8: import androidx.compose.material3.darkColorScheme
  9: import androidx.compose.material3.lightColorScheme
 10: import androidx.compose.runtime.Composable
 11: import androidx.compose.runtime.CompositionLocalProvider
 12: import androidx.compose.runtime.SideEffect
 13: import androidx.compose.runtime.getValue
 14: import androidx.compose.runtime.staticCompositionLocalOf
 15: import androidx.compose.ui.graphics.Color
 16: import androidx.compose.ui.platform.LocalView
 17: import com.google.accompanist.systemuicontroller.rememberSystemUiController
 18: import android.app.Activity
 19: import androidx.compose.ui.graphics.toArgb
 20: import androidx.core.view.WindowCompat
 21: 
 22: // Animation specifications
 23: private val ColorAnimationSpec: AnimationSpec<Color> = tween(durationMillis = 600)
 24: 
 25: // Animated color scheme for light theme
 26: private val AnimatedLightColorScheme = lightColorScheme(
 27:     primary = AppColors.PrimaryLight,
 28:     secondary = AppColors.SecondaryLight,
 29:     background = AppColors.BackgroundLight,
 30:     surface = AppColors.SurfaceLight,
 31:     error = AppColors.Error,
 32:     onPrimary = Color.White,
 33:     onSecondary = Color.Black,
 34:     onBackground = AppColors.TextPrimaryLight,
 35:     onSurface = AppColors.TextPrimaryLight,
 36:     onError = Color.White
 37: )
 38: 
 39: // Animated color scheme for dark theme
 40: private val AnimatedDarkColorScheme = darkColorScheme(
 41:     primary = AppColors.PrimaryDark,
 42:     secondary = AppColors.SecondaryDark,
 43:     background = AppColors.BackgroundDark,
 44:     surface = AppColors.SurfaceDark,
 45:     error = AppColors.Error,
 46:     onPrimary = Color.Black,
 47:     onSecondary = Color.Black,
 48:     onBackground = AppColors.TextPrimaryDark,
 49:     onSurface = AppColors.TextPrimaryDark,
 50:     onError = Color.White
 51: )
 52: 
 53: /**
 54:  * Animated version of CocktailBarTheme that smoothly transitions between light and dark themes
 55:  */
 56: @Composable
 57: fun AnimatedCocktailBarTheme(
 58:     darkTheme: Boolean = isSystemInDarkTheme(),
 59:     content: @Composable () -> Unit
 60: ) {
 61:     // Update the AppColors isDarkTheme value to match current theme
 62:     AppColors.isDarkTheme = darkTheme
 63:     
 64:     // Animate color transitions
 65:     val primary by animateColorAsState(
 66:         targetValue = if (darkTheme) AppColors.PrimaryDark else AppColors.PrimaryLight,
 67:         animationSpec = ColorAnimationSpec,
 68:         label = "primary"
 69:     )
 70:     
 71:     val secondary by animateColorAsState(
 72:         targetValue = if (darkTheme) AppColors.SecondaryDark else AppColors.SecondaryLight,
 73:         animationSpec = ColorAnimationSpec,
 74:         label = "secondary"
 75:     )
 76:     
 77:     val background by animateColorAsState(
 78:         targetValue = if (darkTheme) AppColors.BackgroundDark else AppColors.BackgroundLight,
 79:         animationSpec = ColorAnimationSpec,
 80:         label = "background"
 81:     )
 82:     
 83:     val surface by animateColorAsState(
 84:         targetValue = if (darkTheme) AppColors.SurfaceDark else AppColors.SurfaceLight,
 85:         animationSpec = ColorAnimationSpec,
 86:         label = "surface"
 87:     )
 88:     
 89:     val onBackground by animateColorAsState(
 90:         targetValue = if (darkTheme) AppColors.TextPrimaryDark else AppColors.TextPrimaryLight,
 91:         animationSpec = ColorAnimationSpec,
 92:         label = "onBackground"
 93:     )
 94:     
 95:     val onSurface by animateColorAsState(
 96:         targetValue = if (darkTheme) AppColors.TextPrimaryDark else AppColors.TextPrimaryLight,
 97:         animationSpec = ColorAnimationSpec,
 98:         label = "onSurface"
 99:     )
100:     
101:     // Create a custom color scheme with animated colors
102:     val colorScheme = if (darkTheme) {
103:         AnimatedDarkColorScheme.copy(
104:             primary = primary,
105:             secondary = secondary,
106:             background = background,
107:             surface = surface,
108:             onBackground = onBackground,
109:             onSurface = onSurface
110:         )
111:     } else {
112:         AnimatedLightColorScheme.copy(
113:             primary = primary,
114:             secondary = secondary,
115:             background = background,
116:             surface = surface,
117:             onBackground = onBackground,
118:             onSurface = onSurface
119:         )
120:     }
121:     
122:     // Handle system UI colors with animation
123:     val statusBarColor by animateColorAsState(
124:         targetValue = if (darkTheme) AppColors.BackgroundDark else AppColors.PrimaryLight,
125:         animationSpec = ColorAnimationSpec,
126:         label = "statusBarColor"
127:     )
128:     
129:     val view = LocalView.current
130:     if (!view.isInEditMode) {
131:         SideEffect {
132:             val window = (view.context as Activity).window
133:             
134:             // Set status bar color with animation
135:             window.statusBarColor = statusBarColor.toArgb()
136:             
137:             // Set status bar icons to be light or dark based on theme
138:             WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
139:         }
140:     }
141:     
142:     MaterialTheme(
143:         colorScheme = colorScheme,
144:         content = content
145:     )
146: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/theme/Theme.kt
````kotlin
  1: package com.cocktailcraft.ui.theme
  2: 
  3: import android.app.Activity
  4: import androidx.compose.foundation.isSystemInDarkTheme
  5: import androidx.compose.material3.MaterialTheme
  6: import androidx.compose.material3.darkColorScheme
  7: import androidx.compose.material3.lightColorScheme
  8: import androidx.compose.runtime.Composable
  9: import androidx.compose.runtime.SideEffect
 10: import androidx.compose.ui.graphics.Color
 11: import androidx.compose.ui.graphics.toArgb
 12: import androidx.compose.ui.platform.LocalView
 13: import androidx.core.view.WindowCompat
 14: 
 15: // Cocktail Bar color palette
 16: object AppColors {
 17:     // Light Theme Colors
 18:     val PrimaryLight = Color(0xFFEB6A43) // Coral/Orange for main elements
 19:     val SecondaryLight = Color(0xFFFFC84D) // Yellow/Gold for wave and accents
 20:     val BackgroundLight = Color(0xFFFAFAFA) // Light background
 21:     val SurfaceLight = Color.White // White surface
 22:     val TextPrimaryLight = Color(0xFF000000) // Black for primary text
 23:     val TextSecondaryLight = Color(0xFF8E8E93) // Gray for secondary text
 24: 
 25:     // Dark Theme Colors
 26:     val PrimaryDark = Color(0xFFFF8A65) // Lighter coral for dark theme
 27:     val SecondaryDark = Color(0xFFFFD180) // Lighter gold for dark theme
 28:     val BackgroundDark = Color(0xFF121212) // Dark background
 29:     val SurfaceDark = Color(0xFF1E1E1E) // Dark surface
 30:     val TextPrimaryDark = Color.White // White for primary text in dark mode
 31:     val TextSecondaryDark = Color(0xFFB0B0B0) // Light gray for secondary text in dark mode
 32: 
 33:     // Common Colors (same in both themes)
 34:     val Success = Color(0xFF34C759) // Green for success states
 35:     val Error = Color(0xFFFF3B30) // Red for errors
 36:     val Warning = Color(0xFFFF9500) // Orange for warnings and stars
 37:     val Gray = Color(0xFF8E8E93) // Gray for secondary text
 38:     val DarkGray = Color(0xFF636366) // Dark gray for stronger text
 39:     val LightGray = Color(0xFFE5E5EA) // Light gray for backgrounds
 40:     val ChipBackground = Color(0xFF9C5C38) // Brown for category chips like "Vodka"
 41: 
 42:     // Dynamic colors based on theme - these will be set by the theme
 43:     var isDarkTheme = false
 44: 
 45:     val Primary get() = if (isDarkTheme) PrimaryDark else PrimaryLight
 46:     val Secondary get() = if (isDarkTheme) SecondaryDark else SecondaryLight
 47:     val Background get() = if (isDarkTheme) BackgroundDark else BackgroundLight
 48:     val Surface get() = if (isDarkTheme) SurfaceDark else SurfaceLight
 49:     val TextPrimary get() = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
 50:     val TextSecondary get() = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
 51: }
 52: 
 53: private val LightColorScheme = lightColorScheme(
 54:     primary = AppColors.PrimaryLight,
 55:     secondary = AppColors.SecondaryLight,
 56:     background = AppColors.BackgroundLight,
 57:     surface = AppColors.SurfaceLight,
 58:     error = AppColors.Error,
 59:     onPrimary = Color.White,
 60:     onSecondary = Color.Black,
 61:     onBackground = AppColors.TextPrimaryLight,
 62:     onSurface = AppColors.TextPrimaryLight,
 63:     onError = Color.White
 64: )
 65: 
 66: private val DarkColorScheme = darkColorScheme(
 67:     primary = AppColors.PrimaryDark,
 68:     secondary = AppColors.SecondaryDark,
 69:     background = AppColors.BackgroundDark,
 70:     surface = AppColors.SurfaceDark,
 71:     error = AppColors.Error,
 72:     onPrimary = Color.Black,
 73:     onSecondary = Color.Black,
 74:     onBackground = AppColors.TextPrimaryDark,
 75:     onSurface = AppColors.TextPrimaryDark,
 76:     onError = Color.White
 77: )
 78: 
 79: @Composable
 80: fun CocktailBarTheme(
 81:     darkTheme: Boolean = isSystemInDarkTheme(),
 82:     content: @Composable () -> Unit
 83: ) {
 84:     // Update the AppColors isDarkTheme value to match current theme
 85:     AppColors.isDarkTheme = darkTheme
 86: 
 87:     val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
 88: 
 89:     val view = LocalView.current
 90:     if (!view.isInEditMode) {
 91:         SideEffect {
 92:             val window = (view.context as Activity).window
 93: 
 94:             // Set status bar color based on theme
 95:             window.statusBarColor = if (darkTheme) {
 96:                 AppColors.BackgroundDark.toArgb() // Dark background for status bar in dark mode
 97:             } else {
 98:                 AppColors.PrimaryLight.toArgb() // Primary color for status bar in light mode
 99:             }
100: 
101:             // Set status bar icons to be light or dark based on theme
102:             WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
103:         }
104:     }
105: 
106:     MaterialTheme(
107:         colorScheme = colorScheme,
108:         content = content
109:     )
110: }
````

## File: androidApp/src/main/java/com/cocktailcraft/ui/theme/ThemeAssets.kt
````kotlin
 1: package com.cocktailcraft.ui.theme
 2: 
 3: import androidx.compose.runtime.Composable
 4: import androidx.compose.ui.graphics.painter.Painter
 5: import androidx.compose.ui.res.painterResource
 6: import com.cocktailcraft.android.R
 7: 
 8: /**
 9:  * Provides access to theme-specific assets throughout the app.
10:  * This allows different images, icons, and other resources to be used
11:  * based on whether the app is in light or dark mode.
12:  */
13: object ThemeAssets {
14: 
15:     /**
16:      * Returns a theme-specific logo based on the current theme
17:      */
18:     @Composable
19:     fun logo(): Painter {
20:         return if (AppColors.isDarkTheme) {
21:             painterResource(id = R.drawable.logo_dark)
22:         } else {
23:             painterResource(id = R.drawable.logo_light)
24:         }
25:     }
26: 
27:     /**
28:      * Returns a theme-specific background image based on the current theme
29:      */
30:     @Composable
31:     fun backgroundPattern(): Painter {
32:         return if (AppColors.isDarkTheme) {
33:             painterResource(id = R.drawable.bg_pattern_dark)
34:         } else {
35:             painterResource(id = R.drawable.bg_pattern_light)
36:         }
37:     }
38: 
39:     /**
40:      * Returns a theme-specific placeholder image for cocktails
41:      */
42:     @Composable
43:     fun cocktailPlaceholder(): Painter {
44:         return if (AppColors.isDarkTheme) {
45:             painterResource(id = R.drawable.cocktail_placeholder_dark)
46:         } else {
47:             painterResource(id = R.drawable.cocktail_placeholder_light)
48:         }
49:     }
50: 
51:     /**
52:      * Returns a theme-specific empty state illustration
53:      */
54:     @Composable
55:     fun emptyStateIllustration(): Painter {
56:         return if (AppColors.isDarkTheme) {
57:             painterResource(id = R.drawable.empty_state_dark)
58:         } else {
59:             painterResource(id = R.drawable.empty_state_light)
60:         }
61:     }
62: }
````

## File: androidApp/src/main/java/com/cocktailcraft/util/ErrorUtils.kt
````kotlin
  1: package com.cocktailcraft.util
  2: 
  3: import androidx.compose.material.icons.Icons
  4: import androidx.compose.material.icons.filled.Error
  5: import androidx.compose.material.icons.filled.Warning
  6: import androidx.compose.material.icons.filled.WifiOff
  7: import androidx.compose.runtime.Composable
  8: import androidx.compose.ui.graphics.Color
  9: import androidx.compose.ui.graphics.vector.ImageVector
 10: import com.cocktailcraft.domain.util.ErrorCode
 11: import com.cocktailcraft.ui.theme.AppColors
 12: import java.net.ConnectException
 13: import java.net.SocketTimeoutException
 14: import java.net.UnknownHostException
 15: import java.util.concurrent.TimeoutException
 16: 
 17: /**
 18:  * Utility class for handling errors consistently throughout the app.
 19:  */
 20: object ErrorUtils {
 21: 
 22:     /**
 23:      * Error categories for grouping similar errors
 24:      */
 25:     enum class ErrorCategory {
 26:         NETWORK,
 27:         SERVER,
 28:         CLIENT,
 29:         AUTHENTICATION,
 30:         DATA,
 31:         UNKNOWN
 32:     }
 33: 
 34:     /**
 35:      * Data class representing a user-friendly error with recovery options
 36:      */
 37:     data class UserFriendlyError(
 38:         val title: String,
 39:         val message: String,
 40:         val category: ErrorCategory,
 41:         val recoveryAction: RecoveryAction? = null,
 42:         val originalException: Throwable? = null,
 43:         val errorCode: ErrorCode = ErrorCode.UNKNOWN
 44:     )
 45: 
 46:     /**
 47:      * Recovery action that can be taken to resolve an error
 48:      */
 49:     data class RecoveryAction(
 50:         val actionLabel: String,
 51:         val action: () -> Unit
 52:     )
 53: 
 54:     /**
 55:      * Convert an exception to a user-friendly error message
 56:      */
 57:     fun getErrorFromException(
 58:         exception: Throwable,
 59:         defaultMessage: String = "Something went wrong. Please try again.",
 60:         recoveryAction: RecoveryAction? = null
 61:     ): UserFriendlyError {
 62:         return when (exception) {
 63:             // Network errors
 64:             is UnknownHostException,
 65:             is ConnectException -> UserFriendlyError(
 66:                 title = "Network Error",
 67:                 message = "Unable to connect to the server. Please check your internet connection and try again.",
 68:                 category = ErrorCategory.NETWORK,
 69:                 recoveryAction = recoveryAction,
 70:                 originalException = exception,
 71:                 errorCode = ErrorCode.NETWORK
 72:             )
 73: 
 74:             // Timeout errors
 75:             is SocketTimeoutException,
 76:             is TimeoutException -> UserFriendlyError(
 77:                 title = "Connection Timeout",
 78:                 message = "The connection timed out. This might be due to slow internet or server issues. Please try again.",
 79:                 category = ErrorCategory.NETWORK,
 80:                 recoveryAction = recoveryAction,
 81:                 originalException = exception,
 82:                 errorCode = ErrorCode.TIMEOUT
 83:             )
 84: 
 85:             // Authentication errors
 86:             is SecurityException -> UserFriendlyError(
 87:                 title = "Authentication Error",
 88:                 message = "You don't have permission to access this resource. Please log in again.",
 89:                 category = ErrorCategory.AUTHENTICATION,
 90:                 recoveryAction = recoveryAction,
 91:                 originalException = exception,
 92:                 errorCode = ErrorCode.UNAUTHORIZED
 93:             )
 94: 
 95:             // Data errors
 96:             is IllegalArgumentException,
 97:             is IllegalStateException -> UserFriendlyError(
 98:                 title = "Data Error",
 99:                 message = exception.message ?: "Invalid data received. Please try again.",
100:                 category = ErrorCategory.DATA,
101:                 recoveryAction = recoveryAction,
102:                 originalException = exception,
103:                 errorCode = ErrorCode.INVALID_DATA
104:             )
105: 
106:             // Default for unknown errors
107:             else -> {
108:                 val message = when {
109:                     exception.message?.contains("timeout", ignoreCase = true) == true ->
110:                         "The request timed out. Please try again."
111:                     exception.message?.contains("connect", ignoreCase = true) == true ->
112:                         "Unable to connect to the server. Please check your internet connection."
113:                     exception.message?.contains("server", ignoreCase = true) == true ->
114:                         "The server encountered an error. Please try again later."
115:                     exception.message?.contains("404", ignoreCase = true) == true ->
116:                         "The requested resource was not found."
117:                     exception.message?.contains("401", ignoreCase = true) == true ||
118:                     exception.message?.contains("403", ignoreCase = true) == true ->
119:                         "You don't have permission to access this resource."
120:                     exception.message?.contains("500", ignoreCase = true) == true ->
121:                         "The server encountered an internal error. Please try again later."
122:                     else -> defaultMessage
123:                 }
124: 
125:                 val category = when {
126:                     message.contains("internet", ignoreCase = true) ||
127:                     message.contains("connect", ignoreCase = true) -> ErrorCategory.NETWORK
128:                     message.contains("server", ignoreCase = true) -> ErrorCategory.SERVER
129:                     message.contains("permission", ignoreCase = true) -> ErrorCategory.AUTHENTICATION
130:                     else -> ErrorCategory.UNKNOWN
131:                 }
132: 
133:                 UserFriendlyError(
134:                     title = getCategoryTitle(category),
135:                     message = message,
136:                     category = category,
137:                     recoveryAction = recoveryAction,
138:                     originalException = exception
139:                 )
140:             }
141:         }
142:     }
143: 
144:     /**
145:      * Get a user-friendly title for an error category
146:      */
147:     private fun getCategoryTitle(category: ErrorCategory): String {
148:         return when (category) {
149:             ErrorCategory.NETWORK -> "Network Error"
150:             ErrorCategory.SERVER -> "Server Error"
151:             ErrorCategory.CLIENT -> "Application Error"
152:             ErrorCategory.AUTHENTICATION -> "Authentication Error"
153:             ErrorCategory.DATA -> "Data Error"
154:             ErrorCategory.UNKNOWN -> "Error"
155:         }
156:     }
157: 
158:     /**
159:      * Get a recovery suggestion based on error category
160:      */
161:     fun getRecoverySuggestion(category: ErrorCategory): String {
162:         return when (category) {
163:             ErrorCategory.NETWORK -> "Check your internet connection and try again."
164:             ErrorCategory.SERVER -> "Our servers are experiencing issues. Please try again later."
165:             ErrorCategory.CLIENT -> "Try restarting the app."
166:             ErrorCategory.AUTHENTICATION -> "Please log in again."
167:             ErrorCategory.DATA -> "Try refreshing the data."
168:             ErrorCategory.UNKNOWN -> "Please try again or contact support if the issue persists."
169:         }
170:     }
171: 
172:     /**
173:      * Get a default recovery action based on error category
174:      */
175:     fun getDefaultRecoveryAction(category: ErrorCategory, action: () -> Unit): RecoveryAction {
176:         val label = when (category) {
177:             ErrorCategory.NETWORK -> "Retry"
178:             ErrorCategory.SERVER -> "Try Again"
179:             ErrorCategory.CLIENT -> "Refresh"
180:             ErrorCategory.AUTHENTICATION -> "Log In"
181:             ErrorCategory.DATA -> "Refresh"
182:             ErrorCategory.UNKNOWN -> "Try Again"
183:         }
184: 
185:         return RecoveryAction(label, action)
186:     }
187: 
188:     /**
189:      * Create a user-friendly error with the given parameters
190:      */
191:     fun createUserFriendlyError(
192:         title: String,
193:         message: String,
194:         category: ErrorCategory = ErrorCategory.UNKNOWN,
195:         recoveryAction: RecoveryAction? = null,
196:         originalException: Throwable? = null,
197:         errorCode: ErrorCode = ErrorCode.UNKNOWN
198:     ): UserFriendlyError {
199:         return UserFriendlyError(
200:             title = title,
201:             message = message,
202:             category = category,
203:             recoveryAction = recoveryAction,
204:             originalException = originalException,
205:             errorCode = errorCode
206:         )
207:     }
208: 
209:     /**
210:      * Create a standard network error
211:      */
212:     fun createNetworkError(retryAction: (() -> Unit)? = null): UserFriendlyError {
213:         return UserFriendlyError(
214:             title = "Network Error",
215:             message = "Unable to connect to the server. Please check your internet connection and try again.",
216:             category = ErrorCategory.NETWORK,
217:             recoveryAction = retryAction?.let { RecoveryAction("Retry", it) },
218:             errorCode = ErrorCode.NETWORK
219:         )
220:     }
221: 
222:     /**
223:      * Create a standard server error
224:      */
225:     fun createServerError(retryAction: (() -> Unit)? = null): UserFriendlyError {
226:         return UserFriendlyError(
227:             title = "Server Error",
228:             message = "The server encountered an error. Please try again later.",
229:             category = ErrorCategory.SERVER,
230:             recoveryAction = retryAction?.let { RecoveryAction("Retry", it) },
231:             errorCode = ErrorCode.SERVER_ERROR
232:         )
233:     }
234: 
235:     /**
236:      * Create an error from an exception
237:      */
238:     fun createErrorFromException(
239:         exception: Throwable,
240:         defaultTitle: String = "Error",
241:         defaultMessage: String = "An unexpected error occurred.",
242:         retryAction: (() -> Unit)? = null
243:     ): UserFriendlyError {
244:         // Check for network-related exceptions
245:         if (exception is java.net.UnknownHostException ||
246:             exception is java.net.ConnectException ||
247:             exception.message?.contains("connect", ignoreCase = true) == true ||
248:             exception.message?.contains("network", ignoreCase = true) == true) {
249:             return createNetworkError(retryAction)
250:         }
251: 
252:         // Check for timeout exceptions
253:         if (exception is java.net.SocketTimeoutException ||
254:             exception is java.util.concurrent.TimeoutException ||
255:             exception.message?.contains("timeout", ignoreCase = true) == true) {
256:             return UserFriendlyError(
257:                 title = "Network Error",
258:                 message = "The request timed out. Please try again.",
259:                 category = ErrorCategory.NETWORK,
260:                 recoveryAction = retryAction?.let { RecoveryAction("Retry", it) },
261:                 originalException = exception,
262:                 errorCode = ErrorCode.TIMEOUT
263:             )
264:         }
265: 
266:         // Default error with the exception message
267:         return UserFriendlyError(
268:             title = defaultTitle,
269:             message = "$defaultMessage: ${exception.message}",
270:             category = ErrorCategory.UNKNOWN,
271:             recoveryAction = retryAction?.let { RecoveryAction("Retry", it) },
272:             originalException = exception,
273:             errorCode = ErrorCode.UNKNOWN
274:         )
275:     }
276: 
277:     /**
278:      * Create an error from an error code
279:      */
280:     fun createErrorFromErrorCode(
281:         errorCode: ErrorCode,
282:         defaultTitle: String = "Error",
283:         defaultMessage: String = "An error occurred",
284:         retryAction: (() -> Unit)? = null
285:     ): UserFriendlyError {
286:         return when (errorCode) {
287:             ErrorCode.NETWORK -> createNetworkError(retryAction)
288:             ErrorCode.TIMEOUT -> UserFriendlyError(
289:                 title = "Network Error",
290:                 message = "The request timed out. Please try again.",
291:                 category = ErrorCategory.NETWORK,
292:                 recoveryAction = retryAction?.let { RecoveryAction("Retry", it) },
293:                 errorCode = errorCode
294:             )
295:             ErrorCode.UNAUTHORIZED -> UserFriendlyError(
296:                 title = "Authentication Error",
297:                 message = "You are not authorized to perform this action. Please sign in and try again.",
298:                 category = ErrorCategory.AUTHENTICATION,
299:                 recoveryAction = retryAction?.let { RecoveryAction("Sign In", it) },
300:                 errorCode = errorCode
301:             )
302:             ErrorCode.INVALID_DATA -> UserFriendlyError(
303:                 title = "Data Error",
304:                 message = "The data provided is invalid. Please check your input and try again.",
305:                 category = ErrorCategory.DATA,
306:                 recoveryAction = retryAction?.let { RecoveryAction("Try Again", it) },
307:                 errorCode = errorCode
308:             )
309:             else -> UserFriendlyError(
310:                 title = defaultTitle,
311:                 message = defaultMessage,
312:                 category = ErrorCategory.UNKNOWN,
313:                 recoveryAction = retryAction?.let { RecoveryAction("Try Again", it) },
314:                 errorCode = errorCode
315:             )
316:         }
317:     }
318: 
319:     /**
320:      * Get an appropriate icon for the error category
321:      */
322:     @Composable
323:     fun getErrorIcon(category: ErrorCategory): ImageVector {
324:         return when (category) {
325:             ErrorCategory.NETWORK -> Icons.Default.WifiOff
326:             ErrorCategory.SERVER,
327:             ErrorCategory.CLIENT -> Icons.Default.Warning
328:             else -> Icons.Default.Error
329:         }
330:     }
331: 
332:     /**
333:      * Get an appropriate color for the error category
334:      */
335:     @Composable
336:     fun getErrorColor(category: ErrorCategory): Color {
337:         return when (category) {
338:             ErrorCategory.NETWORK -> AppColors.Primary
339:             ErrorCategory.SERVER -> Color(0xFFF57C00) // Orange
340:             ErrorCategory.AUTHENTICATION -> Color(0xFFD32F2F) // Red
341:             else -> AppColors.Error
342:         }
343:     }
344: }
````

## File: androidApp/src/main/java/com/cocktailcraft/util/FilterOptionsLoader.kt
````kotlin
 1: package com.cocktailcraft.util
 2: 
 3: import androidx.compose.runtime.Composable
 4: import androidx.compose.runtime.LaunchedEffect
 5: import androidx.compose.runtime.getValue
 6: import androidx.compose.runtime.mutableStateOf
 7: import androidx.compose.runtime.remember
 8: import androidx.compose.runtime.setValue
 9: import com.cocktailcraft.domain.repository.CocktailRepository
10: 
11: /**
12:  * A utility class for loading filter options from the repository.
13:  */
14: object FilterOptionsLoader {
15:     
16:     /**
17:      * A data class representing filter options.
18:      */
19:     data class FilterOptions(
20:         val categories: List<String>,
21:         val ingredients: List<String>,
22:         val glasses: List<String>
23:     )
24:     
25:     /**
26:      * Loads filter options from the repository and returns them as a state.
27:      *
28:      * @param repository The cocktail repository to load options from
29:      * @param defaultCategories The default categories to use if loading fails
30:      * @param defaultIngredients The default ingredients to use if loading fails
31:      * @param defaultGlasses The default glasses to use if loading fails
32:      * @return A FilterOptions object containing the loaded options
33:      */
34:     @Composable
35:     fun rememberFilterOptions(
36:         repository: CocktailRepository,
37:         defaultCategories: List<String> = listOf(
38:             "Cocktail", "Ordinary Drink", "Shot", "Coffee / Tea",
39:             "Punch / Party Drink", "Homemade Liqueur", "Beer", "Soft Drink"
40:         ),
41:         defaultIngredients: List<String> = emptyList(),
42:         defaultGlasses: List<String> = emptyList()
43:     ): FilterOptions {
44:         var categories by remember { mutableStateOf(defaultCategories) }
45:         var ingredients by remember { mutableStateOf(defaultIngredients) }
46:         var glasses by remember { mutableStateOf(defaultGlasses) }
47:         
48:         LaunchedEffect(Unit) {
49:             try {
50:                 repository.getCategories().collect { categoryList ->
51:                     categories = categoryList
52:                 }
53:                 
54:                 repository.getIngredients().collect { ingredientList ->
55:                     ingredients = ingredientList
56:                 }
57:                 
58:                 repository.getGlasses().collect { glassList ->
59:                     glasses = glassList
60:                 }
61:             } catch (e: Exception) {
62:                 // Use default values if loading fails
63:             }
64:         }
65:         
66:         return FilterOptions(
67:             categories = categories,
68:             ingredients = ingredients,
69:             glasses = glasses
70:         )
71:     }
72: }
````

## File: androidApp/src/main/java/com/cocktailcraft/util/ImageLoaderSingleton.kt
````kotlin
 1: package com.cocktailcraft.util
 2: 
 3: import android.content.Context
 4: import android.os.Build
 5: import coil.ImageLoader
 6: import coil.annotation.ExperimentalCoilApi
 7: import coil.disk.DiskCache
 8: import coil.memory.MemoryCache
 9: import coil.request.CachePolicy
10: import coil.util.DebugLogger
11: import okhttp3.OkHttpClient
12: import java.io.File
13: import java.util.concurrent.TimeUnit
14: 
15: /**
16:  * Singleton for managing a shared ImageLoader instance with optimized caching.
17:  */
18: object ImageLoaderSingleton {
19:     private var imageLoader: ImageLoader? = null
20: 
21:     // Constants for cache sizes
22:     private const val MEMORY_CACHE_PERCENT = 0.25 // Use 25% of available memory for the image cache
23:     private const val DISK_CACHE_SIZE = 100 * 1024 * 1024L // 100MB disk cache
24: 
25:     /**
26:      * Get the shared ImageLoader instance, creating it if necessary.
27:      */
28:     fun getImageLoader(context: Context): ImageLoader {
29:         if (imageLoader == null) {
30:             synchronized(this) {
31:                 if (imageLoader == null) {
32:                     imageLoader = createImageLoader(context)
33:                 }
34:             }
35:         }
36:         return imageLoader!!
37:     }
38: 
39:     /**
40:      * Create a new ImageLoader with optimized settings.
41:      */
42:     private fun createImageLoader(context: Context): ImageLoader {
43:         return ImageLoader.Builder(context)
44:             // Memory cache configuration
45:             .memoryCache {
46:                 MemoryCache.Builder(context)
47:                     .maxSizePercent(MEMORY_CACHE_PERCENT)
48:                     .build()
49:             }
50:             // Disk cache configuration
51:             .diskCache {
52:                 DiskCache.Builder()
53:                     .directory(File(context.cacheDir, "image_cache"))
54:                     .maxSizeBytes(DISK_CACHE_SIZE)
55:                     .build()
56:             }
57:             // Network configuration
58:             .okHttpClient {
59:                 OkHttpClient.Builder()
60:                     .connectTimeout(15, TimeUnit.SECONDS)
61:                     .readTimeout(15, TimeUnit.SECONDS)
62:                     .build()
63:             }
64:             // Cache policies
65:             .respectCacheHeaders(false) // Override server cache policies
66:             .diskCachePolicy(CachePolicy.ENABLED)
67:             .memoryCachePolicy(CachePolicy.ENABLED)
68:             .networkCachePolicy(CachePolicy.ENABLED)
69:             // We'll skip custom decoders for now as they're causing issues
70:             // Coil has default decoders that will work fine
71:             // Enable logging in debug mode
72:             .logger(DebugLogger())
73:             .crossfade(true)
74:             .build()
75:     }
76: 
77:     /**
78:      * Clear all caches (memory and disk).
79:      */
80:     @OptIn(ExperimentalCoilApi::class)
81:     fun clearCache() {
82:         imageLoader?.let { loader ->
83:             loader.memoryCache?.clear()
84:             loader.diskCache?.clear()
85:         }
86:     }
87: }
````

## File: androidApp/src/main/java/com/cocktailcraft/util/ImageUtils.kt
````kotlin
  1: package com.cocktailcraft.util
  2: 
  3: import android.content.Context
  4: import android.graphics.drawable.Drawable
  5: import androidx.compose.runtime.Composable
  6: import androidx.compose.ui.platform.LocalContext
  7: import coil.request.ErrorResult
  8: import coil.request.ImageRequest
  9: import coil.request.SuccessResult
 10: import com.cocktailcraft.domain.model.Cocktail
 11: import kotlinx.coroutines.CoroutineScope
 12: import kotlinx.coroutines.Dispatchers
 13: import kotlinx.coroutines.launch
 14: import kotlinx.coroutines.withContext
 15: 
 16: /**
 17:  * Utility class for image operations like preloading and advanced image handling.
 18:  */
 19: object ImageUtils {
 20:     
 21:     /**
 22:      * Preload a list of images into the cache.
 23:      * 
 24:      * @param context The application context
 25:      * @param imageUrls List of image URLs to preload
 26:      * @param onProgress Optional callback for preloading progress (0.0-1.0)
 27:      */
 28:     fun preloadImages(
 29:         context: Context,
 30:         imageUrls: List<String>,
 31:         onProgress: ((Float) -> Unit)? = null
 32:     ) {
 33:         if (imageUrls.isEmpty()) return
 34:         
 35:         val imageLoader = ImageLoaderSingleton.getImageLoader(context)
 36:         val scope = CoroutineScope(Dispatchers.IO)
 37:         
 38:         scope.launch {
 39:             var loadedCount = 0
 40:             
 41:             imageUrls.forEach { url ->
 42:                 if (url.isNotBlank()) {
 43:                     val request = ImageRequest.Builder(context)
 44:                         .data(url)
 45:                         .memoryCacheKey(url)
 46:                         .diskCacheKey(url)
 47:                         .build()
 48:                     
 49:                     try {
 50:                         // Execute the request to preload the image
 51:                         val result = imageLoader.execute(request)
 52:                         
 53:                         // Update progress
 54:                         loadedCount++
 55:                         val progress = loadedCount.toFloat() / imageUrls.size
 56:                         withContext(Dispatchers.Main) {
 57:                             onProgress?.invoke(progress)
 58:                         }
 59:                         
 60:                         // Log success or failure
 61:                         when (result) {
 62:                             is SuccessResult -> {
 63:                                 // Image loaded successfully
 64:                             }
 65:                             is ErrorResult -> {
 66:                                 // Image failed to load
 67:                             }
 68:                         }
 69:                     } catch (e: Exception) {
 70:                         // Handle exceptions
 71:                     }
 72:                 }
 73:             }
 74:         }
 75:     }
 76:     
 77:     /**
 78:      * Preload cocktail images for a list of cocktails.
 79:      */
 80:     fun preloadCocktailImages(
 81:         context: Context,
 82:         cocktails: List<Cocktail>,
 83:         onProgress: ((Float) -> Unit)? = null
 84:     ) {
 85:         val imageUrls = cocktails.mapNotNull { it.imageUrl }.filter { it.isNotBlank() }
 86:         preloadImages(context, imageUrls, onProgress)
 87:     }
 88:     
 89:     /**
 90:      * Build an optimized image request with proper sizing and caching.
 91:      * 
 92:      * @param url The image URL
 93:      * @param size Target size for the image (for resizing)
 94:      * @param placeholder Placeholder drawable
 95:      * @param error Error drawable
 96:      * @param allowHardware Whether to allow hardware bitmaps (disable for shared elements)
 97:      */
 98:     @Composable
 99:     fun buildOptimizedImageRequest(
100:         url: String?,
101:         size: Int? = null,
102:         placeholder: Drawable? = null,
103:         error: Drawable? = null,
104:         allowHardware: Boolean = true
105:     ): ImageRequest {
106:         val context = LocalContext.current
107:         
108:         return ImageRequest.Builder(context)
109:             .data(url)
110:             .apply {
111:                 // Apply size if provided (for resizing)
112:                 size?.let { 
113:                     size(it)
114:                 }
115:                 
116:                 // Set placeholders
117:                 placeholder?.let { placeholder(it) }
118:                 error?.let { error(it) }
119:                 
120:                 // Cache configuration
121:                 memoryCacheKey(url)
122:                 diskCacheKey(url)
123:                 
124:                 // Performance settings
125:                 allowHardware(allowHardware)
126:                 crossfade(true)
127:             }
128:             .build()
129:     }
130: }
````

## File: androidApp/src/main/java/com/cocktailcraft/util/ListOptimizations.kt
````kotlin
  1: package com.cocktailcraft.util
  2: 
  3: import androidx.compose.foundation.lazy.LazyListState
  4: import androidx.compose.runtime.Composable
  5: import androidx.compose.runtime.LaunchedEffect
  6: import androidx.compose.runtime.derivedStateOf
  7: import androidx.compose.runtime.remember
  8: import androidx.compose.runtime.snapshotFlow
  9: import kotlinx.coroutines.flow.distinctUntilChanged
 10: import kotlinx.coroutines.flow.filter
 11: 
 12: /**
 13:  * Utility functions for optimizing list rendering performance.
 14:  */
 15: object ListOptimizations {
 16:     
 17:     /**
 18:      * Calculate a stable key for a list item.
 19:      * This helps Compose efficiently update only changed items.
 20:      * 
 21:      * @param prefix A prefix to ensure uniqueness across different lists
 22:      * @param id The unique identifier of the item
 23:      * @return A stable string key
 24:      */
 25:     fun itemKey(prefix: String, id: String): String {
 26:         return "${prefix}_$id"
 27:     }
 28:     
 29:     /**
 30:      * Calculate a stable key for a list item with an index.
 31:      * 
 32:      * @param prefix A prefix to ensure uniqueness across different lists
 33:      * @param id The unique identifier of the item
 34:      * @param index The index of the item in the list
 35:      * @return A stable string key
 36:      */
 37:     fun itemKeyWithIndex(prefix: String, id: String, index: Int): String {
 38:         return "${prefix}_${id}_$index"
 39:     }
 40:     
 41:     /**
 42:      * Detect when a lazy list has reached its end to implement pagination.
 43:      * 
 44:      * @param listState The LazyListState to monitor
 45:      * @param buffer Number of items from the end to trigger the load (default: 3)
 46:      * @param onLoadMore Callback to invoke when more items should be loaded
 47:      */
 48:     @Composable
 49:     fun LazyListState.OnBottomReached(
 50:         buffer: Int = 3,
 51:         onLoadMore: () -> Unit
 52:     ) {
 53:         // Create a derived state that checks if we're near the end of the list
 54:         val shouldLoadMore = remember {
 55:             derivedStateOf {
 56:                 val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
 57:                     ?: return@derivedStateOf false
 58:                 
 59:                 // Check if we're at the end of the list minus the buffer
 60:                 lastVisibleItem.index >= layoutInfo.totalItemsCount - 1 - buffer
 61:             }
 62:         }
 63:         
 64:         // Use LaunchedEffect to react to changes in the derived state
 65:         LaunchedEffect(shouldLoadMore) {
 66:             snapshotFlow { shouldLoadMore.value }
 67:                 .distinctUntilChanged()
 68:                 .filter { it }
 69:                 .collect {
 70:                     onLoadMore()
 71:                 }
 72:         }
 73:     }
 74:     
 75:     /**
 76:      * Detect when a lazy list has been scrolled to implement features like
 77:      * hiding/showing UI elements based on scroll position.
 78:      * 
 79:      * @param listState The LazyListState to monitor
 80:      * @param threshold Scroll position threshold to trigger the callback
 81:      * @param onScrollPastThreshold Callback with a boolean indicating if we're past the threshold
 82:      */
 83:     @Composable
 84:     fun LazyListState.OnScrollPastThreshold(
 85:         threshold: Int = 0,
 86:         onScrollPastThreshold: (Boolean) -> Unit
 87:     ) {
 88:         // Create a derived state that checks if we've scrolled past the threshold
 89:         val isPastThreshold = remember {
 90:             derivedStateOf {
 91:                 firstVisibleItemIndex > threshold || 
 92:                 (firstVisibleItemIndex == threshold && firstVisibleItemScrollOffset > 0)
 93:             }
 94:         }
 95:         
 96:         // Use LaunchedEffect to react to changes in the derived state
 97:         LaunchedEffect(isPastThreshold) {
 98:             snapshotFlow { isPastThreshold.value }
 99:                 .distinctUntilChanged()
100:                 .collect {
101:                     onScrollPastThreshold(it)
102:                 }
103:         }
104:     }
105: }
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/BaseViewModel.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import androidx.lifecycle.viewModelScope
  4: import com.cocktailcraft.util.ErrorUtils
  5: import kotlinx.coroutines.flow.MutableSharedFlow
  6: import kotlinx.coroutines.flow.MutableStateFlow
  7: import kotlinx.coroutines.flow.SharedFlow
  8: import kotlinx.coroutines.flow.StateFlow
  9: import kotlinx.coroutines.flow.asSharedFlow
 10: import kotlinx.coroutines.flow.asStateFlow
 11: import kotlinx.coroutines.launch
 12: 
 13: /**
 14:  * Base ViewModel class that provides common functionality for all ViewModels,
 15:  * including standardized error handling, loading state management, and Koin integration.
 16:  */
 17: abstract class BaseViewModel : KoinViewModel() {
 18: 
 19:     // Loading state
 20:     private val _isLoading = MutableStateFlow(false)
 21:     val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
 22: 
 23:     // Error state - can be observed for displaying error UI
 24:     private val _error = MutableStateFlow<ErrorUtils.UserFriendlyError?>(null)
 25:     val error: StateFlow<ErrorUtils.UserFriendlyError?> = _error.asStateFlow()
 26: 
 27:     // Error events - for one-time error handling (like showing a dialog)
 28:     private val _errorEvent = MutableSharedFlow<ErrorUtils.UserFriendlyError>()
 29:     val errorEvent: SharedFlow<ErrorUtils.UserFriendlyError> = _errorEvent.asSharedFlow()
 30: 
 31:     /**
 32:      * Set loading state
 33:      */
 34:     protected open fun setLoading(isLoading: Boolean) {
 35:         _isLoading.value = isLoading
 36:     }
 37: 
 38:     /**
 39:      * Handle an exception and convert it to a user-friendly error
 40:      */
 41:     protected open fun handleException(
 42:         exception: Throwable,
 43:         defaultMessage: String = "Something went wrong. Please try again.",
 44:         showAsEvent: Boolean = false,
 45:         recoveryAction: ErrorUtils.RecoveryAction? = null
 46:     ) {
 47:         val userFriendlyError = ErrorUtils.getErrorFromException(
 48:             exception = exception,
 49:             defaultMessage = defaultMessage,
 50:             recoveryAction = recoveryAction
 51:         )
 52: 
 53:         if (showAsEvent) {
 54:             // Emit as a one-time event
 55:             viewModelScope.launch {
 56:                 _errorEvent.emit(userFriendlyError)
 57:             }
 58:         } else {
 59:             // Set as a persistent state
 60:             _error.value = userFriendlyError
 61:         }
 62: 
 63:         // Always log the error
 64:         println("Error in ${this::class.simpleName}: ${exception.message}")
 65:         exception.printStackTrace()
 66:     }
 67: 
 68:     /**
 69:      * Set a user-friendly error directly
 70:      */
 71:     protected open fun setError(
 72:         title: String,
 73:         message: String,
 74:         category: ErrorUtils.ErrorCategory = ErrorUtils.ErrorCategory.UNKNOWN,
 75:         showAsEvent: Boolean = false,
 76:         recoveryAction: ErrorUtils.RecoveryAction? = null
 77:     ) {
 78:         val userFriendlyError = ErrorUtils.UserFriendlyError(
 79:             title = title,
 80:             message = message,
 81:             category = category,
 82:             recoveryAction = recoveryAction
 83:         )
 84: 
 85:         if (showAsEvent) {
 86:             viewModelScope.launch {
 87:                 _errorEvent.emit(userFriendlyError)
 88:             }
 89:         } else {
 90:             _error.value = userFriendlyError
 91:         }
 92:     }
 93: 
 94:     /**
 95:      * Clear the current error
 96:      */
 97:     fun clearError() {
 98:         _error.value = null
 99:     }
100: 
101:     /**
102:      * Execute a suspending operation with automatic error handling
103:      */
104:     protected open fun <T> executeWithErrorHandling(
105:         operation: suspend () -> T,
106:         onSuccess: (T) -> Unit,
107:         onError: ((ErrorUtils.UserFriendlyError) -> Unit)? = null,
108:         defaultErrorMessage: String = "Something went wrong. Please try again.",
109:         showAsEvent: Boolean = false,
110:         showLoading: Boolean = true,
111:         recoveryAction: ErrorUtils.RecoveryAction? = null
112:     ) {
113:         viewModelScope.launch {
114:             try {
115:                 if (showLoading) {
116:                     setLoading(true)
117:                 }
118: 
119:                 val result = operation()
120:                 onSuccess(result)
121: 
122:             } catch (e: Exception) {
123:                 val error = ErrorUtils.getErrorFromException(
124:                     exception = e,
125:                     defaultMessage = defaultErrorMessage,
126:                     recoveryAction = recoveryAction
127:                 )
128: 
129:                 if (showAsEvent) {
130:                     _errorEvent.emit(error)
131:                 } else {
132:                     _error.value = error
133:                 }
134: 
135:                 onError?.invoke(error)
136: 
137:             } finally {
138:                 if (showLoading) {
139:                     setLoading(false)
140:                 }
141:             }
142:         }
143:     }
144: }
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/CartViewModel.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import androidx.lifecycle.ViewModel
  4: import androidx.lifecycle.viewModelScope
  5: import com.cocktailcraft.domain.model.CocktailCartItem
  6: import com.cocktailcraft.domain.model.Cocktail
  7: import com.cocktailcraft.domain.repository.CartRepository
  8: import kotlinx.coroutines.flow.MutableStateFlow
  9: import kotlinx.coroutines.flow.StateFlow
 10: import kotlinx.coroutines.flow.asStateFlow
 11: import kotlinx.coroutines.launch
 12: import org.koin.core.component.KoinComponent
 13: import org.koin.core.component.inject
 14: 
 15: class CartViewModel(
 16:     private val cartRepository: CartRepository? = null
 17: ) : ViewModel(), KoinComponent {
 18:     
 19:     // Use injected repository if not provided in constructor (for production)
 20:     private val injectedCartRepository: CartRepository by inject()
 21:     
 22:     // Use the provided repository or the injected one
 23:     private val repository: CartRepository
 24:         get() = cartRepository ?: injectedCartRepository
 25:     
 26:     private val _cartItems = MutableStateFlow<List<CocktailCartItem>>(emptyList())
 27:     val cartItems: StateFlow<List<CocktailCartItem>> = _cartItems.asStateFlow()
 28:     
 29:     private val _totalPrice = MutableStateFlow(0.0)
 30:     val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()
 31:     
 32:     private val _isLoading = MutableStateFlow(false)
 33:     val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
 34:     
 35:     private val _error = MutableStateFlow<String?>(null)
 36:     val error: StateFlow<String?> = _error.asStateFlow()
 37:     
 38:     init {
 39:         loadCartItems()
 40:     }
 41:     
 42:     fun loadCartItems() {
 43:         viewModelScope.launch {
 44:             _isLoading.value = true
 45:             _error.value = null
 46:             
 47:             try {
 48:                 repository.getCartItems().collect { items ->
 49:                     _cartItems.value = items
 50:                 }
 51:                 
 52:                 repository.getCartTotal().collect { total ->
 53:                     _totalPrice.value = total
 54:                 }
 55:             } catch (e: Exception) {
 56:                 _error.value = "Failed to load cart: ${e.message}"
 57:             } finally {
 58:                 _isLoading.value = false
 59:             }
 60:         }
 61:     }
 62:     
 63:     fun addToCart(cocktail: Cocktail, quantity: Int = 1) {
 64:         viewModelScope.launch {
 65:             try {
 66:                 val cartItem = CocktailCartItem(cocktail, quantity)
 67:                 repository.addToCart(cartItem)
 68:                 loadCartItems()
 69:             } catch (e: Exception) {
 70:                 _error.value = "Failed to add to cart: ${e.message}"
 71:             }
 72:         }
 73:     }
 74:     
 75:     fun removeFromCart(cocktailId: String) {
 76:         viewModelScope.launch {
 77:             try {
 78:                 repository.removeFromCart(cocktailId)
 79:                 loadCartItems()
 80:             } catch (e: Exception) {
 81:                 _error.value = "Failed to remove from cart: ${e.message}"
 82:             }
 83:         }
 84:     }
 85:     
 86:     fun updateQuantity(cocktailId: String, quantity: Int) {
 87:         viewModelScope.launch {
 88:             try {
 89:                 repository.updateQuantity(cocktailId, quantity)
 90:                 loadCartItems()
 91:             } catch (e: Exception) {
 92:                 _error.value = "Failed to update quantity: ${e.message}"
 93:             }
 94:         }
 95:     }
 96:     
 97:     fun clearCart() {
 98:         viewModelScope.launch {
 99:             try {
100:                 repository.clearCart()
101:                 loadCartItems()
102:             } catch (e: Exception) {
103:                 _error.value = "Failed to clear cart: ${e.message}"
104:             }
105:         }
106:     }
107: }
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/CocktailDetailViewModel.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import androidx.lifecycle.viewModelScope
  4: import com.cocktailcraft.domain.model.Cocktail
  5: import com.cocktailcraft.domain.recommendation.CocktailRecommendationEngine
  6: import com.cocktailcraft.domain.repository.CocktailRepository
  7: import com.cocktailcraft.util.ErrorUtils
  8: import kotlinx.coroutines.flow.MutableStateFlow
  9: import kotlinx.coroutines.flow.StateFlow
 10: import kotlinx.coroutines.flow.asStateFlow
 11: import kotlinx.coroutines.launch
 12: 
 13: class CocktailDetailViewModel(
 14:     private val cocktailRepository: CocktailRepository,
 15:     private val recommendationEngine: CocktailRecommendationEngine
 16: ) : BaseViewModel() {
 17: 
 18:     private val _cocktail = MutableStateFlow<Cocktail?>(null)
 19:     val cocktail: StateFlow<Cocktail?> = _cocktail.asStateFlow()
 20: 
 21:     private val _isFavorite = MutableStateFlow(false)
 22:     val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
 23: 
 24:     private val _recommendations = MutableStateFlow<List<Cocktail>>(emptyList())
 25:     val recommendations: StateFlow<List<Cocktail>> = _recommendations.asStateFlow()
 26: 
 27:     private val _isLoadingRecommendations = MutableStateFlow(false)
 28:     val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations.asStateFlow()
 29: 
 30:     fun loadCocktail(id: String) {
 31:         setLoading(true)
 32:         clearError()
 33: 
 34:         viewModelScope.launch {
 35:             try {
 36:                 cocktailRepository.getCocktailById(id).collect { loadedCocktail ->
 37:                     _cocktail.value = loadedCocktail
 38: 
 39:                     // Check if this cocktail is a favorite
 40:                     checkFavoriteStatus(id)
 41: 
 42:                     // Load recommendations for this cocktail
 43:                     loadRecommendations(loadedCocktail)
 44: 
 45:                     setLoading(false)
 46:                 }
 47:             } catch (e: Exception) {
 48:                 handleException(e, "Failed to load cocktail details")
 49:             }
 50:         }
 51:     }
 52: 
 53:     private fun checkFavoriteStatus(id: String) {
 54:         viewModelScope.launch {
 55:             try {
 56:                 cocktailRepository.isCocktailFavorite(id).collect { favorite ->
 57:                     _isFavorite.value = favorite
 58:                 }
 59:             } catch (e: Exception) {
 60:                 // Just log the error but don't show to user as this is not critical
 61:                 println("Failed to check favorite status: ${e.message}")
 62:             }
 63:         }
 64:     }
 65: 
 66:     fun toggleFavorite() {
 67:         val currentCocktail = _cocktail.value ?: return
 68: 
 69:         viewModelScope.launch {
 70:             try {
 71:                 if (_isFavorite.value) {
 72:                     cocktailRepository.removeFromFavorites(currentCocktail)
 73:                     _isFavorite.value = false
 74:                 } else {
 75:                     cocktailRepository.addToFavorites(currentCocktail)
 76:                     _isFavorite.value = true
 77:                 }
 78:             } catch (e: Exception) {
 79:                 setError(
 80:                     title = "Favorite Error",
 81:                     message = "Failed to update favorite status: ${e.message}",
 82:                     category = ErrorUtils.ErrorCategory.DATA
 83:                 )
 84:             }
 85:         }
 86:     }
 87: 
 88:     /**
 89:      * Set the current cocktail directly without loading from the repository.
 90:      * This is useful when we already have the cocktail data and want to avoid an extra network call.
 91:      */
 92:     fun setCurrentCocktail(cocktail: Cocktail) {
 93:         _cocktail.value = cocktail
 94: 
 95:         // Check if this cocktail is a favorite
 96:         viewModelScope.launch {
 97:             try {
 98:                 cocktailRepository.isCocktailFavorite(cocktail.id).collect { favorite ->
 99:                     _isFavorite.value = favorite
100:                 }
101:             } catch (e: Exception) {
102:                 // Just log the error but don't show to user as this is not critical
103:                 println("Failed to check favorite status: ${e.message}")
104:             }
105:         }
106: 
107:         // Load recommendations for this cocktail
108:         loadRecommendations(cocktail)
109:     }
110: 
111:     private fun loadRecommendations(cocktail: Cocktail?) {
112:         if (cocktail == null) return
113: 
114:         _isLoadingRecommendations.value = true
115: 
116:         viewModelScope.launch {
117:             try {
118:                 val recommendedCocktails = recommendationEngine.getRecommendations(cocktail)
119:                 _recommendations.value = recommendedCocktails
120:             } catch (e: Exception) {
121:                 // Just log the error but don't show to user as recommendations are not critical
122:                 println("Failed to load recommendations: ${e.message}")
123:                 _recommendations.value = emptyList()
124:             } finally {
125:                 _isLoadingRecommendations.value = false
126:             }
127:         }
128:     }
129: }
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/FavoritesViewModel.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import androidx.lifecycle.ViewModel
  4: import androidx.lifecycle.viewModelScope
  5: import com.cocktailcraft.domain.model.Cocktail
  6: import com.cocktailcraft.domain.repository.CocktailRepository
  7: import com.cocktailcraft.domain.usecase.ToggleFavoriteUseCase
  8: import com.cocktailcraft.domain.util.Result
  9: import kotlinx.coroutines.flow.MutableStateFlow
 10: import kotlinx.coroutines.flow.StateFlow
 11: import kotlinx.coroutines.flow.asStateFlow
 12: import kotlinx.coroutines.launch
 13: import org.koin.core.component.KoinComponent
 14: import org.koin.core.component.inject
 15: 
 16: class FavoritesViewModel : ViewModel(), KoinComponent {
 17:     private val cocktailRepository: CocktailRepository by inject()
 18:     private val toggleFavoriteUseCase: ToggleFavoriteUseCase by inject()
 19:     
 20:     private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())
 21:     val favorites: StateFlow<List<Cocktail>> = _favorites.asStateFlow()
 22:     
 23:     private val _isLoading = MutableStateFlow(false)
 24:     val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
 25:     
 26:     private val _error = MutableStateFlow<String?>(null)
 27:     val error: StateFlow<String?> = _error.asStateFlow()
 28:     
 29:     init {
 30:         loadFavorites()
 31:     }
 32:     
 33:     fun loadFavorites() {
 34:         viewModelScope.launch {
 35:             _isLoading.value = true
 36:             _error.value = null
 37:             
 38:             try {
 39:                 cocktailRepository.getFavoriteCocktails().collect { cocktails ->
 40:                     _favorites.value = cocktails
 41:                 }
 42:             } catch (e: Exception) {
 43:                 _error.value = "Failed to load favorites: ${e.message}"
 44:             } finally {
 45:                 _isLoading.value = false
 46:             }
 47:         }
 48:     }
 49:     
 50:     fun addToFavorites(cocktail: Cocktail) {
 51:         viewModelScope.launch {
 52:             try {
 53:                 toggleFavoriteUseCase(cocktail).collect { result ->
 54:                     when (result) {
 55:                         is Result.Success -> {
 56:                             if (result.data) {
 57:                                 // Successfully added to favorites
 58:                                 loadFavorites()
 59:                             }
 60:                         }
 61:                         is Result.Error -> {
 62:                             _error.value = "Failed to add to favorites: ${result.message}"
 63:                         }
 64:                         is Result.Loading -> {
 65:                             // No action needed
 66:                         }
 67:                     }
 68:                 }
 69:             } catch (e: Exception) {
 70:                 _error.value = "Failed to add to favorites: ${e.message}"
 71:             }
 72:         }
 73:     }
 74:     
 75:     fun removeFromFavorites(cocktail: Cocktail) {
 76:         viewModelScope.launch {
 77:             try {
 78:                 toggleFavoriteUseCase(cocktail).collect { result ->
 79:                     when (result) {
 80:                         is Result.Success -> {
 81:                             if (!result.data) {
 82:                                 // Successfully removed from favorites
 83:                                 loadFavorites()
 84:                             }
 85:                         }
 86:                         is Result.Error -> {
 87:                             _error.value = "Failed to remove from favorites: ${result.message}"
 88:                         }
 89:                         is Result.Loading -> {
 90:                             // No action needed
 91:                         }
 92:                     }
 93:                 }
 94:             } catch (e: Exception) {
 95:                 _error.value = "Failed to remove from favorites: ${e.message}"
 96:             }
 97:         }
 98:     }
 99:     
100:     fun isFavorite(id: String, callback: (Boolean) -> Unit) {
101:         viewModelScope.launch {
102:             try {
103:                 cocktailRepository.isCocktailFavorite(id).collect { isFavorite ->
104:                     callback(isFavorite)
105:                 }
106:             } catch (e: Exception) {
107:                 _error.value = "Failed to check favorite status: ${e.message}"
108:                 callback(false)
109:             }
110:         }
111:     }
112:     
113:     fun toggleFavorite(cocktail: Cocktail) {
114:         viewModelScope.launch {
115:             try {
116:                 val isFav = favorites.value.any { it.id == cocktail.id }
117:                 if (isFav) {
118:                     removeFromFavorites(cocktail)
119:                 } else {
120:                     addToFavorites(cocktail)
121:                 }
122:             } catch (e: Exception) {
123:                 _error.value = "Failed to toggle favorite: ${e.message}"
124:             }
125:         }
126:     }
127: }
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/HomeViewModel.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import androidx.lifecycle.viewModelScope
  4: import com.cocktailcraft.domain.model.Cocktail
  5: import com.cocktailcraft.domain.repository.CocktailRepository
  6: import com.cocktailcraft.util.ErrorUtils
  7: import com.cocktailcraft.util.NetworkMonitor
  8: import kotlinx.coroutines.Job
  9: import kotlinx.coroutines.delay
 10: import kotlinx.coroutines.flow.MutableSharedFlow
 11: import kotlinx.coroutines.flow.MutableStateFlow
 12: import kotlinx.coroutines.flow.StateFlow
 13: import kotlinx.coroutines.flow.asStateFlow
 14: import kotlinx.coroutines.flow.catch
 15: import kotlinx.coroutines.flow.collectLatest
 16: import kotlinx.coroutines.flow.first
 17: import kotlinx.coroutines.launch
 18: import org.koin.core.component.KoinComponent
 19: import org.koin.core.component.inject
 20: import com.cocktailcraft.util.CocktailDebugLogger
 21: 
 22: class HomeViewModel(
 23:     private val cocktailRepository: CocktailRepository? = null
 24: ) : BaseViewModel(), KoinComponent {
 25:     
 26: 
 27:     // Use injected repository if not provided in constructor (for production)
 28:     private val injectedCocktailRepository: CocktailRepository by inject()
 29:     private val networkMonitor: NetworkMonitor by inject()
 30: 
 31:     // Use the provided repository or the injected one
 32:     val repository: CocktailRepository
 33:         get() = cocktailRepository ?: injectedCocktailRepository
 34: 
 35:     // Track offline mode status
 36:     private val _isOfflineMode = MutableStateFlow(false)
 37:     val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
 38: 
 39:     // Track network availability
 40:     private val _isNetworkAvailable = MutableStateFlow(true)
 41:     val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
 42: 
 43:     private val _cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
 44:     val cocktails: StateFlow<List<Cocktail>> = _cocktails.asStateFlow()
 45: 
 46:     private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())
 47:     val favorites: StateFlow<List<Cocktail>> = _favorites.asStateFlow()
 48: 
 49:     // Legacy error string for backward compatibility
 50:     private val _errorString = MutableStateFlow<String>("")
 51:     val errorString: StateFlow<String> = _errorString.asStateFlow()
 52: 
 53:     // Pagination state
 54:     private val _currentPage = MutableStateFlow(1)
 55:     private val _hasMoreData = MutableStateFlow(true)
 56:     val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()
 57:     private val _isLoadingMore = MutableStateFlow(false)
 58:     val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
 59:     private val PAGE_SIZE = 10
 60: 
 61:     // Search query state
 62:     private val _searchQuery = MutableStateFlow("")
 63:     val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
 64: 
 65:     // Search active state
 66:     private val _isSearchActive = MutableStateFlow(false)
 67:     val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()
 68: 
 69:     // Advanced search filters state
 70:     private val _searchFilters = MutableStateFlow(com.cocktailcraft.domain.model.SearchFilters())
 71:     val searchFilters: StateFlow<com.cocktailcraft.domain.model.SearchFilters> = _searchFilters.asStateFlow()
 72: 
 73:     // Advanced search active state
 74:     private val _isAdvancedSearchActive = MutableStateFlow(false)
 75:     val isAdvancedSearchActive: StateFlow<Boolean> = _isAdvancedSearchActive.asStateFlow()
 76: 
 77:     // Add a shared flow for connectivity status changes
 78:     private val _connectivityStatus = MutableSharedFlow<Boolean>(replay = 1)
 79: 
 80:     // Keep track of search job for debouncing
 81:     private var searchJob: Job? = null
 82: 
 83:     // Network error message with retry button
 84:     private val networkErrorMessage = "Unable to connect to the cocktail database. Please check your internet connection and try again."
 85: 
 86:     init {
 87:         CocktailDebugLogger.log("🎯 HomeViewModel init() called")
 88:         // Initialize offline mode status
 89:         _isOfflineMode.value = repository.isOfflineModeEnabled()
 90:         CocktailDebugLogger.log("   - Initial offline mode: ${_isOfflineMode.value}")
 91: 
 92:         // Start monitoring network connectivity
 93:         viewModelScope.launch {
 94:             networkMonitor.startMonitoring()
 95:             networkMonitor.isOnline.collectLatest { isOnline ->
 96:                 CocktailDebugLogger.log("📡 Network status changed: $isOnline")
 97:                 _isNetworkAvailable.value = isOnline
 98: 
 99:                 // If network becomes available and we had an error, retry loading
100:                 if (isOnline && _errorString.value.isNotBlank() && cocktails.value.isEmpty()) {
101:                     CocktailDebugLogger.log("   🔄 Network restored with error state, retrying...")
102:                     retry()
103:                 }
104: 
105:                 // If network becomes unavailable and offline mode is not enabled,
106:                 // automatically enable it
107:                 if (!isOnline && !_isOfflineMode.value) {
108:                     CocktailDebugLogger.log("   📴 Network lost, enabling offline mode")
109:                     setOfflineMode(true)
110:                 }
111:             }
112:         }
113: 
114:         // Monitor offline mode changes from repository
115:         viewModelScope.launch {
116:             _isOfflineMode.value = repository.isOfflineModeEnabled()
117:         }
118: 
119:         // Delay initial load slightly to ensure cache is ready
120:         viewModelScope.launch {
121:             CocktailDebugLogger.log("   ⏳ Delaying initial load by 100ms...")
122:             delay(100) // Small delay to ensure cache initialization
123:             
124:             // Only load if we don't have cocktails already (ViewModel might be reused)
125:             if (cocktails.value.isEmpty()) {
126:                 CocktailDebugLogger.log("   🚀 LAZY LOADING: Starting initial load with 'Cocktail' category only")
127:                 // Start with just the "Cocktail" category to reduce initial API calls
128:                 loadCocktailsByCategory("Cocktail")
129:             } else {
130:                 CocktailDebugLogger.log("   ✅ Skipping initial load - already have ${cocktails.value.size} cocktails")
131:             }
132:             
133:             loadFavorites()
134:         }
135:         
136:         monitorConnectivity()
137:     }
138: 
139:     /**
140:      * Set offline mode to a specific value.
141:      */
142:     fun setOfflineMode(enabled: Boolean) {
143:         _isOfflineMode.value = enabled
144:         repository.setOfflineMode(enabled)
145: 
146:         // If switching to online mode, reload data
147:         if (!enabled && _isNetworkAvailable.value) {
148:             retry()
149:         }
150:     }
151: 
152:     private fun monitorConnectivity() {
153:         viewModelScope.launch {
154:             _connectivityStatus.emit(checkConnectivity())
155: 
156:             // Periodically check connectivity when there's an error AND no data
157:             while (true) {
158:                 delay(30000) // Check every 30 seconds
159:                 if (_errorString.value.isNotBlank() && cocktails.value.isEmpty()) {
160:                     val isConnected = checkConnectivity()
161:                     _connectivityStatus.emit(isConnected)
162: 
163:                     // If connection restored and we had an error with no data, reload
164:                     if (isConnected && _errorString.value == networkErrorMessage) {
165:                         _errorString.value = ""
166:                         clearError()
167:                         loadCocktails()
168:                     }
169:                 }
170:             }
171:         }
172:     }
173: 
174:     private suspend fun checkConnectivity(): Boolean {
175:         CocktailDebugLogger.log("🔌 HomeViewModel.checkConnectivity() called")
176:         return try {
177:             val isConnected = repository.checkApiConnectivity().first()
178:             CocktailDebugLogger.log("   - API connectivity: $isConnected")
179:             isConnected
180:         } catch (e: Exception) {
181:             CocktailDebugLogger.log("   ❌ Connectivity check failed: ${e.message}")
182:             false
183:         }
184:     }
185: 
186:     fun loadCocktails() {
187:         CocktailDebugLogger.log("🏠 HomeViewModel.loadCocktails() called")
188:         CocktailDebugLogger.log("   🎯 LAZY LOADING: Defaulting to 'Cocktail' category")
189:         
190:         // When loadCocktails is called without a category, default to "Cocktail"
191:         // This reduces initial API calls from fetching all categories
192:         loadCocktailsByCategory("Cocktail")
193:     }
194: 
195:     /**
196:      * Load more cocktails for pagination
197:      */
198:     fun loadMoreCocktails() {
199:         // Don't load more if already loading or no more data
200:         if (_isLoadingMore.value || !_hasMoreData.value) return
201: 
202:         viewModelScope.launch {
203:             _isLoadingMore.value = true
204: 
205:             try {
206:                 // Increment page
207:                 val nextPage = _currentPage.value + 1
208: 
209:                 repository.getCocktailsSortedByNewest()
210:                     .catch { e ->
211:                         handleError("Failed to load more cocktails", e)
212:                         _isLoadingMore.value = false
213:                     }
214:                     .collect { allCocktails ->
215:                         // Calculate the range for the next page
216:                         val startIndex = _currentPage.value * PAGE_SIZE
217:                         val endIndex = startIndex + PAGE_SIZE
218: 
219:                         // Get items for the next page
220:                         val newItems = allCocktails.drop(startIndex).take(PAGE_SIZE)
221: 
222:                         if (newItems.isNotEmpty()) {
223:                             // Append new items to existing list
224:                             _cocktails.value = _cocktails.value + newItems
225:                             _currentPage.value = nextPage
226: 
227:                             // Check if we have more data
228:                             _hasMoreData.value = endIndex < allCocktails.size
229:                         } else {
230:                             // No more items to load
231:                             _hasMoreData.value = false
232:                         }
233: 
234:                         _isLoadingMore.value = false
235:                     }
236:             } catch (e: Exception) {
237:                 handleError("Failed to load more cocktails", e)
238:                 _isLoadingMore.value = false
239:             }
240:         }
241:     }
242: 
243:     private fun loadFavorites() {
244:         viewModelScope.launch {
245:             try {
246:                 repository.getFavoriteCocktails()
247:                     .catch { e ->
248:                         // Don't show error UI for favorites loading, just log
249:                         CocktailDebugLogger.log("Failed to load favorites: ${e.message}")
250:                     }
251:                     .collect { cocktailList ->
252:                         _favorites.value = cocktailList
253:                     }
254:             } catch (e: Exception) {
255:                 CocktailDebugLogger.log("Failed to load favorites: ${e.message}")
256:             }
257:         }
258:     }
259: 
260:     // Updated function with debouncing
261:     fun searchCocktails(query: String) {
262:         _searchQuery.value = query
263: 
264:         // Update the search filters with the new query
265:         _searchFilters.value = _searchFilters.value.copy(query = query)
266: 
267:         // Automatically activate search mode when query is not empty
268:         if (query.isNotEmpty() && !_isSearchActive.value) {
269:             _isSearchActive.value = true
270:         }
271: 
272:         // Cancel previous search job if it exists
273:         searchJob?.cancel()
274: 
275:         if (query.isBlank() && !_searchFilters.value.hasActiveFilters()) {
276:             _isSearchActive.value = false
277:             _isAdvancedSearchActive.value = false
278:             loadCocktails() // Reset to all cocktails if query is empty and no filters
279:             return
280:         }
281: 
282:         // Create a new search job with debounce
283:         searchJob = viewModelScope.launch {
284:             delay(300) // Debounce for 300ms
285:             setLoading(true)
286:             clearError() // Clear base class error
287:             _errorString.value = "" // Clear legacy error string
288: 
289:             try {
290:                 // Check connectivity first
291:                 if (!checkConnectivity()) {
292:                     setError(
293:                         title = "Network Error",
294:                         message = networkErrorMessage,
295:                         category = ErrorUtils.ErrorCategory.NETWORK,
296:                         recoveryAction = ErrorUtils.RecoveryAction("Retry") { retry() }
297:                     )
298:                     _errorString.value = networkErrorMessage
299:                     setLoading(false)
300:                     return@launch
301:                 }
302: 
303:                 // If we have active filters, use advanced search
304:                 if (_searchFilters.value.hasActiveFilters() || _isAdvancedSearchActive.value) {
305:                     performAdvancedSearch()
306:                 } else {
307:                     // Otherwise use basic search
308:                     repository.searchCocktailsByName(query)
309:                         .catch { e ->
310:                             handleError("Failed to search cocktails", e)
311:                         }
312:                         .collect { cocktailList ->
313:                             _cocktails.value = cocktailList
314:                             setLoading(false)
315:                         }
316:                 }
317:             } catch (e: Exception) {
318:                 handleError("Failed to search cocktails", e)
319:             }
320:         }
321:     }
322: 
323:     /**
324:      * Perform advanced search with current filters
325:      */
326:     private suspend fun performAdvancedSearch() {
327:         try {
328:             repository.advancedSearch(_searchFilters.value)
329:                 .catch { e ->
330:                     handleError("Failed to perform advanced search", e)
331:                 }
332:                 .collect { cocktailList ->
333:                     _cocktails.value = cocktailList
334:                     setLoading(false)
335:                 }
336:         } catch (e: Exception) {
337:             handleError("Failed to perform advanced search", e)
338:             setLoading(false)
339:         }
340:     }
341: 
342:     /**
343:      * Update search filters and perform search
344:      */
345:     fun updateSearchFilters(filters: com.cocktailcraft.domain.model.SearchFilters) {
346:         _searchFilters.value = filters
347:         _searchQuery.value = filters.query
348: 
349:         // Activate advanced search mode
350:         _isAdvancedSearchActive.value = filters.hasActiveFilters()
351: 
352:         // If we have a query or active filters, activate search mode
353:         if (filters.hasBasicSearch() || filters.hasActiveFilters()) {
354:             _isSearchActive.value = true
355: 
356:             // Cancel previous search job if it exists
357:             searchJob?.cancel()
358: 
359:             // Create a new search job with debounce
360:             searchJob = viewModelScope.launch {
361:                 delay(300) // Debounce for 300ms
362:                 setLoading(true)
363:                 clearError() // Clear base class error
364:                 _errorString.value = "" // Clear legacy error string
365: 
366:                 performAdvancedSearch()
367:             }
368:         } else {
369:             // If no query and no filters, reset to all cocktails
370:             _isSearchActive.value = false
371:             _isAdvancedSearchActive.value = false
372:             loadCocktails()
373:         }
374:     }
375: 
376:     /**
377:      * Clear all search filters
378:      */
379:     fun clearSearchFilters() {
380:         val clearedFilters = _searchFilters.value.clearAllFilters()
381:         updateSearchFilters(clearedFilters)
382:     }
383: 
384:     /**
385:      * Toggle advanced search mode
386:      */
387:     fun toggleAdvancedSearchMode(active: Boolean) {
388:         _isAdvancedSearchActive.value = active
389: 
390:         if (!active) {
391:             // Clear all filters except query when disabling advanced search
392:             val query = _searchQuery.value
393:             _searchFilters.value = com.cocktailcraft.domain.model.SearchFilters(query = query)
394: 
395:             // If we still have a query, perform basic search
396:             if (query.isNotBlank()) {
397:                 searchCocktails(query)
398:             } else {
399:                 // Otherwise reset to all cocktails
400:                 _isSearchActive.value = false
401:                 loadCocktails()
402:             }
403:         }
404:     }
405: 
406:     // Helper function to handle errors consistently
407:     private fun handleError(baseMessage: String, e: Throwable) {
408:         CocktailDebugLogger.log("❌ HomeViewModel.handleError() called")
409:         CocktailDebugLogger.log("   - Base message: $baseMessage")
410:         CocktailDebugLogger.log("   - Error message: ${e.message}")
411:         CocktailDebugLogger.log("   - Error type: ${e::class.simpleName}")
412:         
413:         // Create a recovery action based on the error
414:         val recoveryAction = ErrorUtils.RecoveryAction("Retry") { retry() }
415: 
416:         // Determine error category
417:         val category = when {
418:             e.message?.contains("timeout") == true -> ErrorUtils.ErrorCategory.NETWORK
419:             e.message?.contains("connection") == true ||
420:             e.message?.contains("network") == true -> ErrorUtils.ErrorCategory.NETWORK
421:             e.message?.contains("server") == true -> ErrorUtils.ErrorCategory.SERVER
422:             else -> ErrorUtils.ErrorCategory.UNKNOWN
423:         }
424:         CocktailDebugLogger.log("   - Error category: $category")
425: 
426:         // Format the error message
427:         val errorMessage = when {
428:             e.message?.contains("timeout") == true ->
429:                 "$baseMessage: The request timed out. Please try again."
430:             e.message?.contains("connection") == true ||
431:             e.message?.contains("network") == true ->
432:                 networkErrorMessage
433:             else -> "$baseMessage: ${e.message}"
434:         }
435:         CocktailDebugLogger.log("   - Formatted error: $errorMessage")
436: 
437:         // Set the error using the base class method
438:         setError(
439:             title = if (category == ErrorUtils.ErrorCategory.NETWORK) "Network Error" else "Error",
440:             message = errorMessage,
441:             category = category,
442:             recoveryAction = recoveryAction
443:         )
444: 
445:         // Also update the legacy error string for backward compatibility
446:         _errorString.value = errorMessage
447:         CocktailDebugLogger.log("   - Error string set: ${_errorString.value}")
448: 
449:         // Log the error
450:         CocktailDebugLogger.log("❌ Error set: $baseMessage: ${e.message}")
451:     }
452: 
453:     // Toggle search mode
454:     fun toggleSearchMode(active: Boolean) {
455:         _isSearchActive.value = active
456:         if (!active) {
457:             _searchQuery.value = ""
458:             loadCocktails() // Reset to all cocktails when search is deactivated
459:         }
460:     }
461: 
462:     // Load cocktails filtered by category - add this new method
463:     fun loadCocktailsByCategory(category: String?) {
464:         CocktailDebugLogger.log("🏷️ HomeViewModel.loadCocktailsByCategory() called with category: $category")
465:         
466:         // Track the current category to prevent duplicate loads
467:         val currentCategory = category ?: "Cocktail" // Default to "Cocktail" if null
468:         
469:         viewModelScope.launch {
470:             setLoading(true)
471:             clearError() // Clear base class error
472:             _errorString.value = "" // Clear legacy error string
473:             _currentPage.value = 1 // Reset to first page
474:             _hasMoreData.value = true // Reset pagination state
475: 
476:             try {
477:                 CocktailDebugLogger.log("   🏷️ Loading cocktails for category: $currentCategory")
478:                 repository.filterByCategory(currentCategory)
479:                     .catch { e ->
480:                         CocktailDebugLogger.log("   ❌ Flow error caught: ${e.message}")
481:                         // Check if we have cached data to use
482:                         val isOffline = _isOfflineMode.value || !_isNetworkAvailable.value
483:                         if (isOffline) {
484:                             CocktailDebugLogger.log("   📴 Offline mode, suppressing error")
485:                             setLoading(false)
486:                         } else {
487:                             handleError("Failed to filter cocktails", e)
488:                         }
489:                     }
490:                     .collect { cocktailList ->
491:                         CocktailDebugLogger.log("   ✅ Loaded ${cocktailList.size} cocktails for category: $currentCategory")
492:                         // Apply pagination
493:                         val paginatedList = cocktailList.take(PAGE_SIZE)
494:                         _cocktails.value = paginatedList
495:                         _hasMoreData.value = paginatedList.size < cocktailList.size
496:                         setLoading(false)
497:                         
498:                         // Clear any errors when we successfully get data
499:                         clearError() // Clear base class error
500:                         _errorString.value = "" // Clear legacy error string
501:                         CocktailDebugLogger.log("   ✅ Successfully loaded cocktails, errors cleared")
502:                     }
503:             } catch (e: Exception) {
504:                 CocktailDebugLogger.log("   ❌ Exception in loadCocktailsByCategory: ${e.message}")
505:                 CocktailDebugLogger.log("   Exception type: ${e::class.simpleName}")
506:                 
507:                 // Try to use cached data before showing error
508:                 try {
509:                     val cachedCocktails = repository.getRecentlyViewedCocktails().first()
510:                         .filter { category == null || it.category == currentCategory }
511:                     
512:                     if (cachedCocktails.isNotEmpty()) {
513:                         _cocktails.value = cachedCocktails
514:                         clearError()
515:                         _errorString.value = ""
516:                         setLoading(false)
517:                         CocktailDebugLogger.log("   ✅ Using ${cachedCocktails.size} cached cocktails")
518:                         return@launch
519:                     }
520:                 } catch (cacheException: Exception) {
521:                     CocktailDebugLogger.log("   ❌ Cache access failed: ${cacheException.message}")
522:                 }
523:                 
524:                 // Show error if no cached data
525:                 handleError("Failed to filter cocktails", e)
526:             }
527:         }
528:     }
529: 
530:     fun addToFavorites(cocktail: Cocktail) {
531:         viewModelScope.launch {
532:             try {
533:                 repository.addToFavorites(cocktail)
534:                 loadFavorites() // Refresh favorites after adding
535:             } catch (e: Exception) {
536:                 CocktailDebugLogger.log("Failed to add to favorites: ${e.message}")
537:                 // Don't show UI error for favorite operations
538:             }
539:         }
540:     }
541: 
542:     // Add retry functionality to reload data based on current state
543:     fun retry() {
544:         if (_isSearchActive.value && _searchQuery.value.isNotEmpty()) {
545:             // If in search mode, retry the search
546:             searchCocktails(_searchQuery.value)
547:         } else if (_isSearchActive.value && _searchQuery.value.isEmpty()) {
548:             // If search is active but query is empty, reset to normal mode
549:             toggleSearchMode(false)
550:         } else {
551:             // Otherwise retry loading based on selected category
552:             // This will be handled by the LaunchedEffect in the UI
553:             loadCocktails()
554:         }
555:     }
556: 
557:     fun removeFromFavorites(cocktail: Cocktail) {
558:         viewModelScope.launch {
559:             try {
560:                 repository.removeFromFavorites(cocktail)
561:                 loadFavorites() // Refresh favorites after removing
562:             } catch (e: Exception) {
563:                 CocktailDebugLogger.log("Failed to remove from favorites: ${e.message}")
564:                 // Don't show UI error for favorite operations
565:             }
566:         }
567:     }
568: 
569:     fun isFavorite(cocktailId: String): Boolean {
570:         return favorites.value.any { it.id == cocktailId }
571:     }
572: 
573:     fun toggleFavorite(cocktail: Cocktail) {
574:         if (isFavorite(cocktail.id)) {
575:             removeFromFavorites(cocktail)
576:         } else {
577:             addToFavorites(cocktail)
578:         }
579:     }
580: 
581:     /**
582:      * Get cocktails by category - improved version for recommendations
583:      * This method returns a list of cocktails based on the provided category
584:      * and ensures we always return varied recommendations
585:      */
586:     fun getCocktailsByCategory(category: String, limit: Int = 3): List<Cocktail> {
587:         // First try to get from the current list with exact category match
588:         val fromCurrentList = _cocktails.value
589:             .filter { it.category == category && it.imageUrl?.isNotBlank() == true }
590:             .shuffled() // Add randomness
591:             .take(limit)
592: 
593:         if (fromCurrentList.size >= limit) {
594:             return fromCurrentList
595:         }
596: 
597:         // If we don't have enough from the exact category, try to get any cocktails
598:         val anyRecommendations = _cocktails.value
599:             .filter { it.category != null && it.imageUrl?.isNotBlank() == true }
600:             .shuffled() // Add randomness
601:             .take(limit)
602: 
603:         if (anyRecommendations.size >= limit) {
604:             return anyRecommendations
605:         }
606: 
607:         // If we still don't have enough, trigger a background load
608:         viewModelScope.launch {
609:             try {
610:                 repository.getCocktailsSortedByNewest()
611:                     .collect { cocktailList ->
612:                         if (cocktailList.isNotEmpty()) {
613:                             _cocktails.value = cocktailList
614:                         }
615:                     }
616:             } catch (e: Exception) {
617:                 CocktailDebugLogger.log("Failed to load recommendations: ${e.message}")
618:             }
619:         }
620: 
621:         // Return a varied set of fallback cocktails based on the requested category
622:         val fallbackCocktails = when (category.lowercase()) {
623:             "cocktail" -> listOf(
624:                 createFallbackCocktail(
625:                     "11000", "Mojito", "Cocktail",
626:                     "https://www.thecocktaildb.com/images/media/drink/3z6xdi1589574603.jpg"
627:                 ),
628:                 createFallbackCocktail(
629:                     "11001", "Old Fashioned", "Cocktail",
630:                     "https://www.thecocktaildb.com/images/media/drink/vrwquq1478252802.jpg"
631:                 ),
632:                 createFallbackCocktail(
633:                     "11002", "Long Island Tea", "Cocktail",
634:                     "https://www.thecocktaildb.com/images/media/drink/nkwr4c1606770558.jpg"
635:                 )
636:             )
637:             "ordinary drink" -> listOf(
638:                 createFallbackCocktail(
639:                     "11007", "Margarita", "Ordinary Drink",
640:                     "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg"
641:                 ),
642:                 createFallbackCocktail(
643:                     "11008", "Manhattan", "Ordinary Drink",
644:                     "https://www.thecocktaildb.com/images/media/drink/yk70e31606771240.jpg"
645:                 ),
646:                 createFallbackCocktail(
647:                     "11009", "Moscow Mule", "Ordinary Drink",
648:                     "https://www.thecocktaildb.com/images/media/drink/3pylqc1504370988.jpg"
649:                 )
650:             )
651:             "shot" -> listOf(
652:                 createFallbackCocktail(
653:                     "12127", "Jello shots", "Shot",
654:                     "https://www.thecocktaildb.com/images/media/drink/l0smzo1504884904.jpg"
655:                 ),
656:                 createFallbackCocktail(
657:                     "13192", "Kamikaze", "Shot",
658:                     "https://www.thecocktaildb.com/images/media/drink/d7mo481504889531.jpg"
659:                 ),
660:                 createFallbackCocktail(
661:                     "14610", "ACID", "Shot",
662:                     "https://www.thecocktaildb.com/images/media/drink/xuxpxt1479209317.jpg"
663:                 )
664:             )
665:             else -> listOf(
666:                 createFallbackCocktail(
667:                     "11000", "Mojito", "Cocktail",
668:                     "https://www.thecocktaildb.com/images/media/drink/3z6xdi1589574603.jpg"
669:                 ),
670:                 createFallbackCocktail(
671:                     "11007", "Margarita", "Ordinary Drink",
672:                     "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg"
673:                 ),
674:                 createFallbackCocktail(
675:                     "12127", "Jello shots", "Shot",
676:                     "https://www.thecocktaildb.com/images/media/drink/l0smzo1504884904.jpg"
677:                 )
678:             )
679:         }
680: 
681:         // Combine any real recommendations with fallbacks to reach the limit
682:         val combined = (fromCurrentList + anyRecommendations + fallbackCocktails)
683:             .distinctBy { it.id } // Remove duplicates
684:             .take(limit)
685: 
686:         return combined
687:     }
688: 
689:     /**
690:      * Helper method to create fallback cocktails with consistent properties
691:      */
692:     private fun createFallbackCocktail(
693:         id: String,
694:         name: String,
695:         category: String,
696:         imageUrl: String
697:     ): Cocktail {
698:         return Cocktail(
699:             id = id,
700:             name = name,
701:             category = category,
702:             alcoholic = "Alcoholic",
703:             glass = "Cocktail glass",
704:             instructions = "Mix ingredients and serve.",
705:             imageUrl = imageUrl,
706:             ingredients = emptyList(),
707:             price = 8.99 + (id.hashCode() % 5), // Varied price based on ID
708:             stockCount = 10 + (id.hashCode() % 10) // Varied stock based on ID
709:         )
710:     }
711: 
712:     fun sortByPrice(ascending: Boolean) {
713:         viewModelScope.launch {
714:             setLoading(true)
715: 
716:             try {
717:                 val sortedList = if (ascending) {
718:                     _cocktails.value.sortedBy { it.price }
719:                 } else {
720:                     _cocktails.value.sortedByDescending { it.price }
721:                 }
722:                 _cocktails.value = sortedList
723:             } catch (e: Exception) {
724:                 handleError("Failed to sort cocktails", e)
725:             } finally {
726:                 setLoading(false)
727:             }
728:         }
729:     }
730: 
731:     fun sortByRating() {
732:         viewModelScope.launch {
733:             setLoading(true)
734: 
735:             try {
736:                 val sortedList = _cocktails.value.sortedByDescending { it.rating }
737:                 _cocktails.value = sortedList
738:             } catch (e: Exception) {
739:                 handleError("Failed to sort cocktails", e)
740:             } finally {
741:                 setLoading(false)
742:             }
743:         }
744:     }
745: 
746:     fun sortByPopularity() {
747:         viewModelScope.launch {
748:             setLoading(true)
749: 
750:             try {
751:                 val sortedList = _cocktails.value.sortedByDescending { it.popularity }
752:                 _cocktails.value = sortedList
753:             } catch (e: Exception) {
754:                 handleError("Failed to sort cocktails", e)
755:             } finally {
756:                 setLoading(false)
757:             }
758:         }
759:     }
760: 
761:     fun getCategories(): List<String> {
762:         return _cocktails.value
763:             .mapNotNull { it.category }
764:             .distinct()
765:             .filterNot { it.isBlank() }
766:             .sorted()
767:     }
768: 
769:     // Override the base clearError to also clear our legacy error string
770:     fun clearLegacyError() {
771:         clearError() // Call the base class method
772:         _errorString.value = "" // Also clear the legacy error string
773:     }
774: 
775:     // Add method to get cocktail by ID
776:     fun getCocktailById(id: String): kotlinx.coroutines.flow.Flow<Cocktail?> {
777:         viewModelScope.launch {
778:             setLoading(true)
779:             clearError() // Clear base class error
780:             _errorString.value = "" // Clear legacy error string
781:         }
782: 
783:         return kotlinx.coroutines.flow.flow {
784:             try {
785:                 repository.getCocktailById(id).collect { cocktail ->
786:                     emit(cocktail)
787:                     setLoading(false)
788:                 }
789:             } catch (e: Exception) {
790:                 handleError("Failed to load cocktail details", e)
791:                 emit(null)
792:             }
793:         }
794:     }
795: 
796:     // Add method to force refresh cocktail details
797:     fun forceRefreshCocktailDetails(id: String) {
798:         viewModelScope.launch {
799:             setLoading(true)
800:             clearError() // Clear base class error
801:             _errorString.value = "" // Clear legacy error string
802: 
803:             try {
804:                 // Clear any cached data for this cocktail
805:                 _cocktails.value = _cocktails.value.filter { it.id != id }
806: 
807:                 // Force a new API call
808:                 repository.getCocktailById(id).collect { cocktail ->
809:                     if (cocktail != null) {
810:                         // Add the refreshed cocktail to the list
811:                         _cocktails.value = _cocktails.value + cocktail
812:                     } else {
813:                         setError(
814:                             title = "Refresh Failed",
815:                             message = "Could not refresh cocktail details. Please try again.",
816:                             category = ErrorUtils.ErrorCategory.DATA,
817:                             recoveryAction = ErrorUtils.RecoveryAction("Retry") { forceRefreshCocktailDetails(id) }
818:                         )
819:                         _errorString.value = "Could not refresh cocktail details. Please try again."
820:                     }
821:                     setLoading(false)
822:                 }
823:             } catch (e: Exception) {
824:                 handleError("Failed to refresh cocktail details", e)
825:                 setLoading(false)
826:             }
827:         }
828:     }
829: }
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/KoinViewModel.kt
````kotlin
 1: package com.cocktailcraft.viewmodel
 2: 
 3: import androidx.lifecycle.ViewModel
 4: import org.koin.core.component.KoinComponent
 5: 
 6: /**
 7:  * Base ViewModel class that implements KoinComponent.
 8:  * All ViewModels that need Koin injection should extend this class.
 9:  * 
10:  * This provides a consistent pattern for dependency injection in ViewModels.
11:  */
12: abstract class KoinViewModel : ViewModel(), KoinComponent
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/OfflineModeViewModel.kt
````kotlin
 1: package com.cocktailcraft.viewmodel
 2: 
 3: import androidx.lifecycle.ViewModel
 4: import androidx.lifecycle.viewModelScope
 5: import com.cocktailcraft.data.cache.CocktailCache
 6: import com.cocktailcraft.domain.repository.CocktailRepository
 7: import com.cocktailcraft.util.NetworkMonitor
 8: import kotlinx.coroutines.flow.MutableStateFlow
 9: import kotlinx.coroutines.flow.StateFlow
10: import kotlinx.coroutines.flow.asStateFlow
11: import kotlinx.coroutines.flow.collectLatest
12: import kotlinx.coroutines.launch
13: import org.koin.core.component.KoinComponent
14: import org.koin.core.component.inject
15: 
16: class OfflineModeViewModel : ViewModel(), KoinComponent {
17: 
18:     private val cocktailRepository: CocktailRepository by inject()
19:     private val networkMonitor: NetworkMonitor by inject()
20:     private val cocktailCache: CocktailCache by inject()
21: 
22:     private val _isOfflineModeEnabled = MutableStateFlow(false)
23:     val isOfflineModeEnabled: StateFlow<Boolean> = _isOfflineModeEnabled.asStateFlow()
24: 
25:     private val _isNetworkAvailable = MutableStateFlow(true)
26:     val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
27: 
28:     private val _recentlyViewedCocktails = MutableStateFlow<List<com.cocktailcraft.domain.model.Cocktail>>(emptyList())
29:     val recentlyViewedCocktails: StateFlow<List<com.cocktailcraft.domain.model.Cocktail>> = _recentlyViewedCocktails.asStateFlow()
30: 
31:     init {
32:         // Initialize with current offline mode setting
33:         _isOfflineModeEnabled.value = cocktailRepository.isOfflineModeEnabled()
34: 
35:         // Monitor network connectivity
36:         viewModelScope.launch {
37:             networkMonitor.startMonitoring()
38:             networkMonitor.isOnline.collectLatest { isOnline ->
39:                 _isNetworkAvailable.value = isOnline
40:             }
41:         }
42: 
43:         // Load recently viewed cocktails
44:         loadRecentlyViewedCocktails()
45:     }
46: 
47:     /**
48:      * Toggle offline mode on/off.
49:      */
50:     fun toggleOfflineMode() {
51:         val newValue = !_isOfflineModeEnabled.value
52:         _isOfflineModeEnabled.value = newValue
53:         cocktailRepository.setOfflineMode(newValue)
54:     }
55: 
56:     /**
57:      * Set offline mode to a specific value.
58:      */
59:     fun setOfflineMode(enabled: Boolean) {
60:         _isOfflineModeEnabled.value = enabled
61:         cocktailRepository.setOfflineMode(enabled)
62:     }
63: 
64:     /**
65:      * Load recently viewed cocktails.
66:      */
67:     fun loadRecentlyViewedCocktails() {
68:         viewModelScope.launch {
69:             cocktailRepository.getRecentlyViewedCocktails().collectLatest { cocktails ->
70:                 _recentlyViewedCocktails.value = cocktails
71:             }
72:         }
73:     }
74: 
75:     /**
76:      * Clear the cache of all cocktails.
77:      */
78:     fun clearCache() {
79:         cocktailCache.clearCache()
80:         loadRecentlyViewedCocktails()
81:     }
82: 
83:     /**
84:      * Get the number of cached cocktails.
85:      */
86:     fun getCachedCocktailCount(): Int {
87:         return cocktailCache.getCachedCocktailCount()
88:     }
89: 
90:     override fun onCleared() {
91:         super.onCleared()
92:         networkMonitor.stopMonitoring()
93:     }
94: }
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/OrderViewModel.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import androidx.lifecycle.ViewModel
  4: import androidx.lifecycle.viewModelScope
  5: import com.cocktailcraft.domain.model.CocktailCartItem
  6: import com.cocktailcraft.domain.model.Order
  7: import com.cocktailcraft.domain.model.OrderItem
  8: import com.cocktailcraft.domain.repository.OrderRepository
  9: import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
 10: import com.cocktailcraft.domain.util.Result
 11: import kotlinx.coroutines.flow.Flow
 12: import kotlinx.coroutines.flow.MutableStateFlow
 13: import kotlinx.coroutines.flow.StateFlow
 14: import kotlinx.coroutines.flow.asStateFlow
 15: import kotlinx.coroutines.flow.flow
 16: import kotlinx.coroutines.launch
 17: import org.koin.core.component.KoinComponent
 18: import org.koin.core.component.inject
 19: import java.text.SimpleDateFormat
 20: import java.util.Date
 21: import java.util.Locale
 22: 
 23: class OrderViewModel(
 24:     private val orderRepository: OrderRepository? = null,
 25:     private val placeOrderUseCase: PlaceOrderUseCase? = null
 26: ) : ViewModel(), KoinComponent {
 27:     
 28:     // Use injected repositories if not provided in constructor (for production)
 29:     private val injectedOrderRepository: OrderRepository by inject()
 30:     private val injectedPlaceOrderUseCase: PlaceOrderUseCase by inject()
 31:     
 32:     // Use the provided repositories or the injected ones
 33:     private val repository: OrderRepository
 34:         get() = orderRepository ?: injectedOrderRepository
 35:     
 36:     private val useCase: PlaceOrderUseCase
 37:         get() = placeOrderUseCase ?: injectedPlaceOrderUseCase
 38:     
 39:     private val _orders = MutableStateFlow<List<Order>>(emptyList())
 40:     val orders: StateFlow<List<Order>> = _orders.asStateFlow()
 41:     
 42:     private val _isLoading = MutableStateFlow(false)
 43:     val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
 44:     
 45:     private val _error = MutableStateFlow<String?>(null)
 46:     val error: StateFlow<String?> = _error.asStateFlow()
 47:     
 48:     init {
 49:         loadOrders()
 50:     }
 51:     
 52:     fun loadOrders() {
 53:         viewModelScope.launch {
 54:             _isLoading.value = true
 55:             _error.value = null
 56:             
 57:             try {
 58:                 repository.getOrderHistory().collect { orderList ->
 59:                     _orders.value = orderList
 60:                 }
 61:             } catch (e: Exception) {
 62:                 _error.value = "Failed to load orders: ${e.message}"
 63:             } finally {
 64:                 _isLoading.value = false
 65:             }
 66:         }
 67:     }
 68:     
 69:     fun addOrder(order: Order) {
 70:         viewModelScope.launch {
 71:             try {
 72:                 repository.addOrder(order)
 73:                 loadOrders() // Refresh the orders list
 74:             } catch (e: Exception) {
 75:                 _error.value = "Failed to add order: ${e.message}"
 76:             }
 77:         }
 78:     }
 79:     
 80:     fun placeOrderDirectly(cartItems: List<CocktailCartItem>, totalPrice: Double) {
 81:         viewModelScope.launch {
 82:             _isLoading.value = true
 83:             _error.value = null
 84:             
 85:             try {
 86:                 // Create order object
 87:                 val orderId = "ORD-${System.currentTimeMillis()}"
 88:                 val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
 89:                 
 90:                 // Map cart items to order items
 91:                 val orderItems = cartItems.map { cartItem ->
 92:                     OrderItem(
 93:                         name = cartItem.cocktail.name,
 94:                         quantity = cartItem.quantity,
 95:                         price = cartItem.cocktail.price
 96:                     )
 97:                 }
 98:                 
 99:                 val order = Order(
100:                     id = orderId,
101:                     date = currentDate,
102:                     items = orderItems,
103:                     total = totalPrice,
104:                     status = "Processing"
105:                 )
106:                 
107:                 repository.placeOrder(order).collect { success ->
108:                     if (success) {
109:                         loadOrders() // Refresh orders list on success
110:                     } else {
111:                         _error.value = "Failed to place order. Please try again."
112:                     }
113:                 }
114:             } catch (e: Exception) {
115:                 _error.value = "Error: ${e.message}"
116:             } finally {
117:                 _isLoading.value = false
118:             }
119:         }
120:     }
121:     
122:     fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double) {
123:         viewModelScope.launch {
124:             _isLoading.value = true
125:             _error.value = null
126:             
127:             useCase(cartItems, totalPrice).collect { result ->
128:                 when (result) {
129:                     is Result.Success -> {
130:                         loadOrders() // Refresh the orders list
131:                     }
132:                     is Result.Error -> {
133:                         _error.value = "Failed to place order: ${result.message}"
134:                     }
135:                     is Result.Loading -> {
136:                         // Already handled by setting isLoading to true
137:                     }
138:                 }
139:                 _isLoading.value = false
140:             }
141:         }
142:     }
143:     
144:     fun updateOrderStatus(orderId: String, status: String) {
145:         viewModelScope.launch {
146:             try {
147:                 repository.updateOrderStatus(orderId, status)
148:                 loadOrders() // Refresh the orders list
149:             } catch (e: Exception) {
150:                 _error.value = "Failed to update order status: ${e.message}"
151:             }
152:         }
153:     }
154:     
155:     fun cancelOrder(orderId: String) {
156:         viewModelScope.launch {
157:             try {
158:                 repository.cancelOrder(orderId).collect { success ->
159:                     if (success) {
160:                         loadOrders() // Refresh orders list on success
161:                     } else {
162:                         _error.value = "Failed to cancel order. It may be too late to cancel."
163:                     }
164:                 }
165:             } catch (e: Exception) {
166:                 _error.value = "Error cancelling order: ${e.message}"
167:             }
168:         }
169:     }
170:     
171:     fun getOrderById(orderId: String): Flow<Order?> {
172:         return flow {
173:             _isLoading.value = true
174:             try {
175:                 orders.value.find { it.id == orderId }?.let { 
176:                     emit(it) 
177:                 } ?: run {
178:                     // Try to load from repository if not found in current list
179:                     repository.getOrderById(orderId).collect { order ->
180:                         emit(order)
181:                     }
182:                 }
183:             } catch (e: Exception) {
184:                 _error.value = "Failed to load order details: ${e.message}"
185:                 emit(null)
186:             } finally {
187:                 _isLoading.value = false
188:             }
189:         }
190:     }
191: }
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/ProfileViewModel.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import androidx.lifecycle.ViewModel
  4: import androidx.lifecycle.viewModelScope
  5: import com.cocktailcraft.domain.model.User
  6: import com.cocktailcraft.domain.repository.AuthRepository
  7: import kotlinx.coroutines.flow.MutableStateFlow
  8: import kotlinx.coroutines.flow.StateFlow
  9: import kotlinx.coroutines.flow.asStateFlow
 10: import kotlinx.coroutines.launch
 11: import org.koin.core.component.KoinComponent
 12: import org.koin.core.component.inject
 13: 
 14: class ProfileViewModel(
 15:     private val authRepository: AuthRepository? = null
 16: ) : ViewModel(), KoinComponent {
 17:     
 18:     // Use injected repository if not provided in constructor (for production)
 19:     private val injectedAuthRepository: AuthRepository by inject()
 20:     
 21:     // Use the provided repository or the injected one
 22:     private val repository: AuthRepository
 23:         get() = authRepository ?: injectedAuthRepository
 24:     
 25:     private val _user = MutableStateFlow<User?>(null)
 26:     val user: StateFlow<User?> = _user.asStateFlow()
 27:     
 28:     private val _isSignedIn = MutableStateFlow(false)
 29:     val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()
 30:     
 31:     private val _isLoading = MutableStateFlow(false)
 32:     val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
 33:     
 34:     private val _error = MutableStateFlow<String?>(null)
 35:     val error: StateFlow<String?> = _error.asStateFlow()
 36:     
 37:     init {
 38:         checkSignInStatus()
 39:         loadUserProfile()
 40:     }
 41:     
 42:     private fun checkSignInStatus() {
 43:         viewModelScope.launch {
 44:             repository.isUserSignedIn().collect { isSignedIn ->
 45:                 _isSignedIn.value = isSignedIn
 46:             }
 47:         }
 48:     }
 49:     
 50:     private fun loadUserProfile() {
 51:         viewModelScope.launch {
 52:             _isLoading.value = true
 53:             _error.value = null
 54:             
 55:             try {
 56:                 repository.getCurrentUser().collect { user ->
 57:                     _user.value = user
 58:                 }
 59:             } catch (e: Exception) {
 60:                 _error.value = "Failed to load profile: ${e.message}"
 61:             } finally {
 62:                 _isLoading.value = false
 63:             }
 64:         }
 65:     }
 66:     
 67:     fun signIn(email: String, password: String) {
 68:         viewModelScope.launch {
 69:             _isLoading.value = true
 70:             _error.value = null
 71:             
 72:             try {
 73:                 repository.signIn(email, password).collect { success ->
 74:                     if (success) {
 75:                         checkSignInStatus()
 76:                         loadUserProfile()
 77:                     } else {
 78:                         _error.value = "Invalid email or password"
 79:                     }
 80:                 }
 81:             } catch (e: Exception) {
 82:                 _error.value = "Sign in failed: ${e.message}"
 83:             } finally {
 84:                 _isLoading.value = false
 85:             }
 86:         }
 87:     }
 88:     
 89:     fun signUp(name: String, email: String, password: String) {
 90:         viewModelScope.launch {
 91:             _isLoading.value = true
 92:             _error.value = null
 93:             
 94:             try {
 95:                 repository.signUp(email, password).collect { success ->
 96:                     if (success) {
 97:                         updateUserName(name)
 98:                         checkSignInStatus()
 99:                         loadUserProfile()
100:                     } else {
101:                         _error.value = "Sign up failed. Email may already be in use."
102:                     }
103:                 }
104:             } catch (e: Exception) {
105:                 _error.value = "Sign up failed: ${e.message}"
106:             } finally {
107:                 _isLoading.value = false
108:             }
109:         }
110:     }
111:     
112:     fun signOut() {
113:         viewModelScope.launch {
114:             _isLoading.value = true
115:             _error.value = null
116:             
117:             try {
118:                 repository.signOut().collect { success ->
119:                     if (success) {
120:                         _user.value = null
121:                         checkSignInStatus()
122:                     } else {
123:                         _error.value = "Sign out failed"
124:                     }
125:                 }
126:             } catch (e: Exception) {
127:                 _error.value = "Sign out failed: ${e.message}"
128:             } finally {
129:                 _isLoading.value = false
130:             }
131:         }
132:     }
133:     
134:     fun updateUserName(name: String) {
135:         viewModelScope.launch {
136:             _isLoading.value = true
137:             _error.value = null
138:             
139:             try {
140:                 repository.updateUserName(name).collect { success ->
141:                     if (success) {
142:                         loadUserProfile()
143:                     } else {
144:                         _error.value = "Failed to update name"
145:                     }
146:                 }
147:             } catch (e: Exception) {
148:                 _error.value = "Failed to update name: ${e.message}"
149:             } finally {
150:                 _isLoading.value = false
151:             }
152:         }
153:     }
154:     
155:     fun clearError() {
156:         _error.value = null
157:     }
158: }
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/ReviewViewModel.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import android.util.Log
  4: import androidx.lifecycle.ViewModel
  5: import androidx.lifecycle.viewModelScope
  6: import com.cocktailcraft.domain.model.Review
  7: import kotlinx.coroutines.flow.MutableStateFlow
  8: import kotlinx.coroutines.flow.StateFlow
  9: import kotlinx.coroutines.flow.asStateFlow
 10: import kotlinx.coroutines.launch
 11: 
 12: class ReviewViewModel : ViewModel() {
 13:     private val TAG = "ReviewViewModel"
 14:     
 15:     private val _reviews = MutableStateFlow<Map<String, List<Review>>>(emptyMap())
 16:     val reviews: StateFlow<Map<String, List<Review>>> = _reviews.asStateFlow()
 17:     
 18:     private val _isLoading = MutableStateFlow(false)
 19:     val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
 20:     
 21:     private val _error = MutableStateFlow<String?>(null)
 22:     val error: StateFlow<String?> = _error.asStateFlow()
 23:     
 24:     init {
 25:         // In a real app, you would load reviews from a database or API
 26:         loadMockReviews()
 27:     }
 28:     
 29:     private fun loadMockReviews() {
 30:         viewModelScope.launch {
 31:             _isLoading.value = true
 32:             try {
 33:                 // In a real app, this would be a call to a repository
 34:                 // For now, we'll just use some mock data
 35:                 val mockReviews = createMockReviews()
 36:                 _reviews.value = mockReviews.groupBy { it.cocktailId }
 37:                 _error.value = null
 38:             } catch (e: Exception) {
 39:                 _error.value = e.message
 40:             } finally {
 41:                 _isLoading.value = false
 42:             }
 43:         }
 44:     }
 45:     
 46:     fun getReviewsForCocktail(cocktailId: String): List<Review> {
 47:         return _reviews.value[cocktailId] ?: emptyList()
 48:     }
 49:     
 50:     fun addReview(review: Review) {
 51:         viewModelScope.launch {
 52:             try {
 53:                 val currentReviews = _reviews.value.toMutableMap()
 54:                 val cocktailReviews = currentReviews[review.cocktailId]?.toMutableList() ?: mutableListOf()
 55:                 cocktailReviews.add(review)
 56:                 currentReviews[review.cocktailId] = cocktailReviews
 57:                 _reviews.value = currentReviews
 58:             } catch (e: Exception) {
 59:                 // Safely handle any exceptions during review creation
 60:                 _error.value = "Error adding review: ${e.message}"
 61:                 e.printStackTrace()
 62:             }
 63:         }
 64:     }
 65:     
 66:     private fun createMockReviews(): List<Review> {
 67:         // In a real app, this would come from a database
 68:         return listOf(
 69:             Review(
 70:                 cocktailId = "11007", // Margarita
 71:                 userName = "John",
 72:                 rating = 4.5f,
 73:                 comment = "Great classic cocktail, perfect balance of sweet and sour."
 74:             ),
 75:             Review(
 76:                 cocktailId = "11007", // Margarita
 77:                 userName = "Sarah",
 78:                 rating = 5.0f,
 79:                 comment = "My favorite! Always refreshing."
 80:             ),
 81:             Review(
 82:                 cocktailId = "11001", // Old Fashioned
 83:                 userName = "Mike",
 84:                 rating = 4.0f,
 85:                 comment = "Strong and smooth, a perfect evening drink."
 86:             )
 87:         )
 88:     }
 89:     
 90:     fun getAverageRating(cocktailId: String): Float {
 91:         val cocktailReviews = _reviews.value[cocktailId] ?: return 0f
 92:         if (cocktailReviews.isEmpty()) return 0f
 93:         return cocktailReviews.map { it.rating }.average().toFloat()
 94:     }
 95:     
 96:     // A safer method to create and add a review
 97:     fun createAndAddReview(cocktailId: String, userName: String, rating: Float, comment: String) {
 98:         viewModelScope.launch {
 99:             try {
100:                 Log.d(TAG, "Creating review for cocktail: $cocktailId, user: $userName")
101:                 
102:                 // Create review with safe parameters
103:                 val review = Review(
104:                     cocktailId = cocktailId,
105:                     userName = userName.trim(),
106:                     rating = rating.coerceIn(0f, 5f),
107:                     comment = comment.trim()
108:                 )
109:                 
110:                 Log.d(TAG, "Successfully created review object")
111:                 
112:                 // Add to reviews list
113:                 val currentReviews = _reviews.value.toMutableMap()
114:                 val cocktailReviews = currentReviews[cocktailId]?.toMutableList() ?: mutableListOf()
115:                 cocktailReviews.add(review)
116:                 currentReviews[cocktailId] = cocktailReviews
117:                 _reviews.value = currentReviews
118:                 
119:                 Log.d(TAG, "Successfully added review to the list")
120:             } catch (e: Exception) {
121:                 Log.e(TAG, "Error adding review", e)
122:                 _error.value = "Error adding review: ${e.message}"
123:                 e.printStackTrace()
124:             }
125:         }
126:     }
127: }
````

## File: androidApp/src/main/java/com/cocktailcraft/viewmodel/ThemeViewModel.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import androidx.lifecycle.viewModelScope
  4: import com.cocktailcraft.domain.model.UserPreferences
  5: import com.cocktailcraft.domain.repository.AuthRepository
  6: import kotlinx.coroutines.flow.MutableStateFlow
  7: import kotlinx.coroutines.flow.StateFlow
  8: import kotlinx.coroutines.flow.asStateFlow
  9: import kotlinx.coroutines.flow.first
 10: import kotlinx.coroutines.launch
 11: import org.koin.core.component.inject
 12: 
 13: /**
 14:  * ViewModel for managing theme preferences.
 15:  * Supports both constructor injection (for testing) and Koin injection (for production).
 16:  */
 17: class ThemeViewModel(
 18:     private val authRepository: AuthRepository? = null
 19: ) : BaseViewModel() {
 20: 
 21:     // Use injected repository if not provided in constructor (for production)
 22:     private val injectedAuthRepository: AuthRepository by inject()
 23: 
 24:     // Use the provided repository or the injected one
 25:     private val repository: AuthRepository
 26:         get() = authRepository ?: injectedAuthRepository
 27: 
 28:     private val _isDarkMode = MutableStateFlow(false)
 29:     val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
 30: 
 31:     private val _followSystemTheme = MutableStateFlow(true)
 32:     val followSystemTheme: StateFlow<Boolean> = _followSystemTheme.asStateFlow()
 33: 
 34:     // Current system dark mode state
 35:     private val _isSystemInDarkMode = MutableStateFlow(false)
 36: 
 37:     init {
 38:         loadThemePreference()
 39:     }
 40: 
 41:     /**
 42:      * Updates the system dark mode state
 43:      */
 44:     fun updateSystemDarkMode(isDark: Boolean) {
 45:         _isSystemInDarkMode.value = isDark
 46:         // If following system theme, update the dark mode state
 47:         if (_followSystemTheme.value) {
 48:             _isDarkMode.value = isDark
 49:         }
 50:     }
 51: 
 52:     private fun loadThemePreference() {
 53:         viewModelScope.launch {
 54:             setLoading(true)
 55:             try {
 56:                 val preferences = repository.getUserPreferences().first()
 57:                 _followSystemTheme.value = preferences.followSystemTheme
 58: 
 59:                 // If following system theme, use system setting, otherwise use saved preference
 60:                 _isDarkMode.value = if (preferences.followSystemTheme) {
 61:                     _isSystemInDarkMode.value
 62:                 } else {
 63:                     preferences.darkMode
 64:                 }
 65:             } catch (e: Exception) {
 66:                 // If there's an error, default to system setting
 67:                 _followSystemTheme.value = true
 68:                 _isDarkMode.value = _isSystemInDarkMode.value
 69:             } finally {
 70:                 setLoading(false)
 71:             }
 72:         }
 73:     }
 74: 
 75:     fun toggleDarkMode() {
 76:         viewModelScope.launch {
 77:             // Only toggle if not following system theme
 78:             if (!_followSystemTheme.value) {
 79:                 val currentValue = _isDarkMode.value
 80:                 val newValue = !currentValue
 81:                 _isDarkMode.value = newValue
 82: 
 83:                 try {
 84:                     // Get current preferences first
 85:                     val currentPreferences = repository.getUserPreferences().first()
 86: 
 87:                     // Update with new dark mode value
 88:                     val updatedPreferences = currentPreferences.copy(darkMode = newValue)
 89:                     repository.updateUserPreferences(updatedPreferences)
 90:                 } catch (e: Exception) {
 91:                     // If saving fails, revert the UI state
 92:                     _isDarkMode.value = currentValue
 93:                 }
 94:             }
 95:         }
 96:     }
 97: 
 98:     fun setDarkMode(enabled: Boolean) {
 99:         viewModelScope.launch {
100:             if (_isDarkMode.value != enabled) {
101:                 _isDarkMode.value = enabled
102: 
103:                 try {
104:                     // Get current preferences first
105:                     val currentPreferences = repository.getUserPreferences().first()
106: 
107:                     // Update with new dark mode value and ensure follow system is disabled
108:                     val updatedPreferences = currentPreferences.copy(
109:                         darkMode = enabled,
110:                         followSystemTheme = false
111:                     )
112:                     repository.updateUserPreferences(updatedPreferences)
113:                     _followSystemTheme.value = false
114:                 } catch (e: Exception) {
115:                     // If saving fails, revert the UI state
116:                     _isDarkMode.value = !enabled
117:                 }
118:             }
119:         }
120:     }
121: 
122:     /**
123:      * Toggle whether to follow the system theme
124:      */
125:     fun toggleFollowSystemTheme() {
126:         viewModelScope.launch {
127:             val currentValue = _followSystemTheme.value
128:             val newValue = !currentValue
129:             _followSystemTheme.value = newValue
130: 
131:             try {
132:                 // Get current preferences first
133:                 val currentPreferences = repository.getUserPreferences().first()
134: 
135:                 if (newValue) {
136:                     // If enabling follow system, update dark mode to match system
137:                     _isDarkMode.value = _isSystemInDarkMode.value
138: 
139:                     // Update preferences
140:                     val updatedPreferences = currentPreferences.copy(
141:                         followSystemTheme = newValue,
142:                         darkMode = _isSystemInDarkMode.value
143:                     )
144:                     repository.updateUserPreferences(updatedPreferences)
145:                 } else {
146:                     // If disabling follow system, keep current dark mode
147:                     val updatedPreferences = currentPreferences.copy(
148:                         followSystemTheme = newValue
149:                     )
150:                     repository.updateUserPreferences(updatedPreferences)
151:                 }
152:             } catch (e: Exception) {
153:                 // If saving fails, revert the UI state
154:                 _followSystemTheme.value = currentValue
155:             }
156:         }
157:     }
158: }
````

## File: androidApp/src/main/java/com/cocktailcraft/CocktailCraftApplication.kt
````kotlin
 1: package com.cocktailcraft
 2: 
 3: import android.app.Application
 4: import com.cocktailcraft.di.appModule
 5: import com.cocktailcraft.di.platformModule
 6: import com.cocktailcraft.di.recommendationModule
 7: import org.koin.android.ext.koin.androidContext
 8: import org.koin.android.ext.koin.androidLogger
 9: import org.koin.core.context.startKoin
10: 
11: class CocktailCraftApplication : Application() {
12:     override fun onCreate() {
13:         super.onCreate()
14: 
15:         startKoin {
16:             androidLogger()
17:             androidContext(this@CocktailCraftApplication)
18:             modules(appModule + platformModule() + recommendationModule)
19:         }
20:     }
21: }
````

## File: androidApp/src/main/java/com/cocktailcraft/MainActivity.kt
````kotlin
 1: package com.cocktailcraft
 2: 
 3: import android.os.Bundle
 4: import androidx.activity.ComponentActivity
 5: import androidx.activity.compose.setContent
 6: import androidx.compose.foundation.isSystemInDarkTheme
 7: import androidx.compose.foundation.layout.fillMaxSize
 8: import androidx.compose.material3.MaterialTheme
 9: import androidx.compose.material3.Surface
10: import androidx.compose.runtime.DisposableEffect
11: import androidx.compose.runtime.collectAsState
12: import androidx.compose.runtime.getValue
13: import androidx.compose.ui.Modifier
14: import androidx.lifecycle.viewmodel.compose.viewModel
15: import com.cocktailcraft.ui.main.MainScreen
16: import com.cocktailcraft.ui.theme.AnimatedCocktailBarTheme
17: import com.cocktailcraft.viewmodel.ThemeViewModel
18: 
19: class MainActivity : ComponentActivity() {
20:     override fun onCreate(savedInstanceState: Bundle?) {
21:         super.onCreate(savedInstanceState)
22:         setContent {
23:             // Get the ThemeViewModel to observe dark mode preference
24:             val themeViewModel: ThemeViewModel = viewModel()
25:             val isDarkMode by themeViewModel.isDarkMode.collectAsState()
26: 
27:             // Get the current system dark mode state
28:             val isSystemInDarkTheme = isSystemInDarkTheme()
29: 
30:             // Update the ThemeViewModel with the current system dark mode state
31:             DisposableEffect(isSystemInDarkTheme) {
32:                 themeViewModel.updateSystemDarkMode(isSystemInDarkTheme)
33:                 onDispose { }
34:             }
35: 
36:             // Use the dark mode value from the ThemeViewModel
37:             AnimatedCocktailBarTheme(darkTheme = isDarkMode) {
38:                 Surface(
39:                     modifier = Modifier.fillMaxSize(),
40:                     color = MaterialTheme.colorScheme.background
41:                 ) {
42:                     MainScreen()
43:                 }
44:             }
45:         }
46:     }
47: }
````

## File: androidApp/src/main/res/drawable/bg_pattern_dark.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <vector xmlns:android="http://schemas.android.com/apk/res/android"
 3:     android:width="24dp"
 4:     android:height="24dp"
 5:     android:viewportWidth="24"
 6:     android:viewportHeight="24">
 7:     <path
 8:         android:fillColor="#1E1E1E"
 9:         android:pathData="M0,0h24v24h-24z"/>
10:     <path
11:         android:fillColor="#2A2A2A"
12:         android:pathData="M0,0L24,24"/>
13:     <path
14:         android:fillColor="#2A2A2A"
15:         android:pathData="M24,0L0,24"/>
16:     <path
17:         android:fillColor="#2A2A2A"
18:         android:pathData="M12,0L12,24"/>
19:     <path
20:         android:fillColor="#2A2A2A"
21:         android:pathData="M0,12L24,12"/>
22: </vector>
````

## File: androidApp/src/main/res/drawable/bg_pattern_light.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <vector xmlns:android="http://schemas.android.com/apk/res/android"
 3:     android:width="24dp"
 4:     android:height="24dp"
 5:     android:viewportWidth="24"
 6:     android:viewportHeight="24">
 7:     <path
 8:         android:fillColor="#F5F5F5"
 9:         android:pathData="M0,0h24v24h-24z"/>
10:     <path
11:         android:fillColor="#EBEBEB"
12:         android:pathData="M0,0L24,24"/>
13:     <path
14:         android:fillColor="#EBEBEB"
15:         android:pathData="M24,0L0,24"/>
16:     <path
17:         android:fillColor="#EBEBEB"
18:         android:pathData="M12,0L12,24"/>
19:     <path
20:         android:fillColor="#EBEBEB"
21:         android:pathData="M0,12L24,12"/>
22: </vector>
````

## File: androidApp/src/main/res/drawable/cocktail_placeholder_dark.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <vector xmlns:android="http://schemas.android.com/apk/res/android"
 3:     android:width="24dp"
 4:     android:height="24dp"
 5:     android:viewportWidth="24"
 6:     android:viewportHeight="24">
 7:     <path
 8:         android:fillColor="#333333"
 9:         android:pathData="M0,0h24v24h-24z"/>
10:     <path
11:         android:fillColor="#666666"
12:         android:pathData="M12,2C8.14,2 5,5.14 5,9C5,11.38 6.19,13.47 8,14.74V17C8,17.55 8.45,18 9,18H15C15.55,18 16,17.55 16,17V14.74C17.81,13.47 19,11.38 19,9C19,5.14 15.86,2 12,2M9,21C9,21.55 9.45,22 10,22H14C14.55,22 15,21.55 15,21V20H9V21Z"/>
13: </vector>
````

## File: androidApp/src/main/res/drawable/cocktail_placeholder_light.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <vector xmlns:android="http://schemas.android.com/apk/res/android"
 3:     android:width="24dp"
 4:     android:height="24dp"
 5:     android:viewportWidth="24"
 6:     android:viewportHeight="24">
 7:     <path
 8:         android:fillColor="#DDDDDD"
 9:         android:pathData="M0,0h24v24h-24z"/>
10:     <path
11:         android:fillColor="#AAAAAA"
12:         android:pathData="M12,2C8.14,2 5,5.14 5,9C5,11.38 6.19,13.47 8,14.74V17C8,17.55 8.45,18 9,18H15C15.55,18 16,17.55 16,17V14.74C17.81,13.47 19,11.38 19,9C19,5.14 15.86,2 12,2M9,21C9,21.55 9.45,22 10,22H14C14.55,22 15,21.55 15,21V20H9V21Z"/>
13: </vector>
````

## File: androidApp/src/main/res/drawable/empty_state_dark.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <vector xmlns:android="http://schemas.android.com/apk/res/android"
 3:     android:width="24dp"
 4:     android:height="24dp"
 5:     android:viewportWidth="24"
 6:     android:viewportHeight="24">
 7:     <path
 8:         android:fillColor="#2A2A2A"
 9:         android:pathData="M0,0h24v24h-24z"/>
10:     <path
11:         android:fillColor="#777777"
12:         android:pathData="M12,2C6.47,2 2,6.47 2,12C2,17.53 6.47,22 12,22C17.53,22 22,17.53 22,12C22,6.47 17.53,2 12,2M12,20C7.58,20 4,16.42 4,12C4,7.58 7.58,4 12,4C16.42,4 20,7.58 20,12C20,16.42 16.42,20 12,20M15.5,11C16.33,11 17,10.33 17,9.5C17,8.67 16.33,8 15.5,8C14.67,8 14,8.67 14,9.5C14,10.33 14.67,11 15.5,11M8.5,11C9.33,11 10,10.33 10,9.5C10,8.67 9.33,8 8.5,8C7.67,8 7,8.67 7,9.5C7,10.33 7.67,11 8.5,11M12,17.5C14.33,17.5 16.31,16.04 17.11,14H6.89C7.69,16.04 9.67,17.5 12,17.5Z"/>
13: </vector>
````

## File: androidApp/src/main/res/drawable/empty_state_light.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <vector xmlns:android="http://schemas.android.com/apk/res/android"
 3:     android:width="24dp"
 4:     android:height="24dp"
 5:     android:viewportWidth="24"
 6:     android:viewportHeight="24">
 7:     <path
 8:         android:fillColor="#EEEEEE"
 9:         android:pathData="M0,0h24v24h-24z"/>
10:     <path
11:         android:fillColor="#CCCCCC"
12:         android:pathData="M12,2C6.47,2 2,6.47 2,12C2,17.53 6.47,22 12,22C17.53,22 22,17.53 22,12C22,6.47 17.53,2 12,2M12,20C7.58,20 4,16.42 4,12C4,7.58 7.58,4 12,4C16.42,4 20,7.58 20,12C20,16.42 16.42,20 12,20M15.5,11C16.33,11 17,10.33 17,9.5C17,8.67 16.33,8 15.5,8C14.67,8 14,8.67 14,9.5C14,10.33 14.67,11 15.5,11M8.5,11C9.33,11 10,10.33 10,9.5C10,8.67 9.33,8 8.5,8C7.67,8 7,8.67 7,9.5C7,10.33 7.67,11 8.5,11M12,17.5C14.33,17.5 16.31,16.04 17.11,14H6.89C7.69,16.04 9.67,17.5 12,17.5Z"/>
13: </vector>
````

## File: androidApp/src/main/res/drawable/ic_launcher_foreground.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <vector xmlns:android="http://schemas.android.com/apk/res/android"
 3:     android:width="108dp"
 4:     android:height="108dp"
 5:     android:viewportWidth="108"
 6:     android:viewportHeight="108"
 7:     android:tint="#000000">
 8:     <group android:scaleX="2.61"
 9:         android:scaleY="2.61"
10:         android:translateX="22.68"
11:         android:translateY="22.68">
12:         <path
13:             android:fillColor="@android:color/white"
14:             android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8zM12,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6 6,-2.69 6,-6 -2.69,-6 -6,-6zM12,16c-2.21,0 -4,-1.79 -4,-4s1.79,-4 4,-4 4,1.79 4,4 -1.79,4 -4,4z"/>
15:     </group>
16: </vector>
````

## File: androidApp/src/main/res/drawable/logo_dark.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <vector xmlns:android="http://schemas.android.com/apk/res/android"
 3:     android:width="24dp"
 4:     android:height="24dp"
 5:     android:viewportWidth="24"
 6:     android:viewportHeight="24">
 7:     <path
 8:         android:fillColor="#FF8A65"
 9:         android:pathData="M7,3C6.45,3 6,3.45 6,4V5H5C3.9,5 3,5.9 3,7V8C3,9.1 3.9,10 5,10H7V16C7,17.1 7.9,18 9,18H10V19C10,20.1 10.9,21 12,21H15C16.1,21 17,20.1 17,19V18H18C19.1,18 20,17.1 20,16V10H19C17.9,10 17,9.1 17,8V7C17,5.9 16.1,5 15,5H14V4C14,3.45 13.55,3 13,3H7M7,5H13V7C13,8.1 13.9,9 15,9H18V16H17C15.9,16 15,16.9 15,18V19H12V18C12,16.9 11.1,16 10,16H9V10H7C5.9,10 5,9.1 5,8V7H6C6.55,7 7,6.55 7,6V5Z"/>
10: </vector>
````

## File: androidApp/src/main/res/drawable/logo_light.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <vector xmlns:android="http://schemas.android.com/apk/res/android"
 3:     android:width="24dp"
 4:     android:height="24dp"
 5:     android:viewportWidth="24"
 6:     android:viewportHeight="24">
 7:     <path
 8:         android:fillColor="#EB6A43"
 9:         android:pathData="M7,3C6.45,3 6,3.45 6,4V5H5C3.9,5 3,5.9 3,7V8C3,9.1 3.9,10 5,10H7V16C7,17.1 7.9,18 9,18H10V19C10,20.1 10.9,21 12,21H15C16.1,21 17,20.1 17,19V18H18C19.1,18 20,17.1 20,16V10H19C17.9,10 17,9.1 17,8V7C17,5.9 16.1,5 15,5H14V4C14,3.45 13.55,3 13,3H7M7,5H13V7C13,8.1 13.9,9 15,9H18V16H17C15.9,16 15,16.9 15,18V19H12V18C12,16.9 11.1,16 10,16H9V10H7C5.9,10 5,9.1 5,8V7H6C6.55,7 7,6.55 7,6V5Z"/>
10: </vector>
````

## File: androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
````xml
1: <?xml version="1.0" encoding="utf-8"?>
2: <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
3:     <background android:drawable="@color/ic_launcher_background"/>
4:     <foreground android:drawable="@drawable/ic_launcher_foreground"/>
5: </adaptive-icon>
````

## File: androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
````xml
1: <?xml version="1.0" encoding="utf-8"?>
2: <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
3:     <background android:drawable="@color/ic_launcher_background"/>
4:     <foreground android:drawable="@drawable/ic_launcher_foreground"/>
5: </adaptive-icon>
````

## File: androidApp/src/main/res/values/colors.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <resources>
 3:     <!-- Primary Colors -->
 4:     <color name="cocktail_primary">#5E2CA5</color>
 5:     <color name="cocktail_primary_dark">#371A64</color>
 6:     <color name="cocktail_primary_light">#8A4FD3</color>
 7: 
 8:     <!-- Secondary Colors -->
 9:     <color name="cocktail_accent">#FF6B6B</color>
10:     <color name="cocktail_accent_light">#FFA5A5</color>
11: 
12:     <!-- Background Colors -->
13:     <color name="background_light">#F8F9FA</color>
14:     <color name="background_dark">#121212</color>
15: 
16:     <!-- Text Colors -->
17:     <color name="text_primary_light">#212529</color>
18:     <color name="text_primary_dark">#F8F9FA</color>
19:     <color name="text_secondary_light">#495057</color>
20:     <color name="text_secondary_dark">#ADB5BD</color>
21: 
22:     <!-- Status Colors -->
23:     <color name="success">#4CAF50</color>
24:     <color name="error">#F44336</color>
25:     <color name="warning">#FFC107</color>
26:     <color name="info">#2196F3</color>
27: 
28:     <!-- System Colors -->
29:     <color name="black">#000000</color>
30:     <color name="white">#FFFFFF</color>
31: </resources>
````

## File: androidApp/src/main/res/values/ic_launcher_background.xml
````xml
1: <?xml version="1.0" encoding="utf-8"?>
2: <resources>
3:     <color name="ic_launcher_background">#FFFFFF</color>
4: </resources>
````

## File: androidApp/src/main/res/values/strings.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <resources>
 3:     <string name="app_name">Cocktail Bar</string>
 4:     
 5:     <!-- Authentication -->
 6:     <string name="sign_in">Sign In</string>
 7:     <string name="sign_up">Sign Up</string>
 8:     <string name="email">Email</string>
 9:     <string name="password">Password</string>
10:     <string name="confirm_password">Confirm Password</string>
11:     <string name="forgot_password">Forgot Password?</string>
12:     <string name="reset_password">Reset Password</string>
13:     <string name="logout">Logout</string>
14: 
15:     <!-- Navigation -->
16:     <string name="home">Home</string>
17:     <string name="cart">Cart</string>
18:     <string name="profile">Profile</string>
19:     <string name="favorites">Favorites</string>
20:     <string name="orders">Orders</string>
21: 
22:     <!-- Product Related -->
23:     <string name="add_to_cart">Add to Cart</string>
24:     <string name="remove_from_cart">Remove</string>
25:     <string name="price">Price</string>
26:     <string name="quantity">Quantity</string>
27:     <string name="total">Total</string>
28:     <string name="checkout">Checkout</string>
29:     <string name="add_to_favorites">Add to Favorites</string>
30:     <string name="remove_from_favorites">Remove from Favorites</string>
31: 
32:     <!-- Search and Filter -->
33:     <string name="search">Search</string>
34:     <string name="filter">Filter</string>
35:     <string name="sort">Sort</string>
36:     <string name="sort_by_name_asc">Name: A to Z</string>
37:     <string name="sort_by_name_desc">Name: Z to A</string>
38:     <string name="filter_by_alcoholic">Alcoholic</string>
39:     <string name="filter_by_non_alcoholic">Non-Alcoholic</string>
40:     <string name="filter_by_category">Filter by Category</string>
41:     <string name="filter_by_glass">Filter by Glass</string>
42:     <string name="filter_by_ingredient">Filter by Ingredient</string>
43: 
44:     <!-- Settings -->
45:     <string name="settings">Settings</string>
46:     <string name="dark_mode">Dark Mode</string>
47:     <string name="notifications">Notifications</string>
48:     <string name="language">Language</string>
49: 
50:     <!-- Error Messages -->
51:     <string name="error_network">Network error. Please check your connection.</string>
52:     <string name="error_generic">Something went wrong. Please try again.</string>
53:     <string name="error_invalid_credentials">Invalid email or password</string>
54:     <string name="error_passwords_dont_match">Passwords don\'t match</string>
55: </resources>
````

## File: androidApp/src/main/res/values/themes.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <resources>
 3:     <!-- Base application theme -->
 4:     <style name="Theme.CocktailBar" parent="Theme.Material3.DayNight.NoActionBar">
 5:         <!-- Primary brand colors -->
 6:         <item name="colorPrimary">@color/cocktail_primary</item>
 7:         <item name="colorPrimaryVariant">@color/cocktail_primary_dark</item>
 8:         <item name="colorOnPrimary">@color/white</item>
 9: 
10:         <!-- Secondary brand colors -->
11:         <item name="colorSecondary">@color/cocktail_accent</item>
12:         <item name="colorSecondaryVariant">@color/cocktail_accent_light</item>
13:         <item name="colorOnSecondary">@color/black</item>
14: 
15:         <!-- Status bar color -->
16:         <item name="android:statusBarColor">@color/cocktail_primary_dark</item>
17: 
18:         <!-- Background colors -->
19:         <item name="android:colorBackground">@color/background_light</item>
20:         <item name="colorSurface">@color/background_light</item>
21:         <item name="colorOnBackground">@color/text_primary_light</item>
22:         <item name="colorOnSurface">@color/text_primary_light</item>
23: 
24:         <!-- Text colors -->
25:         <item name="android:textColorPrimary">@color/text_primary_light</item>
26:         <item name="android:textColorSecondary">@color/text_secondary_light</item>
27: 
28:         <!-- Custom attributes -->
29:         <item name="elevationOverlayEnabled">true</item>
30:         <item name="android:windowLightStatusBar">false</item>
31:     </style>
32: 
33:     <!-- Dark theme -->
34:     <style name="Theme.CocktailBar.Dark" parent="Theme.Material3.DayNight.NoActionBar">
35:         <!-- Primary brand colors -->
36:         <item name="colorPrimary">@color/cocktail_primary_light</item>
37:         <item name="colorPrimaryVariant">@color/cocktail_primary</item>
38:         <item name="colorOnPrimary">@color/black</item>
39: 
40:         <!-- Secondary brand colors -->
41:         <item name="colorSecondary">@color/cocktail_accent_light</item>
42:         <item name="colorSecondaryVariant">@color/cocktail_accent</item>
43:         <item name="colorOnSecondary">@color/black</item>
44: 
45:         <!-- Status bar color -->
46:         <item name="android:statusBarColor">@color/background_dark</item>
47: 
48:         <!-- Background colors -->
49:         <item name="android:colorBackground">@color/background_dark</item>
50:         <item name="colorSurface">@color/background_dark</item>
51:         <item name="colorOnBackground">@color/text_primary_dark</item>
52:         <item name="colorOnSurface">@color/text_primary_dark</item>
53: 
54:         <!-- Text colors -->
55:         <item name="android:textColorPrimary">@color/text_primary_dark</item>
56:         <item name="android:textColorSecondary">@color/text_secondary_dark</item>
57: 
58:         <!-- Custom attributes -->
59:         <item name="elevationOverlayEnabled">true</item>
60:         <item name="android:windowLightStatusBar">true</item>
61:     </style>
62: </resources>
````

## File: androidApp/src/main/AndroidManifest.xml
````xml
 1: <?xml version="1.0" encoding="utf-8"?>
 2: <manifest xmlns:android="http://schemas.android.com/apk/res/android">
 3:     <uses-permission android:name="android.permission.INTERNET" />
 4:     <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 5: 
 6:     <application
 7:         android:name="com.cocktailcraft.CocktailCraftApplication"
 8:         android:allowBackup="true"
 9:         android:icon="@mipmap/ic_launcher"
10:         android:label="@string/app_name"
11:         android:roundIcon="@mipmap/ic_launcher_round"
12:         android:supportsRtl="true"
13:         android:theme="@style/Theme.CocktailBar">
14:         <activity
15:             android:name="com.cocktailcraft.MainActivity"
16:             android:exported="true"
17:             android:theme="@style/Theme.CocktailBar">
18:             <intent-filter>
19:                 <action android:name="android.intent.action.MAIN" />
20:                 <category android:name="android.intent.category.LAUNCHER" />
21:             </intent-filter>
22:         </activity>
23:     </application>
24: </manifest>
````

## File: androidApp/src/test/java/com/cocktailcraft/data/repository/AuthRepositoryImplTest.kt
````kotlin
  1: package com.cocktailcraft.data.repository
  2: 
  3: import app.cash.turbine.test
  4: import com.russhwolf.settings.Settings
  5: import kotlinx.coroutines.test.runTest
  6: import kotlinx.serialization.json.Json
  7: import org.junit.Before
  8: import org.junit.Test
  9: import org.mockito.kotlin.any
 10: import org.mockito.kotlin.mock
 11: import org.mockito.kotlin.whenever
 12: import kotlin.test.assertEquals
 13: import kotlin.test.assertFalse
 14: import kotlin.test.assertNotNull
 15: import kotlin.test.assertTrue
 16: 
 17: class AuthRepositoryImplTest {
 18:     
 19:     private lateinit var repository: AuthRepositoryImpl
 20:     private val settings: Settings = mock()
 21:     private val json = Json { ignoreUnknownKeys = true }
 22:     
 23:     @Before
 24:     fun setup() = runTest {
 25:         repository = AuthRepositoryImpl(settings, json)
 26:     }
 27:     
 28:     @Test
 29:     fun `signUp should return success when email is not taken`() = runTest {
 30:         // Mock settings behavior
 31:         whenever(settings.getStringOrNull(any())).thenReturn(null)
 32:         
 33:         // Test sign up
 34:         repository.signUp("test@example.com", "password").test {
 35:             assertTrue(awaitItem())
 36:             awaitComplete()
 37:         }
 38:     }
 39:     
 40:     @Test
 41:     fun `signUp should return failure when email is already taken`() = runTest {
 42:         // Mock settings behavior to simulate existing user
 43:         val existingUserJson = """
 44:             [{"id":"1","email":"test@example.com","name":"Test User","preferences":{}}]
 45:         """.trimIndent()
 46:         whenever(settings.getStringOrNull("users")).thenReturn(existingUserJson)
 47:         
 48:         // Test sign up with existing email
 49:         repository.signUp("test@example.com", "password").test {
 50:             assertFalse(awaitItem())
 51:             awaitComplete()
 52:         }
 53:     }
 54:     
 55:     @Test
 56:     fun `signIn should return success with valid credentials`() = runTest {
 57:         // Mock settings behavior
 58:         whenever(settings.getStringOrNull("password_test@example.com")).thenReturn("password")
 59:         
 60:         val existingUserJson = """
 61:             [{"id":"1","email":"test@example.com","name":"Test User","preferences":{}}]
 62:         """.trimIndent()
 63:         whenever(settings.getStringOrNull("users")).thenReturn(existingUserJson)
 64:         
 65:         // Test sign in
 66:         repository.signIn("test@example.com", "password").test {
 67:             assertTrue(awaitItem())
 68:             awaitComplete()
 69:         }
 70:     }
 71:     
 72:     @Test
 73:     fun `signIn should return failure with invalid credentials`() = runTest {
 74:         // Mock settings behavior
 75:         whenever(settings.getStringOrNull("password_test@example.com")).thenReturn("correctpassword")
 76:         
 77:         // Test sign in with wrong password
 78:         repository.signIn("test@example.com", "wrongpassword").test {
 79:             assertFalse(awaitItem())
 80:             awaitComplete()
 81:         }
 82:     }
 83:     
 84:     @Test
 85:     fun `signOut should clear current user`() = runTest {
 86:         // Test sign out
 87:         repository.signOut().test {
 88:             assertTrue(awaitItem())
 89:             awaitComplete()
 90:         }
 91:     }
 92:     
 93:     @Test
 94:     fun `isUserSignedIn should return true when user is signed in`() = runTest {
 95:         // Mock settings behavior
 96:         whenever(settings.getStringOrNull("current_user_id")).thenReturn("1")
 97:         
 98:         // Test is user signed in
 99:         repository.isUserSignedIn().test {
100:             assertTrue(awaitItem())
101:             awaitComplete()
102:         }
103:     }
104:     
105:     @Test
106:     fun `isUserSignedIn should return false when no user is signed in`() = runTest {
107:         // Mock settings behavior
108:         whenever(settings.getStringOrNull("current_user_id")).thenReturn(null)
109:         
110:         // Test is user signed in
111:         repository.isUserSignedIn().test {
112:             assertFalse(awaitItem())
113:             awaitComplete()
114:         }
115:     }
116:     
117:     @Test
118:     fun `getCurrentUser should return user when signed in`() = runTest {
119:         // Mock settings behavior
120:         whenever(settings.getStringOrNull("current_user_id")).thenReturn("1")
121:         
122:         val existingUserJson = """
123:             [{"id":"1","email":"test@example.com","name":"Test User","preferences":{}}]
124:         """.trimIndent()
125:         whenever(settings.getStringOrNull("users")).thenReturn(existingUserJson)
126:         
127:         // Test get current user
128:         repository.getCurrentUser().test {
129:             val user = awaitItem()
130:             assertNotNull(user)
131:             assertEquals("1", user!!.id)
132:             assertEquals("test@example.com", user.email)
133:             assertEquals("Test User", user.name)
134:             awaitComplete()
135:         }
136:     }
137:     
138:     @Test
139:     fun `getCurrentUser should return null when not signed in`() = runTest {
140:         // Mock settings behavior
141:         whenever(settings.getStringOrNull("current_user_id")).thenReturn(null)
142:         
143:         // Test get current user
144:         repository.getCurrentUser().test {
145:             assertEquals(null, awaitItem())
146:             awaitComplete()
147:         }
148:     }
149:     
150:     @Test
151:     fun `updateUserName should update user name`() = runTest {
152:         // Mock settings behavior
153:         whenever(settings.getStringOrNull("current_user_id")).thenReturn("1")
154:         
155:         val existingUserJson = """
156:             [{"id":"1","email":"test@example.com","name":"Old Name","preferences":{}}]
157:         """.trimIndent()
158:         whenever(settings.getStringOrNull("users")).thenReturn(existingUserJson)
159:         
160:         // Test update user name
161:         repository.updateUserName("New Name").test {
162:             assertTrue(awaitItem())
163:             awaitComplete()
164:         }
165:     }
166: }
````

## File: androidApp/src/test/java/com/cocktailcraft/di/TestModule.kt
````kotlin
 1: package com.cocktailcraft.di
 2: 
 3: import com.cocktailcraft.data.config.AppConfigImpl
 4: import com.cocktailcraft.domain.config.AppConfig
 5: import com.cocktailcraft.domain.repository.AuthRepository
 6: import com.cocktailcraft.domain.repository.CartRepository
 7: import com.cocktailcraft.domain.repository.CocktailRepository
 8: import com.cocktailcraft.domain.repository.OrderRepository
 9: import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
10: import com.cocktailcraft.domain.usecase.ToggleFavoriteUseCase
11: import com.cocktailcraft.util.NetworkMonitor
12: import kotlinx.coroutines.flow.MutableStateFlow
13: import kotlinx.serialization.json.Json
14: import org.koin.dsl.module
15: import org.mockito.kotlin.mock
16: 
17: /**
18:  * Koin module for testing that provides mock implementations.
19:  * This module can be used in unit tests to provide mock dependencies.
20:  */
21: val testModule = module {
22:     // Mock repositories
23:     single<CocktailRepository> { mock() }
24:     single<CartRepository> { mock() }
25:     single<AuthRepository> { mock() }
26:     single<OrderRepository> { mock() }
27:     
28:     // Mock use cases
29:     factory { PlaceOrderUseCase(orderRepository = get()) }
30:     factory { ToggleFavoriteUseCase(cocktailRepository = get()) }
31:     
32:     // Mock config
33:     single<AppConfig> { AppConfigImpl() }
34:     
35:     // Mock JSON
36:     single {
37:         Json {
38:             ignoreUnknownKeys = true
39:             isLenient = true
40:         }
41:     }
42:     
43:     // Mock NetworkMonitor
44:     single { 
45:         mock<NetworkMonitor>().apply {
46:             val isOnlineFlow = MutableStateFlow(true)
47:             org.mockito.kotlin.whenever(this.isOnline).thenReturn(isOnlineFlow)
48:         }
49:     }
50: }
````

## File: androidApp/src/test/java/com/cocktailcraft/domain/usecase/PlaceOrderUseCaseTest.kt
````kotlin
  1: package com.cocktailcraft.domain.usecase
  2: 
  3: import app.cash.turbine.test
  4: import com.cocktailcraft.domain.model.Cocktail
  5: import com.cocktailcraft.domain.model.CocktailCartItem
  6: import com.cocktailcraft.domain.model.CocktailIngredient
  7: import com.cocktailcraft.domain.model.Order
  8: import com.cocktailcraft.domain.repository.OrderRepository
  9: import com.cocktailcraft.domain.util.Result
 10: import kotlinx.coroutines.ExperimentalCoroutinesApi
 11: import kotlinx.coroutines.flow.Flow
 12: import kotlinx.coroutines.flow.flowOf
 13: import kotlinx.coroutines.test.runTest
 14: import org.junit.Assert.assertEquals
 15: import org.junit.Assert.assertTrue
 16: import org.junit.Before
 17: import org.junit.Test
 18: import org.mockito.Mock
 19: import org.mockito.Mockito.verify
 20: import org.mockito.MockitoAnnotations
 21: 
 22: @OptIn(ExperimentalCoroutinesApi::class)
 23: class PlaceOrderUseCaseTest {
 24:     
 25:     private lateinit var placeOrderUseCase: PlaceOrderUseCase
 26:     
 27:     @Mock
 28:     private lateinit var orderRepository: OrderRepository
 29:     
 30:     @Before
 31:     fun setup() {
 32:         MockitoAnnotations.openMocks(this)
 33:         placeOrderUseCase = PlaceOrderUseCase(orderRepository)
 34:     }
 35:     
 36:     @Test
 37:     fun `invoke should create order and add to repository`() = runTest {
 38:         // Given
 39:         val cartItems = listOf(
 40:             CocktailCartItem(
 41:                 cocktail = Cocktail(
 42:                     id = "cocktail1",
 43:                     name = "Mojito",
 44:                     imageUrl = "mojito.jpg",
 45:                     price = 10.0,
 46:                     ingredients = listOf(
 47:                         CocktailIngredient("Rum", "2 oz"),
 48:                         CocktailIngredient("Mint", "6 leaves"),
 49:                         CocktailIngredient("Lime", "1"),
 50:                         CocktailIngredient("Sugar", "2 tsp"),
 51:                         CocktailIngredient("Soda", "splash")
 52:                     ),
 53:                     instructions = "Muddle mint and lime...",
 54:                     rating = 4.5f,
 55:                     category = "Classic"
 56:                 ),
 57:                 quantity = 2
 58:             ),
 59:             CocktailCartItem(
 60:                 cocktail = Cocktail(
 61:                     id = "cocktail2",
 62:                     name = "Margarita",
 63:                     imageUrl = "margarita.jpg",
 64:                     price = 12.0,
 65:                     ingredients = listOf(
 66:                         CocktailIngredient("Tequila", "2 oz"),
 67:                         CocktailIngredient("Triple Sec", "1 oz"),
 68:                         CocktailIngredient("Lime Juice", "1 oz"),
 69:                         CocktailIngredient("Salt", "pinch")
 70:                     ),
 71:                     instructions = "Shake with ice...",
 72:                     rating = 4.7f,
 73:                     category = "Classic"
 74:                 ),
 75:                 quantity = 1
 76:             )
 77:         )
 78:         val totalPrice = 32.0 // (10.0 * 2) + (12.0 * 1)
 79:         
 80:         // When & Then
 81:         placeOrderUseCase(cartItems, totalPrice).test {
 82:             val result = awaitItem()
 83:             assertTrue(result is Result.Success)
 84:             
 85:             val order = (result as Result.Success).data
 86:             
 87:             // Verify order properties
 88:             assertTrue(order.id.startsWith("ORD-"))
 89:             assertEquals(2, order.items.size)
 90:             assertEquals("Mojito", order.items[0].name)
 91:             assertEquals(2, order.items[0].quantity)
 92:             assertEquals(10.0, order.items[0].price, 0.01)
 93:             assertEquals("Margarita", order.items[1].name)
 94:             assertEquals(1, order.items[1].quantity)
 95:             assertEquals(12.0, order.items[1].price, 0.01)
 96:             assertEquals(totalPrice, order.total, 0.01)
 97:             assertEquals("Processing", order.status)
 98:             
 99:             // Verify repository was called
100:             verify(orderRepository).addOrder(order)
101:             
102:             awaitComplete()
103:         }
104:     }
105:     
106:     // Create a test implementation of OrderRepository that throws exceptions
107:     private class ExceptionThrowingOrderRepository : OrderRepository {
108:         override suspend fun addOrder(order: Order) {
109:             throw RuntimeException("Network error")
110:         }
111:         
112:         // Implement other methods with default behavior
113:         override suspend fun getOrderHistory(): Flow<List<Order>> = flowOf(emptyList())
114:         override suspend fun getOrderById(orderId: String): Flow<Order?> = flowOf(null)
115:         override suspend fun updateOrderStatus(orderId: String, status: String) {}
116:         override suspend fun cancelOrder(orderId: String): Flow<Boolean> = flowOf(false)
117:         override suspend fun placeOrder(order: Order): Flow<Boolean> = flowOf(false)
118:         override suspend fun deleteOrder(id: String) {}
119:         override suspend fun getOrders(): Flow<List<Order>> = flowOf(emptyList())
120:     }
121:     
122:     @Test
123:     fun `invoke should return error result when repository throws exception`() = runTest {
124:         // Given - Create a use case with a repository that throws exceptions
125:         val exceptionThrowingRepo = ExceptionThrowingOrderRepository()
126:         val useCase = PlaceOrderUseCase(exceptionThrowingRepo)
127:         
128:         val cartItems = listOf(
129:             CocktailCartItem(
130:                 cocktail = Cocktail(
131:                     id = "cocktail1",
132:                     name = "Mojito",
133:                     imageUrl = "mojito.jpg",
134:                     price = 10.0,
135:                     ingredients = listOf(
136:                         CocktailIngredient("Rum", "2 oz"),
137:                         CocktailIngredient("Mint", "6 leaves"),
138:                         CocktailIngredient("Lime", "1"),
139:                         CocktailIngredient("Sugar", "2 tsp"),
140:                         CocktailIngredient("Soda", "splash")
141:                     ),
142:                     instructions = "Muddle mint and lime...",
143:                     rating = 4.5f,
144:                     category = "Classic"
145:                 ),
146:                 quantity = 2
147:             )
148:         )
149:         val totalPrice = 20.0
150:         
151:         // When & Then
152:         useCase(cartItems, totalPrice).test {
153:             val result = awaitItem()
154:             assertTrue(result is Result.Error)
155:             assertEquals("Network error", (result as Result.Error).message)
156:             awaitComplete()
157:         }
158:     }
159: }
````

## File: androidApp/src/test/java/com/cocktailcraft/navigation/NavigationManagerTest.kt
````kotlin
  1: package com.cocktailcraft.navigation
  2: 
  3: import androidx.navigation.NavController
  4: import androidx.navigation.NavOptions
  5: import androidx.navigation.NavOptionsBuilder
  6: import com.cocktailcraft.domain.model.Cocktail
  7: import com.cocktailcraft.domain.model.CocktailIngredient
  8: import org.junit.Before
  9: import org.junit.Test
 10: import org.mockito.kotlin.any
 11: import org.mockito.kotlin.eq
 12: import org.mockito.kotlin.mock
 13: import org.mockito.kotlin.verify
 14: import org.mockito.kotlin.isNull
 15: 
 16: class NavigationManagerTest {
 17:     
 18:     private lateinit var navigationManager: NavigationManager
 19:     private val navController: NavController = mock()
 20:     
 21:     @Before
 22:     fun setup() {
 23:         navigationManager = NavigationManager(navController)
 24:     }
 25:     
 26:     @Test
 27:     fun `navigateToHome should navigate to home route`() {
 28:         navigationManager.navigateToHome()
 29:         verify(navController).navigate(
 30:             eq(Screen.Home.route),
 31:             any<(NavOptionsBuilder) -> Unit>()
 32:         )
 33:     }
 34:     
 35:     @Test
 36:     fun `navigateToCart should navigate to cart route`() {
 37:         navigationManager.navigateToCart()
 38:         verify(navController).navigate(eq(Screen.Cart.route), isNull(), isNull())
 39:     }
 40:     
 41:     @Test
 42:     fun `navigateToOrderList should navigate to order list route`() {
 43:         navigationManager.navigateToOrderList()
 44:         verify(navController).navigate(
 45:             eq(Screen.OrderList.route),
 46:             any<(NavOptionsBuilder) -> Unit>()
 47:         )
 48:     }
 49:     
 50:     @Test
 51:     fun `navigateToProfile should navigate to profile route`() {
 52:         navigationManager.navigateToProfile()
 53:         verify(navController).navigate(
 54:             eq(Screen.Profile.route),
 55:             any<(NavOptionsBuilder) -> Unit>()
 56:         )
 57:     }
 58:     
 59:     @Test
 60:     fun `navigateToFavorites should navigate to favorites route`() {
 61:         navigationManager.navigateToFavorites()
 62:         verify(navController).navigate(
 63:             eq(Screen.Favorites.route),
 64:             any<(NavOptionsBuilder) -> Unit>()
 65:         )
 66:     }
 67:     
 68:     @Test
 69:     fun `navigateToCocktailDetail should navigate to cocktail detail route`() {
 70:         val ingredients = listOf(
 71:             CocktailIngredient("Ingredient 1", "1 oz"),
 72:             CocktailIngredient("Ingredient 2", "2 oz")
 73:         )
 74:         
 75:         val cocktail = Cocktail(
 76:             id = "1",
 77:             name = "Test Cocktail",
 78:             instructions = "Step 1, Step 2",
 79:             imageUrl = "test.jpg",
 80:             price = 9.99,
 81:             ingredients = ingredients,
 82:             rating = 4.5f,
 83:             category = "Test Category"
 84:         )
 85:         
 86:         navigationManager.navigateToCocktailDetail(cocktail)
 87:         verify(navController).navigate(eq("cocktail_detail/1"), isNull(), isNull())
 88:     }
 89:     
 90:     @Test
 91:     fun `navigateToReviews should navigate to reviews route`() {
 92:         val cocktailId = "1"
 93:         navigationManager.navigateToReviews(cocktailId)
 94:         verify(navController).navigate(eq("reviews/1"), isNull(), isNull())
 95:     }
 96:     
 97:     @Test
 98:     fun `navigateBack should pop back stack`() {
 99:         navigationManager.navigateBack()
100:         verify(navController).popBackStack()
101:     }
102:     
103:     @Test
104:     fun `navigateToBottomNavDestination should navigate to destination route`() {
105:         navigationManager.navigateToBottomNavDestination(Screen.Profile)
106:         verify(navController).navigate(
107:             eq(Screen.Profile.route),
108:             any<(NavOptionsBuilder) -> Unit>()
109:         )
110:     }
111: }
````

## File: androidApp/src/test/java/com/cocktailcraft/util/ErrorUtilsTest.kt
````kotlin
  1: package com.cocktailcraft.util
  2: 
  3: import com.cocktailcraft.domain.util.ErrorCode
  4: import org.junit.Test
  5: import kotlin.test.assertEquals
  6: import kotlin.test.assertNotNull
  7: import kotlin.test.assertNull
  8: 
  9: class ErrorUtilsTest {
 10: 
 11:     @Test
 12:     fun `createUserFriendlyError should create error with all parameters`() {
 13:         // Given
 14:         val title = "Test Error"
 15:         val message = "This is a test error message"
 16:         val category = ErrorUtils.ErrorCategory.NETWORK
 17:         val recoveryAction = ErrorUtils.RecoveryAction("Retry") { /* no-op */ }
 18:         
 19:         // When
 20:         val error = ErrorUtils.createUserFriendlyError(
 21:             title = title,
 22:             message = message,
 23:             category = category,
 24:             recoveryAction = recoveryAction
 25:         )
 26:         
 27:         // Then
 28:         assertNotNull(error)
 29:         assertEquals(title, error.title)
 30:         assertEquals(message, error.message)
 31:         assertEquals(category, error.category)
 32:         assertEquals(recoveryAction.actionLabel, error.recoveryAction?.actionLabel)
 33:     }
 34:     
 35:     @Test
 36:     fun `createUserFriendlyError should create error with minimal parameters`() {
 37:         // Given
 38:         val title = "Minimal Error"
 39:         val message = "Minimal error message"
 40:         
 41:         // When
 42:         val error = ErrorUtils.createUserFriendlyError(
 43:             title = title,
 44:             message = message
 45:         )
 46:         
 47:         // Then
 48:         assertNotNull(error)
 49:         assertEquals(title, error.title)
 50:         assertEquals(message, error.message)
 51:         assertEquals(ErrorUtils.ErrorCategory.UNKNOWN, error.category)
 52:         assertNull(error.recoveryAction)
 53:     }
 54:     
 55:     @Test
 56:     fun `createNetworkError should create network error`() {
 57:         // When
 58:         val error = ErrorUtils.createNetworkError()
 59:         
 60:         // Then
 61:         assertNotNull(error)
 62:         assertEquals("Network Error", error.title)
 63:         assertEquals("Unable to connect to the server. Please check your internet connection and try again.", error.message)
 64:         assertEquals(ErrorUtils.ErrorCategory.NETWORK, error.category)
 65:         assertNotNull(error.recoveryAction)
 66:         assertEquals("Retry", error.recoveryAction?.actionLabel)
 67:     }
 68:     
 69:     @Test
 70:     fun `createServerError should create server error`() {
 71:         // When
 72:         val error = ErrorUtils.createServerError()
 73:         
 74:         // Then
 75:         assertNotNull(error)
 76:         assertEquals("Server Error", error.title)
 77:         assertEquals("The server encountered an error. Please try again later.", error.message)
 78:         assertEquals(ErrorUtils.ErrorCategory.SERVER, error.category)
 79:         assertNotNull(error.recoveryAction)
 80:         assertEquals("Retry", error.recoveryAction?.actionLabel)
 81:     }
 82:     
 83:     @Test
 84:     fun `createErrorFromException should handle network exception`() {
 85:         // Given
 86:         val exception = Exception("Failed to connect to server")
 87:         
 88:         // When
 89:         val error = ErrorUtils.createErrorFromException(
 90:             exception = exception,
 91:             defaultTitle = "Default Error",
 92:             defaultMessage = "An error occurred"
 93:         )
 94:         
 95:         // Then
 96:         assertNotNull(error)
 97:         assertEquals("Network Error", error.title)
 98:         assertEquals("Unable to connect to the server. Please check your internet connection and try again.", error.message)
 99:         assertEquals(ErrorUtils.ErrorCategory.NETWORK, error.category)
100:     }
101:     
102:     @Test
103:     fun `createErrorFromException should handle timeout exception`() {
104:         // Given
105:         val exception = Exception("timeout")
106:         
107:         // When
108:         val error = ErrorUtils.createErrorFromException(
109:             exception = exception,
110:             defaultTitle = "Default Error",
111:             defaultMessage = "An error occurred"
112:         )
113:         
114:         // Then
115:         assertNotNull(error)
116:         assertEquals("Network Error", error.title)
117:         assertEquals("The request timed out. Please try again.", error.message)
118:         assertEquals(ErrorUtils.ErrorCategory.NETWORK, error.category)
119:     }
120:     
121:     @Test
122:     fun `createErrorFromException should use default for unknown exception`() {
123:         // Given
124:         val exception = Exception("Some unknown error")
125:         
126:         // When
127:         val error = ErrorUtils.createErrorFromException(
128:             exception = exception,
129:             defaultTitle = "Default Error",
130:             defaultMessage = "An error occurred"
131:         )
132:         
133:         // Then
134:         assertNotNull(error)
135:         assertEquals("Default Error", error.title)
136:         assertEquals("An error occurred: Some unknown error", error.message)
137:         assertEquals(ErrorUtils.ErrorCategory.UNKNOWN, error.category)
138:     }
139:     
140:     @Test
141:     fun `createErrorFromErrorCode should handle network error code`() {
142:         // When
143:         val error = ErrorUtils.createErrorFromErrorCode(
144:             errorCode = ErrorCode.NETWORK,
145:             defaultTitle = "Default Error",
146:             defaultMessage = "An error occurred"
147:         )
148:         
149:         // Then
150:         assertNotNull(error)
151:         assertEquals("Network Error", error.title)
152:         assertEquals("Unable to connect to the server. Please check your internet connection and try again.", error.message)
153:         assertEquals(ErrorUtils.ErrorCategory.NETWORK, error.category)
154:     }
155:     
156:     @Test
157:     fun `createErrorFromErrorCode should handle timeout error code`() {
158:         // When
159:         val error = ErrorUtils.createErrorFromErrorCode(
160:             errorCode = ErrorCode.TIMEOUT,
161:             defaultTitle = "Default Error",
162:             defaultMessage = "An error occurred"
163:         )
164:         
165:         // Then
166:         assertNotNull(error)
167:         assertEquals("Network Error", error.title)
168:         assertEquals("The request timed out. Please try again.", error.message)
169:         assertEquals(ErrorUtils.ErrorCategory.NETWORK, error.category)
170:     }
171:     
172:     @Test
173:     fun `createErrorFromErrorCode should handle unauthorized error code`() {
174:         // When
175:         val error = ErrorUtils.createErrorFromErrorCode(
176:             errorCode = ErrorCode.UNAUTHORIZED,
177:             defaultTitle = "Default Error",
178:             defaultMessage = "An error occurred"
179:         )
180:         
181:         // Then
182:         assertNotNull(error)
183:         assertEquals("Authentication Error", error.title)
184:         assertEquals("You are not authorized to perform this action. Please sign in and try again.", error.message)
185:         assertEquals(ErrorUtils.ErrorCategory.AUTHENTICATION, error.category)
186:     }
187:     
188:     @Test
189:     fun `createErrorFromErrorCode should handle invalid data error code`() {
190:         // When
191:         val error = ErrorUtils.createErrorFromErrorCode(
192:             errorCode = ErrorCode.INVALID_DATA,
193:             defaultTitle = "Default Error",
194:             defaultMessage = "An error occurred"
195:         )
196:         
197:         // Then
198:         assertNotNull(error)
199:         assertEquals("Data Error", error.title)
200:         assertEquals("The data provided is invalid. Please check your input and try again.", error.message)
201:         assertEquals(ErrorUtils.ErrorCategory.DATA, error.category)
202:     }
203:     
204:     @Test
205:     fun `createErrorFromErrorCode should use default for unknown error code`() {
206:         // When
207:         val error = ErrorUtils.createErrorFromErrorCode(
208:             errorCode = ErrorCode.UNKNOWN,
209:             defaultTitle = "Default Error",
210:             defaultMessage = "An error occurred"
211:         )
212:         
213:         // Then
214:         assertNotNull(error)
215:         assertEquals("Default Error", error.title)
216:         assertEquals("An error occurred", error.message)
217:         assertEquals(ErrorUtils.ErrorCategory.UNKNOWN, error.category)
218:     }
219: }
````

## File: androidApp/src/test/java/com/cocktailcraft/viewmodel/BaseViewModelTest.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import com.cocktailcraft.util.ErrorUtils
  4: import kotlinx.coroutines.Dispatchers
  5: import kotlinx.coroutines.ExperimentalCoroutinesApi
  6: import kotlinx.coroutines.test.StandardTestDispatcher
  7: import kotlinx.coroutines.test.resetMain
  8: import kotlinx.coroutines.test.runTest
  9: import kotlinx.coroutines.test.setMain
 10: import org.junit.After
 11: import org.junit.Before
 12: import org.junit.Test
 13: import kotlin.test.assertEquals
 14: import kotlin.test.assertFalse
 15: import kotlin.test.assertNotNull
 16: import kotlin.test.assertNull
 17: import kotlin.test.assertTrue
 18: 
 19: @ExperimentalCoroutinesApi
 20: class BaseViewModelTest {
 21: 
 22:     private val testDispatcher = StandardTestDispatcher()
 23: 
 24:     private lateinit var viewModel: TestBaseViewModel
 25: 
 26:     @Before
 27:     fun setup() {
 28:         Dispatchers.setMain(testDispatcher)
 29:         viewModel = TestBaseViewModel()
 30:     }
 31: 
 32:     @After
 33:     fun tearDown() {
 34:         Dispatchers.resetMain()
 35:     }
 36: 
 37:     @Test
 38:     fun `initial state should have no error and not loading`() = runTest {
 39:         // Then
 40:         assertFalse(viewModel.isLoading.value)
 41:         assertNull(viewModel.error.value)
 42:     }
 43: 
 44:     @Test
 45:     fun `setLoading should update loading state`() = runTest {
 46:         // When
 47:         viewModel.testSetLoading(true)
 48: 
 49:         // Then
 50:         assertTrue(viewModel.isLoading.value)
 51: 
 52:         // When
 53:         viewModel.testSetLoading(false)
 54: 
 55:         // Then
 56:         assertFalse(viewModel.isLoading.value)
 57:     }
 58: 
 59:     @Test
 60:     fun `setError should update error state`() = runTest {
 61:         // Given
 62:         val title = "Test Error"
 63:         val message = "This is a test error message"
 64:         val category = ErrorUtils.ErrorCategory.NETWORK
 65:         val recoveryAction = ErrorUtils.RecoveryAction("Retry") { /* no-op */ }
 66: 
 67:         // When
 68:         viewModel.testSetError(title, message, category, false, recoveryAction)
 69: 
 70:         // Then
 71:         assertNotNull(viewModel.error.value)
 72:         assertEquals(title, viewModel.error.value?.title)
 73:         assertEquals(message, viewModel.error.value?.message)
 74:         assertEquals(category, viewModel.error.value?.category)
 75:         assertEquals(recoveryAction.actionLabel, viewModel.error.value?.recoveryAction?.actionLabel)
 76:     }
 77: 
 78:     @Test
 79:     fun `clearError should reset error state`() = runTest {
 80:         // Given
 81:         viewModel.testSetError(
 82:             title = "Test Error",
 83:             message = "This is a test error message",
 84:             category = ErrorUtils.ErrorCategory.NETWORK,
 85:             showAsEvent = false,
 86:             recoveryAction = null
 87:         )
 88: 
 89:         // When
 90:         viewModel.clearError()
 91: 
 92:         // Then
 93:         assertNull(viewModel.error.value)
 94:     }
 95: 
 96:     @Test
 97:     fun `setError with minimal parameters should set defaults`() = runTest {
 98:         // When
 99:         viewModel.testSetError(
100:             title = "Minimal Error",
101:             message = "Minimal error message",
102:             category = ErrorUtils.ErrorCategory.UNKNOWN,
103:             showAsEvent = false,
104:             recoveryAction = null
105:         )
106: 
107:         // Then
108:         assertNotNull(viewModel.error.value)
109:         assertEquals("Minimal Error", viewModel.error.value?.title)
110:         assertEquals("Minimal error message", viewModel.error.value?.message)
111:         assertEquals(ErrorUtils.ErrorCategory.UNKNOWN, viewModel.error.value?.category)
112:         assertNull(viewModel.error.value?.recoveryAction)
113:     }
114: 
115:     // Test implementation of BaseViewModel for testing
116:     private class TestBaseViewModel : BaseViewModel() {
117:         // Expose protected methods for testing
118:         fun testSetLoading(isLoading: Boolean) {
119:             setLoading(isLoading)
120:         }
121: 
122:         fun testSetError(
123:             title: String,
124:             message: String,
125:             category: ErrorUtils.ErrorCategory = ErrorUtils.ErrorCategory.UNKNOWN,
126:             showAsEvent: Boolean = false,
127:             recoveryAction: ErrorUtils.RecoveryAction? = null
128:         ) {
129:             setError(title, message, category, showAsEvent, recoveryAction)
130:         }
131: 
132:         fun testHandleException(
133:             exception: Throwable,
134:             defaultMessage: String = "Something went wrong. Please try again.",
135:             showAsEvent: Boolean = false,
136:             recoveryAction: ErrorUtils.RecoveryAction? = null
137:         ) {
138:             handleException(exception, defaultMessage, showAsEvent, recoveryAction)
139:         }
140: 
141:         fun <T> testExecuteWithErrorHandling(
142:             operation: suspend () -> T,
143:             onSuccess: (T) -> Unit,
144:             onError: ((ErrorUtils.UserFriendlyError) -> Unit)? = null,
145:             defaultErrorMessage: String = "Something went wrong. Please try again.",
146:             showAsEvent: Boolean = false,
147:             showLoading: Boolean = true,
148:             recoveryAction: ErrorUtils.RecoveryAction? = null
149:         ) {
150:             executeWithErrorHandling(
151:                 operation,
152:                 onSuccess,
153:                 onError,
154:                 defaultErrorMessage,
155:                 showAsEvent,
156:                 showLoading,
157:                 recoveryAction
158:             )
159:         }
160:     }
161: }
````

## File: androidApp/src/test/java/com/cocktailcraft/viewmodel/CartViewModelTest.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import app.cash.turbine.test
  4: import com.cocktailcraft.domain.model.Cocktail
  5: import com.cocktailcraft.domain.model.CocktailCartItem
  6: import com.cocktailcraft.domain.model.CocktailIngredient
  7: import com.cocktailcraft.domain.repository.CartRepository
  8: import kotlinx.coroutines.Dispatchers
  9: import kotlinx.coroutines.ExperimentalCoroutinesApi
 10: import kotlinx.coroutines.flow.flowOf
 11: import kotlinx.coroutines.test.StandardTestDispatcher
 12: import kotlinx.coroutines.test.resetMain
 13: import kotlinx.coroutines.test.runTest
 14: import kotlinx.coroutines.test.setMain
 15: import org.junit.After
 16: import org.junit.Before
 17: import org.junit.Test
 18: import org.mockito.Mockito
 19: import org.mockito.kotlin.mock
 20: import org.mockito.kotlin.times
 21: import org.mockito.kotlin.whenever
 22: import kotlin.test.assertEquals
 23: import kotlin.test.assertNull
 24: import kotlin.time.Duration.Companion.seconds
 25: 
 26: @OptIn(ExperimentalCoroutinesApi::class)
 27: class CartViewModelTest {
 28:     
 29:     private lateinit var viewModel: CartViewModel
 30:     private val cartRepository: CartRepository = mock()
 31:     private val testDispatcher = StandardTestDispatcher()
 32:     
 33:     // Sample test data
 34:     private val testCocktail = Cocktail(
 35:         id = "1",
 36:         name = "Mojito",
 37:         instructions = "Mix ingredients and serve with ice",
 38:         imageUrl = "mojito.jpg",
 39:         price = 9.99,
 40:         ingredients = listOf(
 41:             CocktailIngredient("Rum", "2 oz"),
 42:             CocktailIngredient("Mint", "6 leaves")
 43:         ),
 44:         rating = 4.5f,
 45:         category = "Classic"
 46:     )
 47:     
 48:     private val testCartItem = CocktailCartItem(testCocktail, 2)
 49:     private val testCartItems = listOf(testCartItem)
 50:     private val testCartTotal = 19.98 // 9.99 * 2
 51:     
 52:     @Before
 53:     fun setup() = runTest {
 54:         Dispatchers.setMain(testDispatcher)
 55:         
 56:         // Mock repository responses
 57:         whenever(cartRepository.getCartItems()).thenReturn(flowOf(testCartItems))
 58:         whenever(cartRepository.getCartTotal()).thenReturn(flowOf(testCartTotal))
 59:         
 60:         // Create view model with mocked repository
 61:         viewModel = CartViewModel(cartRepository)
 62:     }
 63:     
 64:     @After
 65:     fun tearDown() {
 66:         Dispatchers.resetMain()
 67:     }
 68:     
 69:     @Test
 70:     fun `initial state should load cart items and total`() = runTest {
 71:         // Advance the dispatcher to allow coroutines to complete
 72:         testDispatcher.scheduler.advanceUntilIdle()
 73:         
 74:         // Verify cart items are loaded
 75:         viewModel.cartItems.test(timeout = 5.seconds) {
 76:             val items = awaitItem()
 77:             assertEquals(1, items.size)
 78:             assertEquals("Mojito", items[0].cocktail.name)
 79:             assertEquals(2, items[0].quantity)
 80:             cancelAndIgnoreRemainingEvents()
 81:         }
 82:         
 83:         // Verify total price is calculated
 84:         viewModel.totalPrice.test(timeout = 5.seconds) {
 85:             assertEquals(19.98, awaitItem(), 0.01)
 86:             cancelAndIgnoreRemainingEvents()
 87:         }
 88:         
 89:         // Verify loading state is false after initialization
 90:         viewModel.isLoading.test {
 91:             assertEquals(false, awaitItem())
 92:             cancelAndIgnoreRemainingEvents()
 93:         }
 94:         
 95:         // Verify no errors
 96:         viewModel.error.test {
 97:             assertNull(awaitItem())
 98:             cancelAndIgnoreRemainingEvents()
 99:         }
100:     }
101:     
102:     @Test
103:     fun `addToCart should add item to cart`() = runTest {
104:         // Setup
105:         val newCocktail = testCocktail.copy(id = "2", name = "Margarita")
106:         
107:         // Execute
108:         viewModel.addToCart(newCocktail, 1)
109:         
110:         // Advance the dispatcher to allow coroutines to complete
111:         testDispatcher.scheduler.advanceUntilIdle()
112:         
113:         // Verify repository method was called with correct parameters
114:         Mockito.verify(cartRepository).addToCart(CocktailCartItem(newCocktail, 1))
115:         
116:         // Verify loadCartItems was called to refresh the cart
117:         // The method is called once during init and once after addToCart
118:         Mockito.verify(cartRepository, times(2)).getCartItems()
119:         Mockito.verify(cartRepository, times(2)).getCartTotal()
120:     }
121:     
122:     @Test
123:     fun `removeFromCart should remove item from cart`() = runTest {
124:         // Execute
125:         viewModel.removeFromCart("1")
126:         
127:         // Advance the dispatcher to allow coroutines to complete
128:         testDispatcher.scheduler.advanceUntilIdle()
129:         
130:         // Verify repository method was called with correct parameters
131:         Mockito.verify(cartRepository).removeFromCart("1")
132:         
133:         // Verify loadCartItems was called to refresh the cart
134:         // The method is called once during init and once after removeFromCart
135:         Mockito.verify(cartRepository, times(2)).getCartItems()
136:         Mockito.verify(cartRepository, times(2)).getCartTotal()
137:     }
138:     
139:     @Test
140:     fun `updateQuantity should update item quantity in cart`() = runTest {
141:         // Execute
142:         viewModel.updateQuantity("1", 3)
143:         
144:         // Advance the dispatcher to allow coroutines to complete
145:         testDispatcher.scheduler.advanceUntilIdle()
146:         
147:         // Verify repository method was called with correct parameters
148:         Mockito.verify(cartRepository).updateQuantity("1", 3)
149:         
150:         // Verify loadCartItems was called to refresh the cart
151:         // The method is called once during init and once after updateQuantity
152:         Mockito.verify(cartRepository, times(2)).getCartItems()
153:         Mockito.verify(cartRepository, times(2)).getCartTotal()
154:     }
155:     
156:     @Test
157:     fun `clearCart should clear all items from cart`() = runTest {
158:         // Execute
159:         viewModel.clearCart()
160:         
161:         // Advance the dispatcher to allow coroutines to complete
162:         testDispatcher.scheduler.advanceUntilIdle()
163:         
164:         // Verify repository method was called
165:         Mockito.verify(cartRepository).clearCart()
166:         
167:         // Verify loadCartItems was called to refresh the cart
168:         // The method is called once during init and once after clearCart
169:         Mockito.verify(cartRepository, times(2)).getCartItems()
170:         Mockito.verify(cartRepository, times(2)).getCartTotal()
171:     }
172:     
173:     @Test
174:     fun `error handling should set error state`() = runTest {
175:         // Setup - mock repository to throw exception
176:         val errorMessage = "Network error"
177:         whenever(cartRepository.getCartItems()).thenThrow(RuntimeException(errorMessage))
178:         
179:         // Execute
180:         viewModel.loadCartItems()
181:         
182:         // Advance the dispatcher to allow coroutines to complete
183:         testDispatcher.scheduler.advanceUntilIdle()
184:         
185:         // Verify error state is set
186:         viewModel.error.test {
187:             val error = awaitItem()
188:             assertEquals("Failed to load cart: $errorMessage", error)
189:             cancelAndIgnoreRemainingEvents()
190:         }
191:         
192:         // Verify loading state is false after error
193:         viewModel.isLoading.test {
194:             assertEquals(false, awaitItem())
195:             cancelAndIgnoreRemainingEvents()
196:         }
197:     }
198: }
````

## File: androidApp/src/test/java/com/cocktailcraft/viewmodel/HomeViewModelTest.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import com.cocktailcraft.domain.model.Cocktail
  4: import com.cocktailcraft.domain.model.CocktailIngredient
  5: import com.cocktailcraft.domain.repository.CocktailRepository
  6: import com.cocktailcraft.util.ErrorUtils
  7: import com.cocktailcraft.util.NetworkMonitor
  8: import kotlinx.coroutines.Dispatchers
  9: import kotlinx.coroutines.ExperimentalCoroutinesApi
 10: import kotlinx.coroutines.flow.MutableStateFlow
 11: import kotlinx.coroutines.flow.StateFlow
 12: import kotlinx.coroutines.test.StandardTestDispatcher
 13: import kotlinx.coroutines.test.resetMain
 14: import kotlinx.coroutines.test.runTest
 15: import kotlinx.coroutines.test.setMain
 16: import org.junit.After
 17: import org.junit.Before
 18: import org.junit.Test
 19: import org.mockito.Mock
 20: import org.mockito.Mockito.`when`
 21: import org.mockito.Mockito.verify
 22: import org.mockito.Mockito.doAnswer
 23: import org.mockito.MockitoAnnotations
 24: import kotlin.test.assertEquals
 25: import kotlin.test.assertFalse
 26: import kotlin.test.assertNotNull
 27: import kotlin.test.assertNull
 28: import kotlin.test.assertTrue
 29: 
 30: @ExperimentalCoroutinesApi
 31: class HomeViewModelTest {
 32: 
 33:     private val testDispatcher = StandardTestDispatcher()
 34: 
 35:     @Mock
 36:     private lateinit var cocktailRepository: CocktailRepository
 37: 
 38:     @Mock
 39:     private lateinit var networkMonitor: NetworkMonitor
 40: 
 41:     private lateinit var viewModel: HomeViewModel
 42: 
 43:     private val sampleCocktails = listOf(
 44:         Cocktail(
 45:             id = "1",
 46:             name = "Mojito",
 47:             imageUrl = "https://example.com/mojito.jpg",
 48:             ingredients = listOf(
 49:                 CocktailIngredient("Rum", "2 oz"),
 50:                 CocktailIngredient("Mint", "6 leaves"),
 51:                 CocktailIngredient("Lime", "1"),
 52:                 CocktailIngredient("Sugar", "2 tsp"),
 53:                 CocktailIngredient("Soda", "Top")
 54:             ),
 55:             instructions = "Muddle mint and sugar, add rum and lime, top with soda",
 56:             category = "Refreshing",
 57:             price = 8.99,
 58:             rating = 4.5f,
 59:             popularity = 95,
 60:             alcoholic = "Alcoholic"
 61:         ),
 62:         Cocktail(
 63:             id = "2",
 64:             name = "Margarita",
 65:             imageUrl = "https://example.com/margarita.jpg",
 66:             ingredients = listOf(
 67:                 CocktailIngredient("Tequila", "2 oz"),
 68:                 CocktailIngredient("Triple Sec", "1 oz"),
 69:                 CocktailIngredient("Lime Juice", "1 oz"),
 70:                 CocktailIngredient("Salt", "Rim")
 71:             ),
 72:             instructions = "Shake ingredients with ice, strain into salt-rimmed glass",
 73:             category = "Classic",
 74:             price = 9.99,
 75:             rating = 4.7f,
 76:             popularity = 98,
 77:             alcoholic = "Alcoholic"
 78:         )
 79:     )
 80: 
 81:     @Before
 82:     fun setup() {
 83:         MockitoAnnotations.openMocks(this)
 84:         Dispatchers.setMain(testDispatcher)
 85: 
 86:         // Setup default mocks
 87:         val onlineFlow = MutableStateFlow(true)
 88:         `when`(networkMonitor.isOnline).thenReturn(onlineFlow)
 89: 
 90:         // Since getCocktailsSortedByNewest is a suspend function, we need to handle it differently
 91:         // We'll mock it in the specific test cases
 92: 
 93:         viewModel = HomeViewModel(cocktailRepository)
 94:     }
 95: 
 96:     @After
 97:     fun tearDown() {
 98:         Dispatchers.resetMain()
 99:     }
100: 
101:     @Test
102:     fun `initial state should have empty cocktails and no error`() = runTest {
103:         // Initial state before loading
104:         assertTrue(viewModel.cocktails.value.isEmpty())
105:         assertFalse(viewModel.isLoading.value)
106:         assertEquals("", viewModel.errorString.value)
107:         assertNull(viewModel.error.value)
108:     }
109: 
110:     @Test
111:     fun `loadCocktails should update cocktails state`() = runTest {
112:         // Given
113:         doAnswer {
114:             kotlinx.coroutines.flow.flow { emit(sampleCocktails) }
115:         }.`when`(cocktailRepository).getCocktailsSortedByNewest()
116: 
117:         // When
118:         viewModel.loadCocktails()
119:         testDispatcher.scheduler.advanceUntilIdle()
120: 
121:         // Then
122:         assertEquals(sampleCocktails, viewModel.cocktails.value)
123:         assertFalse(viewModel.isLoading.value)
124:         assertEquals("", viewModel.errorString.value)
125:         assertNull(viewModel.error.value)
126:     }
127: 
128:     @Test
129:     fun `loadCocktails should handle network error`() = runTest {
130:         // Given
131:         val errorMessage = "Network error"
132:         doAnswer {
133:             throw RuntimeException(errorMessage)
134:         }.`when`(cocktailRepository).getCocktailsSortedByNewest()
135: 
136:         // When
137:         viewModel.loadCocktails()
138:         testDispatcher.scheduler.advanceUntilIdle()
139: 
140:         // Then
141:         assertTrue(viewModel.cocktails.value.isEmpty())
142:         assertFalse(viewModel.isLoading.value)
143:         assertTrue(viewModel.errorString.value.isNotEmpty())
144:         assertNotNull(viewModel.error.value)
145:         assertEquals(ErrorUtils.ErrorCategory.UNKNOWN, viewModel.error.value?.category)
146:     }
147: 
148:     @Test
149:     fun `loadCocktails should handle offline mode`() = runTest {
150:         // Given
151:         val offlineFlow = MutableStateFlow(false)
152:         `when`(networkMonitor.isOnline).thenReturn(offlineFlow)
153: 
154:         // Since these are suspend functions, we need to use doAnswer to handle them
155:         doAnswer {
156:             kotlinx.coroutines.flow.flow { emit(sampleCocktails) }
157:         }.`when`(cocktailRepository).getRecentlyViewedCocktails()
158: 
159:         // When
160:         viewModel.loadCocktails()
161:         testDispatcher.scheduler.advanceUntilIdle()
162: 
163:         // Then
164:         assertEquals(sampleCocktails, viewModel.cocktails.value)
165:         assertTrue(viewModel.isOfflineMode.value)
166:         assertFalse(viewModel.isLoading.value)
167:     }
168: 
169:     @Test
170:     fun `retry should reload cocktails`() = runTest {
171:         // Given
172:         val errorMessage = "Network error"
173: 
174:         // First call throws an exception
175:         doAnswer {
176:             throw RuntimeException(errorMessage)
177:         }.`when`(cocktailRepository).getCocktailsSortedByNewest()
178: 
179:         // When - first load fails
180:         viewModel.loadCocktails()
181:         testDispatcher.scheduler.advanceUntilIdle()
182: 
183:         // Then - error state
184:         assertTrue(viewModel.errorString.value.isNotEmpty())
185:         assertNotNull(viewModel.error.value)
186: 
187:         // Setup for retry - second call succeeds
188:         doAnswer {
189:             kotlinx.coroutines.flow.flow { emit(sampleCocktails) }
190:         }.`when`(cocktailRepository).getCocktailsSortedByNewest()
191: 
192:         // When - retry succeeds
193:         viewModel.retry()
194:         testDispatcher.scheduler.advanceUntilIdle()
195: 
196:         // Then - success state
197:         assertEquals(sampleCocktails, viewModel.cocktails.value)
198:         assertEquals("", viewModel.errorString.value)
199:         assertNull(viewModel.error.value)
200:     }
201: 
202:     @Test
203:     fun `clearError should reset error states`() = runTest {
204:         // Given - set an error
205:         val errorMessage = "Network error"
206:         doAnswer {
207:             throw RuntimeException(errorMessage)
208:         }.`when`(cocktailRepository).getCocktailsSortedByNewest()
209: 
210:         viewModel.loadCocktails()
211:         testDispatcher.scheduler.advanceUntilIdle()
212: 
213:         // When
214:         viewModel.clearLegacyError()
215: 
216:         // Then
217:         assertEquals("", viewModel.errorString.value)
218:         assertNull(viewModel.error.value)
219:     }
220: 
221:     @Test
222:     fun `setOfflineMode should update repository and state`() = runTest {
223:         // When
224:         viewModel.setOfflineMode(true)
225: 
226:         // Then
227:         assertTrue(viewModel.isOfflineMode.value)
228:         verify(cocktailRepository).setOfflineMode(true)
229:     }
230: }
````

## File: androidApp/src/test/java/com/cocktailcraft/viewmodel/KoinThemeViewModelTest.kt
````kotlin
 1: package com.cocktailcraft.viewmodel
 2: 
 3: import app.cash.turbine.test
 4: import com.cocktailcraft.BaseKoinTest
 5: import com.cocktailcraft.domain.model.UserPreferences
 6: import com.cocktailcraft.domain.repository.AuthRepository
 7: import kotlinx.coroutines.Dispatchers
 8: import kotlinx.coroutines.ExperimentalCoroutinesApi
 9: import kotlinx.coroutines.flow.flowOf
10: import kotlinx.coroutines.test.StandardTestDispatcher
11: import kotlinx.coroutines.test.resetMain
12: import kotlinx.coroutines.test.runTest
13: import kotlinx.coroutines.test.setMain
14: import org.junit.After
15: import org.junit.Before
16: import org.junit.Test
17: import org.koin.core.component.inject
18: import org.mockito.kotlin.verify
19: import org.mockito.kotlin.whenever
20: import kotlin.test.assertEquals
21: import kotlin.test.assertFalse
22: import kotlin.test.assertTrue
23: import kotlin.time.Duration.Companion.seconds
24: 
25: /**
26:  * Example of a ViewModel test that uses Koin for dependency injection.
27:  * This demonstrates how to use the BaseKoinTest class for cleaner tests.
28:  */
29: @OptIn(ExperimentalCoroutinesApi::class)
30: class KoinThemeViewModelTest : BaseKoinTest() {
31:     
32:     private lateinit var viewModel: ThemeViewModel
33:     private val authRepository: AuthRepository by inject()
34:     private val testDispatcher = StandardTestDispatcher()
35:     
36:     @Before
37:     override fun setUp() {
38:         super.setUp() // Initialize Koin
39:         Dispatchers.setMain(testDispatcher)
40:         
41:         // Mock the initial state - default to light mode
42:         val initialPreferences = UserPreferences(darkMode = false)
43:         whenever(authRepository.getUserPreferences()).thenReturn(flowOf(initialPreferences))
44:         
45:         // Create the view model using Koin injection
46:         viewModel = ThemeViewModel()
47:         
48:         // Advance the dispatcher to allow the init block to complete
49:         testDispatcher.scheduler.advanceUntilIdle()
50:     }
51:     
52:     @After
53:     override fun tearDown() {
54:         Dispatchers.resetMain()
55:         super.tearDown() // Clean up Koin
56:     }
57:     
58:     @Test
59:     fun `initial state should reflect user preferences`() = runTest {
60:         viewModel.isDarkMode.test(timeout = 2.seconds) {
61:             assertFalse(awaitItem()) // Should be false (light mode) based on our mock
62:             cancelAndIgnoreRemainingEvents()
63:         }
64:     }
65:     
66:     @Test
67:     fun `toggleDarkMode should toggle the dark mode state`() = runTest {
68:         // Initial state is false (light mode)
69:         viewModel.isDarkMode.test {
70:             assertFalse(awaitItem())
71:             cancelAndIgnoreRemainingEvents()
72:         }
73:         
74:         // Mock the update preferences call
75:         val updatedPreferences = UserPreferences(darkMode = true)
76:         whenever(authRepository.updateUserPreferences(updatedPreferences)).thenReturn(flowOf(true))
77:         
78:         // Toggle dark mode
79:         viewModel.toggleDarkMode()
80:         testDispatcher.scheduler.advanceUntilIdle()
81:         
82:         // Verify the state was toggled
83:         viewModel.isDarkMode.test {
84:             assertTrue(awaitItem()) // Should now be true (dark mode)
85:             cancelAndIgnoreRemainingEvents()
86:         }
87:         
88:         // Verify the repository was called to update preferences
89:         verify(authRepository).updateUserPreferences(updatedPreferences)
90:     }
91: }
````

## File: androidApp/src/test/java/com/cocktailcraft/viewmodel/OrderViewModelTest.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import app.cash.turbine.test
  4: import com.cocktailcraft.domain.model.CocktailCartItem
  5: import com.cocktailcraft.domain.model.Cocktail
  6: import com.cocktailcraft.domain.model.CocktailIngredient
  7: import com.cocktailcraft.domain.model.Order
  8: import com.cocktailcraft.domain.model.OrderItem
  9: import com.cocktailcraft.domain.repository.OrderRepository
 10: import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
 11: import com.cocktailcraft.domain.util.Result
 12: import kotlinx.coroutines.Dispatchers
 13: import kotlinx.coroutines.ExperimentalCoroutinesApi
 14: import kotlinx.coroutines.flow.flowOf
 15: import kotlinx.coroutines.test.StandardTestDispatcher
 16: import kotlinx.coroutines.test.resetMain
 17: import kotlinx.coroutines.test.runTest
 18: import kotlinx.coroutines.test.setMain
 19: import org.junit.After
 20: import org.junit.Assert.assertEquals
 21: import org.junit.Assert.assertNotNull
 22: import org.junit.Assert.assertNull
 23: import org.junit.Before
 24: import org.junit.Test
 25: import org.mockito.Mock
 26: import org.mockito.Mockito.`when`
 27: import org.mockito.Mockito.verify
 28: import org.mockito.MockitoAnnotations
 29: import org.mockito.kotlin.any
 30: import org.mockito.kotlin.whenever
 31: 
 32: @OptIn(ExperimentalCoroutinesApi::class)
 33: class OrderViewModelTest {
 34:     
 35:     private lateinit var viewModel: OrderViewModel
 36:     
 37:     @Mock
 38:     private lateinit var orderRepository: OrderRepository
 39:     
 40:     @Mock
 41:     private lateinit var placeOrderUseCase: PlaceOrderUseCase
 42:     
 43:     private val testDispatcher = StandardTestDispatcher()
 44:     
 45:     @Before
 46:     fun setup() {
 47:         MockitoAnnotations.openMocks(this)
 48:         Dispatchers.setMain(testDispatcher)
 49:         viewModel = OrderViewModel(orderRepository, placeOrderUseCase)
 50:     }
 51:     
 52:     @After
 53:     fun tearDown() {
 54:         Dispatchers.resetMain()
 55:     }
 56:     
 57:     @Test
 58:     fun `initial state should have empty orders, no loading, and no error`() = runTest {
 59:         // Given
 60:         val emptyOrderList = emptyList<Order>()
 61:         whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(emptyOrderList))
 62:         
 63:         // When
 64:         viewModel.loadOrders()
 65:         testDispatcher.scheduler.advanceUntilIdle()
 66:         
 67:         // Then
 68:         viewModel.orders.test {
 69:             assertEquals(emptyOrderList, awaitItem())
 70:             cancelAndIgnoreRemainingEvents()
 71:         }
 72:         
 73:         viewModel.isLoading.test {
 74:             assertEquals(false, awaitItem())
 75:             cancelAndIgnoreRemainingEvents()
 76:         }
 77:         
 78:         viewModel.error.test {
 79:             assertNull(awaitItem())
 80:             cancelAndIgnoreRemainingEvents()
 81:         }
 82:     }
 83:     
 84:     @Test
 85:     fun `loadOrders should update orders state with repository data`() = runTest {
 86:         // Given
 87:         val ordersList = listOf(
 88:             Order(
 89:                 id = "order1",
 90:                 date = "2023-05-01",
 91:                 items = listOf(OrderItem("Mojito", 2, 10.0)),
 92:                 total = 20.0,
 93:                 status = "Delivered"
 94:             ),
 95:             Order(
 96:                 id = "order2",
 97:                 date = "2023-05-02",
 98:                 items = listOf(OrderItem("Margarita", 1, 12.0)),
 99:                 total = 12.0,
100:                 status = "Processing"
101:             )
102:         )
103:         whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(ordersList))
104:         
105:         // When
106:         viewModel.loadOrders()
107:         testDispatcher.scheduler.advanceUntilIdle()
108:         
109:         // Then
110:         viewModel.orders.test {
111:             assertEquals(ordersList, awaitItem())
112:             cancelAndIgnoreRemainingEvents()
113:         }
114:     }
115:     
116:     @Test
117:     fun `addOrder should call repository and reload orders`() = runTest {
118:         // Given
119:         val order = Order(
120:             id = "order3",
121:             date = "2023-05-03",
122:             items = listOf(OrderItem("Daiquiri", 1, 11.0)),
123:             total = 11.0,
124:             status = "Pending"
125:         )
126:         val updatedOrdersList = listOf(order)
127:         whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(updatedOrdersList))
128:         
129:         // When
130:         viewModel.addOrder(order)
131:         testDispatcher.scheduler.advanceUntilIdle()
132:         
133:         // Then
134:         verify(orderRepository).addOrder(order)
135:         viewModel.orders.test {
136:             assertEquals(updatedOrdersList, awaitItem())
137:             cancelAndIgnoreRemainingEvents()
138:         }
139:     }
140:     
141:     @Test
142:     fun `placeOrder should call useCase and update state on success`() = runTest {
143:         // Given
144:         val cartItems = listOf(
145:             CocktailCartItem(
146:                 cocktail = Cocktail(
147:                     id = "cocktail1",
148:                     name = "Mojito",
149:                     imageUrl = "mojito.jpg",
150:                     price = 10.0,
151:                     ingredients = listOf(
152:                         CocktailIngredient("Rum", "2 oz"),
153:                         CocktailIngredient("Mint", "6 leaves"),
154:                         CocktailIngredient("Lime", "1"),
155:                         CocktailIngredient("Sugar", "2 tsp"),
156:                         CocktailIngredient("Soda", "splash")
157:                     ),
158:                     instructions = "Muddle mint and lime...",
159:                     rating = 4.5f,
160:                     category = "Classic"
161:                 ),
162:                 quantity = 2
163:             )
164:         )
165:         val totalPrice = 20.0
166:         val newOrder = Order(
167:             id = "order1",
168:             date = "2023-05-01",
169:             items = listOf(OrderItem("Mojito", 2, 10.0)),
170:             total = 20.0,
171:             status = "Processing"
172:         )
173:         val updatedOrdersList = listOf(newOrder)
174:         
175:         whenever(placeOrderUseCase(cartItems, totalPrice)).thenReturn(
176:             flowOf(Result.Success(newOrder))
177:         )
178:         whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(updatedOrdersList))
179:         
180:         // When
181:         viewModel.placeOrder(cartItems, totalPrice)
182:         testDispatcher.scheduler.advanceUntilIdle()
183:         
184:         // Then
185:         verify(placeOrderUseCase).invoke(cartItems, totalPrice)
186:         viewModel.orders.test {
187:             assertEquals(updatedOrdersList, awaitItem())
188:             cancelAndIgnoreRemainingEvents()
189:         }
190:         
191:         viewModel.isLoading.test {
192:             assertEquals(false, awaitItem())
193:             cancelAndIgnoreRemainingEvents()
194:         }
195:         
196:         viewModel.error.test {
197:             assertNull(awaitItem())
198:             cancelAndIgnoreRemainingEvents()
199:         }
200:     }
201:     
202:     @Test
203:     fun `placeOrder should update error state on failure`() = runTest {
204:         // Given
205:         val cartItems = listOf(
206:             CocktailCartItem(
207:                 cocktail = Cocktail(
208:                     id = "cocktail1",
209:                     name = "Mojito",
210:                     imageUrl = "mojito.jpg",
211:                     price = 10.0,
212:                     ingredients = listOf(
213:                         CocktailIngredient("Rum", "2 oz"),
214:                         CocktailIngredient("Mint", "6 leaves"),
215:                         CocktailIngredient("Lime", "1"),
216:                         CocktailIngredient("Sugar", "2 tsp"),
217:                         CocktailIngredient("Soda", "splash")
218:                     ),
219:                     instructions = "Muddle mint and lime...",
220:                     rating = 4.5f,
221:                     category = "Classic"
222:                 ),
223:                 quantity = 2
224:             )
225:         )
226:         val totalPrice = 20.0
227:         val errorMessage = "Network error"
228:         
229:         whenever(placeOrderUseCase(cartItems, totalPrice)).thenReturn(
230:             flowOf(Result.Error(errorMessage))
231:         )
232:         
233:         // When
234:         viewModel.placeOrder(cartItems, totalPrice)
235:         testDispatcher.scheduler.advanceUntilIdle()
236:         
237:         // Then
238:         verify(placeOrderUseCase).invoke(cartItems, totalPrice)
239:         
240:         viewModel.error.test {
241:             assertEquals("Failed to place order: $errorMessage", awaitItem())
242:             cancelAndIgnoreRemainingEvents()
243:         }
244:         
245:         viewModel.isLoading.test {
246:             assertEquals(false, awaitItem())
247:             cancelAndIgnoreRemainingEvents()
248:         }
249:     }
250:     
251:     @Test
252:     fun `placeOrderDirectly should create order and call repository`() = runTest {
253:         // Given
254:         val cartItems = listOf(
255:             CocktailCartItem(
256:                 cocktail = Cocktail(
257:                     id = "cocktail1",
258:                     name = "Mojito",
259:                     imageUrl = "mojito.jpg",
260:                     price = 10.0,
261:                     ingredients = listOf(
262:                         CocktailIngredient("Rum", "2 oz"),
263:                         CocktailIngredient("Mint", "6 leaves"),
264:                         CocktailIngredient("Lime", "1"),
265:                         CocktailIngredient("Sugar", "2 tsp"),
266:                         CocktailIngredient("Soda", "splash")
267:                     ),
268:                     instructions = "Muddle mint and lime...",
269:                     rating = 4.5f,
270:                     category = "Classic"
271:                 ),
272:                 quantity = 2
273:             )
274:         )
275:         val totalPrice = 20.0
276:         val updatedOrdersList = listOf(
277:             Order(
278:                 id = "order1",
279:                 date = "2023-05-01",
280:                 items = listOf(OrderItem("Mojito", 2, 10.0)),
281:                 total = 20.0,
282:                 status = "Processing"
283:             )
284:         )
285:         
286:         // We can't mock the exact order since it uses System.currentTimeMillis()
287:         whenever(orderRepository.placeOrder(any())).thenReturn(flowOf(true))
288:         whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(updatedOrdersList))
289:         
290:         // When
291:         viewModel.placeOrderDirectly(cartItems, totalPrice)
292:         testDispatcher.scheduler.advanceUntilIdle()
293:         
294:         // Then
295:         // Verify placeOrder was called with any Order object
296:         verify(orderRepository).placeOrder(any())
297:         
298:         viewModel.orders.test {
299:             assertEquals(updatedOrdersList, awaitItem())
300:             cancelAndIgnoreRemainingEvents()
301:         }
302:         
303:         viewModel.isLoading.test {
304:             assertEquals(false, awaitItem())
305:             cancelAndIgnoreRemainingEvents()
306:         }
307:         
308:         viewModel.error.test {
309:             assertNull(awaitItem())
310:             cancelAndIgnoreRemainingEvents()
311:         }
312:     }
313:     
314:     @Test
315:     fun `updateOrderStatus should call repository and reload orders`() = runTest {
316:         // Given
317:         val orderId = "order1"
318:         val newStatus = "Delivered"
319:         val updatedOrdersList = listOf(
320:             Order(
321:                 id = orderId,
322:                 date = "2023-05-01",
323:                 items = listOf(OrderItem("Mojito", 2, 10.0)),
324:                 total = 20.0,
325:                 status = newStatus
326:             )
327:         )
328:         
329:         whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(updatedOrdersList))
330:         
331:         // When
332:         viewModel.updateOrderStatus(orderId, newStatus)
333:         testDispatcher.scheduler.advanceUntilIdle()
334:         
335:         // Then
336:         verify(orderRepository).updateOrderStatus(orderId, newStatus)
337:         
338:         viewModel.orders.test {
339:             assertEquals(updatedOrdersList, awaitItem())
340:             cancelAndIgnoreRemainingEvents()
341:         }
342:     }
343:     
344:     @Test
345:     fun `cancelOrder should call repository and reload orders on success`() = runTest {
346:         // Given
347:         val orderId = "order1"
348:         val updatedOrdersList = listOf(
349:             Order(
350:                 id = orderId,
351:                 date = "2023-05-01",
352:                 items = listOf(OrderItem("Mojito", 2, 10.0)),
353:                 total = 20.0,
354:                 status = "Cancelled"
355:             )
356:         )
357:         
358:         whenever(orderRepository.cancelOrder(orderId)).thenReturn(flowOf(true))
359:         whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(updatedOrdersList))
360:         
361:         // When
362:         viewModel.cancelOrder(orderId)
363:         testDispatcher.scheduler.advanceUntilIdle()
364:         
365:         // Then
366:         verify(orderRepository).cancelOrder(orderId)
367:         
368:         viewModel.orders.test {
369:             assertEquals(updatedOrdersList, awaitItem())
370:             cancelAndIgnoreRemainingEvents()
371:         }
372:         
373:         viewModel.error.test {
374:             assertNull(awaitItem())
375:             cancelAndIgnoreRemainingEvents()
376:         }
377:     }
378:     
379:     @Test
380:     fun `cancelOrder should update error state on failure`() = runTest {
381:         // Given
382:         val orderId = "order1"
383:         
384:         whenever(orderRepository.cancelOrder(orderId)).thenReturn(flowOf(false))
385:         
386:         // When
387:         viewModel.cancelOrder(orderId)
388:         testDispatcher.scheduler.advanceUntilIdle()
389:         
390:         // Then
391:         verify(orderRepository).cancelOrder(orderId)
392:         
393:         viewModel.error.test {
394:             assertEquals("Failed to cancel order. It may be too late to cancel.", awaitItem())
395:             cancelAndIgnoreRemainingEvents()
396:         }
397:     }
398:     
399:     @Test
400:     fun `getOrderById should return order from current list if available`() = runTest {
401:         // Given
402:         val orderId = "order1"
403:         val order = Order(
404:             id = orderId,
405:             date = "2023-05-01",
406:             items = listOf(OrderItem("Mojito", 2, 10.0)),
407:             total = 20.0,
408:             status = "Processing"
409:         )
410:         val ordersList = listOf(order)
411:         
412:         // Set up the current orders list
413:         whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(ordersList))
414:         viewModel.loadOrders()
415:         testDispatcher.scheduler.advanceUntilIdle()
416:         
417:         // When & Then
418:         viewModel.getOrderById(orderId).test {
419:             val result = awaitItem()
420:             assertNotNull(result)
421:             assertEquals(orderId, result?.id)
422:             assertEquals("Processing", result?.status)
423:             cancelAndIgnoreRemainingEvents()
424:         }
425:     }
426:     
427:     @Test
428:     fun `getOrderById should fetch from repository if not in current list`() = runTest {
429:         // Given
430:         val orderId = "order2"
431:         val order = Order(
432:             id = orderId,
433:             date = "2023-05-02",
434:             items = listOf(OrderItem("Margarita", 1, 12.0)),
435:             total = 12.0,
436:             status = "Pending"
437:         )
438:         
439:         // Current list doesn't contain the order
440:         whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(emptyList()))
441:         viewModel.loadOrders()
442:         testDispatcher.scheduler.advanceUntilIdle()
443:         
444:         // Repository will return the order
445:         whenever(orderRepository.getOrderById(orderId)).thenReturn(flowOf(order))
446:         
447:         // When & Then
448:         viewModel.getOrderById(orderId).test {
449:             val result = awaitItem()
450:             assertNotNull(result)
451:             assertEquals(orderId, result?.id)
452:             assertEquals("Pending", result?.status)
453:             cancelAndIgnoreRemainingEvents()
454:         }
455:     }
456: }
````

## File: androidApp/src/test/java/com/cocktailcraft/viewmodel/ProfileViewModelTest.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import app.cash.turbine.test
  4: import com.cocktailcraft.domain.model.User
  5: import com.cocktailcraft.domain.repository.AuthRepository
  6: import junit.framework.TestCase.assertEquals
  7: import junit.framework.TestCase.assertFalse
  8: import junit.framework.TestCase.assertNull
  9: import junit.framework.TestCase.assertTrue
 10: import kotlinx.coroutines.Dispatchers
 11: import kotlinx.coroutines.ExperimentalCoroutinesApi
 12: import kotlinx.coroutines.flow.flowOf
 13: import kotlinx.coroutines.test.StandardTestDispatcher
 14: import kotlinx.coroutines.test.resetMain
 15: import kotlinx.coroutines.test.runTest
 16: import kotlinx.coroutines.test.setMain
 17: import org.junit.After
 18: import org.junit.Before
 19: import org.junit.Test
 20: import org.mockito.kotlin.mock
 21: import org.mockito.kotlin.verify
 22: import org.mockito.kotlin.whenever
 23: import kotlin.time.Duration.Companion.seconds
 24: 
 25: @OptIn(ExperimentalCoroutinesApi::class)
 26: class ProfileViewModelTest {
 27:     
 28:     private lateinit var viewModel: ProfileViewModel
 29:     private val authRepository: AuthRepository = mock()
 30:     private val testDispatcher = StandardTestDispatcher()
 31:     
 32:     @Before
 33:     fun setup() = runTest {
 34:         Dispatchers.setMain(testDispatcher)
 35:         
 36:         // Mock the initial state - now within a coroutine context
 37:         whenever(authRepository.isUserSignedIn()).thenReturn(flowOf(false))
 38:         whenever(authRepository.getCurrentUser()).thenReturn(flowOf(null))
 39:         
 40:         // Create the view model with the mocked repository
 41:         viewModel = ProfileViewModel(authRepository)
 42:     }
 43:     
 44:     @After
 45:     fun tearDown() {
 46:         Dispatchers.resetMain()
 47:     }
 48:     
 49:     @Test
 50:     fun `initial state should be not signed in`() = runTest {
 51:         viewModel.isSignedIn.test {
 52:             assertFalse(awaitItem())
 53:             cancelAndIgnoreRemainingEvents()
 54:         }
 55:     }
 56:     
 57:     @Test
 58:     fun `initial user should be null`() = runTest {
 59:         viewModel.user.test {
 60:             assertNull(awaitItem())
 61:             cancelAndIgnoreRemainingEvents()
 62:         }
 63:     }
 64:     
 65:     @Test
 66:     fun `sign in success should update state`() = runTest {
 67:         // Mock successful sign in
 68:         val testUser = User(id = "1", name = "Test User", email = "test@example.com")
 69:         whenever(authRepository.signIn("test@example.com", "password")).thenReturn(flowOf(true))
 70:         whenever(authRepository.isUserSignedIn()).thenReturn(flowOf(true))
 71:         whenever(authRepository.getCurrentUser()).thenReturn(flowOf(testUser))
 72:         
 73:         // Call sign in
 74:         viewModel.signIn("test@example.com", "password")
 75:         
 76:         // Advance the dispatcher to allow coroutines to complete
 77:         testDispatcher.scheduler.advanceUntilIdle()
 78:         
 79:         // Verify states
 80:         viewModel.isSignedIn.test(timeout = 5.seconds) {
 81:             assertTrue(awaitItem())
 82:             cancelAndIgnoreRemainingEvents()
 83:         }
 84:         
 85:         viewModel.user.test(timeout = 5.seconds) {
 86:             val user = awaitItem()
 87:             assertEquals("Test User", user?.name)
 88:             assertEquals("test@example.com", user?.email)
 89:             cancelAndIgnoreRemainingEvents()
 90:         }
 91:         
 92:         viewModel.isLoading.test {
 93:             assertFalse(awaitItem())
 94:             cancelAndIgnoreRemainingEvents()
 95:         }
 96:         
 97:         viewModel.error.test {
 98:             assertNull(awaitItem())
 99:             cancelAndIgnoreRemainingEvents()
100:         }
101:     }
102:     
103:     @Test
104:     fun `sign in failure should show error`() = runTest {
105:         // Mock failed sign in
106:         whenever(authRepository.signIn("wrong@example.com", "password")).thenReturn(flowOf(false))
107:         
108:         // Call sign in
109:         viewModel.signIn("wrong@example.com", "password")
110:         
111:         // Advance the dispatcher to allow coroutines to complete
112:         testDispatcher.scheduler.advanceUntilIdle()
113:         
114:         // Verify states
115:         viewModel.isSignedIn.test {
116:             assertFalse(awaitItem())
117:             cancelAndIgnoreRemainingEvents()
118:         }
119:         
120:         viewModel.error.test {
121:             assertEquals("Invalid email or password", awaitItem())
122:             cancelAndIgnoreRemainingEvents()
123:         }
124:         
125:         viewModel.isLoading.test {
126:             assertFalse(awaitItem())
127:             cancelAndIgnoreRemainingEvents()
128:         }
129:     }
130:     
131:     @Test
132:     fun `sign up success should update state`() = runTest {
133:         // Mock successful sign up
134:         whenever(authRepository.signUp("test@example.com", "password")).thenReturn(flowOf(true))
135:         whenever(authRepository.updateUserName("Test User")).thenReturn(flowOf(true))
136:         whenever(authRepository.isUserSignedIn()).thenReturn(flowOf(true))
137:         whenever(authRepository.getCurrentUser()).thenReturn(
138:             flowOf(User(id = "1", name = "Test User", email = "test@example.com"))
139:         )
140:         
141:         // Call sign up
142:         viewModel.signUp("Test User", "test@example.com", "password")
143:         
144:         // Advance the dispatcher to allow coroutines to complete
145:         testDispatcher.scheduler.advanceUntilIdle()
146:         
147:         // Verify states
148:         viewModel.isSignedIn.test {
149:             assertTrue(awaitItem())
150:             cancelAndIgnoreRemainingEvents()
151:         }
152:         
153:         viewModel.user.test {
154:             val user = awaitItem()
155:             assertEquals("Test User", user?.name)
156:             assertEquals("test@example.com", user?.email)
157:             cancelAndIgnoreRemainingEvents()
158:         }
159:         
160:         viewModel.isLoading.test {
161:             assertFalse(awaitItem())
162:             cancelAndIgnoreRemainingEvents()
163:         }
164:         
165:         viewModel.error.test {
166:             assertNull(awaitItem())
167:             cancelAndIgnoreRemainingEvents()
168:         }
169:         
170:         // Verify updateUserName was called
171:         verify(authRepository).updateUserName("Test User")
172:     }
173:     
174:     @Test
175:     fun `clear error should set error to null`() = runTest {
176:         // Make sure signIn is properly mocked
177:         whenever(authRepository.signIn("wrong@example.com", "password")).thenReturn(flowOf(false))
178:         
179:         // Set an error
180:         viewModel.signIn("wrong@example.com", "password")
181:         
182:         // Advance the dispatcher to allow coroutines to complete
183:         testDispatcher.scheduler.advanceUntilIdle()
184:         
185:         // Verify error is set
186:         viewModel.error.test {
187:             assertEquals("Invalid email or password", awaitItem())
188:             cancelAndIgnoreRemainingEvents()
189:         }
190:         
191:         // Clear error
192:         viewModel.clearError()
193:         
194:         // Verify error is cleared
195:         viewModel.error.test {
196:             assertNull(awaitItem())
197:             cancelAndIgnoreRemainingEvents()
198:         }
199:     }
200:     
201:     @Test
202:     fun `sign out success should update state`() = runTest {
203:         // Setup signed in state first
204:         val testUser = User(id = "1", name = "Test User", email = "test@example.com")
205:         whenever(authRepository.isUserSignedIn()).thenReturn(flowOf(true))
206:         whenever(authRepository.getCurrentUser()).thenReturn(flowOf(testUser))
207:         
208:         // Create a new view model with the signed-in state
209:         val signedInViewModel = ProfileViewModel(authRepository)
210:         
211:         // Advance the dispatcher to allow the init block to complete
212:         testDispatcher.scheduler.advanceUntilIdle()
213:         
214:         // Mock successful sign out
215:         whenever(authRepository.signOut()).thenReturn(flowOf(true))
216:         whenever(authRepository.isUserSignedIn()).thenReturn(flowOf(false))
217:         
218:         // Call sign out
219:         signedInViewModel.signOut()
220:         
221:         // Advance the dispatcher to allow coroutines to complete
222:         testDispatcher.scheduler.advanceUntilIdle()
223:         
224:         // Verify states
225:         signedInViewModel.isSignedIn.test {
226:             assertFalse(awaitItem())
227:             cancelAndIgnoreRemainingEvents()
228:         }
229:         
230:         signedInViewModel.user.test(timeout = 5.seconds) {
231:             assertNull(awaitItem())
232:             cancelAndIgnoreRemainingEvents()
233:         }
234:         
235:         signedInViewModel.isLoading.test {
236:             assertFalse(awaitItem())
237:             cancelAndIgnoreRemainingEvents()
238:         }
239:         
240:         signedInViewModel.error.test {
241:             assertNull(awaitItem())
242:             cancelAndIgnoreRemainingEvents()
243:         }
244:     }
245: }
````

## File: androidApp/src/test/java/com/cocktailcraft/viewmodel/ThemeViewModelTest.kt
````kotlin
  1: package com.cocktailcraft.viewmodel
  2: 
  3: import app.cash.turbine.test
  4: import com.cocktailcraft.domain.model.UserPreferences
  5: import com.cocktailcraft.domain.repository.AuthRepository
  6: import kotlinx.coroutines.Dispatchers
  7: import kotlinx.coroutines.ExperimentalCoroutinesApi
  8: import kotlinx.coroutines.flow.flowOf
  9: import kotlinx.coroutines.test.StandardTestDispatcher
 10: import kotlinx.coroutines.test.resetMain
 11: import kotlinx.coroutines.test.runTest
 12: import kotlinx.coroutines.test.setMain
 13: import org.junit.After
 14: import org.junit.Before
 15: import org.junit.Test
 16: import org.mockito.kotlin.mock
 17: import org.mockito.kotlin.verify
 18: import org.mockito.kotlin.whenever
 19: import kotlin.test.assertEquals
 20: import kotlin.test.assertFalse
 21: import kotlin.test.assertTrue
 22: import kotlin.time.Duration.Companion.seconds
 23: 
 24: @OptIn(ExperimentalCoroutinesApi::class)
 25: class ThemeViewModelTest {
 26:     
 27:     private lateinit var viewModel: ThemeViewModel
 28:     private val authRepository: AuthRepository = mock()
 29:     private val testDispatcher = StandardTestDispatcher()
 30:     
 31:     @Before
 32:     fun setup() = runTest {
 33:         Dispatchers.setMain(testDispatcher)
 34:         
 35:         // Mock the initial state - default to light mode
 36:         val initialPreferences = UserPreferences(darkMode = false)
 37:         whenever(authRepository.getUserPreferences()).thenReturn(flowOf(initialPreferences))
 38:         
 39:         // Create the view model with the mocked repository
 40:         viewModel = ThemeViewModel(authRepository)
 41:         
 42:         // Advance the dispatcher to allow the init block to complete
 43:         testDispatcher.scheduler.advanceUntilIdle()
 44:     }
 45:     
 46:     @After
 47:     fun tearDown() {
 48:         Dispatchers.resetMain()
 49:     }
 50:     
 51:     @Test
 52:     fun `initial state should load dark mode preference from repository`() = runTest {
 53:         // Verify the initial state
 54:         viewModel.isDarkMode.test(timeout = 5.seconds) {
 55:             assertFalse(awaitItem()) // Should be false (light mode) based on our mock
 56:             cancelAndIgnoreRemainingEvents()
 57:         }
 58:     }
 59:     
 60:     @Test
 61:     fun `toggleDarkMode should toggle the dark mode state`() = runTest {
 62:         // Initial state is false (light mode)
 63:         viewModel.isDarkMode.test {
 64:             assertFalse(awaitItem())
 65:             cancelAndIgnoreRemainingEvents()
 66:         }
 67:         
 68:         // Mock the update preferences call
 69:         val updatedPreferences = UserPreferences(darkMode = true)
 70:         whenever(authRepository.updateUserPreferences(updatedPreferences)).thenReturn(flowOf(true))
 71:         
 72:         // Toggle dark mode
 73:         viewModel.toggleDarkMode()
 74:         testDispatcher.scheduler.advanceUntilIdle()
 75:         
 76:         // Verify the state was toggled
 77:         viewModel.isDarkMode.test {
 78:             assertTrue(awaitItem()) // Should now be true (dark mode)
 79:             cancelAndIgnoreRemainingEvents()
 80:         }
 81:         
 82:         // Verify the repository was called to update preferences
 83:         verify(authRepository).updateUserPreferences(updatedPreferences)
 84:     }
 85:     
 86:     @Test
 87:     fun `setDarkMode should set the dark mode state to the specified value`() = runTest {
 88:         // Initial state is false (light mode)
 89:         viewModel.isDarkMode.test {
 90:             assertFalse(awaitItem())
 91:             cancelAndIgnoreRemainingEvents()
 92:         }
 93:         
 94:         // Mock the update preferences call
 95:         val updatedPreferences = UserPreferences(darkMode = true)
 96:         whenever(authRepository.updateUserPreferences(updatedPreferences)).thenReturn(flowOf(true))
 97:         
 98:         // Set dark mode to true
 99:         viewModel.setDarkMode(true)
100:         testDispatcher.scheduler.advanceUntilIdle()
101:         
102:         // Verify the state was set to true
103:         viewModel.isDarkMode.test {
104:             assertTrue(awaitItem())
105:             cancelAndIgnoreRemainingEvents()
106:         }
107:         
108:         // Verify the repository was called to update preferences
109:         verify(authRepository).updateUserPreferences(updatedPreferences)
110:     }
111: }
````

## File: androidApp/src/test/java/com/cocktailcraft/BaseKoinTest.kt
````kotlin
 1: package com.cocktailcraft
 2: 
 3: import com.cocktailcraft.di.testModule
 4: import org.junit.After
 5: import org.junit.Before
 6: import org.koin.core.context.startKoin
 7: import org.koin.core.context.stopKoin
 8: import org.koin.test.KoinTest
 9: 
10: /**
11:  * Base class for tests that need Koin dependency injection.
12:  * Automatically sets up and tears down Koin with the test module.
13:  */
14: abstract class BaseKoinTest : KoinTest {
15:     
16:     @Before
17:     open fun setUp() {
18:         stopKoin() // Ensure Koin is stopped before starting
19:         startKoin {
20:             modules(testModule)
21:         }
22:     }
23:     
24:     @After
25:     open fun tearDown() {
26:         stopKoin()
27:     }
28: }
````

## File: androidApp/build.gradle.kts
````
  1: plugins {
  2:     id("com.android.application")
  3:     kotlin("android")
  4: }
  5: 
  6: android {
  7:     namespace = "com.cocktailcraft.android"
  8:     compileSdk = 34
  9: 
 10:     defaultConfig {
 11:         applicationId = "com.cocktailcraft"
 12:         minSdk = 24
 13:         targetSdk = 34
 14:         versionCode = 1
 15:         versionName = "1.0"
 16: 
 17:         // Use our custom test runner for instrumented tests
 18:         testInstrumentationRunner = "com.cocktailcraft.CocktailCraftTestRunner"
 19:     }
 20: 
 21:     buildFeatures {
 22:         compose = true
 23:     }
 24: 
 25:     composeOptions {
 26:         kotlinCompilerExtensionVersion = "1.5.8"
 27:     }
 28: 
 29:     compileOptions {
 30:         sourceCompatibility = JavaVersion.VERSION_17
 31:         targetCompatibility = JavaVersion.VERSION_17
 32:     }
 33: 
 34:     kotlinOptions {
 35:         jvmTarget = "17"
 36:         freeCompilerArgs += listOf("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
 37:     }
 38: }
 39: 
 40: // Add a configuration for dependency resolution
 41: configurations.all {
 42:     resolutionStrategy {
 43:         // Force a specific version of Espresso to avoid conflicts
 44:         force("androidx.test.espresso:espresso-core:3.5.0")
 45:         force("androidx.test.espresso:espresso-idling-resource:3.5.0")
 46:     }
 47: }
 48: 
 49: dependencies {
 50:     implementation(project(":shared"))
 51:     implementation("com.google.android.material:material:1.11.0")
 52:     implementation("androidx.core:core-ktx:1.12.0")
 53:     implementation("androidx.appcompat:appcompat:1.6.1")
 54:     implementation(libs.androidx.compose.material)
 55:     implementation(libs.androidx.ui.test.junit4.android)
 56: 
 57:     // Compose
 58:     val composeBom = platform(libs.androidx.compose.bom)
 59:     implementation(composeBom)
 60:     implementation("androidx.compose.ui:ui")
 61:     implementation("androidx.compose.ui:ui-graphics")
 62:     implementation("androidx.compose.ui:ui-tooling-preview")
 63:     implementation("androidx.compose.material3:material3")
 64:     implementation("androidx.activity:activity-compose:1.8.2")
 65: 
 66:     // Material Icons Extended - provides additional icons
 67:     implementation("androidx.compose.material:material-icons-extended")
 68: 
 69:     // Navigation
 70:     implementation("androidx.navigation:navigation-compose:2.7.7")
 71: 
 72:     // Koin
 73:     implementation("io.insert-koin:koin-android:3.4.0")
 74:     implementation("io.insert-koin:koin-androidx-compose:3.4.0")
 75: 
 76:     // ViewModel
 77:     implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
 78:     implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
 79: 
 80:     // Add Coil for image loading
 81:     implementation("io.coil-kt:coil-compose:2.4.0")
 82: 
 83:     // Accompanist libraries for animations and system UI controller
 84:     implementation(libs.accompanist.systemuicontroller)
 85:     implementation(libs.accompanist.navigation.animation)
 86: 
 87:     debugImplementation("androidx.compose.ui:ui-tooling")
 88:     debugImplementation("androidx.compose.ui:ui-test-manifest")
 89: 
 90:     // Test dependencies
 91:     testImplementation("junit:junit:4.13.2")
 92:     testImplementation("org.mockito:mockito-core:5.3.1")
 93:     testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
 94:     testImplementation("app.cash.turbine:turbine:1.0.0")
 95:     testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
 96:     testImplementation("androidx.navigation:navigation-testing:2.7.7")
 97:     testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
 98:     testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
 99:     testImplementation("com.russhwolf:multiplatform-settings:1.1.1")
100: 
101:     // Instrumented test dependencies
102:     androidTestImplementation("androidx.test.ext:junit:1.1.5")
103:     androidTestImplementation(composeBom)
104:     androidTestImplementation("androidx.compose.ui:ui-test-junit4")
105:     // Use the version of Espresso that's compatible with Compose UI test
106:     androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
107:     androidTestImplementation("org.mockito:mockito-android:5.3.1")
108:     androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
109:     androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
110: }
````

## File: docs/images/mermaid/class_diagram.md
````markdown
  1: # Class Diagram
  2: 
  3: ```mermaid
  4: classDiagram
  5:     %% Domain Models
  6:     class Cocktail {
  7:         +String id
  8:         +String name
  9:         +String? alternateName
 10:         +List~String~? tags
 11:         +String? category
 12:         +String? iba
 13:         +String? alcoholic
 14:         +String? glass
 15:         +String? instructions
 16:         +String? imageUrl
 17:         +List~CocktailIngredient~ ingredients
 18:         +String? imageSource
 19:         +String? imageAttribution
 20:         +Boolean? creativeCommonsConfirmed
 21:         +String? dateModified
 22:         +Double price
 23:         +Boolean inStock
 24:         +Int stockCount
 25:         +Float rating
 26:         +Int popularity
 27:         +Long dateAdded
 28:     }
 29: 
 30:     class CocktailIngredient {
 31:         +String name
 32:         +String? measure
 33:     }
 34: 
 35:     class CocktailCartItem {
 36:         +Cocktail cocktail
 37:         +Int quantity
 38:     }
 39: 
 40:     class Order {
 41:         +String id
 42:         +String date
 43:         +List~OrderItem~ items
 44:         +Double total
 45:         +String status
 46:     }
 47: 
 48:     class OrderItem {
 49:         +String name
 50:         +Int quantity
 51:         +Double price
 52:     }
 53: 
 54:     class User {
 55:         +String id
 56:         +String name
 57:         +String email
 58:         +String? profileImageUrl
 59:         +Boolean isLoggedIn
 60:     }
 61: 
 62:     class Review {
 63:         +String id
 64:         +String cocktailId
 65:         +String userId
 66:         +String userName
 67:         +Float rating
 68:         +String comment
 69:         +Long date
 70:     }
 71: 
 72:     class UserPreferences {
 73:         +Boolean darkMode
 74:         +Boolean followSystemTheme
 75:         +Boolean offlineMode
 76:         +Long lastCacheUpdate
 77:     }
 78: 
 79:     class SortOption {
 80:         <<enumeration>>
 81:         NEWEST
 82:         PRICE_LOW_TO_HIGH
 83:         PRICE_HIGH_TO_LOW
 84:         POPULARITY
 85:     }
 86: 
 87:     %% Repository Interfaces
 88:     class CocktailRepository {
 89:         <<interface>>
 90:         +searchCocktailsByName(String name): Flow~List~Cocktail~~
 91:         +searchCocktailsByFirstLetter(Char letter): Flow~List~Cocktail~~
 92:         +getCocktailById(String id): Flow~Cocktail?~
 93:         +getRandomCocktail(): Flow~Cocktail?~
 94:         +filterByIngredient(String ingredient): Flow~List~Cocktail~~
 95:         +filterByAlcoholic(Boolean alcoholic): Flow~List~Cocktail~~
 96:         +filterByCategory(String category): Flow~List~Cocktail~~
 97:         +filterByGlass(String glass): Flow~List~Cocktail~~
 98:         +getCategories(): Flow~List~String~~
 99:         +getGlasses(): Flow~List~String~~
100:         +getIngredients(): Flow~List~String~~
101:         +getAlcoholicFilters(): Flow~List~String~~
102:         +getFavoriteCocktails(): Flow~List~Cocktail~~
103:         +addToFavorites(Cocktail cocktail)
104:         +removeFromFavorites(Cocktail cocktail)
105:         +isCocktailFavorite(String id): Flow~Boolean~
106:         +getCocktailsSortedByNewest(): Flow~List~Cocktail~~
107:         +getCocktailsSortedByPriceLowToHigh(): Flow~List~Cocktail~~
108:         +getCocktailsSortedByPriceHighToLow(): Flow~List~Cocktail~~
109:         +getCocktailsSortedByPopularity(): Flow~List~Cocktail~~
110:         +getCocktailsByPriceRange(Double minPrice, Double maxPrice): Flow~List~Cocktail~~
111:         +getCocktailImageUrl(Cocktail cocktail): String
112:         +checkApiConnectivity(): Flow~Boolean~
113:         +isOfflineModeEnabled(): Boolean
114:         +setOfflineMode(Boolean enabled)
115:         +getCocktailsByIngredient(String ingredient): List~Cocktail~
116:         +getCocktailsByCategory(String category): List~Cocktail~
117:         +getRecentlyViewedCocktails(): Flow~List~Cocktail~~
118:         +clearCache()
119:     }
120: 
121:     class CartRepository {
122:         <<interface>>
123:         +getCartItems(): Flow~List~CocktailCartItem~~
124:         +addToCart(CocktailCartItem item)
125:         +removeFromCart(String cocktailId)
126:         +updateQuantity(String cocktailId, Int quantity)
127:         +clearCart()
128:         +getCartTotal(): Flow~Double~
129:     }
130: 
131:     class OrderRepository {
132:         <<interface>>
133:         +getOrders(): Flow~List~Order~~
134:         +getOrderById(String id): Flow~Order?~
135:         +addOrder(Order order)
136:         +updateOrderStatus(String orderId, String status)
137:     }
138: 
139:     class AuthRepository {
140:         <<interface>>
141:         +getCurrentUser(): Flow~User?~
142:         +login(String email, String password): Flow~Result~User~~
143:         +register(String name, String email, String password): Flow~Result~User~~
144:         +logout(): Flow~Result~Unit~~
145:         +isLoggedIn(): Flow~Boolean~
146:         +getUserPreferences(): Flow~UserPreferences~
147:         +updateUserPreferences(UserPreferences preferences)
148:     }
149: 
150:     class FavoritesRepository {
151:         <<interface>>
152:         +getFavorites(): Flow~List~Cocktail~~
153:         +addFavorite(Cocktail cocktail)
154:         +removeFavorite(String cocktailId)
155:         +isFavorite(String cocktailId): Flow~Boolean~
156:     }
157: 
158:     %% Use Cases
159:     class PlaceOrderUseCase {
160:         -OrderRepository orderRepository
161:         +invoke(List~CocktailCartItem~ cartItems, Double totalPrice): Flow~Result~Order~~
162:     }
163: 
164:     class ToggleFavoriteUseCase {
165:         -CocktailRepository cocktailRepository
166:         +invoke(Cocktail cocktail, Boolean isFavorite): Flow~Result~Boolean~~
167:     }
168: 
169:     %% Utility Classes
170:     class NetworkMonitor {
171:         +startMonitoring()
172:         +stopMonitoring()
173:         +isOnline: StateFlow~Boolean~
174:     }
175: 
176:     class CocktailCache {
177:         -Settings settings
178:         -Json json
179:         -AppConfig appConfig
180:         +cacheCocktail(Cocktail cocktail)
181:         +getCachedCocktail(String id): Cocktail?
182:         +getAllCachedCocktails(): List~Cocktail~
183:         +clearCache()
184:         +addToRecentlyViewed(Cocktail cocktail)
185:         +getRecentlyViewed(): List~Cocktail~
186:     }
187: 
188:     class ErrorUtils {
189:         <<static>>
190:         +getErrorFromException(Exception exception, String defaultMessage): UserFriendlyError
191:         +createNetworkError(Function retryAction): UserFriendlyError
192:         +createErrorFromErrorCode(ErrorCode errorCode): UserFriendlyError
193:     }
194: 
195:     class UserFriendlyError {
196:         +String title
197:         +String message
198:         +ErrorCategory category
199:         +RecoveryAction? recoveryAction
200:         +Throwable? originalException
201:         +ErrorCode errorCode
202:     }
203: 
204:     class RecoveryAction {
205:         +String actionLabel
206:         +Function action
207:     }
208: 
209:     class ErrorCategory {
210:         <<enumeration>>
211:         NETWORK
212:         SERVER
213:         CLIENT
214:         AUTHENTICATION
215:         DATA
216:         UNKNOWN
217:     }
218: 
219:     %% ViewModels
220:     class BaseViewModel {
221:         #MutableStateFlow~Boolean~ _isLoading
222:         #MutableStateFlow~UserFriendlyError?~ _error
223:         #MutableSharedFlow~UserFriendlyError~ _errorEvent
224:         +StateFlow~Boolean~ isLoading
225:         +StateFlow~UserFriendlyError?~ error
226:         +SharedFlow~UserFriendlyError~ errorEvent
227:         #setLoading(Boolean loading)
228:         #handleException(Throwable exception, String defaultMessage)
229:         #setError(String title, String message, ErrorCategory category)
230:         +clearError()
231:         #executeWithErrorHandling(Function operation, Function onSuccess)
232:     }
233: 
234:     class ThemeViewModel {
235:         -AuthRepository repository
236:         -MutableStateFlow~Boolean~ _isDarkMode
237:         -MutableStateFlow~Boolean~ _followSystemTheme
238:         -MutableStateFlow~Boolean~ _isSystemInDarkMode
239:         +StateFlow~Boolean~ isDarkMode
240:         +StateFlow~Boolean~ followSystemTheme
241:         +updateSystemDarkMode(Boolean isDark)
242:         +toggleDarkMode()
243:         +setDarkMode(Boolean enabled)
244:         +toggleFollowSystemTheme()
245:         -loadThemePreference()
246:     }
247: 
248:     class OfflineModeViewModel {
249:         -CocktailRepository cocktailRepository
250:         -NetworkMonitor networkMonitor
251:         -MutableStateFlow~Boolean~ _isOfflineModeEnabled
252:         -MutableStateFlow~Boolean~ _isNetworkAvailable
253:         -MutableStateFlow~List~Cocktail~~ _recentlyViewedCocktails
254:         +StateFlow~Boolean~ isOfflineModeEnabled
255:         +StateFlow~Boolean~ isNetworkAvailable
256:         +StateFlow~List~Cocktail~~ recentlyViewedCocktails
257:         +toggleOfflineMode()
258:         +setOfflineMode(Boolean enabled)
259:         +clearCache()
260:         -loadRecentlyViewedCocktails()
261:     }
262: 
263:     %% Recommendation System
264:     class CocktailRecommendationEngine {
265:         -CocktailRepository cocktailRepository
266:         -FavoritesRepository favoritesRepository
267:         +getRecommendations(Cocktail cocktail, Int limit): List~Cocktail~
268:         -getCocktailsByCategory(String category, Set~String~ excludeIds): List~Cocktail~
269:         -getCocktailsByIngredient(String ingredient, Set~String~ excludeIds): List~Cocktail~
270:         -getCocktailsByAlcoholicFilter(String alcoholicFilter, Set~String~ excludeIds): List~Cocktail~
271:         -getFallbackRecommendations(Set~String~ excludeIds): List~Cocktail~
272:     }
273: 
274:     %% Relationships
275:     Cocktail "1" *-- "many" CocktailIngredient : contains
276:     CocktailCartItem "1" *-- "1" Cocktail : contains
277:     Order "1" *-- "many" OrderItem : contains
278: 
279:     PlaceOrderUseCase --> OrderRepository : uses
280:     PlaceOrderUseCase --> CocktailCartItem : uses
281:     PlaceOrderUseCase --> Order : creates
282:     ToggleFavoriteUseCase --> CocktailRepository : uses
283:     ToggleFavoriteUseCase --> Cocktail : uses
284: 
285:     BaseViewModel --> ErrorUtils : uses
286:     ThemeViewModel --|> BaseViewModel : extends
287:     OfflineModeViewModel --|> BaseViewModel : extends
288: 
289:     ThemeViewModel --> AuthRepository : uses
290:     ThemeViewModel --> UserPreferences : manages
291: 
292:     OfflineModeViewModel --> CocktailRepository : uses
293:     OfflineModeViewModel --> NetworkMonitor : uses
294: 
295:     CocktailRecommendationEngine --> CocktailRepository : uses
296:     CocktailRecommendationEngine --> FavoritesRepository : uses
297: 
298:     CocktailRepository --> CocktailCache : uses
299:     CocktailRepository --> NetworkMonitor : uses
300: 
301:     ErrorUtils --> UserFriendlyError : creates
302:     UserFriendlyError *-- RecoveryAction : contains
303: ```
304: 
305: This diagram shows the key domain models and their relationships in the CocktailCraft application, including:
306: 
307: 1. **Domain Models**: Cocktail, CocktailIngredient, CocktailCartItem, Order, OrderItem, User, Review, and the new UserPreferences model for theme and offline settings
308: 2. **Repository Interfaces**: Extended with new methods for offline mode, caching, and recommendations
309: 3. **Use Cases**: PlaceOrderUseCase and ToggleFavoriteUseCase
310: 4. **Utility Classes**: NetworkMonitor for connectivity tracking, CocktailCache for offline mode, and ErrorUtils for error handling
311: 5. **ViewModels**: BaseViewModel with error handling, ThemeViewModel for dark mode, and OfflineModeViewModel
312: 6. **Recommendation System**: CocktailRecommendationEngine for generating cocktail recommendations
313: 
314: The diagram shows the relationships between these classes, including inheritance, composition, and dependencies.
````

## File: docs/images/mermaid/component_diagram.md
````markdown
  1: # Component Diagram
  2: 
  3: ```mermaid
  4: graph TD
  5:     %% UI Layer
  6:     subgraph UI["UI Layer"]
  7:         Main["MainScreen"]
  8:         Home["HomeScreen"]
  9:         Cart["CartScreen"]
 10:         Favorites["FavoritesScreen"]
 11:         Profile["ProfileScreen"]
 12:         OrderList["OrderListScreen"]
 13:         Detail["CocktailDetailScreen"]
 14:         Offline["OfflineModeScreen"]
 15:         UIComp["UI Components"]
 16:         ErrorComp["Error Components"]
 17:         ThemeComp["Theme Components"]
 18:     end
 19: 
 20:     %% Navigation
 21:     subgraph Nav["Navigation"]
 22:         NavManager["NavigationManager"]
 23:         Screens["Screen Definitions"]
 24:     end
 25: 
 26:     %% ViewModel Layer
 27:     subgraph VM["ViewModel Layer"]
 28:         BaseVM["BaseViewModel"]
 29:         HomeVM["HomeViewModel"]
 30:         CartVM["CartViewModel"]
 31:         FavVM["FavoritesViewModel"]
 32:         ProfileVM["ProfileViewModel"]
 33:         OrderVM["OrderViewModel"]
 34:         ReviewVM["ReviewViewModel"]
 35:         DetailVM["CocktailDetailViewModel"]
 36:         ThemeVM["ThemeViewModel"]
 37:         OfflineVM["OfflineModeViewModel"]
 38:     end
 39: 
 40:     %% Domain Layer
 41:     subgraph Domain["Domain Layer"]
 42:         subgraph Repos["Repository Interfaces"]
 43:             CocktailRepo["CocktailRepository"]
 44:             CartRepo["CartRepository"]
 45:             OrderRepo["OrderRepository"]
 46:             AuthRepo["AuthRepository"]
 47:             FavRepo["FavoritesRepository"]
 48:         end
 49: 
 50:         subgraph UseCases["Use Cases"]
 51:             PlaceOrderUC["PlaceOrderUseCase"]
 52:             ToggleFavoriteUC["ToggleFavoriteUseCase"]
 53:         end
 54: 
 55:         subgraph Models["Models"]
 56:             CocktailModel["Cocktail"]
 57:             CartItemModel["CocktailCartItem"]
 58:             OrderModel["Order"]
 59:             UserModel["User"]
 60:             ReviewModel["Review"]
 61:         end
 62: 
 63:         subgraph Recommendation["Recommendation System"]
 64:             RecEngine["CocktailRecommendationEngine"]
 65:         end
 66:     end
 67: 
 68:     %% Data Layer
 69:     subgraph Data["Data Layer"]
 70:         CocktailRepoImpl["CocktailRepositoryImpl"]
 71:         CartRepoImpl["CartRepositoryImpl"]
 72:         OrderRepoImpl["OrderRepositoryImpl"]
 73:         AuthRepoImpl["AuthRepositoryImpl"]
 74:         FavRepoImpl["FavoritesRepositoryImpl"]
 75: 
 76:         CocktailApiInt["CocktailApi"]
 77:         CocktailApiImpl["CocktailApiImpl"]
 78: 
 79:         LocalStorage["Local Storage"]
 80:         CocktailCache["CocktailCache"]
 81:         NetworkMonitor["NetworkMonitor"]
 82:         ErrorUtils["ErrorUtils"]
 83:         AppConfig["AppConfig"]
 84:     end
 85: 
 86:     %% DI
 87:     subgraph DI["Dependency Injection"]
 88:         AppModule["AppModule"]
 89:         DataModule["DataModule"]
 90:         DomainModule["DomainModule"]
 91:         NetworkModule["NetworkModule"]
 92:         PlatformModule["PlatformModule"]
 93:         RecommendationModule["RecommendationModule"]
 94:     end
 95: 
 96:     %% UI Layer Relationships
 97:     Main --> Home
 98:     Main --> Cart
 99:     Main --> Favorites
100:     Main --> Profile
101:     Main --> OrderList
102:     Main --> Detail
103:     Main --> Offline
104:     Home --> UIComp
105:     Cart --> UIComp
106:     Favorites --> UIComp
107:     Detail --> UIComp
108:     Profile --> ThemeComp
109:     Offline --> UIComp
110:     UIComp --> ErrorComp
111:     UIComp --> ThemeComp
112: 
113:     %% Navigation Relationships
114:     Main --> NavManager
115:     NavManager --> Screens
116: 
117:     %% ViewModel Relationships
118:     BaseVM --> ErrorUtils
119:     HomeVM --> BaseVM
120:     CartVM --> BaseVM
121:     FavVM --> BaseVM
122:     ProfileVM --> BaseVM
123:     OrderVM --> BaseVM
124:     ReviewVM --> BaseVM
125:     DetailVM --> BaseVM
126:     OfflineVM --> BaseVM
127: 
128:     Home --> HomeVM
129:     Cart --> CartVM
130:     Favorites --> FavVM
131:     Profile --> ProfileVM
132:     Profile --> ThemeVM
133:     OrderList --> OrderVM
134:     Detail --> DetailVM
135:     Detail --> ReviewVM
136:     Offline --> OfflineVM
137: 
138:     %% Domain Layer Relationships
139:     HomeVM --> CocktailRepo
140:     CartVM --> CartRepo
141:     FavVM --> CocktailRepo
142:     FavVM --> FavRepo
143:     OrderVM --> OrderRepo
144:     ProfileVM --> AuthRepo
145:     ReviewVM --> CocktailRepo
146:     DetailVM --> CocktailRepo
147:     DetailVM --> RecEngine
148:     OfflineVM --> CocktailRepo
149:     OfflineVM --> NetworkMonitor
150:     ThemeVM --> AuthRepo
151: 
152:     PlaceOrderUC --> OrderRepo
153:     ToggleFavoriteUC --> CocktailRepo
154:     RecEngine --> CocktailRepo
155:     RecEngine --> FavRepo
156: 
157:     %% Data Layer Relationships
158:     CocktailRepoImpl --> CocktailRepo
159:     CartRepoImpl --> CartRepo
160:     OrderRepoImpl --> OrderRepo
161:     AuthRepoImpl --> AuthRepo
162:     FavRepoImpl --> FavRepo
163: 
164:     CocktailRepoImpl --> CocktailApiInt
165:     CocktailRepoImpl --> LocalStorage
166:     CocktailRepoImpl --> CocktailCache
167:     CocktailRepoImpl --> NetworkMonitor
168:     CocktailRepoImpl --> AppConfig
169:     CartRepoImpl --> LocalStorage
170:     OrderRepoImpl --> LocalStorage
171:     AuthRepoImpl --> LocalStorage
172:     FavRepoImpl --> LocalStorage
173:     FavRepoImpl --> CocktailRepo
174: 
175:     CocktailApiImpl --> CocktailApiInt
176:     CocktailCache --> LocalStorage
177: 
178:     %% DI Relationships
179:     AppModule --> DataModule
180:     AppModule --> DomainModule
181:     AppModule --> NetworkModule
182: 
183:     NetworkModule --> CocktailApiImpl
184:     NetworkModule --> NetworkMonitor
185: 
186:     DataModule --> CocktailRepoImpl
187:     DataModule --> CartRepoImpl
188:     DataModule --> OrderRepoImpl
189:     DataModule --> AuthRepoImpl
190:     DataModule --> FavRepoImpl
191:     DataModule --> CocktailCache
192: 
193:     DomainModule --> PlaceOrderUC
194:     DomainModule --> ToggleFavoriteUC
195:     DomainModule --> AppConfig
196: 
197:     RecommendationModule --> RecEngine
198:     RecommendationModule --> DetailVM
199: 
200:     PlatformModule --> LocalStorage
201: ```
202: 
203: This diagram shows the main components of the CocktailCraft application and their relationships, including:
204: 
205: 1. **UI Layer**: All screens and UI components, including new components for Error Handling, Theme System (Dark Mode), and Offline Mode
206: 2. **ViewModel Layer**: All ViewModels, including new ones for Theme management, Offline Mode, and Cocktail Detail with recommendations
207: 3. **Domain Layer**: Repository interfaces, Use Cases, Models, and the new Recommendation System
208: 4. **Data Layer**: Repository implementations, API interfaces and implementations, Local Storage, Caching System for Offline Mode, Network Monitor, Error Utilities, and App Configuration
209: 5. **Dependency Injection**: Modular Koin setup with specialized modules for different concerns
210: 
211: The diagram shows how these components interact with each other, with special attention to the new features like Dark Mode, Offline Mode, Error Handling, and the Recommendation System.
````

## File: docs/images/mermaid/data_flow_diagram.md
````markdown
  1: # Data Flow Diagram
  2: 
  3: ```mermaid
  4: flowchart TD
  5:     %% External Entities
  6:     user([User])
  7:     api([Cocktail API\nTheCocktailDB])
  8:     system([System\nOS Theme & Network])
  9: 
 10:     %% Processes
 11:     ui[UI Layer\nCompose Screens]
 12:     theme[Theme System\nLight/Dark Mode]
 13:     error_ui[Error Handling UI]
 14:     viewmodels[ViewModels]
 15:     base_vm[Base ViewModel\nError Handling]
 16:     usecases[Use Cases]
 17:     recommendation[Recommendation Engine]
 18:     repositories[Repository Layer]
 19:     cache[Caching System\nOffline Mode]
 20:     network[Network Monitor]
 21:     error_handler[Error Handler]
 22: 
 23:     %% Data Stores
 24:     prefs[(Local Storage\nSharedPreferences)]
 25:     datastore[(DataStore)]
 26:     cache_store[(Cache Storage)]
 27: 
 28:     %% Data Flows - System Interactions
 29:     system -->|System Theme Changes| theme
 30:     system -->|Network Status| network
 31: 
 32:     %% Data Flows - User Interactions
 33:     user -->|User Input\nClicks, Text Input| ui
 34:     ui -->|UI Updates\nDisplay Data| user
 35: 
 36:     %% Theme System
 37:     ui -->|Theme Preferences| theme
 38:     theme -->|Theme Updates| ui
 39:     theme -->|Save Theme Preferences| viewmodels
 40:     viewmodels -->|Theme State| theme
 41: 
 42:     %% Error Handling UI
 43:     error_handler -->|Error Information| error_ui
 44:     error_ui -->|Error Display| ui
 45:     ui -->|Retry Actions| error_handler
 46: 
 47:     %% UI to ViewModels
 48:     ui -->|UI Events\nActions| viewmodels
 49:     viewmodels -->|State Updates\nUI Data| ui
 50: 
 51:     %% Base ViewModel
 52:     viewmodels -->|Inherit Error Handling| base_vm
 53:     base_vm -->|Error Events| error_handler
 54:     error_handler -->|User-Friendly Errors| base_vm
 55: 
 56:     %% ViewModels to Use Cases & Recommendation
 57:     viewmodels -->|Business Logic\nRequests| usecases
 58:     usecases -->|Domain Data\nResults| viewmodels
 59:     viewmodels -->|Get Recommendations| recommendation
 60:     recommendation -->|Similar Cocktails| viewmodels
 61: 
 62:     %% Use Cases to Repositories
 63:     usecases -->|Data Operations| repositories
 64:     repositories -->|Domain Models| usecases
 65:     recommendation -->|Fetch Data| repositories
 66: 
 67:     %% Network Monitoring
 68:     network -->|Connection Status| repositories
 69:     network -->|Connection Status| viewmodels
 70: 
 71:     %% Repositories to Data Sources
 72:     repositories -->|API Requests\nWhen Online| api
 73:     api -->|API Responses\nDTOs| repositories
 74: 
 75:     repositories -->|Cache Data\nFor Offline| cache
 76:     cache -->|Cached Data\nWhen Offline| repositories
 77: 
 78:     repositories -->|Read/Write\nPreferences| prefs
 79:     prefs -->|Stored Data| repositories
 80: 
 81:     repositories -->|Read/Write\nStructured Data| datastore
 82:     datastore -->|Stored Data| repositories
 83: 
 84:     cache -->|Store Cached Data| cache_store
 85:     cache_store -->|Retrieve Cached Data| cache
 86: 
 87:     %% Notes
 88:     subgraph UI_Data [UI Data Flows]
 89:         direction LR
 90:         ui_data1[Cocktail List Display]
 91:         ui_data2[Cocktail Details]
 92:         ui_data3[Cart Items]
 93:         ui_data4[Order History]
 94:         ui_data5[User Profile]
 95:         ui_data6[Theme Settings]
 96:         ui_data7[Offline Mode Status]
 97:         ui_data8[Error Messages]
 98:         ui_data9[Recommendations]
 99:     end
100: 
101:     subgraph VM_Data [ViewModel Data]
102:         direction LR
103:         vm_data1[Cocktail Lists]
104:         vm_data2[Cart State]
105:         vm_data3[Order State]
106:         vm_data4[User State]
107:         vm_data5[Favorites State]
108:         vm_data6[Theme State]
109:         vm_data7[Network State]
110:         vm_data8[Error State]
111:         vm_data9[Recommendation State]
112:     end
113: 
114:     subgraph Repo_Ops [Repository Operations]
115:         direction LR
116:         repo_op1[Fetch Cocktails from API]
117:         repo_op2[Store/Retrieve Favorites]
118:         repo_op3[Store/Retrieve Cart Items]
119:         repo_op4[Store/Retrieve Orders]
120:         repo_op5[Store/Retrieve User Data]
121:         repo_op6[Cache Cocktails]
122:         repo_op7[Check Network Status]
123:         repo_op8[Handle API Errors]
124:     end
125: 
126:     subgraph Cache_Ops [Cache Operations]
127:         direction LR
128:         cache_op1[Store Recently Viewed]
129:         cache_op2[Retrieve When Offline]
130:         cache_op3[Clear Cache]
131:         cache_op4[Check Cache Age]
132:     end
133: 
134:     subgraph API_Data [API Data]
135:         direction LR
136:         api_data1[Cocktail Information]
137:         api_data2[Ingredients]
138:         api_data3[Categories]
139:         api_data4[Glasses]
140:     end
141: 
142:     UI_Data -.-> ui
143:     VM_Data -.-> viewmodels
144:     Repo_Ops -.-> repositories
145:     Cache_Ops -.-> cache
146:     API_Data -.-> api
147: ```
148: 
149: This data flow diagram shows how data moves through the CocktailCraft application, including:
150: 
151: 1. **User Interface Layer**: Shows how user input flows through the UI components, including theme settings and error handling UI
152: 2. **ViewModel Layer**: Illustrates how ViewModels manage state and handle errors through the BaseViewModel
153: 3. **Domain Layer**: Shows how Use Cases and the Recommendation Engine interact with repositories
154: 4. **Data Layer**: Depicts how repositories interact with the API, caching system, and local storage
155: 5. **External Systems**: Includes the Cocktail API, system theme changes, and network status
156: 
157: The diagram highlights the new features:
158: - **Dark Mode**: Theme system with user preferences and system theme integration
159: - **Offline Mode**: Caching system and network monitoring for offline functionality
160: - **Error Handling**: Comprehensive error handling flow from API to user interface
161: - **Recommendation System**: Flow of data for cocktail recommendations
````

## File: docs/images/mermaid/high_level_architecture.md
````markdown
 1: # High-Level Architecture Diagram
 2: 
 3: ```mermaid
 4: graph TD
 5:     subgraph "CocktailCraft Application"
 6:         subgraph "Android App"
 7:             UI["UI Layer<br>(Compose Screens)"]
 8:             Theme["Theme System<br>(Light/Dark Mode)"]
 9:             VM["ViewModels"]
10:             Nav["Navigation"]
11:             ErrorUI["Error Handling UI"]
12:         end
13: 
14:         subgraph "Shared Module"
15:             subgraph "Domain Layer"
16:                 Models["Models"]
17:                 RepoInt["Repository Interfaces"]
18:                 UC["Use Cases"]
19:                 RecEngine["Recommendation Engine"]
20:             end
21: 
22:             subgraph "Data Layer"
23:                 RepoImpl["Repository Implementations"]
24:                 Remote["Remote Data Source"]
25:                 Local["Local Data Source"]
26:                 Cache["Caching System"]
27:                 Network["Network Monitor"]
28:                 ErrorHandler["Error Handler"]
29:             end
30: 
31:             subgraph "Cross-Cutting Concerns"
32:                 DI["Dependency Injection<br>(Koin)"]
33:                 Config["App Configuration"]
34:             end
35:         end
36: 
37:         subgraph "Platform-Specific"
38:             AndroidImpl["Android Implementation"]
39:             iOSImpl["iOS Implementation"]
40:         end
41:     end
42: 
43:     %% Relationships
44:     UI --> VM
45:     UI --> Theme
46:     UI --> ErrorUI
47:     VM --> UC
48:     VM --> RecEngine
49:     VM --> Nav
50:     VM --> ErrorHandler
51:     UC --> RepoInt
52:     RecEngine --> RepoInt
53:     RepoImpl --> RepoInt
54:     RepoImpl --> Remote
55:     RepoImpl --> Local
56:     RepoImpl --> Cache
57:     RepoImpl --> Network
58:     RepoImpl --> Models
59:     RepoImpl --> ErrorHandler
60:     AndroidImpl --> RepoImpl
61:     iOSImpl --> RepoImpl
62:     DI --> RepoImpl
63:     DI --> UC
64:     DI --> RecEngine
65:     DI --> VM
66:     DI --> Network
67:     Config --> RepoImpl
68: ```
69: 
70: This diagram shows the high-level architecture of the CocktailCraft application, including:
71: 
72: 1. **Android App Layer**: UI components, ViewModels, Navigation, Theme System for Dark Mode support, and Error Handling UI
73: 2. **Shared Module**:
74:    - **Domain Layer**: Models, Repository Interfaces, Use Cases, and Recommendation Engine
75:    - **Data Layer**: Repository Implementations, Remote/Local Data Sources, Caching System for Offline Mode, Network Monitor, and Error Handler
76:    - **Cross-Cutting Concerns**: Dependency Injection with Koin and App Configuration
77: 3. **Platform-Specific Implementations**: Android and iOS implementations
````

## File: docs/images/mermaid/package_diagram.md
````markdown
  1: # Package Diagram
  2: 
  3: ```mermaid
  4: graph TD
  5:     %% Main package
  6:     CocktailCraft[com.cocktailcraft]
  7: 
  8:     %% Android App
  9:     AndroidApp[androidApp]
 10:     JavaCocktailCraft[java.com.cocktailcraft]
 11: 
 12:     %% Screens
 13:     Screens[screens]
 14:     HomeScreen[HomeScreen]
 15:     CartScreen[CartScreen]
 16:     FavoritesScreen[FavoritesScreen]
 17:     ProfileScreen[ProfileScreen]
 18:     OrderListScreen[OrderListScreen]
 19:     CocktailDetailScreen[CocktailDetailScreen]
 20:     OfflineModeScreen[OfflineModeScreen]
 21: 
 22:     %% UI
 23:     UI[ui]
 24:     Components[components]
 25:     CartItemCard[CartItemCard]
 26:     CocktailItem[CocktailItem]
 27:     FilterChip[FilterChip]
 28:     RatingBar[RatingBar]
 29:     WriteReviewDialog[WriteReviewDialog]
 30:     ErrorDialog[ErrorDialog]
 31:     ErrorBanner[ErrorBanner]
 32: 
 33:     Main[main]
 34:     MainScreen[MainScreen]
 35: 
 36:     Theme[theme]
 37:     ThemeFile[Theme]
 38:     AnimatedTheme[AnimatedTheme]
 39:     AppColors[AppColors]
 40: 
 41:     %% ViewModel
 42:     ViewModel[viewmodel]
 43:     BaseViewModel[BaseViewModel]
 44:     HomeViewModel[HomeViewModel]
 45:     CartViewModel[CartViewModel]
 46:     FavoritesViewModel[FavoritesViewModel]
 47:     ProfileViewModel[ProfileViewModel]
 48:     OrderViewModel[OrderViewModel]
 49:     ReviewViewModel[ReviewViewModel]
 50:     ThemeViewModel[ThemeViewModel]
 51:     OfflineModeViewModel[OfflineModeViewModel]
 52:     CocktailDetailViewModel[CocktailDetailViewModel]
 53: 
 54:     %% Navigation
 55:     Navigation[navigation]
 56:     NavigationManager[NavigationManager]
 57:     Screen[Screen]
 58: 
 59:     %% Model
 60:     Model[model]
 61:     SortOption[SortOption]
 62:     UserPreferences[UserPreferences]
 63: 
 64:     %% Util
 65:     AppUtil[util]
 66:     ErrorUtils[ErrorUtils]
 67: 
 68:     %% Main Activity
 69:     MainActivity[MainActivity]
 70:     CocktailCraftApplication[CocktailCraftApplication]
 71: 
 72:     %% Shared Module
 73:     Shared[shared]
 74:     CommonMain[commonMain.kotlin.com.cocktailcraft]
 75: 
 76:     %% Domain
 77:     Domain[domain]
 78:     DomainModel[model]
 79:     Cocktail[Cocktail]
 80:     CocktailCartItem[CocktailCartItem]
 81:     CocktailIngredient[CocktailIngredient]
 82:     Order[Order]
 83:     Review[Review]
 84:     User[User]
 85: 
 86:     DomainRepository[repository]
 87:     AuthRepository[AuthRepository]
 88:     CartRepository[CartRepository]
 89:     CocktailRepository[CocktailRepository]
 90:     OrderRepository[OrderRepository]
 91:     FavoritesRepository[FavoritesRepository]
 92: 
 93:     UseCase[usecase]
 94:     PlaceOrderUseCase[PlaceOrderUseCase]
 95:     ToggleFavoriteUseCase[ToggleFavoriteUseCase]
 96: 
 97:     Recommendation[recommendation]
 98:     CocktailRecommendationEngine[CocktailRecommendationEngine]
 99: 
100:     Config[config]
101:     AppConfig[AppConfig]
102: 
103:     Util[util]
104:     Result[Result]
105:     NetworkMonitor[NetworkMonitor]
106: 
107:     %% Data
108:     Data[data]
109:     DataRepository[repository]
110:     AuthRepositoryImpl[AuthRepositoryImpl]
111:     CartRepositoryImpl[CartRepositoryImpl]
112:     CocktailRepositoryImpl[CocktailRepositoryImpl]
113:     OrderRepositoryImpl[OrderRepositoryImpl]
114:     FavoritesRepositoryImpl[FavoritesRepositoryImpl]
115: 
116:     Cache[cache]
117:     CocktailCache[CocktailCache]
118: 
119:     Remote[remote]
120:     CocktailApi[CocktailApi]
121:     CocktailApiImpl[CocktailApiImpl]
122:     CocktailDto[CocktailDto]
123: 
124:     DataConfig[config]
125:     AppConfigImpl[AppConfigImpl]
126: 
127:     %% DI
128:     DI[di]
129:     AppModule[AppModule]
130:     DataModule[DataModule]
131:     DomainModule[DomainModule]
132:     NetworkModule[NetworkModule]
133:     PlatformModule[PlatformModule]
134:     RecommendationModule[RecommendationModule]
135: 
136:     %% Platform-specific
137:     AndroidMain[androidMain.kotlin.com.cocktailcraft]
138:     AndroidDI[di]
139:     AndroidPlatformModule[PlatformModule]
140:     AndroidNetworkMonitor[util.NetworkMonitor]
141: 
142:     IOSMain[iosMain.kotlin.com.cocktailcraft]
143:     IOSDI[di]
144:     IOSPlatformModule[PlatformModule]
145:     IOSNetworkMonitor[util.NetworkMonitor]
146: 
147:     %% Package Hierarchy
148:     CocktailCraft --> AndroidApp
149:     CocktailCraft --> Shared
150: 
151:     AndroidApp --> JavaCocktailCraft
152: 
153:     JavaCocktailCraft --> Screens
154:     JavaCocktailCraft --> UI
155:     JavaCocktailCraft --> ViewModel
156:     JavaCocktailCraft --> Navigation
157:     JavaCocktailCraft --> Model
158:     JavaCocktailCraft --> AppUtil
159:     JavaCocktailCraft --> MainActivity
160:     JavaCocktailCraft --> CocktailCraftApplication
161: 
162:     Screens --> HomeScreen
163:     Screens --> CartScreen
164:     Screens --> FavoritesScreen
165:     Screens --> ProfileScreen
166:     Screens --> OrderListScreen
167:     Screens --> CocktailDetailScreen
168:     Screens --> OfflineModeScreen
169: 
170:     UI --> Components
171:     UI --> Main
172:     UI --> Theme
173: 
174:     Components --> CartItemCard
175:     Components --> CocktailItem
176:     Components --> FilterChip
177:     Components --> RatingBar
178:     Components --> WriteReviewDialog
179:     Components --> ErrorDialog
180:     Components --> ErrorBanner
181: 
182:     Main --> MainScreen
183: 
184:     Theme --> ThemeFile
185:     Theme --> AnimatedTheme
186:     Theme --> AppColors
187: 
188:     AppUtil --> ErrorUtils
189: 
190:     ViewModel --> BaseViewModel
191:     ViewModel --> HomeViewModel
192:     ViewModel --> CartViewModel
193:     ViewModel --> FavoritesViewModel
194:     ViewModel --> ProfileViewModel
195:     ViewModel --> OrderViewModel
196:     ViewModel --> ReviewViewModel
197:     ViewModel --> ThemeViewModel
198:     ViewModel --> OfflineModeViewModel
199:     ViewModel --> CocktailDetailViewModel
200: 
201:     Navigation --> NavigationManager
202:     Navigation --> Screen
203: 
204:     Model --> SortOption
205:     Model --> UserPreferences
206: 
207:     Shared --> CommonMain
208:     Shared --> AndroidMain
209:     Shared --> IOSMain
210: 
211:     CommonMain --> Domain
212:     CommonMain --> Data
213:     CommonMain --> DI
214: 
215:     Domain --> DomainModel
216:     Domain --> DomainRepository
217:     Domain --> UseCase
218:     Domain --> Recommendation
219:     Domain --> Config
220:     Domain --> Util
221: 
222:     DomainModel --> Cocktail
223:     DomainModel --> CocktailCartItem
224:     DomainModel --> CocktailIngredient
225:     DomainModel --> Order
226:     DomainModel --> Review
227:     DomainModel --> User
228: 
229:     DomainRepository --> AuthRepository
230:     DomainRepository --> CartRepository
231:     DomainRepository --> CocktailRepository
232:     DomainRepository --> OrderRepository
233:     DomainRepository --> FavoritesRepository
234: 
235:     UseCase --> PlaceOrderUseCase
236:     UseCase --> ToggleFavoriteUseCase
237: 
238:     Recommendation --> CocktailRecommendationEngine
239: 
240:     Config --> AppConfig
241: 
242:     Util --> Result
243:     Util --> NetworkMonitor
244: 
245:     Data --> DataRepository
246:     Data --> Remote
247:     Data --> Cache
248:     Data --> DataConfig
249: 
250:     DataRepository --> AuthRepositoryImpl
251:     DataRepository --> CartRepositoryImpl
252:     DataRepository --> CocktailRepositoryImpl
253:     DataRepository --> OrderRepositoryImpl
254:     DataRepository --> FavoritesRepositoryImpl
255: 
256:     Cache --> CocktailCache
257: 
258:     Remote --> CocktailApi
259:     Remote --> CocktailApiImpl
260:     Remote --> CocktailDto
261: 
262:     DataConfig --> AppConfigImpl
263: 
264:     DI --> AppModule
265:     DI --> DataModule
266:     DI --> DomainModule
267:     DI --> NetworkModule
268:     DI --> PlatformModule
269:     DI --> RecommendationModule
270: 
271:     AndroidMain --> AndroidDI
272:     AndroidMain --> AndroidNetworkMonitor
273:     AndroidDI --> AndroidPlatformModule
274: 
275:     IOSMain --> IOSDI
276:     IOSMain --> IOSNetworkMonitor
277:     IOSDI --> IOSPlatformModule
278: 
279:     %% Dependencies between packages
280:     Screens --> ViewModel
281:     Screens --> Navigation
282:     Screens --> UI
283: 
284:     Main --> Screens
285:     Main --> Navigation
286:     Main --> ViewModel
287: 
288:     BaseViewModel --> ErrorUtils
289:     HomeViewModel --> BaseViewModel
290:     CartViewModel --> BaseViewModel
291:     FavoritesViewModel --> BaseViewModel
292:     ProfileViewModel --> BaseViewModel
293:     OrderViewModel --> BaseViewModel
294:     ReviewViewModel --> BaseViewModel
295:     ThemeViewModel --> BaseViewModel
296:     OfflineModeViewModel --> BaseViewModel
297:     CocktailDetailViewModel --> BaseViewModel
298: 
299:     ViewModel --> DomainRepository
300:     ViewModel --> UseCase
301:     ViewModel --> DomainModel
302:     ViewModel --> Recommendation
303:     ViewModel --> NetworkMonitor
304: 
305:     ThemeViewModel --> UserPreferences
306:     OfflineModeViewModel --> NetworkMonitor
307: 
308:     UseCase --> DomainRepository
309:     UseCase --> DomainModel
310: 
311:     CocktailRecommendationEngine --> CocktailRepository
312:     CocktailRecommendationEngine --> FavoritesRepository
313: 
314:     DataRepository --> DomainRepository
315:     DataRepository --> DomainModel
316:     DataRepository --> Remote
317:     DataRepository --> Cache
318:     DataRepository --> NetworkMonitor
319: 
320:     CocktailCache --> Cocktail
321: 
322:     DataConfig --> Config
323: 
324:     AppModule --> DataModule
325:     AppModule --> DomainModule
326:     AppModule --> NetworkModule
327: 
328:     DataModule --> DataRepository
329:     DataModule --> Remote
330:     DataModule --> Cache
331: 
332:     DomainModule --> UseCase
333:     DomainModule --> Config
334: 
335:     NetworkModule --> Remote
336:     NetworkModule --> NetworkMonitor
337: 
338:     RecommendationModule --> Recommendation
339:     RecommendationModule --> CocktailDetailViewModel
340: 
341:     CocktailCraftApplication --> DI
342: ```
343: 
344: This package diagram shows the organization of the CocktailCraft codebase, including:
345: 
346: 1. **Android App**: UI components, ViewModels, Navigation, and Utilities
347:    - New screens for Offline Mode
348:    - Error handling components
349:    - Animated theme components for Dark Mode
350:    - Base ViewModel with error handling
351:    - Theme and Offline Mode ViewModels
352: 
353: 2. **Shared Module**: Domain and Data layers
354:    - Domain Layer: Models, Repository Interfaces, Use Cases, and Recommendation Engine
355:    - Data Layer: Repository Implementations, Remote Data Sources, Caching System, and Network Monitor
356:    - Cross-Cutting Concerns: Dependency Injection with modular Koin setup
357: 
358: 3. **Platform-Specific**: Android and iOS implementations of platform-specific features like Network Monitoring
359: 
360: The diagram shows the relationships and dependencies between these packages, highlighting the modular architecture of the application.
````

## File: docs/images/mermaid/README.md
````markdown
 1: # CocktailCraft Architecture Diagrams
 2: 
 3: This directory contains various architecture diagrams for our CocktailCraft application. These diagrams provide a visual representation of different aspects of our system's architecture.
 4: 
 5: ## Available Diagrams
 6: 
 7: ### 1. [High-Level Architecture Diagram](high_level_architecture.md)
 8: 
 9: I've created this diagram to provide an overview of our application's architecture, showing the main layers and their relationships:
10: - Android App (UI Layer, ViewModels, Navigation)
11: - Shared Module (Domain Layer, Data Layer)
12: - Platform-Specific Implementations
13: 
14: ### 2. [Component Diagram](component_diagram.md)
15: 
16: I've designed this diagram to show the main components of our application and how they interact with each other:
17: - UI Components (Screens, UI Elements)
18: - ViewModels
19: - Domain Layer (Repositories, Use Cases, Models)
20: - Data Layer (Repository Implementations, API)
21: - Dependency Injection
22: 
23: ### 3. [Use Case Diagram](use_case_diagram.md)
24: 
25: I've illustrated the main user interactions with our system:
26: - Browse and search cocktails
27: - Cart management
28: - Favorites management
29: - Order management
30: - Profile management
31: - Review management
32: 
33: ### 4. [Class Diagram](class_diagram.md)
34: 
35: I've created this diagram to show the key domain models and their relationships:
36: - Domain Models (Cocktail, Order, User, etc.)
37: - Repository Interfaces
38: - Use Cases
39: 
40: ### 5. [Sequence Diagram - Place Order](sequence_diagram_place_order.md)
41: 
42: I've designed this diagram to illustrate the sequence of interactions when a user places an order:
43: - User interaction with UI
44: - ViewModel processing
45: - Use Case execution
46: - Repository operations
47: - Data storage
48: 
49: ### 6. [Data Flow Diagram](data_flow_diagram.md)
50: 
51: I've created this diagram to show how data flows through our application:
52: - User input and UI updates
53: - Data flow between layers
54: - External API interactions
55: - Local storage operations
56: 
57: ### 7. [Package Diagram](package_diagram.md)
58: 
59: I've designed this diagram to show the organization of our codebase:
60: - Package structure
61: - Dependencies between packages
62: - Module organization
63: 
64: 
65: ## Architecture Overview
66: 
67: CocktailCraft follows Clean Architecture principles with a MVVM pattern for the UI layer:
68: 
69: 1. **UI Layer**:
70:    - Jetpack Compose UI components
71:    - Screen composables
72:    - Navigation
73: 
74: 2. **ViewModel Layer**:
75:    - Manages UI state
76:    - Handles user actions
77:    - Communicates with domain layer
78: 
79: 3. **Domain Layer**:
80:    - Business logic
81:    - Use cases
82:    - Repository interfaces
83:    - Domain models
84: 
85: 4. **Data Layer**:
86:    - Repository implementations
87:    - Remote data sources (API)
88:    - Local data sources (Storage)
89:    - DTOs and mappers
90: 
91: 5. **Platform-Specific**:
92:    - Android-specific implementations
93:    - iOS-specific implementations
````

## File: docs/images/mermaid/sequence_diagram_place_order.md
````markdown
  1: # Sequence Diagram - Place Order
  2: 
  3: ```mermaid
  4: sequenceDiagram
  5:     actor User
  6:     participant UI as CartScreen
  7:     participant ErrorUI as ErrorDialog
  8:     participant VM as CartViewModel
  9:     participant BaseVM as BaseViewModel
 10:     participant Network as NetworkMonitor
 11:     participant UC as PlaceOrderUseCase
 12:     participant Repo as OrderRepository
 13:     participant Cache as CocktailCache
 14:     participant Storage as LocalStorage
 15:     participant ErrorUtils as ErrorUtils
 16: 
 17:     %% Check Network Status
 18:     Note over User, ErrorUtils: Check Network Status
 19:     VM->>Network: isOnline.collect()
 20:     activate Network
 21:     Network-->>VM: Network status (online/offline)
 22:     deactivate Network
 23: 
 24:     %% View Cart
 25:     Note over User, ErrorUtils: View Cart
 26:     User->>UI: View cart
 27:     activate UI
 28:     UI->>VM: loadCartItems()
 29:     activate VM
 30:     VM->>VM: setLoading(true)
 31:     VM->>Repo: getCartItems()
 32:     activate Repo
 33:     Repo->>Storage: Read cart items
 34:     activate Storage
 35:     Storage-->>Repo: Return cart items
 36:     deactivate Storage
 37:     Repo-->>VM: Return cart items
 38:     deactivate Repo
 39:     VM->>Repo: getCartTotal()
 40:     activate Repo
 41:     Repo->>Storage: Calculate total
 42:     activate Storage
 43:     Storage-->>Repo: Return total
 44:     deactivate Storage
 45:     Repo-->>VM: Return total
 46:     deactivate Repo
 47:     VM->>VM: setLoading(false)
 48:     VM-->>UI: Update UI with cart items and total
 49:     deactivate VM
 50:     UI-->>User: Display cart items and total
 51:     deactivate UI
 52: 
 53:     %% Place Order - Success Path
 54:     Note over User, ErrorUtils: Place Order - Success Path
 55:     User->>UI: Click "Place Order"
 56:     activate UI
 57:     UI->>VM: placeOrder()
 58:     activate VM
 59:     VM->>BaseVM: executeWithErrorHandling()
 60:     activate BaseVM
 61:     BaseVM->>BaseVM: setLoading(true)
 62:     BaseVM->>UC: invoke(cartItems, totalPrice)
 63:     activate UC
 64:     UC->>UC: Create Order object
 65:     UC->>Repo: addOrder(order)
 66:     activate Repo
 67:     Repo->>Storage: Save order
 68:     activate Storage
 69:     Storage-->>Repo: Confirm save
 70:     deactivate Storage
 71:     Repo-->>UC: Return success
 72:     deactivate Repo
 73:     UC-->>BaseVM: Return Result.Success(order)
 74:     deactivate UC
 75:     BaseVM->>Repo: clearCart()
 76:     activate Repo
 77:     Repo->>Storage: Clear cart items
 78:     activate Storage
 79:     Storage-->>Repo: Confirm clear
 80:     deactivate Storage
 81:     Repo-->>BaseVM: Return success
 82:     deactivate Repo
 83:     BaseVM->>BaseVM: setLoading(false)
 84:     BaseVM-->>VM: Order placed successfully
 85:     deactivate BaseVM
 86:     VM-->>UI: Order placed successfully
 87:     deactivate VM
 88:     UI-->>User: Show order confirmation
 89:     deactivate UI
 90: 
 91:     %% Place Order - Error Path
 92:     Note over User, ErrorUtils: Place Order - Error Path
 93:     User->>UI: Click "Place Order" (with network error)
 94:     activate UI
 95:     UI->>VM: placeOrder()
 96:     activate VM
 97:     VM->>BaseVM: executeWithErrorHandling()
 98:     activate BaseVM
 99:     BaseVM->>BaseVM: setLoading(true)
100:     BaseVM->>UC: invoke(cartItems, totalPrice)
101:     activate UC
102:     UC->>UC: Create Order object
103:     UC->>Repo: addOrder(order)
104:     activate Repo
105:     Repo--xRepo: Network error occurs
106:     Repo-->>UC: Throw exception
107:     deactivate Repo
108:     UC-->>BaseVM: Throw exception
109:     deactivate UC
110:     BaseVM->>ErrorUtils: getErrorFromException()
111:     activate ErrorUtils
112:     ErrorUtils-->>BaseVM: Return UserFriendlyError
113:     deactivate ErrorUtils
114:     BaseVM->>BaseVM: setError(userFriendlyError)
115:     BaseVM->>BaseVM: setLoading(false)
116:     BaseVM-->>VM: Error event
117:     deactivate BaseVM
118:     VM-->>UI: Show error
119:     deactivate VM
120:     UI->>ErrorUI: Show error dialog
121:     activate ErrorUI
122:     ErrorUI-->>User: Display error with retry option
123:     User->>ErrorUI: Click "Retry"
124:     ErrorUI->>UI: Retry action
125:     deactivate ErrorUI
126:     UI->>VM: placeOrder() (retry)
127:     deactivate UI
128: 
129:     %% Navigate to Order History
130:     Note over User, ErrorUtils: Navigate to Order History
131:     User->>UI: Click "View Orders"
132:     activate UI
133:     UI->>NavigationManager: navigateToOrderList()
134:     NavigationManager->>OrderListScreen: Navigate
135:     OrderListScreen-->>User: Display order history
136:     deactivate UI
137: ```
138: 
139: This sequence diagram illustrates the flow of interactions when a user places an order in the CocktailCraft application, including:
140: 
141: 1. **Network Status Check**: Monitoring network connectivity before operations
142: 2. **View Cart**: Loading and displaying cart items with loading state management
143: 3. **Place Order - Success Path**: The happy path when placing an order succeeds
144:    - Using BaseViewModel's error handling wrapper
145:    - Managing loading state
146:    - Clearing cart after successful order
147: 4. **Place Order - Error Path**: The error handling path when network issues occur
148:    - Error conversion to user-friendly format
149:    - Displaying error dialog with retry option
150:    - Retry flow
151: 5. **Navigation**: Moving to order history after placing an order
152: 
153: The diagram shows how the application handles both successful operations and error scenarios with proper error handling and user feedback.
````

## File: docs/images/mermaid/use_case_diagram.md
````markdown
  1: # Use Case Diagram
  2: 
  3: ```mermaid
  4: graph LR
  5:     User((User))
  6:     System((System))
  7: 
  8:     subgraph CocktailCraft["CocktailCraft Application"]
  9:         %% Home Screen Use Cases
 10:         UC1[Browse Cocktails]
 11:         UC2[Search Cocktails]
 12:         UC3[Filter Cocktails]
 13:         UC4[Sort Cocktails]
 14:         UC5[View Cocktail Details]
 15: 
 16:         %% Cart Use Cases
 17:         UC6[Add to Cart]
 18:         UC7[View Cart]
 19:         UC8[Update Quantity]
 20:         UC9[Remove from Cart]
 21:         UC10[Place Order]
 22: 
 23:         %% Favorites Use Cases
 24:         UC11[Add to Favorites]
 25:         UC12[View Favorites]
 26:         UC13[Remove from Favorites]
 27: 
 28:         %% Order Use Cases
 29:         UC14[View Order History]
 30:         UC15[View Order Details]
 31: 
 32:         %% Profile Use Cases
 33:         UC16[View Profile]
 34:         UC17[Edit Profile]
 35:         UC18[Login/Logout]
 36: 
 37:         %% Review Use Cases
 38:         UC19[Write Review]
 39:         UC20[View Reviews]
 40: 
 41:         %% Theme Use Cases
 42:         UC21[Toggle Dark Mode]
 43:         UC22[Toggle System Theme]
 44: 
 45:         %% Offline Mode Use Cases
 46:         UC23[Enable/Disable Offline Mode]
 47:         UC24[View Cached Cocktails]
 48:         UC25[Clear Cache]
 49: 
 50:         %% Recommendation Use Cases
 51:         UC26[View Similar Cocktails]
 52:         UC27[Get Personalized Recommendations]
 53: 
 54:         %% Error Handling Use Cases
 55:         UC28[Handle Network Errors]
 56:         UC29[Retry Failed Operations]
 57:     end
 58: 
 59:     %% User Associations
 60:     User --> UC1
 61:     User --> UC2
 62:     User --> UC3
 63:     User --> UC4
 64:     User --> UC5
 65:     User --> UC6
 66:     User --> UC7
 67:     User --> UC8
 68:     User --> UC9
 69:     User --> UC10
 70:     User --> UC11
 71:     User --> UC12
 72:     User --> UC13
 73:     User --> UC14
 74:     User --> UC15
 75:     User --> UC16
 76:     User --> UC17
 77:     User --> UC18
 78:     User --> UC19
 79:     User --> UC20
 80:     User --> UC21
 81:     User --> UC22
 82:     User --> UC23
 83:     User --> UC24
 84:     User --> UC25
 85:     User --> UC26
 86:     User --> UC29
 87: 
 88:     %% System Associations
 89:     System --> UC27
 90:     System --> UC28
 91: 
 92:     %% Include/Extend Relationships
 93:     UC5 --> UC6
 94:     UC5 --> UC11
 95:     UC5 --> UC19
 96:     UC5 --> UC20
 97:     UC5 --> UC26
 98:     UC7 --> UC8
 99:     UC7 --> UC9
100:     UC7 --> UC10
101:     UC10 --> UC7
102:     UC12 --> UC13
103:     UC15 --> UC14
104:     UC16 --> UC21
105:     UC16 --> UC22
106:     UC1 --> UC28
107:     UC2 --> UC28
108:     UC3 --> UC28
109:     UC5 --> UC28
110:     UC10 --> UC28
111:     UC28 --> UC29
112:     UC1 --> UC23
113:     UC1 --> UC24
114:     UC24 --> UC25
115: ```
116: 
117: This diagram illustrates the main user interactions with the CocktailCraft system, including:
118: 
119: 1. **Core Functionality**: Browsing cocktails, searching, filtering, viewing details, managing cart, placing orders, managing favorites, and writing reviews
120: 2. **Theme Management**: Toggling dark mode and system theme integration
121: 3. **Offline Mode**: Enabling/disabling offline mode, viewing cached cocktails, and clearing cache
122: 4. **Recommendations**: Viewing similar cocktails and getting personalized recommendations
123: 5. **Error Handling**: Handling network errors and retrying failed operations
124: 
125: The diagram shows both user-initiated use cases and system-initiated use cases, as well as the relationships between them.
````

## File: docs/AdvancedSearch.md
````markdown
  1: # Advanced Search and Filtering
  2: 
  3: This document describes the implementation of the Advanced Search and Filtering functionality in the CocktailCraft application.
  4: 
  5: ## Overview
  6: 
  7: The Advanced Search and Filtering system allows users to search for cocktails using multiple criteria simultaneously, providing a powerful way to discover cocktails that match specific preferences. The system supports filtering by various attributes including:
  8: 
  9: - Category
 10: - Ingredients (both inclusion and exclusion)
 11: - Alcoholic/Non-alcoholic
 12: - Glass type
 13: - Price range
 14: - Taste profile
 15: - Complexity level
 16: - Preparation time
 17: 
 18: ## Implementation Details
 19: 
 20: ### Key Components
 21: 
 22: 1. **SearchFilters Model**
 23:    - Located in `shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/SearchFilters.kt`
 24:    - Represents all possible filter criteria as a data class
 25:    - Includes helper methods for checking active filters and generating descriptions
 26: 
 27: 2. **AdvancedSearchPanel**
 28:    - Located in `androidApp/src/main/java/com/cocktailcraft/ui/components/AdvancedSearchPanel.kt`
 29:    - Provides a comprehensive UI for setting and applying multiple filters
 30:    - Implemented as a modal dialog with collapsible sections for each filter type
 31: 
 32: 3. **Filter Components**
 33:    - `FilterSection`: Collapsible section for organizing filter controls
 34:    - `CategorySelector`: Dropdown for selecting cocktail categories
 35:    - `IngredientSelector`: Multi-select interface for including/excluding ingredients
 36:    - `AlcoholicFilterContent`: Toggle switches for alcoholic/non-alcoholic options
 37:    - `GlassSelector`: Dropdown for selecting glass types
 38:    - `PriceRangeFilterContent`: Range slider for setting price boundaries
 39:    - `TasteProfileSelector`: Chip-based selector for taste profiles
 40:    - `ComplexitySelector`: Chip-based selector for complexity levels
 41:    - `PrepTimeSelector`: Chip-based selector for preparation times
 42: 
 43: 4. **SearchFilterChips**
 44:    - Located in `androidApp/src/main/java/com/cocktailcraft/ui/components/SearchFilterChips.kt`
 45:    - Displays active filters as chips for quick visibility and removal
 46:    - Provides a "Clear All" option to reset all filters
 47: 
 48: ### Filter Types
 49: 
 50: 1. **Category Filter**
 51:    - Allows filtering by cocktail category (e.g., "Cocktail", "Shot", "Ordinary Drink")
 52:    - Implemented as a dropdown selector with "All Categories" option
 53: 
 54: 2. **Ingredient Filter**
 55:    - Supports both inclusion and exclusion of ingredients
 56:    - Allows multiple ingredients to be selected for each
 57:    - Provides a searchable dialog for finding specific ingredients
 58: 
 59: 3. **Alcoholic Filter**
 60:    - Toggle switches for showing only alcoholic or non-alcoholic drinks
 61:    - Mutually exclusive options with ability to clear both
 62: 
 63: 4. **Glass Type Filter**
 64:    - Dropdown selector for filtering by glass type
 65:    - Includes "All Glass Types" option to clear the filter
 66: 
 67: 5. **Price Range Filter**
 68:    - Range slider for setting minimum and maximum price
 69:    - Toggle to enable/disable price filtering
 70: 
 71: 6. **Taste Profile Filter**
 72:    - Chip-based selector for taste profiles (Sweet, Sour, Bitter, etc.)
 73:    - Based on the `TasteProfile` enum
 74: 
 75: 7. **Complexity Filter**
 76:    - Chip-based selector for complexity levels (Easy, Medium, Complex)
 77:    - Based on the `Complexity` enum
 78: 
 79: 8. **Preparation Time Filter**
 80:    - Chip-based selector for preparation times (Quick, Medium, Long)
 81:    - Based on the `PreparationTime` enum
 82: 
 83: ### Data Flow
 84: 
 85: 1. User opens the Advanced Search panel from the main search interface
 86: 2. User selects desired filters across multiple categories
 87: 3. On "Apply Filters", the selected criteria are combined into a `SearchFilters` object
 88: 4. The `SearchFilters` object is passed to the repository layer via the ViewModel
 89: 5. The repository applies the filters to retrieve matching cocktails
 90: 6. Results are displayed to the user with active filters shown as chips
 91: 
 92: ### Repository Implementation
 93: 
 94: The `CocktailRepositoryImpl` class handles the application of filters:
 95: 
 96: 1. Starts with a base list of cocktails (from search, category, or all cocktails)
 97: 2. Applies each filter sequentially to narrow down results
 98: 3. For complex filters like taste profile, uses heuristics based on ingredients and descriptions
 99: 4. Returns a filtered list of cocktails that match all criteria
100: 
101: ## Usage Example
102: 
103: ```kotlin
104: // Create a SearchFilters object with multiple criteria
105: val filters = SearchFilters(
106:     query = "Margarita",
107:     category = "Cocktail",
108:     ingredients = listOf("Tequila", "Lime Juice"),
109:     excludeIngredients = listOf("Salt"),
110:     alcoholic = true,
111:     tasteProfile = TasteProfile.SOUR,
112:     complexity = Complexity.MEDIUM
113: )
114: 
115: // Apply filters through the ViewModel
116: viewModel.applyFilters(filters)
117: 
118: // The UI will update to show matching cocktails
119: ```
120: 
121: ## UI Components
122: 
123: The Advanced Search UI is designed for usability and clarity:
124: 
125: - **Collapsible Sections**: Each filter type has its own collapsible section
126: - **Visual Indicators**: Active filters are highlighted and summarized
127: - **Chip-Based Selection**: Where appropriate, filters use chips for easy selection
128: - **Searchable Dialogs**: For large lists like ingredients, searchable dialogs are provided
129: - **Clear Options**: Each filter can be individually cleared, and a "Clear All" option is available
130: 
131: ## Future Enhancements
132: 
133: 1. **Saved Filters**: Allow users to save and name their favorite filter combinations
134: 2. **Filter Suggestions**: Suggest popular filter combinations based on user preferences
135: 3. **Visual Filter Builder**: Provide a more visual interface for building complex filter combinations
136: 4. **Filter Analytics**: Track which filters are most commonly used to improve the interface
137: 5. **Voice Search Integration**: Allow setting filters via voice commands
138: 
139: ## Technical Considerations
140: 
141: 1. **Performance**: Filters are applied efficiently to minimize processing time
142: 2. **Caching**: Frequently used filter results are cached to improve performance
143: 3. **Offline Support**: Basic filtering works offline with cached cocktail data
144: 4. **Extensibility**: The filter system is designed to be easily extended with new filter types
````

## File: docs/animations.md
````markdown
  1: # Animations and Transitions in CocktailCraft
  2: 
  3: This document outlines the animations and transitions implemented in the CocktailCraft app to enhance the user experience.
  4: 
  5: ## Animation Types
  6: 
  7: ### 1. UI Component Animations
  8: 
  9: #### Button Animations
 10: - Scale animations on press/release for buttons
 11: - Custom animated icon buttons with scale effects
 12: - Animated floating action buttons
 13: 
 14: ```kotlin
 15: // Example usage
 16: AnimatedButton(
 17:     text = "Add to Cart",
 18:     onClick = { /* action */ }
 19: )
 20: 
 21: AnimatedIconButton(
 22:     onClick = { /* action */ },
 23:     icon = Icons.Default.Favorite,
 24:     contentDescription = "Add to favorites"
 25: )
 26: ```
 27: 
 28: #### List Item Animations
 29: - Staggered entry animations for list items
 30: - Scale animations on hover
 31: - Coordinated animations for multiple elements
 32: - Batched loading with smooth animations
 33: 
 34: ```kotlin
 35: // Example usage of animated item
 36: AnimatedCocktailItem(
 37:     cocktail = cocktail,
 38:     onClick = { /* action */ },
 39:     onAddToCart = { /* action */ },
 40:     isFavorite = true,
 41:     onToggleFavorite = { /* action */ },
 42:     index = index // For staggered animation
 43: )
 44: 
 45: // Example of batched loading implementation
 46: val visibleItemsCount = remember { mutableStateOf(0) }
 47: 
 48: // Update visible items based on scroll position
 49: LaunchedEffect(listState.firstVisibleItemIndex) {
 50:     val targetVisible = minOf(
 51:         items.size,
 52:         listState.firstVisibleItemIndex + 12 // Current visible + 3 batches ahead
 53:     )
 54: 
 55:     if (targetVisible > visibleItemsCount.value) {
 56:         // Animate in batches of 3 items
 57:         val batchSize = 3
 58:         val currentBatch = visibleItemsCount.value / batchSize
 59:         val targetBatch = targetVisible / batchSize
 60: 
 61:         for (batch in currentBatch until targetBatch) {
 62:             val newCount = minOf((batch + 1) * batchSize, items.size)
 63:             visibleItemsCount.value = newCount
 64:             delay(100) // Small delay between batches
 65:         }
 66:     }
 67: }
 68: ```
 69: 
 70: ### 2. Loading State Animations
 71: 
 72: #### Shimmer Loading Effects
 73: - Shimmer loading placeholders for cocktail items
 74: - Shimmer loading placeholders for detail screens
 75: - Custom shimmer effect that can be applied to any component
 76: 
 77: ```kotlin
 78: // Apply shimmer effect to any component
 79: Box(
 80:     modifier = Modifier
 81:         .size(100.dp)
 82:         .shimmerEffect()
 83: )
 84: 
 85: // Use pre-built shimmer components
 86: CocktailItemShimmer()
 87: CocktailDetailShimmer()
 88: ```
 89: 
 90: ### 3. Transition Animations
 91: 
 92: #### Screen Transitions
 93: - Fade and slide animations for navigation
 94: - Coordinated enter/exit animations
 95: 
 96: ```kotlin
 97: // Example from NavHost configuration
 98: NavHost(
 99:     navController = navController,
100:     startDestination = Screen.Home.route,
101:     enterTransition = {
102:         slideIntoContainer(
103:             towards = AnimatedContentTransitionScope.SlideDirection.Left,
104:             animationSpec = tween(300)
105:         ) + fadeIn(animationSpec = tween(300))
106:     },
107:     exitTransition = {
108:         slideOutOfContainer(
109:             towards = AnimatedContentTransitionScope.SlideDirection.Left,
110:             animationSpec = tween(300)
111:         ) + fadeOut(animationSpec = tween(300))
112:     }
113: )
114: ```
115: 
116: #### Theme Transitions
117: - Smooth color transitions when switching between light and dark modes
118: - Animated theme switch with custom effects
119: 
120: ```kotlin
121: // Example usage
122: AnimatedCocktailBarTheme(
123:     darkTheme = isDarkMode,
124:     content = { /* app content */ }
125: )
126: 
127: AnimatedThemeSwitch(
128:     isDarkMode = isDarkMode,
129:     onToggle = { isDarkMode = !isDarkMode }
130: )
131: ```
132: 
133: ## Animation Utilities
134: 
135: The app includes a set of reusable animation utilities in the `AnimationUtils` class:
136: 
137: ```kotlin
138: // Example utilities
139: val fadeInMedium = fadeIn(
140:     animationSpec = tween(
141:         durationMillis = ANIMATION_DURATION_MEDIUM,
142:         easing = LinearOutSlowInEasing
143:     )
144: )
145: 
146: val scaleInMedium = scaleIn(
147:     animationSpec = tween(
148:         durationMillis = ANIMATION_DURATION_MEDIUM,
149:         easing = LinearOutSlowInEasing
150:     ),
151:     initialScale = 0.9f
152: )
153: 
154: // Combined animations
155: val enterWithFadeAndScale: EnterTransition = fadeInMedium + scaleInMedium
156: ```
157: 
158: ## Optimized Scrolling Performance
159: 
160: The app implements several techniques to ensure smooth scrolling with animations:
161: 
162: ### Batched Loading Mechanism
163: 
164: Instead of loading and animating all items at once, the app uses a batched loading approach:
165: 
166: 1. **Visibility Tracking**: Maintains a count of how many items should be visible based on scroll position
167: 2. **Batch Processing**: Loads items in small batches (3 at a time) with slight delays between batches
168: 3. **Predictive Loading**: Preloads items that will soon be visible (3 batches ahead of current view)
169: 4. **Coordinated Animations**: Items within the same batch animate together for a cohesive effect
170: 
171: ### Animation Optimization Techniques
172: 
173: 1. **Direct Animation Properties**: Uses `animateFloatAsState` for alpha and offset animations
174: 2. **Simplified Rendering**: Optimizes rendering path for better performance during scrolling
175: 3. **Efficient Composables**: Extracts and reuses content to minimize recomposition
176: 4. **Minimal Layout Changes**: Avoids layout changes during animations to reduce jank
177: 
178: ```kotlin
179: // Example of optimized item rendering with animations
180: if (isVisible) {
181:     // Calculate animation parameters based on batch
182:     val delayMillis = batchIndex * 100
183:     val animationDuration = 300
184: 
185:     // Animate alpha and offset for entry
186:     val animatedAlpha by animateFloatAsState(
187:         targetValue = 1f,
188:         animationSpec = tween(
189:             durationMillis = animationDuration,
190:             delayMillis = delayMillis
191:         )
192:     )
193: 
194:     val animatedOffset by animateFloatAsState(
195:         targetValue = 0f,
196:         animationSpec = tween(
197:             durationMillis = animationDuration,
198:             delayMillis = delayMillis
199:         )
200:     )
201: 
202:     Box(
203:         modifier = Modifier
204:             .alpha(animatedAlpha)
205:             .offset(y = animatedOffset.dp)
206:     ) {
207:         // Render item content
208:     }
209: }
210: ```
211: 
212: ## Best Practices
213: 
214: 1. **Performance Considerations**
215:    - Use lightweight animations for frequently updated components
216:    - Implement batched loading for list animations
217:    - Use direct animation properties instead of complex composables for better performance
218:    - Consider disabling animations on low-end devices
219: 
220: 2. **Accessibility**
221:    - Provide options to reduce or disable animations for users with motion sensitivity
222:    - Ensure animations don't interfere with screen readers
223: 
224: 3. **Consistency**
225:    - Use consistent animation durations and easing curves throughout the app
226:    - Maintain a cohesive animation style that matches the app's design language
227: 
228: ## Implementation Guidelines
229: 
230: When adding new animations to the app:
231: 
232: 1. Consider whether the animation enhances or distracts from the user experience
233: 2. Use the provided animation utilities for consistency
234: 3. Test animations on both high-end and low-end devices
235: 4. Ensure animations work correctly in both light and dark modes
236: 5. Add appropriate documentation for complex animations
````

## File: docs/DependencyInjection.md
````markdown
  1: # Dependency Injection in CocktailCraft
  2: 
  3: This document outlines the dependency injection (DI) approach used in the CocktailCraft app.
  4: 
  5: ## Overview
  6: 
  7: CocktailCraft uses Koin for dependency injection. The DI setup has been organized to improve testability and separation of concerns.
  8: 
  9: ## Recent Improvements
 10: 
 11: The dependency injection system has been significantly improved with the following changes:
 12: 
 13: ### 1. Module Reorganization
 14: 
 15: We've refactored the single large `appModule` into smaller, focused modules:
 16: 
 17: - **NetworkModule**: Contains all network-related dependencies (HTTP client, API interfaces, network monitoring)
 18: - **DataModule**: Contains data-layer dependencies (repositories, caching)
 19: - **DomainModule**: Contains domain-layer dependencies (use cases, app configuration)
 20: 
 21: This modular approach provides several benefits:
 22: - Better separation of concerns
 23: - Easier maintenance
 24: - Improved testability
 25: - Clearer dependency boundaries
 26: - More focused modules with single responsibilities
 27: 
 28: ### 2. Testing Improvements
 29: 
 30: We've added dedicated testing support:
 31: 
 32: - **TestModule**: Provides mock implementations for testing
 33: - **BaseKoinTest**: A base class for tests that handles Koin setup and teardown
 34: - **Example Test**: Demonstrated how to use Koin for testing with `KoinThemeViewModelTest`
 35: 
 36: These improvements make it easier to write tests that use Koin for dependency injection, reducing boilerplate code and making tests more consistent.
 37: 
 38: ### 3. ViewModel Architecture
 39: 
 40: We've standardized the ViewModel architecture:
 41: 
 42: - **KoinViewModel**: A base class that implements `KoinComponent`
 43: - **BaseViewModel**: Extended to inherit from `KoinViewModel`
 44: - **Updated ViewModels**: Refactored to use the new base classes
 45: 
 46: This approach provides a consistent pattern for dependency injection in ViewModels, making the code more maintainable and easier to understand.
 47: 
 48: ## Module Structure
 49: 
 50: The DI system is organized into the following modules:
 51: 
 52: ### 1. Domain Module (`domainModule`)
 53: - Contains domain-level dependencies
 54: - Includes use cases and domain configurations
 55: - Located in `shared/src/commonMain/kotlin/com/cocktailcraft/di/DomainModule.kt`
 56: 
 57: ### 2. Data Module (`dataModule`)
 58: - Contains data-layer dependencies
 59: - Includes repositories and caching mechanisms
 60: - Located in `shared/src/commonMain/kotlin/com/cocktailcraft/di/DataModule.kt`
 61: 
 62: ### 3. Network Module (`networkModule`)
 63: - Contains network-related dependencies
 64: - Includes API clients, HTTP configuration, and network monitoring
 65: - Located in `shared/src/commonMain/kotlin/com/cocktailcraft/di/NetworkModule.kt`
 66: 
 67: ### 4. Platform Module (`platformModule`)
 68: - Contains platform-specific dependencies
 69: - Implemented differently for Android and iOS
 70: - Located in platform-specific source sets
 71: 
 72: ### 5. Test Module (`testModule`)
 73: - Contains mock implementations for testing
 74: - Used only in test environments
 75: - Located in `androidApp/src/test/java/com/cocktailcraft/di/TestModule.kt`
 76: 
 77: ## ViewModel Dependency Injection
 78: 
 79: ViewModels use a consistent pattern for dependency injection:
 80: 
 81: 1. All ViewModels extend `BaseViewModel` which extends `KoinViewModel`
 82: 2. `KoinViewModel` implements `KoinComponent` to enable Koin injection
 83: 3. ViewModels support both constructor injection (for testing) and Koin injection (for production)
 84: 
 85: Example:
 86: ```kotlin
 87: class ThemeViewModel(
 88:     private val authRepository: AuthRepository? = null
 89: ) : BaseViewModel() {
 90: 
 91:     // Use injected repository if not provided in constructor
 92:     private val injectedAuthRepository: AuthRepository by inject()
 93: 
 94:     // Use the provided repository or the injected one
 95:     private val repository: AuthRepository
 96:         get() = authRepository ?: injectedAuthRepository
 97: 
 98:     // ViewModel implementation...
 99: }
100: ```
101: 
102: ## Testing with Koin
103: 
104: For testing with Koin:
105: 
106: 1. Extend `BaseKoinTest` which handles Koin setup and teardown
107: 2. Use the `testModule` which provides mock implementations
108: 3. Inject dependencies using Koin or provide them via constructor
109: 
110: Example:
111: ```kotlin
112: class KoinThemeViewModelTest : BaseKoinTest() {
113: 
114:     private lateinit var viewModel: ThemeViewModel
115:     private val authRepository: AuthRepository by inject()
116: 
117:     @Before
118:     override fun setUp() {
119:         super.setUp() // Initialize Koin
120:         // Test setup...
121:     }
122: 
123:     @Test
124:     fun `test something`() {
125:         // Test implementation...
126:     }
127: }
128: ```
129: 
130: ## Implementation Details
131: 
132: ### Module Implementation
133: 
134: Each module is implemented as a Koin module with clear responsibilities:
135: 
136: ```kotlin
137: // NetworkModule.kt
138: val networkModule = module {
139:     // HTTP Client
140:     single {
141:         HttpClient {
142:             // HTTP client configuration...
143:         }
144:     }
145: 
146:     // API
147:     single<CocktailApi> { CocktailApiImpl(get()) }
148: 
149:     // Network monitoring
150:     single { NetworkMonitor(get()) }
151: }
152: 
153: // DataModule.kt
154: val dataModule = module {
155:     // JSON
156:     single {
157:         Json {
158:             ignoreUnknownKeys = true
159:             isLenient = true
160:         }
161:     }
162: 
163:     // Cache
164:     single { CocktailCache(get(), get(), get()) }
165: 
166:     // Repositories
167:     single<CocktailRepository> {
168:         CocktailRepositoryImpl(
169:             api = get(),
170:             settings = get(),
171:             appConfig = get(),
172:             json = get(),
173:             networkMonitor = get()
174:         )
175:     }
176:     // Other repositories...
177: }
178: 
179: // DomainModule.kt
180: val domainModule = module {
181:     // Config
182:     single<AppConfig> { AppConfigImpl() }
183: 
184:     // Use Cases
185:     factory { PlaceOrderUseCase(orderRepository = get()) }
186:     factory { ToggleFavoriteUseCase(cocktailRepository = get()) }
187: }
188: ```
189: 
190: ### ViewModel Base Classes
191: 
192: The ViewModel architecture uses a hierarchy of base classes:
193: 
194: ```kotlin
195: // KoinViewModel.kt
196: abstract class KoinViewModel : ViewModel(), KoinComponent
197: 
198: // BaseViewModel.kt
199: abstract class BaseViewModel : KoinViewModel() {
200:     // Common ViewModel functionality...
201: }
202: ```
203: 
204: ### Test Module Implementation
205: 
206: The test module provides mock implementations for testing:
207: 
208: ```kotlin
209: // TestModule.kt
210: val testModule = module {
211:     // Mock repositories
212:     single<CocktailRepository> { mock() }
213:     single<CartRepository> { mock() }
214:     single<AuthRepository> { mock() }
215:     single<OrderRepository> { mock() }
216: 
217:     // Mock use cases
218:     factory { PlaceOrderUseCase(orderRepository = get()) }
219:     factory { ToggleFavoriteUseCase(cocktailRepository = get()) }
220: 
221:     // Other mock dependencies...
222: }
223: ```
224: 
225: ## Best Practices
226: 
227: 1. **Separation of Concerns**: Keep modules focused on specific layers or functionality
228: 2. **Constructor Injection**: Prefer constructor injection for testing
229: 3. **Interface-Based Injection**: Inject interfaces rather than concrete implementations
230: 4. **Minimal Dependencies**: Keep dependencies minimal and focused
231: 5. **Documentation**: Document non-obvious dependencies and injection patterns
232: 6. **Module Organization**: Group related dependencies in the same module
233: 7. **Testability**: Design with testing in mind, making it easy to replace real implementations with mocks
234: 8. **Consistency**: Follow consistent patterns for dependency injection across the codebase
235: 
236: ## Benefits of the New Approach
237: 
238: The improved dependency injection setup provides several benefits:
239: 
240: 1. **Improved Testability**: Easier to mock dependencies for testing
241: 2. **Better Separation of Concerns**: Each module has a clear responsibility
242: 3. **Reduced Boilerplate**: Standardized patterns reduce repetitive code
243: 4. **Clearer Architecture**: Dependencies are more explicit and better organized
244: 5. **Easier Maintenance**: Smaller, focused modules are easier to maintain
245: 6. **Better Scalability**: Easier to add new features without modifying existing modules
246: 7. **Improved Readability**: Code is more organized and easier to understand
````

## File: docs/DependencyInjectionMigration.md
````markdown
  1: # Dependency Injection Migration Guide
  2: 
  3: This document provides guidance on migrating existing ViewModels and tests to use the new dependency injection approach.
  4: 
  5: ## Migrating ViewModels
  6: 
  7: ### Before
  8: 
  9: ```kotlin
 10: class ExampleViewModel(
 11:     private val repository: ExampleRepository? = null
 12: ) : ViewModel(), KoinComponent {
 13: 
 14:     // Use injected repository if not provided in constructor
 15:     private val injectedRepository: ExampleRepository by inject()
 16: 
 17:     // Use the provided repository or the injected one
 18:     private val repository: ExampleRepository
 19:         get() = repository ?: injectedRepository
 20:         
 21:     // ViewModel implementation...
 22: }
 23: ```
 24: 
 25: ### After
 26: 
 27: ```kotlin
 28: class ExampleViewModel(
 29:     private val repository: ExampleRepository? = null
 30: ) : BaseViewModel() {
 31: 
 32:     // Use injected repository if not provided in constructor
 33:     private val injectedRepository: ExampleRepository by inject()
 34: 
 35:     // Use the provided repository or the injected one
 36:     private val repository: ExampleRepository
 37:         get() = repository ?: injectedRepository
 38:         
 39:     // ViewModel implementation...
 40: }
 41: ```
 42: 
 43: ## Migrating Tests
 44: 
 45: ### Before
 46: 
 47: ```kotlin
 48: class ExampleViewModelTest {
 49:     
 50:     private lateinit var viewModel: ExampleViewModel
 51:     private val repository: ExampleRepository = mock()
 52:     private val testDispatcher = StandardTestDispatcher()
 53:     
 54:     @Before
 55:     fun setup() {
 56:         Dispatchers.setMain(testDispatcher)
 57:         viewModel = ExampleViewModel(repository)
 58:     }
 59:     
 60:     @After
 61:     fun tearDown() {
 62:         Dispatchers.resetMain()
 63:     }
 64:     
 65:     // Test methods...
 66: }
 67: ```
 68: 
 69: ### After
 70: 
 71: ```kotlin
 72: class ExampleViewModelTest : BaseKoinTest() {
 73:     
 74:     private lateinit var viewModel: ExampleViewModel
 75:     private val repository: ExampleRepository by inject()
 76:     private val testDispatcher = StandardTestDispatcher()
 77:     
 78:     @Before
 79:     override fun setUp() {
 80:         super.setUp() // Initialize Koin
 81:         Dispatchers.setMain(testDispatcher)
 82:         
 83:         // Configure mocks
 84:         whenever(repository.someMethod()).thenReturn(expectedValue)
 85:         
 86:         viewModel = ExampleViewModel()
 87:     }
 88:     
 89:     @After
 90:     override fun tearDown() {
 91:         Dispatchers.resetMain()
 92:         super.tearDown() // Clean up Koin
 93:     }
 94:     
 95:     // Test methods...
 96: }
 97: ```
 98: 
 99: ## Step-by-Step Migration Process
100: 
101: 1. **Update ViewModel Imports**:
102:    - Remove `import androidx.lifecycle.ViewModel`
103:    - Remove `import org.koin.core.component.KoinComponent`
104:    - Add `import org.koin.core.component.inject` if not already present
105: 
106: 2. **Update ViewModel Inheritance**:
107:    - Change `ViewModel(), KoinComponent` to `BaseViewModel()`
108: 
109: 3. **Update Tests**:
110:    - Make test class extend `BaseKoinTest`
111:    - Override `setUp()` and `tearDown()` methods
112:    - Call `super.setUp()` at the beginning of `setUp()`
113:    - Call `super.tearDown()` at the end of `tearDown()`
114:    - Replace manual mocking with Koin injection using `by inject()`
115:    - Configure mock behavior in `setUp()`
116: 
117: 4. **Verify**:
118:    - Run tests to ensure they still pass
119:    - Check for any missing dependencies or configuration issues
120: 
121: ## Common Issues and Solutions
122: 
123: ### Issue: Missing Dependencies in Tests
124: 
125: If tests fail with missing dependencies, ensure the `testModule` provides all required dependencies:
126: 
127: ```kotlin
128: // Add to TestModule.kt
129: single<MissingDependency> { mock() }
130: ```
131: 
132: ### Issue: Conflicting Modules
133: 
134: If you encounter conflicts between test modules and production modules:
135: 
136: ```kotlin
137: // In your test setup
138: stopKoin() // Ensure Koin is stopped before starting
139: startKoin {
140:     modules(testModule)
141: }
142: ```
143: 
144: ### Issue: ViewModel Not Using Mocked Dependencies
145: 
146: Ensure you're using the ViewModel constructor without parameters to force Koin injection:
147: 
148: ```kotlin
149: // Incorrect
150: viewModel = ExampleViewModel(repository) // Uses provided repository, not Koin
151: 
152: // Correct
153: viewModel = ExampleViewModel() // Uses Koin-injected repository
154: ```
````

## File: docs/Libraries.md
````markdown
 1: # CocktailCraft Libraries
 2: 
 3: This document provides detailed information about the libraries used in the CocktailCraft application.
 4: 
 5: ## Detailed Libraries Table
 6: 
 7: | Library | Version | Purpose |
 8: |---------|---------|---------|
 9: | **Core & Architecture** |  |  |
10: | Kotlin | 1.9.22 | Programming language for cross-platform development |
11: | Kotlin Coroutines | 1.7.3 | Asynchronous programming framework |
12: | Koin | 3.4.0 | Modular dependency injection framework |
13: | Ktor | 2.0.0 | HTTP client for API communication |
14: | Kotlinx Serialization | 1.6.0 | JSON/data serialization |
15: | Multiplatform Settings | 1.1.1 | Cross-platform settings/preferences storage |
16: | Kotlinx DateTime | 0.5.0 | Date and time handling |
17: | **UI & Navigation** |  |  |
18: | Jetpack Compose | 1.0.5 | Modern declarative UI toolkit |
19: | Compose Material3 | 1.0.0-alpha01 | Material Design 3 implementation for Compose |
20: | Compose BOM | 2023.01.00 | Bill of Materials for Compose dependencies |
21: | Activity Compose | 1.8.2 | Integration between Compose and Activities |
22: | Navigation Compose | 2.7.7 | Navigation framework for Compose |
23: | Accompanist | 0.30.0 | Utilities for Jetpack Compose (System UI Controller, Navigation Animation) |
24: | Coil | 2.4.0 | Image loading library for Android |
25: | Kamel | 0.3.0 | Multiplatform image loading library |
26: | **State Management** |  |  |
27: | Lifecycle ViewModel | 2.7.0 | Component to store and manage UI-related data |
28: | DataStore | 1.0.0 | Data storage solution (replaces SharedPreferences) |
29: | **Security** |  |  |
30: | Security Crypto | 1.1.0-alpha03 | Encryption and security utilities |
31: | **Testing** |  |  |
32: | JUnit | 4.13.2 | Unit testing framework |
33: | Mockito | 5.3.1 | Mocking framework for unit tests |
34: | Mockito Kotlin | 5.1.0 | Kotlin extensions for Mockito |
35: | Mockk | 1.13.8 | Kotlin-friendly mocking library |
36: | Turbine | 0.12.1 | Testing library for Kotlin Flow |
37: | Espresso | 3.5.0 | UI testing framework for Android |
38: | Navigation Testing | 2.7.7 | Testing utilities for Navigation component |
39: | **Dependency Injection** |  |  |
40: | Koin | 3.4.0 | Modular dependency injection framework with improved testability |
41: | Koin Test | 3.4.0 | Testing utilities for Koin dependency injection |
42: 
43: ## Important Notes
44: 
45: ### Compose Compiler Compatibility
46: The project uses Compose Compiler Extension version 1.5.8, which is compatible with Kotlin 1.9.22. When updating Kotlin versions, ensure the Compose Compiler version is also updated according to the [official compatibility table](https://developer.android.com/jetpack/androidx/releases/compose-kotlin).
47: 
48: ## Library Categories
49: 
50: ### Core & Architecture
51: These libraries form the foundation of the application architecture, providing essential functionality for cross-platform development, asynchronous programming, and data handling.
52: 
53: ### UI & Navigation
54: These libraries are responsible for the user interface, including the modern declarative UI toolkit Jetpack Compose, navigation between screens, and image loading.
55: 
56: ### State Management
57: These libraries help manage application state, including UI-related data and persistent storage.
58: 
59: ### Security
60: Security libraries provide encryption and other security utilities to protect sensitive data.
61: 
62: ### Testing
63: These libraries support various testing approaches, including unit testing, mocking, and UI testing.
64: 
65: ### Dependency Injection
66: Koin provides a lightweight and pragmatic dependency injection framework that helps maintain clean architecture and testability.
````

## File: docs/README.md
````markdown
  1: # CocktailCraft Documentation
  2: 
  3: This directory contains documentation for the CocktailCraft application, a Kotlin Multiplatform app for Android that allows users to browse, favorite, and order cocktails.
  4: 
  5: ## Architecture Diagrams
  6: 
  7: I've created architecture diagrams to help understand the structure and design of our CocktailCraft application.
  8: 
  9: ### Available Diagrams
 10: 
 11: 1. **[High-Level Architecture](images/mermaid/high_level_architecture.md)**: Overview of our application's layered architecture
 12: 2. **[Component Diagram](images/mermaid/component_diagram.md)**: Main components and their interactions
 13: 3. **[Use Case Diagram](images/mermaid/use_case_diagram.md)**: User interactions with our system
 14: 4. **[Class Diagram](images/mermaid/class_diagram.md)**: Key domain models and their relationships
 15: 5. **[Sequence Diagram - Place Order](images/mermaid/sequence_diagram_place_order.md)**: Interaction flow for placing an order
 16: 6. **[Data Flow Diagram](images/mermaid/data_flow_diagram.md)**: How data flows through our application
 17: 7. **[Package Diagram](images/mermaid/package_diagram.md)**: Organization of our codebase
 18: 
 19: These diagrams provide a comprehensive view of our application architecture, making it easier to understand the system design, onboard new team members, and plan future enhancements.
 20: 
 21: ## Application Architecture
 22: 
 23: CocktailCraft follows Clean Architecture principles with a MVVM pattern for the UI layer:
 24: 
 25: ## Additional Documentation
 26: 
 27: - **[Animations and Transitions](animations.md)**: Documentation of the app's animation system and implementation details, including:
 28:   - Animation utilities and reusable components
 29:   - Shimmer loading effects and micro-interactions
 30:   - Batched loading mechanism for optimized list animations
 31:   - Scrolling performance optimizations
 32: 
 33: - **[Recommendation System](RecommendationSystem.md)**: Documentation of the cocktail recommendation system, including:
 34:   - API limitations and constraints
 35:   - Recommendation strategies and algorithms
 36:   - Implementation approach and optimization techniques
 37:   - Future enhancement possibilities
 38: 
 39: - **[Advanced Search and Filtering](AdvancedSearch.md)**: Documentation of the advanced search functionality, including:
 40:   - Multi-criteria filtering implementation
 41:   - Filter types and UI components
 42:   - Search filter data flow
 43:   - Future enhancements for search capabilities
 44: 
 45: - **[Libraries](Libraries.md)**: Detailed information about the libraries used in the application, including:
 46:   - Versions and purposes
 47:   - Categorized by functionality
 48:   - Brief descriptions of each category
 49: 
 50: ### Key Architectural Layers
 51: 
 52: 1. **UI Layer**:
 53:    - Jetpack Compose UI components
 54:    - Screen composables
 55:    - Navigation
 56: 
 57: 2. **ViewModel Layer**:
 58:    - Manages UI state
 59:    - Handles user actions
 60:    - Communicates with domain layer
 61: 
 62: 3. **Domain Layer**:
 63:    - Business logic
 64:    - Use cases
 65:    - Repository interfaces
 66:    - Domain models
 67: 
 68: 4. **Data Layer**:
 69:    - Repository implementations
 70:    - Remote data sources (API)
 71:    - Local data sources (Storage)
 72:    - DTOs and mappers
 73: 
 74: 5. **Platform-Specific**:
 75:    - Android-specific implementations
 76:    - iOS-specific implementations (in progress)
 77: 
 78: ### Key Design Patterns
 79: 
 80: 1. **MVVM (Model-View-ViewModel)**: Separates UI from business logic
 81: 2. **Repository Pattern**: Abstracts data sources
 82: 3. **Use Case Pattern**: Encapsulates business logic
 83: 4. **Dependency Injection**: Uses Koin for DI
 84: 5. **Observer Pattern**: Uses Kotlin Flow for reactive programming
 85: 6. **Clean Architecture**: Separation of concerns with clear boundaries
 86: 
 87: ### Technologies
 88: 
 89: 1. **Kotlin Multiplatform**: For sharing code between platforms
 90: 2. **Jetpack Compose**: For declarative UI on Android
 91: 3. **Kotlin Coroutines & Flow**: For asynchronous programming
 92: 4. **Koin**: For dependency injection
 93: 5. **Ktor**: For networking
 94: 6. **Kotlin Serialization**: For JSON parsing
 95: 7. **Settings**: For local storage
 96: 
 97: ## Features
 98: 
 99: CocktailCraft provides the following features:
100: 
101: 1. **Browse Cocktails**: View a list of cocktails with filtering and sorting options
102: 2. **Cocktail Details**: View detailed information about a cocktail, including ingredients and instructions
103: 3. **Cart Management**: Add cocktails to cart, update quantities, and remove items
104: 4. **Order Management**: Place orders and view order history
105: 5. **Favorites**: Save favorite cocktails for quick access
106: 6. **User Profile**: View and edit user profile information
107: 7. **Reviews**: Read and write reviews for cocktails
108: 
109: ## Future Enhancements
110: 
111: 1. **iOS Support**: Complete iOS implementation using Kotlin Multiplatform
112: 2. **Backend Integration**: Develop a custom backend for user accounts, orders, and payments
113: 3. **Offline Support**: Enhance offline capabilities with local caching
114: 4. **Social Features**: Add social sharing and friend recommendations
115: 5. **Personalization**: Implement personalized recommendations based on user preferences
116: 6. **Saved Searches**: Allow users to save their favorite search filter combinations
````

## File: docs/RecommendationSystem.md
````markdown
  1: # Cocktail Recommendation System
  2: 
  3: This document outlines the implementation of the cocktail recommendation system in the CocktailCraft app, including the approach, limitations, and design decisions.
  4: 
  5: ## API Limitations and Constraints
  6: 
  7: The CocktailCraft app uses the free tier of TheCocktailDB API, which has several limitations that influenced our recommendation system design:
  8: 
  9: 1. **Limited API Endpoints**: The free tier only provides basic search and lookup functionality without dedicated recommendation endpoints.
 10: 2. **Rate Limiting**: Excessive API calls may be throttled or blocked.
 11: 3. **No User Tracking**: The API doesn't track user preferences or provide personalization features.
 12: 4. **Limited Data Access**: Some advanced data fields are only available in the paid tier.
 13: 
 14: ## Recommendation System Options Considered
 15: 
 16: We evaluated several approaches for implementing a recommendation system within these constraints:
 17: 
 18: ### 1. Server-side Recommendation Engine
 19: - **Description**: Building a custom backend service to track user preferences and generate recommendations
 20: - **Pros**: Powerful personalization, advanced algorithms
 21: - **Cons**: Requires backend infrastructure, significant development effort, ongoing maintenance
 22: - **Feasibility**: Low (given current project constraints)
 23: 
 24: ### 2. Third-party Recommendation Service
 25: - **Description**: Integrating with a specialized recommendation API
 26: - **Pros**: Sophisticated algorithms, minimal development effort
 27: - **Cons**: Additional costs, dependency on external service, data privacy concerns
 28: - **Feasibility**: Medium (cost concerns)
 29: 
 30: ### 3. Client-side Simple Recommendations
 31: - **Description**: Implementing basic recommendations directly in the app
 32: - **Pros**: No additional backend needed, works within API limitations
 33: - **Cons**: Limited sophistication, less personalized
 34: - **Feasibility**: High
 35: 
 36: ### 4. Local History-Based Recommendations
 37: - **Description**: Using locally stored user history to suggest similar cocktails
 38: - **Pros**: Privacy-friendly, works offline, no additional API calls
 39: - **Cons**: Limited to user's own history, cold start problem
 40: - **Feasibility**: High
 41: 
 42: ## Implemented Approach: Hybrid Client-side Recommendation Engine
 43: 
 44: We implemented a hybrid client-side approach that combines multiple recommendation strategies while working within the constraints of the free API tier.
 45: 
 46: ### Key Components
 47: 
 48: 1. **CocktailRecommendationEngine**: Core engine that implements multiple recommendation strategies
 49: 2. **Repository Extensions**: New methods added to CocktailRepository to support recommendations
 50: 3. **UI Components**: "You might also like" section in the cocktail detail screen
 51: 
 52: ### Recommendation Strategies
 53: 
 54: The engine uses a multi-strategy approach to generate recommendations:
 55: 
 56: 1. **Category-Based Matching**: Recommends cocktails from the same category
 57:    ```kotlin
 58:    // Example implementation
 59:    private suspend fun getCocktailsByCategory(category: String): List<Cocktail> {
 60:        return cocktailRepository.getCocktailsByCategory(category)
 61:            .filter { it.id !in excludeIds }
 62:            .shuffled()
 63:            .take(limit)
 64:    }
 65:    ```
 66: 
 67: 2. **Ingredient-Based Matching**: Recommends cocktails with similar ingredients
 68:    ```kotlin
 69:    // Example implementation
 70:    private suspend fun getCocktailsByIngredient(ingredient: String): List<Cocktail> {
 71:        return cocktailRepository.getCocktailsByIngredient(ingredient)
 72:            .filter { it.id !in excludeIds }
 73:            .shuffled()
 74:            .take(limit)
 75:    }
 76:    ```
 77: 
 78: 3. **User Preference Inference**: Analyzes user favorites to identify preferred categories
 79:    ```kotlin
 80:    // Example implementation
 81:    val favorites = favoritesRepository.getFavorites().first()
 82:    val favoriteCategories = favorites
 83:        .mapNotNull { it.category }
 84:        .groupBy { it }
 85:        .maxByOrNull { it.value.size }
 86:        ?.key
 87:    ```
 88: 
 89: 4. **Alcoholic/Non-alcoholic Matching**: Recommends cocktails with similar alcohol content
 90:    ```kotlin
 91:    // Example implementation
 92:    private suspend fun getCocktailsByAlcoholicFilter(alcoholicFilter: String): List<Cocktail> {
 93:        return cocktailRepository.getCocktailsByAlcoholicFilter(alcoholicFilter)
 94:            .filter { it.id !in excludeIds }
 95:            .shuffled()
 96:            .take(limit)
 97:    }
 98:    ```
 99: 
100: ### Optimization Techniques
101: 
102: To work efficiently within API constraints, we implemented several optimizations:
103: 
104: 1. **Caching**: Extensive use of local caching to minimize API calls
105:    ```kotlin
106:    // Example implementation
107:    if (isOffline()) {
108:        // Use cached cocktails when offline
109:        cocktailCache.getAllCachedCocktails()
110:            .filter { it.category == category }
111:            .take(5)
112:    } else {
113:        filterByCategory(category).first()
114:    }
115:    ```
116: 
117: 2. **Batched Loading**: Recommendations are loaded in a single batch
118: 3. **Fallback Mechanisms**: Graceful degradation when API calls fail
119: 4. **Offline Support**: Recommendations work even without internet connection
120: 
121: ### UI Implementation
122: 
123: The recommendation UI is designed to be:
124: 
125: 1. **Non-intrusive**: Displayed as a horizontal carousel below the main content
126: 2. **Visually appealing**: Animated entry, consistent with app design language
127: 3. **Performance-optimized**: Lazy loading, efficient image handling
128: 4. **Gracefully degrading**: Hides completely if no recommendations are available
129: 
130: ## Pros and Cons of the Implemented Approach
131: 
132: ### Pros
133: 1. **Works within API limitations**: No excessive API calls or paid tier requirements
134: 2. **Enhances user experience**: Provides discovery without complex backend
135: 3. **Offline-friendly**: Can work with cached data
136: 4. **Scalable**: Can be enhanced later if we move to a paid API or custom backend
137: 
138: ### Cons
139: 1. **Limited sophistication**: Simpler than advanced recommendation algorithms
140: 2. **Less personalized**: Limited user preference analysis
141: 3. **Cold start issues**: Limited recommendations for new users
142: 4. **API dependency**: Still relies on API data structure
143: 
144: ## Future Enhancements
145: 
146: If the app grows and resources become available, we could enhance the recommendation system by:
147: 
148: 1. **Implementing a custom backend**: For more sophisticated recommendation algorithms
149: 2. **Adding collaborative filtering**: "Users who liked this also liked..."
150: 3. **Incorporating explicit user preferences**: Allowing users to specify taste preferences
151: 4. **Expanding data sources**: Integrating with additional cocktail databases
152: 5. **Adding machine learning**: For more personalized recommendations
153: 
154: ## Conclusion
155: 
156: Our hybrid client-side recommendation approach provides a valuable feature enhancement while working within the constraints of the free TheCocktailDB API. It balances user experience, technical feasibility, and resource constraints to deliver useful cocktail recommendations without requiring additional backend infrastructure or paid API tiers.
````

## File: docs/UI_Components.md
````markdown
  1: # UI Components Documentation
  2: 
  3: This document provides an overview of the reusable UI components in the CocktailCraft app, their purpose, and guidelines for using them in new features or refactoring existing code.
  4: 
  5: ## Table of Contents
  6: 
  7: 1. [Introduction](#introduction)
  8: 2. [Component Usage Guidelines](#component-usage-guidelines)
  9: 3. [Available Components](#available-components)
 10:    - [EmptyStateComponent](#emptystatecomponent)
 11:    - [DetailHeaderImage](#detailheaderimage)
 12:    - [SectionHeader](#sectionheader)
 13:    - [LoadingStateComponent](#loadingstatecomponent)
 14:    - [OrderSummaryCard](#ordersummarycard)
 15:    - [ConfirmationDialog](#confirmationdialog)
 16:    - [DetailInfoCard](#detailinfocard)
 17:    - [RatingDisplay](#ratingdisplay)
 18:    - [StatusIndicator](#statusindicator)
 19:    - [SettingsCard](#settingscard)
 20:    - [InfoCard](#infocard)
 21:    - [ToggleSettingItem](#togglesettingitem)
 22:    - [NetworkStatusCard](#networkstatuscard)
 23:    - [ProfileCard](#profilecard)
 24:    - [AuthDialog](#authdialog)
 25:    - [CocktailItem](#cocktailitem)
 26:    - [AnimatedCocktailItem](#animatedcocktailitem)
 27:    - [CartItemCard](#cartitemcard)
 28:    - [FilterChip](#filterchip)
 29:    - [RatingBar](#ratingbar)
 30:    - [OptimizedImage](#optimizedimage)
 31:    - [ErrorDialog and ErrorBanner](#errordialog-and-errorbanner)
 32:    - [AnimatedButtons](#animatedbuttons)
 33:    - [ExpandableAdvancedSearchPanel](#expandableadvancedsearchpanel)
 34:    - [CocktailSearchBar](#cocktailsearchbar)
 35:    - [NetworkErrorStateDisplay](#networkerrordisplay)
 36:    - [AnimatedCocktailList](#animatedcocktaillist)
 37:    - [CategoryFilterRow](#categoryfilterrow)
 38:    - [CocktailLoadingShimmer](#cocktailloadingshimmer)
 39:    - [EndOfListMessage](#endoflistmessage)
 40:    - [LoadingMoreIndicator](#loadingmoreindicator)
 41: 4. [Refactoring Process](#refactoring-process)
 42: 5. [Best Practices](#best-practices)
 43: 
 44: ## Introduction
 45: 
 46: The CocktailCraft app uses a component-based architecture for its UI, with reusable components that ensure consistency across the app. These components are located in the `androidApp/src/main/java/com/cocktailcraft/ui/components` directory.
 47: 
 48: Using these reusable components offers several benefits:
 49: - **Consistency**: Ensures a uniform look and feel across the app
 50: - **Maintainability**: Changes to components are reflected everywhere they're used
 51: - **Efficiency**: Reduces development time for new features
 52: - **Readability**: Makes the code more organized and easier to understand
 53: 
 54: ## Component Usage Guidelines
 55: 
 56: When developing new features or refactoring existing code, follow these guidelines:
 57: 
 58: 1. **Always check for existing components first** before creating new ones
 59: 2. **Use the most specific component** that meets your needs
 60: 3. **Extend existing components** when you need slight variations rather than creating entirely new ones
 61: 4. **Keep components focused** on a single responsibility
 62: 5. **Document any new components** you create in this guide
 63: 
 64: ## Available Components
 65: 
 66: ### EmptyStateComponent
 67: 
 68: **Purpose**: Displays a standardized empty state with an icon/image, title, message, and optional action button.
 69: 
 70: **Usage**:
 71: ```kotlin
 72: EmptyStateComponent(
 73:     title = "Your cart is empty",
 74:     message = "Add some cocktails to your cart and they will appear here",
 75:     actionButtonText = "Start Shopping",
 76:     onActionButtonClick = onStartShopping,
 77:     icon = Icons.Filled.ShoppingCart
 78: )
 79: ```
 80: 
 81: **When to use**: For any screen that needs to display an empty state, such as an empty cart, empty favorites list, or no search results.
 82: 
 83: ### DetailHeaderImage
 84: 
 85: **Purpose**: Displays a header image with a gradient overlay, commonly used in detail screens.
 86: 
 87: **Usage**:
 88: ```kotlin
 89: DetailHeaderImage(
 90:     imageUrl = imageUrl,
 91:     contentDescription = cocktailData.name,
 92:     height = 250,
 93:     targetSize = 800 // Higher resolution for detail view
 94: )
 95: ```
 96: 
 97: **When to use**: For detail screens that display a large header image with a gradient overlay.
 98: 
 99: ### SectionHeader
100: 
101: **Purpose**: Provides a consistent header for sections with an optional action button.
102: 
103: **Usage**:
104: ```kotlin
105: SectionHeader(
106:     title = "Your Cart",
107:     fontSize = 20,
108:     modifier = Modifier.padding(bottom = 8.dp),
109:     actionText = "Clear All",
110:     onActionClick = { /* Clear action */ }
111: )
112: ```
113: 
114: **When to use**: For any section in a screen that needs a header, especially when you want to maintain consistent styling across the app.
115: 
116: ### LoadingStateComponent
117: 
118: **Purpose**: Displays a standardized loading indicator with animation.
119: 
120: **Usage**:
121: ```kotlin
122: LoadingStateComponent(
123:     isLoading = isLoading,
124:     paddingValues = paddingValues
125: )
126: ```
127: 
128: **When to use**: Whenever you need to show a loading state while data is being fetched or processed.
129: 
130: ### OrderSummaryCard
131: 
132: **Purpose**: Displays an order summary with subtotal, delivery fee, and total.
133: 
134: **Usage**:
135: ```kotlin
136: OrderSummaryCard(
137:     subtotal = totalPrice,
138:     deliveryFee = 5.99,
139:     modifier = Modifier.padding(vertical = 8.dp),
140:     currencyFormatter = currencyFormatter
141: )
142: ```
143: 
144: **When to use**: In cart or checkout screens where you need to display an order summary.
145: 
146: ### ConfirmationDialog
147: 
148: **Purpose**: Displays a confirmation dialog with customizable title, message, and buttons.
149: 
150: **Usage**:
151: ```kotlin
152: ConfirmationDialog(
153:     showDialog = showPlaceOrderDialog,
154:     title = "Confirm Order",
155:     message = "Are you sure you want to place this order?",
156:     confirmButtonText = "Confirm",
157:     dismissButtonText = "Cancel",
158:     onConfirm = { /* Confirm action */ },
159:     onDismiss = { /* Dismiss action */ }
160: )
161: ```
162: 
163: **When to use**: When you need to confirm a user action, such as placing an order, deleting an item, or logging out.
164: 
165: ### DetailInfoCard
166: 
167: **Purpose**: Displays detailed information in a card with a consistent style.
168: 
169: **Usage**:
170: ```kotlin
171: DetailInfoCard(
172:     title = "How to Prepare",
173:     modifier = Modifier.padding(vertical = 8.dp),
174:     backgroundColor = AppColors.Surface.copy(alpha = 0.8f)
175: ) {
176:     // Content goes here
177:     Text(
178:         text = instructionsText,
179:         fontSize = 15.sp,
180:         color = AppColors.TextPrimary,
181:         lineHeight = 24.sp
182:     )
183: }
184: ```
185: 
186: **When to use**: For displaying detailed information in a card format, especially in detail screens.
187: 
188: ### RatingDisplay
189: 
190: **Purpose**: Displays a rating with stars and review count.
191: 
192: **Usage**:
193: ```kotlin
194: RatingDisplay(
195:     rating = 4.5f,
196:     reviewCount = 12,
197:     starSize = 16,
198:     useHalfStars = true
199: )
200: ```
201: 
202: **When to use**: When you need to display a rating with stars and review count, such as in product listings or detail screens.
203: 
204: ### StatusIndicator
205: 
206: **Purpose**: Displays a status with a colored dot or icon and text.
207: 
208: **Usage**:
209: ```kotlin
210: StatusIndicator(
211:     status = order.status,
212:     isActive = order.status == "Completed",
213:     activeColor = Color(0xFF4CAF50),
214:     icon = Icons.Default.CheckCircle
215: )
216: ```
217: 
218: **When to use**: When you need to display a status indicator, such as order status, network status, or any other state that can be active or inactive.
219: 
220: ### SettingsCard
221: 
222: **Purpose**: Provides a consistent card layout for settings sections with a title and content.
223: 
224: **Usage**:
225: ```kotlin
226: SettingsCard(
227:     title = "Account Settings",
228:     modifier = Modifier.padding(bottom = 16.dp)
229: ) {
230:     // Content goes here
231:     SettingsItem(
232:         icon = Icons.Default.Person,
233:         title = "Edit Profile",
234:         onClick = { /* Handle edit profile */ }
235:     )
236: 
237:     SettingsItem(
238:         icon = Icons.Default.Lock,
239:         title = "Change Password",
240:         onClick = { /* Handle change password */ }
241:     )
242: }
243: ```
244: 
245: **When to use**: For settings screens or any section that needs a consistent card layout with a title and content.
246: 
247: ### InfoCard
248: 
249: **Purpose**: Displays information with an icon, title, and content in a card layout.
250: 
251: **Usage**:
252: ```kotlin
253: InfoCard(
254:     title = "Cached Cocktails",
255:     icon = Icons.Default.Storage,
256:     modifier = Modifier.padding(bottom = 16.dp)
257: ) {
258:     // Content goes here
259:     Row(
260:         modifier = Modifier.fillMaxWidth(),
261:         horizontalArrangement = Arrangement.SpaceBetween
262:     ) {
263:         Text(
264:             text = "Cocktails available offline:",
265:             fontSize = 14.sp,
266:             color = AppColors.TextSecondary
267:         )
268: 
269:         Text(
270:             text = "${recentlyViewedCocktails.size}",
271:             fontSize = 14.sp,
272:             fontWeight = FontWeight.Bold,
273:             color = AppColors.TextPrimary
274:         )
275:     }
276: }
277: ```
278: 
279: **When to use**: When you need to display information with an icon and title in a card layout, such as in settings screens or information sections.
280: 
281: ### ToggleSettingItem
282: 
283: **Purpose**: Displays a setting with a title, description, icon, and toggle switch.
284: 
285: **Usage**:
286: ```kotlin
287: ToggleSettingItem(
288:     title = "Offline Mode",
289:     description = "When enabled, the app will only use cached data and won't make network requests.",
290:     icon = Icons.Default.AirplanemodeActive,
291:     isEnabled = isOfflineModeEnabled,
292:     onToggle = { viewModel.toggleOfflineMode() }
293: )
294: ```
295: 
296: **When to use**: For settings that can be toggled on or off, such as offline mode, dark mode, or notifications.
297: 
298: ### NetworkStatusCard
299: 
300: **Purpose**: Displays the current network status with an appropriate icon and color.
301: 
302: **Usage**:
303: ```kotlin
304: NetworkStatusCard(
305:     isNetworkAvailable = isNetworkAvailable,
306:     modifier = Modifier.padding(bottom = 16.dp)
307: )
308: ```
309: 
310: **When to use**: When you need to display the current network status, especially in screens related to offline functionality.
311: 
312: ### ProfileCard
313: 
314: **Purpose**: Displays user profile information with an avatar, name, email, and sign-in/sign-up buttons if not signed in.
315: 
316: **Usage**:
317: ```kotlin
318: ProfileCard(
319:     userName = userName,
320:     userEmail = userEmail,
321:     isSignedIn = isSignedIn,
322:     onSignIn = { showSignInDialog = true },
323:     onSignUp = { showSignUpDialog = true }
324: )
325: ```
326: 
327: **When to use**: For profile screens to display user information and authentication options.
328: 
329: ### AuthDialog
330: 
331: **Purpose**: Provides a dialog for user authentication (sign-in or sign-up).
332: 
333: **Usage**:
334: ```kotlin
335: AuthDialog(
336:     type = AuthDialogType.SIGN_IN,
337:     onDismiss = { showSignInDialog = false },
338:     onSubmit = { name, email, password ->
339:         profileViewModel.signIn(email, password)
340:         showSignInDialog = false
341:     }
342: )
343: ```
344: 
345: **When to use**: When you need to display a sign-in or sign-up dialog for user authentication.
346: 
347: ### CocktailItem
348: 
349: **Purpose**: Displays a cocktail item in a list with image, name, price, and actions.
350: 
351: **Usage**:
352: ```kotlin
353: CocktailItem(
354:     cocktail = cocktail,
355:     onClick = { /* Navigate to detail */ },
356:     onAddToCart = { cocktail ->
357:         cartViewModel.addToCart(cocktail)
358:         onAddToCart(cocktail)
359:     },
360:     isFavorite = true,
361:     onToggleFavorite = { cocktail ->
362:         favoritesViewModel.toggleFavorite(cocktail)
363:     }
364: )
365: ```
366: 
367: **When to use**: In lists of cocktails, such as in the home screen or search results.
368: 
369: ### AnimatedCocktailItem
370: 
371: **Purpose**: An enhanced version of CocktailItem with animations.
372: 
373: **Usage**:
374: ```kotlin
375: AnimatedCocktailItem(
376:     cocktail = cocktail,
377:     onClick = { /* Navigate to detail */ },
378:     onAddToCart = { cocktail ->
379:         cartViewModel.addToCart(cocktail)
380:         onAddToCart(cocktail)
381:     },
382:     isFavorite = true,
383:     onToggleFavorite = { cocktail ->
384:         favoritesViewModel.toggleFavorite(cocktail)
385:     },
386:     index = index
387: )
388: ```
389: 
390: **When to use**: When you want to add animations to cocktail items in a list for a more engaging user experience.
391: 
392: ### CartItemCard
393: 
394: **Purpose**: Displays a cart item with image, name, price, quantity controls, and actions.
395: 
396: **Usage**:
397: ```kotlin
398: CartItemCard(
399:     item = item,
400:     onIncreaseQuantity = { viewModel.updateQuantity(item.cocktail.id, item.quantity + 1) },
401:     onDecreaseQuantity = {
402:         if (item.quantity > 1) {
403:             viewModel.updateQuantity(item.cocktail.id, item.quantity - 1)
404:         } else {
405:             viewModel.removeFromCart(item.cocktail.id)
406:         }
407:     },
408:     onRemove = { viewModel.removeFromCart(item.cocktail.id) },
409:     isFavorite = favorites.any { it.id == item.cocktail.id },
410:     onToggleFavorite = { favoritesViewModel.toggleFavorite(item.cocktail) }
411: )
412: ```
413: 
414: **When to use**: In the cart screen to display items in the user's cart.
415: 
416: ### FilterChip
417: 
418: **Purpose**: Displays a selectable chip for filtering options.
419: 
420: **Usage**:
421: ```kotlin
422: FilterChip(
423:     selected = selectedComplexity == complexity,
424:     onClick = { onComplexitySelected(complexity) },
425:     label = complexity.toString()
426: )
427: ```
428: 
429: **When to use**: For filtering options in search or browse screens.
430: 
431: ### RatingBar
432: 
433: **Purpose**: Displays a row of stars representing a rating.
434: 
435: **Usage**:
436: ```kotlin
437: RatingBar(
438:     rating = 4.5f,
439:     stars = 5,
440:     starsColor = AppColors.Secondary,
441:     useHalfStars = true
442: )
443: ```
444: 
445: **When to use**: When you need to display a rating with stars, such as in product listings or detail screens.
446: 
447: ### OptimizedImage
448: 
449: **Purpose**: Loads and displays images with optimization for performance.
450: 
451: **Usage**:
452: ```kotlin
453: OptimizedImage(
454:     url = imageUrl,
455:     contentDescription = cocktailData.name,
456:     modifier = Modifier.fillMaxSize(),
457:     contentScale = ContentScale.Crop,
458:     targetSize = 800 // Higher resolution for detail view
459: )
460: ```
461: 
462: **When to use**: Whenever you need to display an image from a URL, especially when performance is a concern.
463: 
464: ### ErrorDialog and ErrorBanner
465: 
466: **Purpose**: Displays error messages in a dialog or banner format.
467: 
468: **Usage**:
469: ```kotlin
470: ErrorDialog(
471:     error = error,
472:     onDismiss = { /* Dismiss action */ },
473:     onRetry = { /* Retry action */ }
474: )
475: 
476: ErrorBanner(
477:     error = error,
478:     onDismiss = { /* Dismiss action */ },
479:     onAction = { /* Action */ }
480: )
481: ```
482: 
483: **When to use**: When you need to display error messages to the user, either in a modal dialog or a non-modal banner.
484: 
485: ### AnimatedButtons
486: 
487: **Purpose**: Provides buttons with scale animations when pressed.
488: 
489: **Usage**:
490: ```kotlin
491: AnimatedButton(
492:     onClick = { /* Action */ },
493:     modifier = Modifier
494: ) {
495:     Text("Click Me")
496: }
497: 
498: AnimatedTextButton(
499:     text = "Click Me",
500:     onClick = { /* Action */ }
501: )
502: ```
503: 
504: **When to use**: When you want to add animations to buttons for a more engaging user experience.
505: 
506: ### ExpandableAdvancedSearchPanel
507: 
508: **Purpose**: Provides an expandable panel for advanced search filters that can be integrated directly into a screen.
509: 
510: **Usage**:
511: ```kotlin
512: ExpandableAdvancedSearchPanel(
513:     isExpanded = isAdvancedSearchActive,
514:     currentFilters = searchFilters,
515:     categories = categories,
516:     ingredients = ingredients,
517:     glasses = glasses,
518:     onApplyFilters = { filters ->
519:         viewModel.updateSearchFilters(filters)
520:     },
521:     onClearFilters = {
522:         viewModel.clearSearchFilters()
523:     }
524: )
525: ```
526: 
527: **When to use**: When you need to provide advanced search functionality that can be expanded and collapsed within a screen, rather than showing a separate dialog.
528: 
529: ### CocktailSearchBar
530: 
531: **Purpose**: Provides a search bar with an advanced search button for filtering cocktails.
532: 
533: **Usage**:
534: ```kotlin
535: CocktailSearchBar(
536:     searchQuery = searchQuery,
537:     isAdvancedSearchActive = isAdvancedSearchActive,
538:     hasActiveFilters = searchFilters.hasActiveFilters(),
539:     onSearchQueryChange = { viewModel.searchCocktails(it) },
540:     onClearSearch = { viewModel.toggleSearchMode(false) },
541:     onToggleAdvancedSearch = { viewModel.toggleAdvancedSearchMode(!isAdvancedSearchActive) },
542:     onShowAdvancedSearchDialog = { showAdvancedSearch = true }
543: )
544: ```
545: 
546: **When to use**: When you need a search bar with advanced filtering capabilities, such as in the home screen or any screen with searchable content.
547: 
548: ### NetworkErrorStateDisplay
549: 
550: **Purpose**: Displays network and offline error states with appropriate icons, messages, and action buttons.
551: 
552: **Usage**:
553: ```kotlin
554: NetworkErrorStateDisplay(
555:     errorMessage = errorMessage,
556:     isOfflineMode = isOfflineMode,
557:     isNetworkAvailable = isNetworkAvailable,
558:     hasContent = cocktails.isNotEmpty(),
559:     onRetry = { viewModel.retry() },
560:     onEnableOfflineMode = {
561:         viewModel.setOfflineMode(true)
562:         viewModel.retry()
563:     },
564:     onGoOnline = {
565:         viewModel.setOfflineMode(false)
566:         viewModel.retry()
567:     }
568: )
569: ```
570: 
571: **When to use**: When you need to display network-related errors with appropriate recovery options, especially in screens that depend on network connectivity.
572: 
573: ### AnimatedCocktailList
574: 
575: **Purpose**: Displays a list of cocktails with animations, pagination, and end-of-list messaging.
576: 
577: **Usage**:
578: ```kotlin
579: AnimatedCocktailList(
580:     cocktails = cocktails,
581:     isSearchActive = isSearchActive,
582:     selectedCategory = selectedCategory,
583:     isLoadingMore = isLoadingMore,
584:     hasMoreData = hasMoreData,
585:     favorites = favorites,
586:     onCocktailClick = onCocktailClick,
587:     onAddToCart = onAddToCart,
588:     onToggleFavorite = { cocktailToToggle ->
589:         favoritesViewModel.toggleFavorite(cocktailToToggle)
590:     },
591:     onLoadMore = {
592:         viewModel.loadMoreCocktails()
593:     }
594: )
595: ```
596: 
597: **When to use**: When you need to display a list of cocktails with animations and pagination, such as in the home screen, favorites screen, or search results.
598: 
599: ### CategoryFilterRow
600: 
601: **Purpose**: Displays a horizontal row of category filter chips for filtering content.
602: 
603: **Usage**:
604: ```kotlin
605: CategoryFilterRow(
606:     categories = categories,
607:     selectedCategory = selectedCategory,
608:     onCategorySelected = onCategorySelected
609: )
610: ```
611: 
612: **When to use**: When you need to display a horizontal list of categories for filtering content, such as in the home screen or browse screen.
613: 
614: ### CocktailLoadingShimmer
615: 
616: **Purpose**: Displays a shimmer loading effect for cocktails while content is loading.
617: 
618: **Usage**:
619: ```kotlin
620: CocktailLoadingShimmer()
621: ```
622: 
623: **When to use**: When you need to display a loading state for cocktails, providing a better user experience than a simple spinner.
624: 
625: ### EndOfListMessage
626: 
627: **Purpose**: Displays an animated message when the user reaches the end of a list.
628: 
629: **Usage**:
630: ```kotlin
631: EndOfListMessage(
632:     visible = !hasMoreData && !isSearchActive && cocktails.isNotEmpty(),
633:     message = "You've reached the end of the list"
634: )
635: ```
636: 
637: **When to use**: When you need to indicate to the user that they have reached the end of a paginated list.
638: 
639: ### LoadingMoreIndicator
640: 
641: **Purpose**: Displays a loading indicator when loading more items in a paginated list.
642: 
643: **Usage**:
644: ```kotlin
645: LoadingMoreIndicator(
646:     isLoading = isLoadingMore
647: )
648: ```
649: 
650: **When to use**: When you need to indicate that more items are being loaded in a paginated list.
651: 
652: ## Refactoring Process
653: 
654: When refactoring UI code to use reusable components, follow these steps:
655: 
656: 1. **Identify patterns**: Look for repeated UI patterns across different screens
657: 2. **Extract components**: Create reusable components for these patterns
658: 3. **Parameterize**: Make components flexible with parameters for variations
659: 4. **Replace instances**: Update all occurrences to use the new components
660: 5. **Test thoroughly**: Ensure the refactored code works as expected
661: 
662: Recent refactoring in the CocktailCraft app focused on:
663: 
664: **First Phase:**
665: - Creating standardized empty states
666: - Extracting header images with gradient overlays
667: - Standardizing section headers
668: - Creating reusable cards for different types of information
669: - Implementing consistent loading states
670: 
671: **Second Phase:**
672: - Creating status indicators for order status and network status
673: - Extracting settings cards for profile and settings screens
674: - Creating info cards for displaying information with icons
675: - Implementing toggle setting items for settings screens
676: - Creating network status cards for offline functionality
677: - Extracting profile cards for user information
678: - Implementing authentication dialogs for sign-in and sign-up
679: 
680: **Third Phase:**
681: - Creating a reusable search bar with advanced search functionality
682: - Implementing a network error state display with offline mode support
683: - Extracting an animated cocktail list with pagination
684: - Creating a category filter row for content filtering
685: - Implementing a shimmer loading effect for cocktails
686: - Creating end-of-list and loading-more indicators for pagination
687: - Extracting filter options loading logic into a utility
688: 
689: ## Best Practices
690: 
691: 1. **Component Naming**: Use clear, descriptive names that indicate the component's purpose
692: 2. **Documentation**: Add KDoc comments to explain parameters and usage
693: 3. **Default Values**: Provide sensible defaults for optional parameters
694: 4. **Theming**: Use app theme colors and dimensions for consistency
695: 5. **Accessibility**: Ensure components are accessible with proper content descriptions
696: 6. **Composition**: Prefer composition over inheritance for component reuse
697: 7. **Testing**: Write tests for components to ensure they work as expected
698: 
699: By following these guidelines and using the available components, you can maintain a consistent, maintainable, and efficient UI codebase for the CocktailCraft app.
````

## File: gradle/wrapper/gradle-wrapper.properties
````
1: distributionBase=GRADLE_USER_HOME
2: distributionPath=wrapper/dists
3: distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
4: networkTimeout=10000
5: validateDistributionUrl=true
6: zipStoreBase=GRADLE_USER_HOME
7: zipStorePath=wrapper/dists
````

## File: shared/src/androidMain/kotlin/com/cocktailcraft/di/PlatformModule.kt
````kotlin
 1: package com.cocktailcraft.di
 2: 
 3: import android.content.Context
 4: import androidx.datastore.core.DataStore
 5: import androidx.datastore.preferences.core.Preferences
 6: import androidx.datastore.preferences.preferencesDataStore
 7: import com.russhwolf.settings.ExperimentalSettingsApi
 8: import com.russhwolf.settings.ExperimentalSettingsImplementation
 9: import com.russhwolf.settings.Settings
10: import com.russhwolf.settings.SharedPreferencesSettings
11: import com.russhwolf.settings.coroutines.FlowSettings
12: import com.russhwolf.settings.datastore.DataStoreSettings
13: import org.koin.dsl.module
14: 
15: private val Context.dataStore by preferencesDataStore(name = "cocktailcraft_preferences")
16: 
17: @OptIn(ExperimentalSettingsImplementation::class, ExperimentalSettingsApi::class)
18: actual fun platformModule() = module {
19:     single { 
20:         get<Context>().dataStore 
21:     }
22:     
23:     single<FlowSettings> {
24:         DataStoreSettings(get<DataStore<Preferences>>())
25:     }
26:     
27:     single<Settings> {
28:         val context = get<Context>()
29:         val sharedPrefs = context.getSharedPreferences("cocktailcraft_prefs", Context.MODE_PRIVATE)
30:         SharedPreferencesSettings(sharedPrefs)
31:     }
32: }
````

## File: shared/src/androidMain/kotlin/com/cocktailcraft/util/CocktailDebugLogger.kt
````kotlin
 1: package com.cocktailcraft.util
 2: 
 3: import android.util.Log
 4: 
 5: actual fun logInternal(message: String) {
 6:     // Use Log.e for now to ensure it shows up (priority 6)
 7:     Log.e(CocktailDebugLogger.TAG, message)
 8:     // Also use println as fallback
 9:     println("CocktailDebug: $message")
10: }
````

## File: shared/src/androidMain/kotlin/com/cocktailcraft/util/NetworkMonitor.kt
````kotlin
 1: package com.cocktailcraft.util
 2: 
 3: import android.content.Context
 4: import android.net.ConnectivityManager
 5: import android.net.Network
 6: import android.net.NetworkCapabilities
 7: import android.net.NetworkRequest
 8: import kotlinx.coroutines.flow.StateFlow
 9: import kotlinx.coroutines.flow.asStateFlow
10: 
11: /**
12:  * Android implementation of NetworkMonitor.
13:  */
14: actual class NetworkMonitor actual constructor(
15:     private val context: Context
16: ) : BaseNetworkMonitor() {
17: 
18:     actual override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
19:     private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
20: 
21:     private val networkCallback = object : ConnectivityManager.NetworkCallback() {
22:         override fun onAvailable(network: Network) {
23:             _isOnline.value = true
24:         }
25: 
26:         override fun onLost(network: Network) {
27:             // Only set offline if there are no other networks available
28:             if (!isNetworkAvailable()) {
29:                 _isOnline.value = false
30:             }
31:         }
32: 
33:         override fun onCapabilitiesChanged(
34:             network: Network,
35:             networkCapabilities: NetworkCapabilities
36:         ) {
37:             // Update online status based on internet capability
38:             val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
39:             if (_isOnline.value != hasInternet) {
40:                 _isOnline.value = hasInternet
41:             }
42:         }
43:     }
44: 
45:     actual override fun startMonitoring() {
46:         val networkRequest = NetworkRequest.Builder()
47:             .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
48:             .build()
49: 
50:         connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
51: 
52:         // Initialize with current status
53:         _isOnline.value = isNetworkAvailable()
54:     }
55: 
56:     actual override fun stopMonitoring() {
57:         try {
58:             connectivityManager.unregisterNetworkCallback(networkCallback)
59:         } catch (e: Exception) {
60:             // Ignore if not registered
61:         }
62:     }
63: 
64:     private fun isNetworkAvailable(): Boolean {
65:         val activeNetwork = connectivityManager.activeNetwork ?: return false
66:         val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
67:         return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
68:     }
69: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/data/cache/CocktailCache.kt
````kotlin
  1: package com.cocktailcraft.data.cache
  2: 
  3: import com.cocktailcraft.domain.config.AppConfig
  4: import com.cocktailcraft.domain.model.Cocktail
  5: import com.russhwolf.settings.Settings
  6: import kotlinx.serialization.decodeFromString
  7: import kotlinx.serialization.encodeToString
  8: import kotlinx.serialization.json.Json
  9: import kotlinx.serialization.builtins.ListSerializer
 10: import kotlinx.datetime.Clock
 11: import com.cocktailcraft.util.CocktailDebugLogger
 12: 
 13: /**
 14:  * Simple LRU cache implementation
 15:  */
 16: class SimpleLruCache<K, V>(private val maxSize: Int) {
 17:     private val cache = LinkedHashMap<K, V>(maxSize + 1, 0.75f, true)
 18:     
 19:     fun put(key: K, value: V) {
 20:         synchronized(cache) {
 21:             cache[key] = value
 22:             if (cache.size > maxSize) {
 23:                 val iterator = cache.entries.iterator()
 24:                 iterator.next()
 25:                 iterator.remove()
 26:             }
 27:         }
 28:     }
 29:     
 30:     fun get(key: K): V? {
 31:         synchronized(cache) {
 32:             return cache[key]
 33:         }
 34:     }
 35:     
 36:     fun snapshot(): Map<K, V> {
 37:         synchronized(cache) {
 38:             return cache.toMap()
 39:         }
 40:     }
 41:     
 42:     fun clear() {
 43:         synchronized(cache) {
 44:             cache.clear()
 45:         }
 46:     }
 47:     
 48:     val size: Int
 49:         get() = synchronized(cache) { cache.size }
 50: }
 51: 
 52: /**
 53:  * Manages caching of cocktail data for offline access using a simple LRU cache.
 54:  */
 55: class CocktailCache(
 56:     private val settings: Settings,
 57:     private val json: Json,
 58:     private val appConfig: AppConfig
 59: ) {
 60:     companion object {
 61:         private const val CACHE_PREFIX = "cocktail_cache_"
 62:         private const val RECENTLY_VIEWED_KEY = "recently_viewed_cocktails"
 63:         private const val ALL_COCKTAILS_KEY = "all_cached_cocktails"
 64:         private const val CACHE_METADATA_PREFIX = "cache_metadata_"
 65:         private const val MAX_RECENTLY_VIEWED = 20
 66:         private const val MAX_CACHED_COCKTAILS = 100
 67:     }
 68:     
 69:     // In-memory cache for cocktails using LRU strategy
 70:     private val cocktailCache = SimpleLruCache<String, Cocktail>(MAX_CACHED_COCKTAILS)
 71: 
 72:     // In-memory cache for recently viewed cocktails
 73:     private val recentlyViewedCache = SimpleLruCache<String, Cocktail>(MAX_RECENTLY_VIEWED)
 74:     
 75:     init {
 76:         CocktailDebugLogger.log("🗄️ CocktailCache init()")
 77:         // Load persisted cocktails into memory cache on initialization
 78:         loadPersistedCocktails()
 79:     }
 80:     
 81:     private fun loadPersistedCocktails() {
 82:         CocktailDebugLogger.log("📂 Loading persisted cocktails...")
 83:         try {
 84:             // Load all cached cocktails from persistent storage
 85:             val cachedJson = settings.getStringOrNull(ALL_COCKTAILS_KEY)
 86:             if (!cachedJson.isNullOrBlank()) {
 87:                 val cocktails = json.decodeFromString<List<Cocktail>>(cachedJson)
 88:                 CocktailDebugLogger.log("   ✅ Loaded ${cocktails.size} cocktails from persistent storage")
 89:                 cocktails.forEach { cocktail ->
 90:                     cocktailCache.put(cocktail.id, cocktail)
 91:                 }
 92:             } else {
 93:                 CocktailDebugLogger.log("   ⚠️ No persisted cocktails found")
 94:             }
 95:             
 96:             // Load recently viewed cocktails
 97:             val recentJson = settings.getStringOrNull(RECENTLY_VIEWED_KEY)
 98:             if (!recentJson.isNullOrBlank()) {
 99:                 val recentCocktails = json.decodeFromString<List<Cocktail>>(recentJson)
100:                 CocktailDebugLogger.log("   ✅ Loaded ${recentCocktails.size} recently viewed cocktails")
101:                 recentCocktails.forEach { cocktail ->
102:                     recentlyViewedCache.put(cocktail.id, cocktail)
103:                 }
104:             } else {
105:                 CocktailDebugLogger.log("   ⚠️ No recently viewed cocktails found")
106:             }
107:         } catch (e: Exception) {
108:             // Log error but don't crash - caching is not critical
109:             CocktailDebugLogger.log("   ❌ Failed to load persisted cocktails: ${e.message}")
110:         }
111:     }
112:     
113:     private fun persistCocktails() {
114:         try {
115:             // Persist all cached cocktails
116:             val allCocktails = cocktailCache.snapshot().values.toList()
117:             CocktailDebugLogger.log("💾 Persisting ${allCocktails.size} cocktails to storage")
118:             if (allCocktails.isNotEmpty()) {
119:                 val jsonString = json.encodeToString(allCocktails)
120:                 settings.putString(ALL_COCKTAILS_KEY, jsonString)
121:                 CocktailDebugLogger.log("   ✅ Successfully persisted cocktails")
122:             }
123:         } catch (e: Exception) {
124:             CocktailDebugLogger.log("   ❌ Failed to persist cocktails: ${e.message}")
125:         }
126:     }
127:     
128:     private fun persistRecentlyViewed() {
129:         try {
130:             // Persist recently viewed cocktails
131:             val recentCocktails = recentlyViewedCache.snapshot().values.toList()
132:             if (recentCocktails.isNotEmpty()) {
133:                 val jsonString = json.encodeToString(recentCocktails)
134:                 settings.putString(RECENTLY_VIEWED_KEY, jsonString)
135:             }
136:         } catch (e: Exception) {
137:             CocktailDebugLogger.log("Failed to persist recently viewed: ${e.message}")
138:         }
139:     }
140:     
141:     /**
142:      * Cache a cocktail for offline access.
143:      */
144:     suspend fun cacheCocktail(cocktail: Cocktail) {
145:         cocktailCache.put(cocktail.id, cocktail)
146:         persistCocktails() // Persist to storage
147:     }
148:     
149:     /**
150:      * Get a cached cocktail by ID.
151:      */
152:     suspend fun getCachedCocktail(id: String): Cocktail? {
153:         return cocktailCache.get(id)
154:     }
155:     
156:     /**
157:      * Get all cached cocktails.
158:      */
159:     suspend fun getAllCachedCocktails(): List<Cocktail> {
160:         val cocktails = cocktailCache.snapshot().values.toList()
161:         CocktailDebugLogger.log("📦 CocktailCache.getAllCachedCocktails() returning ${cocktails.size} items")
162:         return cocktails
163:     }
164:     
165:     /**
166:      * Add a cocktail to the recently viewed list.
167:      */
168:     suspend fun addToRecentlyViewed(cocktail: Cocktail) {
169:         recentlyViewedCache.put(cocktail.id, cocktail)
170:         // Also add to main cache if not already there
171:         if (cocktailCache.get(cocktail.id) == null) {
172:             cocktailCache.put(cocktail.id, cocktail)
173:             persistCocktails()
174:         }
175:         persistRecentlyViewed() // Persist to storage
176:     }
177:     
178:     /**
179:      * Get the list of recently viewed cocktails.
180:      */
181:     suspend fun getRecentlyViewedCocktails(): List<Cocktail> {
182:         val recent = recentlyViewedCache.snapshot().values.toList().reversed()
183:         CocktailDebugLogger.log("👀 CocktailCache.getRecentlyViewedCocktails() returning ${recent.size} items")
184:         return recent
185:     }
186:     
187:     /**
188:      * Clear all cached cocktails.
189:      */
190:     fun clearCache() {
191:         cocktailCache.clear()
192:         recentlyViewedCache.clear()
193:         // Clear from persistent storage
194:         settings.remove(ALL_COCKTAILS_KEY)
195:         settings.remove(RECENTLY_VIEWED_KEY)
196:     }
197:     
198:     /**
199:      * Check if a cocktail is cached.
200:      */
201:     suspend fun isCocktailCached(id: String): Boolean {
202:         return cocktailCache.get(id) != null
203:     }
204:     
205:     /**
206:      * Get the number of cached cocktails.
207:      */
208:     fun getCachedCocktailCount(): Int {
209:         return cocktailCache.size
210:     }
211: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/data/config/AppConfigImpl.kt
````kotlin
 1: package com.cocktailcraft.data.config
 2: 
 3: import com.cocktailcraft.domain.config.AppConfig
 4: 
 5: /**
 6:  * Implementation of AppConfig that provides configuration values for the application.
 7:  * This is an adapter in the hexagonal architecture that implements the port defined in the domain layer.
 8:  */
 9: class AppConfigImpl : AppConfig {
10:     override val apiBaseUrl: String = "https://www.thecocktaildb.com/api/json/v1/1"
11: 
12:     override val imageBaseUrl: String = "https://www.thecocktaildb.com/images"
13: 
14:     override val ingredientsImagePath: String = "ingredients"
15: 
16:     override val cocktailsImagePath: String = "media/k/drinks"
17: 
18:     // Timeouts for network requests - generous defaults
19:     override val networkTimeoutMs: Long = 30000 // 30 seconds
20: 
21:     // Progressive timeouts for retries
22:     override val initialNetworkTimeoutMs: Long = 10000 // 10 seconds for first attempt
23:     override val maxNetworkTimeoutMs: Long = 60000 // 60 seconds max timeout
24: 
25:     // Number of retries for network operations
26:     override val maxRetries: Int = 3
27: 
28:     // Storage keys
29:     override val favoritesStorageKey: String = "favorite_cocktails"
30:     override val ordersStorageKey: String = "orders_data"
31:     override val recentlyViewedStorageKey: String = "recently_viewed_cocktails"
32:     override val offlineModeEnabledKey: String = "offline_mode_enabled"
33: 
34:     // Cache configuration
35:     override val cacheExpirationMs: Long = 86400000 // 24 hours cache expiration
36:     override val maxOfflineCocktails: Int = 50 // Maximum number of cocktails to cache
37: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/data/remote/CocktailApi.kt
````kotlin
  1: package com.cocktailcraft.data.remote
  2: 
  3: import io.ktor.client.HttpClient
  4: import io.ktor.client.call.*
  5: import io.ktor.client.plugins.timeout
  6: import io.ktor.client.request.*
  7: import io.ktor.http.*
  8: 
  9: interface CocktailApi {
 10:     suspend fun searchCocktailsByName(name: String): List<CocktailDto>
 11:     suspend fun searchCocktailsByFirstLetter(letter: Char): List<CocktailDto>
 12:     suspend fun getCocktailById(id: String): CocktailDto?
 13:     suspend fun getRandomCocktail(): CocktailDto?
 14:     suspend fun filterByIngredient(ingredient: String): List<CocktailDto>
 15:     suspend fun filterByAlcoholic(alcoholic: Boolean): List<CocktailDto>
 16:     suspend fun filterByCategory(category: String): List<CocktailDto>
 17:     suspend fun filterByGlass(glass: String): List<CocktailDto>
 18:     suspend fun getCategories(): List<CategoryDto>
 19:     suspend fun getGlasses(): List<GlassDto>
 20:     suspend fun getIngredients(): List<IngredientDto>
 21:     suspend fun getAlcoholicFilters(): List<AlcoholicFilterDto>
 22:     
 23:     // New method to check API connectivity
 24:     suspend fun pingApi(): Boolean
 25: }
 26: 
 27: class CocktailApiImpl(
 28:     private val client: HttpClient
 29: ) : CocktailApi {
 30:     
 31:     override suspend fun searchCocktailsByName(name: String): List<CocktailDto> {
 32:         val response = client.get("$BASE_URL/search.php") {
 33:             parameter("s", name)
 34:         }.body<CocktailResponse>()
 35:         
 36:         return response.drinks ?: emptyList()
 37:     }
 38:     
 39:     override suspend fun searchCocktailsByFirstLetter(letter: Char): List<CocktailDto> {
 40:         val response = client.get("$BASE_URL/search.php") {
 41:             parameter("f", letter.toString())
 42:         }.body<CocktailResponse>()
 43:         
 44:         return response.drinks ?: emptyList()
 45:     }
 46:     
 47:     override suspend fun getCocktailById(id: String): CocktailDto? {
 48:         try {
 49:             // Force API call to specifically use the lookup endpoint for full details
 50:             val response = client.get("$BASE_URL/lookup.php") {
 51:                 parameter("i", id)
 52:                 // Add a timeout to ensure the request doesn't hang
 53:                 timeout {
 54:                     requestTimeoutMillis = 10000
 55:                 }
 56:             }.body<CocktailResponse>()
 57:             
 58:             val cocktail = response.drinks?.firstOrNull()
 59:             
 60:             if (cocktail == null) {
 61:                 return null
 62:             }
 63:             
 64:             return cocktail
 65:         } catch (e: Exception) {
 66:             throw e
 67:         }
 68:     }
 69:     
 70:     override suspend fun getRandomCocktail(): CocktailDto? {
 71:         val response = client.get("$BASE_URL/random.php")
 72:             .body<CocktailResponse>()
 73:         
 74:         return response.drinks?.firstOrNull()
 75:     }
 76:     
 77:     override suspend fun filterByIngredient(ingredient: String): List<CocktailDto> {
 78:         val response = client.get("$BASE_URL/filter.php") {
 79:             parameter("i", ingredient)
 80:         }.body<CocktailResponse>()
 81:         
 82:         return response.drinks ?: emptyList()
 83:     }
 84:     
 85:     override suspend fun filterByAlcoholic(alcoholic: Boolean): List<CocktailDto> {
 86:         val filter = if (alcoholic) "Alcoholic" else "Non_Alcoholic"
 87:         val response = client.get("$BASE_URL/filter.php") {
 88:             parameter("a", filter)
 89:         }.body<CocktailResponse>()
 90:         
 91:         return response.drinks ?: emptyList()
 92:     }
 93:     
 94:     override suspend fun filterByCategory(category: String): List<CocktailDto> {
 95:         try {
 96:             // First get the list of cocktails in this category (only ID, name, and thumbnail)
 97:             val response = client.get("$BASE_URL/filter.php") {
 98:                 parameter("c", category)
 99:             }.body<CocktailResponse>()
100:             
101:             val basicCocktails = response.drinks ?: emptyList()
102:             
103:             // If we need full details (with instructions) for these cocktails later,
104:             // we'll need to fetch them individually using lookup.php
105:             return basicCocktails
106:         } catch (e: Exception) {
107:             return emptyList()
108:         }
109:     }
110:     
111:     override suspend fun filterByGlass(glass: String): List<CocktailDto> {
112:         val response = client.get("$BASE_URL/filter.php") {
113:             parameter("g", glass)
114:         }.body<CocktailResponse>()
115:         
116:         return response.drinks ?: emptyList()
117:     }
118:     
119:     override suspend fun getCategories(): List<CategoryDto> {
120:         val response = client.get("$BASE_URL/list.php") {
121:             parameter("c", "list")
122:         }.body<CategoryResponse>()
123:         
124:         return response.categories ?: emptyList()
125:     }
126:     
127:     override suspend fun getGlasses(): List<GlassDto> {
128:         val response = client.get("$BASE_URL/list.php") {
129:             parameter("g", "list")
130:         }.body<GlassResponse>()
131:         
132:         return response.glasses ?: emptyList()
133:     }
134:     
135:     override suspend fun getIngredients(): List<IngredientDto> {
136:         val response = client.get("$BASE_URL/list.php") {
137:             parameter("i", "list")
138:         }.body<IngredientResponse>()
139:         
140:         return response.ingredients ?: emptyList()
141:     }
142:     
143:     override suspend fun getAlcoholicFilters(): List<AlcoholicFilterDto> {
144:         val response = client.get("$BASE_URL/list.php") {
145:             parameter("a", "list")
146:         }.body<AlcoholicFilterResponse>()
147:         
148:         return response.filters ?: emptyList()
149:     }
150:     
151:     // Implemented the ping method
152:     override suspend fun pingApi(): Boolean {
153:         return try {
154:             // Use a lightweight endpoint for checking connectivity
155:             val response = client.get("$BASE_URL/random.php")
156:             response.status.isSuccess()
157:         } catch (e: Exception) {
158:             false
159:         }
160:     }
161:     
162:     companion object {
163:         private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1"
164:         
165:         fun create(): CocktailApi {
166:             return CocktailApiImpl(
167:                 client = HttpClient() // Assuming a default client setup
168:             )
169:         }
170:     }
171: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/data/remote/CocktailApiImpl.kt
````kotlin
1: 
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/data/remote/CocktailDto.kt
````kotlin
  1: package com.cocktailcraft.data.remote
  2: 
  3: import kotlinx.serialization.SerialName
  4: import kotlinx.serialization.Serializable
  5: import com.cocktailcraft.domain.model.CocktailIngredient
  6: 
  7: @Serializable
  8: data class CocktailResponse(
  9:     @SerialName("drinks")
 10:     val drinks: List<CocktailDto>? = null
 11: )
 12: 
 13: @Serializable
 14: data class CocktailDto(
 15:     @SerialName("idDrink")
 16:     val id: String,
 17:     
 18:     @SerialName("strDrink")
 19:     val name: String,
 20:     
 21:     @SerialName("strDrinkAlternate")
 22:     val alternateName: String? = null,
 23:     
 24:     @SerialName("strTags")
 25:     val tags: String? = null,
 26:     
 27:     @SerialName("strCategory")
 28:     val category: String? = null,
 29:     
 30:     @SerialName("strIBA")
 31:     val iba: String? = null,
 32:     
 33:     @SerialName("strAlcoholic")
 34:     val alcoholic: String? = null,
 35:     
 36:     @SerialName("strGlass")
 37:     val glass: String? = null,
 38:     
 39:     @SerialName("strInstructions")
 40:     val instructions: String? = null,
 41:     
 42:     @SerialName("strDrinkThumb")
 43:     val imageUrl: String? = null,
 44:     
 45:     @SerialName("strIngredient1")
 46:     val ingredient1: String? = null,
 47:     
 48:     @SerialName("strIngredient2")
 49:     val ingredient2: String? = null,
 50:     
 51:     @SerialName("strIngredient3")
 52:     val ingredient3: String? = null,
 53:     
 54:     @SerialName("strIngredient4")
 55:     val ingredient4: String? = null,
 56:     
 57:     @SerialName("strIngredient5")
 58:     val ingredient5: String? = null,
 59:     
 60:     @SerialName("strIngredient6")
 61:     val ingredient6: String? = null,
 62:     
 63:     @SerialName("strIngredient7")
 64:     val ingredient7: String? = null,
 65:     
 66:     @SerialName("strIngredient8")
 67:     val ingredient8: String? = null,
 68:     
 69:     @SerialName("strIngredient9")
 70:     val ingredient9: String? = null,
 71:     
 72:     @SerialName("strIngredient10")
 73:     val ingredient10: String? = null,
 74:     
 75:     @SerialName("strIngredient11")
 76:     val ingredient11: String? = null,
 77:     
 78:     @SerialName("strIngredient12")
 79:     val ingredient12: String? = null,
 80:     
 81:     @SerialName("strIngredient13")
 82:     val ingredient13: String? = null,
 83:     
 84:     @SerialName("strIngredient14")
 85:     val ingredient14: String? = null,
 86:     
 87:     @SerialName("strIngredient15")
 88:     val ingredient15: String? = null,
 89:     
 90:     @SerialName("strMeasure1")
 91:     val measure1: String? = null,
 92:     
 93:     @SerialName("strMeasure2")
 94:     val measure2: String? = null,
 95:     
 96:     @SerialName("strMeasure3")
 97:     val measure3: String? = null,
 98:     
 99:     @SerialName("strMeasure4")
100:     val measure4: String? = null,
101:     
102:     @SerialName("strMeasure5")
103:     val measure5: String? = null,
104:     
105:     @SerialName("strMeasure6")
106:     val measure6: String? = null,
107:     
108:     @SerialName("strMeasure7")
109:     val measure7: String? = null,
110:     
111:     @SerialName("strMeasure8")
112:     val measure8: String? = null,
113:     
114:     @SerialName("strMeasure9")
115:     val measure9: String? = null,
116:     
117:     @SerialName("strMeasure10")
118:     val measure10: String? = null,
119:     
120:     @SerialName("strMeasure11")
121:     val measure11: String? = null,
122:     
123:     @SerialName("strMeasure12")
124:     val measure12: String? = null,
125:     
126:     @SerialName("strMeasure13")
127:     val measure13: String? = null,
128:     
129:     @SerialName("strMeasure14")
130:     val measure14: String? = null,
131:     
132:     @SerialName("strMeasure15")
133:     val measure15: String? = null,
134:     
135:     @SerialName("strImageSource")
136:     val imageSource: String? = null,
137:     
138:     @SerialName("strImageAttribution")
139:     val imageAttribution: String? = null,
140:     
141:     @SerialName("strCreativeCommonsConfirmed")
142:     val creativeCommonsConfirmed: String? = null,
143:     
144:     @SerialName("dateModified")
145:     val dateModified: String? = null
146: ) {
147:     fun getIngredients(): List<CocktailIngredient> {
148:         val ingredients = mutableListOf<CocktailIngredient>()
149:         
150:         // Helper function to get ingredient and measure by index
151:         fun getIngredientAndMeasure(index: Int): Pair<String?, String?> {
152:             return when (index) {
153:                 1 -> ingredient1 to measure1
154:                 2 -> ingredient2 to measure2
155:                 3 -> ingredient3 to measure3
156:                 4 -> ingredient4 to measure4
157:                 5 -> ingredient5 to measure5
158:                 6 -> ingredient6 to measure6
159:                 7 -> ingredient7 to measure7
160:                 8 -> ingredient8 to measure8
161:                 9 -> ingredient9 to measure9
162:                 10 -> ingredient10 to measure10
163:                 11 -> ingredient11 to measure11
164:                 12 -> ingredient12 to measure12
165:                 13 -> ingredient13 to measure13
166:                 14 -> ingredient14 to measure14
167:                 15 -> ingredient15 to measure15
168:                 else -> null to null
169:             }
170:         }
171:         
172:         // Collect all non-null ingredients with their measures
173:         (1..15).forEach { index ->
174:             val (ingredient, measure) = getIngredientAndMeasure(index)
175:             if (!ingredient.isNullOrBlank()) {
176:                 ingredients.add(
177:                     CocktailIngredient(
178:                         name = ingredient.trim(),
179:                         measure = measure?.trim() ?: ""
180:                     )
181:                 )
182:             }
183:         }
184:         
185:         return ingredients
186:     }
187: }
188: 
189: @Serializable
190: data class CategoryResponse(
191:     @SerialName("drinks")
192:     val categories: List<CategoryDto>? = null
193: )
194: 
195: @Serializable
196: data class CategoryDto(
197:     @SerialName("strCategory")
198:     val name: String
199: )
200: 
201: @Serializable
202: data class GlassResponse(
203:     @SerialName("drinks")
204:     val glasses: List<GlassDto>? = null
205: )
206: 
207: @Serializable
208: data class GlassDto(
209:     @SerialName("strGlass")
210:     val name: String
211: )
212: 
213: @Serializable
214: data class IngredientResponse(
215:     @SerialName("drinks")
216:     val ingredients: List<IngredientDto>? = null
217: )
218: 
219: @Serializable
220: data class IngredientDto(
221:     @SerialName("strIngredient1")
222:     val name: String
223: )
224: 
225: @Serializable
226: data class AlcoholicFilterResponse(
227:     @SerialName("drinks")
228:     val filters: List<AlcoholicFilterDto>? = null
229: )
230: 
231: @Serializable
232: data class AlcoholicFilterDto(
233:     @SerialName("strAlcoholic")
234:     val name: String
235: )
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/AuthRepositoryImpl.kt
````kotlin
  1: package com.cocktailcraft.data.repository
  2: 
  3: import com.cocktailcraft.domain.model.User
  4: import com.cocktailcraft.domain.model.UserPreferences
  5: import com.cocktailcraft.domain.model.Address
  6: import com.cocktailcraft.domain.repository.AuthRepository
  7: import com.russhwolf.settings.Settings
  8: import kotlinx.coroutines.flow.Flow
  9: import kotlinx.coroutines.flow.flow
 10: import kotlinx.serialization.decodeFromString
 11: import kotlinx.serialization.encodeToString
 12: import kotlinx.serialization.json.Json
 13: import java.util.UUID
 14: 
 15: class AuthRepositoryImpl(
 16:     private val settings: Settings,
 17:     private val json: Json
 18: ) : AuthRepository {
 19: 
 20:     // Authentication methods
 21:     override suspend fun signUp(email: String, password: String): Flow<Boolean> = flow {
 22:         try {
 23:             // Check if user already exists
 24:             if (getUserByEmail(email) != null) {
 25:                 emit(false)
 26:                 return@flow
 27:             }
 28: 
 29:             // Create new user
 30:             val user = User(
 31:                 id = UUID.randomUUID().toString(),
 32:                 email = email,
 33:                 name = "",
 34:                 preferences = emptyMap()
 35:             )
 36: 
 37:             // Save user
 38:             val users = getCurrentUsers().toMutableList()
 39:             users.add(user)
 40:             saveUsers(users)
 41: 
 42:             // Save credentials
 43:             saveCredentials(email, password)
 44: 
 45:             // Set as current user
 46:             settings.putString(CURRENT_USER_ID_KEY, user.id)
 47: 
 48:             emit(true)
 49:         } catch (e: Exception) {
 50:             emit(false)
 51:         }
 52:     }
 53: 
 54:     override suspend fun signIn(email: String, password: String): Flow<Boolean> = flow {
 55:         try {
 56:             // Check credentials
 57:             val storedPassword = settings.getStringOrNull("password_$email")
 58:             if (storedPassword != password) {
 59:                 emit(false)
 60:                 return@flow
 61:             }
 62: 
 63:             // Get user
 64:             val user = getUserByEmail(email)
 65:             if (user == null) {
 66:                 emit(false)
 67:                 return@flow
 68:             }
 69: 
 70:             // Set as current user
 71:             settings.putString(CURRENT_USER_ID_KEY, user.id)
 72: 
 73:             emit(true)
 74:         } catch (e: Exception) {
 75:             emit(false)
 76:         }
 77:     }
 78: 
 79:     override suspend fun signOut(): Flow<Boolean> = flow {
 80:         try {
 81:             // Clear current user
 82:             settings.remove(CURRENT_USER_ID_KEY)
 83:             emit(true)
 84:         } catch (e: Exception) {
 85:             emit(false)
 86:         }
 87:     }
 88: 
 89:     override suspend fun resetPassword(email: String): Flow<Boolean> = flow {
 90:         try {
 91:             // In a real app, this would send an email with a reset link
 92:             // For this demo, we'll just reset the password to a default value
 93:             val user = getUserByEmail(email)
 94:             if (user == null) {
 95:                 emit(false)
 96:                 return@flow
 97:             }
 98: 
 99:             // Reset password to "password123"
100:             saveCredentials(email, "password123")
101:             emit(true)
102:         } catch (e: Exception) {
103:             emit(false)
104:         }
105:     }
106: 
107:     override suspend fun changePassword(oldPassword: String, newPassword: String): Flow<Boolean> = flow {
108:         try {
109:             val currentUser = getCurrentUserSync()
110:             if (currentUser == null) {
111:                 emit(false)
112:                 return@flow
113:             }
114: 
115:             // Check old password
116:             val storedPassword = settings.getStringOrNull("password_${currentUser.email}")
117:             if (storedPassword != oldPassword) {
118:                 emit(false)
119:                 return@flow
120:             }
121: 
122:             // Update password
123:             saveCredentials(currentUser.email, newPassword)
124:             emit(true)
125:         } catch (e: Exception) {
126:             emit(false)
127:         }
128:     }
129: 
130:     override suspend fun isUserSignedIn(): Flow<Boolean> = flow {
131:         val currentUserId = settings.getStringOrNull(CURRENT_USER_ID_KEY)
132:         emit(currentUserId != null)
133:     }
134: 
135:     override suspend fun getCurrentUser(): Flow<User?> = flow {
136:         emit(getCurrentUserSync())
137:     }
138: 
139:     // Profile management methods
140:     override suspend fun updateUserName(name: String): Flow<Boolean> = flow {
141:         try {
142:             val currentUser = getCurrentUserSync()
143:             if (currentUser == null) {
144:                 emit(false)
145:                 return@flow
146:             }
147: 
148:             // Update user
149:             val updatedUser = currentUser.copy(name = name)
150:             updateUser(updatedUser)
151:             emit(true)
152:         } catch (e: Exception) {
153:             emit(false)
154:         }
155:     }
156: 
157:     override suspend fun updateUserEmail(email: String, password: String): Flow<Boolean> = flow {
158:         try {
159:             val currentUser = getCurrentUserSync()
160:             if (currentUser == null) {
161:                 emit(false)
162:                 return@flow
163:             }
164: 
165:             // Check if email already exists
166:             if (getUserByEmail(email) != null && email != currentUser.email) {
167:                 emit(false)
168:                 return@flow
169:             }
170: 
171:             // Get old credentials
172:             val oldPassword = settings.getStringOrNull("password_${currentUser.email}")
173:             if (oldPassword != password) {
174:                 emit(false)
175:                 return@flow
176:             }
177: 
178:             // Remove old credentials
179:             settings.remove("password_${currentUser.email}")
180: 
181:             // Update user
182:             val updatedUser = currentUser.copy(email = email)
183:             updateUser(updatedUser)
184: 
185:             // Save new credentials
186:             saveCredentials(email, password)
187:             emit(true)
188:         } catch (e: Exception) {
189:             emit(false)
190:         }
191:     }
192: 
193:     override suspend fun updateUserAddress(address: Address): Flow<Boolean> = flow {
194:         try {
195:             val currentUser = getCurrentUserSync()
196:             if (currentUser == null) {
197:                 emit(false)
198:                 return@flow
199:             }
200: 
201:             // Update user
202:             val updatedUser = currentUser.copy(address = address)
203:             updateUser(updatedUser)
204:             emit(true)
205:         } catch (e: Exception) {
206:             emit(false)
207:         }
208:     }
209: 
210:     // Preferences management
211:     override suspend fun updateUserPreferences(preferences: UserPreferences): Flow<Boolean> = flow {
212:         try {
213:             val currentUser = getCurrentUserSync()
214:             if (currentUser == null) {
215:                 emit(false)
216:                 return@flow
217:             }
218: 
219:             // Convert UserPreferences to Map<String, String>
220:             val preferencesMap = mapOf(
221:                 "darkMode" to preferences.darkMode.toString(),
222:                 "followSystemTheme" to preferences.followSystemTheme.toString(),
223:                 "notificationsEnabled" to preferences.notificationsEnabled.toString(),
224:                 "language" to preferences.language
225:             )
226: 
227:             // Update user
228:             val updatedUser = currentUser.copy(preferences = preferencesMap)
229:             updateUser(updatedUser)
230:             emit(true)
231:         } catch (e: Exception) {
232:             emit(false)
233:         }
234:     }
235: 
236:     override suspend fun getUserPreferences(): Flow<UserPreferences> = flow {
237:         val currentUser = getCurrentUserSync()
238:         if (currentUser != null) {
239:             // Convert Map<String, String> to UserPreferences
240:             val prefs = currentUser.preferences
241:             val darkMode = prefs["darkMode"]?.toBoolean() ?: false
242:             val followSystemTheme = prefs["followSystemTheme"]?.toBoolean() ?: true
243:             val notificationsEnabled = prefs["notificationsEnabled"]?.toBoolean() ?: true
244:             val language = prefs["language"] ?: "en"
245: 
246:             emit(UserPreferences(
247:                 darkMode = darkMode,
248:                 followSystemTheme = followSystemTheme,
249:                 notificationsEnabled = notificationsEnabled,
250:                 language = language
251:             ))
252:         } else {
253:             emit(UserPreferences())
254:         }
255:     }
256: 
257:     // Helper methods
258:     private fun getCurrentUserSync(): User? {
259:         val currentUserId = settings.getStringOrNull(CURRENT_USER_ID_KEY) ?: return null
260:         val users = getCurrentUsers()
261:         return users.find { it.id == currentUserId }
262:     }
263: 
264:     private fun getUserByEmail(email: String): User? {
265:         val users = getCurrentUsers()
266:         return users.find { it.email == email }
267:     }
268: 
269:     private fun updateUser(user: User) {
270:         val users = getCurrentUsers().toMutableList()
271:         val index = users.indexOfFirst { it.id == user.id }
272:         if (index != -1) {
273:             users[index] = user
274:             saveUsers(users)
275:         }
276:     }
277: 
278:     private fun getCurrentUsers(): List<User> {
279:         val usersJson = settings.getStringOrNull(USERS_KEY) ?: "[]"
280:         return try {
281:             json.decodeFromString(usersJson)
282:         } catch (e: Exception) {
283:             emptyList()
284:         }
285:     }
286: 
287:     private fun saveUsers(users: List<User>) {
288:         val usersJson = json.encodeToString(users)
289:         settings.putString(USERS_KEY, usersJson)
290:     }
291: 
292:     private fun saveCredentials(email: String, password: String) {
293:         settings.putString("password_$email", password)
294:     }
295: 
296:     companion object {
297:         private const val USERS_KEY = "users"
298:         private const val CURRENT_USER_ID_KEY = "current_user_id"
299:     }
300: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CartRepositoryImpl.kt
````kotlin
 1: package com.cocktailcraft.data.repository
 2: 
 3: import com.cocktailcraft.domain.model.CocktailCartItem
 4: import com.cocktailcraft.domain.repository.CartRepository
 5: import com.russhwolf.settings.Settings
 6: import kotlinx.coroutines.flow.Flow
 7: import kotlinx.coroutines.flow.flow
 8: import kotlinx.serialization.decodeFromString
 9: import kotlinx.serialization.encodeToString
10: import kotlinx.serialization.json.Json
11: 
12: class CartRepositoryImpl(
13:     private val settings: Settings,
14:     private val json: Json
15: ) : CartRepository {
16: 
17:     override suspend fun getCartItems(): Flow<List<CocktailCartItem>> = flow {
18:         val cartJson = settings.getStringOrNull(CART_ITEMS_KEY) ?: "[]"
19:         try {
20:             val cartItems = json.decodeFromString<List<CocktailCartItem>>(cartJson)
21:             emit(cartItems)
22:         } catch (e: Exception) {
23:             emit(emptyList())
24:         }
25:     }
26: 
27:     override suspend fun addToCart(cartItem: CocktailCartItem) {
28:         val currentItems = getCurrentCartItems().toMutableList()
29:         val existingItemIndex = currentItems.indexOfFirst { it.cocktail.id == cartItem.cocktail.id }
30: 
31:         if (existingItemIndex != -1) {
32:             // Update existing item
33:             val existingItem = currentItems[existingItemIndex]
34:             currentItems[existingItemIndex] = existingItem.copy(
35:                 quantity = existingItem.quantity + cartItem.quantity
36:             )
37:         } else {
38:             // Add new item
39:             currentItems.add(cartItem)
40:         }
41: 
42:         saveCartItems(currentItems)
43:     }
44: 
45:     override suspend fun removeFromCart(cocktailId: String) {
46:         val currentItems = getCurrentCartItems().toMutableList()
47:         currentItems.removeAll { it.cocktail.id == cocktailId }
48:         saveCartItems(currentItems)
49:     }
50: 
51:     override suspend fun updateQuantity(cocktailId: String, quantity: Int) {
52:         val currentItems = getCurrentCartItems().toMutableList()
53:         val itemIndex = currentItems.indexOfFirst { it.cocktail.id == cocktailId }
54: 
55:         if (itemIndex != -1) {
56:             val item = currentItems[itemIndex]
57:             if (quantity <= 0) {
58:                 currentItems.removeAt(itemIndex)
59:             } else {
60:                 currentItems[itemIndex] = item.copy(quantity = quantity)
61:             }
62:             saveCartItems(currentItems)
63:         }
64:     }
65: 
66:     override suspend fun clearCart() {
67:         settings.remove(CART_ITEMS_KEY)
68:     }
69: 
70:     override suspend fun getCartTotal(): Flow<Double> = flow {
71:         val cartItems = getCurrentCartItems()
72:         val total = cartItems.sumOf { it.cocktail.price * it.quantity }
73:         emit(total)
74:     }
75: 
76:     private fun getCurrentCartItems(): List<CocktailCartItem> {
77:         val cartJson = settings.getStringOrNull(CART_ITEMS_KEY) ?: "[]"
78:         return try {
79:             json.decodeFromString(cartJson)
80:         } catch (e: Exception) {
81:             emptyList()
82:         }
83:     }
84: 
85:     private fun saveCartItems(items: List<CocktailCartItem>) {
86:         val cartJson = json.encodeToString(items)
87:         settings.putString(CART_ITEMS_KEY, cartJson)
88:     }
89: 
90:     companion object {
91:         private const val CART_ITEMS_KEY = "cocktail_cart_items"
92:     }
93: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CocktailRepositoryImpl.kt
````kotlin
  1: package com.cocktailcraft.data.repository
  2: 
  3: import com.cocktailcraft.data.cache.CocktailCache
  4: import com.cocktailcraft.data.remote.CocktailApi
  5: import com.cocktailcraft.data.remote.CocktailDto
  6: import com.cocktailcraft.domain.model.Cocktail
  7: import com.cocktailcraft.domain.model.CocktailIngredient
  8: import com.cocktailcraft.domain.repository.CocktailRepository
  9: import com.cocktailcraft.util.NetworkMonitor
 10: import com.cocktailcraft.util.CocktailDebugLogger
 11: import com.russhwolf.settings.Settings
 12: import kotlinx.coroutines.flow.Flow
 13: import kotlinx.coroutines.flow.flow
 14: import kotlinx.coroutines.flow.map
 15: import kotlinx.coroutines.flow.first
 16: import kotlinx.coroutines.delay
 17: import com.cocktailcraft.domain.config.AppConfig
 18: 
 19: /**
 20:  * Implementation of CocktailRepository that handles API interactions and data caching.
 21:  * 
 22:  * API Limitations & Workarounds:
 23:  * - Filter endpoints (filter.php) only return partial data (id, name, thumbnail)
 24:  * - Full cocktail details require individual lookup.php calls
 25:  * - Price, rating, popularity are not provided by API - we generate demo values
 26:  * - Advanced filtering (multi-ingredient, taste profiles) requires local filtering
 27:  * - The free API has rate limits, so we cache aggressively
 28:  */
 29: class CocktailRepositoryImpl(
 30:     private val api: CocktailApi,
 31:     private val settings: Settings,
 32:     private val appConfig: AppConfig,
 33:     private val networkMonitor: NetworkMonitor,
 34:     private val cocktailCache: CocktailCache
 35: ) : CocktailRepository {
 36: 
 37:     // Store all fetched cocktails for pagination (use companion object for persistence)
 38:     private var allCocktailsCache: List<Cocktail> 
 39:         get() = globalCocktailsCache
 40:         set(value) { globalCocktailsCache = value }
 41:     
 42:     private var lastFetchTime: Long
 43:         get() = globalLastFetchTime
 44:         set(value) { globalLastFetchTime = value }
 45: 
 46:     // Flag to force offline mode (user preference)
 47:     private var forceOfflineMode: Boolean
 48:         get() = settings.getBoolean(appConfig.offlineModeEnabledKey, false)
 49:         set(value) = settings.putBoolean(appConfig.offlineModeEnabledKey, value)
 50: 
 51:     // Check if we're currently offline
 52:     private suspend fun isOffline(): Boolean {
 53:         val networkOnline = networkMonitor.isOnline.first()
 54:         val result = forceOfflineMode || !networkOnline
 55:         CocktailDebugLogger.log("   🔍 isOffline check: forceOfflineMode=$forceOfflineMode, networkOnline=$networkOnline, result=$result")
 56:         return result
 57:     }
 58: 
 59:     override suspend fun searchCocktailsByName(name: String): Flow<List<Cocktail>> = flow {
 60:         try {
 61:             val cocktails = api.searchCocktailsByName(name).map { dto ->
 62:                 mapDtoToCocktail(dto)
 63:             }
 64:             emit(cocktails)
 65:         } catch (e: Exception) {
 66:             emit(emptyList())
 67:         }
 68:     }
 69: 
 70:     override suspend fun searchCocktailsByFirstLetter(letter: Char): Flow<List<Cocktail>> = flow {
 71:         try {
 72:             val cocktails = api.searchCocktailsByFirstLetter(letter).map { dto ->
 73:                 mapDtoToCocktail(dto)
 74:             }
 75:             emit(cocktails)
 76:         } catch (e: Exception) {
 77:             emit(emptyList())
 78:         }
 79:     }
 80: 
 81:     override suspend fun getCocktailById(id: String): Flow<Cocktail?> = flow {
 82:         try {
 83:             // First check if we have a cached cocktail with full details
 84:             val cachedCocktail = cocktailCache.getCachedCocktail(id)
 85:             
 86:             // If we have a cached cocktail with ingredients (full details), return it
 87:             if (cachedCocktail != null && 
 88:                 cachedCocktail.ingredients.isNotEmpty() && 
 89:                 cachedCocktail.ingredients.first().name != "Tap to view ingredients") {
 90:                 // Add to recently viewed when accessed from cache
 91:                 cocktailCache.addToRecentlyViewed(cachedCocktail)
 92:                 emit(cachedCocktail)
 93:                 return@flow
 94:             }
 95:             
 96:             // If we're offline, return whatever we have cached
 97:             if (isOffline()) {
 98:                 emit(cachedCocktail)
 99:                 return@flow
100:             }
101: 
102:             // If online, fetch full details from API
103:             try {
104:                 // Add a small delay to avoid rate limiting if called rapidly
105:                 delay(200)
106:                 
107:                 val dto = api.getCocktailById(id)
108: 
109:                 if (dto != null) {
110:                     val cocktail = mapDtoToCocktail(dto)
111: 
112:                     // Cache the full cocktail details
113:                     cocktailCache.cacheCocktail(cocktail)
114:                     
115:                     // Add to recently viewed
116:                     cocktailCache.addToRecentlyViewed(cocktail)
117:                     
118:                     // Update the in-memory cache as well
119:                     allCocktailsCache = allCocktailsCache.map { 
120:                         if (it.id == id) cocktail else it 
121:                     }
122: 
123:                     emit(cocktail)
124:                 } else {
125:                     // If API returns null, use cached version
126:                     emit(cachedCocktail)
127:                 }
128:             } catch (e: Exception) {
129:                 // On API error, return cached version
130:                 CocktailDebugLogger.log("Error fetching cocktail details for $id: ${e.message}")
131:                 emit(cachedCocktail)
132:             }
133:         } catch (e: Exception) {
134:             emit(null)
135:         }
136:     }
137: 
138:     override suspend fun getRandomCocktail(): Flow<Cocktail?> = flow {
139:         try {
140:             val dto = api.getRandomCocktail()
141:             if (dto != null) {
142:                 emit(mapDtoToCocktail(dto))
143:             } else {
144:                 emit(null)
145:             }
146:         } catch (e: Exception) {
147:             emit(null)
148:         }
149:     }
150: 
151:     override suspend fun filterByIngredient(ingredient: String): Flow<List<Cocktail>> = flow {
152:         try {
153:             val cocktails = api.filterByIngredient(ingredient).map { dto ->
154:                 mapDtoToCocktail(dto)
155:             }
156:             emit(cocktails)
157:         } catch (e: Exception) {
158:             emit(emptyList())
159:         }
160:     }
161: 
162:     override suspend fun filterByAlcoholic(alcoholic: Boolean): Flow<List<Cocktail>> = flow {
163:         try {
164:             val cocktails = api.filterByAlcoholic(alcoholic).map { dto ->
165:                 mapDtoToCocktail(dto)
166:             }
167:             emit(cocktails)
168:         } catch (e: Exception) {
169:             emit(emptyList())
170:         }
171:     }
172: 
173:     override suspend fun filterByCategory(category: String): Flow<List<Cocktail>> = flow {
174:         CocktailDebugLogger.log("🏷️ filterByCategory() called with category: $category")
175:         
176:         // Use the new lazy loading method
177:         fetchCocktailsByCategory(category).collect { cocktails ->
178:             emit(cocktails)
179:         }
180:     }
181:     
182:     /**
183:      * Lazily fetch cocktails by category with caching and rate limiting
184:      */
185:     private suspend fun fetchCocktailsByCategory(category: String): Flow<List<Cocktail>> = flow {
186:         CocktailDebugLogger.log("🔄 fetchCocktailsByCategory() called for: $category")
187:         
188:         try {
189:             // First check category-specific cache
190:             val categoryCachedCocktails = categoryCacheMap[category] ?: emptyList()
191:             val categoryLastFetch = categoryLastFetchMap[category] ?: 0L
192:             val categoryCacheAge = System.currentTimeMillis() - categoryLastFetch
193:             
194:             CocktailDebugLogger.log("   - Category cache size: ${categoryCachedCocktails.size}")
195:             CocktailDebugLogger.log("   - Category cache age: ${categoryCacheAge/1000}s")
196:             
197:             // Emit cached data immediately if available
198:             if (categoryCachedCocktails.isNotEmpty()) {
199:                 CocktailDebugLogger.log("   ✅ Emitting ${categoryCachedCocktails.size} cached cocktails for $category")
200:                 emit(categoryCachedCocktails.sortedByDescending { it.dateAdded })
201:                 
202:                 // If cache is fresh, don't fetch from API
203:                 if (categoryCacheAge < CACHE_VALIDITY_MS) {
204:                     CocktailDebugLogger.log("   ✅ Category cache is fresh, skipping API call")
205:                     return@flow
206:                 }
207:             }
208:             
209:             // Check if we're offline
210:             if (isOffline()) {
211:                 CocktailDebugLogger.log("   📴 Offline mode - using cache only")
212:                 // Try general cache if category cache is empty
213:                 if (categoryCachedCocktails.isEmpty()) {
214:                     val generalCache = cocktailCache.getAllCachedCocktails()
215:                         .filter { it.category == category }
216:                     if (generalCache.isNotEmpty()) {
217:                         emit(generalCache.sortedByDescending { it.dateAdded })
218:                     }
219:                 }
220:                 return@flow
221:             }
222:             
223:             // Apply rate limiting with exponential backoff
224:             val timeSinceLastCall = System.currentTimeMillis() - lastApiCallTime
225:             val requiredInterval = maxOf(MIN_API_CALL_INTERVAL_MS, rateLimitBackoffMs)
226:             
227:             if (timeSinceLastCall < requiredInterval) {
228:                 val waitTime = requiredInterval - timeSinceLastCall
229:                 CocktailDebugLogger.log("   ⏳ Rate limiting: waiting ${waitTime}ms before API call")
230:                 delay(waitTime)
231:             }
232:             
233:             // Check API connectivity
234:             if (!pingApiInternal()) {
235:                 CocktailDebugLogger.log("   ❌ API unreachable")
236:                 if (categoryCachedCocktails.isNotEmpty()) {
237:                     return@flow
238:                 }
239:                 throw Exception("API is not reachable")
240:             }
241:             
242:             CocktailDebugLogger.log("   📡 Fetching $category cocktails from API...")
243:             lastApiCallTime = System.currentTimeMillis()
244:             
245:             try {
246:                 val basicCocktails = api.filterByCategory(category)
247:                 
248:                 // Reset backoff on successful call
249:                 rateLimitBackoffMs = 0
250:                 
251:                 // Map and cache cocktails
252:                 val enrichedCocktails = basicCocktails.map { basicDto ->
253:                     try {
254:                         // Check if we already have full data
255:                         if (!basicDto.instructions.isNullOrBlank()) {
256:                             mapDtoToCocktail(basicDto)
257:                         } else {
258:                             // Try to get from cache first
259:                             val cachedCocktail = cocktailCache.getCachedCocktail(basicDto.id)
260:                             if (cachedCocktail != null) {
261:                                 cachedCocktail
262:                             } else {
263:                                 // If not in cache, just use basic data
264:                                 // Full details will be fetched when user clicks on the cocktail
265:                                 mapDtoToCocktail(basicDto)
266:                             }
267:                         }
268:                     } catch (e: Exception) {
269:                         mapDtoToCocktail(basicDto)
270:                     }
271:                 }
272:                 
273:                 CocktailDebugLogger.log("   ✅ Fetched ${enrichedCocktails.size} cocktails for $category")
274:                 
275:                 // Update category cache
276:                 categoryCacheMap[category] = enrichedCocktails
277:                 categoryLastFetchMap[category] = System.currentTimeMillis()
278:                 
279:                 // Cache individual cocktails
280:                 enrichedCocktails.forEach { cocktail ->
281:                     cocktailCache.cacheCocktail(cocktail)
282:                 }
283:                 
284:                 emit(enrichedCocktails.sortedByDescending { it.dateAdded })
285:                 
286:             } catch (e: Exception) {
287:                 CocktailDebugLogger.log("   ❌ API call failed: ${e.message}")
288:                 
289:                 // Check if it's a rate limit error
290:                 if (e.message?.contains("429") == true || e.message?.contains("Too Many Requests") == true) {
291:                     CocktailDebugLogger.log("   ⚠️ Rate limited - applying exponential backoff")
292:                     // Apply exponential backoff
293:                     rateLimitBackoffMs = minOf(
294:                         if (rateLimitBackoffMs == 0L) 2000L else rateLimitBackoffMs * 2,
295:                         MAX_BACKOFF_MS
296:                     )
297:                     CocktailDebugLogger.log("   ⏰ Next backoff: ${rateLimitBackoffMs}ms")
298:                 }
299:                 
300:                 // If we have cached data, use it
301:                 if (categoryCachedCocktails.isNotEmpty()) {
302:                     CocktailDebugLogger.log("   ✅ Using stale cache due to API error")
303:                     return@flow
304:                 }
305:                 
306:                 // Try general cache as last resort
307:                 val generalCache = cocktailCache.getAllCachedCocktails()
308:                     .filter { it.category == category }
309:                 if (generalCache.isNotEmpty()) {
310:                     emit(generalCache.sortedByDescending { it.dateAdded })
311:                 } else {
312:                     throw e
313:                 }
314:             }
315:         } catch (e: Exception) {
316:             CocktailDebugLogger.log("   ❌ fetchCocktailsByCategory failed: ${e.message}")
317:             emit(emptyList())
318:         }
319:     }
320: 
321:     override suspend fun filterByGlass(glass: String): Flow<List<Cocktail>> = flow {
322:         try {
323:             val cocktails = api.filterByGlass(glass).map { dto ->
324:                 mapDtoToCocktail(dto)
325:             }
326:             emit(cocktails)
327:         } catch (e: Exception) {
328:             emit(emptyList())
329:         }
330:     }
331: 
332:     override suspend fun getCategories(): Flow<List<String>> = flow {
333:         try {
334:             val categories = api.getCategories().map { it.name }
335:             emit(categories)
336:         } catch (e: Exception) {
337:             emit(emptyList())
338:         }
339:     }
340: 
341:     override suspend fun getGlasses(): Flow<List<String>> = flow {
342:         try {
343:             val glasses = api.getGlasses().map { it.name }
344:             emit(glasses)
345:         } catch (e: Exception) {
346:             emit(emptyList())
347:         }
348:     }
349: 
350:     override suspend fun getIngredients(): Flow<List<String>> = flow {
351:         try {
352:             val ingredients = api.getIngredients().map { it.name }
353:             emit(ingredients)
354:         } catch (e: Exception) {
355:             emit(emptyList())
356:         }
357:     }
358: 
359:     override suspend fun getAlcoholicFilters(): Flow<List<String>> = flow {
360:         try {
361:             val filters = api.getAlcoholicFilters().map { it.name }
362:             emit(filters)
363:         } catch (e: Exception) {
364:             emit(emptyList())
365:         }
366:     }
367: 
368:     override suspend fun getFavoriteCocktails(): Flow<List<Cocktail>> = flow {
369:         val favoriteIds = settings.getStringOrNull(appConfig.favoritesStorageKey)
370:             ?.split(",")
371:             ?.filter { it.isNotEmpty() }
372:             ?: emptyList()
373: 
374:         val favoriteCocktails = mutableListOf<Cocktail>()
375: 
376:         // If we're offline, only use cached favorites
377:         if (isOffline()) {
378:             for (id in favoriteIds) {
379:                 val cachedCocktail = cocktailCache.getCachedCocktail(id)
380:                 if (cachedCocktail != null) {
381:                     favoriteCocktails.add(cachedCocktail)
382:                 }
383:             }
384:             emit(favoriteCocktails)
385:             return@flow
386:         }
387: 
388:         // If online, get favorites from API and cache them
389:         for (id in favoriteIds) {
390:             getCocktailById(id).collect { cocktail ->
391:                 if (cocktail != null) {
392:                     favoriteCocktails.add(cocktail)
393:                     // Ensure favorites are cached
394:                     cocktailCache.cacheCocktail(cocktail)
395:                 }
396:             }
397:         }
398: 
399:         emit(favoriteCocktails)
400:     }
401: 
402:     override suspend fun addToFavorites(cocktail: Cocktail) {
403:         val currentFavorites = settings.getStringOrNull(appConfig.favoritesStorageKey)
404:             ?.split(",")
405:             ?.filter { it.isNotEmpty() }
406:             ?.toMutableList()
407:             ?: mutableListOf()
408: 
409:         if (!currentFavorites.contains(cocktail.id)) {
410:             currentFavorites.add(cocktail.id)
411:             settings.putString(appConfig.favoritesStorageKey, currentFavorites.joinToString(","))
412:         }
413:     }
414: 
415:     override suspend fun removeFromFavorites(cocktail: Cocktail) {
416:         val currentFavorites = settings.getStringOrNull(appConfig.favoritesStorageKey)
417:             ?.split(",")
418:             ?.filter { it.isNotEmpty() }
419:             ?.toMutableList()
420:             ?: mutableListOf()
421: 
422:         currentFavorites.remove(cocktail.id)
423:         settings.putString(appConfig.favoritesStorageKey, currentFavorites.joinToString(","))
424:     }
425: 
426:     override suspend fun isCocktailFavorite(id: String): Flow<Boolean> = flow {
427:         val favoriteIds = settings.getStringOrNull(appConfig.favoritesStorageKey)
428:             ?.split(",")
429:             ?.filter { it.isNotEmpty() }
430:             ?: emptyList()
431: 
432:         emit(favoriteIds.contains(id))
433:     }
434: 
435:     override suspend fun getCocktailsSortedByNewest(): Flow<List<Cocktail>> = flow {
436:         CocktailDebugLogger.log("📚 CocktailRepositoryImpl.getCocktailsSortedByNewest() called")
437:         CocktailDebugLogger.log("   🎯 LAZY LOADING: Only fetching 'Cocktail' category initially")
438:         
439:         // Instead of fetching all categories, just fetch "Cocktail" category
440:         fetchCocktailsByCategory("Cocktail").collect { cocktails ->
441:             emit(cocktails)
442:         }
443:     }
444: 
445:     override suspend fun getCocktailsSortedByPriceLowToHigh(): Flow<List<Cocktail>> = flow {
446:         try {
447:             // Use the same method as getCocktailsSortedByNewest to get all cocktails
448:             getCocktailsSortedByNewest().collect { allCocktails ->
449:                 emit(allCocktails.sortedBy { it.price })
450:             }
451:         } catch (e: Exception) {
452:             emit(emptyList())
453:         }
454:     }
455: 
456:     override suspend fun getCocktailsSortedByPriceHighToLow(): Flow<List<Cocktail>> = flow {
457:         try {
458:             // Use the same method as getCocktailsSortedByNewest to get all cocktails
459:             getCocktailsSortedByNewest().collect { allCocktails ->
460:                 emit(allCocktails.sortedByDescending { it.price })
461:             }
462:         } catch (e: Exception) {
463:             emit(emptyList())
464:         }
465:     }
466: 
467:     override suspend fun getCocktailsSortedByPopularity(): Flow<List<Cocktail>> = flow {
468:         try {
469:             // Use the same method as getCocktailsSortedByNewest to get all cocktails
470:             getCocktailsSortedByNewest().collect { allCocktails ->
471:                 emit(allCocktails.sortedByDescending { it.popularity })
472:             }
473:         } catch (e: Exception) {
474:             emit(emptyList())
475:         }
476:     }
477: 
478:     override suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Cocktail>> = flow {
479:         try {
480:             // Use the same method as getCocktailsSortedByNewest to get all cocktails
481:             getCocktailsSortedByNewest().collect { allCocktails ->
482:                 emit(allCocktails.filter { it.price in minPrice..maxPrice })
483:             }
484:         } catch (e: Exception) {
485:             emit(emptyList())
486:         }
487:     }
488: 
489:     private fun mapDtoToCocktail(dto: CocktailDto): Cocktail {
490:         return Cocktail(
491:             id = dto.id,
492:             name = dto.name,
493:             instructions = dto.instructions ?: "Tap to view recipe",
494:             imageUrl = dto.imageUrl,
495:             price = generateRandomPrice(),
496:             ingredients = dto.getIngredients().ifEmpty { 
497:                 // If no ingredients (from filter endpoint), add placeholder
498:                 listOf(CocktailIngredient("Tap to view ingredients", ""))
499:             },
500:             rating = generateRandomRating(),
501:             category = dto.category,
502:             glass = dto.glass,
503:             alcoholic = dto.alcoholic ?: "Unknown",
504:             dateAdded = parseDateToTimestamp(dto.dateModified),
505:             popularity = generateRandomPopularity()
506:         )
507:     }
508: 
509:     // Helper functions for demo data
510:     private fun generateRandomPrice(): Double {
511:         return (500..1500).random() / 100.0 // Random price between $5.00 and $15.00
512:     }
513: 
514:     private fun generateRandomRating(): Float {
515:         return (30..50).random() / 10.0f // Random rating between 3.0 and 5.0
516:     }
517: 
518:     private fun generateRandomPopularity(): Int {
519:         return (1..100).random() // Random popularity score between 1 and 100
520:     }
521: 
522:     private fun parseDateToTimestamp(dateStr: String?): Long {
523:         return try {
524:             // If date string is null or empty, return current timestamp
525:             if (dateStr.isNullOrBlank()) {
526:                 System.currentTimeMillis()
527:             } else {
528:                 // Parse the date string (format: "YYYY-MM-DD HH:mm:ss")
529:                 // For simplicity, we'll just use current timestamp if parsing fails
530:                 System.currentTimeMillis()
531:             }
532:         } catch (e: Exception) {
533:             System.currentTimeMillis()
534:         }
535:     }
536: 
537:     // Add a method to get consistent cocktail image URLs with fallbacks
538:     override fun getCocktailImageUrl(cocktail: Cocktail): String {
539:         // Return the direct imageUrl if available
540:         if (!cocktail.imageUrl.isNullOrBlank()) {
541:             return cocktail.imageUrl
542:         }
543: 
544:         // If no image URL, construct one from the ID if possible
545:         return if (cocktail.id.isNotBlank()) {
546:             "${appConfig.imageBaseUrl}/${appConfig.cocktailsImagePath}/${cocktail.id}.jpg"
547:         } else {
548:             // Return an empty string as fallback - UI will handle displaying a placeholder
549:             ""
550:         }
551:     }
552: 
553:     // Implement the interface method
554:     override suspend fun checkApiConnectivity(): Flow<Boolean> = flow {
555:         emit(pingApiInternal())
556:     }
557: 
558:     /**
559:      * Get recently viewed cocktails.
560:      */
561:     override suspend fun getRecentlyViewedCocktails(): Flow<List<Cocktail>> = flow {
562:         val recentlyViewedCocktails = cocktailCache.getRecentlyViewedCocktails()
563:         emit(recentlyViewedCocktails)
564:     }
565: 
566:     /**
567:      * Set the offline mode preference.
568:      */
569:     override fun setOfflineMode(enabled: Boolean) {
570:         forceOfflineMode = enabled
571:     }
572: 
573:     /**
574:      * Check if offline mode is enabled.
575:      */
576:     override fun isOfflineModeEnabled(): Boolean {
577:         return forceOfflineMode
578:     }
579: 
580:     /**
581:      * Get cocktails by category for recommendations.
582:      * This implementation uses the filterByCategory flow and collects the first result.
583:      */
584:     override suspend fun getCocktailsByCategory(category: String): List<Cocktail> {
585:         return try {
586:             // Check if we're offline
587:             if (isOffline()) {
588:                 // Use cached cocktails when offline
589:                 cocktailCache.getAllCachedCocktails()
590:                     .filter { it.category == category }
591:                     .take(5)
592:             } else {
593:                 filterByCategory(category).first()
594:             }
595:         } catch (e: Exception) {
596:             // Return empty list on error
597:             emptyList()
598:         }
599:     }
600: 
601:     /**
602:      * Get cocktails by ingredient for recommendations.
603:      * This implementation uses the filterByIngredient flow and collects the first result.
604:      */
605:     override suspend fun getCocktailsByIngredient(ingredient: String): List<Cocktail> {
606:         return try {
607:             // Check if we're offline
608:             if (isOffline()) {
609:                 // Use cached cocktails when offline
610:                 cocktailCache.getAllCachedCocktails()
611:                     .filter { cocktail ->
612:                         cocktail.ingredients.any {
613:                             it.name.contains(ingredient, ignoreCase = true)
614:                         }
615:                     }
616:                     .take(5)
617:             } else {
618:                 filterByIngredient(ingredient).first()
619:             }
620:         } catch (e: Exception) {
621:             // Return empty list on error
622:             emptyList()
623:         }
624:     }
625: 
626:     /**
627:      * Get cocktails by alcoholic filter for recommendations.
628:      */
629:     override suspend fun getCocktailsByAlcoholicFilter(alcoholicFilter: String): List<Cocktail> {
630:         return try {
631:             // Check if we're offline
632:             if (isOffline()) {
633:                 // Use cached cocktails when offline
634:                 cocktailCache.getAllCachedCocktails()
635:                     .filter { it.alcoholic == alcoholicFilter }
636:                     .take(5)
637:             } else {
638:                 // Use the existing filter method but convert boolean to string filter
639:                 val isAlcoholic = alcoholicFilter.equals("Alcoholic", ignoreCase = true)
640:                 filterByAlcoholic(isAlcoholic).first()
641:             }
642:         } catch (e: Exception) {
643:             // Return empty list on error
644:             emptyList()
645:         }
646:     }
647: 
648:     /**
649:      * Advanced search implementation that combines multiple filters
650:      */
651:     override suspend fun advancedSearch(filters: com.cocktailcraft.domain.model.SearchFilters): Flow<List<Cocktail>> = flow {
652:         try {
653:             // Check if we're offline
654:             if (isOffline()) {
655:                 // Use cached cocktails when offline
656:                 val cachedCocktails = cocktailCache.getAllCachedCocktails()
657:                 val filteredCocktails = applyFiltersToList(cachedCocktails, filters)
658:                 emit(filteredCocktails)
659:                 return@flow
660:             }
661: 
662:             // Start with a base list of cocktails
663:             val cocktails = when {
664:                 // If we have a text query, start with that
665:                 filters.query.isNotBlank() ->
666:                     searchCocktailsByName(filters.query).first()
667: 
668:                 // If we have a category, start with that
669:                 filters.category != null ->
670:                     filterByCategory(filters.category).first()
671: 
672:                 // If we have an ingredient, start with that
673:                 filters.ingredient != null ->
674:                     filterByIngredient(filters.ingredient).first()
675: 
676:                 // If we have alcoholic filter, start with that
677:                 filters.alcoholic != null ->
678:                     filterByAlcoholic(filters.alcoholic).first()
679: 
680:                 // If we have glass filter, start with that
681:                 filters.glass != null ->
682:                     filterByGlass(filters.glass).first()
683: 
684:                 // If no primary filter, get all cocktails
685:                 else ->
686:                     getCocktailsSortedByNewest().first()
687:             }
688: 
689:             // Apply all remaining filters to the result
690:             val filteredCocktails = applyFiltersToList(cocktails, filters)
691: 
692:             // Cache the results for offline access
693:             filteredCocktails.forEach { cocktail ->
694:                 cocktailCache.cacheCocktail(cocktail)
695:             }
696: 
697:             emit(filteredCocktails)
698:         } catch (e: Exception) {
699:             // Try to use cache as fallback
700:             val cachedCocktails = cocktailCache.getAllCachedCocktails()
701:             val filteredCocktails = applyFiltersToList(cachedCocktails, filters)
702: 
703:             if (filteredCocktails.isNotEmpty()) {
704:                 emit(filteredCocktails)
705:             } else {
706:                 emit(emptyList())
707:             }
708:         }
709:     }
710: 
711:     /**
712:      * Helper method to apply filters to a list of cocktails
713:      */
714:     private fun applyFiltersToList(cocktails: List<Cocktail>, filters: com.cocktailcraft.domain.model.SearchFilters): List<Cocktail> {
715:         var result = cocktails
716: 
717:         // Apply category filter if not used as primary filter
718:         if (filters.category != null && filters.query.isNotBlank()) {
719:             result = result.filter { it.category == filters.category }
720:         }
721: 
722:         // Apply ingredient filter if not used as primary filter
723:         if (filters.ingredient != null && (filters.query.isNotBlank() || filters.category != null)) {
724:             result = result.filter { cocktail ->
725:                 cocktail.ingredients.any { it.name.contains(filters.ingredient, ignoreCase = true) }
726:             }
727:         }
728: 
729:         // Apply alcoholic filter if not used as primary filter
730:         if (filters.alcoholic != null &&
731:             (filters.query.isNotBlank() || filters.category != null || filters.ingredient != null)) {
732:             val alcoholicString = if (filters.alcoholic) "Alcoholic" else "Non_Alcoholic"
733:             result = result.filter { it.alcoholic == alcoholicString }
734:         }
735: 
736:         // Apply glass filter if not used as primary filter
737:         if (filters.glass != null &&
738:             (filters.query.isNotBlank() || filters.category != null ||
739:              filters.ingredient != null || filters.alcoholic != null)) {
740:             result = result.filter { it.glass == filters.glass }
741:         }
742: 
743:         // Apply price range filter
744:         if (filters.priceRange != null) {
745:             result = result.filter {
746:                 it.price.toFloat() in filters.priceRange
747:             }
748:         }
749: 
750:         // Apply multiple ingredients filter
751:         if (filters.ingredients.isNotEmpty()) {
752:             result = result.filter { cocktail ->
753:                 filters.ingredients.all { ingredient ->
754:                     cocktail.ingredients.any { it.name.contains(ingredient, ignoreCase = true) }
755:                 }
756:             }
757:         }
758: 
759:         // Apply excluded ingredients filter
760:         if (filters.excludeIngredients.isNotEmpty()) {
761:             result = result.filter { cocktail ->
762:                 filters.excludeIngredients.none { ingredient ->
763:                     cocktail.ingredients.any { it.name.contains(ingredient, ignoreCase = true) }
764:                 }
765:             }
766:         }
767: 
768:         // Apply taste profile filter (this is more complex and would require ingredient analysis)
769:         // For now, we'll use a simplified approach based on common ingredients
770:         if (filters.tasteProfile != null) {
771:             result = result.filter { cocktail ->
772:                 when (filters.tasteProfile) {
773:                     com.cocktailcraft.domain.model.TasteProfile.SWEET ->
774:                         cocktail.ingredients.any {
775:                             it.name.contains("sugar", ignoreCase = true) ||
776:                             it.name.contains("syrup", ignoreCase = true) ||
777:                             it.name.contains("liqueur", ignoreCase = true)
778:                         }
779:                     com.cocktailcraft.domain.model.TasteProfile.SOUR ->
780:                         cocktail.ingredients.any {
781:                             it.name.contains("lemon", ignoreCase = true) ||
782:                             it.name.contains("lime", ignoreCase = true) ||
783:                             it.name.contains("citrus", ignoreCase = true)
784:                         }
785:                     com.cocktailcraft.domain.model.TasteProfile.BITTER ->
786:                         cocktail.ingredients.any {
787:                             it.name.contains("bitters", ignoreCase = true) ||
788:                             it.name.contains("campari", ignoreCase = true)
789:                         }
790:                     com.cocktailcraft.domain.model.TasteProfile.SPICY ->
791:                         cocktail.ingredients.any {
792:                             it.name.contains("pepper", ignoreCase = true) ||
793:                             it.name.contains("ginger", ignoreCase = true) ||
794:                             it.name.contains("cinnamon", ignoreCase = true)
795:                         }
796:                     com.cocktailcraft.domain.model.TasteProfile.FRUITY ->
797:                         cocktail.ingredients.any {
798:                             it.name.contains("fruit", ignoreCase = true) ||
799:                             it.name.contains("berry", ignoreCase = true) ||
800:                             it.name.contains("apple", ignoreCase = true) ||
801:                             it.name.contains("orange", ignoreCase = true) ||
802:                             it.name.contains("pineapple", ignoreCase = true)
803:                         }
804:                     com.cocktailcraft.domain.model.TasteProfile.HERBAL ->
805:                         cocktail.ingredients.any {
806:                             it.name.contains("herb", ignoreCase = true) ||
807:                             it.name.contains("mint", ignoreCase = true) ||
808:                             it.name.contains("basil", ignoreCase = true) ||
809:                             it.name.contains("rosemary", ignoreCase = true)
810:                         }
811:                     com.cocktailcraft.domain.model.TasteProfile.CREAMY ->
812:                         cocktail.ingredients.any {
813:                             it.name.contains("cream", ignoreCase = true) ||
814:                             it.name.contains("milk", ignoreCase = true) ||
815:                             it.name.contains("coconut", ignoreCase = true)
816:                         }
817:                     else -> true
818:                 }
819:             }
820:         }
821: 
822:         // Apply complexity filter (based on number of ingredients and preparation steps)
823:         if (filters.complexity != null) {
824:             result = result.filter { cocktail ->
825:                 val ingredientCount = cocktail.ingredients.size
826:                 val instructionLength = cocktail.instructions?.length ?: 0
827: 
828:                 when (filters.complexity) {
829:                     com.cocktailcraft.domain.model.Complexity.EASY ->
830:                         ingredientCount <= 3 && instructionLength < 100
831:                     com.cocktailcraft.domain.model.Complexity.MEDIUM ->
832:                         ingredientCount in 4..6 && instructionLength in 100..200
833:                     com.cocktailcraft.domain.model.Complexity.COMPLEX ->
834:                         ingredientCount > 6 || instructionLength > 200
835:                     else -> true
836:                 }
837:             }
838:         }
839: 
840:         // Apply preparation time filter (estimated based on complexity)
841:         if (filters.preparationTime != null) {
842:             result = result.filter { cocktail ->
843:                 val ingredientCount = cocktail.ingredients.size
844:                 val instructionLength = cocktail.instructions?.length ?: 0
845:                 val hasComplexTechniques = cocktail.instructions?.contains("muddle", ignoreCase = true) == true ||
846:                                           cocktail.instructions?.contains("shake", ignoreCase = true) == true ||
847:                                           cocktail.instructions?.contains("stir", ignoreCase = true) == true
848: 
849:                 when (filters.preparationTime) {
850:                     com.cocktailcraft.domain.model.PreparationTime.QUICK ->
851:                         ingredientCount <= 3 && !hasComplexTechniques
852:                     com.cocktailcraft.domain.model.PreparationTime.MEDIUM ->
853:                         ingredientCount in 4..6 || hasComplexTechniques
854:                     com.cocktailcraft.domain.model.PreparationTime.LONG ->
855:                         ingredientCount > 6 || instructionLength > 300
856:                     else -> true
857:                 }
858:             }
859:         }
860: 
861:         return result
862:     }
863: 
864:     // Helper method to check API connectivity
865:     private suspend fun pingApiInternal(): Boolean {
866:         return try {
867:             // Don't ping if we're in offline mode
868:             if (forceOfflineMode) {
869:                 return false
870:             }
871:             
872:             // Skip ping if we're in backoff period
873:             if (rateLimitBackoffMs > 0) {
874:                 val timeSinceLastCall = System.currentTimeMillis() - lastApiCallTime
875:                 if (timeSinceLastCall < rateLimitBackoffMs) {
876:                     CocktailDebugLogger.log("   ⏳ In rate limit backoff period, skipping ping")
877:                     return false
878:                 }
879:             }
880:             
881:             // Check if we've made recent API calls to avoid rate limiting
882:             val timeSinceLastCall = System.currentTimeMillis() - lastApiCallTime
883:             if (timeSinceLastCall < MIN_API_CALL_INTERVAL_MS) {
884:                 CocktailDebugLogger.log("   ⏳ Skipping ping to avoid rate limit (last call ${timeSinceLastCall}ms ago)")
885:                 return true // Assume API is reachable
886:             }
887:             
888:             lastApiCallTime = System.currentTimeMillis()
889:             val isConnected = api.pingApi()
890:             
891:             // Reset backoff on successful ping
892:             if (isConnected) {
893:                 rateLimitBackoffMs = 0
894:             }
895:             
896:             isConnected
897:         } catch (e: Exception) {
898:             // If it's a rate limit error, consider API as reachable but throttled
899:             if (e.message?.contains("429") == true || e.message?.contains("Too Many Requests") == true) {
900:                 CocktailDebugLogger.log("   ⚠️ API rate limited but reachable")
901:                 // Apply exponential backoff
902:                 rateLimitBackoffMs = minOf(
903:                     if (rateLimitBackoffMs == 0L) 2000L else rateLimitBackoffMs * 2,
904:                     MAX_BACKOFF_MS
905:                 )
906:                 return true
907:             }
908:             false
909:         }
910:     }
911:     
912:     companion object {
913:         // Global cache to persist across repository instances
914:         private var globalCocktailsCache: List<Cocktail> = emptyList()
915:         private var globalLastFetchTime: Long = 0
916:         private const val CACHE_VALIDITY_MS = 5 * 60 * 1000 // 5 minutes
917:         
918:         // Category-specific cache
919:         private val categoryCacheMap = mutableMapOf<String, List<Cocktail>>()
920:         private val categoryLastFetchMap = mutableMapOf<String, Long>()
921:         
922:         // Rate limit tracking
923:         private var lastApiCallTime: Long = 0
924:         private var rateLimitBackoffMs: Long = 0
925:         private const val MIN_API_CALL_INTERVAL_MS = 1000L // 1 second between calls
926:         private const val MAX_BACKOFF_MS = 30000L // Max 30 seconds backoff
927:     }
928: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/FavoritesRepositoryImpl.kt
````kotlin
 1: package com.cocktailcraft.data.repository
 2: 
 3: import com.cocktailcraft.domain.config.AppConfig
 4: import com.cocktailcraft.domain.model.Cocktail
 5: import com.cocktailcraft.domain.repository.CocktailRepository
 6: import com.cocktailcraft.domain.repository.FavoritesRepository
 7: import com.russhwolf.settings.Settings
 8: import kotlinx.coroutines.flow.Flow
 9: import kotlinx.coroutines.flow.flow
10: 
11: /**
12:  * Implementation of the FavoritesRepository interface.
13:  * This implementation delegates to the CocktailRepository for most operations.
14:  */
15: class FavoritesRepositoryImpl(
16:     private val cocktailRepository: CocktailRepository,
17:     private val settings: Settings,
18:     private val appConfig: AppConfig
19: ) : FavoritesRepository {
20: 
21:     override suspend fun getFavorites(): Flow<List<Cocktail>> {
22:         return cocktailRepository.getFavoriteCocktails()
23:     }
24: 
25:     override suspend fun addToFavorites(cocktail: Cocktail) {
26:         cocktailRepository.addToFavorites(cocktail)
27:     }
28: 
29:     override suspend fun removeFromFavorites(cocktail: Cocktail) {
30:         cocktailRepository.removeFromFavorites(cocktail)
31:     }
32: 
33:     override suspend fun isFavorite(id: String): Flow<Boolean> {
34:         return cocktailRepository.isCocktailFavorite(id)
35:     }
36: 
37:     override suspend fun toggleFavorite(cocktail: Cocktail) {
38:         // Check if the cocktail is already a favorite
39:         val isFavorite = settings.getStringOrNull(appConfig.favoritesStorageKey)
40:             ?.split(",")
41:             ?.filter { it.isNotEmpty() }
42:             ?.contains(cocktail.id)
43:             ?: false
44: 
45:         if (isFavorite) {
46:             removeFromFavorites(cocktail)
47:         } else {
48:             addToFavorites(cocktail)
49:         }
50:     }
51: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/OrderRepositoryImpl.kt
````kotlin
  1: package com.cocktailcraft.data.repository
  2: 
  3: import com.cocktailcraft.domain.config.AppConfig
  4: import com.cocktailcraft.domain.model.Order
  5: import com.cocktailcraft.domain.repository.OrderRepository
  6: import com.russhwolf.settings.Settings
  7: import kotlinx.coroutines.flow.Flow
  8: import kotlinx.coroutines.flow.MutableStateFlow
  9: import kotlinx.coroutines.flow.asStateFlow
 10: import kotlinx.coroutines.flow.flow
 11: import kotlinx.serialization.decodeFromString
 12: import kotlinx.serialization.encodeToString
 13: import kotlinx.serialization.json.Json
 14: import java.util.UUID
 15: 
 16: class OrderRepositoryImpl(
 17:     private val settings: Settings,
 18:     private val json: Json,
 19:     private val appConfig: AppConfig
 20: ) : OrderRepository {
 21: 
 22:     // In-memory cache of orders
 23:     private val _orders = MutableStateFlow<List<Order>>(emptyList())
 24:     
 25:     init {
 26:         // Load orders from persistent storage when repository is created
 27:         loadOrdersFromStorage()
 28:     }
 29:     
 30:     private fun loadOrdersFromStorage() {
 31:         val ordersJson = settings.getStringOrNull(appConfig.ordersStorageKey) ?: "[]"
 32:         try {
 33:             _orders.value = json.decodeFromString(ordersJson)
 34:         } catch (e: Exception) {
 35:             // If there's an error parsing, start with empty list
 36:             _orders.value = emptyList()
 37:         }
 38:     }
 39:     
 40:     private fun saveOrdersToStorage() {
 41:         val ordersJson = json.encodeToString(_orders.value)
 42:         settings.putString(appConfig.ordersStorageKey, ordersJson)
 43:     }
 44: 
 45:     override suspend fun getOrders(): Flow<List<Order>> = _orders.asStateFlow()
 46: 
 47:     override suspend fun addOrder(order: Order) {
 48:         val updatedOrders = _orders.value.toMutableList().apply {
 49:             add(order)
 50:         }
 51:         _orders.value = updatedOrders
 52:         saveOrdersToStorage()
 53:     }
 54: 
 55:     override suspend fun getOrderById(id: String): Flow<Order?> = flow {
 56:         emit(_orders.value.find { it.id == id })
 57:     }
 58: 
 59:     override suspend fun updateOrderStatus(id: String, status: String) {
 60:         val updatedOrders = _orders.value.map { order ->
 61:             if (order.id == id) order.copy(status = status) else order
 62:         }
 63:         _orders.value = updatedOrders
 64:         saveOrdersToStorage()
 65:     }
 66: 
 67:     override suspend fun deleteOrder(id: String) {
 68:         val updatedOrders = _orders.value.filter { it.id != id }
 69:         _orders.value = updatedOrders
 70:         saveOrdersToStorage()
 71:     }
 72: 
 73:     override suspend fun placeOrder(order: Order): Flow<Boolean> = flow {
 74:         try {
 75:             val orders = _orders.value.toMutableList()
 76:             // Generate a new ID if not provided
 77:             val orderWithId = if (order.id.isBlank()) {
 78:                 order.copy(id = UUID.randomUUID().toString())
 79:             } else {
 80:                 order
 81:             }
 82:             orders.add(orderWithId)
 83:             _orders.value = orders
 84:             saveOrdersToStorage()
 85:             emit(true)
 86:         } catch (e: Exception) {
 87:             emit(false)
 88:         }
 89:     }
 90: 
 91:     override suspend fun getOrderHistory(): Flow<List<Order>> = flow {
 92:         emit(_orders.value)
 93:     }
 94: 
 95:     override suspend fun cancelOrder(orderId: String): Flow<Boolean> = flow {
 96:         try {
 97:             val orders = _orders.value.toMutableList()
 98:             val orderIndex = orders.indexOfFirst { it.id == orderId }
 99:             
100:             if (orderIndex != -1) {
101:                 val order = orders[orderIndex]
102:                 // Only allow cancellation if the order is still pending or processing
103:                 if (order.status == "Processing" || order.status == "Pending") {
104:                     orders[orderIndex] = order.copy(status = "Cancelled")
105:                     _orders.value = orders
106:                     saveOrdersToStorage()
107:                     emit(true)
108:                 } else {
109:                     emit(false)
110:                 }
111:             } else {
112:                 emit(false)
113:             }
114:         } catch (e: Exception) {
115:             emit(false)
116:         }
117:     }
118: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/di/AppModule.kt
````kotlin
 1: package com.cocktailcraft.di
 2: 
 3: import org.koin.core.module.Module
 4: import org.koin.dsl.module
 5: 
 6: /**
 7:  * Main application module that combines all other modules.
 8:  * This modular approach improves maintainability and testability.
 9:  */
10: val appModule = listOf(
11:     networkModule,
12:     dataModule,
13:     domainModule
14: )
15: // The platformModule function is declared in PlatformModule.kt
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/di/DataModule.kt
````kotlin
 1: package com.cocktailcraft.di
 2: 
 3: import com.cocktailcraft.data.cache.CocktailCache
 4: import com.cocktailcraft.data.repository.AuthRepositoryImpl
 5: import com.cocktailcraft.data.repository.CartRepositoryImpl
 6: import com.cocktailcraft.data.repository.CocktailRepositoryImpl
 7: import com.cocktailcraft.data.repository.FavoritesRepositoryImpl
 8: import com.cocktailcraft.data.repository.OrderRepositoryImpl
 9: import com.cocktailcraft.domain.repository.AuthRepository
10: import com.cocktailcraft.domain.repository.CartRepository
11: import com.cocktailcraft.domain.repository.CocktailRepository
12: import com.cocktailcraft.domain.repository.FavoritesRepository
13: import com.cocktailcraft.domain.repository.OrderRepository
14: import kotlinx.serialization.json.Json
15: import org.koin.dsl.module
16: 
17: /**
18:  * Koin module for data-related dependencies including repositories and caching.
19:  */
20: val dataModule = module {
21:     // JSON
22:     single {
23:         Json {
24:             ignoreUnknownKeys = true
25:             isLenient = true
26:         }
27:     }
28: 
29:     // Cache
30:     single { CocktailCache(
31:         settings = get(),
32:         json = get(),
33:         appConfig = get()
34:     ) }
35: 
36:     // Repositories
37:     single<CocktailRepository> {
38:         CocktailRepositoryImpl(
39:             api = get(),
40:             settings = get(),
41:             appConfig = get(),
42:             networkMonitor = get(),
43:             cocktailCache = get()
44:         )
45:     }
46: 
47:     single<CartRepository> {
48:         CartRepositoryImpl(get(), get())
49:     }
50: 
51:     single<AuthRepository> {
52:         AuthRepositoryImpl(get(), get())
53:     }
54: 
55:     single<OrderRepository> {
56:         OrderRepositoryImpl(
57:             settings = get(),
58:             json = get(),
59:             appConfig = get()
60:         )
61:     }
62: 
63:     single<FavoritesRepository> {
64:         FavoritesRepositoryImpl(
65:             cocktailRepository = get(),
66:             settings = get(),
67:             appConfig = get()
68:         )
69:     }
70: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/di/DomainModule.kt
````kotlin
 1: package com.cocktailcraft.di
 2: 
 3: import com.cocktailcraft.data.config.AppConfigImpl
 4: import com.cocktailcraft.domain.config.AppConfig
 5: import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
 6: import com.cocktailcraft.domain.usecase.ToggleFavoriteUseCase
 7: import org.koin.dsl.module
 8: 
 9: /**
10:  * Koin module for domain-related dependencies including use cases and domain configurations.
11:  */
12: val domainModule = module {
13:     // Config
14:     single<AppConfig> { AppConfigImpl() }
15: 
16:     // Use Cases
17:     factory { PlaceOrderUseCase(orderRepository = get()) }
18:     factory { ToggleFavoriteUseCase(cocktailRepository = get()) }
19: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/di/NetworkModule.kt
````kotlin
 1: package com.cocktailcraft.di
 2: 
 3: import com.cocktailcraft.data.remote.CocktailApi
 4: import com.cocktailcraft.data.remote.CocktailApiImpl
 5: import com.cocktailcraft.domain.config.AppConfig
 6: import com.cocktailcraft.util.NetworkMonitor
 7: import io.ktor.client.*
 8: import io.ktor.client.plugins.contentnegotiation.*
 9: import io.ktor.client.plugins.logging.*
10: import io.ktor.client.plugins.HttpRequestRetry
11: import io.ktor.client.plugins.HttpResponseValidator
12: import io.ktor.client.plugins.ServerResponseException
13: import io.ktor.client.request.header
14: import io.ktor.client.network.sockets.ConnectTimeoutException
15: import io.ktor.http.ContentType
16: import io.ktor.http.HttpHeaders
17: import io.ktor.serialization.kotlinx.json.*
18: import kotlinx.serialization.json.Json
19: import org.koin.dsl.module
20: 
21: /**
22:  * Koin module for network-related dependencies.
23:  */
24: val networkModule = module {
25:     // HTTP Client
26:     single {
27:         HttpClient {
28:             install(ContentNegotiation) {
29:                 json(get<Json>())
30:             }
31:             install(Logging) {
32:                 level = LogLevel.ALL
33:             }
34: 
35:             install(io.ktor.client.plugins.HttpTimeout) {
36:                 val config = get<AppConfig>()
37:                 connectTimeoutMillis = config.networkTimeoutMs
38:                 requestTimeoutMillis = config.networkTimeoutMs
39:                 socketTimeoutMillis = config.networkTimeoutMs
40:             }
41: 
42:             install(io.ktor.client.plugins.DefaultRequest) {
43:                 // Add default headers for consistency
44:                 header(HttpHeaders.ContentType, ContentType.Application.Json)
45:                 header(HttpHeaders.Accept, ContentType.Application.Json)
46:             }
47: 
48:             // Add HttpRequestRetry for automatic retries
49:             install(io.ktor.client.plugins.HttpRequestRetry) {
50:                 retryOnExceptionOrServerErrors(maxRetries = 3)
51:                 exponentialDelay()
52:                 modifyRequest { request ->
53:                     // Log retry attempts - access retryCount from the context
54:                     println("Retrying request to ${request.url} (attempt #${retryCount})")
55:                 }
56:             }
57: 
58:             // Add error handling
59:             HttpResponseValidator {
60:                 validateResponse { response ->
61:                     val statusCode = response.status.value
62:                     if (statusCode >= 400) {
63:                         when (statusCode) {
64:                             in 400..499 -> throw ConnectTimeoutException(
65:                                 "Client error: ${response.status.description}", Throwable("Client error")
66:                             )
67:                             in 500..599 -> throw ServerResponseException(
68:                                 response, "Server error: ${response.status.description}"
69:                             )
70:                         }
71:                     }
72:                 }
73: 
74:                 handleResponseExceptionWithRequest { exception, _ ->
75:                     println("Network error: ${exception.message}")
76:                 }
77:             }
78: 
79:             engine {
80:                 // Engine-specific config
81:             }
82:         }
83:     }
84: 
85:     // API
86:     single<CocktailApi> { CocktailApiImpl(get()) }
87: 
88:     // Network monitoring
89:     single { NetworkMonitor(get()) }
90: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/di/PlatformModule.kt
````kotlin
1: package com.cocktailcraft.di
2: 
3: import org.koin.core.module.Module
4: 
5: expect fun platformModule(): Module
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/config/AppConfig.kt
````kotlin
 1: package com.cocktailcraft.domain.config
 2: 
 3: /**
 4:  * Application configuration interface that provides access to various configuration values.
 5:  * This follows the hexagonal architecture by defining a port in the domain layer
 6:  * that will be implemented by adapters in the infrastructure layer.
 7:  */
 8: interface AppConfig {
 9:     /**
10:      * Base URL for the API
11:      */
12:     val apiBaseUrl: String
13: 
14:     /**
15:      * Base URL for images
16:      */
17:     val imageBaseUrl: String
18: 
19:     /**
20:      * Path for ingredient images
21:      */
22:     val ingredientsImagePath: String
23: 
24:     /**
25:      * Path for cocktail images
26:      */
27:     val cocktailsImagePath: String
28: 
29:     /**
30:      * Default timeout for network requests in milliseconds
31:      */
32:     val networkTimeoutMs: Long
33: 
34:     /**
35:      * Initial timeout for first network request attempt
36:      */
37:     val initialNetworkTimeoutMs: Long
38: 
39:     /**
40:      * Maximum timeout for network requests after retries
41:      */
42:     val maxNetworkTimeoutMs: Long
43: 
44:     /**
45:      * Maximum number of retries for network operations
46:      */
47:     val maxRetries: Int
48: 
49:     /**
50:      * Key for storing favorites in local storage
51:      */
52:     val favoritesStorageKey: String
53: 
54:     /**
55:      * Key for storing orders in local storage
56:      */
57:     val ordersStorageKey: String
58: 
59:     /**
60:      * Expiration time for cached data in milliseconds
61:      */
62:     val cacheExpirationMs: Long
63: 
64:     /**
65:      * Key for storing recently viewed cocktails in local storage
66:      */
67:     val recentlyViewedStorageKey: String
68: 
69:     /**
70:      * Maximum number of cocktails to cache for offline mode
71:      */
72:     val maxOfflineCocktails: Int
73: 
74:     /**
75:      * Key for storing offline mode status in local storage
76:      */
77:     val offlineModeEnabledKey: String
78: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/Cocktail.kt
````kotlin
 1: package com.cocktailcraft.domain.model
 2: 
 3: import kotlinx.serialization.Serializable
 4: 
 5: @Serializable
 6: data class Cocktail(
 7:     val id: String,
 8:     val name: String,
 9:     val alternateName: String? = null,
10:     val tags: List<String>? = null,
11:     val category: String? = null,
12:     val iba: String? = null,
13:     val alcoholic: String? = null,
14:     val glass: String? = null,
15:     val instructions: String? = null,
16:     val imageUrl: String? = null,
17:     val ingredients: List<CocktailIngredient> = emptyList(),
18:     val imageSource: String? = null,
19:     val imageAttribution: String? = null,
20:     val creativeCommonsConfirmed: Boolean? = null,
21:     val dateModified: String? = null,
22:     val price: Double = 10.0,
23:     val inStock: Boolean = true,
24:     val stockCount: Int = 50,
25:     val rating: Float = 4.5f,
26:     val popularity: Int = 0,
27:     val dateAdded: Long = System.currentTimeMillis()
28: )
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/CocktailCartItem.kt
````kotlin
1: package com.cocktailcraft.domain.model
2: 
3: import kotlinx.serialization.Serializable
4: 
5: @Serializable
6: data class CocktailCartItem(
7:     val cocktail: Cocktail,
8:     val quantity: Int
9: )
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/CocktailIngredient.kt
````kotlin
1: package com.cocktailcraft.domain.model
2: 
3: import kotlinx.serialization.Serializable
4: 
5: @Serializable
6: data class CocktailIngredient(
7:     val name: String,
8:     val measure: String
9: )
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/Order.kt
````kotlin
 1: package com.cocktailcraft.domain.model
 2: 
 3: import kotlinx.serialization.Serializable
 4: 
 5: @Serializable
 6: data class Order(
 7:     val id: String,
 8:     val date: String,
 9:     val items: List<OrderItem>,
10:     val total: Double,
11:     val status: String
12: )
13: 
14: @Serializable
15: data class OrderItem(
16:     val name: String,
17:     val quantity: Int,
18:     val price: Double
19: )
20: 
21: enum class OrderStatus {
22:     PENDING,
23:     PROCESSING,
24:     SHIPPED,
25:     DELIVERED,
26:     CANCELLED
27: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/Review.kt
````kotlin
 1: package com.cocktailcraft.domain.model
 2: 
 3: import kotlinx.serialization.Serializable
 4: 
 5: @Serializable
 6: data class Review(
 7:     val id: String = System.currentTimeMillis().toString(),
 8:     val cocktailId: String,
 9:     val userName: String,
10:     val rating: Float,
11:     val comment: String,
12:     val date: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
13:         .format(java.util.Date())
14: )
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/SearchFilters.kt
````kotlin
  1: package com.cocktailcraft.domain.model
  2: 
  3: /**
  4:  * Model representing search filters for cocktails
  5:  */
  6: data class SearchFilters(
  7:     val query: String = "",
  8:     val category: String? = null,
  9:     val ingredient: String? = null,
 10:     val alcoholic: Boolean? = null,
 11:     val glass: String? = null,
 12:     val priceRange: ClosedFloatingPointRange<Float>? = null,
 13:     
 14:     // Additional filters for advanced search
 15:     val ingredients: List<String> = emptyList(), // Multiple ingredients
 16:     val excludeIngredients: List<String> = emptyList(), // Ingredients to exclude
 17:     val tasteProfile: TasteProfile? = null, // Taste profile preferences
 18:     val complexity: Complexity? = null, // Complexity level
 19:     val preparationTime: PreparationTime? = null // Preparation time
 20: ) {
 21:     // Helper function to check if any filters are active
 22:     fun hasActiveFilters(): Boolean {
 23:         return category != null || 
 24:                ingredient != null || 
 25:                alcoholic != null || 
 26:                glass != null || 
 27:                priceRange != null ||
 28:                ingredients.isNotEmpty() ||
 29:                excludeIngredients.isNotEmpty() ||
 30:                tasteProfile != null ||
 31:                complexity != null ||
 32:                preparationTime != null
 33:     }
 34:     
 35:     // Helper function to check if basic search is active
 36:     fun hasBasicSearch(): Boolean {
 37:         return query.isNotBlank()
 38:     }
 39:     
 40:     // Helper function to get a description of active filters
 41:     fun getActiveFiltersDescription(): String {
 42:         val activeFilters = mutableListOf<String>()
 43:         
 44:         if (category != null) activeFilters.add("Category: $category")
 45:         if (ingredient != null) activeFilters.add("Ingredient: $ingredient")
 46:         if (alcoholic != null) activeFilters.add(if (alcoholic) "Alcoholic" else "Non-Alcoholic")
 47:         if (glass != null) activeFilters.add("Glass: $glass")
 48:         if (priceRange != null) activeFilters.add("Price: $${priceRange.start} - $${priceRange.endInclusive}")
 49:         if (ingredients.isNotEmpty()) activeFilters.add("Ingredients: ${ingredients.joinToString(", ")}")
 50:         if (excludeIngredients.isNotEmpty()) activeFilters.add("Exclude: ${excludeIngredients.joinToString(", ")}")
 51:         if (tasteProfile != null) activeFilters.add("Taste: $tasteProfile")
 52:         if (complexity != null) activeFilters.add("Complexity: $complexity")
 53:         if (preparationTime != null) activeFilters.add("Prep Time: $preparationTime")
 54:         
 55:         return activeFilters.joinToString(" • ")
 56:     }
 57:     
 58:     // Helper function to clear all filters
 59:     fun clearAllFilters(): SearchFilters {
 60:         return copy(
 61:             category = null,
 62:             ingredient = null,
 63:             alcoholic = null,
 64:             glass = null,
 65:             priceRange = null,
 66:             ingredients = emptyList(),
 67:             excludeIngredients = emptyList(),
 68:             tasteProfile = null,
 69:             complexity = null,
 70:             preparationTime = null
 71:         )
 72:     }
 73: }
 74: 
 75: /**
 76:  * Enum representing taste profile preferences
 77:  */
 78: enum class TasteProfile {
 79:     SWEET,
 80:     SOUR,
 81:     BITTER,
 82:     SPICY,
 83:     FRUITY,
 84:     HERBAL,
 85:     CREAMY;
 86:     
 87:     override fun toString(): String {
 88:         return name.lowercase().replaceFirstChar { it.uppercase() }
 89:     }
 90: }
 91: 
 92: /**
 93:  * Enum representing complexity level
 94:  */
 95: enum class Complexity {
 96:     EASY,
 97:     MEDIUM,
 98:     COMPLEX;
 99:     
100:     override fun toString(): String {
101:         return name.lowercase().replaceFirstChar { it.uppercase() }
102:     }
103: }
104: 
105: /**
106:  * Enum representing preparation time
107:  */
108: enum class PreparationTime {
109:     QUICK,
110:     MEDIUM,
111:     LONG;
112:     
113:     override fun toString(): String {
114:         return when(this) {
115:             QUICK -> "< 5 min"
116:             MEDIUM -> "5-10 min"
117:             LONG -> "> 10 min"
118:         }
119:     }
120: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/User.kt
````kotlin
 1: package com.cocktailcraft.domain.model
 2: 
 3: import kotlinx.serialization.Serializable
 4: 
 5: @Serializable
 6: data class User(
 7:     val id: String,
 8:     val name: String = "",
 9:     val email: String,
10:     val photoUrl: String? = null,
11:     val phoneNumber: String? = null,
12:     val isEmailVerified: Boolean = false,
13:     val joinDate: String? = null,
14:     val address: Address? = null,
15:     val preferences: Map<String, String> = emptyMap()
16: )
17: 
18: @Serializable
19: data class Address(
20:     val street: String = "",
21:     val city: String = "",
22:     val state: String = "",
23:     val zipCode: String = "",
24:     val country: String = ""
25: )
26: 
27: @Serializable
28: data class UserPreferences(
29:     val darkMode: Boolean = false,
30:     val followSystemTheme: Boolean = true,
31:     val notificationsEnabled: Boolean = true,
32:     val language: String = "en"
33: )
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/repository/AuthRepository.kt
````kotlin
 1: package com.cocktailcraft.domain.repository
 2: 
 3: import com.cocktailcraft.domain.model.User
 4: import com.cocktailcraft.domain.model.UserPreferences
 5: import com.cocktailcraft.domain.model.Address
 6: import kotlinx.coroutines.flow.Flow
 7: 
 8: interface AuthRepository {
 9:     // Authentication methods
10:     suspend fun signUp(email: String, password: String): Flow<Boolean>
11:     suspend fun signIn(email: String, password: String): Flow<Boolean>
12:     suspend fun signOut(): Flow<Boolean>
13:     suspend fun resetPassword(email: String): Flow<Boolean>
14:     suspend fun changePassword(oldPassword: String, newPassword: String): Flow<Boolean>
15:     suspend fun isUserSignedIn(): Flow<Boolean>
16:     suspend fun getCurrentUser(): Flow<User?>
17:     
18:     // Profile management methods
19:     suspend fun updateUserName(name: String): Flow<Boolean>
20:     suspend fun updateUserEmail(email: String, password: String): Flow<Boolean>
21:     suspend fun updateUserAddress(address: Address): Flow<Boolean>
22:     
23:     // Preferences management
24:     suspend fun updateUserPreferences(preferences: UserPreferences): Flow<Boolean>
25:     suspend fun getUserPreferences(): Flow<UserPreferences>
26: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/repository/CartRepository.kt
````kotlin
 1: package com.cocktailcraft.domain.repository
 2: 
 3: import com.cocktailcraft.domain.model.CocktailCartItem
 4: import kotlinx.coroutines.flow.Flow
 5: 
 6: interface CartRepository {
 7:     suspend fun getCartItems(): Flow<List<CocktailCartItem>>
 8:     suspend fun addToCart(cartItem: CocktailCartItem)
 9:     suspend fun removeFromCart(cocktailId: String)
10:     suspend fun updateQuantity(cocktailId: String, quantity: Int)
11:     suspend fun clearCart()
12:     suspend fun getCartTotal(): Flow<Double>
13: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/repository/CocktailRepository.kt
````kotlin
 1: package com.cocktailcraft.domain.repository
 2: 
 3: import com.cocktailcraft.domain.model.Cocktail
 4: import kotlinx.coroutines.flow.Flow
 5: 
 6: interface CocktailRepository {
 7:     suspend fun searchCocktailsByName(name: String): Flow<List<Cocktail>>
 8:     suspend fun searchCocktailsByFirstLetter(letter: Char): Flow<List<Cocktail>>
 9:     suspend fun getCocktailById(id: String): Flow<Cocktail?>
10:     suspend fun getRandomCocktail(): Flow<Cocktail?>
11:     suspend fun filterByIngredient(ingredient: String): Flow<List<Cocktail>>
12:     suspend fun filterByAlcoholic(alcoholic: Boolean): Flow<List<Cocktail>>
13:     suspend fun filterByCategory(category: String): Flow<List<Cocktail>>
14:     suspend fun filterByGlass(glass: String): Flow<List<Cocktail>>
15:     suspend fun getCategories(): Flow<List<String>>
16:     suspend fun getGlasses(): Flow<List<String>>
17:     suspend fun getIngredients(): Flow<List<String>>
18:     suspend fun getAlcoholicFilters(): Flow<List<String>>
19:     suspend fun getFavoriteCocktails(): Flow<List<Cocktail>>
20:     suspend fun addToFavorites(cocktail: Cocktail)
21:     suspend fun removeFromFavorites(cocktail: Cocktail)
22:     suspend fun isCocktailFavorite(id: String): Flow<Boolean>
23:     suspend fun getCocktailsSortedByNewest(): Flow<List<Cocktail>>
24:     suspend fun getCocktailsSortedByPriceLowToHigh(): Flow<List<Cocktail>>
25:     suspend fun getCocktailsSortedByPriceHighToLow(): Flow<List<Cocktail>>
26:     suspend fun getCocktailsSortedByPopularity(): Flow<List<Cocktail>>
27:     suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Cocktail>>
28: 
29:     // Advanced search and filtering
30:     suspend fun advancedSearch(filters: com.cocktailcraft.domain.model.SearchFilters): Flow<List<Cocktail>>
31: 
32:     // New method for getting image URL
33:     fun getCocktailImageUrl(cocktail: Cocktail): String
34: 
35:     // New method to check API connectivity
36:     suspend fun checkApiConnectivity(): Flow<Boolean>
37: 
38:     // Methods for offline mode
39:     suspend fun getRecentlyViewedCocktails(): Flow<List<Cocktail>>
40:     fun setOfflineMode(enabled: Boolean)
41:     fun isOfflineModeEnabled(): Boolean
42: 
43:     // Methods for recommendations
44:     suspend fun getCocktailsByCategory(category: String): List<Cocktail>
45:     suspend fun getCocktailsByIngredient(ingredient: String): List<Cocktail>
46:     suspend fun getCocktailsByAlcoholicFilter(alcoholicFilter: String): List<Cocktail>
47: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/repository/FavoritesRepository.kt
````kotlin
 1: package com.cocktailcraft.domain.repository
 2: 
 3: import com.cocktailcraft.domain.model.Cocktail
 4: import kotlinx.coroutines.flow.Flow
 5: 
 6: /**
 7:  * Repository interface for managing favorite cocktails.
 8:  */
 9: interface FavoritesRepository {
10:     /**
11:      * Get all favorite cocktails.
12:      */
13:     suspend fun getFavorites(): Flow<List<Cocktail>>
14:     
15:     /**
16:      * Add a cocktail to favorites.
17:      */
18:     suspend fun addToFavorites(cocktail: Cocktail)
19:     
20:     /**
21:      * Remove a cocktail from favorites.
22:      */
23:     suspend fun removeFromFavorites(cocktail: Cocktail)
24:     
25:     /**
26:      * Check if a cocktail is in favorites.
27:      */
28:     suspend fun isFavorite(id: String): Flow<Boolean>
29:     
30:     /**
31:      * Toggle favorite status of a cocktail.
32:      */
33:     suspend fun toggleFavorite(cocktail: Cocktail)
34: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/repository/OrderRepository.kt
````kotlin
 1: package com.cocktailcraft.domain.repository
 2: 
 3: import com.cocktailcraft.domain.model.Order
 4: import kotlinx.coroutines.flow.Flow
 5: 
 6: interface OrderRepository {
 7:     suspend fun getOrders(): Flow<List<Order>>
 8:     suspend fun addOrder(order: Order)
 9:     suspend fun getOrderById(id: String): Flow<Order?>
10:     suspend fun updateOrderStatus(id: String, status: String)
11:     suspend fun deleteOrder(id: String)
12:     
13:     // Methods needed by the implementation
14:     suspend fun placeOrder(order: Order): Flow<Boolean>
15:     suspend fun getOrderHistory(): Flow<List<Order>>
16:     suspend fun cancelOrder(orderId: String): Flow<Boolean>
17: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/usecase/PlaceOrderUseCase.kt
````kotlin
 1: package com.cocktailcraft.domain.usecase
 2: 
 3: import com.cocktailcraft.domain.model.Cocktail
 4: import com.cocktailcraft.domain.model.CocktailCartItem
 5: import com.cocktailcraft.domain.model.Order
 6: import com.cocktailcraft.domain.model.OrderItem
 7: import com.cocktailcraft.domain.repository.OrderRepository
 8: import com.cocktailcraft.domain.util.Result
 9: import kotlinx.coroutines.flow.Flow
10: import kotlinx.coroutines.flow.flow
11: import java.text.SimpleDateFormat
12: import java.util.Date
13: import java.util.Locale
14: 
15: class PlaceOrderUseCase(
16:     private val orderRepository: OrderRepository
17: ) {
18:     suspend operator fun invoke(cartItems: List<CocktailCartItem>, totalPrice: Double): Flow<Result<Order>> = flow {
19:         try {
20:             // Generate order ID and date
21:             val orderId = "ORD-${System.currentTimeMillis()}"
22:             val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
23:             
24:             // Map cart items to order items
25:             val orderItems = cartItems.map { cartItem ->
26:                 OrderItem(
27:                     name = cartItem.cocktail.name,
28:                     quantity = cartItem.quantity,
29:                     price = cartItem.cocktail.price
30:                 )
31:             }
32:             
33:             // Create order object
34:             val order = Order(
35:                 id = orderId,
36:                 date = currentDate,
37:                 items = orderItems,
38:                 total = totalPrice,
39:                 status = "Processing"
40:             )
41:             
42:             // Add order to repository
43:             orderRepository.addOrder(order)
44:             
45:             // Emit success result with created order
46:             emit(Result.Success(order))
47:         } catch (e: Exception) {
48:             // Emit error result
49:             emit(Result.Error(e.message ?: "Unknown error occurred"))
50:         }
51:     }
52: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/usecase/ToggleFavoriteUseCase.kt
````kotlin
 1: package com.cocktailcraft.domain.usecase
 2: 
 3: import com.cocktailcraft.domain.model.Cocktail
 4: import com.cocktailcraft.domain.repository.CocktailRepository
 5: import com.cocktailcraft.domain.util.Result
 6: import kotlinx.coroutines.flow.Flow
 7: import kotlinx.coroutines.flow.flow
 8: import kotlinx.coroutines.flow.first
 9: 
10: class ToggleFavoriteUseCase(
11:     private val cocktailRepository: CocktailRepository
12: ) {
13:     suspend operator fun invoke(cocktail: Cocktail): Flow<Result<Boolean>> = flow {
14:         try {
15:             // Check if cocktail is already a favorite
16:             val isAlreadyFavorite = cocktailRepository.isCocktailFavorite(cocktail.id).first()
17:             
18:             // Toggle favorite status
19:             if (isAlreadyFavorite) {
20:                 cocktailRepository.removeFromFavorites(cocktail)
21:                 emit(Result.Success(false)) // Now not a favorite
22:             } else {
23:                 cocktailRepository.addToFavorites(cocktail)
24:                 emit(Result.Success(true)) // Now a favorite
25:             }
26:         } catch (e: Exception) {
27:             emit(Result.Error(e.message ?: "Unknown error occurred"))
28:         }
29:     }
30: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/util/ErrorCode.kt
````kotlin
 1: package com.cocktailcraft.domain.util
 2: 
 3: /**
 4:  * Standardized error codes for the application.
 5:  * These codes can be used to categorize errors and provide consistent error handling.
 6:  */
 7: enum class ErrorCode {
 8:     // Network related errors
 9:     NETWORK,
10:     NETWORK_ERROR,  // Legacy name, same as NETWORK
11:     TIMEOUT,
12: 
13:     // Authentication errors
14:     UNAUTHORIZED,
15:     FORBIDDEN,
16: 
17:     // Data errors
18:     INVALID_DATA,
19:     VALIDATION_ERROR,  // Legacy name, same as INVALID_DATA
20:     NOT_FOUND,
21: 
22:     // Server errors
23:     SERVER_ERROR,
24: 
25:     // Client errors
26:     CLIENT_ERROR,
27: 
28:     // Unknown errors
29:     UNKNOWN
30: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/domain/util/Result.kt
````kotlin
 1: package com.cocktailcraft.domain.util
 2: 
 3: sealed class Result<out T> {
 4:     data class Success<T>(val data: T) : Result<T>()
 5:     data class Error(val message: String, val code: ErrorCode = ErrorCode.UNKNOWN) : Result<Nothing>()
 6:     object Loading : Result<Nothing>()
 7: 
 8:     // Helper functions to safely handle results
 9:     fun isSuccess(): Boolean = this is Success
10:     fun isError(): Boolean = this is Error
11:     fun isLoading(): Boolean = this is Loading
12: 
13:     // Safely get data or null
14:     fun getOrNull(): T? = when (this) {
15:         is Success -> data
16:         else -> null
17:     }
18: 
19:     // Convert result to another type
20:     fun <R> map(transform: (T) -> R): Result<R> = when (this) {
21:         is Success -> Success(transform(data))
22:         is Error -> this
23:         is Loading -> this
24:     }
25: }
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/util/CocktailDebugLogger.kt
````kotlin
 1: package com.cocktailcraft.util
 2: 
 3: /**
 4:  * Centralized debug logger for cocktail loading issues
 5:  */
 6: object CocktailDebugLogger {
 7:     const val TAG = "CocktailDebug"
 8:     
 9:     // Use expect/actual for platform-specific logging
10:     fun log(message: String) {
11:         logInternal(message)
12:     }
13: }
14: 
15: // Platform-specific implementation
16: expect fun logInternal(message: String)
````

## File: shared/src/commonMain/kotlin/com/cocktailcraft/util/NetworkMonitor.kt
````kotlin
 1: package com.cocktailcraft.util
 2: 
 3: import android.content.Context
 4: import kotlinx.coroutines.flow.MutableStateFlow
 5: import kotlinx.coroutines.flow.StateFlow
 6: import kotlinx.coroutines.flow.asStateFlow
 7: 
 8: /**
 9:  * Common interface for monitoring network connectivity.
10:  * Currently only implemented for Android.
11:  */
12: expect class NetworkMonitor(context: Context) {
13:     fun startMonitoring()
14:     fun stopMonitoring()
15:     val isOnline: StateFlow<Boolean>
16: }
17: 
18: /**
19:  * Base implementation with common functionality.
20:  */
21: abstract class BaseNetworkMonitor {
22:     protected val _isOnline = MutableStateFlow(true)
23:     open val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
24: 
25:     abstract fun startMonitoring()
26:     abstract fun stopMonitoring()
27: }
````

## File: shared/src/iosMain/kotlin/com/cocktailcraft/di/PlatformModule.kt
````kotlin
 1: package com.cocktailcraft.di
 2: 
 3: import com.russhwolf.settings.NSUserDefaultsSettings
 4: import com.russhwolf.settings.Settings
 5: import org.koin.dsl.module
 6: import platform.Foundation.NSUserDefaults
 7: 
 8: actual fun platformModule() = module {
 9:     single<Settings> {
10:         NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
11:     }
12: }
````

## File: shared/build.gradle.kts
````
 1: plugins {
 2:     kotlin("multiplatform")
 3:     kotlin("plugin.serialization")
 4:     id("com.android.library")
 5: }
 6: 
 7: android {
 8:     namespace = "com.cocktailcraft"
 9:     compileSdk = 34
10:     defaultConfig {
11:         minSdk = 24
12:     }
13:     
14:     compileOptions {
15:         sourceCompatibility = JavaVersion.VERSION_17
16:         targetCompatibility = JavaVersion.VERSION_17
17:     }
18: }
19: 
20: kotlin {
21:     jvmToolchain(17)
22:     
23:     androidTarget()
24:     // iosX64()
25:     // iosArm64()
26:     // iosSimulatorArm64()
27: 
28:     sourceSets {
29:         val commonMain by getting {
30:             dependencies {
31:                 // Ktor
32:                 implementation(libs.ktor.client.core)
33:                 implementation(libs.ktor.client.content.negotiation)
34:                 implementation(libs.ktor.client.logging)
35:                 implementation(libs.ktor.serialization.kotlinx.json)
36: 
37:                 // Serialization
38:                 implementation(libs.kotlinx.serialization.json)
39: 
40:                 // Coroutines
41:                 implementation(libs.kotlinx.coroutines.core)
42: 
43:                 // DateTime
44:                 implementation(libs.kotlinx.datetime)
45: 
46:                 // Settings
47:                 implementation(libs.multiplatform.settings)
48: 
49:                 // DI
50:                 implementation("io.insert-koin:koin-core:3.4.0")
51:                 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
52: 
53:                 // No external caching library needed - using custom implementation
54:             }
55:         }
56: 
57:         val androidMain by getting {
58:             dependencies {
59:                 implementation(libs.ktor.client.android)
60:                 implementation("androidx.datastore:datastore-preferences:1.0.0")
61:                 implementation("com.russhwolf:multiplatform-settings:1.1.1")
62:                 implementation("com.russhwolf:multiplatform-settings-datastore:1.1.1")
63:                 implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.1")
64:             }
65:         }
66: 
67:         // val iosX64Main by getting
68:         // val iosArm64Main by getting
69:         // val iosSimulatorArm64Main by getting
70:         // val iosMain by creating {
71:         //     dependsOn(commonMain)
72:         //     iosX64Main.dependsOn(this)
73:         //     iosArm64Main.dependsOn(this)
74:         //     iosSimulatorArm64Main.dependsOn(this)
75: 
76:         //     dependencies {
77:         //         implementation(libs.ktor.client.darwin)
78:         //     }
79:         // }
80: 
81:         val commonTest by getting {
82:             dependencies {
83:                 implementation(kotlin("test"))
84:                 implementation(libs.mockk)
85:                 implementation(libs.kotlinx.coroutines.test)
86:                 implementation(libs.koin.test)
87:                 implementation(libs.turbine)
88:             }
89:         }
90:     }
91: }
92: 
93: repositories {
94:     google()
95:     mavenCentral()
96:     gradlePluginPortal()
97: }
````

## File: .gitignore
````
 1: # IntelliJ
 2: .idea/
 3: *.iml
 4: *.ipr
 5: *.iws
 6: 
 7: 
 8: # Android Studio
 9: local.properties
10: .DS_Store
11: 
12: # Generated files
13: captures/
14: output.json
15: *.log
16: *.lock
17: *.bin
18: 
19: # OS-specific files
20: Thumbs.db
21: .DS_Store
22: 
23: 
24: # Gradle files
25: .gradle/
26: *.iml
27: .gradle/**
28: /build/
29: **/build/
30: 
31: # Android
32: *.apk
33: *.ap_
34: *.dex
35: *.class
36: *.jar
37: *.war
38: *.ear
39: *.keystore
40: *.jks
41: local.properties
42: captures/
43: output.json
44: 
45: # IntelliJ / Android Studio
46: .idea/
47: /*.iml
48: *.hprof
49: *.DS_Store
50: *.log
51: 
52: # Kotlin specific caches
53: **/build/kotlin/
54: **/build/tmp/
55: **/build/intermediates/
56: **/build/outputs/
57: **/build/generated/
58: **/build/kotlin/compile*/
59: 
60: # Other system-specific files
61: .DS_Store
62: Thumbs.db
````

## File: build.gradle.kts
````
 1: plugins {
 2:     kotlin("multiplatform") version "1.9.22" apply false
 3:     kotlin("plugin.serialization") version "1.9.22" apply false
 4:     id("com.android.library") version "8.2.2" apply false
 5:     id("com.android.application") version "8.2.2" apply false
 6: }
 7: 
 8: allprojects {
 9:     repositories {
10:         google()
11:         mavenCentral()
12:     }
13: }
````

## File: gradle.properties
````
 1: # Gradle
 2: org.gradle.jvmargs=-Xmx2048M -Dfile.encoding=UTF-8 -Dkotlin.daemon.jvm.options\="-Xmx2048M"
 3: org.gradle.parallel=true
 4: 
 5: # Android
 6: android.useAndroidX=true
 7: android.nonTransitiveRClass=true
 8: android.enableJetifier=false
 9: 
10: # Kotlin
11: kotlin.code.style=official
12: kotlin.mpp.enableCInteropCommonization=true
13: kotlin.mpp.androidSourceSetLayoutVersion=2
````

## File: gradlew
````
  1: #!/bin/sh
  2: 
  3: #
  4: # Copyright © 2015-2021 the original authors.
  5: #
  6: # Licensed under the Apache License, Version 2.0 (the "License");
  7: # you may not use this file except in compliance with the License.
  8: # You may obtain a copy of the License at
  9: #
 10: #      https://www.apache.org/licenses/LICENSE-2.0
 11: #
 12: # Unless required by applicable law or agreed to in writing, software
 13: # distributed under the License is distributed on an "AS IS" BASIS,
 14: # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 15: # See the License for the specific language governing permissions and
 16: # limitations under the License.
 17: #
 18: # SPDX-License-Identifier: Apache-2.0
 19: #
 20: 
 21: ##############################################################################
 22: #
 23: #   Gradle start up script for POSIX generated by Gradle.
 24: #
 25: #   Important for running:
 26: #
 27: #   (1) You need a POSIX-compliant shell to run this script. If your /bin/sh is
 28: #       noncompliant, but you have some other compliant shell such as ksh or
 29: #       bash, then to run this script, type that shell name before the whole
 30: #       command line, like:
 31: #
 32: #           ksh Gradle
 33: #
 34: #       Busybox and similar reduced shells will NOT work, because this script
 35: #       requires all of these POSIX shell features:
 36: #         * functions;
 37: #         * expansions «$var», «${var}», «${var:-default}», «${var+SET}»,
 38: #           «${var#prefix}», «${var%suffix}», and «$( cmd )»;
 39: #         * compound commands having a testable exit status, especially «case»;
 40: #         * various built-in commands including «command», «set», and «ulimit».
 41: #
 42: #   Important for patching:
 43: #
 44: #   (2) This script targets any POSIX shell, so it avoids extensions provided
 45: #       by Bash, Ksh, etc; in particular arrays are avoided.
 46: #
 47: #       The "traditional" practice of packing multiple parameters into a
 48: #       space-separated string is a well documented source of bugs and security
 49: #       problems, so this is (mostly) avoided, by progressively accumulating
 50: #       options in "$@", and eventually passing that to Java.
 51: #
 52: #       Where the inherited environment variables (DEFAULT_JVM_OPTS, JAVA_OPTS,
 53: #       and GRADLE_OPTS) rely on word-splitting, this is performed explicitly;
 54: #       see the in-line comments for details.
 55: #
 56: #       There are tweaks for specific operating systems such as AIX, CygWin,
 57: #       Darwin, MinGW, and NonStop.
 58: #
 59: #   (3) This script is generated from the Groovy template
 60: #       https://github.com/gradle/gradle/blob/HEAD/platforms/jvm/plugins-application/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt
 61: #       within the Gradle project.
 62: #
 63: #       You can find Gradle at https://github.com/gradle/gradle/.
 64: #
 65: ##############################################################################
 66: 
 67: # Attempt to set APP_HOME
 68: 
 69: # Resolve links: $0 may be a link
 70: app_path=$0
 71: 
 72: # Need this for daisy-chained symlinks.
 73: while
 74:     APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
 75:     [ -h "$app_path" ]
 76: do
 77:     ls=$( ls -ld "$app_path" )
 78:     link=${ls#*' -> '}
 79:     case $link in             #(
 80:       /*)   app_path=$link ;; #(
 81:       *)    app_path=$APP_HOME$link ;;
 82:     esac
 83: done
 84: 
 85: # This is normally unused
 86: # shellcheck disable=SC2034
 87: APP_BASE_NAME=${0##*/}
 88: # Discard cd standard output in case $CDPATH is set (https://github.com/gradle/gradle/issues/25036)
 89: APP_HOME=$( cd -P "${APP_HOME:-./}" > /dev/null && printf '%s
 90: ' "$PWD" ) || exit
 91: 
 92: # Use the maximum available, or set MAX_FD != -1 to use that value.
 93: MAX_FD=maximum
 94: 
 95: warn () {
 96:     echo "$*"
 97: } >&2
 98: 
 99: die () {
100:     echo
101:     echo "$*"
102:     echo
103:     exit 1
104: } >&2
105: 
106: # OS specific support (must be 'true' or 'false').
107: cygwin=false
108: msys=false
109: darwin=false
110: nonstop=false
111: case "$( uname )" in                #(
112:   CYGWIN* )         cygwin=true  ;; #(
113:   Darwin* )         darwin=true  ;; #(
114:   MSYS* | MINGW* )  msys=true    ;; #(
115:   NONSTOP* )        nonstop=true ;;
116: esac
117: 
118: CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
119: 
120: 
121: # Determine the Java command to use to start the JVM.
122: if [ -n "$JAVA_HOME" ] ; then
123:     if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
124:         # IBM's JDK on AIX uses strange locations for the executables
125:         JAVACMD=$JAVA_HOME/jre/sh/java
126:     else
127:         JAVACMD=$JAVA_HOME/bin/java
128:     fi
129:     if [ ! -x "$JAVACMD" ] ; then
130:         die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME
131: 
132: Please set the JAVA_HOME variable in your environment to match the
133: location of your Java installation."
134:     fi
135: else
136:     JAVACMD=java
137:     if ! command -v java >/dev/null 2>&1
138:     then
139:         die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
140: 
141: Please set the JAVA_HOME variable in your environment to match the
142: location of your Java installation."
143:     fi
144: fi
145: 
146: # Increase the maximum file descriptors if we can.
147: if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
148:     case $MAX_FD in #(
149:       max*)
150:         # In POSIX sh, ulimit -H is undefined. That's why the result is checked to see if it worked.
151:         # shellcheck disable=SC2039,SC3045
152:         MAX_FD=$( ulimit -H -n ) ||
153:             warn "Could not query maximum file descriptor limit"
154:     esac
155:     case $MAX_FD in  #(
156:       '' | soft) :;; #(
157:       *)
158:         # In POSIX sh, ulimit -n is undefined. That's why the result is checked to see if it worked.
159:         # shellcheck disable=SC2039,SC3045
160:         ulimit -n "$MAX_FD" ||
161:             warn "Could not set maximum file descriptor limit to $MAX_FD"
162:     esac
163: fi
164: 
165: # Collect all arguments for the java command, stacking in reverse order:
166: #   * args from the command line
167: #   * the main class name
168: #   * -classpath
169: #   * -D...appname settings
170: #   * --module-path (only if needed)
171: #   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.
172: 
173: # For Cygwin or MSYS, switch paths to Windows format before running java
174: if "$cygwin" || "$msys" ; then
175:     APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
176:     CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )
177: 
178:     JAVACMD=$( cygpath --unix "$JAVACMD" )
179: 
180:     # Now convert the arguments - kludge to limit ourselves to /bin/sh
181:     for arg do
182:         if
183:             case $arg in                                #(
184:               -*)   false ;;                            # don't mess with options #(
185:               /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
186:                     [ -e "$t" ] ;;                      #(
187:               *)    false ;;
188:             esac
189:         then
190:             arg=$( cygpath --path --ignore --mixed "$arg" )
191:         fi
192:         # Roll the args list around exactly as many times as the number of
193:         # args, so each arg winds up back in the position where it started, but
194:         # possibly modified.
195:         #
196:         # NB: a `for` loop captures its iteration list before it begins, so
197:         # changing the positional parameters here affects neither the number of
198:         # iterations, nor the values presented in `arg`.
199:         shift                   # remove old arg
200:         set -- "$@" "$arg"      # push replacement arg
201:     done
202: fi
203: 
204: 
205: # Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
206: DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
207: 
208: # Collect all arguments for the java command:
209: #   * DEFAULT_JVM_OPTS, JAVA_OPTS, JAVA_OPTS, and optsEnvironmentVar are not allowed to contain shell fragments,
210: #     and any embedded shellness will be escaped.
211: #   * For example: A user cannot expect ${Hostname} to be expanded, as it is an environment variable and will be
212: #     treated as '${Hostname}' itself on the command line.
213: 
214: set -- \
215:         "-Dorg.gradle.appname=$APP_BASE_NAME" \
216:         -classpath "$CLASSPATH" \
217:         org.gradle.wrapper.GradleWrapperMain \
218:         "$@"
219: 
220: # Stop when "xargs" is not available.
221: if ! command -v xargs >/dev/null 2>&1
222: then
223:     die "xargs is not available"
224: fi
225: 
226: # Use "xargs" to parse quoted args.
227: #
228: # With -n1 it outputs one arg per line, with the quotes and backslashes removed.
229: #
230: # In Bash we could simply go:
231: #
232: #   readarray ARGS < <( xargs -n1 <<<"$var" ) &&
233: #   set -- "${ARGS[@]}" "$@"
234: #
235: # but POSIX shell has neither arrays nor command substitution, so instead we
236: # post-process each arg (as a line of input to sed) to backslash-escape any
237: # character that might be a shell metacharacter, then use eval to reverse
238: # that process (while maintaining the separation between arguments), and wrap
239: # the whole thing up as a single "set" statement.
240: #
241: # This will of course break if any of these variables contains a newline or
242: # an unmatched quote.
243: #
244: 
245: eval "set -- $(
246:         printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
247:         xargs -n1 |
248:         sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
249:         tr '\n' ' '
250:     )" '"$@"'
251: 
252: exec "$JAVACMD" "$@"
````

## File: gradlew.bat
````
 1: @rem
 2: @rem Copyright 2015 the original author or authors.
 3: @rem
 4: @rem Licensed under the Apache License, Version 2.0 (the "License");
 5: @rem you may not use this file except in compliance with the License.
 6: @rem You may obtain a copy of the License at
 7: @rem
 8: @rem      https://www.apache.org/licenses/LICENSE-2.0
 9: @rem
10: @rem Unless required by applicable law or agreed to in writing, software
11: @rem distributed under the License is distributed on an "AS IS" BASIS,
12: @rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
13: @rem See the License for the specific language governing permissions and
14: @rem limitations under the License.
15: @rem
16: @rem SPDX-License-Identifier: Apache-2.0
17: @rem
18: 
19: @if "%DEBUG%"=="" @echo off
20: @rem ##########################################################################
21: @rem
22: @rem  Gradle startup script for Windows
23: @rem
24: @rem ##########################################################################
25: 
26: @rem Set local scope for the variables with windows NT shell
27: if "%OS%"=="Windows_NT" setlocal
28: 
29: set DIRNAME=%~dp0
30: if "%DIRNAME%"=="" set DIRNAME=.
31: @rem This is normally unused
32: set APP_BASE_NAME=%~n0
33: set APP_HOME=%DIRNAME%
34: 
35: @rem Resolve any "." and ".." in APP_HOME to make it shorter.
36: for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi
37: 
38: @rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
39: set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
40: 
41: @rem Find java.exe
42: if defined JAVA_HOME goto findJavaFromJavaHome
43: 
44: set JAVA_EXE=java.exe
45: %JAVA_EXE% -version >NUL 2>&1
46: if %ERRORLEVEL% equ 0 goto execute
47: 
48: echo. 1>&2
49: echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
50: echo. 1>&2
51: echo Please set the JAVA_HOME variable in your environment to match the 1>&2
52: echo location of your Java installation. 1>&2
53: 
54: goto fail
55: 
56: :findJavaFromJavaHome
57: set JAVA_HOME=%JAVA_HOME:"=%
58: set JAVA_EXE=%JAVA_HOME%/bin/java.exe
59: 
60: if exist "%JAVA_EXE%" goto execute
61: 
62: echo. 1>&2
63: echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
64: echo. 1>&2
65: echo Please set the JAVA_HOME variable in your environment to match the 1>&2
66: echo location of your Java installation. 1>&2
67: 
68: goto fail
69: 
70: :execute
71: @rem Setup the command line
72: 
73: set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
74: 
75: 
76: @rem Execute Gradle
77: "%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
78: 
79: :end
80: @rem End local scope for the variables with windows NT shell
81: if %ERRORLEVEL% equ 0 goto mainEnd
82: 
83: :fail
84: rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
85: rem the _cmd.exe /c_ return code!
86: set EXIT_CODE=%ERRORLEVEL%
87: if %EXIT_CODE% equ 0 set EXIT_CODE=1
88: if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
89: exit /b %EXIT_CODE%
90: 
91: :mainEnd
92: if "%OS%"=="Windows_NT" endlocal
93: 
94: :omega
````

## File: libraries.toml
````toml
 1: [libraries]
 2: accompanist-navigation-animation = { module = "com.google.accompanist:accompanist-navigation-animation", version.ref = "accompanistSystemuicontroller" }
 3: accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanistSystemuicontroller" }
 4: accompanist-systemuicontroller = { module = "com.google.accompanist:accompanist-systemuicontroller", version.ref = "accompanistSystemuicontroller" }
 5: androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
 6: androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastorePreferences" }
 7: androidx-espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espressoCore" }
 8: androidx-hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
 9: androidx-junit = { module = "androidx.test.ext:junit", version.ref = "junitVersion" }
10: androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycleViewmodelCompose" }
11: androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
12: androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigationCompose" }
13: androidx-security-crypto = { module = "androidx.security:security-crypto", version.ref = "securityCrypto" }
14: androidx-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
15: androidx-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
16: coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coilCompose" }
17: hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hiltAndroid" }
18: hilt-android-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hiltAndroidCompiler" }
19: junit = { module = "junit:junit", version.ref = "junit" }
20: kamel-image = { module = "media.kamel:kamel-image", version.ref = "kamelImage" }
21: koin-core = { module = "io.insert-koin:koin-core", version.ref = "koinCore" }
22: koin-test = { module = "io.insert-koin:koin-test", version.ref = "koinTest" }
23: kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
24: androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activityCompose" }
25: compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }
26: compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling", version.ref = "compose" }
27: compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview", version.ref = "compose" }
28: compose-foundation = { module = "androidx.compose.foundation:foundation", version.ref = "compose" }
29: compose-material3 = { module = "androidx.compose.material3:material3", version.ref = "compose-material3" }
30: kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinxCoroutinesCore" }
31: kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinxCoroutinesTest" }
32: kotlinx-coroutines-test-v173 = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinxCoroutinesTestVersion" }
33: kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinxDatetime" }
34: kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
35: ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktorClientAndroid" }
36: ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktorClientContentNegotiation" }
37: ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktorClientCore" }
38: ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktorClientDarwin" }
39: ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktorClientLogging" }
40: ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktorClientMock" }
41: ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktorSerializationKotlinxJson" }
42: mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
43: multiplatform-settings = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatformSettings" }
44: turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
45: androidx-compose-material = { group = "androidx.compose.material", name = "material", version.ref = "compose" }
46: androidx-ui-test-junit4-android = { group = "androidx.compose.ui", name = "ui-test-junit4-android", version.ref = "uiTestJunit4Android" }
47: 
48: [versions]
49: mockk = "1.13.8"
50: junit = "4.13.2"
51: turbine = "0.12.1"
52: koinTest = "3.4.0"
53: accompanistSystemuicontroller = "0.30.0"
54: composeBom = "2023.01.00"
55: datastorePreferences = "1.0.0"
56: espressoCore = "3.4.0"
57: hiltNavigationCompose = "1.0.0"
58: junitVersion = "1.1.3"
59: lifecycleViewmodelCompose = "2.4.0"
60: navigationCompose = "2.4.0"
61: securityCrypto = "1.1.0-alpha03"
62: coilCompose = "1.4.0"
63: hiltAndroid = "2.38.1"
64: hiltAndroidCompiler = "2.38.1"
65: kamelImage = "0.3.0"
66: koinCore = "3.1.2"
67: kotlin = "1.9.0"
68: androidx-activityCompose = "1.3.1"
69: compose = "1.0.5"
70: compose-material3 = "1.0.0-alpha01"
71: kotlinxCoroutinesCore = "1.7.3"
72: kotlinxCoroutinesTest = "1.7.3"
73: kotlinxCoroutinesTestVersion = "1.7.3"
74: kotlinxDatetime = "0.5.0"
75: kotlinxSerializationJson = "1.6.0"
76: ktorClientAndroid = "2.0.0"
77: ktorClientContentNegotiation = "2.0.0"
78: ktorClientCore = "2.0.0"
79: ktorClientDarwin = "2.0.0"
80: ktorClientLogging = "2.0.0"
81: ktorClientMock = "2.0.0"
82: ktorSerializationKotlinxJson = "2.0.0"
83: multiplatformSettings = "1.1.1"
84: composeMaterial = "1.4.1"
85: uiTestJunit4Android = "1.5.4"
````

## File: README.md
````markdown
  1: # CocktailCraft App
  2: 
  3: ## Overview
  4: CocktailCraft is a Kotlin Multiplatform project for a feature-rich cocktail ordering and discovery application, designed to run in Android platform. The app allows users to browse, search, and order various cocktails, manage their shopping cart, track orders, and maintain a profile. It leverages modern mobile app development practices including MVVM architecture, reactive programming, and dependency injection.
  5: 
  6: ![CocktailCraft Home Screen](docs/images/Screenshot_20250419_014459.png)
  7: 
  8: ## Features
  9: - **Cocktail Discovery**: Browse and search for cocktails with detailed information
 10: - **Advanced Search & Filtering**: Multi-criteria search with filters for ingredients, taste profiles, complexity, and more
 11: - **Shopping Cart**: Add cocktails to cart, update quantities, and checkout
 12: - **User Authentication**: Register, login, and manage user profiles
 13: - **Order Management**: Place orders and track order history
 14: - **Favorites**: Save and manage favorite cocktails
 15: - **Reviews**: Read and submit reviews for cocktails
 16: - **Personalized Recommendations**: "You might also like" suggestions based on viewing history and preferences
 17: - **Dark Mode Support**: Adaptive UI that supports both light and dark themes with smooth transitions
 18: - **Offline Mode**: Browse recently viewed cocktails and favorites without internet connection
 19: - **Robust Error Handling**: User-friendly error messages with recovery options
 20: - **Polished Animations**: Enhanced user experience with smooth animations and micro-interactions
 21: - **Cross-Platform**: Same codebase for both Android and iOS platforms
 22: 
 23: ## Architecture
 24: The application follows the **Clean Architecture** pattern with **MVVM** (Model-View-ViewModel) for presentation, reactive state management, and a modular **Dependency Injection** system:
 25: 
 26: ```
 27: ┌─────────────────────────────────────────────────────────────────────────────────┐
 28: │                              Presentation Layer                                  │
 29: │                                                                                  │
 30: │  ┌─────────────┐       ┌─────────────────┐       ┌───────────────────┐          │
 31: │  │   Screens   │◄─────►│    ViewModels   │◄─────►│    UI Elements    │          │
 32: │  │  (Compose)  │       │ (KoinViewModel) │       │ (Compose/Material)│          │
 33: │  └─────────────┘       └─────────────────┘       └───────────────────┘          │
 34: │         ▲                       ▲                         ▲                      │
 35: │         │                       │                         │                      │
 36: │         ▼                       ▼                         ▼                      │
 37: │  ┌─────────────┐       ┌─────────────────┐       ┌───────────────────┐          │
 38: │  │  Navigation │       │  State Handling │       │   Theme Manager   │          │
 39: │  │  (Compose)  │       │  (StateFlow)    │       │   (Dark Mode)     │          │
 40: │  └─────────────┘       └─────────────────┘       └───────────────────┘          │
 41: └─────────────────────────────────┬─────────────────────────────────────────────┘
 42:                                   │
 43: ┌─────────────────────────────────▼─────────────────────────────────────────────┐
 44: │                               Domain Layer                                     │
 45: │                                                                                │
 46: │  ┌─────────────┐       ┌─────────────────┐       ┌───────────────────┐        │
 47: │  │   Models    │       │    Use Cases    │       │    Repositories   │        │
 48: │  │             │       │                 │       │    (Interfaces)   │        │
 49: │  └─────────────┘       └─────────────────┘       └───────────────────┘        │
 50: └─────────────────────────────────┬─────────────────────────────────────────────┘
 51:                                   │
 52: ┌─────────────────────────────────▼─────────────────────────────────────────────┐
 53: │                               Data Layer                                       │
 54: │                                                                                │
 55: │  ┌─────────────┐       ┌─────────────────┐       ┌───────────────────┐        │
 56: │  │ Repository  │       │   Data Sources  │       │      Mappers      │        │
 57: │  │    Impl     │◄─────►│ Remote / Local  │       │                   │        │
 58: │  └─────────────┘       └─────────────────┘       └───────────────────┘        │
 59: │         ▲                       ▲                                              │
 60: │         │                       │                                              │
 61: │         ▼                       ▼                                              │
 62: │  ┌─────────────┐       ┌─────────────────┐                                    │
 63: │  │   Offline   │       │  Error Handling │                                    │
 64: │  │   Support   │       │  (ErrorUtils)   │                                    │
 65: │  └─────────────┘       └─────────────────┘                                    │
 66: └─────────────────────────────────┬─────────────────────────────────────────────┘
 67:                                   │
 68: ┌─────────────────────────────────▼─────────────────────────────────────────────┐
 69: │                         Dependency Injection                                   │
 70: │                                                                                │
 71: │  ┌─────────────┐       ┌─────────────────┐       ┌───────────────────┐        │
 72: │  │  Network    │       │      Data       │       │      Domain       │        │
 73: │  │   Module    │       │     Module      │       │      Module       │        │
 74: │  └─────────────┘       └─────────────────┘       └───────────────────┘        │
 75: │                                                                                │
 76: │  ┌─────────────────────────────────────────────────────────────────────┐      │
 77: │  │                          Platform Module                             │      │
 78: │  └─────────────────────────────────────────────────────────────────────┘      │
 79: └────────────────────────────────────────────────────────────────────────────────┘
 80: ```
 81: 
 82: ### Key Architecture Components:
 83: - **Shared Module**: Contains common business logic, models, and repositories that can be used across platforms
 84: - **Platform-Specific Apps**: Android and iOS implementations with platform-specific UI and functionality
 85: - **MVVM Pattern**: Separates UI (View) from business logic (ViewModel) and data management (Model)
 86: - **Repository Pattern**: Abstracts data sources and provides a clean API for the domain layer
 87: - **Use Case Pattern**: Encapsulates business logic in single-responsibility classes
 88: - **Dependency Injection**: Modular Koin setup for better testability and separation of concerns
 89: 
 90: ### Architecture Layers:
 91: - **Presentation Layer**:
 92:   - **Screens**: Compose UI components that display data and handle user interactions
 93:   - **ViewModels**: Manage UI state, process user actions, and communicate with the domain layer
 94:   - **UI Elements**: Reusable Compose components for consistent UI across the app (see [UI Components Documentation](docs/UI_Components.md))
 95:   - **KoinViewModel**: Base class for all ViewModels that provides standardized Koin integration
 96:   - **Navigation**: Compose Navigation for handling screen transitions and deep linking
 97:   - **State Handling**: Kotlin StateFlow and SharedFlow for reactive UI updates
 98:   - **Theme Manager**: Manages light/dark mode and theme preferences with smooth transitions
 99: 
100: - **Domain Layer**:
101:   - **Models**: Core business entities that represent the application's data structures
102:   - **Use Cases**: Business logic operations that orchestrate data flow between UI and data layers
103:   - **Repository Interfaces**: Define contracts for data access without implementation details
104:   - **Business Rules**: Encapsulate application-specific business logic and validation
105: 
106: - **Data Layer**:
107:   - **Repository Implementations**: Concrete implementations of repository interfaces
108:   - **Data Sources**: Remote (API) and local (cache/database) data access
109:   - **Mappers**: Convert between data models and domain models
110:   - **Network Utilities**: Handle API communication, caching, and offline support
111:   - **Offline Support**: CocktailCache and NetworkMonitor for offline functionality
112:   - **Error Handling**: Centralized error handling with ErrorUtils and recovery options
113: 
114: - **Dependency Injection Layer**:
115:   - **Network Module**: Provides HTTP client, API interfaces, and network monitoring
116:   - **Data Module**: Provides repositories and caching mechanisms
117:   - **Domain Module**: Provides use cases and application configuration
118:   - **Platform Module**: Provides platform-specific dependencies
119:   - **Test Module**: Provides mock implementations for testing
120: 
121: ### Cross-Cutting Concerns:
122: - **Error Handling**: Standardized error handling through BaseViewModel with user-friendly messages
123: - **Offline Mode**: Network state monitoring and data caching for offline access
124: - **Reactive Programming**: Kotlin Flow for asynchronous data streams and UI updates
125: - **Dependency Injection**: Modular Koin setup for better testability and separation of concerns
126: - **Dark Mode**: System-integrated and user-configurable theme preferences
127: 
128: For more detailed architecture diagrams, please see the [Architecture Documentation](docs/README.md) which includes high-level architecture, component diagrams, use case diagrams, and more.
129: 
130: ### Animations and Transitions
131: The app features a comprehensive set of animations and transitions to enhance the user experience:
132: - **Micro-interactions**: Button animations, hover effects, and feedback animations
133: - **List Animations**: Staggered entry animations and smooth item transitions
134: - **Loading States**: Shimmer loading effects for a polished loading experience
135: - **Screen Transitions**: Coordinated animations for navigation between screens
136: - **Theme Transitions**: Smooth animations when switching between light and dark modes
137: - **Optimized Scrolling**: Batched loading mechanism that loads and animates items in small groups
138: - **Performance Optimizations**: Techniques to maintain smooth animations during fast scrolling
139: 
140: The animation system is designed for both visual appeal and performance:
141: - **Batched Loading**: Items load in groups of 3 with staggered animations for better performance
142: - **Predictive Loading**: Preloads items that will soon be visible (3 batches ahead of current view)
143: - **Direct Animation Properties**: Uses optimized animation properties for smooth scrolling
144: - **Coordinated Animations**: Items within the same batch animate together for a cohesive effect
145: 
146: For detailed information about the animations implementation, see:
147: - [Animations Documentation](docs/animations.md) - Overview, implementation details, and best practices
148: 
149: ### Dependency Injection
150: The app uses Koin for dependency injection with a modular approach:
151: - **Modular Structure**: Separate modules for network, data, and domain layers
152: - **Testability**: Easy mocking and test module setup
153: - **ViewModel Integration**: Standardized pattern for ViewModel dependency injection
154: 
155: For detailed information about the dependency injection implementation, see:
156: - [Dependency Injection Documentation](docs/DependencyInjection.md) - Overview, module structure, and best practices
157: - [Dependency Injection Migration Guide](docs/DependencyInjectionMigration.md) - Guide for migrating existing code to the new DI approach
158: 
159: ## Libraries Used
160: 
161: ### Core & Architecture
162: - **Kotlin Multiplatform**: For sharing code between Android and iOS
163: - **Coroutines + Flow**: For asynchronous programming and reactive streams
164: - **Koin**: For dependency injection
165: - **Ktor**: For networking and API calls
166: - **Kotlinx.Serialization**: For JSON parsing
167: - **Multiplatform Settings**: For cross-platform data storage
168: 
169: ### UI & Navigation
170: - **Jetpack Compose**: Modern declarative UI toolkit for Android
171: - **Material3**: For consistent Material Design implementation
172: - **Accompanist**: Compose UI utilities
173: - **Navigation Compose**: For handling navigation between screens
174: - **Coil & Kamel**: For image loading and caching
175: - **Animation Utilities**: Custom animation components and utilities for enhanced UX
176: - **Shimmer Effects**: Loading state animations with shimmer effects
177: 
178: ### Testing
179: - **JUnit**: For unit testing
180: - **Mockito & Mockk**: For mocking in tests
181: - **Turbine**: For testing Flow emissions
182: - **Espresso**: For UI testing
183: 
184: For a detailed list of all libraries with versions and purposes, see the [Libraries Documentation](docs/Libraries.md).
185: 
186: For information about the reusable UI components in the app, see the [UI Components Documentation](docs/UI_Components.md).
187: 
188: ## Package Structure
189: 
190: ### Domain Layer
191: - **Models**: Data classes representing core business entities like `Cocktail`, `User`, `Order`, etc.
192: - **Repositories**: Interfaces defining data access contracts
193: - **UseCases**: Business logic operations and workflows
194: 
195: ### Data Layer
196: - **Repository Implementations**: Concrete implementations of repository interfaces
197: - **Remote Data Sources**: API clients and network-related code
198: - **Local Data Sources**: Database and preference storage
199: 
200: ### Presentation Layer (Android)
201: - **ViewModels**: Manage UI state and handle user interactions
202: - **Screens**: Compose UI components representing app screens
203: - **UI Elements**: Reusable UI components
204: 
205: ## Key Features Implementation
206: 
207: ### Cart Functionality
208: - **CartViewModel**: Manages cart state and operations like adding/removing items and checkout
209: - **CartRepository**: Handles cart data persistence and retrieval
210: - **CartScreen**: UI for displaying and managing cart items
211: 
212: ### User Authentication
213: - **AuthRepository**: Handles user registration, login, and session management
214: - **ProfileViewModel**: Manages user profile data and settings
215: - **ProfileScreen**: UI for user authentication and profile management
216: 
217: ### Cocktail Discovery
218: - **HomeViewModel**: Manages cocktail listings, categories, and search
219: - **CocktailRepository**: Provides cocktail data from remote and local sources
220: - **HomeScreen & CocktailDetailScreen**: UI for browsing and viewing cocktail details
221: 
222: ### Recommendation System
223: - **CocktailRecommendationEngine**: Client-side engine that generates personalized recommendations
224: - **RecommendationsSection**: UI component that displays "You might also like" suggestions
225: - **Multiple Strategies**: Category-based, ingredient-based, and user preference-based recommendations
226: - **Offline Support**: Works with cached data when no internet connection is available
227: 
228: ### Dark Mode Support
229: - **ThemeViewModel**: Manages theme state and preferences
230: - **AppTheme**: Provides theme-specific colors and typography
231: - **ThemeAwareComponents**: UI components that adapt to the current theme
232: - **Smooth Transitions**: Animated transitions between light and dark modes
233: - **System Integration**: Option to follow system theme settings
234: 
235: ### Offline Mode
236: - **NetworkMonitor**: Detects and monitors network connectivity
237: - **OfflineRepository**: Caches data for offline access
238: - **CocktailRepository**: Provides fallback to cached data when offline
239: - **UI Indicators**: Clear indicators of offline status and available actions
240: 
241: ### Error Handling
242: - **ErrorUtils**: Centralized error handling and categorization
243: - **BaseViewModel**: Common error handling for all ViewModels
244: - **ErrorDialog & ErrorBanner**: User-friendly error display components
245: - **Recovery Actions**: Actionable error messages with recovery options
246: 
247: ## Test Coverage
248: The application includes comprehensive test coverage:
249: 
250: - **Unit Tests**: Testing individual components in isolation
251: - **Integration Tests**: Testing component interactions
252: - **ViewModel Tests**: Verifying ViewModel behavior and state management
253: - **Repository Tests**: Testing data access and manipulation
254: - **UI Tests**: Verifying screen workflows and user interactions
255: 
256: ## Setup & Running the Project
257: 1. **Clone the Repository**:
258:    ```bash
259:    git clone <repository-url>
260:    cd CocktailCraft
261:    ```
262: 
263: 2. **Open the Project**:
264:    - Open the project in Android Studio.
265: 
266: 3. **Sync the Project**:
267:    - Allow Gradle to sync and download all necessary dependencies.
268: 
269: 4. **Run on Android**:
270:    - Select the `androidApp` configuration
271:    - Choose an emulator or device
272:    - Click Run
273: 
274: ## Development Setup
275: - **Android Studio**: Latest version recommended (Flamingo or newer)
276: - **JDK**: Version 17 or higher
277: - **Xcode**: Latest version (for iOS development)
278: - **Gradle**: Managed by the project
279: - **Git**: For version control
280: 
281: ## Troubleshooting
282: - **Build Issues**: Try cleaning and rebuilding the project
283: - **KMP Plugin Issues**: Make sure the Kotlin Multiplatform plugin is up to date
284: - **iOS Builds**: Make sure the Kotlin/Native target is properly configured
285: - **Dependency Resolution**: Check Gradle settings and versions.toml file
286: 
287: ## Contributing
288: Contributions are welcome! Please follow the standard GitHub flow:
289: 1. Fork the repository
290: 2. Create a feature branch
291: 3. Make your changes
292: 4. Submit a pull request
293: 
294: 
295: ## Detailed Test Cases
296: 
297: For comprehensive information about the test cases and testing approach, see the [Test Cases Documentation](TEST_CASES.md).
````

## File: settings.gradle.kts
````
 1: pluginManagement {
 2:     repositories {
 3:         google()
 4:         mavenCentral()
 5:         gradlePluginPortal()
 6:     }
 7: }
 8: 
 9: dependencyResolutionManagement {
10:     repositories {
11:         google()
12:         mavenCentral()
13:     }
14:     versionCatalogs {
15:         create("libs") {
16:             from(files("libraries.toml"))
17:         }
18:     }
19: }
20: 
21: rootProject.name = "CocktailCraft"
22: include(":androidApp")
23: include(":shared")
````

## File: TEST_CASES.md
````markdown
 1: # Detailed Test Cases
 2: 
 3: ## ViewModel Tests
 4: 
 5: ### CartViewModelTest
 6: - **`initial state should load cart items and total`**: Validates that the CartViewModel properly initializes by loading cart items and calculating the total price.
 7: - **`addToCart should add item to cart`**: Verifies that adding an item to the cart correctly calls the repository method and refreshes the cart data.
 8: - **`removeFromCart should remove item from cart`**: Ensures that removing an item from the cart correctly calls the repository method and updates the cart state.
 9: - **`updateQuantity should update item quantity in cart`**: Tests that updating an item's quantity properly calls the repository method with the correct parameters.
10: - **`clearCart should clear all items from cart`**: Confirms that the clearCart function properly calls the repository method and refreshes the cart.
11: - **`error handling should set error state`**: Validates that the ViewModel correctly handles and exposes errors that occur during cart operations.
12: 
13: ### ProfileViewModelTest
14: - **`initial state should be not signed in`**: Verifies that the initial state of the ProfileViewModel correctly reflects a user not being signed in.
15: - **`initial user should be null`**: Ensures that the user object is initially null when no user is signed in.
16: - **`sign in success should update state`**: Tests that a successful sign-in updates the signed-in state and user object.
17: - **`sign in failure should set error state`**: Validates error handling during the sign-in process.
18: - **`sign out should update state`**: Ensures that signing out properly updates the authentication state and clears user data.
19: - **`update profile should call repository and refresh user data`**: Tests that profile updates are properly handled and user data is refreshed.
20: 
21: ### OrderViewModelTest
22: - **`place order should create order from cart items`**: Verifies that placing an order correctly creates an order from the current cart items.
23: - **`get orders should load user orders`**: Tests that the ViewModel correctly loads and exposes user orders.
24: - **`cancel order should update order status`**: Validates that canceling an order updates its status appropriately.
25: - **`track order should return order details`**: Ensures that order tracking functionality returns the correct order information.
26: 
27: ## Domain Layer Tests
28: 
29: ### PlaceOrderUseCaseTest
30: - **`invoke should create order and add to repository`**: Tests that the PlaceOrderUseCase correctly creates an order from cart items and adds it to the repository.
31: - **`invoke should handle empty cart`**: Verifies appropriate handling of empty cart situations.
32: - **`invoke should handle repository errors`**: Tests error handling when repository operations fail.
33: - **`generated order should have correct properties`**: Ensures that generated orders have the correct properties including ID format, items list, and pricing.
34: 
35: ## Repository Tests
36: 
37: ### AuthRepositoryImplTest
38: - **`signUp should return success when email is not taken`**: Validates that user registration works when the email is available.
39: - **`signUp should return failure when email is already taken`**: Tests that registration fails appropriately when the email is already in use.
40: - **`signIn should return success with valid credentials`**: Ensures authentication works with correct credentials.
41: - **`signIn should return failure with invalid credentials`**: Verifies that authentication fails with incorrect credentials.
42: - **`signOut should clear current user`**: Tests that the sign-out process correctly clears user session data.
43: - **`isUserSignedIn should return true when user is signed in`**: Validates the signed-in state detection.
44: - **`getCurrentUser should return correct user data`**: Ensures that the repository correctly returns the current user's data.
45: - **`updateProfile should update user information`**: Tests that profile updates are correctly stored.
46: 
47: ## Running Tests
48: To run the tests, you can use:
49: 
50: - **Android Studio**:
51:   - Right-click on a test class or method and select "Run"
52:   - Navigate to the test directory, right-click and select "Run Tests in..."
53: 
54: - **Command Line**:
55:   ```bash
56:   ./gradlew test        # Run all tests
57:   ./gradlew :androidApp:testDebugUnitTest  # Run Android unit tests
58:   ```
````

## File: versions.toml
````toml
 1: [versions]
 2: accompanist = "0.32.0"
 3: agp = "8.8.2"
 4: coil = "2.6.0"
 5: compose-bom = "2025.02.00"
 6: datastore = "1.1.3"
 7: espresso = "3.6.1"
 8: hilt = "2.51.1"
 9: hilt-navigation = "1.2.0"
10: junit = "4.13.2"
11: junit-android = "1.2.1"
12: kamel = "1.0.3"
13: koin = "4.0.1"
14: kotlin = "2.0.21"
15: compose = "1.2.0-alpha01-dev709"
16: compose-material3 = "1.3.1"
17: activity-compose = "1.8.0"
18: coroutines = "1.10.1"
19: datetime = "0.6.0"
20: serialization = "1.7.3"
21: ktor = "3.0.3"
22: lifecycle = "2.8.7"
23: mockk = "1.13.8"
24: settings = "1.1.1"
25: navigation = "2.8.8"
26: security = "1.1.0-alpha06"
27: turbine = "1.0.0"
28: 
29: [libraries]
30: accompanist-navigation-animation = { module = "com.google.accompanist:accompanist-navigation-animation", version.ref = "accompanist" }
31: accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanist" }
32: accompanist-systemuicontroller = { module = "com.google.accompanist:accompanist-systemuicontroller", version.ref = "accompanist" }
33: androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
34: androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
35: androidx-espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso" }
36: androidx-hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hilt-navigation" }
37: androidx-junit = { module = "androidx.test.ext:junit", version.ref = "junit-android" }
38: androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
39: androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
40: androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
41: androidx-security-crypto = { module = "androidx.security:security-crypto", version.ref = "security" }
42: androidx-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
43: androidx-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
44: coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }
45: hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
46: hilt-android-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
47: junit = { module = "junit:junit", version.ref = "junit" }
48: kamel-image = { module = "media.kamel:kamel-image", version.ref = "kamel" }
49: koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
50: koin-test = { module = "io.insert-koin:koin-test", version.ref = "koin" }
51: kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
52: androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }
53: compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }
54: compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling", version.ref = "compose" }
55: compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview", version.ref = "compose" }
56: compose-foundation = { module = "androidx.compose.foundation:foundation", version.ref = "compose" }
57: compose-material3 = { module = "androidx.compose.material3:material3", version.ref = "compose-material3" }
58: kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
59: kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
60: kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "datetime" }
61: kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
62: ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
63: ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
64: ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
65: ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
66: ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
67: ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }
68: ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
69: mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
70: multiplatform-settings = { module = "com.russhwolf:multiplatform-settings", version.ref = "settings" }
71: turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
72: 
73: [plugins]
74: androidApplication = { id = "com.android.application", version.ref = "agp" }
75: androidLibrary = { id = "com.android.library", version.ref = "agp" }
76: kotlinAndroid = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
77: kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
78: kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
````
