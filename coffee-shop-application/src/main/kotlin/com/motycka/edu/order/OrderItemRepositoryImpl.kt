package com.motycka.edu.order

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class OrderItemRepositoryImpl(private val database: Database) : OrderItemRepository {

    override fun selectByOrderId(orderId: Long): List<OrderItemDTO> = transaction(database) {
        OrderItemDAO.find { OrderItemTable.orderId eq orderId }.map { it.toDTO() }
    }

    override fun createOrderItems(orderItems: List<OrderItemDTO>) = transaction(database) {
        orderItems.forEach { item ->
            OrderItemDAO.new {
                this.orderId = item.orderId
                this.menuItemId = item.menuItemId
                this.quantity = item.quantity
            }
        }
    }

    fun findByOrderId(orderId: Long): List<OrderItemDTO> = selectByOrderId(orderId)

    fun create(orderItem: OrderItemDTO): OrderItemDTO = transaction(database) {
        val newItem = OrderItemDAO.new {
            this.orderId = orderItem.orderId
            this.menuItemId = orderItem.menuItemId
            this.quantity = orderItem.quantity
        }
        newItem.toDTO()
    }
}