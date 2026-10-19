package com.cocktailcraft.android.screenshot

import com.cocktailcraft.android.screens.OrderDetailContent
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.viewmodel.state.OrderDetailUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The order detail screen (top bar included), in a window tall enough for
 * the whole order: a cancellable one with Reorder and its Cancel Order
 * button, and a cancelled one without, from before order items recorded
 * their cocktail (Reorder disabled, with the reason).
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(qualifiers = "w360dp-h860dp")
class OrderDetailScreenshotTest(variant: ThemeVariant) : ScreenshotTest(variant) {

    @Test
    fun cancellableOrder() = snapshot("OrderDetail_cancellable") {
        val order = ScreenshotFixtures.completedOrder.copy(status = OrderStatus.PROCESSING)
        OrderDetailContent(
            state = OrderDetailUiState(order = order),
            onBackClick = {},
            onConfirmCancel = {}
        )
    }

    @Test
    fun cancelledOrder() = snapshot("OrderDetail_cancelled") {
        val order = ScreenshotFixtures.inProgressOrder.copy(status = OrderStatus.CANCELLED)
        OrderDetailContent(
            state = OrderDetailUiState(order = order),
            onBackClick = {},
            onConfirmCancel = {}
        )
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun variants() = ThemeVariant.lightAndDark()
    }
}
