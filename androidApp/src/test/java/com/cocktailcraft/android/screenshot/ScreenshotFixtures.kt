package com.cocktailcraft.android.screenshot

import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.SearchFilters

/**
 * Fixed sample data for screenshot tests, built on the design-time
 * [PreviewData]. Every image URL is null, so Coil resolves straight to the
 * placeholder/error state — no network, no flaky image loads.
 */
internal object ScreenshotFixtures {

    val cocktail = PreviewData.cocktail
    val outOfStockCocktail = PreviewData.outOfStockCocktail
    val cartItem = PreviewData.cartItem
    val reviews = PreviewData.reviews

    val cocktails = listOf(
        cocktail,
        outOfStockCocktail,
        cocktail.copy(
            id = "11009",
            name = "Mojito",
            category = "Cocktail",
            ingredients = listOf(
                CocktailIngredient("Light rum", "2-3 oz"),
                CocktailIngredient("Lime", "Juice of 1")
            ),
            price = Money(900)
        )
    )

    val categories = listOf("Cocktail", "Ordinary Drink", "Shot", "Punch / Party Drink")

    val activeFilters = SearchFilters(
        category = "Cocktail",
        ingredient = "Tequila",
        alcoholic = true
    )

    val completedOrder = Order(
        id = "ORD-1024",
        date = "2026-07-02",
        items = listOf(
            OrderItem(name = "Margarita", quantity = 2, price = Money(1250), cocktailId = "11007"),
            OrderItem(name = "Mojito", quantity = 1, price = Money(900), cocktailId = "11000")
        ),
        total = Money(3999),
        status = OrderStatus.DELIVERED
    )

    val inProgressOrder = completedOrder.copy(
        id = "ORD-1025",
        items = listOf(OrderItem(name = "Manhattan", quantity = 1, price = Money(1400))),
        total = Money(1999),
        status = OrderStatus.PROCESSING
    )
}
