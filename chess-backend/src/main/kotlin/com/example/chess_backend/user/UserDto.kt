package com.example.chess_backend.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class RegisterRequest(
    @field:Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    val name: String? = null  // Optional: uses JWT claim if not provided
)

data class CreateUserRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    val name: String,

    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Auth0 ID is required")
    @field:Size(max = 64, message = "Auth0 ID must not exceed 64 characters")
    val auth0Id: String
)

data class UpdateUserRequest(
    @field:Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    val name: String?,

    @field:Email(message = "Invalid email format")
    val email: String?
)

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val auth0Id: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun User.toResponse() = UserResponse(
    id = id ?: 0,
    name = name,
    email = email,
    auth0Id = auth0Id,
    createdAt = createdAt,
    updatedAt = updatedAt
)