package com.example.chessandroid.data.api.dto

import kotlinx.serialization.Serializable

/**
 * DTOs matching the Spring Boot backend API responses
 */

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val pageable: PageableDto,
    val totalPages: Int,
    val totalElements: Long,
    val last: Boolean,
    val first: Boolean,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val empty: Boolean
)

@Serializable
data class PageableDto(
    val sort: SortDto,
    val offset: Long,
    val pageNumber: Int,
    val pageSize: Int,
    val paged: Boolean,
    val unpaged: Boolean
)

@Serializable
data class SortDto(
    val empty: Boolean,
    val sorted: Boolean,
    val unsorted: Boolean
)

@Serializable
data class MatchResponseDto(
    val id: Int,
    val whitePlayerId: Int,
    val whitePlayerName: String,
    val blackPlayerId: Int,
    val blackPlayerName: String,
    val status: String, // "PENDING", "IN_PROGRESS", "COMPLETED", "ABANDONED"
    val result: String, // "WHITE_WIN", "BLACK_WIN", "DRAW", "ONGOING"
    val createdAt: String, // ISO-8601 format
    val updatedAt: String,  // ISO-8601 format
    val isPlayerWhite: Boolean
)

@Serializable
data class UserResponseDto(
    val id: Int,
    val name: String,
    val email: String,
    val auth0Id: String,
    val createdAt: String, // ISO-8601 format
    val updatedAt: String  // ISO-8601 format
)