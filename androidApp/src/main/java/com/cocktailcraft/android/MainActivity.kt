package com.cocktailcraft.android

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.cocktailcraft.android.navigation.CocktailDeepLink
import com.cocktailcraft.android.ui.main.MainScreen
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.widget.refreshFavoritesWidgetOnChange
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.viewmodel.SharedThemeViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    // System "Remove animations" (animator duration scale 0). Re-read on
    // every resume: changing it doesn't recreate the activity.
    private var systemAnimationsOff by mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        systemAnimationsOff = Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // targetSdk 35+ enforces edge-to-edge; opt in explicitly so older
        // API levels get the same window behavior.
        enableEdgeToEdge()

        // NavHost reads deep links straight from the launch intent. This
        // activity is exported, so keep only a well-formed cocktail deep link
        // (rebuilt without extras or flags) and drop everything else from
        // what Navigation sees — see CocktailDeepLink.navigationIntentOrNull.
        intent = CocktailDeepLink.navigationIntentOrNull(intent) ?: Intent(Intent.ACTION_MAIN)

        // Favorites only change while the app is on screen, so this is
        // where the Favorites widgets learn about it right away. Lives until
        // onDestroy (not just onStop), so a change made right before leaving
        // the app still reaches the widget.
        val favoritesRepository: CocktailFavoritesRepository = get()
        lifecycleScope.launch {
            refreshFavoritesWidgetOnChange(this@MainActivity, favoritesRepository.observeFavorites())
        }
        setContent {
            // Observe the shared theme state (single source of truth)
            val themeViewModel: SharedThemeViewModel = koinInject()
            val themeState by themeViewModel.uiState.collectAsStateWithLifecycle()
            val isDarkMode = themeState.isDarkMode
            val isSystemTheme = themeState.isSystemTheme

            // Get the current system dark mode state
            val isSystemInDarkTheme = isSystemInDarkTheme()

            // Determine actual dark mode:
            // - If following system theme, use the actual system dark mode setting
            // - Otherwise, use the user's preference from the ViewModel
            val effectiveDarkMode = if (isSystemTheme) isSystemInDarkTheme else isDarkMode

            // In-app Font Size setting multiplies the system font scale
            val fontScale = when (themeState.fontSize.lowercase()) {
                "small" -> 0.85f
                "large" -> 1.15f
                "xlarge" -> 1.3f
                else -> 1f
            }

            AnimatedCocktailBarTheme(
                darkTheme = effectiveDarkMode,
                accentColor = themeState.accentColor,
                highContrast = themeState.isHighContrast,
                fontScale = fontScale,
                // The in-app Reduce Motion setting or the system's
                // "Remove animations" accessibility setting
                reducedMotion = themeState.isReducedMotion || systemAnimationsOff
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        // Expose Compose testTags as resource-ids so UiAutomator
                        // (baseline profile generator, macrobenchmarks) can match
                        // nodes with By.res instead of locale-dependent text.
                        .semantics { testTagsAsResourceId = true },
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}
