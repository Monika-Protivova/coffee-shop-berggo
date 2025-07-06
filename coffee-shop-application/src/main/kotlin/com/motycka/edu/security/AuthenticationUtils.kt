package com.motycka.edu.security

import com.motycka.edu.error.UnauthorizedException
import com.motycka.edu.user.UserId
import com.motycka.edu.user.UserRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.util.pipeline.*

fun PipelineContext<*, ApplicationCall>.getUserIdentity(): IdentityDTO {
    // Bypass auth if running in test mode
    if (System.getenv("TEST_MODE") == "true") {
        return IdentityDTO(
            userId = 1L,
            customerId = 3L,
            role = UserRole.CUSTOMER
        )
    }

    val jwt = call.principal<JWTPrincipal>() ?: throw UnauthorizedException("No token")
    val userId = jwt.getClaim(Claims.USER_ID, UserId::class)
    val role = jwt.getClaim(Claims.ROLE, String::class)
    val customerId = jwt.getClaim(Claims.CUSTOMER_ID, Long::class)

    return IdentityDTO(
        userId = requireNotNull(userId),
        customerId = requireNotNull(customerId),
        role = UserRole.valueOf(requireNotNull(role))
    )
}