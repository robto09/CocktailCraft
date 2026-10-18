package com.cocktailcraft.android.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.components.AnimatedCocktailItem
import com.cocktailcraft.android.ui.components.AnimatedThemeToggleRow
import com.cocktailcraft.android.ui.components.CartItemCard
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.components.DetailInfoCard
import com.cocktailcraft.android.ui.components.OrderCard
import com.cocktailcraft.android.ui.components.OrderSummaryCard
import com.cocktailcraft.android.ui.components.SettingsCard
import com.cocktailcraft.android.ui.components.SettingsItem
import com.cocktailcraft.domain.model.Money
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class CardScreenshotTest(variant: ThemeVariant) : ScreenshotTest(variant) {

    @Test
    fun cocktailItem() = snapshot("CocktailItem") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CocktailItem(
                cocktail = ScreenshotFixtures.cocktail,
                onClick = {},
                onAddToCart = {},
                isFavorite = true
            )
            CocktailItem(
                cocktail = ScreenshotFixtures.outOfStockCocktail,
                onClick = {},
                onAddToCart = {},
                isFavorite = false
            )
        }
    }

    @Test
    fun animatedCocktailItem() = snapshot("AnimatedCocktailItem") {
        AnimatedCocktailItem(
            cocktail = ScreenshotFixtures.cocktail,
            onClick = {},
            onAddToCart = {},
            isFavorite = true
        )
    }

    @Test
    fun cartItemCard() = snapshot("CartItemCard") {
        CartItemCard(
            item = ScreenshotFixtures.cartItem,
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
            onRemove = {},
            isFavorite = true,
            onToggleFavorite = {}
        )
    }

    @Test
    fun orderCard() = snapshot("OrderCard") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OrderCard(order = ScreenshotFixtures.completedOrder)
            OrderCard(order = ScreenshotFixtures.inProgressOrder)
        }
    }

    @Test
    fun orderSummaryCard() = snapshot("OrderSummaryCard") {
        OrderSummaryCard(subtotal = Money(3400))
    }

    @Test
    fun detailInfoCard() = snapshot("DetailInfoCard") {
        DetailInfoCard(
            title = "Instructions",
            content = { Text(ScreenshotFixtures.cocktail.instructions.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        )
    }

    @Test
    fun settingsCard() = snapshot("SettingsCard") {
        SettingsCard(
            title = "Account Settings",
            content = {
                Column {
                    SettingsItem(icon = Icons.Default.Person, title = "Edit Profile", onClick = {})
                    SettingsItem(icon = Icons.Default.Lock, title = "Change Password", onClick = {})
                    AnimatedThemeToggleRow(
                        title = "Dark Mode",
                        subtitle = "On",
                        icon = Icons.Default.DarkMode,
                        isChecked = true,
                        onToggle = {}
                    )
                    AnimatedThemeToggleRow(
                        title = "Dark Mode",
                        subtitle = "Controlled by system",
                        icon = Icons.Default.DarkMode,
                        isChecked = false,
                        onToggle = {},
                        enabled = false
                    )
                }
            }
        )
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun variants() = ThemeVariant.lightAndDark()
    }
}
