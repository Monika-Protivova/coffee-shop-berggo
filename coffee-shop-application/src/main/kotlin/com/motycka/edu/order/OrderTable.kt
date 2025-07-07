package com.motycka.edu.order

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable


object OrderTable : LongIdTable("orders") {
    val customerId = long("customer_id")
    val status = enumerationByName("status", 20, OrderStatus::class)
    val isPaid = bool("is_paid").default(false)
    val totalPrice = double("total_price").default(0.0)
}

class OrderEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<OrderEntity>(OrderTable)

    var customerId by OrderTable.customerId
    var status by OrderTable.status
    var isPaid by OrderTable.isPaid
    var totalPrice by OrderTable.totalPrice

    fun toDTO(orderItems: List<OrderItemDTO>): OrderDTO = OrderDTO(
        id = id.value,
        customerId = customerId,
        orderItems = orderItems,
        totalPrice = totalPrice,
        status = status,
        isPaid = isPaid
    )
}