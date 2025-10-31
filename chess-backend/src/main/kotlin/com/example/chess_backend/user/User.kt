package com.example.chess_backend.user

import jakarta.persistence.*
import jakarta.validation.constraints.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @field:Column(nullable = false, unique = true, updatable = true)
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 50)
    val name: String,

    @field:Column(nullable = false, unique = true, updatable = true)
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:Column(nullable = false, unique = true, updatable = false, length = 64)
    @field:NotBlank(message = "Auth0 ID is required")
    val auth0Id: String,

    @field:CreatedDate
    @field:Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @field:LastModifiedDate
    @field:Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
