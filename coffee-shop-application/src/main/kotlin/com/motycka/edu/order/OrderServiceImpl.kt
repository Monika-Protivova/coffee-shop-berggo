package com.motycka.edu.order

import com.motycka.edu.customer.CustomerRepository
import com.motycka.edu.menu.InternalMenuService
import com.motycka.edu.menu.MenuItemResponse
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val menuService: InternalMenuService,
    private val customerRepository: CustomerRepository
) : OrderService {

    override suspend fun getAllOrders(): List<OrderResponse> {
        logger.info { "Getting all orders" }
        return orderRepository.selectAll().map { it.toOrderResponse(menuService) }
    }

    override suspend fun getOrderById(id: Long): OrderResponse {
        logger.info { "Getting order by id: $id" }
        return orderRepository.selectById(id)?.toOrderResponse(menuService)
            ?: throw IllegalArgumentException("Order not found: $id")
    }

    override suspend fun createOrder(customerId: Long?, request: OrderRequest): CreateOrderResponse {
        if (request.items.isEmpty()) {
            throw IllegalArgumentException("Order must have at least one item")
        }

        // Get customer discount if customer ID is provided
        val customerDiscount = customerId?.let {
            customerRepository.selectCustomer(it)?.discountPercent ?: 0.0
        } ?: 0.0

        // Get menu items for the requested items
        val menuItemIds = request.items.map { it.menuItemId }
        val menuItems = menuService.getMenuItemsByIds(menuItemIds)

        // Calculate price with discount
        val totalPrice = PriceCalculator.calculatePrice(
            menuItems = menuItems,
            discountInPercent = customerDiscount,
            orderItems = request.items.map {
                OrderItemDTO(
                    id = null,
                    orderId = 0, // Will be set later when order is created
                    menuItemId = it.menuItemId,
                    quantity = it.quantity
                )
            }
        )

        // Create the order
        val orderDTO = OrderDTO(
            id = null,
            customerId = customerId ?: 0, // Anonymous order if no customer ID
            orderItems = request.items.map {
                OrderItemDTO(
                    id = null,
                    orderId = 0, // Will be set by repository
                    menuItemId = it.menuItemId,
                    quantity = it.quantity
                )
            },
            totalPrice = totalPrice,
            status = OrderStatus.PENDING,
            isPaid = false
        )

        val createdOrder = orderRepository.create(orderDTO)

        return CreateOrderResponse(
            orderId = createdOrder.id!!,
            totalPrice = createdOrder.totalPrice
        )
    }

    override suspend fun updateOrder(id: Long, request: OrderUpdateRequest): OrderResponse {
        // Get the existing order
        val existingOrder = orderRepository.selectById(id)
            ?: throw IllegalArgumentException("Order not found: $id")

        // Update status
        val updatedOrder = existingOrder.copy(
            status = request.status,
            isPaid = if (request.status == OrderStatus.PAID) true else existingOrder.isPaid
        )

        // Save updated order
        val savedOrder = orderRepository.update(updatedOrder)

        return savedOrder.toOrderResponse(menuService)
    }
}

// Extension function to convert OrderDTO to OrderResponse
private suspend fun OrderDTO.toOrderResponse(menuService: InternalMenuService): OrderResponse {
    val menuItems = menuService.getMenuItemsByIds(this.orderItems.map { it.menuItemId })

    return OrderResponse(
        id = this.id!!,
        customerId = this.customerId,
        menuItems = this.orderItems.map { orderItem ->
            val menuItem = menuItems.find { it.id == orderItem.menuItemId }!!
            OrderItemResponse(
                menuItem = MenuItemResponse(
                    id = menuItem.id!!,
                    name = menuItem.name,
                    description = menuItem.description,
                    price = menuItem.price
                ),
                quantity = orderItem.quantity
            )
        },
        totalPrice = this.totalPrice,
        status = this.status,
        isPaid = this.isPaid
    )
}