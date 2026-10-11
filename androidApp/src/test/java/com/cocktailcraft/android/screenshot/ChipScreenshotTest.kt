package com.cocktailcraft.android.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.components.AccentColorPicker
import com.cocktailcraft.android.ui.components.CategoryFilterRow
import com.cocktailcraft.android.ui.components.FilterChip
import com.cocktailcraft.android.ui.components.SearchFilterChips
import com.cocktailcraft.android.ui.components.StatusIndicator
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.designsystem.AccentColorTokens
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ChipScreenshotTest(variant: ThemeVariant) : ScreenshotTest(variant) {

    @Test
    fun filterChip() = snapshot("FilterChip") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, label = "Cocktail")
            FilterChip(selected = false, onClick = {}, label = "Shot")
            FilterChip(selected = true, onClick = {}, label = "Tequila", trailingIcon = Icons.Default.Close)
        }
    }

    @Test
    fun categoryFilterRow() = snapshot("CategoryFilterRow") {
        CategoryFilterRow(
            categories = ScreenshotFixtures.categories,
            selectedCategory = "Cocktail",
            onCategorySelected = {}
        )
    }

    @Test
    fun categoryFilterRowAllSelected() = snapshot("CategoryFilterRow_all") {
        CategoryFilterRow(
            categories = ScreenshotFixtures.categories,
            selectedCategory = null,
            onCategorySelected = {}
        )
    }

    @Test
    fun searchFilterChips() = snapshot("SearchFilterChips") {
        SearchFilterChips(
            filters = ScreenshotFixtures.activeFilters,
            onClearFilter = {},
            onClearAllFilters = {}
        )
    }

    @Test
    fun accentColorPicker() = snapshot("AccentColorPicker") {
        AccentColorPicker(
            selected = AccentColorTokens.DEFAULT,
            options = AccentColorTokens.names,
            onSelect = {}
        )
    }

    @Test
    fun statusIndicator() = snapshot("StatusIndicator") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusIndicator(status = "Online", isActive = true, activeColor = AppColors.Success)
            StatusIndicator(status = "Offline", isActive = false, activeColor = AppColors.Success)
            StatusIndicator(
                status = "Delivered",
                isActive = true,
                activeColor = AppColors.Primary,
                icon = Icons.Default.CheckCircle
            )
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun variants() = ThemeVariant.lightAndDark()
    }
}
