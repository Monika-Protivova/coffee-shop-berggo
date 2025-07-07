package com.motycka.edu.order

interface OrderItemRepository {
    fun selectByOrderId(orderId: Long): List<OrderItemDTO>
    fun createOrderItems(orderItems: List<OrderItemDTO>)
}