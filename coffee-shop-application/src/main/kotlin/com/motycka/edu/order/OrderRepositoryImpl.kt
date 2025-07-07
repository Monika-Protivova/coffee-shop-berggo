package com.motycka.edu.order

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class OrderRepositoryImpl(private val database: Database,
                          private val orderItemRepository: OrderItemRepository) : OrderRepository {

    override fun selectAll(): List<OrderDTO> = transaction(database) {
        OrderEntity.all().map { orderEntity ->
            val orderItems = orderItemRepository.selectByOrderId(orderEntity.id.value)
            orderEntity.toDTO(orderItems)
        }
    }

    override fun selectById(id: Long): OrderDTO? = transaction(database) {
        OrderEntity.findById(id)?.let { orderEntity ->
            val orderItems = orderItemRepository.selectByOrderId(orderEntity.id.value)
            orderEntity.toDTO(orderItems)
        }
    }

    override fun create(order: OrderDTO): OrderDTO = transaction(database) {
        val newOrder = OrderEntity.new {
            customerId = order.customerId
            status = order.status
            isPaid = order.isPaid
            totalPrice = order.totalPrice
        }

        // Save the order items
        val orderItemDTOs = order.orderItems.map { item ->
            OrderItemDTO(
                id = null,
                orderId = newOrder.id.value,
                menuItemId = item.menuItemId,
                quantity = item.quantity
            )
        }

        orderItemRepository.createOrderItems(orderItemDTOs)

        // Return the created order with its items
        val createdOrderItems = orderItemRepository.selectByOrderId(newOrder.id.value)
        newOrder.toDTO(createdOrderItems)
    }

    override fun update(order: OrderDTO): OrderDTO = transaction(database) {
        val orderEntity = order.id?.let { OrderEntity.findById(it) }
            ?: throw IllegalArgumentException("Order not found")

        orderEntity.apply {
            status = order.status
            isPaid = order.isPaid
            totalPrice = order.totalPrice
        }

        val orderItems = orderItemRepository.selectByOrderId(orderEntity.id.value)
        orderEntity.toDTO(orderItems)
    }
}