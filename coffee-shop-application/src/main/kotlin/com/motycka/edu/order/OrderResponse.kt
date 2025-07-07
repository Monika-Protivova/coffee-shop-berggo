package com.motycka.edu.order

import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    val id: Long,
    val customerId: Long,
    val menuItems: List<OrderItemResponse>,
    val totalPrice: Double,
    val status: OrderStatus,
    val isPaid: Boolean
)

@Serializable
data class CreateOrderResponse(
    val orderId: Long,
    val totalPrice: Double
)