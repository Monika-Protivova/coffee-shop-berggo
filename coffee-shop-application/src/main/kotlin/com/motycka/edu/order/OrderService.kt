package com.motycka.edu.order

interface OrderService {
    suspend fun getAllOrders(): List<OrderResponse>
    suspend fun getOrderById(id: Long): OrderResponse
    suspend fun createOrder(customerId: Long?, request: OrderRequest): CreateOrderResponse
    suspend fun updateOrder(id: Long, request: OrderUpdateRequest): OrderResponse
}