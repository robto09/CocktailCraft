package com.cocktailcraft.android.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.components.AnimatedButton
import com.cocktailcraft.android.ui.components.AnimatedIconButton
import com.cocktailcraft.android.ui.components.AnimatedTextButton
import com.cocktailcraft.android.ui.components.PrimaryButton
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ButtonScreenshotTest(variant: ThemeVariant) : ScreenshotTest(variant) {

    @Test
    fun primaryButtonStates() = snapshot("PrimaryButton") {
        Column(
            modifier = Modifier.width(320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val fill = Modifier.fillMaxWidth()
            PrimaryButton(text = "Place Order", onClick = {}, modifier = fill)
            PrimaryButton(text = "Place Order", onClick = {}, modifier = fill, enabled = false)
            PrimaryButton(text = "Place Order", onClick = {}, modifier = fill, isLoading = true)
        }
    }

    @Test
    fun animatedButtons() = snapshot("AnimatedButtons") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AnimatedButton(onClick = {}) { Text("Add to Cart") }
            AnimatedButton(onClick = {}, enabled = false) { Text("Add to Cart") }
            AnimatedTextButton(text = "See all", onClick = {})
            AnimatedIconButton(
                onClick = {},
                icon = Icons.Default.Favorite,
                contentDescription = null
            )
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun variants() = ThemeVariant.lightAndDark()
    }
}
