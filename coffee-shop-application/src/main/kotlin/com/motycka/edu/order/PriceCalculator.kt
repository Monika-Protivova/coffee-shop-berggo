package com.motycka.edu.order

import com.motycka.edu.menu.MenuItemDTO

object PriceCalculator {

    fun calculatePrice(menuItems: List<MenuItemDTO>, discountInPercent: Double, orderItems: List<OrderItemDTO> = emptyList()): Double {
        val basePrice = if (orderItems.isEmpty()) {
            menuItems.sumOf { it.price }
        } else {
            orderItems.sumOf { orderItem ->
                val menuItem = menuItems.find { it.id == orderItem.menuItemId }
                menuItem?.price?.times(orderItem.quantity) ?: 0.0
            }
        }

        // Apply discount
        return basePrice * (1 - discountInPercent / 100.0)
    }
}