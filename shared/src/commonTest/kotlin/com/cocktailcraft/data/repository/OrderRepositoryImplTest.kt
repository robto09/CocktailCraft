package com.cocktailcraft.data.repository

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.util.getOrThrow
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderRepositoryImplTest {

    private val settings = MapSettings()
    private val json = Json { ignoreUnknownKeys = true }

    private fun repository() = OrderRepositoryImpl(settings, json, AppConfigImpl())

    private fun order(id: String) = Order(
        id = id,
        date = "2026-07-07",
        items = listOf(OrderItem(name = "Margarita", quantity = 1, price = 9.5)),
        total = 9.5,
        status = "Processing"
    )

    @Test
    fun placeOrderGeneratesIdForBlankIdAndKeepsProvidedId() = runTest {
        val repo = repository()
        repo.placeOrder(order(id = "")).getOrThrow()
        repo.placeOrder(order(id = "ORD-42")).getOrThrow()

        val orders = repo.getOrders().getOrThrow()
        assertEquals(2, orders.size)
        assertTrue(orders[0].id.isNotBlank(), "placeOrder must generate an id for a blank one")
        assertEquals("ORD-42", orders[1].id)
    }

    @Test
    fun placedOrderSurvivesRepositoryRecreation() = runTest {
        repository().placeOrder(order(id = "ORD-1")).getOrThrow()

        val reloaded = repository().getOrders().getOrThrow()
        assertEquals(listOf("ORD-1"), reloaded.map { it.id })
    }
}
