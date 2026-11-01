package com.cocktailcraft.format

import com.cocktailcraft.designsystem.AccentColorTokens
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_unknown
import com.cocktailcraft.resources.delivery_estimate_minutes
import com.cocktailcraft.resources.order_status_delivered
import com.cocktailcraft.resources.order_status_pending
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The display copy and tokens kept out of :core:domain. */
class DomainDisplayTest {

    @Test
    fun statusBasedEstimateCoversAllStatuses() {
        assertEquals(MR.strings.order_status_delivered.desc(), OrderStatus.DELIVERED.deliveryEstimate)
        assertEquals(MR.strings.delivery_estimate_minutes.format(15, 25), OrderStatus.PROCESSING.deliveryEstimate)
        assertEquals(MR.strings.common_unknown.desc(), deliveryEstimateFor(null))
    }

    @Test
    fun everyStatusHasALabel() {
        assertEquals(MR.strings.order_status_pending.desc(), OrderStatus.PENDING.displayName)
        assertEquals(OrderStatus.entries.size, OrderStatus.entries.map { it.displayName }.toSet().size)
    }

    @Test
    fun userPreferencesDefaultAccentIsTheDesignSystemDefault() {
        // The domain keeps the name as a plain value; it must stay a token
        assertEquals(AccentColorTokens.DEFAULT, UserPreferences.DEFAULT_ACCENT_COLOR)
        assertEquals(AccentColorTokens.DEFAULT, UserPreferences().accentColor)
        assertTrue(UserPreferences.DEFAULT_ACCENT_COLOR in AccentColorTokens.names)
    }
}
