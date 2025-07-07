package com.motycka.edu.order

import com.motycka.edu.security.getUserIdentity
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val logger = KotlinLogging.logger {}

private const val ORDER_NOT_FOUND = "Order not found"
private const val INVALID_ID = "Invalid ID format"

fun Route.orderRoutes(
    orderService: OrderService,
    basePath: String
) {
    route("$basePath/orders") {
        // Get all orders
        get {
            logger.info { "GET all orders" }
            call.respond(orderService.getAllOrders())
        }

        // Get order by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, INVALID_ID)
                return@get
            }

            try {
                val order = orderService.getOrderById(id)
                call.respond(order)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.NotFound, ORDER_NOT_FOUND)
            } catch (e: Exception) {
                logger.error(e) { "Error getting order $id" }
                call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            }
        }

        // Create a new order
        post {
            try {
                val orderRequest = call.receive<OrderRequest>()

                // Validate order request
                if (orderRequest.items.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Order must have at least one item")
                    return@post
                }

                // Get customer ID from authenticated user or null if anonymous
                val customerId = try {
                    getUserIdentity().customerId
                } catch (e: Exception) {
                    logger.warn { "No user identity found, creating order as guest: ${e.message}" }
                    null
                }

                // Create the order
                val createdOrder = orderService.createOrder(customerId, orderRequest)
                call.respond(HttpStatusCode.Created, createdOrder)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid order request")
            } catch (e: Exception) {
                logger.error(e) { "Error creating order" }
                call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            }
        }

        // Update order status
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, INVALID_ID)
                return@put
            }

            try {
                val updateRequest = call.receive<OrderUpdateRequest>()
                val updatedOrder = orderService.updateOrder(id, updateRequest)
                call.respond(HttpStatusCode.OK, updatedOrder)
            } catch (e: IllegalArgumentException) {
                if (e.message?.contains("not found") == true) {
                    call.respond(HttpStatusCode.NotFound, ORDER_NOT_FOUND)
                } else {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid update request")
                }
            } catch (e: Exception) {
                logger.error(e) { "Error updating order $id" }
                call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            }
        }
    }
}